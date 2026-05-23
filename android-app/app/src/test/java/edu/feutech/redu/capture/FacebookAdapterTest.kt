package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class FacebookAdapterTest {
    @Test
    fun extractionKeepsCommentsForSentimentButUsesOnlyPostDescriptionForTransition() {
        val extraction = FacebookAdapter.extractionFromMatches(
            listOf(
                FacebookCaptionMatch(
                    kind = FacebookCaptionKind.POST_DESCRIPTION,
                    normalizedText = "post caption",
                    reason = "test",
                ),
                FacebookCaptionMatch(
                    kind = FacebookCaptionKind.COMMENT,
                    normalizedText = "first comment",
                    reason = "test",
                ),
                FacebookCaptionMatch(
                    kind = FacebookCaptionKind.COMMENT,
                    normalizedText = "second comment",
                    reason = "test",
                ),
            ),
        )

        assertEquals("post caption first comment second comment", extraction.sentimentText)
        assertEquals("post caption", extraction.transitionText)
    }

    @Test
    fun extractionWithoutPostDescriptionHasNoTransitionText() {
        val extraction = FacebookAdapter.extractionFromMatches(
            listOf(
                FacebookCaptionMatch(
                    kind = FacebookCaptionKind.COMMENT,
                    normalizedText = "comment only",
                    reason = "test",
                ),
            ),
        )

        assertEquals("comment only", extraction.sentimentText)
        assertEquals("", extraction.transitionText)
    }
}
