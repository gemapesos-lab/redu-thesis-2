package edu.feutech.redu.debug

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingOverlayPositioningTest {
    private val bounds = OverlayBounds(
        displayWidth = 400,
        displayHeight = 800,
        minVisibleWidth = 40,
        minVisibleHeight = 40,
    )

    @Test
    fun clampXAllowsOnlyMinimumVisibleWidthOffLeftEdge() {
        val clamped = FloatingOverlayPositioning.clampX(
            value = -500,
            width = 120,
            bounds = bounds,
        )

        assertEquals(-80, clamped)
    }

    @Test
    fun clampXAllowsOnlyMinimumVisibleWidthOffRightEdge() {
        val clamped = FloatingOverlayPositioning.clampX(
            value = 500,
            width = 120,
            bounds = bounds,
        )

        assertEquals(360, clamped)
    }

    @Test
    fun clampYKeepsOverlayBelowTopAndPartlyVisibleAtBottom() {
        assertEquals(0, FloatingOverlayPositioning.clampY(-100, height = 120, bounds = bounds))
        assertEquals(760, FloatingOverlayPositioning.clampY(900, height = 120, bounds = bounds))
    }

    @Test
    fun clampUsesMinimumVisibleSizeWhenViewHasNotMeasuredYet() {
        assertEquals(0, FloatingOverlayPositioning.clampX(-20, width = 0, bounds = bounds))
        assertEquals(360, FloatingOverlayPositioning.clampX(500, width = 0, bounds = bounds))
        assertEquals(760, FloatingOverlayPositioning.clampY(900, height = 0, bounds = bounds))
    }
}
