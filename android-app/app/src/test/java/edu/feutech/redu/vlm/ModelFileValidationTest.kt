package edu.feutech.redu.vlm

import android.app.DownloadManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class ModelFileValidationTest {
    @Test
    fun successfulDownloadStatusTriggersFinalization() {
        assertEquals(
            DownloadStatusDisposition.COMPLETE,
            downloadStatusDisposition(DownloadManager.STATUS_SUCCESSFUL),
        )
        assertEquals(
            DownloadStatusDisposition.FAILED,
            downloadStatusDisposition(DownloadManager.STATUS_FAILED),
        )
        assertEquals(
            DownloadStatusDisposition.ACTIVE,
            downloadStatusDisposition(DownloadManager.STATUS_RUNNING),
        )
    }

    @Test
    fun activeDownloadProgressNeverClaimsCompletion() {
        assertEquals(0.5f, activeDownloadProgress(50L, 100L), 0f)
        assertEquals(0.99f, activeDownloadProgress(100L, 100L), 0f)
        assertEquals(0.99f, activeDownloadProgress(120L, 100L), 0f)
        assertEquals(0f, activeDownloadProgress(100L, 0L), 0f)
    }

    @Test
    fun validationAcceptsMatchingSizeAndHash() {
        val file = File.createTempFile("redu-model", ".gguf")
        file.writeText("model")
        val model = ModelFile(
            filename = file.name,
            url = "https://example.test/model.gguf",
            displayName = "Test model",
            sizeBytes = file.length(),
            sha256 = file.sha256(),
        )

        assertTrue(model.isValidModelFile(file))
    }

    @Test
    fun validationRejectsMismatchedSizeOrHash() {
        val file = File.createTempFile("redu-model", ".gguf")
        file.writeText("model")
        val wrongSize = ModelFile(
            filename = file.name,
            url = "https://example.test/model.gguf",
            displayName = "Test model",
            sizeBytes = file.length() + 1,
            sha256 = file.sha256(),
        )
        val wrongHash = wrongSize.copy(sizeBytes = file.length(), sha256 = "0".repeat(64))

        assertFalse(wrongSize.isValidModelFile(file))
        assertFalse(wrongHash.isValidModelFile(file))
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
