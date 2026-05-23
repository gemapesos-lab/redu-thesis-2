package edu.feutech.redu.debug

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.hardware.HardwareBuffer
import android.os.Build
import android.view.Display
import java.io.File
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class DebugCaptureResult(
    val directory: File,
    val screenshotStatus: String,
    val nodeCount: Int,
    val textCandidateCount: Int,
    val elapsedMillis: Long,
) {
    val displayMessage: String = "Saved: ${directory.absolutePath}"
}

private data class DebugScreenshotFrame(
    val buffer: HardwareBuffer,
    val colorSpace: ColorSpace,
)

// DEBUG_OVERLAY_REMOVE: captures raw screen and accessibility node text for local debugging only.
class DebugScreenCaptureHelper(
    private val service: AccessibilityService,
) {
    suspend fun capture(treeSnapshot: DebugNodeSnapshot?, packageName: CharSequence?): DebugCaptureResult = coroutineScope {
        lateinit var captureDirectory: File
        lateinit var screenshotStatus: String
        lateinit var treeDump: DebugAccessibilityTreeDump
        lateinit var captionCandidates: DebugCaptionCandidateDump
        val startedAtMillis = System.currentTimeMillis()

        captureDirectory = withContext(Dispatchers.IO) {
            DebugCaptureFiles.createCaptureDirectory(service)
        }

        val treeJob = async(Dispatchers.Default) {
            val dump = DebugAccessibilityTreeSerializer.dump(treeSnapshot)
            val candidates = DebugAccessibilityTreeSerializer.captionCandidates(treeSnapshot)
            dump to candidates
        }
        val screenshotJob = async(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                takeScreenshotFrame()
            } else {
                null
            }
        }

        val treeResult = treeJob.await()
        treeDump = treeResult.first
        captionCandidates = treeResult.second
        val screenshotFrame = screenshotJob.await()
        screenshotStatus = withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                screenshotFrame?.writePng(File(captureDirectory, "screen.png"))
                    ?: "failure: screenshot unavailable"
            } else {
                "unsupported: AccessibilityService.takeScreenshot requires Android 11/API 30+"
            }
        }
        val elapsedMillis = System.currentTimeMillis() - startedAtMillis

        withContext(Dispatchers.IO) {
            File(captureDirectory, "accessibility-tree.txt").writeText(treeDump.text)
            File(captureDirectory, "caption-candidates.txt").writeText(captionCandidates.text)
            File(captureDirectory, "metadata.txt").writeText(
                buildString {
                    appendLine("timestampMillis=${System.currentTimeMillis()}")
                    appendLine("package=${packageName?.toString().orEmpty()}")
                    appendLine("androidSdk=${Build.VERSION.SDK_INT}")
                    appendLine("screenshotStatus=$screenshotStatus")
                    appendLine("nodeCount=${treeDump.nodeCount}")
                    appendLine("captionCandidateCount=${captionCandidates.candidates.size}")
                    appendLine("textCandidateCount=${captionCandidates.candidates.size}")
                    appendLine("elapsedMillis=$elapsedMillis")
                },
            )
        }

        DebugCaptureResult(
            directory = captureDirectory,
            screenshotStatus = screenshotStatus,
            nodeCount = treeDump.nodeCount,
            textCandidateCount = captionCandidates.candidates.size,
            elapsedMillis = elapsedMillis,
        )
    }

    @TargetApi(Build.VERSION_CODES.R)
    private suspend fun takeScreenshotFrame(): DebugScreenshotFrame? =
        suspendCancellableCoroutine { continuation ->
            val executor = Executor { runnable -> runnable.run() }
            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                        continuation.resume(
                            DebugScreenshotFrame(
                                buffer = screenshot.hardwareBuffer,
                                colorSpace = screenshot.colorSpace,
                            ),
                        )
                    }

                    override fun onFailure(errorCode: Int) {
                        continuation.resume(null)
                    }
                },
            )
        }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun DebugScreenshotFrame.writePng(outputFile: File): String =
        runCatching {
            try {
                Bitmap.wrapHardwareBuffer(buffer, colorSpace)
                    ?.copy(Bitmap.Config.ARGB_8888, false)
                    ?.also { bitmap ->
                        outputFile.outputStream().use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        }
                        bitmap.recycle()
                    }
                    ?: error("Unable to wrap screenshot hardware buffer")
                "success"
            } finally {
                buffer.close()
            }
        }.getOrElse { throwable ->
            "failure: ${throwable.message.orEmpty()}"
        }

    private fun Int.toScreenshotFailureName(): String =
        when (this) {
            AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR -> "internal_error"
            AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT -> "interval_time_short"
            AccessibilityService.ERROR_TAKE_SCREENSHOT_INVALID_DISPLAY -> "invalid_display"
            AccessibilityService.ERROR_TAKE_SCREENSHOT_INVALID_WINDOW -> "invalid_window"
            AccessibilityService.ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS -> "no_accessibility_access"
            AccessibilityService.ERROR_TAKE_SCREENSHOT_SECURE_WINDOW -> "secure_window"
            else -> String.format(Locale.US, "unknown_%d", this)
        }
}
