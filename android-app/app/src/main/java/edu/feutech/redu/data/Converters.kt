package edu.feutech.redu.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun toStudyGroup(value: String): StudyGroup = enumValueOf(value)
    @TypeConverter fun fromStudyGroup(value: StudyGroup): String = value.name

    @TypeConverter fun toPlatform(value: String): Platform = enumValueOf(value)
    @TypeConverter fun fromPlatform(value: Platform): String = value.name

    @TypeConverter fun toRiskLevel(value: String): RiskLevel = enumValueOf(value)
    @TypeConverter fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter fun toSentimentReliability(value: String): SentimentReliability = enumValueOf(value)
    @TypeConverter fun fromSentimentReliability(value: SentimentReliability): String = value.name

    @TypeConverter fun toPromptLevel(value: String): PromptLevel = enumValueOf(value)
    @TypeConverter fun fromPromptLevel(value: PromptLevel): String = value.name

    @TypeConverter fun toPromptAction(value: String): PromptAction = enumValueOf(value)
    @TypeConverter fun fromPromptAction(value: PromptAction): String = value.name

    @TypeConverter fun toReliabilityEventType(value: String): ReliabilityEventType = enumValueOf(value)
    @TypeConverter fun fromReliabilityEventType(value: ReliabilityEventType): String = value.name
}
