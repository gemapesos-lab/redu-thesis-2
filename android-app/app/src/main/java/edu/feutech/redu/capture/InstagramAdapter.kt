package edu.feutech.redu.capture

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.BuildConfig

object InstagramAdapter {
    const val PACKAGE_NAME = "com.instagram.android"
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

    /**
     * Single-pass allowlist scan: walks the entire tree looking for tab elements.
     * Returns true ONLY if a "Reels" tab is found AND it is the selected tab.
     * Returns false for ALL other cases (home, search, profile, messages, etc.).
     */
    private fun AccessibilityNodeInfo.hasSelectedReelsTab(): Boolean {
        if (isSelected && InstagramCaptionRules.isReelsTabElement(text?.toString(), contentDescription?.toString(), viewIdResourceName)) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d(
                    "REDU_IG_SURFACE",
                    "REELS_TAB_FOUND textLength=${text?.length ?: 0} contentDescriptionLength=${contentDescription?.length ?: 0} rid=$viewIdResourceName isSelected=$isSelected",
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
        if (InstagramCaptionRules.isCommentSheetSurfaceMarker(text?.toString(), contentDescription?.toString(), viewIdResourceName)) {
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



    fun extract(root: AccessibilityNodeInfo?): PlatformTextExtraction {
        if (root == null) return PlatformTextExtraction(sentimentText = "", transitionText = "")
        val rootBounds = root.bounds()
        val matches = mutableListOf<InstagramCaptionMatch>()
        collectText(root, rootBounds, matches, ancestorResourceIds = emptyList())
        return extractionFromMatches(matches)
    }

    internal fun extractionFromMatches(matches: List<InstagramCaptionMatch>): PlatformTextExtraction {
        val sentimentText = matches
            .filter { it.kind != InstagramCaptionKind.AUTHOR_NAME }
            .renderText()
        val transitionText = matches
            .filter { it.kind == InstagramCaptionKind.POST_DESCRIPTION || it.kind == InstagramCaptionKind.AUTHOR_NAME }
            .renderText()
        val hasCaptionContent = matches.any {
            it.kind == InstagramCaptionKind.POST_DESCRIPTION || it.kind == InstagramCaptionKind.COMMENT
        }

        return PlatformTextExtraction(
            sentimentText = sentimentText,
            transitionText = transitionText,
            hasCaptionContent = hasCaptionContent,
        )
    }

    private fun List<InstagramCaptionMatch>.renderText(): String {
        if (isEmpty()) return ""
        return asSequence()
            .sortedWith(
                compareBy<InstagramCaptionMatch> {
                    when (it.kind) {
                        InstagramCaptionKind.AUTHOR_NAME -> 0
                        InstagramCaptionKind.POST_DESCRIPTION -> 1
                        InstagramCaptionKind.COMMENT -> 2
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
        values: MutableList<InstagramCaptionMatch>,
        ancestorResourceIds: List<String>,
    ) {
        InstagramCaptionRules.classify(
            resourceId = node.viewIdResourceName,
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            bounds = node.bounds(),
            rootBounds = rootBounds,
            visibleToUser = node.isVisibleToUser,
            ancestorResourceIds = ancestorResourceIds,
        )?.let(values::add)

        val childAncestors = ancestorResourceIds + listOfNotNull(node.viewIdResourceName)
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child ->
                try {
                    collectText(child, rootBounds, values, childAncestors)
                } finally {
                    child.recycle()
                }
            }
        }
    }

    private fun AccessibilityNodeInfo.bounds(): TikTokBounds {
        val rect = Rect()
        getBoundsInScreen(rect)
        return TikTokBounds(rect.left, rect.top, rect.right, rect.bottom)
    }
}
