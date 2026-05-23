package edu.feutech.redu.capture

data class PlatformTextExtraction(
    val sentimentText: String,
    val transitionText: String,
    /** True when the extraction contains actual caption/description text, not just author names. */
    val hasCaptionContent: Boolean = false,
)
