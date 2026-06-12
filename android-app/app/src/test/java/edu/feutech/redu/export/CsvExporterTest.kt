package edu.feutech.redu.export

import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptEventEntity
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.PromptTrigger
import edu.feutech.redu.data.ReliabilityEventEntity
import edu.feutech.redu.data.ReliabilityEventType
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory

class CsvExporterTest {
    @Test
    fun sessionsCsvIncludesHeadersValuesAndEscaping() {
        val week1Start = LocalDate.of(2026, 5, 20)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli()
        val week1End = LocalDate.of(2026, 5, 26)
            .plusDays(1)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli() - 1L
        val csv = CsvExporter.sessionsCsv(
            listOf(
                session(
                    studyCode = "P,01",
                    startedAtMillis = week1Start,
                    riskLevel = RiskLevel.CRITICAL,
                    reliability = SentimentReliability.RELIABLE,
                    nsdPercent = 50.0,
                ),
            ),
            settings = settings(
                week1StartMillis = week1Start,
                week1EndMillis = week1End,
                week2StartMillis = LocalDate.of(2026, 5, 27)
                    .atStartOfDay(CsvExporter.STUDY_ZONE)
                    .toInstant()
                    .toEpochMilli(),
                week2EndMillis = LocalDate.of(2026, 6, 2)
                    .plusDays(1)
                    .atStartOfDay(CsvExporter.STUDY_ZONE)
                    .toInstant()
                    .toEpochMilli() - 1L,
            ),
        )

        val lines = csv.trim().lines()
        assertEquals(
            "study_code,group,platform,study_week,study_day,start_ms,end_ms,raw_duration_ms,prompt_excluded_duration_ms,mean_dwell_ms,swipe_count,risk_score,risk_level,sentiment_reliability,nsd_percent,resolvable_units,negative_units,oov_ratio,session_id",
            lines[0],
        )
        assertTrue(lines[1].startsWith("\"P,01\",INTERVENTION,TIKTOK,1,1,"))
        assertTrue(lines[1].contains(",CRITICAL,RELIABLE,50.0,5,1,0.1,7"))
    }

    @Test
    fun dailySummariesAggregateByStudyCodeDateAndPlatform() {
        val start = Instant.parse("2026-05-20T04:00:00Z").toEpochMilli()
        val expectedDate = Instant.ofEpochMilli(start).atZone(CsvExporter.STUDY_ZONE).toLocalDate()

        val csv = CsvExporter.dailySummariesCsv(
            listOf(
                session(startedAtMillis = start, rawDurationMillis = 10_000L, meanDwellMillis = 2_000L, riskScore = 25.0),
                session(startedAtMillis = start + 1_000L, rawDurationMillis = 20_000L, meanDwellMillis = 4_000L, riskScore = 75.0),
            ),
        )

        val lines = csv.trim().lines()
        assertEquals("study_code,date,platform,study_week,study_day,session_count,mean_duration_ms,mean_dwell_ms,mean_nsd_percent,mean_risk_score,reliable_session_count", lines[0])
        assertEquals("P01,$expectedDate,TIKTOK,,,2,15000,3000,20.0,50.0,2", lines[1])
    }

    @Test
    fun studyPeriodsCsvExportsSavedStudyWindow() {
        val settings = settings(
            week1StartMillis = 100L,
            week1EndMillis = 200L,
            week2StartMillis = 300L,
            week2EndMillis = 400L,
        )

        val csv = CsvExporter.studyPeriodsCsv(settings)

        assertEquals(
            "study_code,group,timezone,week1_start_ms,week1_end_ms,week2_start_ms,week2_end_ms\nP01,INTERVENTION,Asia/Manila,100,200,300,400\n",
            csv,
        )
    }

    @Test
    fun dailySummariesIncludesWeekAndStudyDayWhenPeriodIsConfigured() {
        val week1Start = LocalDate.of(2026, 5, 20)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli()
        val week1End = LocalDate.of(2026, 5, 26)
            .plusDays(1)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli() - 1L
        val week2Start = LocalDate.of(2026, 5, 27)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli()
        val week2End = LocalDate.of(2026, 6, 2)
            .plusDays(1)
            .atStartOfDay(CsvExporter.STUDY_ZONE)
            .toInstant()
            .toEpochMilli() - 1L
        val csv = CsvExporter.dailySummariesCsv(
            listOf(
                session(
                    startedAtMillis = LocalDate.of(2026, 5, 28)
                        .atStartOfDay(CsvExporter.STUDY_ZONE)
                        .toInstant()
                        .toEpochMilli(),
                ),
            ),
            settings = settings(
                week1StartMillis = week1Start,
                week1EndMillis = week1End,
                week2StartMillis = week2Start,
                week2EndMillis = week2End,
            ),
        )

        assertEquals(
            "study_code,date,platform,study_week,study_day,session_count,mean_duration_ms,mean_dwell_ms,mean_nsd_percent,mean_risk_score,reliable_session_count\nP01,2026-05-28,TIKTOK,2,9,1,10000,2000,20.0,25.0,1\n",
            csv,
        )
    }

