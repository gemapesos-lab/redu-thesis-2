package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class InstagramAdapterTest {
    @Test
    fun extractionKeepsCommentsForSentimentButUsesOnlyPostDescriptionForTransition() {
        val extraction = InstagramAdapter.extractionFromMatches(
            listOf(
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.POST_DESCRIPTION,
                    normalizedText = "post caption",
                    reason = "test",
                ),
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.COMMENT,
                    normalizedText = "first comment",
                    reason = "test",
                ),
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.COMMENT,
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
        val extraction = InstagramAdapter.extractionFromMatches(
            listOf(
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.COMMENT,
                    normalizedText = "comment only",
                    reason = "test",
                ),
            ),
        )

        assertEquals("comment only", extraction.sentimentText)
        assertEquals("", extraction.transitionText)
    }

    @Test
    fun extractionUsesAuthorNameOnlyForTransitionNotSentiment() {
        val extraction = InstagramAdapter.extractionFromMatches(
            listOf(
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.AUTHOR_NAME,
                    normalizedText = "@creator.name",
                    reason = "test",
                ),
            ),
        )

        assertEquals("", extraction.sentimentText)
        assertEquals("@creator.name", extraction.transitionText)
        assertEquals(false, extraction.hasCaptionContent)
    }

    @Test
    fun extractionExcludesAuthorNameFromMixedSentimentText() {
        val extraction = InstagramAdapter.extractionFromMatches(
            listOf(
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.AUTHOR_NAME,
                    normalizedText = "@creator.name",
                    reason = "test",
                ),
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.POST_DESCRIPTION,
                    normalizedText = "post caption",
                    reason = "test",
                ),
                InstagramCaptionMatch(
                    kind = InstagramCaptionKind.COMMENT,
                    normalizedText = "first comment",
                    reason = "test",
                ),
            ),
        )

        assertEquals("post caption first comment", extraction.sentimentText)
        assertEquals("@creator.name post caption", extraction.transitionText)
    }
}
