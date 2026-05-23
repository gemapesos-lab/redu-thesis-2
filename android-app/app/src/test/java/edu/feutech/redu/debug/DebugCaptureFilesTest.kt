package edu.feutech.redu.debug

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugCaptureFilesTest {
    @Test
    fun captureDirectoryNameUsesStableTimestampShape() {
        val name = DebugCaptureFiles.captureDirectoryName(1_765_824_496_789L)

        assertTrue(name.startsWith("capture-"))
        assertTrue(Regex("capture-\\d{8}-\\d{6}-\\d{3}").matches(name))
        assertFalse(name.contains('/'))
    }
}
