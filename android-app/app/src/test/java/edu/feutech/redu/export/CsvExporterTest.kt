package edu.feutech.redu.export

import edu.feutech.redu.data.DailySummary
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptEventEntity
import edu.feutech.redu.data.PromptLevel
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
        val csv = CsvExporter.sessionsCsv(
            listOf(
                session(
                    studyCode = "P,01",
                    riskLevel = RiskLevel.CRITICAL,
                    reliability = SentimentReliability.RELIABLE,
                    nsdPercent = 50.0,
                ),
            ),
        )

        val lines = csv.trim().lines()
        assertEquals(
            "study_code,group,platform,start_ms,end_ms,raw_duration_ms,prompt_excluded_duration_ms,mean_dwell_ms,swipe_count,risk_score,risk_level,sentiment_reliability,nsd_percent,oov_ratio",
            lines[0],
        )
        assertTrue(lines[1].startsWith("\"P,01\",INTERVENTION,TIKTOK,"))
        assertTrue(lines[1].contains(",CRITICAL,RELIABLE,50.0,0.1"))
    }

    @Test
    fun dailySummariesAggregateByStudyCodeDateAndPlatform() {
        val start = Instant.parse("2026-05-20T04:00:00Z").toEpochMilli()
        val expectedDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()

        val csv = CsvExporter.dailySummariesCsv(
            listOf(
                session(startedAtMillis = start, rawDurationMillis = 10_000L, meanDwellMillis = 2_000L, riskScore = 25.0),
                session(startedAtMillis = start + 1_000L, rawDurationMillis = 20_000L, meanDwellMillis = 4_000L, riskScore = 75.0),
            ),
        )

        val lines = csv.trim().lines()
        assertEquals("study_code,date,platform,session_count,mean_duration_ms,mean_dwell_ms,mean_nsd_percent,mean_risk_score,reliable_session_count", lines[0])
        assertEquals("P01,$expectedDate,TIKTOK,2,15000,3000,20.0,50.0,2", lines[1])
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
            "study_code,timestamp_ms,session_id,risk_level,prompt_level,action,cooldown_state\nP01,123,,WARNING,L2_PAUSE,SHOWN,false\n",
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
}
