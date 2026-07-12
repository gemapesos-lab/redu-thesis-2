package edu.feutech.redu.capture

import android.view.accessibility.AccessibilityEvent

internal fun Int.advancesBlankItem(): Boolean =
    this == AccessibilityEvent.TYPE_VIEW_SCROLLED

internal fun isAppActivityWindowChange(
    eventPackageName: String?,
    servicePackageName: String,
    eventType: Int,
    eventClassName: String?,
): Boolean =
    eventPackageName == servicePackageName &&
        eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
        eventClassName == "$servicePackageName.MainActivity"
