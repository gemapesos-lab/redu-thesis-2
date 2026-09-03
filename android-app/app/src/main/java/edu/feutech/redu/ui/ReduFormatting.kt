package edu.feutech.redu.ui

import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.export.CsvExporter
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun Long.formatMetricDuration(): String {
    val totalSeconds = coerceAtLeast(0L) / 1_000L
    if (totalSeconds < 60L) return if (totalSeconds == 1L) "1 sec" else "$totalSeconds sec"
    return formatReadableDuration(this)
}

internal fun Double.formatOneDecimal(): String = String.format(Locale.US, "%.1f", this)

internal fun Double.formatPercentRatio(): String = String.format(Locale.US, "%.1f%%", this * 100.0)

internal fun Double.formatPercentValue(): String = String.format(Locale.US, "%.1f%%", this)

internal fun Long.formatTimeOfDay(): String =
    Instant.ofEpochMilli(this)
        .atZone(CsvExporter.STUDY_ZONE)
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

internal fun LocalDate.formatDashboardDate(): String =
    format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))

internal fun LocalDate.formatDateHeader(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

internal fun LocalDate.formatStudyDate(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

internal fun Platform.displayName(): String = when (this) {
    Platform.TIKTOK -> "TikTok"
    Platform.INSTAGRAM -> "Instagram"
    Platform.FACEBOOK -> "Facebook"
}

internal fun SentimentReliability.displayName(): String = when (this) {
    SentimentReliability.RELIABLE -> "Reliable signal"
    SentimentReliability.SENTIMENT_UNRELIABLE -> "Limited signal"
}

internal fun PlatformFilter.displayName(): String = when (this) {
    PlatformFilter.ALL -> "All platforms"
    PlatformFilter.TIKTOK -> "TikTok"
    PlatformFilter.INSTAGRAM -> "Instagram"
    PlatformFilter.FACEBOOK -> "Facebook"
}

internal fun RiskFilter.displayName(): String = when (this) {
    RiskFilter.ALL -> "All patterns"
    RiskFilter.SAFE -> "Low"
    RiskFilter.WARNING -> "Elevated"
    RiskFilter.CRITICAL -> "High"
}

internal fun SessionEntity?.monitoringRecency(today: LocalDate): String {
    this ?: return "No sessions yet"
    val sessionDate = Instant.ofEpochMilli(startedAtMillis).atZone(CsvExporter.STUDY_ZONE).toLocalDate()
    return if (sessionDate == today) {
        "Last session today at ${startedAtMillis.formatTimeOfDay()}"
    } else {
        "Last session ${sessionDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))}"
    }
}

internal fun Int.sessionLabel(): String = if (this == 1) "session" else "sessions"

internal fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) {
        String.format(Locale.US, "%.1f GB", mb / 1024.0)
    } else {
        String.format(Locale.US, "%.0f MB", mb)
    }
}
