package edu.feutech.redu.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

enum class StudyGroup {
    INTERVENTION,
    CONTROL,
}

enum class Platform {
    TIKTOK,
    INSTAGRAM,
    FACEBOOK,
}

enum class RiskLevel {
    SAFE,
    WARNING,
    CRITICAL,
}

enum class SentimentReliability {
    RELIABLE,
    SENTIMENT_UNRELIABLE,
}

enum class PromptLevel {
    NONE,
    L1_AWARENESS,
    L2_PAUSE,
    L3_BREATHING,
}

enum class PromptAction {
    SHOWN,
    DISMISSED,
    CONTINUE,
    TAKE_BREAK,
    VIEW_DASHBOARD,
}

enum class ReliabilityEventType {
    SERVICE_STARTED,
    SERVICE_STOPPED,
    TARGET_FOREGROUND,
    TARGET_BACKGROUND,
    EXTRACTION_FAILURE,
    HIGH_OOV,
    VLM_UNRESOLVED,
    SESSION_FINALIZED,
}

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val studyCode: String = "",
    val studyGroup: StudyGroup = StudyGroup.INTERVENTION,
    val promptsEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val debugOverlayEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val trackTikTokEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val trackInstagramEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val trackFacebookEnabled: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

fun AppSettingsEntity.isTrackingEnabled(platform: Platform): Boolean =
    when (platform) {
        Platform.TIKTOK -> trackTikTokEnabled
        Platform.INSTAGRAM -> trackInstagramEnabled
        Platform.FACEBOOK -> trackFacebookEnabled
    }

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studyCode: String,
    val studyGroup: StudyGroup,
    val platform: Platform,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val rawDurationMillis: Long,
    val promptExcludedDurationMillis: Long,
    val meanDwellMillis: Long,
    val swipeCount: Int,
    val resolvableUnits: Int,
    val negativeUnits: Int,
    val oovRatio: Double,
    val nsdPercent: Double?,
    val riskScore: Double,
    val riskLevel: RiskLevel,
    val sentimentReliability: SentimentReliability,
)

@Entity(tableName = "prompt_events")
data class PromptEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studyCode: String,
    val studyGroup: StudyGroup,
    val sessionId: Long?,
    val timestampMillis: Long,
    val riskScore: Double,
    val riskLevel: RiskLevel,
    val promptLevel: PromptLevel,
    val action: PromptAction,
    val cooldownActive: Boolean,
)

@Entity(tableName = "reliability_events")
data class ReliabilityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studyCode: String,
    val platform: Platform?,
    val timestampMillis: Long,
    val type: ReliabilityEventType,
    val detailsCode: String,
    val affectedSessionId: Long?,
)

@Entity(
    tableName = "risk_personalization",
    primaryKeys = ["studyCode", "studyGroup"],
)
data class RiskPersonalizationEntity(
    val studyCode: String,
    val studyGroup: StudyGroup,
    val lockedAtMillis: Long,
    val reliableBaselineSessionCount: Int,
    val durationQ25Minutes: Double?,
    val durationQ50Minutes: Double?,
    val durationQ75Minutes: Double?,
    val durationQ95Minutes: Double?,
    val nsdQ25Percent: Double?,
    val nsdQ50Percent: Double?,
    val nsdQ75Percent: Double?,
    val nsdQ95Percent: Double?,
)

data class DailySummary(
    val studyCode: String,
    val date: String,
    val platform: Platform,
    val sessionCount: Int,
    val meanDurationMillis: Long,
    val meanDwellMillis: Long,
    val meanNsdPercent: Double?,
    val meanRiskScore: Double,
    val reliableSessionCount: Int,
)
