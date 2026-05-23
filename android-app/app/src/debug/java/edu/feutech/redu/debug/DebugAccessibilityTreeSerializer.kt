package edu.feutech.redu.debug

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.capture.FacebookCaptionKind
import edu.feutech.redu.capture.FacebookCaptionRules
import edu.feutech.redu.capture.InstagramCaptionKind
import edu.feutech.redu.capture.InstagramCaptionRules
import edu.feutech.redu.capture.TikTokBounds
import edu.feutech.redu.capture.TikTokCaptionKind
import edu.feutech.redu.capture.TikTokCaptionRules

// DEBUG_OVERLAY_REMOVE: debug-only tree model used for caption/comment resource-id inspection.
data class DebugNodeSnapshot(
    val path: String,
    val depth: Int,
    val className: String?,
    val packageName: String?,
    val viewIdResourceName: String?,
    val text: String?,
    val contentDescription: String?,
    val boundsInScreen: String,
    val visibleToUser: Boolean,
    val enabled: Boolean,
    val clickable: Boolean,
    val scrollable: Boolean,
    val selected: Boolean = false,
    val childCount: Int,
    val children: List<DebugNodeSnapshot> = emptyList(),
)

data class DebugAccessibilityTreeDump(
    val text: String,
    val nodeCount: Int,
)

data class DebugCaptionCandidate(
    val path: String,
    val kind: String,
    val reason: String,
    val viewIdResourceName: String?,
    val text: String,
    val boundsInScreen: String,
    val visibleToUser: Boolean,
    val className: String?,
)

data class DebugCaptionCandidateDump(
    val text: String,
    val candidates: List<DebugCaptionCandidate>,
)

// DEBUG_OVERLAY_REMOVE: serializes raw accessibility text for local debug builds only.
object DebugAccessibilityTreeSerializer {
    fun snapshotFrom(root: AccessibilityNodeInfo?): DebugNodeSnapshot? =
        root?.toSnapshot(path = "0", depth = 0)

    fun dump(root: DebugNodeSnapshot?): DebugAccessibilityTreeDump {
        if (root == null) {
            return DebugAccessibilityTreeDump(
                text = "Accessibility root: null\n",
                nodeCount = 0,
            )
        }

        val builder = StringBuilder()
        var count = 0
        fun appendNode(node: DebugNodeSnapshot) {
            count += 1
            builder.appendLine("path=${node.path}")
            builder.appendLine("depth=${node.depth}")
            builder.appendLine("className=${node.className.orEmpty()}")
            builder.appendLine("packageName=${node.packageName.orEmpty()}")
            builder.appendLine("viewIdResourceName=${node.viewIdResourceName.orEmpty()}")
            builder.appendLine("text=${node.text.orEmpty()}")
            builder.appendLine("contentDescription=${node.contentDescription.orEmpty()}")
            builder.appendLine("boundsInScreen=${node.boundsInScreen}")
            builder.appendLine("visibleToUser=${node.visibleToUser}")
            builder.appendLine("enabled=${node.enabled}")
            builder.appendLine("clickable=${node.clickable}")
            builder.appendLine("scrollable=${node.scrollable}")
            builder.appendLine("childCount=${node.childCount}")
            builder.appendLine()
            node.children.forEach(::appendNode)
        }

        appendNode(root)
        return DebugAccessibilityTreeDump(text = builder.toString(), nodeCount = count)
    }

