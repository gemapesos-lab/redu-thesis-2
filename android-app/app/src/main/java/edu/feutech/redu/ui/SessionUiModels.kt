package edu.feutech.redu.ui

import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.export.CsvExporter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class PlatformFilter {
    ALL,
    TIKTOK,
    INSTAGRAM,
    FACEBOOK,
}

enum class RiskFilter {
    ALL,
    SAFE,
    WARNING,
    CRITICAL,
}

internal enum class StatusTone {
    NORMAL,
    ELEVATED,
    EXTENDED,
    SUCCESS,
    ATTENTION,
    NEUTRAL,
    ERROR,
}

internal sealed interface AppDataUiState {
    data object Loading : AppDataUiState

    data class Ready(
        val settings: AppSettingsEntity?,
        val sessions: List<SessionEntity>,
        val personalizationRows: List<RiskPersonalizationEntity>,
    ) : AppDataUiState
}

internal sealed interface ExportUiState {
    data object Idle : ExportUiState
    data object Preparing : ExportUiState
    data class Ready(val fileName: String) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

internal enum class SetupStep {
    PARTICIPANT,
    PLATFORMS,
    MONITORING,
    COMPLETE,
}

internal data class ActivityPatternPresentation(
    val label: String,
    val rangeLabel: String,
    val explanation: String,
    val tone: StatusTone,
)

internal enum class PlatformMonitoringState {
    ON,
    OFF,
    PAUSED,
}

data class DashboardSummary(
    val todaySessionCount: Int,
    val todayActiveMillis: Long,
    val latestRiskScore: Double?,
    val peakRiskLevel: RiskLevel?,
    val latestSession: SessionEntity?,
)

internal data class PlatformMonitoringUiState(
    val platform: Platform,
    val state: PlatformMonitoringState,
    val latestSession: SessionEntity?,
)

internal data class DashboardUiState(
    val date: LocalDate,
    val setupComplete: Boolean,
    val accessibilityEnabled: Boolean,
    val summary: DashboardSummary,
    val platforms: List<PlatformMonitoringUiState>,
    val totalSessionCount: Int,
    val reliableSessionCount: Int,
)

data class SessionDateGroup(
    val date: LocalDate,
    val sessions: List<SessionEntity>,
)

internal fun dashboardSummary(
    sessions: List<SessionEntity>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): DashboardSummary {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val todaySessions = sessions.filter {
        Instant.ofEpochMilli(it.startedAtMillis).atZone(zoneId).toLocalDate() == today
    }
    val latest = sessions.maxByOrNull { it.startedAtMillis }
    return DashboardSummary(
        todaySessionCount = todaySessions.size,
        todayActiveMillis = todaySessions.sumOf { it.rawDurationMillis },
        latestRiskScore = latest?.riskScore,
        peakRiskLevel = todaySessions.maxWithOrNull(
            compareBy<SessionEntity> { it.riskLevel.ordinal }.thenBy { it.riskScore },
        )?.riskLevel,
        latestSession = latest,
    )
}

internal fun dashboardUiState(
    sessions: List<SessionEntity>,
    setupComplete: Boolean,
    accessibilityEnabled: Boolean,
    trackTikTokEnabled: Boolean,
    trackInstagramEnabled: Boolean,
    trackFacebookEnabled: Boolean,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): DashboardUiState {
    val enabledPlatforms = mapOf(
        Platform.TIKTOK to trackTikTokEnabled,
        Platform.INSTAGRAM to trackInstagramEnabled,
        Platform.FACEBOOK to trackFacebookEnabled,
    )
    val date = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    return DashboardUiState(
        date = date,
        setupComplete = setupComplete,
        accessibilityEnabled = accessibilityEnabled,
        summary = dashboardSummary(sessions, nowMillis, zoneId),
        platforms = enabledPlatforms.map { (platform, enabled) ->
            PlatformMonitoringUiState(
                platform = platform,
                state = when {
                    !accessibilityEnabled -> PlatformMonitoringState.PAUSED
                    enabled -> PlatformMonitoringState.ON
                    else -> PlatformMonitoringState.OFF
                },
                latestSession = sessions
                    .asSequence()
                    .filter { it.platform == platform }
                    .maxByOrNull { it.startedAtMillis },
            )
        },
        totalSessionCount = sessions.size,
        reliableSessionCount = sessions.count {
            it.sentimentReliability == SentimentReliability.RELIABLE
        },
    )
}

internal fun statusToneFor(riskLevel: RiskLevel): StatusTone =
    when (riskLevel) {
        RiskLevel.SAFE -> StatusTone.NORMAL
        RiskLevel.WARNING -> StatusTone.ELEVATED
        RiskLevel.CRITICAL -> StatusTone.EXTENDED
    }

internal fun activityPatternFor(riskLevel: RiskLevel): ActivityPatternPresentation =
    when (riskLevel) {
        RiskLevel.SAFE -> ActivityPatternPresentation(
            label = "Low",
            rangeLabel = "0-33",
            explanation = "The session stayed in REDU's lower activity-pattern range.",
            tone = StatusTone.NORMAL,
        )
        RiskLevel.WARNING -> ActivityPatternPresentation(
            label = "Elevated",
            rangeLabel = "34-66",
            explanation = "One or more session signals moved above the lower range.",
            tone = StatusTone.ELEVATED,
        )
        RiskLevel.CRITICAL -> ActivityPatternPresentation(
            label = "High",
            rangeLabel = "67-100",
            explanation = "Multiple session signals reached REDU's highest range.",
            tone = StatusTone.EXTENDED,
        )
    }

internal fun setupStepFor(
    hasParticipantCode: Boolean,
    anyPlatformEnabled: Boolean,
    accessibilityEnabled: Boolean,
): SetupStep = when {
    !hasParticipantCode -> SetupStep.PARTICIPANT
    !anyPlatformEnabled -> SetupStep.PLATFORMS
    !accessibilityEnabled -> SetupStep.MONITORING
    else -> SetupStep.COMPLETE
}

internal fun canShowMainShell(setupComplete: Boolean, hasSessions: Boolean): Boolean =
    setupComplete || hasSessions

internal fun formatReadableDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis.coerceAtLeast(0L) / 60_000L
    if (totalMinutes == 0L) return "<1 min"
    if (totalMinutes < 60L) return "$totalMinutes min"

    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (minutes == 0L) "${hours}h" else "${hours}h ${minutes}m"
}

