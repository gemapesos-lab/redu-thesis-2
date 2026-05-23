package edu.feutech.redu.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugAccessibilityTreeSerializerTest {
    @Test
    fun dumpIncludesResourceIdsFullTextAndNodeFlags() {
        val root = DebugNodeSnapshot(
            path = "0",
            depth = 0,
            className = "android.widget.FrameLayout",
            packageName = "com.zhiliaoapp.musically",
            viewIdResourceName = "com.zhiliaoapp.musically:id/root",
            text = null,
            contentDescription = null,
            boundsInScreen = "[0,0][1080,2400]",
            visibleToUser = true,
            enabled = true,
            clickable = false,
            scrollable = false,
            childCount = 1,
            children = listOf(
                DebugNodeSnapshot(
                    path = "0/0",
                    depth = 1,
                    className = "android.widget.TextView",
                    packageName = "com.zhiliaoapp.musically",
                    viewIdResourceName = "com.zhiliaoapp.musically:id/comment_text",
                    text = "full raw comment text for debug mapping",
                    contentDescription = "comment",
                    boundsInScreen = "[20,1200][900,1300]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = true,
                    scrollable = false,
                    childCount = 0,
                ),
            ),
        )

        val dump = DebugAccessibilityTreeSerializer.dump(root)

        assertEquals(2, dump.nodeCount)
        assertTrue(dump.text.contains("viewIdResourceName=com.zhiliaoapp.musically:id/comment_text"))
        assertTrue(dump.text.contains("text=full raw comment text for debug mapping"))
        assertTrue(dump.text.contains("contentDescription=comment"))
        assertTrue(dump.text.contains("clickable=true"))
    }

    @Test
    fun dumpHandlesNullRoot() {
        val dump = DebugAccessibilityTreeSerializer.dump(null)

        assertEquals(0, dump.nodeCount)
        assertTrue(dump.text.contains("Accessibility root: null"))
    }

    @Test
    fun captionCandidateDumpReportsObservedPlatformCaptionRulesOnly() {
        val root = DebugNodeSnapshot(
            path = "0",
            depth = 0,
            className = "android.widget.FrameLayout",
            packageName = "com.ss.android.ugc.trill",
            viewIdResourceName = "",
            text = null,
            contentDescription = null,
            boundsInScreen = "[0,0][1080,2340]",
            visibleToUser = true,
            enabled = true,
            clickable = false,
            scrollable = false,
            childCount = 6,
            children = listOf(
                DebugNodeSnapshot(
                    path = "0/0",
                    depth = 1,
                    className = "X.15IE",
                    packageName = "com.ss.android.ugc.trill",
                    viewIdResourceName = "com.ss.android.ugc.trill:id/d71",
                    text = "kung saan saang offices dito",
                    contentDescription = null,
                    boundsInScreen = "[34,1675][608,1760]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = false,
                    scrollable = false,
                    childCount = 0,
                ),
                DebugNodeSnapshot(
                    path = "0/1",
                    depth = 1,
                    className = "X.15IE",
                    packageName = "com.ss.android.ugc.trill",
                    viewIdResourceName = "com.ss.android.ugc.trill:id/desc",
                    text = "Ping Lacson has yet to beat hiding😂  more ",
                    contentDescription = null,
                    boundsInScreen = "[34,1855][855,1961]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = true,
                    scrollable = false,
                    childCount = 0,
                ),
                DebugNodeSnapshot(
                    path = "0/2",
                    depth = 1,
                    className = "android.widget.Button",
                    packageName = "com.ss.android.ugc.trill",
                    viewIdResourceName = "com.ss.android.ugc.trill:id/e8n",
                    text = null,
                    contentDescription = "Read or add comments. 155 comments",
                    boundsInScreen = "[900,1378][1080,1547]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = true,
                    scrollable = false,
                    childCount = 0,
                ),
                DebugNodeSnapshot(
                    path = "0/3",
                    depth = 1,
                    className = "androidx.recyclerview.widget.RecyclerView",
                    packageName = "com.ss.android.ugc.trill",
                    viewIdResourceName = "com.ss.android.ugc.trill:id/sh4",
                    text = null,
                    contentDescription = null,
                    boundsInScreen = "[0,990][1080,2127]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = false,
                    scrollable = true,
                    childCount = 1,
                    children = listOf(
                        DebugNodeSnapshot(
                            path = "0/3/0",
                            depth = 2,
                            className = "android.widget.FrameLayout",
                            packageName = "com.ss.android.ugc.trill",
                            viewIdResourceName = "com.ss.android.ugc.trill:id/m1a",
                            text = null,
                            contentDescription = null,
                            boundsInScreen = "[0,1337][1080,1564]",
                            visibleToUser = true,
                            enabled = true,
                            clickable = false,
                            scrollable = false,
                            childCount = 1,
                            children = listOf(
                                DebugNodeSnapshot(
                                    path = "0/3/0/0",
                                    depth = 3,
                                    className = "android.widget.TextView",
                                    packageName = "com.ss.android.ugc.trill",
                                    viewIdResourceName = "com.ss.android.ugc.trill:id/emz",
                                    text = "May hindi ba nakakaintindi sa larong ito?",
                                    contentDescription = null,
                                    boundsInScreen = "[179,1410][1046,1474]",
                                    visibleToUser = true,
                                    enabled = true,
                                    clickable = false,
                                    scrollable = false,
                                    childCount = 0,
                                ),
                            ),
                        ),
                    ),
                ),
                DebugNodeSnapshot(
                    path = "0/4",
                    depth = 1,
                    className = "android.view.ViewGroup",
                    packageName = "com.instagram.android",
                    viewIdResourceName = "com.instagram.android:id/clips_caption_component",
                    text = null,
                    contentDescription = null,
                    boundsInScreen = "[45,2019][933,2096]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = false,
                    scrollable = false,
                    childCount = 1,
                    children = listOf(
                        DebugNodeSnapshot(
                            path = "0/4/0",
                            depth = 2,
                            className = "android.view.ViewGroup",
                            packageName = "com.instagram.android",
                            viewIdResourceName = "",
                            text = null,
                            contentDescription = "sorry po lag lang #fyp …",
                            boundsInScreen = "[45,2030][933,2096]",
                            visibleToUser = true,
                            enabled = true,
                            clickable = true,
                            scrollable = false,
                            childCount = 0,
                        ),
                    ),
                ),
                DebugNodeSnapshot(
                    path = "0/5",
                    depth = 1,
                    className = "android.widget.FrameLayout",
                    packageName = "com.facebook.katana",
                    viewIdResourceName = "",
                    text = null,
                    contentDescription = null,
                    boundsInScreen = "[0,0][1080,2340]",
                    visibleToUser = true,
                    enabled = true,
                    clickable = false,
                    scrollable = false,
                    childCount = 2,
                    children = listOf(
                        DebugNodeSnapshot(
                            path = "0/5/0",
                            depth = 2,
                            className = "android.view.View",
                            packageName = "com.facebook.katana",
                            viewIdResourceName = "",
                            text = "Reels",
                            contentDescription = "Reels",
                            boundsInScreen = "[161,162][332,211]",
                            visibleToUser = true,
                            enabled = true,
                            clickable = false,
                            scrollable = false,
                            selected = true,
                            childCount = 0,
                        ),
                        DebugNodeSnapshot(
                            path = "0/5/1",
                            depth = 2,
                            className = "android.widget.Button",
                            packageName = "com.facebook.katana",
                            viewIdResourceName = "",
                            text = null,
                            contentDescription = "5’8” Dom.dunks with the 360 windmill! 🔥 #dunk",
                            boundsInScreen = "[34,2066][900,2107]",
                            visibleToUser = true,
                            enabled = true,
                            clickable = true,
                            scrollable = false,
                            childCount = 0,
                        ),
                    ),
                ),
            ),
        )

        val dump = DebugAccessibilityTreeSerializer.captionCandidates(root)

        assertEquals(4, dump.candidates.size)
        assertTrue(!dump.text.contains("viewIdResourceName=com.ss.android.ugc.trill:id/d71"))
        assertTrue(dump.text.contains("viewIdResourceName=com.ss.android.ugc.trill:id/desc"))
        assertTrue(dump.text.contains("viewIdResourceName=com.ss.android.ugc.trill:id/emz"))
        assertTrue(dump.text.contains("Ping Lacson has yet to beat hiding😂"))
        assertTrue(dump.text.contains("May hindi ba nakakaintindi sa larong ito?"))
        assertTrue(dump.text.contains("sorry po lag lang #fyp"))
        assertTrue(dump.text.contains("5’8” Dom.dunks with the 360 windmill! 🔥 #dunk"))
        assertTrue(!dump.text.contains("Read or add comments"))
    }
}
