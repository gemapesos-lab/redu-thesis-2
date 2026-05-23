package edu.feutech.redu.debug

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// DEBUG_OVERLAY_REMOVE: debug capture file layout; never used by study export.
object DebugCaptureFiles {
    private const val ROOT_DIRECTORY = "debug-captures"
    private val timestampFormat = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun createCaptureDirectory(context: Context, nowMillis: Long = System.currentTimeMillis()): File {
        val root = debugRootDirectory(context)
        val directory = File(root, captureDirectoryName(nowMillis))
        directory.mkdirs()
        return directory
    }

    fun debugRootDirectory(context: Context): File =
        context.getExternalFilesDir(ROOT_DIRECTORY) ?: File(context.filesDir, ROOT_DIRECTORY)

    fun captureDirectoryName(nowMillis: Long): String = "capture-${timestampFormat.format(Date(nowMillis))}"
}
