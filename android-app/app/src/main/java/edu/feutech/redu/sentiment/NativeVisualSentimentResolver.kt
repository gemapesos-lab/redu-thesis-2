package edu.feutech.redu.sentiment

import edu.feutech.redu.vlm.MoondreamLlamaNative
import edu.feutech.redu.vlm.ModelDownloadManager
import edu.feutech.redu.vlm.ModelValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NativeVisualSentimentResolver(
    private val modelDownloadManager: ModelDownloadManager
) : VisualSentimentResolver {

    private val inferenceMutex = Mutex()
    private var initialized = false
    private var loadedModelFiles: ModelFileSnapshot? = null

    override suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel =
        withContext(Dispatchers.Default) {
            inferenceMutex.withLock {
                resolveNoTextItemLocked(frames)
            }
        }

    override fun close() {
        resetNativeModels()
        modelDownloadManager.close()
    }

    private suspend fun resolveNoTextItemLocked(frames: List<ByteArray>): VisualSentimentLabel {
        if (!ensureModelsReady()) return VisualSentimentLabel.UNRESOLVED
        if (frames.isEmpty()) return VisualSentimentLabel.UNRESOLVED

        val votes = frames.map { imageBytes ->
            val response = MoondreamLlamaNative.inferenceImage(imageBytes).trim()
            try {
                val cleanResponse = response.replace(Regex("[^A-Z_]"), "")
                VisualSentimentLabel.valueOf(cleanResponse)
            } catch (e: Exception) {
                VisualSentimentLabel.UNRESOLVED
            }
        }

        // Majority vote
        val validVotes = votes.filter { it != VisualSentimentLabel.UNRESOLVED }
        if (validVotes.isEmpty()) return VisualSentimentLabel.UNRESOLVED

        return validVotes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            ?: VisualSentimentLabel.UNRESOLVED
    }

    private suspend fun ensureModelsReady(): Boolean {
        val snapshot = currentModelFileSnapshot()
        if (snapshot == null) {
            resetNativeModels()
            return false
        }

        if (initialized && loadedModelFiles == snapshot) return true

        resetNativeModels()

        if (modelDownloadManager.validateModels() !is ModelValidationResult.Valid) return false

        initialized = MoondreamLlamaNative.initModels(snapshot.textModelPath, snapshot.mmprojPath)
        loadedModelFiles = if (initialized) snapshot else null
        return initialized
    }

    private fun currentModelFileSnapshot(): ModelFileSnapshot? {
        val textFile = ModelDownloadManager.MODEL_FILES[0]
        val mmprojFile = ModelDownloadManager.MODEL_FILES[1]
        val textModel = modelDownloadManager.modelFile(textFile.filename)
        val mmproj = modelDownloadManager.modelFile(mmprojFile.filename)

        if (!textModel.exists() || textModel.length() != textFile.sizeBytes) return null
        if (!mmproj.exists() || mmproj.length() != mmprojFile.sizeBytes) return null

        return ModelFileSnapshot(
            textModelPath = textModel.absolutePath,
            textModelLastModified = textModel.lastModified(),
            mmprojPath = mmproj.absolutePath,
            mmprojLastModified = mmproj.lastModified(),
        )
    }

    private fun resetNativeModels() {
        if (initialized) {
            MoondreamLlamaNative.freeModels()
            initialized = false
        }
        loadedModelFiles = null
    }

    private data class ModelFileSnapshot(
        val textModelPath: String,
        val textModelLastModified: Long,
        val mmprojPath: String,
        val mmprojLastModified: Long,
    )
}
