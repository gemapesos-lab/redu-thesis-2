package edu.feutech.redu.debug

import edu.feutech.redu.data.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugOverlayStateTest {
    @Test
    fun compactLinesFormatMetricsForSmallPanel() {
        val state = state()

        assertEquals("73", state.chipText())
        assertEquals("72.6 WARNING", state.riskBadgeText())
        assertEquals("Session 1:01.2  Dwell 0:04.5  Mean 0:12.3  Swipes 7", state.sessionLine())
        assertEquals("VADER -0.321  OOV 27.5%", state.sentimentLine())
        assertEquals("VLM inferring  frames=1  last=MILD_NEG", state.vlmLine())
    }

    @Test
    fun tokenLineCapsLongLists() {
        val state = state(negativeTokens = listOf("bad", "worse", "awful", "hate"))

        assertEquals("NEG bad, worse, awful +1", state.tokenLine("NEG", state.negativeTokens))
        assertEquals("POS none", state.tokenLine("POS", emptyList()))
    }

    @Test
    fun snippetIsEllipsizedForExpandedPanel() {
        val state = state(snippet = "x".repeat(140))

        val compact = state.compactSnippet(maxChars = 24)

        assertEquals(24, compact.length)
        assertTrue(compact.endsWith("..."))
    }

    private fun state(
        snippet: String = "short text",
        negativeTokens: List<String> = listOf("bad"),
    ): DebugOverlayState =
        DebugOverlayState(
            sessionDurationMillis = 61_234L,
            currentDwellMillis = 4_567L,
            meanDwellMillis = 12_345L,
            swipeCount = 7,
            riskScore = 72.6,
            riskLevel = RiskLevel.WARNING,
            vaderCompound = -0.3214,
            snippet = snippet,
            negativeTokens = negativeTokens,
            positiveTokens = emptyList(),
            neutralTokens = listOf("ok"),
            unscoredTokens = listOf("x", "y", "z", "q"),
            oovRatio = 0.275,
            vlmFramesCaptured = 1,
            vlmLastLabel = "MILD_NEG",
            vlmStatus = "inferring",
        )
}
