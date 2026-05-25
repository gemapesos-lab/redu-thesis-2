package edu.feutech.redu.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReduDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ReduDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migration1To5AddsSettingsDropsItemSummariesCreatesPersonalizationAndPreservesSessionRows() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO app_settings (
                    id, studyCode, studyGroup, promptsEnabled, createdAtMillis, updatedAtMillis
                ) VALUES (1, 'P01', 'INTERVENTION', 1, 10, 20)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO sessions (
                    studyCode, studyGroup, platform, startedAtMillis, endedAtMillis,
                    rawDurationMillis, promptExcludedDurationMillis, meanDwellMillis,
                    swipeCount, resolvableUnits, negativeUnits, oovRatio, nsdPercent,
                    riskScore, riskLevel, sentimentReliability
                ) VALUES (
                    'P01', 'INTERVENTION', 'TIKTOK', 1, 2,
                    1, 1, 1, 0, 1, 1, 0.0, 100.0,
                    50.0, 'WARNING', 'RELIABLE'
                )
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO item_summaries (
                    sessionId, platform, observedAtMillis, dwellMillis,
                    hasUsableText, sentimentResolved, isNegative
                ) VALUES (1, 'TIKTOK', 1, 1, 1, 1, 1)
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            ReduDatabase.MIGRATION_1_2,
            ReduDatabase.MIGRATION_2_3,
            ReduDatabase.MIGRATION_3_4,
            ReduDatabase.MIGRATION_4_5,
        )

        db.query("SELECT debugOverlayEnabled, trackTikTokEnabled, trackInstagramEnabled, trackFacebookEnabled FROM app_settings WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals(0, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
        }
        db.query("SELECT COUNT(*) FROM sessions").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='item_summaries'").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }
        db.query("SELECT COUNT(*) FROM risk_personalization").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    @Throws(IOException::class)
    fun migration2To5DropsItemSummariesCreatesPersonalizationAndPreservesSessionRows() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                INSERT INTO sessions (
                    studyCode, studyGroup, platform, startedAtMillis, endedAtMillis,
                    rawDurationMillis, promptExcludedDurationMillis, meanDwellMillis,
                    swipeCount, resolvableUnits, negativeUnits, oovRatio, nsdPercent,
                    riskScore, riskLevel, sentimentReliability
                ) VALUES (
                    'P01', 'INTERVENTION', 'TIKTOK', 1, 2,
                    1, 1, 1, 0, 1, 1, 0.0, 100.0,
                    50.0, 'WARNING', 'RELIABLE'
                )
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO item_summaries (
                    sessionId, platform, observedAtMillis, dwellMillis,
                    hasUsableText, sentimentResolved, isNegative
                ) VALUES (1, 'TIKTOK', 1, 1, 1, 1, 1)
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            ReduDatabase.MIGRATION_2_3,
            ReduDatabase.MIGRATION_3_4,
            ReduDatabase.MIGRATION_4_5,
        )

        db.query("SELECT COUNT(*) FROM sessions").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='item_summaries'").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }
        db.query("SELECT COUNT(*) FROM risk_personalization").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    @Throws(IOException::class)
    fun migration3To5CreatesRiskPersonalizationTableAddsPlatformSettingsAndPreservesRows() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO sessions (
                    studyCode, studyGroup, platform, startedAtMillis, endedAtMillis,
                    rawDurationMillis, promptExcludedDurationMillis, meanDwellMillis,
                    swipeCount, resolvableUnits, negativeUnits, oovRatio, nsdPercent,
                    riskScore, riskLevel, sentimentReliability
                ) VALUES (
                    'P01', 'INTERVENTION', 'TIKTOK', 1, 2,
                    1, 1, 1, 0, 1, 1, 0.0, 100.0,
                    50.0, 'WARNING', 'RELIABLE'
                )
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            ReduDatabase.MIGRATION_3_4,
            ReduDatabase.MIGRATION_4_5,
        )

        db.query("SELECT COUNT(*) FROM sessions").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM risk_personalization").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT trackTikTokEnabled, trackInstagramEnabled, trackFacebookEnabled FROM app_settings").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }
    }

    @Test
    @Throws(IOException::class)
    fun migration4To5AddsPlatformTrackingDefaultsOffAndPreservesSettings() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO app_settings (
                    id, studyCode, studyGroup, promptsEnabled, debugOverlayEnabled, createdAtMillis, updatedAtMillis
                ) VALUES (1, 'P01', 'INTERVENTION', 1, 1, 10, 20)
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, ReduDatabase.MIGRATION_4_5)

        db.query(
            """
            SELECT studyCode, promptsEnabled, debugOverlayEnabled,
                   trackTikTokEnabled, trackInstagramEnabled, trackFacebookEnabled
            FROM app_settings WHERE id = 1
            """.trimIndent(),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("P01", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
            assertEquals(1, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
            assertEquals(0, cursor.getInt(4))
            assertEquals(0, cursor.getInt(5))
        }
    }

    private companion object {
        const val TEST_DB = "redu-migration-test"
    }
}
