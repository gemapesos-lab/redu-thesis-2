package edu.feutech.redu.risk

import edu.feutech.redu.data.ReduDatabase
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import kotlin.math.ceil

object RiskPersonalization {
    const val MIN_RELIABLE_BASELINE_SESSIONS = 10

    suspend fun lockForWeek2(
        database: ReduDatabase,
        studyCode: String,
        studyGroup: StudyGroup,
        lockedAtMillis: Long = System.currentTimeMillis(),
    ): RiskPersonalizationEntity {
        database.riskPersonalizationDao().getFor(studyCode, studyGroup)?.let { return it }
        val baseline = database.sessionDao().reliableBaselineSessions(studyCode, studyGroup, lockedAtMillis)
        val personalization = buildLock(
            studyCode = studyCode,
            studyGroup = studyGroup,
            lockedAtMillis = lockedAtMillis,
            baselineSessions = baseline,
        )
        database.riskPersonalizationDao().save(personalization)
        return personalization
    }

    fun buildLock(
        studyCode: String,
        studyGroup: StudyGroup,
        lockedAtMillis: Long,
        baselineSessions: List<SessionEntity>,
    ): RiskPersonalizationEntity {
        val reliable = baselineSessions.filter { it.sentimentReliability == SentimentReliability.RELIABLE }
        val reliableCount = reliable.size
        val durationQuantiles = reliable
            .takeIf { reliableCount >= MIN_RELIABLE_BASELINE_SESSIONS }
            ?.map { it.rawDurationMillis / 60_000.0 }
            ?.nearestRankQuantiles()
            ?.takeIfUsable()
        val nsdQuantiles = reliable
            .takeIf { reliableCount >= MIN_RELIABLE_BASELINE_SESSIONS }
            ?.mapNotNull { it.nsdPercent }
            ?.nearestRankQuantiles()
            ?.takeIfUsable()

        return RiskPersonalizationEntity(
            studyCode = studyCode,
            studyGroup = studyGroup,
            lockedAtMillis = lockedAtMillis,
            reliableBaselineSessionCount = reliableCount,
            durationQ25Minutes = durationQuantiles?.q25,
            durationQ50Minutes = durationQuantiles?.q50,
            durationQ75Minutes = durationQuantiles?.q75,
            durationQ95Minutes = durationQuantiles?.q95,
            nsdQ25Percent = nsdQuantiles?.q25,
            nsdQ50Percent = nsdQuantiles?.q50,
            nsdQ75Percent = nsdQuantiles?.q75,
            nsdQ95Percent = nsdQuantiles?.q95,
        )
    }

    fun configFor(entity: RiskPersonalizationEntity?): RiskMembershipConfig {
        if (entity == null) return RiskMembershipConfig.Fixed
        val duration = entity.durationQuantiles()?.toMembership()
            ?: RiskMembershipConfig.DEFAULT_DURATION
        val nsd = entity.nsdQuantiles()?.toMembership()
            ?: RiskMembershipConfig.DEFAULT_NSD
        return RiskMembershipConfig(duration = duration, nsd = nsd)
    }

    internal fun List<Double>.nearestRankQuantiles(): Quantiles? {
        if (isEmpty()) return null
        val sorted = sorted()
        fun quantile(p: Double): Double {
            val rank = ceil(p * sorted.size).toInt().coerceIn(1, sorted.size)
            return sorted[rank - 1]
        }
        return Quantiles(
            q25 = quantile(0.25),
            q50 = quantile(0.50),
            q75 = quantile(0.75),
            q95 = quantile(0.95),
        )
    }

    internal fun Quantiles.takeIfUsable(): Quantiles? =
        takeIf { q25.isFinite() && q50.isFinite() && q75.isFinite() && q95.isFinite() }
            ?.takeIf { q25 < q50 && q50 < q75 && q75 < q95 }
            ?.takeIf { q50 > 0.0 && q95 > 0.0 }

    private fun RiskPersonalizationEntity.durationQuantiles(): Quantiles? =
        quantiles(durationQ25Minutes, durationQ50Minutes, durationQ75Minutes, durationQ95Minutes)

    private fun RiskPersonalizationEntity.nsdQuantiles(): Quantiles? =
        quantiles(nsdQ25Percent, nsdQ50Percent, nsdQ75Percent, nsdQ95Percent)

    private fun quantiles(q25: Double?, q50: Double?, q75: Double?, q95: Double?): Quantiles? {
        if (q25 == null || q50 == null || q75 == null || q95 == null) return null
        return Quantiles(q25, q50, q75, q95).takeIfUsable()
    }

    private fun Quantiles.toMembership(): TriangularMembership =
        TriangularMembership(
            lowEnd = q50,
            mediumStart = q25,
            mediumPeak = q50,
            mediumEnd = q75,
            highStart = q50,
            highSaturation = q95,
        )
}

data class Quantiles(
    val q25: Double,
    val q50: Double,
    val q75: Double,
    val q95: Double,
)
