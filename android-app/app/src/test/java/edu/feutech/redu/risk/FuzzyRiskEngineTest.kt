package edu.feutech.redu.risk

import edu.feutech.redu.data.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzyRiskEngineTest {
    @Test
    fun ruleTablesHaveExpectedCoverage() {
        assertEquals(27, FuzzyRiskEngine.fullRuleCount())
        assertEquals(9, FuzzyRiskEngine.fallbackRuleCount())
    }

    @Test
    fun allHighReliableInputIsCritical() {
        val result = FuzzyRiskEngine.evaluate(
            RiskInputs(
                meanDwellSeconds = 45.0,
                nsdPercent = 90.0,
                sessionDurationMinutes = 45.0,
                sentimentReliable = true,
            ),
        )

        assertFalse(result.usedFallback)
        assertEquals(RiskLevel.CRITICAL, result.level)
        assertTrue(result.score >= 66.67)
    }

    @Test
    fun fallbackIsUsedWhenSentimentIsUnreliable() {
        val result = FuzzyRiskEngine.evaluate(
            RiskInputs(
                meanDwellSeconds = 45.0,
                nsdPercent = null,
                sessionDurationMinutes = 45.0,
                sentimentReliable = false,
            ),
        )

        assertTrue(result.usedFallback)
        assertEquals(RiskLevel.CRITICAL, result.level)
    }

    @Test
    fun scoreCutoffsMapToRiskLevels() {
        assertEquals(RiskLevel.SAFE, FuzzyRiskEngine.levelForScore(33.32))
        assertEquals(RiskLevel.WARNING, FuzzyRiskEngine.levelForScore(33.33))
        assertEquals(RiskLevel.WARNING, FuzzyRiskEngine.levelForScore(66.66))
        assertEquals(RiskLevel.CRITICAL, FuzzyRiskEngine.levelForScore(66.67))
    }

    @Test
    fun defaultConfigKeepsFixedAnalyticScoring() {
        val result = FuzzyRiskEngine.evaluate(
            RiskInputs(
                meanDwellSeconds = 2.0,
                nsdPercent = 5.0,
                sessionDurationMinutes = 2.0,
                sentimentReliable = true,
            ),
        )

        assertEquals(RiskLevel.SAFE, result.level)
        assertEquals(16.67, result.score, 0.01)
    }

    @Test
    fun customConfigChangesLivePromptScoringOnlyWhenProvided() {
        val inputs = RiskInputs(
            meanDwellSeconds = 12.0,
            nsdPercent = 55.0,
            sessionDurationMinutes = 12.0,
            sentimentReliable = true,
        )
        val fixed = FuzzyRiskEngine.evaluate(inputs)
        val personalized = FuzzyRiskEngine.evaluate(
            inputs,
            membershipConfig = RiskMembershipConfig(
                duration = TriangularMembership(
                    lowEnd = 4.0,
                    mediumStart = 3.0,
                    mediumPeak = 5.0,
                    mediumEnd = 8.0,
                    highStart = 5.0,
                    highSaturation = 10.0,
                ),
                nsd = TriangularMembership(
                    lowEnd = 20.0,
                    mediumStart = 10.0,
                    mediumPeak = 25.0,
                    mediumEnd = 40.0,
                    highStart = 25.0,
                    highSaturation = 60.0,
                ),
            ),
        )

        assertEquals(RiskLevel.WARNING, fixed.level)
        assertEquals(RiskLevel.CRITICAL, personalized.level)
    }
}