    @Test
    fun promptAndReliabilityCsvsUseStableHeaders() {
        val promptCsv = CsvExporter.promptEventsCsv(
            listOf(
                PromptEventEntity(
                    studyCode = "P01",
                    studyGroup = StudyGroup.INTERVENTION,
                    sessionId = null,
                    timestampMillis = 123L,
                    riskScore = 44.0,
                    riskLevel = RiskLevel.WARNING,
                    promptLevel = PromptLevel.L2_PAUSE,
                    action = PromptAction.SHOWN,
                    cooldownActive = false,
                    trigger = PromptTrigger.NEGATIVE_CONTENT,
                ),
                PromptEventEntity(
                    studyCode = "P01",
                    studyGroup = StudyGroup.INTERVENTION,
                    sessionId = 7L,
                    timestampMillis = 124L,
                    riskScore = 66.0,
                    riskLevel = RiskLevel.CRITICAL,
                    promptLevel = PromptLevel.NONE,
                    action = PromptAction.SUPPRESSED,
                    cooldownActive = true,
                ),
            ),
        )
        val reliabilityCsv = CsvExporter.reliabilityEventsCsv(
            listOf(
                ReliabilityEventEntity(
                    studyCode = "P01",
                    platform = Platform.FACEBOOK,
                    timestampMillis = 456L,
                    type = ReliabilityEventType.SESSION_FINALIZED,
                    detailsCode = "session_saved",
                    affectedSessionId = 7L,
                ),
            ),
        )

        assertEquals(
            "study_code,timestamp_ms,session_id,risk_level,prompt_level,action,cooldown_state,group,risk_score,trigger_reason\nP01,123,,WARNING,L2_PAUSE,SHOWN,false,INTERVENTION,44.0,NEGATIVE_CONTENT\nP01,124,7,CRITICAL,NONE,SUPPRESSED,true,INTERVENTION,66.0,NONE\n",
            promptCsv,
        )
        assertEquals(
            "study_code,timestamp_ms,platform,event_type,details_code,affected_session_id\nP01,456,FACEBOOK,SESSION_FINALIZED,session_saved,7\n",
            reliabilityCsv,
        )
    }

    @Test
    fun riskPersonalizationCsvIncludesHeadersAndNullableQuantiles() {
        val csv = CsvExporter.riskPersonalizationCsv(
            listOf(
                RiskPersonalizationEntity(
                    studyCode = "P01",
                    studyGroup = StudyGroup.INTERVENTION,
                    lockedAtMillis = 123L,
                    reliableBaselineSessionCount = 10,
                    durationQ25Minutes = 4.0,
                    durationQ50Minutes = 8.0,
                    durationQ75Minutes = 12.0,
                    durationQ95Minutes = 20.0,
                    nsdQ25Percent = null,
                    nsdQ50Percent = null,
                    nsdQ75Percent = null,
                    nsdQ95Percent = null,
                ),
            ),
        )

        assertEquals(
            "study_code,group,locked_at_ms,reliable_baseline_session_count,duration_q25_min,duration_q50_min,duration_q75_min,duration_q95_min,nsd_q25_percent,nsd_q50_percent,nsd_q75_percent,nsd_q95_percent\nP01,INTERVENTION,123,10,4.0,8.0,12.0,20.0,,,,\n",
            csv,
        )
    }

    @Test
    fun exportZipNameCarriesStudyCodeAndTimestampAndSanitizesUnsafeCharacters() {
        val timestamp = Instant.parse("2026-06-10T07:10:00Z").toEpochMilli()
        // 07:10 UTC is 15:10 in the fixed study timezone (Asia/Manila, UTC+8).
        assertEquals("redu-export-P001-20260610-1510.zip", CsvExporter.exportZipName("P001", timestamp))
        assertEquals("redu-export-P01-20260610-1510.zip", CsvExporter.exportZipName("P,/01", timestamp))
        assertEquals("redu-export-UNSET-20260610-1510.zip", CsvExporter.exportZipName("..", timestamp))
    }

    @Test
    fun zipDirectoryDeletesStagingDirectoryAndKeepsZip() {
        val root = createTempDirectory(prefix = "redu-export-test").toFile()
        val staging = File(root, "redu-export-staging").apply { mkdirs() }
        val zip = File(root, "redu-export.zip")
        File(staging, "sessions.csv").writeText("a,b\n1,2\n")

        CsvExporter.zipDirectoryAndDeleteStaging(staging, zip)

        assertTrue(zip.exists())
        assertTrue(!staging.exists())
        ZipFile(zip).use { archive ->
            assertEquals("a,b\n1,2\n", archive.getInputStream(archive.getEntry("sessions.csv")).reader().readText())
        }
        root.deleteRecursively()
    }

    private fun session(
        studyCode: String = "P01",
        startedAtMillis: Long = LocalDate.of(2026, 5, 20).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        rawDurationMillis: Long = 10_000L,
        meanDwellMillis: Long = 2_000L,
        riskScore: Double = 25.0,
        riskLevel: RiskLevel = RiskLevel.SAFE,
        reliability: SentimentReliability = SentimentReliability.RELIABLE,
        nsdPercent: Double? = 20.0,
    ): SessionEntity =
        SessionEntity(
            id = 7L,
            studyCode = studyCode,
            studyGroup = StudyGroup.INTERVENTION,
            platform = Platform.TIKTOK,
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + rawDurationMillis,
            rawDurationMillis = rawDurationMillis,
            promptExcludedDurationMillis = rawDurationMillis,
            meanDwellMillis = meanDwellMillis,
            swipeCount = 3,
            resolvableUnits = 5,
            negativeUnits = 1,
            oovRatio = 0.1,
            nsdPercent = nsdPercent,
            riskScore = riskScore,
            riskLevel = riskLevel,
            sentimentReliability = reliability,
        )

    private fun settings(
        week1StartMillis: Long? = null,
        week1EndMillis: Long? = null,
        week2StartMillis: Long? = null,
        week2EndMillis: Long? = null,
    ): AppSettingsEntity =
        AppSettingsEntity(
            studyCode = "P01",
            studyGroup = StudyGroup.INTERVENTION,
            week1StartMillis = week1StartMillis,
            week1EndMillis = week1EndMillis,
            week2StartMillis = week2StartMillis,
            week2EndMillis = week2EndMillis,
        )
}
