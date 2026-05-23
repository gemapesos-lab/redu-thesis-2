package edu.feutech.redu.ui

import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

data class DashboardSummary(
    val todaySessionCount: Int,
    val todayActiveMillis: Long,
    val latestRiskScore: Double?,
    val peakRiskLevel: RiskLevel?,
    val latestSession: SessionEntity?,
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
