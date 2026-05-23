package edu.feutech.redu.capture

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.BuildConfig

object FacebookAdapter {
    const val PACKAGE_NAME = "com.facebook.katana"
    val PACKAGE_NAMES = setOf(PACKAGE_NAME)

    fun supports(packageName: CharSequence?): Boolean = packageName?.toString() in PACKAGE_NAMES

    /**
     * Pure allowlist approach: scans the accessibility tree for tab elements
     * and returns true ONLY if the currently selected tab is "Reels".
     * Does NOT use a blocklist — any page that is not Reels is simply not supported.
     */
    fun isReelsSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        return root.hasSelectedReelsTab()
    }

    fun isCommentSheetSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        return root.containsCommentSheetMarker()
    }

    fun isReelsSurface(roots: List<AccessibilityNodeInfo>): Boolean {
        return roots.any { root ->
            root.packageName?.toString() in PACKAGE_NAMES &&
            root.hasSelectedReelsTab()
        }
    }

    fun extract(root: AccessibilityNodeInfo?, reelsSurfaceConfirmed: Boolean = false): PlatformTextExtraction {
        if (root == null) return PlatformTextExtraction(sentimentText = "", transitionText = "")
        val rootBounds = root.bounds()
        val reelsSurface = reelsSurfaceConfirmed || isReelsSurface(root)
        val commentSheetSurface = root.containsCommentSheetMarker()
        val matches = mutableListOf<FacebookCaptionMatch>()
        collectText(
            node = root,
            rootBounds = rootBounds,
            values = matches,
            ancestorResourceIds = emptyList(),
            ancestorClassNames = emptyList(),
            commentSheetSurface = commentSheetSurface,
            reelsSurface = reelsSurface,
        )
        return extractionFromMatches(matches)
    }

    internal fun extractionFromMatches(matches: List<FacebookCaptionMatch>): PlatformTextExtraction {
        val sentimentText = matches.renderText()
        val transitionText = matches
            .filter { it.kind == FacebookCaptionKind.POST_DESCRIPTION }
            .renderText()
        val hasCaptionContent = matches.any {
            it.kind == FacebookCaptionKind.POST_DESCRIPTION || it.kind == FacebookCaptionKind.COMMENT
        }

        return PlatformTextExtraction(
            sentimentText = sentimentText,
            transitionText = transitionText,
            hasCaptionContent = hasCaptionContent,
        )
    }

    private fun List<FacebookCaptionMatch>.renderText(): String {
        if (isEmpty()) return ""
        return asSequence()
            .sortedWith(
                compareBy<FacebookCaptionMatch> {
                    when (it.kind) {
                        FacebookCaptionKind.POST_DESCRIPTION -> 0
                        FacebookCaptionKind.COMMENT -> 1
                    }
                },
            )
            .map { it.normalizedText }
            .distinct()
            .joinToString(separator = " ")
    }

    private fun collectText(
        node: AccessibilityNodeInfo,
        rootBounds: TikTokBounds,
        values: MutableList<FacebookCaptionMatch>,
        ancestorResourceIds: List<String>,
        ancestorClassNames: List<String>,
        commentSheetSurface: Boolean,
        reelsSurface: Boolean,
    ) {
        FacebookCaptionRules.classify(
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className,
            bounds = node.bounds(),
            rootBounds = rootBounds,
            visibleToUser = node.isVisibleToUser,
            clickable = node.isClickable,
            ancestorResourceIds = ancestorResourceIds,
            ancestorClassNames = ancestorClassNames,
            commentSheetSurface = commentSheetSurface,
            reelsSurface = reelsSurface,
        )?.let(values::add)

        val childResourceAncestors = ancestorResourceIds + listOfNotNull(node.viewIdResourceName)
        val childClassAncestors = ancestorClassNames + listOfNotNull(node.className?.toString())
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child ->
                try {
                    collectText(
                        node = child,
                        rootBounds = rootBounds,
                        values = values,
                        ancestorResourceIds = childResourceAncestors,
                        ancestorClassNames = childClassAncestors,
                        commentSheetSurface = commentSheetSurface,
                        reelsSurface = reelsSurface,
                    )
                } finally {
                    child.recycle()
                }
            }
        }
    }

    /**
     * Single-pass allowlist scan: walks the entire tree looking for tab elements.
     * Returns true ONLY if a "Reels" tab is found AND it is the selected tab.
     * Returns false for ALL other cases (home, friends, profile, etc.).
     *
     * A node is considered a "tab" if its text/contentDescription contains the word "tab"
     * or matches known Facebook bottom-bar tab patterns.
     */
    private fun AccessibilityNodeInfo.hasSelectedReelsTab(): Boolean {
        // Check this node: is it a tab-like element that is selected and is "Reels"?
        if (isSelected && FacebookCaptionRules.isReelsTabElement(text?.toString(), contentDescription?.toString())) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d(
                    "REDU_FB_SURFACE",
                    "REELS_TAB_FOUND textLength=${text?.length ?: 0} contentDescriptionLength=${contentDescription?.length ?: 0} isSelected=$isSelected",
                )
            }
            return true
        }

        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                if (child.hasSelectedReelsTab()) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    private fun AccessibilityNodeInfo.containsCommentSheetMarker(): Boolean {
        if (FacebookCaptionRules.isCommentSheetSurfaceText(text?.toString(), contentDescription?.toString())) {
            return true
        }
        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                if (child.containsCommentSheetMarker()) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    private fun AccessibilityNodeInfo.bounds(): TikTokBounds {
        val rect = Rect()
        getBoundsInScreen(rect)
        return TikTokBounds(rect.left, rect.top, rect.right, rect.bottom)
    }
}
