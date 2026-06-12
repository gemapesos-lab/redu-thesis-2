package edu.feutech.redu.prompt

import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.PromptTrigger
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability

data class PromptDecision(
    val shouldShow: Boolean,
    val level: PromptLevel,
    val cooldownActive: Boolean,
    val trigger: PromptTrigger = PromptTrigger.NONE,
)

class PromptPolicy(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val cooldownMillis: Long = 15 * 60_000L,
    private val minimumPromptDurationMillis: Long = 15 * 60_000L,
    private val sustainedRiskMillis: Long = DEFAULT_SUSTAINED_RISK_MILLIS,
) {
    companion object {
        /** Prompt-eligible risk must persist this long before a prompt may show. */
        const val DEFAULT_SUSTAINED_RISK_MILLIS = 60_000L

        /** Minimum resolved sentiment units before NSD may drive prompt decisions. */
        const val MIN_NSD_EVIDENCE_UNITS = 5

        /** NSD at or above this percent counts as negative-content evidence (start of the medium NSD band). */
        const val NEGATIVE_CONTENT_NSD_PERCENT = 17.0

        /** Disregarded prompts in a row before an awareness prompt escalates to a pause prompt. */
        const val DISREGARDS_BEFORE_ESCALATION = 2
    }

    private var lastPromptAtMillis: Long? = null
    private var eligibleSinceMillis: Long? = null
    private var eligibleSessionKey: Long? = null
    private var consecutiveDisregards = 0

    @Synchronized
    fun decide(
        score: Double,
        riskLevel: RiskLevel,
        reliability: SentimentReliability,
        sessionDurationMillis: Long = Long.MAX_VALUE,
        nsdPercent: Double? = null,
        sessionKey: Long = 0L,
    ): PromptDecision {
        val now = clock()
        if (sessionDurationMillis < minimumPromptDurationMillis) {
            resetEligibility()
            return PromptDecision(false, PromptLevel.NONE, false)
        }

        val negativeContent = reliability == SentimentReliability.RELIABLE &&
            nsdPercent != null &&
            nsdPercent >= NEGATIVE_CONTENT_NSD_PERCENT

        var level = when {
            riskLevel == RiskLevel.CRITICAL && reliability == SentimentReliability.RELIABLE -> PromptLevel.L3_BREATHING
            riskLevel == RiskLevel.CRITICAL -> PromptLevel.L2_PAUSE
            riskLevel == RiskLevel.WARNING && score >= 50.0 && negativeContent -> PromptLevel.L2_PAUSE
            riskLevel == RiskLevel.WARNING -> PromptLevel.L1_AWARENESS
            else -> PromptLevel.NONE
        }

        if (level == PromptLevel.NONE) {
            resetEligibility()
            return PromptDecision(false, level, false)
        }
        if (level == PromptLevel.L1_AWARENESS && consecutiveDisregards >= DISREGARDS_BEFORE_ESCALATION) {
            level = PromptLevel.L2_PAUSE
        }
        val trigger = if (negativeContent) PromptTrigger.NEGATIVE_CONTENT else PromptTrigger.DURATION_DWELL

        val eligibleSince = eligibleSinceMillis?.takeIf { eligibleSessionKey == sessionKey } ?: now
        eligibleSinceMillis = eligibleSince
        eligibleSessionKey = sessionKey
        if (now - eligibleSince < sustainedRiskMillis) {
            return PromptDecision(false, PromptLevel.NONE, false, trigger)
        }

        val cooldownActive = lastPromptAtMillis?.let { now - it < cooldownMillis } == true
        if (cooldownActive) return PromptDecision(false, PromptLevel.NONE, true, trigger)
        lastPromptAtMillis = now
        return PromptDecision(true, level, false, trigger)
    }

    /**
     * Feeds prompt outcomes back into the policy: repeated disregards escalate
     * the next awareness prompt, while taking a break resets the streak.
     */
    @Synchronized
    fun onPromptOutcome(action: PromptAction) {
        when (action) {
            PromptAction.CONTINUE, PromptAction.DISMISSED -> consecutiveDisregards += 1
            PromptAction.TAKE_BREAK, PromptAction.VIEW_DASHBOARD -> consecutiveDisregards = 0
            PromptAction.SHOWN, PromptAction.SUPPRESSED -> Unit
        }
    }

    private fun resetEligibility() {
        eligibleSinceMillis = null
        eligibleSessionKey = null
    }
}
