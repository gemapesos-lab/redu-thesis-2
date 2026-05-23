package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FacebookCaptionRulesTest {
    private val rootBounds = TikTokBounds(0, 0, 1080, 2340)

    @Test
    fun lowerReelsCaptionButtonIsSelected() {
        val match = FacebookCaptionRules.classify(
            text = "",
            contentDescription = "5'8\" Dom.dunks with the 360 windmill! \uD83D\uDD25  #dunkademics  #dunk  #basketball",
            className = "android.widget.Button",
            bounds = TikTokBounds(34, 2066, 900, 2107),
            rootBounds = rootBounds,
            visibleToUser = true,
            clickable = true,
            commentSheetSurface = false,
            reelsSurface = true,
        )

        assertEquals(FacebookCaptionKind.POST_DESCRIPTION, match?.kind)
        assertEquals(
            "5'8\" Dom.dunks with the 360 windmill! \uD83D\uDD25 #dunkademics #dunk #basketball",
            match?.normalizedText,
        )
    }

    @Test
    fun fullScreenVideoDescriptionIsIgnoredAsDuplicateCaption() {
        val match = FacebookCaptionRules.classify(
            text = "",
            contentDescription = "5'8\" Dom.dunks with the 360 windmill! \uD83D\uDD25  #dunkademics  #dunk  #basketball",
            className = "android.view.ViewGroup",
            bounds = TikTokBounds(0, 92, 1080, 2140),
            rootBounds = rootBounds,
            visibleToUser = true,
            clickable = true,
            commentSheetSurface = false,
            reelsSurface = true,
        )

        assertNull(match)
    }

    @Test
    fun commentBodyButtonInsideCommentSheetIsSelected() {
        val match = FacebookCaptionRules.classify(
            text = "Don\u2019t let all the old heads see this or they gone say \u201Cit\u2019s not 10 feet!!\u201D",
            contentDescription = "Don\u2019t let all the old heads see this or they gone say \u201Cit\u2019s not 10 feet!!\u201D",
            className = "android.widget.Button",
            bounds = TikTokBounds(169, 965, 1046, 1074),
            rootBounds = rootBounds,
            visibleToUser = true,
            clickable = false,
            ancestorResourceIds = listOf("android:id/content"),
            ancestorClassNames = listOf("android.widget.FrameLayout", "androidx.recyclerview.widget.RecyclerView"),
            commentSheetSurface = true,
            reelsSurface = true,
        )

        assertEquals(FacebookCaptionKind.COMMENT, match?.kind)
        assertEquals(
            "Don\u2019t let all the old heads see this or they gone say \u201Cit\u2019s not 10 feet!!\u201D",
            match?.normalizedText,
        )
    }

    @Test
    fun commentBodyOutsideCommentSheetIsIgnored() {
        val match = FacebookCaptionRules.classify(
            text = "Don\u2019t let all the old heads see this or they gone say \u201Cit\u2019s not 10 feet!!\u201D",
            contentDescription = "Don\u2019t let all the old heads see this or they gone say \u201Cit\u2019s not 10 feet!!\u201D",
            className = "android.widget.Button",
            bounds = TikTokBounds(169, 965, 1046, 1074),
            rootBounds = rootBounds,
            visibleToUser = true,
            clickable = false,
            ancestorResourceIds = listOf("android:id/content"),
            ancestorClassNames = listOf("android.widget.FrameLayout", "androidx.recyclerview.widget.RecyclerView"),
            commentSheetSurface = false,
            reelsSurface = true,
        )

        assertNull(match)
    }

    @Test
    fun usernameReactionNavigationShopAndInputTextAreIgnored() {
        val values = listOf(
            "Billy Dunkademics" to TikTokBounds(158, 1938, 560, 1987),
            "132K reactions" to TikTokBounds(945, 1335, 1080, 1448),
            "1.4k comments" to TikTokBounds(945, 1509, 1080, 1683),
            "Marketplace, tab 4 of 6" to TikTokBounds(540, 2140, 720, 2298),
            "Shop now" to TikTokBounds(540, 2140, 720, 2298),
            "Ad" to TikTokBounds(34, 2004, 166, 2072),
            "Install now" to TikTokBounds(385, 1022, 590, 1079),
            "Play game" to TikTokBounds(390, 1022, 586, 1079),
            "Watch more" to TikTokBounds(376, 1895, 603, 1944),
            "Write a comment\u2026" to TikTokBounds(34, 2175, 1046, 2281),
        )

        values.forEach { (value, bounds) ->
            val match = FacebookCaptionRules.classify(
                text = value,
                contentDescription = value,
                className = "android.widget.Button",
                bounds = bounds,
                rootBounds = rootBounds,
                visibleToUser = true,
                clickable = true,
                ancestorResourceIds = listOf("android:id/content"),
                ancestorClassNames = listOf("android.widget.FrameLayout", "androidx.recyclerview.widget.RecyclerView"),
                commentSheetSurface = true,
                reelsSurface = true,
            )

            assertNull("expected UI text to be ignored: $value", match)
        }
    }

    @Test
    fun facebookTextIsIgnoredWhenNotOnReelsSurface() {
        val match = FacebookCaptionRules.classify(
            text = "",
            contentDescription = "legitimate caption text",
            className = "android.widget.Button",
            bounds = TikTokBounds(34, 2066, 900, 2107),
            rootBounds = rootBounds,
            visibleToUser = true,
            clickable = true,
            commentSheetSurface = false,
            reelsSurface = false,
        )

        assertNull(match)
    }

    // ── Pure allowlist: isReelsTabElement ─────────────────────────────

    @Test
    fun isReelsTabElementMatchesBottomBarTabPatterns() {
        // Standard tab patterns from Facebook's bottom navigation
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement(null, "Reels, tab"))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement(null, "Reels, tab 3 of 6"))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement(null, "Reels, tab 5 of 6"))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement("Reels", null))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement("Reels tab", null))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsTabElement("reels", null))
    }

    @Test
    fun isReelsTabElementRejectsNonReelsContent() {
        // Other tabs — must NOT be detected as Reels
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement("Home", null))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Home, tab"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Home, tab 1 of 6"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Friends, tab 2 of 6"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Profile, tab 6 of 6"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Notifications, tab 4 of 6"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Marketplace, tab 4 of 6"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Watch, tab"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Menu, tab"))

        // Profile page "View reels" button — must NOT be detected
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "View John's reels"))

        // Random content with "reels" in it — must NOT match
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "reels tab details"))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, "Create reel"))

        // Null/blank — must NOT match
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement(null, null))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsTabElement("", ""))
    }

    @Test
    fun isReelsSurfaceTextRequiresSelectedAndDelegatesToTabElement() {
        // Selected + reels tab → true
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsSurfaceText("reels", null, true))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsSurfaceText(null, "reels, tab", true))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isReelsSurfaceText(null, "reels, tab 5 of 6", true))

        // Not selected → always false even with reels text
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText("reels", null, false))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText(null, "reels, tab", false))

        // Non-tab content → false regardless of isSelected
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText("reels tab details", null, false))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText("reels tab details", null, true))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText(null, "View John's reels", false))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isReelsSurfaceText(null, "View John's reels", true))
    }

    @Test
    fun isUnsupportedSurfaceTextOnlyWhenSelected() {
        org.junit.Assert.assertTrue(FacebookCaptionRules.isUnsupportedSurfaceText("friends, tab", null, true))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isUnsupportedSurfaceText(null, "friends, tab 2 of 6", true))
        org.junit.Assert.assertTrue(FacebookCaptionRules.isUnsupportedSurfaceText(null, "home, tab", true))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isUnsupportedSurfaceText("friends, tab", null, false))
        org.junit.Assert.assertFalse(FacebookCaptionRules.isUnsupportedSurfaceText(null, "home, tab", false))

        // Generic ad/install markers should always be unsupported surface markers
        org.junit.Assert.assertTrue(FacebookCaptionRules.isUnsupportedSurfaceText("ad", null, false))
    }
}
