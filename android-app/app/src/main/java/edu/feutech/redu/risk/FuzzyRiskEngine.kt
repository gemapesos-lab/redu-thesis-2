package edu.feutech.redu.risk

import edu.feutech.redu.data.RiskLevel
import kotlin.math.max
import kotlin.math.min

data class RiskInputs(
    val meanDwellSeconds: Double,
    val nsdPercent: Double?,
    val sessionDurationMinutes: Double,
    val sentimentReliable: Boolean,
)

data class RiskResult(
    val score: Double,
    val level: RiskLevel,
    val usedFallback: Boolean,
)

data class TriangularMembership(
    val lowEnd: Double,
    val mediumStart: Double,
    val mediumPeak: Double,
    val mediumEnd: Double,
    val highStart: Double,
    val highSaturation: Double,
) {
    fun evaluate(value: Double, coerceRange: ClosedFloatingPointRange<Double>? = null): FuzzyMemberships {
        val x = coerceRange?.let { value.coerceIn(it.start, it.endInclusive) } ?: value
        return FuzzyMemberships(
            low = FuzzyRiskEngine.leftShoulder(x, 0.0, lowEnd),
            medium = FuzzyRiskEngine.triangle(x, mediumStart, mediumPeak, mediumEnd),
            high = FuzzyRiskEngine.rightShoulder(x, highStart, highSaturation),
        )
    }
}

data class RiskMembershipConfig(
    val duration: TriangularMembership = DEFAULT_DURATION,
    val nsd: TriangularMembership = DEFAULT_NSD,
) {
    companion object {
        val DEFAULT_DWELL = TriangularMembership(
            lowEnd = 5.0,
            mediumStart = 4.0,
            mediumPeak = 12.0,
            mediumEnd = 20.0,
            highStart = 15.0,
            highSaturation = 30.0,
        )
        val DEFAULT_NSD = TriangularMembership(
            lowEnd = 33.0,
            mediumStart = 17.0,
            mediumPeak = 50.0,
            mediumEnd = 83.0,
            highStart = 67.0,
            highSaturation = 100.0,
        )
        val DEFAULT_DURATION = TriangularMembership(
            lowEnd = 10.0,
            mediumStart = 8.0,
            mediumPeak = 15.0,
            mediumEnd = 20.0,
            highStart = 15.0,
            highSaturation = 40.0,
        )
        val Fixed = RiskMembershipConfig()
    }
}

enum class FuzzyLevel {
    LOW,
    MEDIUM,
    HIGH,
}

data class FuzzyMemberships(
    val low: Double,
    val medium: Double,
    val high: Double,
) {
    fun asMap(): Map<FuzzyLevel, Double> = mapOf(
        FuzzyLevel.LOW to low,
        FuzzyLevel.MEDIUM to medium,
        FuzzyLevel.HIGH to high,
    )
}

object FuzzyRiskEngine {
    private val classCenters = mapOf(
        RiskLevel.SAFE to 16.67,
        RiskLevel.WARNING to 50.0,
        RiskLevel.CRITICAL to 83.33,
    )

