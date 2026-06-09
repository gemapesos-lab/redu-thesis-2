package edu.feutech.redu.capture

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.BuildConfig

object FacebookAdapter {
    const val PACKAGE_NAME = "com.facebook.katana"
    val PACKAGE_NAMES = setOf(PACKAGE_NAME)

    fun supports(packageName: CharSequence?): Boolean = packageName?.toString() in PACKAGE_NAMES

    fun isReelsSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        val signals = root.collectSurfaceSignals()
        return signals.hasSelectedReelsTab || signals.isFullscreenReelsViewer()
    }

    fun isCommentSheetSurface(root: AccessibilityNodeInfo?): Boolean {
        if (root == null || root.packageName?.toString() !in PACKAGE_NAMES) return false
        return root.containsCommentSheetMarker()
    }

    fun isReelsSurface(roots: List<AccessibilityNodeInfo>): Boolean {
        return roots.any { root ->
            root.packageName?.toString() in PACKAGE_NAMES &&
                root.collectSurfaceSignals().let { it.hasSelectedReelsTab || it.isFullscreenReelsViewer() }
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

    private fun AccessibilityNodeInfo.collectSurfaceSignals(signals: FacebookSurfaceSignals = FacebookSurfaceSignals()): FacebookSurfaceSignals {
        if (isSelected && FacebookCaptionRules.isReelsTabElement(text?.toString(), contentDescription?.toString())) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d(
                    "REDU_FB_SURFACE",
                    "REELS_TAB_FOUND textLength=${text?.length ?: 0} contentDescriptionLength=${contentDescription?.length ?: 0} isSelected=$isSelected",
                )
            }
            signals.hasSelectedReelsTab = true
        } else if (FacebookCaptionRules.isUnsupportedSurfaceText(text?.toString(), contentDescription?.toString(), isSelected)) {
            signals.hasUnsupportedSelectedTab = true
        }
        if (isVisibleToUser) {
            if (FacebookCaptionRules.isReelsHeaderText(text?.toString(), contentDescription?.toString())) {
                signals.hasReelsHeader = true
            }
            if (FacebookCaptionRules.isCommentActionText(text?.toString(), contentDescription?.toString())) {
                signals.hasCommentAction = true
            }
            if (FacebookCaptionRules.isShareActionText(text?.toString(), contentDescription?.toString())) {
                signals.hasShareAction = true
            }
            if (FacebookCaptionRules.isActionRailText(text?.toString(), contentDescription?.toString())) {
                signals.hasActionRail = true
            }
            if (FacebookCaptionRules.isCaptionOrCreatorText(text?.toString(), contentDescription?.toString())) {
                signals.hasCaptionOrCreator = true
            }
        }

        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                child.collectSurfaceSignals(signals)
            } finally {
                child.recycle()
            }
        }
        return signals
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

    private data class FacebookSurfaceSignals(
        var hasSelectedReelsTab: Boolean = false,
        var hasReelsHeader: Boolean = false,
        var hasCommentAction: Boolean = false,
        var hasShareAction: Boolean = false,
        var hasActionRail: Boolean = false,
        var hasCaptionOrCreator: Boolean = false,
        var hasUnsupportedSelectedTab: Boolean = false,
    ) {
        fun isFullscreenReelsViewer(): Boolean =
            FacebookCaptionRules.isFullscreenReelsSurfaceSupported(
                hasReelsHeader = hasReelsHeader,
                hasCommentAction = hasCommentAction,
                hasShareAction = hasShareAction,
                hasActionRail = hasActionRail,
                hasCaptionOrCreator = hasCaptionOrCreator,
                hasUnsupportedSelectedTab = hasUnsupportedSelectedTab,
            )
    }
}
