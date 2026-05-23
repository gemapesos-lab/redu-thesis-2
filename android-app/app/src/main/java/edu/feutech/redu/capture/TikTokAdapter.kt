package edu.feutech.redu.capture

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object TikTokAdapter {
    const val PACKAGE_NAME_GLOBAL = "com.zhiliaoapp.musically"
    const val PACKAGE_NAME_REGIONAL = "com.ss.android.ugc.trill"
    val PACKAGE_NAMES = setOf(PACKAGE_NAME_GLOBAL, PACKAGE_NAME_REGIONAL)

    fun supports(packageName: CharSequence?): Boolean = packageName?.toString() in PACKAGE_NAMES

    fun isReelsSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        if (root.containsUnsupportedSurfaceMarker()) return false
        return root.containsReelsMarker()
    }

    fun isCommentSheetSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        return root.containsCommentSheetMarker()
    }

    fun isReelsSurface(roots: List<AccessibilityNodeInfo>): Boolean {
        return roots.any { root ->
            root.packageName?.toString() in PACKAGE_NAMES &&
            !root.containsUnsupportedSurfaceMarker() &&
            root.containsReelsMarker()
        }
    }

    private fun AccessibilityNodeInfo.containsReelsMarker(): Boolean {
        if (TikTokCaptionRules.isReelsSurfaceMarker(text?.toString(), contentDescription?.toString(), viewIdResourceName, isSelected)) {
            return true
        }
        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                if (child.containsReelsMarker()) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    private fun AccessibilityNodeInfo.containsUnsupportedSurfaceMarker(): Boolean {
        if (TikTokCaptionRules.isUnsupportedSurfaceMarker(text?.toString(), contentDescription?.toString(), viewIdResourceName, isSelected)) {
            return true
        }
        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                if (child.containsUnsupportedSurfaceMarker()) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }
    
    private fun AccessibilityNodeInfo.containsCommentSheetMarker(): Boolean {
        if (TikTokCaptionRules.isCommentSheetSurfaceMarker(text?.toString(), contentDescription?.toString(), viewIdResourceName, isSelected)) {
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

    fun extractVisibleText(root: AccessibilityNodeInfo?): String {
        return extract(root).sentimentText
    }

    fun extract(root: AccessibilityNodeInfo?): PlatformTextExtraction {
        if (root == null) return PlatformTextExtraction(sentimentText = "", transitionText = "")
        val rootBounds = root.bounds()
        val captionValues = mutableListOf<TikTokCaptionMatch>()
        collectCaptionText(root, rootBounds, captionValues, ancestorResourceIds = emptyList())
        return extractionFromMatches(captionValues)
    }

    internal fun extractionFromMatches(matches: List<TikTokCaptionMatch>): PlatformTextExtraction {
        val sentimentText = matches.renderText()
        val transitionText = matches
            .filter { it.kind == TikTokCaptionKind.POST_DESCRIPTION || it.kind == TikTokCaptionKind.AUTHOR_NAME }
            .renderText()
        val hasCaptionContent = matches.any { it.kind != TikTokCaptionKind.AUTHOR_NAME }

        return PlatformTextExtraction(
            sentimentText = sentimentText,
            transitionText = transitionText,
            hasCaptionContent = hasCaptionContent,
        )
    }

    private fun List<TikTokCaptionMatch>.renderText(): String {
        if (isEmpty()) return ""
        return asSequence()
                .sortedWith(
                    compareBy<TikTokCaptionMatch> {
                        when (it.kind) {
                            TikTokCaptionKind.VIDEO_TEXT -> 0
                            TikTokCaptionKind.AUTHOR_NAME -> 1
                            TikTokCaptionKind.POST_DESCRIPTION -> 2
                            TikTokCaptionKind.COMMENT -> 3
                            TikTokCaptionKind.CAPTION_LIKE_TEXT -> 4
                        }
                    },
                )
                .map { it.normalizedText }
                .distinct()
                .joinToString(separator = " ")
    }

    private fun collectCaptionText(
        node: AccessibilityNodeInfo,
        rootBounds: TikTokBounds,
        values: MutableList<TikTokCaptionMatch>,
        ancestorResourceIds: List<String>,
    ) {
        TikTokCaptionRules.classify(
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
                    collectCaptionText(child, rootBounds, values, childAncestors)
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
