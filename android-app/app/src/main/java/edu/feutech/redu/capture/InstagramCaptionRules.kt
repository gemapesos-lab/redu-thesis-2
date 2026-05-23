package edu.feutech.redu.capture

enum class InstagramCaptionKind {
    POST_DESCRIPTION,
    COMMENT,
    AUTHOR_NAME,
}

data class InstagramCaptionMatch(
    val kind: InstagramCaptionKind,
    val normalizedText: String,
    val reason: String,
)

object InstagramCaptionRules {
    private const val CAPTION_COMPONENT_ID = "clips_caption_component"
    private const val COMMENT_LIST_ID = "sticky_header_list"
    private val authorNameIds = setOf("row_feed_photo_profile_name", "user_name")

    fun classify(
        resourceId: String?,
        text: String?,
        contentDescription: String?,
        bounds: TikTokBounds,
        rootBounds: TikTokBounds,
        visibleToUser: Boolean,
        ancestorResourceIds: List<String> = emptyList(),
    ): InstagramCaptionMatch? {
        if (!visibleToUser || !bounds.hasArea() || !bounds.intersects(rootBounds)) return null

        val suffix = resourceSuffix(resourceId)
        val ancestorSuffixes = ancestorResourceIds.map(::resourceSuffix)
        val rawText = text?.takeIf { it.isNotBlank() }
        val rawDescription = contentDescription?.takeIf { it.isNotBlank() }

        if (ancestorSuffixes.contains(CAPTION_COMPONENT_ID) && suffix.isBlank()) {
            val caption = normalizeCaptionText(rawDescription)
            if (caption.isNotBlank() && !caption.isFeedCaptionPlaceholder()) {
                return InstagramCaptionMatch(
                    kind = InstagramCaptionKind.POST_DESCRIPTION,
                    normalizedText = caption,
                    reason = "caption contentDescription under $CAPTION_COMPONENT_ID",
                )
            }
        }

        if (ancestorSuffixes.contains(COMMENT_LIST_ID) && suffix.isBlank()) {
            val comment = normalizeCommentText(rawText ?: rawDescription)
            if (comment.isNotBlank()) {
                return InstagramCaptionMatch(
                    kind = InstagramCaptionKind.COMMENT,
                    normalizedText = comment,
                    reason = "comment text under $COMMENT_LIST_ID",
                )
            }
        }

        if (suffix in authorNameIds) {
            val author = normalizeCaptionText(rawText ?: rawDescription)
            if (author.isNotBlank()) {
                return InstagramCaptionMatch(
                    kind = InstagramCaptionKind.AUTHOR_NAME,
                    normalizedText = author,
                    reason = "known author name resource id: $suffix",
                )
            }
        }

        return null
    }

    fun normalizeCaptionText(text: String?): String {
        val normalized = normalizeWhitespace(text)
        return normalized
            .replace(Regex("\\s*[.…]+\\s*$"), "")
            .trim()
    }

    fun normalizeCommentText(text: String?): String {
        val normalized = normalizeWhitespace(text)
        val match = Regex("^\\S+\\s+said\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(normalized)
            ?: return ""
        val comment = match.groupValues[1].trim()
        if (comment.isCommentUiText()) return ""
        return comment
    }

    fun resourceSuffix(resourceId: String?): String =
        resourceId?.substringAfterLast('/')?.lowercase().orEmpty()

    private fun normalizeWhitespace(text: String?): String =
        text?.replace(Regex("\\s+"), " ")?.trim().orEmpty()

    private fun String.isFeedCaptionPlaceholder(): Boolean {
        val lower = lowercase()
        return lower == "see more" ||
            lower == "more" ||
            lower.startsWith("reel by ") ||
            lower.startsWith("profile picture") ||
            lower.startsWith("follow ") ||
            lower.startsWith("like") ||
            lower.startsWith("comment") ||
            lower.startsWith("share") ||
            lower.startsWith("save") ||
            lower.startsWith("repost")
    }

    private fun String.isCommentUiText(): Boolean {
        val lower = lowercase()
        return lower == "reply" ||
            lower == "see translation" ||
            lower.startsWith("view ") ||
            lower.startsWith("add a comment") ||
            lower.startsWith("start the conversation") ||
            lower.startsWith("no comments yet")
    }

    /**
     * Strict allowlist check: returns true ONLY when the element is specifically
     * the "Reels" tab in the bottom navigation bar.
     *
     * Instagram's Reels tab has resource-id "clips_tab" and content-desc "Reels".
     * We require the resource-id to avoid false positives from other nodes
     * that happen to have content-desc "Reels" with isSelected=true.
     *
     * isSelected is checked at the adapter level, not here.
     */
    fun isReelsTabElement(text: String?, contentDescription: String?, resourceId: String?): Boolean {
        val suffix = resourceSuffix(resourceId)
        // The definitive signal: Instagram's Reels tab has resource-id "clips_tab"
        if (suffix == "clips_tab") return true
        // Fallback: tab_icon inside clips_tab with "reels" text
        val valText = normalizeWhitespace(text.takeUnless { it.isNullOrBlank() } ?: contentDescription).lowercase()
        if (suffix == "tab_icon" && valText == "reels") return true
        // Tab-pattern content descriptions (e.g. "Reels, tab")
        if (valText.startsWith("reels, tab") || valText == "reels tab") return true
        return false
    }

    /**
     * @deprecated Use [isReelsTabElement] + isSelected check from the adapter.
     * Kept for backward compatibility with tests.
     */
    fun isReelsSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        if (!isSelected) return false
        return isReelsTabElement(text, contentDescription, resourceId)
    }

    fun isUnsupportedSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val valText = normalizeWhitespace(text.takeUnless { it.isNullOrBlank() } ?: contentDescription).lowercase()
        val suffix = resourceSuffix(resourceId)
        
        return (valText == "home" && isSelected) ||
               (valText == "home, tab" && isSelected) ||
               (valText == "search & explore" && isSelected) ||
               (valText == "search and explore" && isSelected) ||
               (valText == "search & explore, tab" && isSelected) ||
               (valText == "search and explore, tab" && isSelected) ||
               (valText.contains("search") && valText.contains("tab") && isSelected) ||
               (valText.contains("explore") && valText.contains("tab") && isSelected) ||
               (valText == "profile" && isSelected) ||
               (valText == "profile, tab" && isSelected) ||
               (valText.contains("profile") && valText.contains("tab") && isSelected) ||
               (suffix == "tab_icon" && isSelected && valText != "reels")
    }

    fun isCommentSheetSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?): Boolean {
        val valText = normalizeWhitespace(text.takeUnless { it.isNullOrBlank() } ?: contentDescription).lowercase()
        val suffix = resourceSuffix(resourceId)
        return valText == "comments" ||
               valText.startsWith("viewing comments") ||
               valText.startsWith("add a comment") ||
               valText.contains(" replies") ||
               suffix == "layout_comment_thread_edittext_multiline" ||
               suffix == "comment_composer_parent_updated" ||
               suffix == "comment_thread_view_group" ||
               suffix == "sticky_header_list"
    }
}
