package edu.feutech.redu.prompt

import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability

data class PromptDecision(
    val shouldShow: Boolean,
    val level: PromptLevel,
    val cooldownActive: Boolean,
)

class PromptPolicy(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val cooldownMillis: Long = 15 * 60_000L,
    private val minimumPromptDurationMillis: Long = 15 * 60_000L,
) {
    private var lastPromptAtMillis: Long? = null

    @Synchronized
    fun decide(
        score: Double,
        riskLevel: RiskLevel,
        reliability: SentimentReliability,
        sessionDurationMillis: Long = Long.MAX_VALUE,
    ): PromptDecision {
        val now = clock()
        val cooldownActive = lastPromptAtMillis?.let { now - it < cooldownMillis } == true
        if (cooldownActive) return PromptDecision(false, PromptLevel.NONE, true)
        if (sessionDurationMillis < minimumPromptDurationMillis) {
            return PromptDecision(false, PromptLevel.NONE, false)
        }

        val level = when {
            riskLevel == RiskLevel.CRITICAL && reliability == SentimentReliability.RELIABLE -> PromptLevel.L3_BREATHING
            riskLevel == RiskLevel.CRITICAL -> PromptLevel.L2_PAUSE
            riskLevel == RiskLevel.WARNING && score >= 50.0 -> PromptLevel.L2_PAUSE
            riskLevel == RiskLevel.WARNING -> PromptLevel.L1_AWARENESS
            else -> PromptLevel.NONE
        }

        if (level == PromptLevel.NONE) return PromptDecision(false, level, false)
        lastPromptAtMillis = now
        return PromptDecision(true, level, false)
    }
}
