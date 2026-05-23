package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TikTokCaptionRulesTest {
    private val rootBounds = TikTokBounds(0, 0, 1080, 2340)

    @Test
    fun selectedForYouTabMatchesReelsSurface() {
        assertTrue(
            TikTokCaptionRules.isReelsSurfaceMarker(
                text = "For You",
                contentDescription = null,
                resourceId = null,
                isSelected = true,
            ),
        )
    }

    @Test
    fun viewPagerAloneDoesNotMatchReelsSurface() {
        assertFalse(
            TikTokCaptionRules.isReelsSurfaceMarker(
                text = null,
                contentDescription = null,
                resourceId = "com.zhiliaoapp.musically:id/view_pager",
                isSelected = false,
            ),
        )
    }

    @Test
    fun liveSubtitleIdIsNotSelectedAsCaption() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/d71",
            text = "kung saan saang offices dito",
            bounds = TikTokBounds(34, 1675, 608, 1760),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }

    @Test
    fun nonTargetZtwNodeIsIgnoredEvenWhenItContainsText() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/ztw",
            text = "🪷 reposted",
            bounds = TikTokBounds(116, 1778, 326, 1823),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }

    @Test
    fun languageLikeTextFromNonTargetIdIsIgnored() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/ztw",
            text = "where did they go",
            bounds = TikTokBounds(116, 1778, 500, 1823),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }

    @Test
    fun postDescriptionIdIsSelectedAndMoreSuffixIsRemoved() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/desc",
            text = "Ping Lacson has yet to beat hiding😂  more ",
            bounds = TikTokBounds(34, 1855, 855, 1961),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertEquals(TikTokCaptionKind.POST_DESCRIPTION, match?.kind)
        assertEquals("Ping Lacson has yet to beat hiding😂", match?.normalizedText)
    }

    @Test
    fun postDescriptionTruncatedEllipsisMoreSuffixIsRemoved() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/desc",
            text = "Because you loved me @Auriz Llorens #fypppppppppppppppppppppp…more",
            bounds = TikTokBounds(34, 1855, 855, 1961),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertEquals(
            "Because you loved me @Auriz Llorens #fypppppppppppppppppppppp",
            match?.normalizedText,
        )
    }

    @Test
    fun tiktokActionButtonsAreNotCaptionCandidates() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e8n",
            text = "Read or add comments. 155 comments",
            bounds = TikTokBounds(900, 1378, 1080, 1547),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }

    @Test
    fun visibleCommentBodyIdIsSelected() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/emz",
            text = "alam naman nating lahat na ayaw ni Keira na sinisigawan siya",
            bounds = TikTokBounds(179, 1716, 1046, 1833),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/sh4",
                "com.ss.android.ugc.trill:id/m1a",
            ),
        )

        assertEquals(TikTokCaptionKind.COMMENT, match?.kind)
        assertEquals(
            "alam naman nating lahat na ayaw ni Keira na sinisigawan siya",
            match?.normalizedText,
        )
    }

    @Test
    fun currentTikTokCommentBodyIdIsSelected() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/enn",
            text = "Ayoko, gusto ko sya 🥺",
            bounds = TikTokBounds(179, 1528, 1046, 1592),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/hv5",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/m3q",
                "com.ss.android.ugc.trill:id/ekt",
            ),
        )

        assertEquals(TikTokCaptionKind.COMMENT, match?.kind)
        assertEquals("Ayoko, gusto ko sya 🥺", match?.normalizedText)
    }

    @Test
    fun currentTikTokCommentBodyIdIsSelectedWithoutLegacyWrapperAncestor() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/enn",
            text = "kaso minsan mapapaisip kana lang din e, kung pinapasuko kana ba nya kasi mali yon",
            bounds = TikTokBounds(179, 1063, 1046, 1286),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/m3q",
            ),
        )

        assertEquals(TikTokCaptionKind.COMMENT, match?.kind)
        assertEquals(
            "kaso minsan mapapaisip kana lang din e, kung pinapasuko kana ba nya kasi mali yon",
            match?.normalizedText,
        )
    }

    @Test
    fun liveTikTokCommentBodyIdIsSelected() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/emb",
            text = "Te tuwing kelan ka wala sa bahay niyo?",
            bounds = TikTokBounds(179, 1063, 1046, 1127),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/sc8",
                "com.ss.android.ugc.trill:id/lyq",
                "com.ss.android.ugc.trill:id/ejh",
            ),
        )

        assertEquals(TikTokCaptionKind.COMMENT, match?.kind)
        assertEquals("Te tuwing kelan ka wala sa bahay niyo?", match?.normalizedText)
    }

    @Test
    fun commentBodyIdOutsideCommentListIsIgnored() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/emz",
            text = "alam naman nating lahat na ayaw ni Keira na sinisigawan siya",
            bounds = TikTokBounds(179, 1716, 1046, 1833),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }

    @Test
    fun hiddenCommentBodyIsIgnored() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/emz",
            text = "Look at this whale😭",
            bounds = TikTokBounds(179, 990, 1046, 830),
            rootBounds = rootBounds,
            visibleToUser = false,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/sh4",
                "com.ss.android.ugc.trill:id/m1a",
            ),
        )

        assertNull(match)
    }

    @Test
    fun currentCommentSheetMetadataAndInputAreIgnored() {
        val username = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/title",
            text = "Monkey D. CHARLS ！！",
            bounds = TikTokBounds(179, 2010, 600, 2055),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/hv5",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/m3q",
                "com.ss.android.ugc.trill:id/ekt",
            ),
        )
        val date = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e8f",
            text = "03-03",
            bounds = TikTokBounds(179, 1604, 279, 1646),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/hv5",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/m3q",
                "com.ss.android.ugc.trill:id/ekt",
                "com.ss.android.ugc.trill:id/e8i",
            ),
        )
        val reply = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e78",
            text = "Reply",
            bounds = TikTokBounds(324, 1601, 424, 1646),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/hv5",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/m3q",
                "com.ss.android.ugc.trill:id/ekt",
                "com.ss.android.ugc.trill:id/e8i",
            ),
        )
        val viewReplies = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/zn0",
            text = "View 29 replies",
            bounds = TikTokBounds(243, 1920, 511, 1965),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/e6u",
                "com.ss.android.ugc.trill:id/hv5",
                "com.ss.android.ugc.trill:id/sk7",
                "com.ss.android.ugc.trill:id/tb3",
                "com.ss.android.ugc.trill:id/m3e",
                "com.ss.android.ugc.trill:id/h89",
            ),
        )
        val input = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e4y",
            text = "Add comment...",
            bounds = TikTokBounds(202, 2151, 697, 2238),
            rootBounds = rootBounds,
            visibleToUser = true,
            ancestorResourceIds = listOf(
                "com.ss.android.ugc.trill:id/hbl",
                "com.ss.android.ugc.trill:id/kk_",
                "com.ss.android.ugc.trill:id/e4f",
                "com.ss.android.ugc.trill:id/e4x",
                "com.ss.android.ugc.trill:id/kge",
                "com.ss.android.ugc.trill:id/e4w",
            ),
        )

        assertNull(username)
        assertNull(date)
        assertNull(reply)
        assertNull(viewReplies)
        assertNull(input)
    }

    @Test
    fun commentSheetMetadataAndInputAreIgnored() {
        val username = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/title",
            text = "pinoymlbb",
            bounds = TikTokBounds(179, 1359, 366, 1404),
            rootBounds = rootBounds,
            visibleToUser = true,
        )
        val reply = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e6k",
            text = "Reply",
            bounds = TikTokBounds(272, 1483, 372, 1528),
            rootBounds = rootBounds,
            visibleToUser = true,
        )
        val replyCount = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/zi5",
            text = "View 61 replies",
            bounds = TikTokBounds(243, 1575, 504, 1620),
            rootBounds = rootBounds,
            visibleToUser = true,
        )
        val input = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/e43",
            text = "Add comment...",
            bounds = TikTokBounds(202, 2151, 697, 2238),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(username)
        assertNull(reply)
        assertNull(replyCount)
        assertNull(input)
    }

    @Test
    fun authorLikeResourceIdIsSelectedAsUsernameTransitionAnchor() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/creator_nickname",
            text = "@pinoymlbb",
            bounds = TikTokBounds(65, 1690, 420, 1745),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertEquals(TikTokCaptionKind.AUTHOR_NAME, match?.kind)
        assertEquals("@pinoymlbb", match?.normalizedText)
    }

    @Test
    fun contentDescriptionAuthorNodeIsSelectedAsUsernameTransitionAnchor() {
        val match = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/profile_name",
            text = null,
            contentDescription = "@creator.name",
            bounds = TikTokBounds(65, 1690, 500, 1745),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertEquals(TikTokCaptionKind.AUTHOR_NAME, match?.kind)
        assertEquals("@creator.name", match?.normalizedText)
    }

    @Test
    fun liveCommentSheetMarkersAreRecognized() {
        assertTrue(
            TikTokCaptionRules.isCommentSheetSurfaceMarker(
                text = "\u200E806 comments",
                contentDescription = null,
                resourceId = "com.ss.android.ugc.trill:id/up7",
                isSelected = false,
            ),
        )
        assertTrue(
            TikTokCaptionRules.isCommentSheetSurfaceMarker(
                text = "Add comment...",
                contentDescription = null,
                resourceId = "com.ss.android.ugc.trill:id/e4b",
                isSelected = false,
            ),
        )
    }

    @Test
    fun bottomSearchAndNavigationTextAreNotCaptionCandidates() {
        val search = TikTokCaptionRules.classify(
            resourceId = "com.ss.android.ugc.trill:id/tst",
            text = "Search · gab lagman at ping lacson",
            bounds = TikTokBounds(90, 2087, 991, 2132),
            rootBounds = rootBounds,
            visibleToUser = true,
        )
        val navigation = TikTokCaptionRules.classify(
            resourceId = "android:id/text1",
            text = "Explore",
            bounds = TikTokBounds(40, 2140, 200, 2200),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(search)
        assertNull(navigation)
    }

    @Test
    fun loginScreenTextIsNotCaptionCandidate() {
        val match = TikTokCaptionRules.classify(
            resourceId = null,
            text = "Log in to existing account",
            bounds = TikTokBounds(300, 1028, 780, 1080),
            rootBounds = rootBounds,
            visibleToUser = true,
        )

        assertNull(match)
    }
}
