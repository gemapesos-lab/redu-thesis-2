package edu.feutech.redu.prompt

import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.PromptTrigger
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptPolicyTest {
    private fun immediatePolicy(clock: () -> Long = { 0L }): PromptPolicy =
        PromptPolicy(clock = clock, sustainedRiskMillis = 0L)

    @Test
    fun criticalReliableSessionCanShowBreathingPrompt() {
        val policy = immediatePolicy()
        val decision = policy.decide(
            score = 83.33,
            riskLevel = RiskLevel.CRITICAL,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 80.0,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L3_BREATHING, decision.level)
        assertEquals(PromptTrigger.NEGATIVE_CONTENT, decision.trigger)
    }

    @Test
    fun criticalUnreliableSessionIsCappedAtPausePrompt() {
        val policy = immediatePolicy()
        val decision = policy.decide(
            score = 83.33,
            riskLevel = RiskLevel.CRITICAL,
            reliability = SentimentReliability.SENTIMENT_UNRELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L2_PAUSE, decision.level)
        assertEquals(PromptTrigger.DURATION_DWELL, decision.trigger)
    }

    @Test
    fun warningWithoutNegativeContentEvidenceIsDowngradedToAwareness() {
        val policy = immediatePolicy()
        val decision = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 0.0,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L1_AWARENESS, decision.level)
        assertEquals(PromptTrigger.DURATION_DWELL, decision.trigger)
    }

    @Test
    fun warningWithNegativeContentEvidenceShowsPausePrompt() {
        val policy = immediatePolicy()
        val decision = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 50.0,
        )

        assertTrue(decision.shouldShow)
        assertEquals(PromptLevel.L2_PAUSE, decision.level)
        assertEquals(PromptTrigger.NEGATIVE_CONTENT, decision.trigger)
    }

    @Test
    fun riskMustBeSustainedBeforePromptShows() {
        var now = 0L
        val policy = PromptPolicy(clock = { now })

        val first = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 50.0,
        )
        assertFalse(first.shouldShow)
        assertFalse(first.cooldownActive)

        now = PromptPolicy.DEFAULT_SUSTAINED_RISK_MILLIS
        val second = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 16 * 60_000L,
            nsdPercent = 50.0,
        )
        assertTrue(second.shouldShow)
        assertEquals(PromptLevel.L2_PAUSE, second.level)
    }

    @Test
    fun riskDropResetsSustainedWindow() {
        var now = 0L
        val policy = PromptPolicy(clock = { now })
        fun warning() = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 16 * 60_000L,
            nsdPercent = 50.0,
        )

        warning()
        now += 30_000L
        policy.decide(
            score = 16.67,
            riskLevel = RiskLevel.SAFE,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 16 * 60_000L,
        )
        now += 30_000L

        // 60s have passed since the first warning, but risk dipped to SAFE in
        // between, so the sustained window restarted.
        assertFalse(warning().shouldShow)
    }

    @Test
    fun newSessionResetsSustainedWindow() {
        var now = 0L
        val policy = PromptPolicy(clock = { now })
        fun warning(sessionKey: Long) = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 16 * 60_000L,
            nsdPercent = 50.0,
            sessionKey = sessionKey,
        )

        warning(sessionKey = 1L)
        now += PromptPolicy.DEFAULT_SUSTAINED_RISK_MILLIS

        assertFalse(warning(sessionKey = 2L).shouldShow)
    }

    @Test
    fun cooldownSuppressesRepeatedPrompt() {
        var now = 0L
        val policy = immediatePolicy(clock = { now })
        policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 50.0,
        )
        now = 1_000L

        val second = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 50.0,
        )

        assertFalse(second.shouldShow)
        assertTrue(second.cooldownActive)
    }

    @Test
    fun promptIsSuppressedBeforeFifteenMinuteLiveGate() {
        val policy = immediatePolicy()
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

    @Test
    fun safeRiskIsNotReportedAsCooldownSuppression() {
        var now = 0L
        val policy = immediatePolicy(clock = { now })
        policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
            nsdPercent = 50.0,
        )
        now = 1_000L

        val safe = policy.decide(
            score = 16.67,
            riskLevel = RiskLevel.SAFE,
            reliability = SentimentReliability.RELIABLE,
            sessionDurationMillis = 15 * 60_000L,
        )

        assertFalse(safe.shouldShow)
        assertFalse(safe.cooldownActive)
        assertEquals(PromptLevel.NONE, safe.level)
    }

    @Test
    fun repeatedDisregardsEscalateAwarenessToPause() {
        var now = 0L
        val policy = immediatePolicy(clock = { now })
        fun durationWarning() = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.SENTIMENT_UNRELIABLE,
            sessionDurationMillis = 16 * 60_000L,
        )

        assertEquals(PromptLevel.L1_AWARENESS, durationWarning().level)
        policy.onPromptOutcome(PromptAction.DISMISSED)
        policy.onPromptOutcome(PromptAction.CONTINUE)

        now += 16 * 60_000L
        assertEquals(PromptLevel.L2_PAUSE, durationWarning().level)
    }

    @Test
    fun takingBreakResetsDisregardEscalation() {
        var now = 0L
        val policy = immediatePolicy(clock = { now })
        fun durationWarning() = policy.decide(
            score = 50.0,
            riskLevel = RiskLevel.WARNING,
            reliability = SentimentReliability.SENTIMENT_UNRELIABLE,
            sessionDurationMillis = 16 * 60_000L,
        )

        durationWarning()
        policy.onPromptOutcome(PromptAction.DISMISSED)
        policy.onPromptOutcome(PromptAction.CONTINUE)
        policy.onPromptOutcome(PromptAction.TAKE_BREAK)

        now += 16 * 60_000L
        assertEquals(PromptLevel.L1_AWARENESS, durationWarning().level)
    }
}
