package edu.feutech.redu.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun get(): AppSettingsEntity?

    @Upsert
    suspend fun save(settings: AppSettingsEntity)
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY startedAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startedAtMillis DESC")
    suspend fun all(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE sentimentReliability = 'RELIABLE' ORDER BY startedAtMillis DESC")
    suspend fun reliableSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE studyCode = :studyCode AND studyGroup = :studyGroup AND sentimentReliability = 'RELIABLE' AND startedAtMillis < :beforeMillis ORDER BY startedAtMillis ASC")
    suspend fun reliableBaselineSessions(studyCode: String, studyGroup: StudyGroup, beforeMillis: Long): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}

@Dao
interface PromptEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: PromptEventEntity): Long

    @Query("UPDATE prompt_events SET sessionId = :sessionId WHERE sessionId IS NULL AND timestampMillis BETWEEN :startedAtMillis AND :endedAtMillis")
    suspend fun attachPendingToSession(sessionId: Long, startedAtMillis: Long, endedAtMillis: Long): Int

    @Query("SELECT * FROM prompt_events ORDER BY timestampMillis DESC")
    suspend fun all(): List<PromptEventEntity>

    @Query("DELETE FROM prompt_events")
    suspend fun clearAll()
}

@Dao
interface ReliabilityEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: ReliabilityEventEntity): Long

    @Query("SELECT * FROM reliability_events ORDER BY timestampMillis DESC")
    suspend fun all(): List<ReliabilityEventEntity>

    @Query("DELETE FROM reliability_events")
    suspend fun clearAll()
}

@Dao
interface RiskPersonalizationDao {
    @Upsert
    suspend fun save(personalization: RiskPersonalizationEntity)

    @Query("SELECT * FROM risk_personalization WHERE studyCode = :studyCode AND studyGroup = :studyGroup LIMIT 1")
    suspend fun getFor(studyCode: String, studyGroup: StudyGroup): RiskPersonalizationEntity?

    @Query("SELECT * FROM risk_personalization WHERE studyCode = :studyCode AND studyGroup = :studyGroup LIMIT 1")
    fun observeFor(studyCode: String, studyGroup: StudyGroup): Flow<RiskPersonalizationEntity?>

    @Query("SELECT * FROM risk_personalization ORDER BY lockedAtMillis DESC")
    fun observeAll(): Flow<List<RiskPersonalizationEntity>>

    @Query("SELECT * FROM risk_personalization ORDER BY lockedAtMillis DESC")
    suspend fun all(): List<RiskPersonalizationEntity>

    @Query("DELETE FROM risk_personalization")
    suspend fun clearAll()
}
