package edu.feutech.redu.vlm

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Manages downloading and lifecycle of the Moondream 0.5B VLM model files.
 *
 * Uses Android's [DownloadManager] so downloads:
 * - Survive app switches and process death
 * - Show progress in the system notification tray
 * - Auto-resume on network failures
 *
 * Two GGUF files are required for on-device VLM inference:
 * 1. Text model  (Q4_K_M quantized, ~877 MB)
 * 2. Vision projector (F16, ~868 MB)
 *
 * Files are stored in the app's internal files directory under `vlm-models/`.
 */
class ModelDownloadManager(context: Context) {
    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "REDU_VLM_DL"
        private const val MODEL_DIR = "vlm-models"

        private const val BASE_URL =
            "https://huggingface.co/ggml-org/moondream2-20250414-GGUF/resolve/main"

        val MODEL_FILES = listOf(
            ModelFile(
                filename = "moondream2-text-model-Q4_K_M.gguf",
                url = "https://huggingface.co/salivosa/moondream2-gguf/resolve/main/moondream2-q4_k.gguf",
                displayName = "Text model (Q4_K_M)",
                sizeBytes = 919_494_048L,
                sha256 = "77baaa54e41cbfc24e305ee15f58ff2aca198517f658368f1be072d07b51f99d",
            ),
            ModelFile(
                filename = "moondream2-mmproj-f16-20250414.gguf",
                url = "$BASE_URL/moondream2-mmproj-f16-20250414.gguf",
                displayName = "Vision projector (F16)",
                sizeBytes = 909_777_984L,
                sha256 = "4cc1cb3660d87ff56432ebeb7884ad35d67c48c7b9f6b2856f305e39c38eed8f",
            ),
        )