    fun captionCandidates(root: DebugNodeSnapshot?): DebugCaptionCandidateDump {
        if (root == null) {
            return DebugCaptionCandidateDump(
                text = "Accessibility root: null\ncaptionCandidateCount=0\ntextCandidateCount=0\n",
                candidates = emptyList(),
            )
        }
        val rootBounds = root.boundsInScreen.parseBounds() ?: TikTokBounds(0, 0, 0, 0)
        val candidates = mutableListOf<DebugCaptionCandidate>()
        val facebookReelsSurface = root.containsFacebookReelsMarker() && !root.containsFacebookUnsupportedSurfaceMarker()
        val facebookCommentSheetSurface = root.containsFacebookCommentSheetMarker()

        fun visit(
            node: DebugNodeSnapshot,
            ancestorResourceIds: List<String>,
            ancestorClassNames: List<String>,
        ) {
            TikTokCaptionRules.classify(
                resourceId = node.viewIdResourceName,
                text = node.text,
                contentDescription = node.contentDescription,
                bounds = node.boundsInScreen.parseBounds() ?: TikTokBounds(0, 0, 0, 0),
                rootBounds = rootBounds,
                visibleToUser = node.visibleToUser,
                ancestorResourceIds = ancestorResourceIds,
            )?.let { match ->
                candidates += DebugCaptionCandidate(
                    path = node.path,
                    kind = match.kind.name,
                    reason = "TikTok: ${match.reason}",
                    viewIdResourceName = node.viewIdResourceName,
                    text = match.normalizedText,
                    boundsInScreen = node.boundsInScreen,
                    visibleToUser = node.visibleToUser,
                    className = node.className,
                )
            }
            InstagramCaptionRules.classify(
                resourceId = node.viewIdResourceName,
                text = node.text,
                contentDescription = node.contentDescription,
                bounds = node.boundsInScreen.parseBounds() ?: TikTokBounds(0, 0, 0, 0),
                rootBounds = rootBounds,
                visibleToUser = node.visibleToUser,
                ancestorResourceIds = ancestorResourceIds,
            )?.let { match ->
                candidates += DebugCaptionCandidate(
                    path = node.path,
                    kind = match.kind.name,
                    reason = "Instagram: ${match.reason}",
                    viewIdResourceName = node.viewIdResourceName,
                    text = match.normalizedText,
                    boundsInScreen = node.boundsInScreen,
                    visibleToUser = node.visibleToUser,
                    className = node.className,
                )
            }
            FacebookCaptionRules.classify(
                text = node.text,
                contentDescription = node.contentDescription,
                className = node.className,
                bounds = node.boundsInScreen.parseBounds() ?: TikTokBounds(0, 0, 0, 0),
                rootBounds = rootBounds,
                visibleToUser = node.visibleToUser,
                clickable = node.clickable,
                ancestorResourceIds = ancestorResourceIds,
                ancestorClassNames = ancestorClassNames,
                commentSheetSurface = facebookCommentSheetSurface,
                reelsSurface = facebookReelsSurface,
            )?.let { match ->
                candidates += DebugCaptionCandidate(
                    path = node.path,
                    kind = match.kind.name,
                    reason = "Facebook: ${match.reason}",
                    viewIdResourceName = node.viewIdResourceName,
                    text = match.normalizedText,
                    boundsInScreen = node.boundsInScreen,
                    visibleToUser = node.visibleToUser,
                    className = node.className,
                )
            }
            val childResourceAncestors = ancestorResourceIds + listOfNotNull(node.viewIdResourceName)
            val childClassAncestors = ancestorClassNames + listOfNotNull(node.className)
            node.children.forEach { visit(it, childResourceAncestors, childClassAncestors) }
        }

        visit(root, ancestorResourceIds = emptyList(), ancestorClassNames = emptyList())
        val ordered = candidates.sortedWith(
            compareBy<DebugCaptionCandidate> {
                when (it.kind) {
                    TikTokCaptionKind.VIDEO_TEXT.name -> 0
                    TikTokCaptionKind.POST_DESCRIPTION.name,
                    InstagramCaptionKind.POST_DESCRIPTION.name,
                    FacebookCaptionKind.POST_DESCRIPTION.name,
                    -> 1
                    TikTokCaptionKind.COMMENT.name,
                    InstagramCaptionKind.COMMENT.name,
                    FacebookCaptionKind.COMMENT.name,
                    -> 2
                    TikTokCaptionKind.CAPTION_LIKE_TEXT.name -> 3
                    else -> 4
                }
            }.thenBy { it.path },
        )
        return DebugCaptionCandidateDump(
            text = ordered.renderCaptionCandidateText(),
            candidates = ordered,
        )
    }

    private fun AccessibilityNodeInfo.toSnapshot(path: String, depth: Int): DebugNodeSnapshot {
        val bounds = Rect()
        getBoundsInScreen(bounds)
        val childSnapshots = mutableListOf<DebugNodeSnapshot>()
        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            try {
                childSnapshots += child.toSnapshot(path = "$path/$index", depth = depth + 1)
            } finally {
                child.recycle()
            }
        }

        return DebugNodeSnapshot(
            path = path,
            depth = depth,
            className = className?.toString(),
            packageName = packageName?.toString(),
            viewIdResourceName = viewIdResourceName,
            text = text?.toString(),
            contentDescription = contentDescription?.toString(),
            boundsInScreen = "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]",
            visibleToUser = isVisibleToUser,
            enabled = isEnabled,
            clickable = isClickable,
            scrollable = isScrollable,
            selected = isSelected,
            childCount = childCount,
            children = childSnapshots,
        )
    }

    private fun List<DebugCaptionCandidate>.renderCaptionCandidateText(): String =
        buildString {
            appendLine("captionCandidateCount=${this@renderCaptionCandidateText.size}")
            appendLine("textCandidateCount=${this@renderCaptionCandidateText.size}")
            appendLine("selectionNotes=These are Accessibility text nodes only; screenshot pixels are not OCR/VLM processed.")
            appendLine()
            this@renderCaptionCandidateText.forEachIndexed { index, candidate ->
                appendLine("candidate=${index + 1}")
                appendLine("kind=${candidate.kind}")
                appendLine("reason=${candidate.reason}")
                appendLine("path=${candidate.path}")
                appendLine("className=${candidate.className.orEmpty()}")
                appendLine("viewIdResourceName=${candidate.viewIdResourceName.orEmpty()}")
                appendLine("boundsInScreen=${candidate.boundsInScreen}")
                appendLine("visibleToUser=${candidate.visibleToUser}")
                appendLine("text=${candidate.text}")
                appendLine()
            }
        }

    private fun String.parseBounds(): TikTokBounds? {
        val match = Regex("\\[(-?\\d+),(-?\\d+)]\\[(-?\\d+),(-?\\d+)]").matchEntire(this) ?: return null
        val values = match.groupValues.drop(1).map { it.toInt() }
        return TikTokBounds(values[0], values[1], values[2], values[3])
    }

    private fun DebugNodeSnapshot.containsFacebookReelsMarker(): Boolean =
        FacebookCaptionRules.isReelsSurfaceText(text, contentDescription, selected) ||
            containsFacebookCommentSheetMarker() ||
            children.any { it.containsFacebookReelsMarker() }

    private fun DebugNodeSnapshot.containsFacebookCommentSheetMarker(): Boolean =
        FacebookCaptionRules.isCommentSheetSurfaceText(text, contentDescription) ||
            children.any { it.containsFacebookCommentSheetMarker() }

    private fun DebugNodeSnapshot.containsFacebookUnsupportedSurfaceMarker(): Boolean =
        FacebookCaptionRules.isUnsupportedSurfaceText(text, contentDescription, selected) ||
            children.any { it.containsFacebookUnsupportedSurfaceMarker() }
}
