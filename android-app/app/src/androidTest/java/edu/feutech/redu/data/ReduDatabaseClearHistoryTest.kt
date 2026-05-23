package edu.feutech.redu.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReduDatabaseClearHistoryTest {
    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        ReduDatabase::class.java,
    ).build()

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun clearHistoryDeletesLogsAndKeepsSettings() = runBlocking {
        val settings = AppSettingsEntity(
            studyCode = "P-01X",
            studyGroup = StudyGroup.INTERVENTION,
            promptsEnabled = true,
            debugOverlayEnabled = true,
            createdAtMillis = 10L,
            updatedAtMillis = 20L,
        )
        database.settingsDao().save(settings)
        val sessionId = database.sessionDao().insert(
            SessionEntity(
                studyCode = "P-01X",
                studyGroup = StudyGroup.INTERVENTION,
                platform = Platform.TIKTOK,
                startedAtMillis = 100L,
                endedAtMillis = 200L,
                rawDurationMillis = 100L,
                promptExcludedDurationMillis = 100L,
                meanDwellMillis = 50L,
                swipeCount = 1,
                resolvableUnits = 2,
                negativeUnits = 1,
                oovRatio = 0.1,
                nsdPercent = 50.0,
                riskScore = 45.0,
                riskLevel = RiskLevel.WARNING,
                sentimentReliability = SentimentReliability.RELIABLE,
            ),
        )
        database.promptEventDao().insert(
            PromptEventEntity(
                studyCode = "P-01X",
                studyGroup = StudyGroup.INTERVENTION,
                sessionId = sessionId,
                timestampMillis = 150L,
                riskScore = 45.0,
                riskLevel = RiskLevel.WARNING,
                promptLevel = PromptLevel.L2_PAUSE,
                action = PromptAction.SHOWN,
                cooldownActive = false,
            ),
        )
        database.reliabilityEventDao().insert(
            ReliabilityEventEntity(
                studyCode = "P-01X",
                platform = Platform.TIKTOK,
                timestampMillis = 160L,
                type = ReliabilityEventType.SESSION_FINALIZED,
                detailsCode = "session_saved",
                affectedSessionId = sessionId,
            ),
        )
        database.riskPersonalizationDao().save(
            RiskPersonalizationEntity(
                studyCode = "P-01X",
                studyGroup = StudyGroup.INTERVENTION,
                lockedAtMillis = 170L,
                reliableBaselineSessionCount = 10,
                durationQ25Minutes = 1.0,
                durationQ50Minutes = 2.0,
                durationQ75Minutes = 3.0,
                durationQ95Minutes = 4.0,
                nsdQ25Percent = 10.0,
                nsdQ50Percent = 20.0,
                nsdQ75Percent = 30.0,
                nsdQ95Percent = 40.0,
            ),
        )

        database.clearHistory()

        assertTrue(database.sessionDao().all().isEmpty())
        assertTrue(database.promptEventDao().all().isEmpty())
        assertTrue(database.reliabilityEventDao().all().isEmpty())
        assertTrue(database.riskPersonalizationDao().all().isEmpty())
        val savedSettings = database.settingsDao().get()
        assertNotNull(savedSettings)
        assertEquals(settings.studyCode, savedSettings?.studyCode)
        assertEquals(settings.studyGroup, savedSettings?.studyGroup)
        assertEquals(settings.promptsEnabled, savedSettings?.promptsEnabled)
        assertEquals(settings.debugOverlayEnabled, savedSettings?.debugOverlayEnabled)
    }
}
