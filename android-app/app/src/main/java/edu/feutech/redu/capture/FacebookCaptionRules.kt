package edu.feutech.redu.capture

enum class FacebookCaptionKind {
    POST_DESCRIPTION,
    COMMENT,
}

data class FacebookCaptionMatch(
    val kind: FacebookCaptionKind,
    val normalizedText: String,
    val reason: String,
)

object FacebookCaptionRules {
    fun isUnsupportedSurfaceText(text: String?, contentDescription: String?, isSelected: Boolean): Boolean {
        val value = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        
        // If a non-reels tab is selected, it's an unsupported surface!
        if (isSelected) {
            val containsTab = value.contains("tab")
            val isHome = value.contains("home") || value.contains("news feed")
            val isFriends = value.contains("friends")
            val isMarketplace = value.contains("marketplace") || value.contains("shop")
            val isNotifications = value.contains("notifications")
            val isProfile = value.contains("profile")
            val isMenu = value.contains("menu")
            val isGroups = value.contains("groups")
            val isWatch = value.contains("watch") || value.contains("video")
            
            if (containsTab && (isHome || isFriends || isMarketplace || isNotifications || isProfile || isMenu || isGroups || isWatch)) {
                return true
            }
            
            // Fallback for exact/standard matches
            if (value == "home" || value == "home, tab" || value == "news feed" || value == "news feed, tab" ||
                value == "friends" || value == "friends, tab" ||
                value == "marketplace" || value == "marketplace, tab" ||
                value == "notifications" || value == "notifications, tab" ||
                value == "profile" || value == "profile, tab" ||
                value == "menu" || value == "menu, tab" ||
                value == "groups" || value == "groups, tab" ||
                value == "watch" || value == "watch, tab" || value == "video" || value == "video, tab"
            ) {
                return true
            }
        }

        return value == "ad" ||
            value == "install now" ||
            value == "play game" ||
            value == "watch more" ||
            value.startsWith("choose destination for your post") ||
            value == "share now"
    }

    fun classify(
        text: String?,
        contentDescription: String?,
        className: CharSequence?,
        bounds: TikTokBounds,
        rootBounds: TikTokBounds,
        visibleToUser: Boolean,
        clickable: Boolean,
        ancestorResourceIds: List<String> = emptyList(),
        ancestorClassNames: List<String> = emptyList(),
        commentSheetSurface: Boolean,
        reelsSurface: Boolean,
    ): FacebookCaptionMatch? {
        if (!reelsSurface || !visibleToUser || !bounds.hasArea() || !bounds.intersects(rootBounds)) return null

        val rawText = text.normalizeWhitespace()
        val rawDescription = contentDescription.normalizeWhitespace()
        val classValue = className?.toString().orEmpty()

        if (commentSheetSurface && isCommentSheetContext(ancestorResourceIds, ancestorClassNames)) {
            val comment = normalizeCommentText(rawText.takeIf { it.isNotBlank() } ?: rawDescription)
            if (comment.isNotBlank() && classValue == "android.widget.Button" && !clickable) {
                return FacebookCaptionMatch(
                    kind = FacebookCaptionKind.COMMENT,
                    normalizedText = comment,
                    reason = "comment body button inside Facebook comment sheet",
                )
            }
        }

        val caption = normalizeCaptionText(rawDescription)
        if (
            caption.isNotBlank() &&
            classValue == "android.widget.Button" &&
            clickable &&
            isFeedCaptionBounds(bounds, rootBounds) &&
            !caption.isFacebookUiText()
        ) {
            return FacebookCaptionMatch(
                kind = FacebookCaptionKind.POST_DESCRIPTION,
                normalizedText = caption,
                reason = "Facebook Reels lower caption button contentDescription",
            )
        }

        return null
    }

