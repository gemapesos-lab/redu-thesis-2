package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class TikTokAdapterTest {
    @Test
    fun extractionKeepsCommentsForSentimentButUsesOnlyPostDescriptionForTransition() {
        val extraction = TikTokAdapter.extractionFromMatches(
            listOf(
                TikTokCaptionMatch(
                    kind = TikTokCaptionKind.POST_DESCRIPTION,
                    normalizedText = "post caption",
                    reason = "test",
                ),
                TikTokCaptionMatch(
                    kind = TikTokCaptionKind.COMMENT,
                    normalizedText = "first comment",
                    reason = "test",
                ),
                TikTokCaptionMatch(
                    kind = TikTokCaptionKind.COMMENT,
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
        val extraction = TikTokAdapter.extractionFromMatches(
            listOf(
                TikTokCaptionMatch(
                    kind = TikTokCaptionKind.COMMENT,
                    normalizedText = "comment only",
                    reason = "test",
                ),
            ),
        )

        assertEquals("comment only", extraction.sentimentText)
        assertEquals("", extraction.transitionText)
    }

    @Test
    fun extractionUsesAuthorNameForNoCaptionTransitionText() {
        val extraction = TikTokAdapter.extractionFromMatches(
            listOf(
                TikTokCaptionMatch(
                    kind = TikTokCaptionKind.AUTHOR_NAME,
                    normalizedText = "@creator.name",
                    reason = "test",
                ),
            ),
        )

        assertEquals("@creator.name", extraction.sentimentText)
        assertEquals("@creator.name", extraction.transitionText)
        assertEquals(false, extraction.hasCaptionContent)
    }
}
