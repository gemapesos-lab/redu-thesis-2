package edu.feutech.redu.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrostWindowTest {
    @Test
    fun alignedPanelMapsOneToOneWhenBitmapMatchesBackdrop() {
        val window = frostWindow(
            dx = 100f,
            dy = 40f,
            panelWidth = 80f,
            panelHeight = 60f,
            backdropWidth = 400,
            backdropHeight = 800,
            bitmapWidth = 400,
            bitmapHeight = 800,
        )

        assertEquals(100, window!!.srcOffsetX)
        assertEquals(40, window.srcOffsetY)
        assertEquals(80, window.srcWidth)
        assertEquals(60, window.srcHeight)
        assertEquals(0, window.dstOffsetX)
        assertEquals(0, window.dstOffsetY)
        assertEquals(80, window.dstWidth)
        assertEquals(60, window.dstHeight)
    }

    @Test
    fun scalesSourceWhenTheBitmapIsHalfTheBackdrop() {
        val window = frostWindow(
            dx = 100f,
            dy = 40f,
            panelWidth = 80f,
            panelHeight = 60f,
            backdropWidth = 400,
            backdropHeight = 800,
            bitmapWidth = 200,
            bitmapHeight = 400,
        )

        assertEquals(50, window!!.srcOffsetX)
        assertEquals(20, window.srcOffsetY)
        assertEquals(40, window.srcWidth)
        assertEquals(30, window.srcHeight)
        assertEquals(80, window.dstWidth)
        assertEquals(60, window.dstHeight)
    }

    @Test
    fun clipsPanelThatStartsBeforeTheBackdrop() {
        val window = frostWindow(
            dx = -20f,
            dy = 10f,
            panelWidth = 100f,
            panelHeight = 50f,
            backdropWidth = 400,
            backdropHeight = 800,
            bitmapWidth = 400,
            bitmapHeight = 800,
        )

        assertEquals(0, window!!.srcOffsetX)
        assertEquals(10, window.srcOffsetY)
        assertEquals(80, window.srcWidth)
        assertEquals(50, window.srcHeight)
        assertEquals(20, window.dstOffsetX)
        assertEquals(0, window.dstOffsetY)
        assertEquals(80, window.dstWidth)
        assertEquals(50, window.dstHeight)
    }

    @Test
    fun clipsPanelThatExtendsPastTheBackdrop() {
        val window = frostWindow(
            dx = 350f,
            dy = 760f,
            panelWidth = 100f,
            panelHeight = 80f,
            backdropWidth = 400,
            backdropHeight = 800,
            bitmapWidth = 400,
            bitmapHeight = 800,
        )

        assertEquals(350, window!!.srcOffsetX)
        assertEquals(760, window.srcOffsetY)
        assertEquals(50, window.srcWidth)
        assertEquals(40, window.srcHeight)
        assertEquals(0, window.dstOffsetX)
        assertEquals(0, window.dstOffsetY)
        assertEquals(50, window.dstWidth)
        assertEquals(40, window.dstHeight)
    }

    @Test
    fun returnsNullWhenThePanelMissesTheBackdrop() {
        assertNull(
            frostWindow(
                dx = 500f,
                dy = 0f,
                panelWidth = 40f,
                panelHeight = 40f,
                backdropWidth = 400,
                backdropHeight = 800,
                bitmapWidth = 400,
                bitmapHeight = 800,
            ),
        )
    }
}