    fun normalizeCaptionText(text: String?): String {
        val normalized = text.normalizeWhitespace()
        return normalized
            .replace(Regex("\\s+more\\s*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*[.…]+\\s*more\\s*$", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    fun normalizeCommentText(text: String?): String {
        val normalized = text.normalizeWhitespace()
        if (normalized.isFacebookUiText()) return ""
        if (normalized.matches(Regex("\\d+[smhdw]$", RegexOption.IGNORE_CASE))) return ""
        if (normalized.matches(Regex("[\\d,.]+[kKmM]?"))) return ""
        return normalized
    }

    /**
     * Strict allowlist check: returns true ONLY when the element is specifically
     * a "Reels" tab in the bottom navigation bar.
     *
     * Requires:
     * - text/contentDescription matches the Reels tab pattern
     * - The node must look like a tab element (contains "tab" or is exactly "reels")
     *
     * This is the ONLY positive signal used for surface detection — no blocklist.
     */
    fun isReelsTabElement(text: String?, contentDescription: String?): Boolean {
        val value = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        // Match bottom-bar tab patterns: "Reels, tab", "Reels, tab 3 of 6", "Reels tab"
        if (value.startsWith("reels, tab") || value == "reels tab") return true
        // Match exact "Reels" text (the tab icon label in bottom bar)
        if (value == "reels") return true
        return false
    }

    /**
     * @deprecated Use [isReelsTabElement] + isSelected check from the adapter.
     * Kept for backward compatibility with tests.
     */
    fun isReelsSurfaceText(text: String?, contentDescription: String?, isSelected: Boolean): Boolean {
        if (!isSelected) return false
        return isReelsTabElement(text, contentDescription)
    }

    fun isCommentSheetSurfaceText(text: String?, contentDescription: String?): Boolean {
        val value = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        return value.startsWith("showing most relevant comments") ||
            value == "most relevant" ||
            value.startsWith("write a comment")
    }

    private fun isCommentSheetContext(
        ancestorResourceIds: List<String>,
        ancestorClassNames: List<String>,
    ): Boolean {
        val hasScrollableAncestor = ancestorClassNames.any {
            it == "android.widget.ScrollView" ||
                it == "androidx.recyclerview.widget.RecyclerView" ||
                it == "android.widget.ListView"
        }
        return hasScrollableAncestor
    }

    private fun isFeedCaptionBounds(bounds: TikTokBounds, rootBounds: TikTokBounds): Boolean {
        val rootHeight = rootBounds.height.coerceAtLeast(1)
        val rootWidth = rootBounds.width.coerceAtLeast(1)
        val topRatio = (bounds.top - rootBounds.top).toDouble() / rootHeight
        val rightRatio = (bounds.right - rootBounds.left).toDouble() / rootWidth
        val leftRatio = (bounds.left - rootBounds.left).toDouble() / rootWidth
        val widthRatio = bounds.width.toDouble() / rootWidth
        return topRatio >= 0.78 && rightRatio <= 0.88 && leftRatio <= 0.25 && widthRatio >= 0.55
    }

    private fun String?.normalizeWhitespace(): String =
        this?.replace(Regex("\\s+"), " ")?.trim().orEmpty()

    private fun String.isFacebookUiText(): Boolean {
        val lower = lowercase()
        return lower.isBlank() ||
            lower == "reels" ||
            lower == "play" ||
            lower == "follow" ||
            lower == "more" ||
            lower == "menu" ||
            lower == "search" ||
            lower == "save" ||
            lower == "share" ||
            lower == "reply" ||
            lower == "like" ||
            lower == "most relevant" ||
            lower == "write a comment..." ||
            lower == "write a comment…" ||
            lower == "create reel" ||
            lower.contains(" tab ") ||
            lower.startsWith("home, tab") ||
            lower.startsWith("reels, tab") ||
            lower.startsWith("friends, tab") ||
            lower.startsWith("marketplace, tab") ||
            lower.startsWith("notifications, tab") ||
            lower.startsWith("profile, tab") ||
            lower.startsWith("tap to ") ||
            lower.startsWith("showing most relevant comments") ||
            lower.startsWith("reply to ") ||
            lower.startsWith("like ") && lower.contains("comment button") ||
            lower.startsWith("downvote ") ||
            lower.startsWith("view ") && lower.contains("repl") ||
            lower.endsWith(" reactions") ||
            lower.contains(" comments") ||
            lower.contains(" shares") ||
            lower.startsWith("follow ") ||
            lower.startsWith("new for you") ||
            lower.endsWith("profile picture") ||
            lower.contains(" by author") ||
            lower.startsWith("sponsored") ||
            lower == "ad" ||
            lower == "install now" ||
            lower == "play game" ||
            lower == "watch more" ||
            lower.contains("shop") ||
            lower.contains("marketplace")
    }
}
