package edu.feutech.redu.capture

import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.risk.FuzzyRiskEngine
import edu.feutech.redu.risk.RiskInputs
import edu.feutech.redu.sentiment.SentimentResult
import kotlin.math.roundToLong

data class ActiveSessionSnapshot(
    val platform: Platform,
    val startedAtMillis: Long,
    val lastEventMillis: Long,
    val swipeCount: Int,
    val currentDwellMillis: Long,
    val meanDwellMillis: Long,
    val durationMillis: Long,
    val promptExcludedDurationMillis: Long,
    val resolvableUnits: Int,
    val negativeUnits: Int,
    val oovRatio: Double,
    val nsdPercent: Double?,
    val sentimentReliability: SentimentReliability,
    val riskScore: Double,
    val riskLevel: RiskLevel,
)

data class FinalizedSession(
    val snapshot: ActiveSessionSnapshot,
    val endedAtMillis: Long,
)

class SessionTracker(
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private var active: MutableSession? = null

    fun onTargetForeground(platform: Platform = Platform.TIKTOK): FinalizedSession? {
        val now = clock()
        val session = active
        if (session == null) {
            active = newSession(platform, now)
            return null
        }
        if (session.platform != platform) {
            session.pauseForeground(now)
            val finalized = FinalizedSession(session.toSnapshot(now), endedAtMillis = now)
            active = newSession(platform, now)
            return finalized
        }
        session.resumeForeground(now)
        return null
    }

    fun onTargetBackground() {
        val session = active ?: return
        val now = clock()
        session.pauseForeground(now)
        session.lastEventMillis = now
    }

    fun onUserInteraction() {
        val session = active ?: return
        val now = clock()
        session.markInteraction(now)
    }

    fun closeForPrompt() {
        val session = active ?: return
        val now = clock()
        val activeNow = session.activeMillisAt(now)
        val dwell = session.currentDwellMillis(activeNow)
        if (dwell > 0L) {
            session.dwellSamples += dwell
        }
        session.lastItemStartedAtActiveMillis = activeNow
        session.lastInteractionAtActiveMillis = activeNow
        session.pausePromptExcluded(now)
        session.promptActive = true
        session.awaitingPostPromptTargetReturn = false
        session.lastEventMillis = now
    }

    fun onPromptClosed() {
        val session = active ?: return
        val now = clock()
        val activeNow = session.activeMillisAt(now)
        session.promptActive = false
        session.awaitingPostPromptTargetReturn = true
        session.lastItemStartedAtActiveMillis = activeNow
        session.lastInteractionAtActiveMillis = activeNow
        session.lastEventMillis = now
    }

    fun onContentObserved(
        transitionFingerprint: String?,
        sentiment: SentimentResult?,
        sentimentFingerprint: String? = transitionFingerprint,
    ) {
        val session = active ?: return
        val now = clock()
        val activeNow = session.activeMillisAt(now)
        if (transitionFingerprint != null) {
            val previousFingerprint = session.lastFingerprint
            if (previousFingerprint != null && previousFingerprint != transitionFingerprint) {
                val dwell = session.currentDwellMillis(activeNow)
                if (dwell > 0L) {
                    session.dwellSamples += dwell
                }
                session.swipeCount += 1
                session.lastItemStartedAtActiveMillis = activeNow
                session.lastInteractionAtActiveMillis = activeNow
            }

            session.lastFingerprint = transitionFingerprint
        }
        session.lastEventMillis = now

        if (sentiment != null) {
            val normalizedSentimentFingerprint = sentimentFingerprint?.takeIf { it.isNotBlank() }
            if (normalizedSentimentFingerprint != null && normalizedSentimentFingerprint == session.lastSentimentFingerprint) {
                return
            }
            session.lastSentimentFingerprint = normalizedSentimentFingerprint
            session.totalTokens += sentiment.totalTokens
            session.recognizedTokens += sentiment.recognizedTokens
            if (sentiment.reliable && sentiment.totalTokens > 0) {
                session.resolvableUnits += 1
                if (sentiment.isNegative) session.negativeUnits += 1
            }
        }
    }

    fun addDelayedVlmSentiment(label: edu.feutech.redu.sentiment.VisualSentimentLabel) {
        val session = active ?: return
        
        if (label != edu.feutech.redu.sentiment.VisualSentimentLabel.UNRESOLVED) {
            session.resolvableUnits += 1
            if (label == edu.feutech.redu.sentiment.VisualSentimentLabel.MILD_NEG || 
                label == edu.feutech.redu.sentiment.VisualSentimentLabel.SEVERE_NEG) {
                session.negativeUnits += 1
            }
        }
    }

    fun snapshot(): ActiveSessionSnapshot? = active?.toSnapshot(clock())

    fun finalizeIfInactive(): FinalizedSession? {
        val session = active ?: return null
        val now = clock()
        val backgroundStartedAt = session.backgroundStartedAtMillis ?: return null
        if (now - backgroundStartedAt < BRIDGE_WINDOW_MILLIS) return null
        active = null
        return FinalizedSession(
            snapshot = session.toSnapshot(backgroundStartedAt),
            endedAtMillis = backgroundStartedAt,
        )
    }

    fun forceFinalize(): FinalizedSession? {
        val session = active ?: return null
        val now = clock()
        session.pauseForeground(now)
        active = null
        return FinalizedSession(session.toSnapshot(now), endedAtMillis = now)
    }

    private fun newSession(platform: Platform, now: Long): MutableSession =
        MutableSession(
            platform = platform,
            startedAtMillis = now,
            lastEventMillis = now,
            foregroundStartedAtMillis = now,
        )

    private data class MutableSession(
        val platform: Platform,
        val startedAtMillis: Long,
        var lastEventMillis: Long,
        var foregroundStartedAtMillis: Long?,
        var backgroundStartedAtMillis: Long? = null,
        var accumulatedActiveMillis: Long = 0L,
        var accumulatedPromptExcludedActiveMillis: Long = 0L,
        var promptExcludedForegroundStartedAtMillis: Long? = foregroundStartedAtMillis,
        var lastItemStartedAtActiveMillis: Long = 0L,
        var lastInteractionAtActiveMillis: Long = 0L,
        var lastFingerprint: String? = null,
        var lastSentimentFingerprint: String? = null,
        var promptActive: Boolean = false,
        var awaitingPostPromptTargetReturn: Boolean = false,
        var swipeCount: Int = 0,
        var totalTokens: Int = 0,
        var recognizedTokens: Int = 0,
        var resolvableUnits: Int = 0,
        var negativeUnits: Int = 0,
        val dwellSamples: MutableList<Long> = mutableListOf(),
    ) {
        fun resumeForeground(now: Long) {
            if (foregroundStartedAtMillis == null) {
                foregroundStartedAtMillis = now
                promptExcludedForegroundStartedAtMillis = if (awaitingPostPromptTargetReturn || promptActive) null else now
                backgroundStartedAtMillis = null
                lastInteractionAtActiveMillis = activeMillisAt(now)
            }
            markReturnedFromPrompt(now)
            lastEventMillis = now
        }

        fun pauseForeground(now: Long) {
            foregroundStartedAtMillis?.let { foregroundStartedAt ->
                accumulatedActiveMillis += (now - foregroundStartedAt).coerceAtLeast(0L)
                foregroundStartedAtMillis = null
            }
            promptExcludedForegroundStartedAtMillis?.let { foregroundStartedAt ->
                accumulatedPromptExcludedActiveMillis += (now - foregroundStartedAt).coerceAtLeast(0L)
                promptExcludedForegroundStartedAtMillis = null
            }
            if (backgroundStartedAtMillis == null) {
                backgroundStartedAtMillis = now
            }
        }

        fun pausePromptExcluded(now: Long) {
            promptExcludedForegroundStartedAtMillis?.let { foregroundStartedAt ->
                accumulatedPromptExcludedActiveMillis += (now - foregroundStartedAt).coerceAtLeast(0L)
                promptExcludedForegroundStartedAtMillis = null
            }
        }

        fun markInteraction(now: Long) {
            if (foregroundStartedAtMillis == null) return
            markReturnedFromPrompt(now)
            lastInteractionAtActiveMillis = activeMillisAt(now)
            lastEventMillis = now
        }

        fun markReturnedFromPrompt(now: Long) {
            if (!awaitingPostPromptTargetReturn || promptActive || foregroundStartedAtMillis == null) return
            val activeNow = activeMillisAt(now)
            awaitingPostPromptTargetReturn = false
            lastItemStartedAtActiveMillis = activeNow
            lastInteractionAtActiveMillis = activeNow
            if (promptExcludedForegroundStartedAtMillis == null) {
                promptExcludedForegroundStartedAtMillis = now
            }
        }

        fun activeMillisAt(now: Long): Long {
            val foregroundMillis = foregroundStartedAtMillis
                ?.let { (now - it).coerceAtLeast(0L) }
                ?: 0L
            return accumulatedActiveMillis + foregroundMillis
        }

        fun promptExcludedActiveMillisAt(now: Long): Long {
            val foregroundMillis = promptExcludedForegroundStartedAtMillis
                ?.let { (now - it).coerceAtLeast(0L) }
                ?: 0L
            return accumulatedPromptExcludedActiveMillis + foregroundMillis
        }

        fun currentDwellMillis(activeNow: Long): Long {
            if (promptActive || awaitingPostPromptTargetReturn) return 0L
            val lastCountedActiveMillis = minOf(activeNow, lastInteractionAtActiveMillis + IDLE_TIMEOUT_MILLIS)
            return (lastCountedActiveMillis - lastItemStartedAtActiveMillis).coerceAtLeast(0L)
        }

        fun toSnapshot(now: Long): ActiveSessionSnapshot {
            val activeNow = activeMillisAt(now)
            val durationMillis = activeNow.coerceAtLeast(0L)
            val promptExcludedDurationMillis = promptExcludedActiveMillisAt(now).coerceAtLeast(0L)
            val currentDwellMillis = currentDwellMillis(activeNow)
            val liveDwellSamples = if (currentDwellMillis > 0L) {
                dwellSamples + currentDwellMillis
            } else {
                dwellSamples
            }
            val meanDwellMillis = if (liveDwellSamples.isEmpty()) 0L else liveDwellSamples.average().roundToLong()
            val oovRatio = if (totalTokens == 0) 1.0 else ((totalTokens - recognizedTokens).coerceAtLeast(0)).toDouble() / totalTokens
            val reliable = totalTokens > 0 && oovRatio < 0.50 && resolvableUnits > 0
            val reliability = if (reliable) SentimentReliability.RELIABLE else SentimentReliability.SENTIMENT_UNRELIABLE
            val nsd = if (reliable) negativeUnits.toDouble() / resolvableUnits.toDouble() * 100.0 else null
            val risk = FuzzyRiskEngine.evaluate(
                RiskInputs(
                    meanDwellSeconds = meanDwellMillis / 1000.0,
                    nsdPercent = nsd,
                    sessionDurationMinutes = durationMillis / 60_000.0,
                    sentimentReliable = reliable,
                ),
            )
            return ActiveSessionSnapshot(
                platform = platform,
                startedAtMillis = startedAtMillis,
                lastEventMillis = lastEventMillis,
                swipeCount = swipeCount,
                currentDwellMillis = currentDwellMillis,
                meanDwellMillis = meanDwellMillis,
                durationMillis = durationMillis,
                promptExcludedDurationMillis = promptExcludedDurationMillis,
                resolvableUnits = resolvableUnits,
                negativeUnits = negativeUnits,
                oovRatio = oovRatio,
                nsdPercent = nsd,
                sentimentReliability = reliability,
                riskScore = risk.score,
                riskLevel = risk.level,
            )
        }
    }

    companion object {
        const val BRIDGE_WINDOW_MILLIS = 30_000L
        const val IDLE_TIMEOUT_MILLIS = 45_000L
    }
}
