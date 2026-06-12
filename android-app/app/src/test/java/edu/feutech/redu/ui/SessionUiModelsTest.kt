package edu.feutech.redu.ui

import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class SessionUiModelsTest {
    @Test
    fun dashboardSummaryUsesTodayForCountsAndLatestForRisk() {
        val zone = ZoneId.of("UTC")
        val now = Instant.parse("2026-05-23T12:00:00Z").toEpochMilli()
        val sessions = listOf(
            session(
                startedAtMillis = Instant.parse("2026-05-22T10:00:00Z").toEpochMilli(),
                rawDurationMillis = 10_000L,
                riskLevel = RiskLevel.SAFE,
                riskScore = 10.0,
            ),
            session(
                startedAtMillis = Instant.parse("2026-05-23T02:00:00Z").toEpochMilli(),
                rawDurationMillis = 20_000L,
                riskLevel = RiskLevel.WARNING,
                riskScore = 40.0,
            ),
            session(
                startedAtMillis = Instant.parse("2026-05-23T08:00:00Z").toEpochMilli(),
                rawDurationMillis = 30_000L,
                riskLevel = RiskLevel.CRITICAL,
                riskScore = 80.0,
            ),
        )

        val summary = dashboardSummary(sessions, nowMillis = now, zoneId = zone)

        assertEquals(2, summary.todaySessionCount)
        assertEquals(50_000L, summary.todayActiveMillis)
        assertEquals(80.0, summary.latestRiskScore!!, 0.0)
        assertEquals(RiskLevel.CRITICAL, summary.peakRiskLevel)
        assertEquals(Instant.parse("2026-05-23T08:00:00Z").toEpochMilli(), summary.latestSession?.startedAtMillis)
    }

    @Test
    fun filteredSessionsAppliesPlatformAndRiskFilters() {
        val sessions = listOf(
            session(platform = Platform.TIKTOK, riskLevel = RiskLevel.SAFE),
            session(platform = Platform.INSTAGRAM, riskLevel = RiskLevel.WARNING),
            session(platform = Platform.FACEBOOK, riskLevel = RiskLevel.CRITICAL),
        )

        val filtered = filteredSessions(
            sessions,
            platformFilter = PlatformFilter.INSTAGRAM,
            riskFilter = RiskFilter.WARNING,
        )

        assertEquals(1, filtered.size)
        assertEquals(Platform.INSTAGRAM, filtered.single().platform)
        assertEquals(RiskLevel.WARNING, filtered.single().riskLevel)
    }

    @Test
    fun groupedSessionsSortsByDateDescending() {
        val zone = ZoneId.of("UTC")
        val sessions = listOf(
            session(startedAtMillis = Instant.parse("2026-05-22T11:00:00Z").toEpochMilli()),
            session(startedAtMillis = Instant.parse("2026-05-23T09:00:00Z").toEpochMilli()),
            session(startedAtMillis = Instant.parse("2026-05-23T12:00:00Z").toEpochMilli()),
        )

        val groups = groupedSessionsByDate(sessions, zoneId = zone)

        assertEquals(2, groups.size)
        assertEquals("2026-05-23", groups[0].date.toString())
        assertEquals(2, groups[0].sessions.size)
        assertEquals("2026-05-22", groups[1].date.toString())
        assertEquals(1, groups[1].sessions.size)
    }

    @Test
    fun dashboardSummaryReturnsNullsForEmptyLists() {
        val summary = dashboardSummary(emptyList(), nowMillis = Instant.parse("2026-05-23T12:00:00Z").toEpochMilli(), zoneId = ZoneId.of("UTC"))

        assertEquals(0, summary.todaySessionCount)
        assertEquals(0L, summary.todayActiveMillis)
        assertNull(summary.latestRiskScore)
        assertNull(summary.peakRiskLevel)
        assertNull(summary.latestSession)
    }

    @Test
    fun participantCodeSuffixControlsStudyGroup() {
        assertEquals(StudyGroup.INTERVENTION, studyGroupForParticipantCode("P-01X"))
        assertEquals(StudyGroup.INTERVENTION, studyGroupForParticipantCode("p-01x"))
        assertEquals(StudyGroup.CONTROL, studyGroupForParticipantCode("P-02Y"))
        assertEquals(StudyGroup.CONTROL, studyGroupForParticipantCode("p-02y"))
    }

    @Test
    fun studyPeriodParserAcceptsFourOrderedDates() {
        val parsed = parseStudyPeriodInputs(
            week1StartInput = "2026-05-20",
            week1EndInput = "2026-05-26",
            week2StartInput = "2026-05-27",
            week2EndInput = "2026-06-02",
        )

        assertTrue(parsed is StudyPeriodParseResult.Valid)
        parsed as StudyPeriodParseResult.Valid
        assertEquals(1779206400000L, parsed.week1StartMillis)
        assertEquals(1779811199999L, parsed.week1EndMillis)
        assertEquals(1779811200000L, parsed.week2StartMillis)
        assertEquals(1780415999999L, parsed.week2EndMillis)
    }

    @Test
    fun studyPeriodParserRejectsPartialInvalidOrOverlappingDates() {
        assertTrue(
            parseStudyPeriodInputs("2026-05-20", "", "2026-05-27", "2026-06-02")
                is StudyPeriodParseResult.Invalid,
        )
        assertTrue(
            parseStudyPeriodInputs("05/20/2026", "2026-05-26", "2026-05-27", "2026-06-02")
                is StudyPeriodParseResult.Invalid,
        )
        assertTrue(
            parseStudyPeriodInputs("2026-05-20", "2026-05-27", "2026-05-27", "2026-06-02")
                is StudyPeriodParseResult.Invalid,
        )
    }

    private fun session(
        platform: Platform = Platform.TIKTOK,
        startedAtMillis: Long = Instant.parse("2026-05-23T00:00:00Z").toEpochMilli(),
        rawDurationMillis: Long = 10_000L,
        riskLevel: RiskLevel = RiskLevel.SAFE,
        riskScore: Double = 25.0,
    ): SessionEntity =
        SessionEntity(
            studyCode = "P01",
            studyGroup = StudyGroup.INTERVENTION,
            platform = platform,
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + rawDurationMillis,
            rawDurationMillis = rawDurationMillis,
            promptExcludedDurationMillis = rawDurationMillis,
            meanDwellMillis = 2_000L,
            swipeCount = 1,
            resolvableUnits = 2,
            negativeUnits = 1,
            oovRatio = 0.2,
            nsdPercent = 50.0,
            riskScore = riskScore,
            riskLevel = riskLevel,
            sentimentReliability = SentimentReliability.RELIABLE,
        )
}
