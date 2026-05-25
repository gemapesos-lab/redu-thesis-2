package edu.feutech.redu.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AppSettingsEntity::class,
        SessionEntity::class,
        PromptEventEntity::class,
        ReliabilityEventEntity::class,
        RiskPersonalizationEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ReduDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun sessionDao(): SessionDao
    abstract fun promptEventDao(): PromptEventDao
    abstract fun reliabilityEventDao(): ReliabilityEventDao
    abstract fun riskPersonalizationDao(): RiskPersonalizationDao

    suspend fun clearHistory() {
        withTransaction {
            promptEventDao().clearAll()
            reliabilityEventDao().clearAll()
            riskPersonalizationDao().clearAll()
            sessionDao().clearAll()
        }
    }

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `app_settings` ADD COLUMN `debugOverlayEnabled` INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `item_summaries`")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `risk_personalization` (
                        `studyCode` TEXT NOT NULL,
                        `studyGroup` TEXT NOT NULL,
                        `lockedAtMillis` INTEGER NOT NULL,
                        `reliableBaselineSessionCount` INTEGER NOT NULL,
                        `durationQ25Minutes` REAL,
                        `durationQ50Minutes` REAL,
                        `durationQ75Minutes` REAL,
                        `durationQ95Minutes` REAL,
                        `nsdQ25Percent` REAL,
                        `nsdQ50Percent` REAL,
                        `nsdQ75Percent` REAL,
                        `nsdQ95Percent` REAL,
                        PRIMARY KEY(`studyCode`, `studyGroup`)
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `app_settings` ADD COLUMN `trackTikTokEnabled` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `app_settings` ADD COLUMN `trackInstagramEnabled` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `app_settings` ADD COLUMN `trackFacebookEnabled` INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        fun create(context: Context): ReduDatabase =
            Room.databaseBuilder(context, ReduDatabase::class.java, "redu.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}
