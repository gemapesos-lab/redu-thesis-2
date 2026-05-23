package edu.feutech.redu.debug

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.capture.ActiveSessionSnapshot
import edu.feutech.redu.sentiment.DebugTokenBreakdown

class DebugOverlayController(
    service: AccessibilityService,
) {
    fun updateCaptureTarget(
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
    ) = Unit

    fun update(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        root: AccessibilityNodeInfo?,
        packageName: CharSequence?,
        vlmFramesCaptured: Int = 0,
        vlmLastLabel: String = "\u2014",
        vlmStatus: String = "idle",
    ) = Unit

    fun updateMetrics(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        vlmFramesCaptured: Int = 0,
        vlmLastLabel: String = "\u2014",
        vlmStatus: String = "idle",
    ) = Unit

    fun clear() = Unit

    fun remove() = Unit

    fun destroy() = Unit
}
