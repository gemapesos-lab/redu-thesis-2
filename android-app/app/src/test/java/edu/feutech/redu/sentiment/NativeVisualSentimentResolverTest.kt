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
}
