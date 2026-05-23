package edu.feutech.redu.debug

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.capture.ActiveSessionSnapshot
import edu.feutech.redu.sentiment.DebugTokenBreakdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// DEBUG_OVERLAY_REMOVE: delete this debug controller and the marked service/UI hooks for final thesis builds.
class DebugOverlayController(
    private val service: AccessibilityService,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val captureHelper = DebugScreenCaptureHelper(service)
    private val captureScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var overlayView: DebugOverlayView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var dragController: FloatingOverlayDragController? = null
    private var latestRoot: AccessibilityNodeInfo? = null
    private var latestPackageName: CharSequence? = null
    private var captureInProgress = false
    private var displayAllowed = false
    private var destroyed = false
    private var addRetryAfterMillis = 0L
    private var addFailureCount = 0
    private var lastX = 16.dp
    private var lastY = 96.dp

    fun updateCaptureTarget(
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
    ) {
        setCaptureTarget(root, packageName, ownsRoot = false)
    }

    fun update(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
        vlmFramesCaptured: Int = 0,
        vlmLastLabel: String = "\u2014",
        vlmStatus: String = "idle",
    ) {
        setCaptureTarget(root, packageName, ownsRoot = true)
        updateMetrics(snapshot, vaderCompound, tokenBreakdown, vlmFramesCaptured, vlmLastLabel, vlmStatus)
    }

    fun updateMetrics(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        vlmFramesCaptured: Int = 0,
        vlmLastLabel: String = "\u2014",
        vlmStatus: String = "idle",
    ) {
        showState(
            DebugOverlayMapper.from(
                snapshot = snapshot,
                vaderCompound = vaderCompound,
                tokenBreakdown = tokenBreakdown,
                vlmFramesCaptured = vlmFramesCaptured,
                vlmLastLabel = vlmLastLabel,
                vlmStatus = vlmStatus,
            ),
        )
    }

    private fun setCaptureTarget(
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
        ownsRoot: Boolean,
    ) {
        latestRoot?.recycle()
        latestRoot = when {
            root == null -> null
            ownsRoot -> root
            else -> AccessibilityNodeInfo.obtain(root)
        }
        latestPackageName = packageName
    }

    private fun showState(state: DebugOverlayState) {
        if (destroyed) return
        displayAllowed = true
        mainHandler.post {
            if (!displayAllowed || destroyed) return@post
            if (SystemClock.uptimeMillis() < addRetryAfterMillis) return@post
            val overlay = overlayView ?: createOverlay().also { created ->
                val params = layoutParams()
                val failure = runCatching { windowManager.addView(created.root, params) }
                    .exceptionOrNull()
                if (failure != null) {
                    dragController = null
                    scheduleAddRetry(failure)
                    return@post
                }
                addRetryAfterMillis = 0L
                addFailureCount = 0
                layoutParams = params
                overlayView = created
            }
            overlay.bind(state)
            if (overlay.root.visibility != View.VISIBLE) {
                overlay.root.visibility = View.VISIBLE
            }
        }
    }

    fun clear() {
        displayAllowed = false
        captureInProgress = false
        addRetryAfterMillis = 0L
        addFailureCount = 0
        mainHandler.post {
            overlayView?.resetCaptureStatus()
            overlayView?.clearState()
            overlayView?.root?.visibility = View.GONE
        }
        latestRoot?.recycle()
        latestRoot = null
        latestPackageName = null
    }

    fun remove() {
        clear()
        mainHandler.post {
            removeOverlayWindow()
        }
    }

    fun destroy() {
        destroyed = true
        displayAllowed = false
        captureInProgress = false
        captureScope.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        val remove = {
            removeOverlayWindow()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            remove()
        } else {
            mainHandler.post(remove)
        }
        latestRoot?.recycle()
        latestRoot = null
        latestPackageName = null
    }

    private fun removeOverlayWindow() {
        val view = overlayView?.root
        overlayView = null
        layoutParams = null
        dragController = null
        view ?: return

        runCatching { windowManager.removeViewImmediate(view) }
            .onFailure { Log.d(TAG, "Debug overlay already removed", it) }
    }

    private fun scheduleAddRetry(failure: Throwable) {
        addFailureCount = (addFailureCount + 1).coerceAtMost(MAX_ADD_FAILURE_BACKOFF_STEPS)
        val retryDelayMillis = (BASE_ADD_RETRY_DELAY_MILLIS * addFailureCount).coerceAtMost(MAX_ADD_RETRY_DELAY_MILLIS)
        addRetryAfterMillis = SystemClock.uptimeMillis() + retryDelayMillis
        Log.w(TAG, "Unable to add debug overlay; retrying in ${retryDelayMillis}ms", failure)
    }

    private fun createOverlay(): DebugOverlayView {
        lateinit var overlay: DebugOverlayView
        dragController = FloatingOverlayDragController(
            windowManager = windowManager,
            touchSlop = ViewConfiguration.get(service).scaledTouchSlop,
            boundsProvider = ::overlayBounds,
            layoutParamsProvider = { layoutParams },
            rootViewProvider = { overlayView?.root },
            onTap = {
                overlay.toggleMinimized()
                overlay.root.post { dragController?.clampCurrentPosition() }
            },
            onPositionChanged = { x, y ->
                lastX = x
                lastY = y
            },
        )
        overlay = DebugOverlayView(
            context = service,
            onCapture = ::captureCurrentScreenAndTree,
            onMinimize = {
                overlay.setMinimized(true)
                overlay.root.post { dragController?.clampCurrentPosition() }
            },
            onTouch = { view, event -> dragController?.handleTouch(view, event) == true },
        )
        overlay.root.post { dragController?.clampCurrentPosition() }
        return overlay
    }

    private fun layoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = lastX
            y = lastY
        }

    private fun overlayBounds(): OverlayBounds =
        OverlayBounds(
            displayWidth = service.resources.displayMetrics.widthPixels,
            displayHeight = service.resources.displayMetrics.heightPixels,
            minVisibleWidth = 40.dp,
            minVisibleHeight = 40.dp,
        )

    private fun captureCurrentScreenAndTree() {
        if (captureInProgress || !displayAllowed) return
        val root = latestRoot?.let { AccessibilityNodeInfo.obtain(it) }
        val packageName = latestPackageName
        captureInProgress = true
        overlayView?.setCapturing()
        overlayView?.root?.visibility = View.GONE
        captureScope.launch {
            delay(OVERLAY_HIDE_SETTLE_MILLIS)
            val treeSnapshot = withContext(Dispatchers.Default) {
                try {
                    DebugAccessibilityTreeSerializer.snapshotFrom(root)
                } finally {
                    root?.recycle()
                }
            }
            val result = runCatching {
                captureHelper.capture(treeSnapshot, packageName)
            }
            if (displayAllowed) {
                overlayView?.root?.visibility = View.VISIBLE
                overlayView?.setCaptureResult(
                    result.fold(
                        onSuccess = { "Saved ${it.nodeCount} nodes, ${it.elapsedMillis} ms" },
                        onFailure = { "Capture failed: ${it.message.orEmpty()}" },
                    ),
                )
            }
            captureInProgress = false
        }
    }

    private val Int.dp: Int
        get() = (this * service.resources.displayMetrics.density).roundToInt()

    private companion object {
        const val OVERLAY_HIDE_SETTLE_MILLIS = 32L
        const val BASE_ADD_RETRY_DELAY_MILLIS = 250L
        const val MAX_ADD_RETRY_DELAY_MILLIS = 2_000L
        const val MAX_ADD_FAILURE_BACKOFF_STEPS = 8
        const val TAG = "REDU-DebugOverlay"
    }
}
