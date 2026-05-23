package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCaptionRulesTest {
    private val rootBounds = TikTokBounds(0, 0, 1080, 2340)

    @Test
    fun captionDescriptionUnderClipsCaptionComponentIsSelected() {
        val match = InstagramCaptionRules.classify(
            resourceId = "",
            text = null,
            contentDescription = "sorry po lag lang #fyp …",
            bounds = TikTokBounds(45, 2030, 933, 2096),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/clips_caption_component"),
        )

        assertEquals(InstagramCaptionKind.POST_DESCRIPTION, match?.kind)
        assertEquals("sorry po lag lang #fyp", match?.normalizedText)
    }

    @Test
    fun seeMorePlaceholderUnderClipsCaptionComponentIsIgnored() {
        val match = InstagramCaptionRules.classify(
            resourceId = "",
            text = null,
            contentDescription = "See more",
            bounds = TikTokBounds(45, 2030, 933, 2096),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/clips_caption_component"),
        )

        assertNull(match)
    }

    @Test
    fun reelVideoDescriptionIsIgnored() {
        val match = InstagramCaptionRules.classify(
            resourceId = "com.instagram.android:id/clips_video_container",
            text = null,
            contentDescription = "Reel by horixxvzi. Double-tap to play or pause.",
            bounds = TikTokBounds(0, 91, 1080, 2163),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = emptyList(),
        )

        assertNull(match)
    }

    @Test
    fun commentTextUnderStickyHeaderListRemovesUsernamePrefix() {
        val match = InstagramCaptionRules.classify(
            resourceId = "",
            text = "_ninongmark said Random: ampake namin baket asa school ka ba ngayon engot nasa ml ka kaya ayusin mo",
            contentDescription = "_ninongmark said Random: ampake namin baket asa school ka ba ngayon engot nasa ml ka kaya ayusin mo",
            bounds = TikTokBounds(183, 1127, 922, 1264),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/sticky_header_list"),
        )

        assertEquals(InstagramCaptionKind.COMMENT, match?.kind)
        assertEquals(
            "Random: ampake namin baket asa school ka ba ngayon engot nasa ml ka kaya ayusin mo",
            match?.normalizedText,
        )
    }

    @Test
    fun commentMetadataAndUiTextAreIgnored() {
        val username = InstagramCaptionRules.classify(
            resourceId = "",
            text = "_ninongmark",
            contentDescription = "_ninongmark",
            bounds = TikTokBounds(183, 1072, 397, 1116),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/sticky_header_list"),
        )
        val date = InstagramCaptionRules.classify(
            resourceId = "",
            text = "24 April",
            contentDescription = "24 April",
            bounds = TikTokBounds(400, 1072, 448, 1116),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/sticky_header_list"),
        )
        val reply = InstagramCaptionRules.classify(
            resourceId = "",
            text = "Reply",
            contentDescription = "Reply",
            bounds = TikTokBounds(183, 1264, 319, 1349),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/sticky_header_list"),
        )
        val viewReplies = InstagramCaptionRules.classify(
            resourceId = "",
            text = "View 22 more replies",
            contentDescription = "View 22 more replies",
            bounds = TikTokBounds(270, 1349, 608, 1393),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf("com.instagram.android:id/sticky_header_list"),
        )

        assertNull(username)
        assertNull(date)
        assertNull(reply)
        assertNull(viewReplies)
    }

    // ── isReelsSurfaceMarker ────────────────────────────────────────────

    @Test
    fun reelsTextAloneWithoutClipsTabIdDoesNotMatch() {
        // This was the bug: text="Reels" with no resource-id was triggering
        // on non-Reels pages because a phantom node had cd="Reels" + isSelected=true
        assertFalse(
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = "Reels",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun clipsTabResourceIdMatchesReelsSurface() {
        assertTrue(
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = null,
                contentDescription = "Reels",
                resourceId = "com.instagram.android:id/clips_tab",
                isSelected = true,
            ),
        )
    }

    @Test
    fun reelsTabNotSelectedDoesNotMatchReelsSurface() {
        assertFalse(
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = "Reels",
                contentDescription = null,
                resourceId = null,
                isSelected = false,
            ),
        )
    }

    @Test
    fun reelsCommaTabSelectedMatchesReelsSurface() {
        assertTrue(
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = null,
                contentDescription = "Reels, tab",
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun reelsTabTextSelectedMatchesReelsSurface() {
        assertTrue(
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = "Reels tab",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun clipsVideoContainerAloneDoesNotMatchReelsSurface() {
        assertFalse(
            "clips_video_container should NOT trigger reels detection by itself",
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = null,
                contentDescription = "Reel by horixxvzi. Double-tap to play or pause.",
                resourceId = "com.instagram.android:id/clips_video_container",
                isSelected = false,
            ),
        )
    }

    @Test
    fun clipsViewerViewPagerAloneDoesNotMatchReelsSurface() {
        assertFalse(
            "clips_viewer_view_pager should NOT trigger reels detection by itself",
            InstagramCaptionRules.isReelsSurfaceMarker(
                text = null,
                contentDescription = null,
                resourceId = "com.instagram.android:id/clips_viewer_view_pager",
                isSelected = false,
            ),
        )
    }

    // ── isUnsupportedSurfaceMarker ──────────────────────────────────────

    @Test
    fun homeTabSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = "Home",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun homeTabNotSelectedIsNotUnsupportedSurface() {
        assertFalse(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = "Home",
                contentDescription = null,
                resourceId = null,
                isSelected = false,
            ),
        )
    }

    @Test
    fun searchAndExploreSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = "Search & Explore",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun searchAndExploreCommaTabSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = null,
                contentDescription = "Search & Explore, tab",
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun profileSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = "Profile",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun profileCommaTabSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = null,
                contentDescription = "Profile, tab",
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun reelsTabSelectedIsNotUnsupportedSurface() {
        assertFalse(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = "Reels",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun homeCommaTabSelectedIsUnsupportedSurface() {
        assertTrue(
            InstagramCaptionRules.isUnsupportedSurfaceMarker(
                text = null,
                contentDescription = "Home, tab",
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun commentSheetMarkerUsesContentDescription() {
        assertTrue(
            InstagramCaptionRules.isCommentSheetSurfaceMarker(
                text = null,
                contentDescription = "Comments",
                resourceId = null,
            ),
        )
    }

    @Test
    fun commentSheetMarkerMatchesLiveReplyCountRows() {
        assertTrue(
            InstagramCaptionRules.isCommentSheetSurfaceMarker(
                text = "555 replies",
                contentDescription = null,
                resourceId = "com.instagram.android:id/ig_text",
            ),
        )
    }
}
