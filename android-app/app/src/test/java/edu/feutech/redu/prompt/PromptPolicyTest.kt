package edu.feutech.redu.prompt

import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptPolicyTest {
    @Test
    fun criticalReliableSessionCanShowBreathingPrompt() {
        val policy = PromptPolicy(clock = { 0L })
        val decision = policy.decide(
            score = 83.33,
            riskLevel = RiskLevel.CRITICAL,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L3_BREATHING, decision.level)
    }

    @Test
    fun criticalUnreliableSessionIsCappedAtPausePrompt() {
        val policy = PromptPolicy(clock = { 0L })
        val decision = policy.decide(
            score = 83.33,
            riskLevel = RiskLevel.CRITICAL,
            reliability = SentimentReliability.SENTIMENT_UNRELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L2_PAUSE, decision.level)
    }

    @Test
    fun cooldownSuppressesRepeatedPrompt() {
        var now = 0L
        val policy = PromptPolicy(clock = { now })
        policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )
        now = 1_000L

        val second = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )

        assertFalse(second.shouldShow)
        assertTrue(second.cooldownActive)
    }

    @Test
    fun promptIsSuppressedBeforeFifteenMinuteLiveGate() {
        val policy = PromptPolicy(clock = { 0L })
        val decision = policy.decide(
            score = 83.33,
            riskLevel = RiskLevel.CRITICAL,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 14 * 60_000L,
        )

        assertFalse(decision.shouldShow)
        assertFalse(decision.cooldownActive)
        assertEquals(PromptLevel.NONE, decision.level)
    }
}
