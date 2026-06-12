package edu.feutech.redu.capture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.ReduApp
import edu.feutech.redu.data.ReliabilityEventEntity
import edu.feutech.redu.data.ReliabilityEventType
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptEventEntity
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.PromptTrigger
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.data.isTrackingEnabled
import edu.feutech.redu.debug.DebugOverlayController
import edu.feutech.redu.prompt.PromptPolicy
import edu.feutech.redu.prompt.PromptPresentationEvent
import edu.feutech.redu.prompt.PromptPresenter
import edu.feutech.redu.risk.FuzzyRiskEngine
import edu.feutech.redu.risk.RiskInputs
import edu.feutech.redu.risk.RiskMembershipConfig
import edu.feutech.redu.risk.RiskPersonalization
import edu.feutech.redu.sentiment.DebugTokenBreakdown
import edu.feutech.redu.sentiment.MvlLexicon
import edu.feutech.redu.sentiment.VADERCompatibleAnalyzer
import edu.feutech.redu.sentiment.SentimentResult
import edu.feutech.redu.sentiment.VisualSentimentLabel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.coroutines.resume

private const val VLM_INFERENCE_TIMEOUT_MILLIS = 25_000L
private const val VLM_CAPTURE_SIZE_PX = 448
private const val TEARDOWN_TIMEOUT_MILLIS = 5_000L

