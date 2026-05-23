package edu.feutech.redu.vlm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class ModelFileValidationTest {
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
