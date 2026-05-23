package edu.feutech.redu.capture

import edu.feutech.redu.data.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformAdapterRegistryTest {
    @Test
    fun facebookKatanaMapsToFacebookPlatform() {
        assertEquals(Platform.FACEBOOK, PlatformAdapterRegistry.platformFor("com.facebook.katana"))
        assertTrue(PlatformAdapterRegistry.packageNames.contains("com.facebook.katana"))
    }
}