        val TOTAL_SIZE_BYTES: Long = MODEL_FILES.sumOf { it.sizeBytes }
        private const val PREF_ACTIVE_DOWNLOADS = "active_downloads"
    }

    private val modelDir: File
        get() = File(appContext.filesDir, MODEL_DIR).also { it.mkdirs() }

    private val _state = MutableStateFlow(checkCurrentState(checkHash = false))
    val state: StateFlow<ModelState> = _state.asStateFlow()

    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val activeDownloadIds = mutableMapOf<Long, ModelFile>()
    private val prefs = appContext.getSharedPreferences("vlm_downloads", Context.MODE_PRIVATE)
    private var progressJob: Job? = null
    private var completionReceiver: BroadcastReceiver? = null

    init {
        if (_state.value !is ModelState.Ready && recoverActiveDownloads()) {
            _state.value = ModelState.Downloading(0f, "Restoring active download")
            registerCompletionReceiver()
            startProgressPolling()
        } else if (_state.value !is ModelState.Ready) {
            managerScope.launch {
                recoverCompletedExternalDownloads()
                _state.value = checkCurrentState(checkHash = false)
            }
        }
    }

    fun modelFile(filename: String): File = File(modelDir, filename)

    val isReady: Boolean
        get() = _state.value is ModelState.Ready

    suspend fun validateModels(deleteInvalid: Boolean = false): ModelValidationResult =
        withContext(Dispatchers.IO) {
            validateModelsBlocking(deleteInvalid = deleteInvalid, checkHash = true)
        }

    private fun validateModelsBlocking(deleteInvalid: Boolean = false, checkHash: Boolean): ModelValidationResult {
        if (checkHash) {
            recoverCompletedExternalDownloads()
        }
        for (model in MODEL_FILES) {
            val file = modelFile(model.filename)
            if (!file.exists()) {
                return ModelValidationResult.Invalid("${model.displayName} is missing")
            }
            val length = file.length()
            if (length != model.sizeBytes) {
                if (deleteInvalid) file.delete()
                return ModelValidationResult.Invalid(
                    "${model.displayName} has invalid size: expected ${model.sizeBytes}, got $length",
                )
            }
            if (checkHash) {
                val actualHash = file.sha256()
                if (!actualHash.equals(model.sha256, ignoreCase = true)) {
                    if (deleteInvalid) file.delete()
                    return ModelValidationResult.Invalid("${model.displayName} failed integrity check")
                }
            }
        }
        return ModelValidationResult.Valid
    }

    private fun checkCurrentState(checkHash: Boolean): ModelState {
        return when (val validation = validateModelsBlocking(checkHash = checkHash)) {
            ModelValidationResult.Valid -> ModelState.Ready
            is ModelValidationResult.Invalid -> {
                if (MODEL_FILES.any { modelFile(it.filename).exists() }) {
                    ModelState.Error(validation.reason)
                } else {
                    ModelState.NotDownloaded
                }
            }
        }
    }

    /**
     * Starts downloading model files via Android DownloadManager.
     * Downloads persist across app switches and show in system notifications.
     */
    fun startDownload() {
        if (_state.value is ModelState.Downloading || _state.value is ModelState.Verifying) return
        managerScope.launch {
            startDownloadOnIo()
        }
    }

    private fun startDownloadOnIo() {
        _state.value = ModelState.Downloading(0f)
        activeDownloadIds.clear()
        clearPersistedDownloads()
        recoverCompletedExternalDownloads()

        for (file in MODEL_FILES) {
            val targetFile = modelFile(file.filename)
            if (targetFile.exists() && file.isValidModelFile(targetFile)) {
                Log.d(TAG, "${file.displayName} already exists, skipping")
                continue
            }
            // Clean up any partial files
            targetFile.delete()

            val request = DownloadManager.Request(Uri.parse(file.url)).apply {
                setTitle("REDU: ${file.displayName}")
                setDescription("Downloading ${file.displayName} for visual sentiment analysis")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, file.filename)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
            }

            val downloadId = downloadManager.enqueue(request)
            activeDownloadIds[downloadId] = file
            persistActiveDownloads()
            Log.d(TAG, "Enqueued ${file.displayName} as download #$downloadId")
        }

        if (activeDownloadIds.isEmpty()) {
            // All files already exist
            _state.value = ModelState.Ready
            return
        }

        registerCompletionReceiver()
        startProgressPolling()
    }

    private fun registerCompletionReceiver() {
        completionReceiver?.let {
            try { appContext.unregisterReceiver(it) } catch (_: Exception) {}
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                val modelFile = activeDownloadIds.remove(id) ?: return
                persistActiveDownloads()
                managerScope.launch {
                    handleDownloadComplete(id, modelFile)
                }
            }
        }
        completionReceiver = receiver
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun handleDownloadComplete(downloadId: Long, file: ModelFile) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusIdx)
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val localUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val localUri = cursor.getString(localUriIdx)
                val downloadedFile = File(Uri.parse(localUri).path!!)
                val targetFile = modelFile(file.filename)

                _state.value = ModelState.Verifying("Moving ${file.displayName}…")
                moveToInternalStorage(downloadedFile, targetFile, file)
                _state.value = ModelState.Verifying("Verifying ${file.displayName}…")
                if (!file.isValidModelFile(targetFile)) {
                    targetFile.delete()
                    Log.e(TAG, "${file.displayName} failed integrity check")
                    _state.value = ModelState.Error("${file.displayName} failed integrity check")
                    cleanup()
                    cursor.close()
                    return
                }
            } else {
                val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = cursor.getInt(reasonIdx)
                Log.e(TAG, "${file.displayName} download failed: status=$status reason=$reason")
                _state.value = ModelState.Error("${file.displayName} download failed (${downloadReasonText(status, reason)})")
                cleanup()
                cursor.close()
                return
            }
            cursor.close()
        }

        // Check if all downloads are complete — skip full re-hash since each
        // file was already validated above; a size-only sanity check suffices.
        if (activeDownloadIds.isEmpty()) {
            cleanup()
            _state.value = when (val validation = validateModelsBlocking(deleteInvalid = false, checkHash = false)) {
                ModelValidationResult.Valid -> ModelState.Ready
                is ModelValidationResult.Invalid -> ModelState.Error(validation.reason)
            }
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = managerScope.launch {
            while (isActive && activeDownloadIds.isNotEmpty()) {
                var totalDownloaded = 0L
                var statusDetail: String? = null
                // Count already-completed files
                for (file in MODEL_FILES) {
                    val target = modelFile(file.filename)
                    if (target.exists() && file.isValidSize(target)) {
                        totalDownloaded += file.sizeBytes
                    }
                }
                // Query active downloads
                for (downloadId in activeDownloadIds.keys.toList()) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        if (bytesIdx >= 0) {
                            totalDownloaded += cursor.getLong(bytesIdx)
                        }
                        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        if (statusIdx >= 0 && reasonIdx >= 0) {
                            val status = cursor.getInt(statusIdx)
                            val reason = cursor.getInt(reasonIdx)
                            if (status == DownloadManager.STATUS_FAILED) {
                                val file = activeDownloadIds[downloadId]
                                val detail = "${file?.displayName ?: "Model file"}: ${downloadReasonText(status, reason)}"
                                Log.e(TAG, "Download #$downloadId $detail")
                                activeDownloadIds.remove(downloadId)
                                _state.value = ModelState.Error(detail)
                                cleanup()
                                cursor.close()
                                return@launch
                            }
                            if (status == DownloadManager.STATUS_PAUSED || status == DownloadManager.STATUS_FAILED) {
                                val file = activeDownloadIds[downloadId]
                                statusDetail = "${file?.displayName ?: "Model file"}: ${downloadReasonText(status, reason)}"
                                Log.w(TAG, "Download #$downloadId ${statusDetail.orEmpty()}")
                            }
                        }
                        cursor.close()
                    }
                }
                val progress = (totalDownloaded.toFloat() / TOTAL_SIZE_BYTES).coerceIn(0f, 1f)
                _state.value = ModelState.Downloading(progress, statusDetail)
                delay(1_000L) // Poll every second
            }
        }
    }

    private fun cleanup() {
        progressJob?.cancel()
        progressJob = null
        clearPersistedDownloads()
        unregisterCompletionReceiver()
    }

    private fun unregisterCompletionReceiver() {
        completionReceiver?.let {
            try { appContext.unregisterReceiver(it) } catch (_: Exception) {}
        }
        completionReceiver = null
    }

    fun close() {
        progressJob?.cancel()
        progressJob = null
        unregisterCompletionReceiver()
        managerScope.cancel()
    }

    fun cancelDownload() {
        for (id in activeDownloadIds.keys) {
            downloadManager.remove(id)
        }
        deleteExternalDownloadFiles()
        activeDownloadIds.clear()
        clearPersistedDownloads()
        cleanup()
        _state.value = ModelState.NotDownloaded
    }

    fun deleteModels() {
        cancelDownload()
        MODEL_FILES.forEach { modelFile(it.filename).delete() }
        _state.value = ModelState.NotDownloaded
    }

    fun refresh() {
        managerScope.launch {
            _state.value = checkCurrentState(checkHash = true)
        }
    }

    private fun recoverCompletedExternalDownloads() {
        for (file in MODEL_FILES) {
            val externalFile = externalDownloadFile(file) ?: continue
            if (!externalFile.exists()) continue
            val targetFile = modelFile(file.filename)
            if (targetFile.exists() && targetFile.length() == file.sizeBytes) {
                if (file.isValidModelFile(targetFile)) {
                    externalFile.delete()
                    continue
                }
            }
            if (file.isValidModelFileDirect(externalFile)) {
                moveToInternalStorage(externalFile, targetFile, file)
            }
        }
    }

    private fun externalDownloadFile(file: ModelFile): File? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let { downloadsDir ->
            File(downloadsDir, file.filename)
        }

    private fun deleteExternalDownloadFiles() {
        MODEL_FILES.forEach { model ->
            externalDownloadFile(model)?.takeIf { it.exists() }?.delete()
        }
    }

    private fun moveToInternalStorage(sourceFile: File, targetFile: File, modelFile: ModelFile) {
        targetFile.parentFile?.mkdirs()
        if (sourceFile.renameTo(targetFile)) {
            Log.d(TAG, "${modelFile.displayName} moved to ${targetFile.absolutePath}")
        } else {
            sourceFile.copyTo(targetFile, overwrite = true)
            sourceFile.delete()
            Log.d(TAG, "${modelFile.displayName} copied to ${targetFile.absolutePath}")
        }
    }

    private fun ModelFile.isValidSize(file: File): Boolean =
        file.length() == sizeBytes

    private fun persistActiveDownloads() {
        val value = activeDownloadIds.entries.joinToString("|") { (id, file) -> "$id,${file.filename}" }
        prefs.edit().putString(PREF_ACTIVE_DOWNLOADS, value).apply()
    }

    private fun clearPersistedDownloads() {
        prefs.edit().remove(PREF_ACTIVE_DOWNLOADS).apply()
    }

    private fun downloadReasonText(status: Int, reason: Int): String =
        when (status) {
            DownloadManager.STATUS_PAUSED -> when (reason) {
                DownloadManager.PAUSED_WAITING_TO_RETRY -> "paused, waiting to retry"
                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "paused, waiting for network"
                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "paused, queued for Wi-Fi"
                DownloadManager.PAUSED_UNKNOWN -> "paused"
                else -> "paused (reason=$reason)"
            }
            DownloadManager.STATUS_FAILED -> when (reason) {
                DownloadManager.ERROR_CANNOT_RESUME -> "failed, cannot resume"
                DownloadManager.ERROR_DEVICE_NOT_FOUND -> "failed, storage unavailable"
                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "failed, file already exists"
                DownloadManager.ERROR_FILE_ERROR -> "failed, file error"
                DownloadManager.ERROR_HTTP_DATA_ERROR -> "failed, HTTP data error"
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> "failed, insufficient space"
                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "failed, too many redirects"
                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "failed, unhandled HTTP response"
                DownloadManager.ERROR_UNKNOWN -> "failed"
                else -> "failed (reason=$reason)"
            }
            else -> "status=$status reason=$reason"
        }

    private fun recoverActiveDownloads(): Boolean {
        activeDownloadIds.clear()
        val persisted = prefs.getString(PREF_ACTIVE_DOWNLOADS, null).orEmpty()
        if (persisted.isBlank()) return false

        for (entry in persisted.split("|")) {
            val parts = entry.split(",", limit = 2)
            val id = parts.getOrNull(0)?.toLongOrNull() ?: continue
            val filename = parts.getOrNull(1) ?: continue
            val file = MODEL_FILES.firstOrNull { it.filename == filename } ?: continue
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
            if (cursor == null) continue
            cursor.use {
                if (!it.moveToFirst()) return@use
                val statusIdx = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIdx >= 0) it.getInt(statusIdx) else return@use
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> handleDownloadComplete(id, file)
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED -> activeDownloadIds[id] = file
                }
            }
        }

        persistActiveDownloads()
        return activeDownloadIds.isNotEmpty()
    }
}

data class ModelFile(
    val filename: String,
    val url: String,
    val displayName: String,
    val sizeBytes: Long,
    val sha256: String,
)

internal fun ModelFile.isValidModelFile(file: File): Boolean =
    file.length() == sizeBytes && file.sha256().equals(sha256, ignoreCase = true)

private fun ModelFile.isValidModelFileDirect(file: File): Boolean =
    file.length() == sizeBytes && file.sha256().equals(sha256, ignoreCase = true)

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

sealed class ModelValidationResult {
    data object Valid : ModelValidationResult()
    data class Invalid(val reason: String) : ModelValidationResult()
}

sealed class ModelState {
    data object NotDownloaded : ModelState()
    data class Downloading(val progress: Float, val detail: String? = null) : ModelState()
    data class Verifying(val detail: String? = null) : ModelState()
    data object Ready : ModelState()
    data class Error(val message: String) : ModelState()
}
