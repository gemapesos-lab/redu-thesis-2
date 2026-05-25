package edu.feutech.redu.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsEntityTest {
    @Test
    fun platformTrackingDefaultsOff() {
        val settings = AppSettingsEntity()

        assertFalse(settings.isTrackingEnabled(Platform.TIKTOK))
        assertFalse(settings.isTrackingEnabled(Platform.INSTAGRAM))
        assertFalse(settings.isTrackingEnabled(Platform.FACEBOOK))
    }

    @Test
    fun platformTrackingMapsToMatchingSetting() {
        val settings = AppSettingsEntity(
            trackTikTokEnabled = true,
            trackInstagramEnabled = false,
            trackFacebookEnabled = true,
        )

        assertTrue(settings.isTrackingEnabled(Platform.TIKTOK))
        assertFalse(settings.isTrackingEnabled(Platform.INSTAGRAM))
        assertTrue(settings.isTrackingEnabled(Platform.FACEBOOK))
    }
}
