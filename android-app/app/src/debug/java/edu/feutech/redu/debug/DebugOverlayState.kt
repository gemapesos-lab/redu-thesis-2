package edu.feutech.redu.debug

import edu.feutech.redu.data.RiskLevel
import java.util.Locale
import kotlin.math.roundToInt

// DEBUG_OVERLAY_REMOVE: delete this debug source-set model with DebugOverlayController to remove the test overlay.
data class DebugOverlayState(
    val sessionDurationMillis: Long,
    val currentDwellMillis: Long,
    val meanDwellMillis: Long,
    val swipeCount: Int,
    val riskScore: Double,
    val riskLevel: RiskLevel,
    val vaderCompound: Double?,
    val snippet: String,
    val negativeTokens: List<String>,
    val positiveTokens: List<String>,
    val neutralTokens: List<String>,
    val unscoredTokens: List<String>,
    val oovRatio: Double,
    val vlmFramesCaptured: Int = 0,
    val vlmLastLabel: String = "—",
    val vlmStatus: String = "idle",
) {
    fun chipText(): String = riskScore.roundToInt().coerceIn(0, 100).toString()

    fun riskBadgeText(): String = "${riskScore.formatOne()} ${riskLevel.name}"

    fun sessionLine(): String =
        "Session ${sessionDurationMillis.formatDuration()}  Dwell ${currentDwellMillis.formatDuration()}  Mean ${meanDwellMillis.formatDuration()}  Swipes $swipeCount"

    fun sentimentLine(): String =
        "VADER ${vaderCompound?.formatThree() ?: "n/a"}  OOV ${(oovRatio * 100).formatOne()}%"

    fun vlmLine(): String =
        "VLM $vlmStatus  frames=$vlmFramesCaptured  last=$vlmLastLabel"

    fun tokenLine(label: String, tokens: List<String>, maxItems: Int = 3): String {
        val visible = tokens.take(maxItems).joinToString(", ").ifBlank { "none" }
        val overflow = (tokens.size - maxItems).coerceAtLeast(0)
        return if (overflow > 0) "$label $visible +$overflow" else "$label $visible"
    }

    fun compactSnippet(maxChars: Int = 96): String =
        snippet.ifBlank { "[blank]" }.ellipsize(maxChars)

    internal fun Long.formatDuration(): String {
        val totalDeciseconds = coerceAtLeast(0L) / 100L
        val minutes = (totalDeciseconds / 10L) / 60L
        val seconds = (totalDeciseconds / 10L) % 60L
        val deciseconds = totalDeciseconds % 10L
        return "%d:%02d.%d".format(Locale.US, minutes, seconds, deciseconds)
    }

    internal fun Double.formatOne(): String = String.format(Locale.US, "%.1f", this)

    internal fun Double.formatThree(): String = String.format(Locale.US, "%.3f", this)

    private fun String.ellipsize(maxChars: Int): String {
        if (length <= maxChars) return this
        if (maxChars <= 3) return take(maxChars)
        return take(maxChars - 3).trimEnd() + "..."
    }
}
