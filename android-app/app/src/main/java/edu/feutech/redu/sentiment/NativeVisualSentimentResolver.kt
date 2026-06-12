package edu.feutech.redu.sentiment

import edu.feutech.redu.vlm.MoondreamLlamaNative
import edu.feutech.redu.vlm.ModelDownloadManager
import edu.feutech.redu.vlm.ModelValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NativeVisualSentimentResolver(
    private val modelDownloadManager: ModelDownloadManager
) : VisualSentimentResolver {

    private val inferenceMutex = Mutex()
    private var initialized = false
    private var loadedModelFiles: ModelFileSnapshot? = null

    override suspend fun warmUp(): Boolean =
        withContext(Dispatchers.Default) {
            inferenceMutex.withLock {
                try {
                    ensureModelsReady()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    initialized = false
                    loadedModelFiles = null
                    false
                }
            }
        }

    override suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel =
        withContext(Dispatchers.Default) {
            try {
                inferenceMutex.withLock {
                    resolveNoTextItemLocked(frames)
                }
            } catch (e: CancellationException) {
                runCatching { MoondreamLlamaNative.cancelInference() }
                throw e
            } catch (e: Throwable) {
                // Native library load, model init, or inference failures
                // (UnsatisfiedLinkError, OutOfMemoryError, JNI exceptions) must
                // degrade to UNRESOLVED instead of crashing the monitoring service.
                initialized = false
                loadedModelFiles = null
                VisualSentimentLabel.UNRESOLVED
            }
        }

    override suspend fun close() {
        runCatching { MoondreamLlamaNative.cancelInference() }
        inferenceMutex.withLock {
            runCatching { resetNativeModels() }
        }
    }

    private suspend fun resolveNoTextItemLocked(frames: List<ByteArray>): VisualSentimentLabel {
        if (!ensureModelsReady()) return VisualSentimentLabel.UNRESOLVED
        if (frames.isEmpty()) return VisualSentimentLabel.UNRESOLVED

        val votes = frames.map { imageBytes ->
            val context = currentCoroutineContext()
            context.ensureActive()
            val cancellationHandle = context.job.invokeOnCompletion { cause ->
                if (cause is CancellationException) {
                    runCatching { MoondreamLlamaNative.cancelInference() }
                }
            }
            val response = try {
                MoondreamLlamaNative.inferenceImage(imageBytes).trim()
            } finally {
                cancellationHandle.dispose()
            }
            context.ensureActive()
            parseVisualSentimentLabel(response)
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

        if (modelDownloadManager.validateModels(checkHash = false) !is ModelValidationResult.Valid) return false

        initialized = runCatching {
            MoondreamLlamaNative.initModels(snapshot.textModelPath, snapshot.mmprojPath)
        }.getOrDefault(false)
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

internal fun parseVisualSentimentLabel(response: String): VisualSentimentLabel {
    val normalized = response.uppercase()
    return VisualSentimentLabel.entries
        .filterNot { it == VisualSentimentLabel.UNRESOLVED }
        .firstOrNull { label ->
            Regex("""(^|[^A-Z0-9_])${Regex.escape(label.name)}([^A-Z0-9_]|$)""")
                .containsMatchIn(normalized)
        }
        ?: VisualSentimentLabel.UNRESOLVED
}
