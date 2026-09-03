package edu.feutech.redu.sentiment

import org.junit.Assert.assertEquals
import org.junit.Test

class NativeVisualSentimentResolverTest {
    @Test
    fun parseVisualSentimentLabelAcceptsPrefixedKnownLabels() {
        assertEquals(
            VisualSentimentLabel.MILD_NEG,
            parseVisualSentimentLabel("Label: MILD_NEG"),
        )
        assertEquals(
            VisualSentimentLabel.SEVERE_POS,
            parseVisualSentimentLabel("The best answer is SEVERE_POS."),
        )
    }

    @Test
    fun parseVisualSentimentLabelRejectsUnknownOrPartialLabels() {
        assertEquals(
            VisualSentimentLabel.UNRESOLVED,
            parseVisualSentimentLabel("MILD_NEGATIVE"),
        )
        assertEquals(
            VisualSentimentLabel.UNRESOLVED,
            parseVisualSentimentLabel("unclear"),
        )
    }

    @Test
    fun parseVisualSentimentLabelRejectsAmbiguousResponses() {
        assertEquals(
            VisualSentimentLabel.UNRESOLVED,
            parseVisualSentimentLabel("MILD_NEG or NEUTRAL"),
        )
    }

    @Test
    fun majorityVoteRejectsTiesAndPluralities() {
        assertEquals(
            VisualSentimentLabel.UNRESOLVED,
            majorityVisualSentimentLabel(
                listOf(VisualSentimentLabel.MILD_NEG, VisualSentimentLabel.MILD_POS),
            ),
        )
        assertEquals(
            VisualSentimentLabel.UNRESOLVED,
            majorityVisualSentimentLabel(
                listOf(
                    VisualSentimentLabel.MILD_NEG,
                    VisualSentimentLabel.NEUTRAL,
                    VisualSentimentLabel.MILD_POS,
                ),
            ),
        )
    }

    @Test
    fun majorityVoteReturnsStrictMajorityOfResolvedVotes() {
        assertEquals(
            VisualSentimentLabel.MILD_NEG,
            majorityVisualSentimentLabel(
                listOf(
                    VisualSentimentLabel.MILD_NEG,
                    VisualSentimentLabel.MILD_NEG,
                    VisualSentimentLabel.MILD_POS,
                    VisualSentimentLabel.UNRESOLVED,
                ),
            ),
        )
    }
}