internal fun filteredSessions(
    sessions: List<SessionEntity>,
    platformFilter: PlatformFilter,
    riskFilter: RiskFilter,
): List<SessionEntity> =
    sessions
        .filter { session ->
            platformFilter == PlatformFilter.ALL || session.platform == platformFilter.toPlatform()
        }
        .filter { session ->
            riskFilter == RiskFilter.ALL || session.riskLevel == riskFilter.toRiskLevel()
        }
        .sortedByDescending { it.startedAtMillis }

internal fun groupedSessionsByDate(
    sessions: List<SessionEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<SessionDateGroup> =
    sessions
        .sortedByDescending { it.startedAtMillis }
        .groupBy { Instant.ofEpochMilli(it.startedAtMillis).atZone(zoneId).toLocalDate() }
        .map { (date, dateSessions) -> SessionDateGroup(date, dateSessions) }
        .sortedByDescending { it.date }

internal fun exportIncludedFiles(): List<String> =
    listOf(
        "sessions.csv",
        "daily_summaries.csv",
        "study_periods.csv",
        "prompt_events.csv",
        "reliability_events.csv",
        "risk_personalization.csv",
    )

internal fun studyGroupForParticipantCode(code: String): StudyGroup =
    when (code.trim().lastOrNull()?.uppercaseChar()) {
        'Y' -> StudyGroup.CONTROL
        else -> StudyGroup.INTERVENTION
    }

internal sealed interface StudyPeriodParseResult {
    data class Valid(
        val week1StartMillis: Long?,
        val week1EndMillis: Long?,
        val week2StartMillis: Long?,
        val week2EndMillis: Long?,
    ) : StudyPeriodParseResult

    data class Invalid(val message: String) : StudyPeriodParseResult
}

internal fun parseStudyPeriodInputs(
    week1StartInput: String,
    week1EndInput: String,
    week2StartInput: String,
    week2EndInput: String,
): StudyPeriodParseResult {
    val values = listOf(
        week1StartInput.trim(),
        week1EndInput.trim(),
        week2StartInput.trim(),
        week2EndInput.trim(),
    )
    if (values.all { it.isBlank() }) {
        return StudyPeriodParseResult.Valid(null, null, null, null)
    }
    if (values.any { it.isBlank() }) {
        return StudyPeriodParseResult.Invalid("Choose all four study period dates or clear all four.")
    }

    val dates = values.map { value ->
        runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrElse {
            return StudyPeriodParseResult.Invalid("Choose a valid date for each study period field.")
        }
    }
    val week1Start = dates[0]
    val week1End = dates[1]
    val week2Start = dates[2]
    val week2End = dates[3]

    if (week1End.isBefore(week1Start) || week2End.isBefore(week2Start)) {
        return StudyPeriodParseResult.Invalid("Each week must end on or after its start date.")
    }
    if (!week1End.isBefore(week2Start)) {
        return StudyPeriodParseResult.Invalid("Week 1 must end before Week 2 starts.")
    }

    return StudyPeriodParseResult.Valid(
        week1StartMillis = week1Start.studyDayStartMillis(),
        week1EndMillis = week1End.studyDayEndMillis(),
        week2StartMillis = week2Start.studyDayStartMillis(),
        week2EndMillis = week2End.studyDayEndMillis(),
    )
}

internal fun Long?.toStudyLocalDate(): LocalDate? =
    this?.let { Instant.ofEpochMilli(it).atZone(CsvExporter.STUDY_ZONE).toLocalDate() }

internal fun LocalDate.studyDayStartMillis(): Long =
    atStartOfDay(CsvExporter.STUDY_ZONE).toInstant().toEpochMilli()

internal fun LocalDate.studyDayEndMillis(): Long =
    plusDays(1).atStartOfDay(CsvExporter.STUDY_ZONE).toInstant().toEpochMilli() - 1L

private fun PlatformFilter.toPlatform(): Platform? =
    when (this) {
        PlatformFilter.ALL -> null
        PlatformFilter.TIKTOK -> Platform.TIKTOK
        PlatformFilter.INSTAGRAM -> Platform.INSTAGRAM
        PlatformFilter.FACEBOOK -> Platform.FACEBOOK
    }

private fun RiskFilter.toRiskLevel(): RiskLevel? =
    when (this) {
        RiskFilter.ALL -> null
        RiskFilter.SAFE -> RiskLevel.SAFE
        RiskFilter.WARNING -> RiskLevel.WARNING
        RiskFilter.CRITICAL -> RiskLevel.CRITICAL
    }
