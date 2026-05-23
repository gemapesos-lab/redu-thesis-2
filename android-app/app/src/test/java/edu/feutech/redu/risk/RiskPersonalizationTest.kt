package edu.feutech.redu.risk

import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskPersonalizationTest {
    @Test
    fun nearestRankQuantilesUseThesisAnchors() {
        val quantiles = with(RiskPersonalization) {
            (1..20).map { it.toDouble() }.nearestRankQuantiles()
        }

        assertEquals(5.0, quantiles?.q25)
        assertEquals(10.0, quantiles?.q50)
        assertEquals(15.0, quantiles?.q75)
        assertEquals(19.0, quantiles?.q95)
    }

    @Test
    fun fewerThanTenReliableSessionsRetainsDefaultPriors() {
        val lock = RiskPersonalization.buildLock(
            studyCode = "P-01X",
            studyGroup = StudyGroup.INTERVENTION,
            lockedAtMillis = 1_000L,
            baselineSessions = (1..9).map { session(index = it) },
        )

        assertEquals(9, lock.reliableBaselineSessionCount)
        assertNull(lock.durationQ25Minutes)
        assertNull(lock.nsdQ25Percent)
        assertEquals(RiskMembershipConfig.Fixed, RiskPersonalization.configFor(lock))
    }

    @Test
    fun validBaselineProducesDurationAndNsdConfig() {
        val lock = RiskPersonalization.buildLock(
            studyCode = "P-01X",
            studyGroup = StudyGroup.INTERVENTION,
            lockedAtMillis = 1_000L,
            baselineSessions = (1..20).map { session(index = it) },
        )
        val config = RiskPersonalization.configFor(lock)

        assertEquals(20, lock.reliableBaselineSessionCount)
        assertEquals(5.0, lock.durationQ25Minutes)
        assertEquals(10.0, lock.durationQ50Minutes)
        assertEquals(15.0, lock.durationQ75Minutes)
        assertEquals(19.0, lock.durationQ95Minutes)
        assertEquals(25.0, lock.nsdQ25Percent)
        assertEquals(50.0, lock.nsdQ50Percent)
        assertEquals(75.0, lock.nsdQ75Percent)
        assertEquals(95.0, lock.nsdQ95Percent)
        assertTrue(config.duration.highSaturation == 19.0)
        assertTrue(config.nsd.highSaturation == 95.0)
    }

    @Test
    fun degenerateQuantilesRetainFixedPriorsForThatVariable() {
        val lock = RiskPersonalization.buildLock(
            studyCode = "P-01X",
            studyGroup = StudyGroup.INTERVENTION,
            lockedAtMillis = 1_000L,
            baselineSessions = (1..20).map { session(index = it, nsdPercent = 50.0) },
        )
        val config = RiskPersonalization.configFor(lock)

        assertEquals(10.0, lock.durationQ50Minutes)
        assertNull(lock.nsdQ25Percent)
        assertEquals(RiskMembershipConfig.DEFAULT_NSD, config.nsd)
    }

    private fun session(index: Int, nsdPercent: Double = index * 5.0): SessionEntity =
        SessionEntity(
            studyCode = "P-01X",
            studyGroup = StudyGroup.INTERVENTION,
            platform = Platform.TIKTOK,
            startedAtMillis = index.toLong(),
            endedAtMillis = index.toLong() + 1L,
            rawDurationMillis = index * 60_000L,
            promptExcludedDurationMillis = index * 60_000L,
            meanDwellMillis = 1_000L,
            swipeCount = 1,
            resolvableUnits = 1,
            negativeUnits = 1,
            oovRatio = 0.0,
            nsdPercent = nsdPercent,
            riskScore = 50.0,
            riskLevel = RiskLevel.WARNING,
            sentimentReliability = SentimentReliability.RELIABLE,
        )
}
