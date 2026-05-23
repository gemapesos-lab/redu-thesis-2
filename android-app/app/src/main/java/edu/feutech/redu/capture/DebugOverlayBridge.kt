package edu.feutech.redu.capture

import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.debug.DebugOverlayController
import edu.feutech.redu.sentiment.DebugTokenBreakdown
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DebugOverlayBridge(
    private val overlay: DebugOverlayController,
    private val scope: CoroutineScope,
    private val mainHandler: Handler,
    private val trackerDispatcher: CoroutineDispatcher,
    private val snapshotProvider: suspend () -> ActiveSessionSnapshot?,
    private val debugBuild: Boolean,
) {
    private var tickerJob: Job? = null
    private var enabled = false
    private var targetInForeground = false
    private var lastVaderCompound: Double? = null
    private var lastTokenBreakdown: DebugTokenBreakdown = emptyTokenBreakdown()
    private var vlmFramesCaptured = 0
    private var vlmLastLabel = "\u2014"
    private var vlmStatus = "idle"
    private var overlayVisible = false

    fun setEnabled(value: Boolean) {
        if (enabled == value) return
        enabled = value
        if (!enabled) {
            clear()
        } else {
            ensureTicker()
        }
    }

    fun setTargetInForeground(value: Boolean) {
        if (targetInForeground == value) return
        targetInForeground = value
        if (!targetInForeground) {
            clear()
        } else {
            ensureTicker()
        }
    }

    fun updateCaptureTarget(root: AccessibilityNodeInfo?, packageName: CharSequence?) {
        if (!debugBuild) {
            root?.recycle()
            return
        }
        mainHandler.post {
            overlay.updateCaptureTarget(root, packageName)
        }
    }

    fun update(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
    ) {
        lastVaderCompound = vaderCompound
        lastTokenBreakdown = tokenBreakdown
        if (!debugBuild || !enabled) {
            root?.recycle()
            return
        }
        overlayVisible = true
        mainHandler.post {
            if (!enabled || !targetInForeground) {
                root?.recycle()
                return@post
            }
            overlay.update(
                snapshot = snapshot,
                vaderCompound = vaderCompound,
                tokenBreakdown = tokenBreakdown,
                root = root,
                packageName = packageName,
                vlmFramesCaptured = vlmFramesCaptured,
                vlmLastLabel = vlmLastLabel,
                vlmStatus = vlmStatus,
            )
        }
        ensureTicker()
    }

    fun updateVlm(framesCaptured: Int? = null, lastLabel: String? = null, status: String? = null) {
        framesCaptured?.let { vlmFramesCaptured = it }
        lastLabel?.let { vlmLastLabel = it }
        status?.let { vlmStatus = it }
    }

    fun clear() {
        stopTicker()
        overlayVisible = false
        mainHandler.post { overlay.clear() }
    }

    fun remove() {
        stopTicker()
        overlayVisible = false
        mainHandler.post { overlay.remove() }
    }

    fun destroy() {
        stopTicker()
        overlayVisible = false
        overlay.destroy()
    }

    private fun ensureTicker() {
        if (!debugBuild || !enabled || !targetInForeground) return
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (isActive && enabled && targetInForeground) {
                delay(DEBUG_OVERLAY_TICK_MILLIS)
                val snapshot = withContext(trackerDispatcher) { snapshotProvider() } ?: continue
                mainHandler.post {
                    if (!enabled || !targetInForeground) return@post
                    overlayVisible = true
                    overlay.updateMetrics(
                        snapshot = snapshot,
                        vaderCompound = lastVaderCompound,
                        tokenBreakdown = lastTokenBreakdown,
                        vlmFramesCaptured = vlmFramesCaptured,
                        vlmLastLabel = vlmLastLabel,
                        vlmStatus = vlmStatus,
                    )
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun emptyTokenBreakdown(): DebugTokenBreakdown =
        DebugTokenBreakdown(
            snippet = "",
            negativeTokens = emptyList(),
            positiveTokens = emptyList(),
            neutralTokens = emptyList(),
            unscoredTokens = emptyList(),
            oovRatio = 0.0,
        )

    private companion object {
        const val DEBUG_OVERLAY_TICK_MILLIS = 100L
    }
}
