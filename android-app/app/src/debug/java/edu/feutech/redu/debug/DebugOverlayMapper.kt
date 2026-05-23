package edu.feutech.redu.debug

import edu.feutech.redu.capture.ActiveSessionSnapshot
import edu.feutech.redu.sentiment.DebugTokenBreakdown

object DebugOverlayMapper {
    fun from(
        snapshot: ActiveSessionSnapshot,
        vaderCompound: Double?,
        tokenBreakdown: DebugTokenBreakdown,
        vlmFramesCaptured: Int,
        vlmLastLabel: String,
        vlmStatus: String,
    ): DebugOverlayState =
        DebugOverlayState(
            sessionDurationMillis = snapshot.durationMillis,
            currentDwellMillis = snapshot.currentDwellMillis,
            meanDwellMillis = snapshot.meanDwellMillis,
            swipeCount = snapshot.swipeCount,
            riskScore = snapshot.riskScore,
            riskLevel = snapshot.riskLevel,
            vaderCompound = vaderCompound,
            snippet = tokenBreakdown.snippet,
            negativeTokens = tokenBreakdown.negativeTokens,
            positiveTokens = tokenBreakdown.positiveTokens,
            neutralTokens = tokenBreakdown.neutralTokens,
            unscoredTokens = tokenBreakdown.unscoredTokens,
            oovRatio = tokenBreakdown.oovRatio,
            vlmFramesCaptured = vlmFramesCaptured,
            vlmLastLabel = vlmLastLabel,
            vlmStatus = vlmStatus,
        )
}
