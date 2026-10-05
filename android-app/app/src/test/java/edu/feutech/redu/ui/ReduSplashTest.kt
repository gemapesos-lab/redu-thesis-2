package edu.feutech.redu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReduSplashTest {
    @Test
    fun coldStartHoldsTheBlinkBeat() {
        assertEquals(SplashBeatMillis, splashHoldMillis(elapsedMillis = 0L, reducedMotion = false))
    }

    @Test
    fun readyDuringTheBeatFinishesIt() {
        assertEquals(1_200L, splashHoldMillis(elapsedMillis = 800L, reducedMotion = false))
    }

    @Test
    fun slowStartLeavesImmediately() {
        assertEquals(0L, splashHoldMillis(elapsedMillis = SplashBeatMillis, reducedMotion = false))
        assertEquals(0L, splashHoldMillis(elapsedMillis = SplashBeatMillis + 1, reducedMotion = false))
    }

    @Test
    fun reducedMotionHoldsAShortStillPose() {
        assertEquals(400L, splashHoldMillis(elapsedMillis = 0L, reducedMotion = true))
        assertEquals(0L, splashHoldMillis(elapsedMillis = 400L, reducedMotion = true))
    }
}