    private val fullRules: Map<Triple<FuzzyLevel, FuzzyLevel, FuzzyLevel>, RiskLevel> = buildMap {
        put(rule(FuzzyLevel.LOW, FuzzyLevel.LOW, FuzzyLevel.LOW), RiskLevel.SAFE)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.LOW, FuzzyLevel.MEDIUM), RiskLevel.SAFE)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.LOW, FuzzyLevel.HIGH), RiskLevel.WARNING)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.MEDIUM, FuzzyLevel.LOW), RiskLevel.SAFE)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM), RiskLevel.WARNING)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.MEDIUM, FuzzyLevel.HIGH), RiskLevel.WARNING)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.HIGH, FuzzyLevel.LOW), RiskLevel.WARNING)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.HIGH, FuzzyLevel.MEDIUM), RiskLevel.WARNING)
        put(rule(FuzzyLevel.LOW, FuzzyLevel.HIGH, FuzzyLevel.HIGH), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.LOW, FuzzyLevel.LOW), RiskLevel.SAFE)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.LOW, FuzzyLevel.MEDIUM), RiskLevel.WARNING)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.LOW, FuzzyLevel.HIGH), RiskLevel.WARNING)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM, FuzzyLevel.LOW), RiskLevel.WARNING)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM), RiskLevel.WARNING)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM, FuzzyLevel.HIGH), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.HIGH, FuzzyLevel.LOW), RiskLevel.WARNING)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.HIGH, FuzzyLevel.MEDIUM), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.MEDIUM, FuzzyLevel.HIGH, FuzzyLevel.HIGH), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.LOW, FuzzyLevel.LOW), RiskLevel.SAFE)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.LOW, FuzzyLevel.MEDIUM), RiskLevel.WARNING)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.LOW, FuzzyLevel.HIGH), RiskLevel.WARNING)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.MEDIUM, FuzzyLevel.LOW), RiskLevel.WARNING)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.MEDIUM, FuzzyLevel.MEDIUM), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.MEDIUM, FuzzyLevel.HIGH), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.HIGH, FuzzyLevel.LOW), RiskLevel.WARNING)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.HIGH, FuzzyLevel.MEDIUM), RiskLevel.CRITICAL)
        put(rule(FuzzyLevel.HIGH, FuzzyLevel.HIGH, FuzzyLevel.HIGH), RiskLevel.CRITICAL)
    }

    private val fallbackRules: Map<Pair<FuzzyLevel, FuzzyLevel>, RiskLevel> = buildMap {
        put(FuzzyLevel.LOW to FuzzyLevel.LOW, RiskLevel.SAFE)
        put(FuzzyLevel.LOW to FuzzyLevel.MEDIUM, RiskLevel.SAFE)
        put(FuzzyLevel.LOW to FuzzyLevel.HIGH, RiskLevel.WARNING)
        put(FuzzyLevel.MEDIUM to FuzzyLevel.LOW, RiskLevel.SAFE)
        put(FuzzyLevel.MEDIUM to FuzzyLevel.MEDIUM, RiskLevel.WARNING)
        put(FuzzyLevel.MEDIUM to FuzzyLevel.HIGH, RiskLevel.WARNING)
        put(FuzzyLevel.HIGH to FuzzyLevel.LOW, RiskLevel.WARNING)
        put(FuzzyLevel.HIGH to FuzzyLevel.MEDIUM, RiskLevel.CRITICAL)
        put(FuzzyLevel.HIGH to FuzzyLevel.HIGH, RiskLevel.CRITICAL)
    }

    fun evaluate(inputs: RiskInputs, membershipConfig: RiskMembershipConfig = RiskMembershipConfig.Fixed): RiskResult {
        val dwell = dwellMembership(inputs.meanDwellSeconds).asMap()
        val duration = durationMembership(inputs.sessionDurationMinutes, membershipConfig).asMap()
        val nsd = inputs.nsdPercent?.let { nsdMembership(it, membershipConfig).asMap() }

        return if (inputs.sentimentReliable && nsd != null) {
            defuzzifyFull(dwell, nsd, duration)
        } else {
            defuzzifyFallback(dwell, duration)
        }
    }

    fun dwellMembership(seconds: Double): FuzzyMemberships =
        RiskMembershipConfig.DEFAULT_DWELL.evaluate(seconds)

    fun nsdMembership(percent: Double, membershipConfig: RiskMembershipConfig = RiskMembershipConfig.Fixed): FuzzyMemberships =
        membershipConfig.nsd.evaluate(percent, 0.0..100.0)

    fun durationMembership(minutes: Double, membershipConfig: RiskMembershipConfig = RiskMembershipConfig.Fixed): FuzzyMemberships =
        membershipConfig.duration.evaluate(minutes)

    fun levelForScore(score: Double): RiskLevel = when {
        score < 33.33 -> RiskLevel.SAFE
        score < 66.67 -> RiskLevel.WARNING
        else -> RiskLevel.CRITICAL
    }

    fun fullRuleCount(): Int = fullRules.size
    fun fallbackRuleCount(): Int = fallbackRules.size

    private fun defuzzifyFull(
        dwell: Map<FuzzyLevel, Double>,
        nsd: Map<FuzzyLevel, Double>,
        duration: Map<FuzzyLevel, Double>,
    ): RiskResult {
        val weighted = fullRules.entries.mapNotNull { (levels, risk) ->
            val activation = min(dwell.getValue(levels.first), min(nsd.getValue(levels.second), duration.getValue(levels.third)))
            activation.takeIf { it > 0.0 }?.let { it to classCenters.getValue(risk) }
        }
        val score = weightedAverage(weighted)
        return RiskResult(score = score, level = levelForScore(score), usedFallback = false)
    }

    private fun defuzzifyFallback(
        dwell: Map<FuzzyLevel, Double>,
        duration: Map<FuzzyLevel, Double>,
    ): RiskResult {
        val weighted = fallbackRules.entries.mapNotNull { (levels, risk) ->
            val activation = min(dwell.getValue(levels.first), duration.getValue(levels.second))
            activation.takeIf { it > 0.0 }?.let { it to classCenters.getValue(risk) }
        }
        val score = weightedAverage(weighted)
        return RiskResult(score = score, level = levelForScore(score), usedFallback = true)
    }

    private fun weightedAverage(weighted: List<Pair<Double, Double>>): Double {
        if (weighted.isEmpty()) return classCenters.getValue(RiskLevel.SAFE)
        val numerator = weighted.sumOf { (weight, center) -> weight * center }
        val denominator = weighted.sumOf { it.first }
        return numerator / denominator
    }

    private fun rule(dwell: FuzzyLevel, nsd: FuzzyLevel, duration: FuzzyLevel) = Triple(dwell, nsd, duration)

    internal fun triangle(x: Double, a: Double, b: Double, c: Double): Double = when {
        a == b && x <= b -> 1.0
        b == c && x >= b -> 1.0
        x <= a || x >= c -> 0.0
        x == b -> 1.0
        x < b -> (x - a) / (b - a)
        else -> (c - x) / (c - b)
    }.coerceIn(0.0, 1.0)

    internal fun leftShoulder(x: Double, minValue: Double, end: Double): Double = when {
        x <= minValue -> 1.0
        x >= end -> 0.0
        else -> 1.0 - ((x - minValue) / (end - minValue))
    }.coerceIn(0.0, 1.0)

    internal fun rightShoulder(x: Double, start: Double, saturation: Double): Double = when {
        x <= start -> 0.0
        x >= saturation -> 1.0
        else -> (x - start) / (saturation - start)
    }.coerceIn(0.0, 1.0)
}
