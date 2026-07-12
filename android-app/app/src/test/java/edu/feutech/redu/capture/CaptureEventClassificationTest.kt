package edu.feutech.redu.capture

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureEventClassificationTest {
    @Test
    fun onlyScrollEventsAdvanceTextlessItems() {
        assertTrue(AccessibilityEvent.TYPE_VIEW_SCROLLED.advancesBlankItem())
        assertFalse(AccessibilityEvent.TYPE_VIEW_CLICKED.advancesBlankItem())
        assertFalse(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED.advancesBlankItem())
        assertFalse(AccessibilityEvent.TYPE_TOUCH_INTERACTION_START.advancesBlankItem())
        assertFalse(AccessibilityEvent.TYPE_TOUCH_INTERACTION_END.advancesBlankItem())
    }

    @Test
    fun appActivityWindowChangeIsDistinguishedFromServiceOverlayEvents() {
        assertTrue(
            isAppActivityWindowChange(
                eventPackageName = "edu.feutech.redu",
                servicePackageName = "edu.feutech.redu",
                eventType = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                eventClassName = "edu.feutech.redu.MainActivity",
            ),
        )
        assertFalse(
            isAppActivityWindowChange(
                eventPackageName = "edu.feutech.redu",
                servicePackageName = "edu.feutech.redu",
                eventType = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                eventClassName = "android.widget.FrameLayout",
            ),
        )
        assertFalse(
            isAppActivityWindowChange(
                eventPackageName = "edu.feutech.redu",
                servicePackageName = "edu.feutech.redu",
                eventType = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                eventClassName = "edu.feutech.redu.MainActivity",
            ),
        )
    }
}