class ReduAccessibilityService : AccessibilityService() {
    companion object {
        const val ACTION_DEMO_PROMPT = "edu.feutech.redu.ACTION_DEMO_PROMPT"
        const val ACTION_CLEAR_ACTIVE_CAPTURE_STATE = "edu.feutech.redu.ACTION_CLEAR_ACTIVE_CAPTURE_STATE"
        const val EXTRA_PROMPT_LEVEL = "prompt_level"
        private const val BLANK_ITEM_DEBOUNCE_MILLIS = 250L
    }
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(serviceJob + Dispatchers.IO)
    private val trackerExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "ReduSessionTracker")
    }
    private val trackerDispatcher: ExecutorCoroutineDispatcher = trackerExecutor.asCoroutineDispatcher()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                targetInForeground = false
                commentSheetSurfaceResolver.onTargetExit()
                cancelPendingVlm("screen off")
                if (::debugOverlayBridge.isInitialized) debugOverlayBridge.setTargetInForeground(false)
                pendingTargetExitJob?.cancel()
                scope.launch {
                    trackTargetBackground()
                    finalizeNow()
                }
                clearDebugOverlay()
            }
        }
    }
    private val demoPromptReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_DEMO_PROMPT) return
            val levelName = intent.getStringExtra(EXTRA_PROMPT_LEVEL) ?: return
            val level = runCatching { enumValueOf<PromptLevel>(levelName) }.getOrNull() ?: return
            mainHandler.post {
                PromptPresenter.show(
                    service = this@ReduAccessibilityService,
                    level = level,
                    score = 55.0,
                )
            }
        }
    }
    private val clearActiveCaptureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_CLEAR_ACTIVE_CAPTURE_STATE) return
            discardActiveSession("history cleared", waitForTracker = true)
        }
    }
    private val tracker = SessionTracker()
    private val analyzer by lazy {
        VADERCompatibleAnalyzer.fromAsset(
            context = this,
            extensionLexicon = MvlLexicon.extensionLexicon,
        )
    }

    private val commentSheetSurfaceResolver = CommentSheetSurfaceResolver()
    private val visualSentimentResolverDelegate = lazy {
        edu.feutech.redu.sentiment.NativeVisualSentimentResolver(
            app.modelDownloadManager,
        )
    }
    private val visualSentimentResolver: edu.feutech.redu.sentiment.VisualSentimentResolver
        by visualSentimentResolverDelegate
    private val promptPolicy = PromptPolicy()
    private lateinit var debugOverlay: DebugOverlayController
    private lateinit var debugOverlayBridge: DebugOverlayBridge
    private lateinit var app: ReduApp
    @Volatile private var targetInForeground = false
    private var pendingTargetExitJob: Job? = null
    @Volatile private var debugOverlayEnabled = false
    @Volatile private var studyGroup = StudyGroup.INTERVENTION
    @Volatile private var promptsEnabled = true
    @Volatile private var enabledPlatforms: Set<Platform> = emptySet()
    @Volatile private var liveRiskMembershipConfig = RiskMembershipConfig.Fixed

    @Volatile private var lastObservedTransitionFingerprint: String? = null
    @Volatile private var lastObservedItemHasText = true
    @Volatile private var blankNoTextSequence = 0
    @Volatile private var lastBlankNoTextSequenceIncrementAtMillis = 0L
    @Volatile private var lastSuppressedPromptKey: SuppressedPromptKey? = null
    @Volatile private var currentVlmRequest: VlmRequestToken? = null
    private var lastProcessTimeMillis = 0L
    private var pendingProcessingJob: Job? = null
    private var followUpProcessingJob: Job? = null

    // On-demand VLM screenshot capture via AccessibilityService.takeScreenshot.
    @Volatile private var vlmCaptureJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        app = application as ReduApp
        debugOverlay = DebugOverlayController(this)
        debugOverlayBridge = DebugOverlayBridge(
            overlay = debugOverlay,
            scope = scope,
            mainHandler = mainHandler,
            trackerDispatcher = trackerDispatcher,
            snapshotProvider = { tracker.snapshot() },
            debugBuild = BuildConfig.DEBUG,
        )
        ContextCompat.registerReceiver(
            this,
            screenStateReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        ContextCompat.registerReceiver(
            this,
            clearActiveCaptureReceiver,
            IntentFilter(ACTION_CLEAR_ACTIVE_CAPTURE_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        if (BuildConfig.DEBUG) {
            ContextCompat.registerReceiver(
                this,
                demoPromptReceiver,
                IntentFilter(ACTION_DEMO_PROMPT),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
        observeSettings()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            // Keep the framework package filter disabled in all variants:
            // non-target events are required to detect when a participant
            // leaves a monitored app. REDU filters target packages in code.
            packageNames = null
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        logReliability(ReliabilityEventType.SERVICE_STARTED, "service_connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val platform = PlatformAdapterRegistry.platformFor(event.packageName)
        val targetEvent = platform != null
        val foregroundChanged = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        if (!targetEvent || platform !in enabledPlatforms) {
            handleNonTargetEvent(event)
            return
        }

        val isUserInteraction = event.eventType.isUserInteractionEvent()
        val eventPackageName = event.packageName?.toString() ?: platform.name
        triggerScreenProcessing(platform, eventPackageName, foregroundChanged, isUserInteraction)
    }

    private fun handleNonTargetEvent(event: AccessibilityEvent) {
        if (!targetInForeground) return
        if (event.packageName?.toString() == packageName) return
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            commentSheetSurfaceResolver.onTargetExit()
            cancelPendingVlm("target exit")
            scheduleTargetExit()
            return
        }

        if (isActiveRootTargetSurface()) return
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
            eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START ||
            eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END
        ) {
            commentSheetSurfaceResolver.onTargetExit()
            cancelPendingVlm("target exit")
            scheduleTargetExit()
        }
    }

    private fun isActiveRootTargetSurface(): Boolean {
        val root = rootInActiveWindow ?: return false
        try {
            val platform = PlatformAdapterRegistry.platformFor(root.packageName) ?: return false
            if (platform !in enabledPlatforms) return false
            val directSupported = PlatformAdapterRegistry.isSupportedSurface(platform, root)
            val commentSheet = !directSupported && PlatformAdapterRegistry.isCommentSheetSurface(platform, root)
            return commentSheetSurfaceResolver.resolve(
                rootWindowId = root.windowId,
                directSupported = directSupported,
                commentSheet = commentSheet,
                platformName = platform.name,
                multiWindowSupported = {
                    val roots = try {
                        windows?.mapNotNull { it.root } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    try {
                        PlatformAdapterRegistry.isSupportedSurface(platform, roots)
                    } finally {
                        roots.forEach { if (it != root) it.recycle() }
                    }
                },
            )
        } finally {
            root.recycle()
        }
    }

    private fun triggerScreenProcessing(platform: Platform, packageName: String, windowChanged: Boolean, isUserInteraction: Boolean = false) {
        val now = System.currentTimeMillis()
        val timeSinceLastProcess = now - lastProcessTimeMillis
        if (windowChanged || timeSinceLastProcess >= 300L) {
            pendingProcessingJob?.cancel()
            lastProcessTimeMillis = now
            processScreen(platform, packageName, windowChanged, isUserInteraction)
        } else {
            pendingProcessingJob?.cancel()
            pendingProcessingJob = scope.launch(Dispatchers.Main) {
                delay(200L)
                lastProcessTimeMillis = System.currentTimeMillis()
                processScreen(platform, packageName, windowChanged, isUserInteraction)
            }
        }

        // Schedule a follow-up processing check 800ms later to handle delayed layout/rendering
        followUpProcessingJob?.cancel()
        followUpProcessingJob = scope.launch(Dispatchers.Main) {
            delay(800L)
            processScreen(platform, packageName, false)
        }
    }

    private fun processScreen(eventPlatform: Platform, packageName: String, windowChanged: Boolean, isUserInteraction: Boolean = false) {
        val root = rootInActiveWindow ?: return
        try {
            val activeRootPlatform = PlatformAdapterRegistry.platformFor(root.packageName)
            if (activeRootPlatform == null) {
                if (windowChanged && targetInForeground) {
                    cancelPendingVlm("non-target window")
                    scheduleTargetExit()
                }
                return
            }
            val platform = activeRootPlatform
            if (platform !in enabledPlatforms) {
                if (targetInForeground) {
                    discardActiveSession("platform disabled")
                }
                return
            }
            val activePackageName = root.packageName?.toString() ?: packageName
            debugLog(
                "REDU_DEBUG",
                "processScreen activePlatform=$platform eventPlatform=$eventPlatform rootPackageMatches=${root.packageName == activePackageName}",
            )
            val directSupported = PlatformAdapterRegistry.isSupportedSurface(platform, root)
            val commentSheet = !directSupported && PlatformAdapterRegistry.isCommentSheetSurface(platform, root)
            val supported = commentSheetSurfaceResolver.resolve(
                rootWindowId = root.windowId,
                directSupported = directSupported,
                commentSheet = commentSheet,
                platformName = platform.name,
                multiWindowSupported = {
                    val roots = try {
                        windows?.mapNotNull { it.root } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    try {
                        PlatformAdapterRegistry.isSupportedSurface(platform, roots)
                    } finally {
                        roots.forEach { if (it != root) it.recycle() }
                    }
                },
            )
            if (!supported) {
                debugLog("REDU_DEBUG", "unsupported surface platform=$platform commentSheet=$commentSheet")
            }

            debugLog("REDU_DEBUG", "processScreen supported=$supported platform=$platform windowChanged=$windowChanged")
            if (!supported) {
                if (targetInForeground) {
                    cancelPendingVlm("unsupported surface")
                    scheduleTargetExit()
                }
                return
            }

            // Surface is confirmed as supported (Reels) — now manage foreground/session state
            pendingTargetExitJob?.cancel()
            pendingTargetExitJob = null
            if (windowChanged) {
                logReliability(
                    ReliabilityEventType.TARGET_FOREGROUND,
                    "${platform.name.lowercase()}_foreground",
                    platform = platform,
                )
            }

            val extraction = PlatformAdapterRegistry.extract(platform, root, reelsSurfaceConfirmed = supported)
            val visibleText = extraction.sentimentText
            val transitionFingerprint = extraction.transitionText.normalizeForFingerprint()

            val debugRoot = if (BuildConfig.DEBUG) AccessibilityNodeInfo.obtain(root) else null
            val debugPackageName = activePackageName
            val hasCaptionContent = extraction.hasCaptionContent

            scope.launch(Dispatchers.Default) {
                var debugRootForOverlay: AccessibilityNodeInfo? = debugRoot
                try {
                    val sentiment = visibleText.takeIf { it.isNotBlank() }?.let(analyzer::analyze)
                    val trackerUpdate = updateTrackerForScreen(
                        platform = platform,
                        isUserInteraction = isUserInteraction,
                        transitionFingerprint = transitionFingerprint,
                        hasCaptionContent = hasCaptionContent,
                        visibleText = visibleText,
                        sentiment = sentiment,
                    )
                    if (trackerUpdate.finalizedSession != null) {
                        commentSheetSurfaceResolver.onTargetExit()
                    }
                    trackerUpdate.finalizedSession?.let { finalized ->
                        persistFinalized(finalized)
                    }
                    trackerUpdate.vlmRequest?.let(::scheduleVlmCapture)
                    val snapshot = trackerUpdate.snapshot ?: return@launch
                    val debugTokenBreakdown = visibleText
                        .takeIf { it.isNotBlank() }
                        ?.let(analyzer::classifyTokensForDebug)
                        ?: emptyDebugTokenBreakdown()

                    if (debugOverlayEnabled) {
                        debugOverlayBridge.update(
                            snapshot = snapshot,
                            vaderCompound = sentiment?.compound,
                            tokenBreakdown = debugTokenBreakdown,
                            root = debugRootForOverlay,
                            packageName = debugPackageName,
                        )
                        debugRootForOverlay = null
                    } else {
                        debugOverlayBridge.clear()
                    }

                    if (studyGroup == StudyGroup.CONTROL || !promptsEnabled) return@launch
                    // NSD may only drive prompts once enough sentiment units resolved;
                    // tiny samples (e.g. 1 negative of 2) fall back to dwell+duration rules.
                    val nsdEvidenceSufficient = snapshot.resolvableUnits >= PromptPolicy.MIN_NSD_EVIDENCE_UNITS
                    val promptNsdPercent = snapshot.nsdPercent?.takeIf { nsdEvidenceSufficient }
                    val promptReliability = if (nsdEvidenceSufficient) {
                        snapshot.sentimentReliability
                    } else {
                        SentimentReliability.SENTIMENT_UNRELIABLE
                    }
                    val livePromptRisk = FuzzyRiskEngine.evaluate(
                        RiskInputs(
                            meanDwellSeconds = snapshot.meanDwellMillis / 1000.0,
                            nsdPercent = promptNsdPercent,
                            sessionDurationMinutes = snapshot.durationMillis / 60_000.0,
                            sentimentReliable = promptReliability == SentimentReliability.RELIABLE,
                        ),
                        membershipConfig = liveRiskMembershipConfig,
                    )
                    val prompt = promptPolicy.decide(
                        score = livePromptRisk.score,
                        riskLevel = livePromptRisk.level,
                        reliability = promptReliability,
                        sessionDurationMillis = snapshot.durationMillis,
                        nsdPercent = promptNsdPercent,
                        sessionKey = snapshot.startedAtMillis,
                    )
                    if (prompt.cooldownActive) {
                        logSuppressedPromptIfNeeded(snapshot, livePromptRisk.score, livePromptRisk.level)
                    }
                    if (prompt.shouldShow) {
                        lastSuppressedPromptKey = null
                        mainHandler.post {
                            PromptPresenter.show(
                                service = this@ReduAccessibilityService,
                                level = prompt.level,
                                score = livePromptRisk.score,
                                onEvent = { event ->
                                    handlePromptEvent(
                                        event = event,
                                        level = prompt.level,
                                        riskScore = livePromptRisk.score,
                                        riskLevel = livePromptRisk.level,
                                        cooldownActive = prompt.cooldownActive,
                                        trigger = prompt.trigger,
                                    )
                                }
                            )
                        }
                    }
                } finally {
                    debugRootForOverlay?.recycle()
                }
            }
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {
        scope.launch { finalizeNow() }
    }

    override fun onDestroy() {
        pendingTargetExitJob?.cancel()
        pendingProcessingJob?.cancel()
        followUpProcessingJob?.cancel()
        cancelPendingVlm("service destroyed")
        PromptPresenter.dismissActivePrompt()
        runCatching { unregisterReceiver(screenStateReceiver) }
        runCatching { unregisterReceiver(clearActiveCaptureReceiver) }
        if (BuildConfig.DEBUG) {
            runCatching { unregisterReceiver(demoPromptReceiver) }
        }
        runBlocking(Dispatchers.IO) {
            withTimeoutOrNull(TEARDOWN_TIMEOUT_MILLIS) {
                finalizeNow()
                insertReliability(ReliabilityEventType.SERVICE_STOPPED, "service_destroyed")
            }
        }
        serviceJob.cancel()
        if (::debugOverlayBridge.isInitialized) debugOverlayBridge.destroy()
        trackerDispatcher.close()
        trackerExecutor.shutdown()
        if (visualSentimentResolverDelegate.isInitialized()) {
            // Close native VLM models off the service thread so teardown
            // cannot block on JNI cleanup.
            Thread({
                runCatching { runBlocking { visualSentimentResolver.close() } }
            }, "ReduVlmClose").start()
        }
        super.onDestroy()
    }

    private fun observeSettings() {
        scope.launch {
            app.database.settingsDao().observe().collectLatest { settings ->
                debugOverlayEnabled = settings?.debugOverlayEnabled == true
                studyGroup = settings?.studyGroup ?: StudyGroup.INTERVENTION
                promptsEnabled = settings?.promptsEnabled == true && studyGroup == StudyGroup.INTERVENTION
                val nextEnabledPlatforms = Platform.entries.filterTo(mutableSetOf()) {
                    settings?.isTrackingEnabled(it) == true
                }
                val activePlatform = withContext(trackerDispatcher) { tracker.snapshot()?.platform }
                enabledPlatforms = nextEnabledPlatforms
                if (activePlatform != null && activePlatform !in nextEnabledPlatforms) {
                    discardActiveSession("platform tracking disabled", waitForTracker = true)
                }
                val studyCode = settings?.studyCode?.takeIf { it.isNotBlank() } ?: "UNSET"
                val personalization = if (promptsEnabled) {
                    app.database.riskPersonalizationDao().getFor(studyCode, studyGroup)
                } else {
                    null
                }
                liveRiskMembershipConfig = RiskPersonalization.configFor(personalization)
                if (::debugOverlayBridge.isInitialized) {
                    debugOverlayBridge.setEnabled(debugOverlayEnabled)
                }
                if (!debugOverlayEnabled) {
                    clearDebugOverlay()
                }
            }
        }
    }

    private fun scheduleTargetExit() {
        pendingTargetExitJob?.cancel()
        pendingProcessingJob?.cancel()
        followUpProcessingJob?.cancel()
        cancelPendingVlm("target exit")
        targetInForeground = false
        debugOverlayBridge.setTargetInForeground(false)
        removeDebugOverlay()
        scope.launch {
            val snapshot = trackTargetBackground()
            logReliability(
                ReliabilityEventType.TARGET_BACKGROUND,
                "target_background_bridge_started",
                platform = snapshot?.platform,
            )
        }
        
        pendingTargetExitJob = scope.launch {
            delay(SessionTracker.BRIDGE_WINDOW_MILLIS)
            if (isTargetWindowStillAvailable()) {
                pendingTargetExitJob = null
                val snapshot = withContext(trackerDispatcher) {
                    val platform = tracker.snapshot()?.platform ?: return@withContext null
                    tracker.onTargetForeground(platform)
                    tracker.snapshot()
                }
                targetInForeground = snapshot != null
                if (snapshot != null) {
                    debugOverlayBridge.setTargetInForeground(true)
                }
                return@launch
            }
            commentSheetSurfaceResolver.onTargetExit()
            finalizeInactive()
            removeDebugOverlay()
        }
    }

    private fun discardActiveSession(reason: String, waitForTracker: Boolean = false) {
        pendingTargetExitJob?.cancel()
        pendingProcessingJob?.cancel()
        followUpProcessingJob?.cancel()
        cancelPendingVlm(reason)
        commentSheetSurfaceResolver.onTargetExit()
        targetInForeground = false
        if (::debugOverlayBridge.isInitialized) {
            debugOverlayBridge.setTargetInForeground(false)
        }
        removeDebugOverlay()
        PromptPresenter.dismissActivePrompt()
        if (waitForTracker) {
            runBlocking(trackerDispatcher) {
                resetPerItemStateLocked()
                tracker.discardActive()
            }
        } else {
            scope.launch(trackerDispatcher) {
                resetPerItemStateLocked()
                tracker.discardActive()
            }
        }
    }

    private suspend fun finalizeInactive() {
        val finalized = withContext(trackerDispatcher) {
            tracker.finalizeIfInactive()
        }
        if (finalized != null) {
            persistFinalized(finalized)
            withContext(trackerDispatcher) {
                resetPerItemState()
            }
        }
    }

    private suspend fun finalizeNow() {
        val finalized = withContext(trackerDispatcher) {
            tracker.forceFinalize()
        }
        if (finalized != null) {
            persistFinalized(finalized)
            withContext(trackerDispatcher) {
                resetPerItemState()
            }
        }
    }

    /**
     * Captures a screenshot using [AccessibilityService.takeScreenshot] (API 30+)
     * and returns it as a JPEG byte array. Returns null on older devices or failure.
     */
    private suspend fun captureScreenshotAsBytes(): ByteArray? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return try {
            suspendCancellableCoroutine { cont ->
                fun resumeCapture(value: ByteArray?) {
                    if (cont.isActive) cont.resume(value)
                }
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshot: ScreenshotResult) {
                            try {
                                val hwBuffer = screenshot.hardwareBuffer
                                val bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, screenshot.colorSpace)
                                hwBuffer.close()
                                if (bitmap != null) {
                                    // Convert hardware bitmap to software, then letterbox before VLM.
                                    val swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                                    bitmap.recycle()
                                    val vlmBitmap = swBitmap.fitCenterSquare(VLM_CAPTURE_SIZE_PX)
                                    if (vlmBitmap != swBitmap) {
                                        swBitmap.recycle()
                                    }
                                    val stream = ByteArrayOutputStream()
                                    vlmBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                                    vlmBitmap.recycle()
                                    resumeCapture(stream.toByteArray())
                                } else {
                                    resumeCapture(null)
                                }
                            } catch (e: Exception) {
                                resumeCapture(null)
                            }
                        }

                        override fun onFailure(errorCode: Int) {
                            debugLog("REDU_VLM", "takeScreenshot failed errorCode=$errorCode")
                            resumeCapture(null)
                        }
                    },
                )
            }
        } catch (e: Exception) {
            debugLog("REDU_VLM", "captureScreenshot exception=${e::class.simpleName}")
            null
        }
    }

    private fun Bitmap.fitCenterSquare(sizePx: Int): Bitmap {
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.BLACK)
        val scale = minOf(sizePx.toFloat() / width.toFloat(), sizePx.toFloat() / height.toFloat())
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val left = (sizePx - scaledWidth) / 2f
        val top = (sizePx - scaledHeight) / 2f
        val dest = RectF(left, top, left + scaledWidth, top + scaledHeight)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        canvas.drawBitmap(this, null, dest, paint)
        return output
    }

    private suspend fun persistFinalized(finalized: FinalizedSession) {
        clearDebugOverlay()
        val settings = app.database.settingsDao().get()
        val studyCode = settings?.studyCode?.takeIf { it.isNotBlank() } ?: "UNSET"
        val studyGroup = settings?.studyGroup ?: StudyGroup.INTERVENTION
        val snapshot = finalized.snapshot
        val sessionId = app.database.sessionDao().insert(
            SessionEntity(
                studyCode = studyCode,
                studyGroup = studyGroup,
                platform = snapshot.platform,
                startedAtMillis = snapshot.startedAtMillis,
                endedAtMillis = finalized.endedAtMillis,
                rawDurationMillis = snapshot.durationMillis,
                promptExcludedDurationMillis = snapshot.promptExcludedDurationMillis,
                meanDwellMillis = snapshot.meanDwellMillis,
                swipeCount = snapshot.swipeCount,
                resolvableUnits = snapshot.resolvableUnits,
                negativeUnits = snapshot.negativeUnits,
                oovRatio = snapshot.oovRatio,
                nsdPercent = snapshot.nsdPercent,
                riskScore = snapshot.riskScore,
                riskLevel = snapshot.riskLevel,
                sentimentReliability = snapshot.sentimentReliability,
            ),
        )
        if (snapshot.sentimentReliability == SentimentReliability.SENTIMENT_UNRELIABLE && snapshot.oovRatio >= 0.50) {
            insertReliability(ReliabilityEventType.HIGH_OOV, "sentiment_unreliable", sessionId, snapshot.platform)
        }
        app.database.promptEventDao().attachPendingToSession(
            sessionId = sessionId,
            startedAtMillis = snapshot.startedAtMillis,
            endedAtMillis = finalized.endedAtMillis,
        )
        app.database.reliabilityEventDao().attachPendingVlmUnresolvedToSession(
            sessionId = sessionId,
            platform = snapshot.platform,
            startedAtMillis = snapshot.startedAtMillis,
            endedAtMillis = finalized.endedAtMillis,
        )
        insertReliability(ReliabilityEventType.SESSION_FINALIZED, "session_saved", sessionId, snapshot.platform)
    }

    private fun clearDebugOverlay() {
        if (::debugOverlayBridge.isInitialized) debugOverlayBridge.clear()
    }

    private fun removeDebugOverlay() {
        if (::debugOverlayBridge.isInitialized) debugOverlayBridge.remove()
    }

    private suspend fun trackTargetBackground(): ActiveSessionSnapshot? =
        withContext(trackerDispatcher) {
            tracker.onTargetBackground()
            tracker.snapshot()
        }

    private fun cancelPendingVlm(reason: String) {
        vlmCaptureJob?.cancel()
        vlmCaptureJob = null
        currentVlmRequest = null
        if (::debugOverlayBridge.isInitialized) {
            debugOverlayBridge.updateVlm(status = "canceled")
        }
        debugLog("REDU_VLM", "canceled pending capture reason=$reason")
    }

    private suspend fun updateTrackerForScreen(
        platform: Platform,
        isUserInteraction: Boolean,
        transitionFingerprint: String,
        hasCaptionContent: Boolean,
        visibleText: String,
        sentiment: SentimentResult?,
    ): TrackerScreenUpdate = withContext(trackerDispatcher) {
        targetInForeground = true
        debugOverlayBridge.setTargetInForeground(true)
        val previousSnapshot = tracker.snapshot()
        val finalizedByPlatformSwitch = tracker.onTargetForeground(platform)
        val platformSwitched = finalizedByPlatformSwitch != null
        if (platformSwitched) {
            resetPerItemStateLocked()
            debugOverlayBridge.updateVlm(framesCaptured = 0, lastLabel = "\u2014", status = "idle")
        }
        if (isUserInteraction) {
            tracker.onUserInteraction()
        }

        debugLog(
            "REDU_VLM",
            "cycle platform=$platform hasCaption=$hasCaptionContent textLength=${visibleText.length} transitionLength=${transitionFingerprint.length} previousPlatform=${previousSnapshot?.platform}",
        )

        val noTextBlankItem = !hasCaptionContent && visibleText.isBlank() && transitionFingerprint.isBlank()
        val effectiveTransitionFingerprint = if (noTextBlankItem) {
            blankNoTextFingerprint(
                platform = platform,
                sessionStartedAtMillis = tracker.snapshot()?.startedAtMillis,
                advanceForInteraction = isUserInteraction,
            )
        } else {
            transitionFingerprint
        }
        val normalizedTransition = effectiveTransitionFingerprint.takeIf { it.isNotBlank() }
        val firstNoTextItem = !hasCaptionContent &&
            visibleText.isBlank() &&
            lastObservedTransitionFingerprint == null &&
            currentVlmRequest == null
        val isTransition = (effectiveTransitionFingerprint != lastObservedTransitionFingerprint &&
            (effectiveTransitionFingerprint.isNotBlank() || lastObservedTransitionFingerprint != null)) ||
            firstNoTextItem
        var vlmRequest: VlmRequestToken? = null
        if (isTransition) {
            vlmCaptureJob?.cancel()
            lastObservedTransitionFingerprint = effectiveTransitionFingerprint
            lastObservedItemHasText = false
            debugOverlayBridge.updateVlm(framesCaptured = 0, status = "waiting")
            vlmRequest = tracker.snapshot()?.let { snapshot ->
                VlmRequestToken(
                    platform = snapshot.platform,
                    sessionStartedAtMillis = snapshot.startedAtMillis,
                    transitionFingerprint = effectiveTransitionFingerprint,
                )
            }
            currentVlmRequest = vlmRequest
            debugLog("REDU_VLM", "transition scheduling capture fingerprint=$effectiveTransitionFingerprint")
        }

        if (hasCaptionContent) {
            lastObservedItemHasText = true
            currentVlmRequest = null
            vlmCaptureJob?.cancel()
            vlmCaptureJob = null
            debugOverlayBridge.updateVlm(status = "skipped (has text)")
            debugLog("REDU_VLM", "skipped capture because caption text exists textLength=${visibleText.length}")
        }

        if (visibleText.isNotBlank() || normalizedTransition != null) {
            tracker.onContentObserved(
                transitionFingerprint = normalizedTransition,
                sentiment = sentiment,
                sentimentFingerprint = visibleText.normalizeForFingerprint().takeIf { it.isNotBlank() },
            )
        }

        TrackerScreenUpdate(
            snapshot = tracker.snapshot(),
            vlmRequest = vlmRequest.takeUnless { hasCaptionContent },
            finalizedSession = finalizedByPlatformSwitch,
        )
    }

    private fun scheduleVlmCapture(token: VlmRequestToken) {
        vlmCaptureJob?.cancel()
        vlmCaptureJob = scope.launch {
            delay(2_000L)
            val shouldCapture = withContext(trackerDispatcher) {
                currentVlmRequest == token && !lastObservedItemHasText && tracker.snapshot()?.matches(token) == true
            }
            if (!shouldCapture) {
                debugOverlayBridge.updateVlm(status = "stale")
                debugLog("REDU_VLM", "skipped stale request")
                return@launch
            }
            if (!isActiveDirectTargetSurface(token.platform)) {
                debugOverlayBridge.updateVlm(status = "stale")
                debugLog("REDU_VLM", "skipped stale surface platform=${token.platform}")
                return@launch
            }

            debugOverlayBridge.updateVlm(status = "warming up")
            val warmedUp = try {
                visualSentimentResolver.warmUp()
            } catch (e: CancellationException) {
                debugOverlayBridge.updateVlm(status = "canceled")
                debugLog("REDU_VLM", "warm-up canceled")
                throw e
            }
            if (!warmedUp) {
                debugOverlayBridge.updateVlm(status = "models unavailable")
                debugLog("REDU_VLM", "warm-up failed")
                logReliability(ReliabilityEventType.VLM_UNRESOLVED, "vlm_models_unavailable", platform = token.platform)
                return@launch
            }

            debugOverlayBridge.updateVlm(status = "capturing")
            debugLog("REDU_VLM", "capturing screenshot")
            val frame = captureScreenshotAsBytes()
            if (frame == null) {
                debugOverlayBridge.updateVlm(framesCaptured = 0, status = "no frames captured")
                logReliability(ReliabilityEventType.VLM_UNRESOLVED, "vlm_frame_capture_failed", platform = token.platform)
                return@launch
            }
            debugOverlayBridge.updateVlm(framesCaptured = 1, status = "captured 1")
            debugLog("REDU_VLM", "captured screenshot bytes=${frame.size}")

            debugOverlayBridge.updateVlm(status = "inferring...")
            val label = try {
                withTimeoutOrNull(VLM_INFERENCE_TIMEOUT_MILLIS) {
                    visualSentimentResolver.resolveNoTextItem(listOf(frame))
                }
            } catch (e: CancellationException) {
                debugOverlayBridge.updateVlm(status = "canceled")
                debugLog("REDU_VLM", "inference canceled")
                throw e
            }
            if (label == null) {
                debugOverlayBridge.updateVlm(status = "timeout")
                debugLog("REDU_VLM", "inference timeout")
                logReliability(ReliabilityEventType.VLM_UNRESOLVED, "vlm_inference_timeout", platform = token.platform)
                return@launch
            }
            if (label == VisualSentimentLabel.UNRESOLVED) {
                logReliability(ReliabilityEventType.VLM_UNRESOLVED, "vlm_unresolved", platform = token.platform)
            }
            val applied = applyVlmResult(token, label)
            debugOverlayBridge.updateVlm(
                lastLabel = label.name,
                status = if (applied) "result: ${label.name}" else "stale result",
            )
            debugLog("REDU_VLM", "result=${label.name} applied=$applied")
        }
    }

    private suspend fun applyVlmResult(token: VlmRequestToken, label: VisualSentimentLabel): Boolean =
        withContext(trackerDispatcher) {
            val snapshot = tracker.snapshot()
            if (currentVlmRequest != token || snapshot?.matches(token) != true) {
                return@withContext false
            }
            tracker.addDelayedVlmSentiment(label)
            currentVlmRequest = null
            true
        }

    private fun handlePromptEvent(
        event: PromptPresentationEvent,
        level: PromptLevel,
        riskScore: Double,
        riskLevel: RiskLevel,
        cooldownActive: Boolean,
        trigger: PromptTrigger = PromptTrigger.NONE,
    ) {
        when (event) {
            PromptPresentationEvent.BlockingShown -> {
                logPromptAction(PromptAction.SHOWN, level, riskScore, riskLevel, cooldownActive, trigger)
                scope.launch {
                    withContext(trackerDispatcher) {
                        tracker.closeForPrompt()
                    }
                }
            }

            PromptPresentationEvent.NonBlockingShown -> {
                logPromptAction(PromptAction.SHOWN, level, riskScore, riskLevel, cooldownActive, trigger)
            }

            is PromptPresentationEvent.Closed -> {
                promptPolicy.onPromptOutcome(event.action)
                logPromptAction(event.action, level, riskScore, riskLevel, cooldownActive, trigger)
                scope.launch {
                    withContext(trackerDispatcher) {
                        tracker.onPromptClosed()
                    }
                }
            }
        }
    }

    private fun ActiveSessionSnapshot.matches(token: VlmRequestToken): Boolean =
        platform == token.platform &&
            startedAtMillis == token.sessionStartedAtMillis &&
            lastObservedTransitionFingerprint == token.transitionFingerprint

    private fun isTargetWindowStillAvailable(): Boolean {
        val root = rootInActiveWindow ?: return false
        try {
            val platform = PlatformAdapterRegistry.platformFor(root.packageName)
            if (platform != null) {
                if (platform !in enabledPlatforms) return false
                val directSupported = PlatformAdapterRegistry.isSupportedSurface(platform, root)
                val commentSheet = !directSupported && PlatformAdapterRegistry.isCommentSheetSurface(platform, root)
                val supported = commentSheetSurfaceResolver.resolve(
                    rootWindowId = root.windowId,
                    directSupported = directSupported,
                    commentSheet = commentSheet,
                    platformName = platform.name,
                    multiWindowSupported = {
                        val roots = try { windows?.mapNotNull { it.root } ?: emptyList() } catch (e: Exception) { emptyList() }
                        try {
                            PlatformAdapterRegistry.isSupportedSurface(platform, roots)
                        } finally {
                            roots.forEach { if (it != root) it.recycle() }
                        }
                    },
                )
                if (supported) return true
            }
        } finally {
            root.recycle()
        }
        return false
    }

    private fun logReliability(
        type: ReliabilityEventType,
        detailsCode: String,
        sessionId: Long? = null,
        platform: Platform? = null,
    ) {
        if (!::app.isInitialized) return
        scope.launch {
            insertReliability(type, detailsCode, sessionId, platform)
        }
    }

    private suspend fun insertReliability(
        type: ReliabilityEventType,
        detailsCode: String,
        sessionId: Long? = null,
        platform: Platform? = null,
    ) {
        val settings = app.database.settingsDao().get()
        app.database.reliabilityEventDao().insert(
            ReliabilityEventEntity(
                studyCode = settings?.studyCode?.takeIf { it.isNotBlank() } ?: "UNSET",
                platform = platform,
                timestampMillis = System.currentTimeMillis(),
                type = type,
                detailsCode = detailsCode,
                affectedSessionId = sessionId,
            ),
        )
    }

    private fun logPromptAction(
        action: PromptAction,
        level: PromptLevel,
        riskScore: Double,
        riskLevel: RiskLevel,
        cooldownActive: Boolean,
        trigger: PromptTrigger = PromptTrigger.NONE,
    ) {
        if (!::app.isInitialized) return
        scope.launch {
            val settings = app.database.settingsDao().get()
            app.database.promptEventDao().insert(
                PromptEventEntity(
                    studyCode = settings?.studyCode?.takeIf { it.isNotBlank() } ?: "UNSET",
                    studyGroup = settings?.studyGroup ?: StudyGroup.INTERVENTION,
                    sessionId = null,
                    timestampMillis = System.currentTimeMillis(),
                    riskScore = riskScore,
                    riskLevel = riskLevel,
                    promptLevel = level,
                    action = action,
                    cooldownActive = cooldownActive,
                    trigger = trigger,
                ),
            )
        }
    }

    private fun logSuppressedPromptIfNeeded(
        snapshot: ActiveSessionSnapshot,
        riskScore: Double,
        riskLevel: RiskLevel,
    ) {
        val key = SuppressedPromptKey(
            platform = snapshot.platform,
            sessionStartedAtMillis = snapshot.startedAtMillis,
            riskLevel = riskLevel,
            riskBand = (riskScore / 10.0).toInt(),
        )
        if (lastSuppressedPromptKey == key) return
        lastSuppressedPromptKey = key
        logPromptAction(
            PromptAction.SUPPRESSED,
            PromptLevel.NONE,
            riskScore,
            riskLevel,
            cooldownActive = true,
        )
    }

    private fun String.normalizeForFingerprint(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()

    private fun resetPerItemState() {
        resetPerItemStateLocked()
        debugOverlayBridge.updateVlm(framesCaptured = 0, lastLabel = "\u2014", status = "idle")
    }

    private fun resetPerItemStateLocked() {
        lastObservedTransitionFingerprint = null
        lastObservedItemHasText = true
        blankNoTextSequence = 0
        lastBlankNoTextSequenceIncrementAtMillis = 0L
        lastSuppressedPromptKey = null
        currentVlmRequest = null
        vlmCaptureJob?.cancel()
        vlmCaptureJob = null
    }

    private fun blankNoTextFingerprint(
        platform: Platform,
        sessionStartedAtMillis: Long?,
        advanceForInteraction: Boolean,
    ): String {
        val now = System.currentTimeMillis()
        if (blankNoTextSequence == 0) {
            blankNoTextSequence = 1
            lastBlankNoTextSequenceIncrementAtMillis = now
        } else if (advanceForInteraction &&
            now - lastBlankNoTextSequenceIncrementAtMillis >= BLANK_ITEM_DEBOUNCE_MILLIS
        ) {
            blankNoTextSequence += 1
            lastBlankNoTextSequenceIncrementAtMillis = now
        }
        val sessionToken = sessionStartedAtMillis ?: 0L
        return "__no_text_${platform.name}_${sessionToken}_$blankNoTextSequence"
    }

    private fun isActiveDirectTargetSurface(platform: Platform): Boolean {
        val root = rootInActiveWindow ?: return false
        try {
            val activePlatform = PlatformAdapterRegistry.platformFor(root.packageName) ?: return false
            return activePlatform == platform && PlatformAdapterRegistry.isSupportedSurface(platform, root)
        } finally {
            root.recycle()
        }
    }

    private fun debugLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) android.util.Log.d(tag, message)
    }

    private fun Int.isUserInteractionEvent(): Boolean =
        this == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            this == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ||
            this == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START ||
            this == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END ||
            this == AccessibilityEvent.TYPE_VIEW_SCROLLED

    private fun emptyDebugTokenBreakdown(): DebugTokenBreakdown =
        DebugTokenBreakdown(
            snippet = "",
            negativeTokens = emptyList(),
            positiveTokens = emptyList(),
            neutralTokens = emptyList(),
            unscoredTokens = emptyList(),
            oovRatio = 0.0,
        )

    private data class VlmRequestToken(
        val platform: Platform,
        val sessionStartedAtMillis: Long,
        val transitionFingerprint: String,
    )

    private data class TrackerScreenUpdate(
        val snapshot: ActiveSessionSnapshot?,
        val vlmRequest: VlmRequestToken?,
        val finalizedSession: FinalizedSession?,
    )

    private data class SuppressedPromptKey(
        val platform: Platform,
        val sessionStartedAtMillis: Long,
        val riskLevel: RiskLevel,
        val riskBand: Int,
    )

}
