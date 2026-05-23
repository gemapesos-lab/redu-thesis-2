package edu.feutech.redu.sentiment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugTokenBreakdownTest {
    @Test
    fun classifyingDebugTokensDoesNotChangeAnalyzerResult() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val text = "not good pero sad ito xyz123"
        val before = analyzer.analyze(text)

        val breakdown = analyzer.classifyTokensForDebug(text)
        val after = analyzer.analyze(text)

        assertEquals(before, after)
        assertTrue(breakdown.positiveTokens.contains("good"))
        assertTrue(breakdown.negativeTokens.contains("sad"))
        assertTrue("pero should be in neutralTokens", breakdown.neutralTokens.contains("pero"))
        assertTrue("xyz123 should be in unscoredTokens", breakdown.unscoredTokens.contains("xyz123"))
        assertEquals("OOV ratio should match between analyze and debug breakdown", before.oovRatio, breakdown.oovRatio, 0.0)
    }

    @Test
    fun debugSnippetIsCappedAtFixedLimit() {
        val analyzer = VADERCompatibleAnalyzer()
        val text = "bad ".repeat(100)

        val breakdown = analyzer.classifyTokensForDebug(text)

        assertEquals(DebugTokenBreakdown.SNIPPET_LIMIT, breakdown.snippet.length)
        assertTrue(breakdown.snippet.endsWith("..."))
    }

    @Test
    fun debugBreakdownFiltersUserMentions() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)

        val breakdown = analyzer.classifyTokensForDebug("@creator.name @another_user maganda")

        assertTrue(!breakdown.snippet.contains("@creator.name"))
        assertTrue(!breakdown.unscoredTokens.contains("creator.name"))
        assertTrue(breakdown.positiveTokens.contains("maganda"))
        assertEquals(0.0, breakdown.oovRatio, 0.0)
    }

    @Test
    fun debugBreakdownRecognizesHahaVariations() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)

        val breakdown = analyzer.classifyTokensForDebug("HAHAHHHAHA")

        assertTrue(breakdown.positiveTokens.contains("HAHAHHHAHA"))
        assertTrue(!breakdown.unscoredTokens.contains("HAHAHHHAHA"))
        assertEquals(0.0, breakdown.oovRatio, 0.0)
    }
}
