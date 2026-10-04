package edu.feutech.redu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReduSplashTest {
    @Test
    fun coldStartHoldsOneFullGlance() {
        assertEquals(BlobatarGlanceCycleMillis, splashHoldMillis(elapsedMillis = 0L, reducedMotion = false))
    }

    @Test
    fun slowStartFinishesTheGlanceAlreadyInProgress() {
        assertEquals(1_856L, splashHoldMillis(elapsedMillis = 4_000L, reducedMotion = false))
    }

    @Test
    fun finishedCycleDoesNotStartAnother() {
        assertEquals(0L, splashHoldMillis(elapsedMillis = BlobatarGlanceCycleMillis, reducedMotion = false))
        assertEquals(BlobatarGlanceCycleMillis - 1, splashHoldMillis(elapsedMillis = BlobatarGlanceCycleMillis + 1, reducedMotion = false))
    }

    @Test
    fun reducedMotionHoldsAShortStillPose() {
        assertEquals(400L, splashHoldMillis(elapsedMillis = 0L, reducedMotion = true))
        assertEquals(0L, splashHoldMillis(elapsedMillis = 400L, reducedMotion = true))
    }
}
