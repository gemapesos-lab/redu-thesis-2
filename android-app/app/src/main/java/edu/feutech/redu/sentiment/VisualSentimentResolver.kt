package edu.feutech.redu.sentiment

enum class VisualSentimentLabel {
    SEVERE_NEG,
    MILD_NEG,
    NEUTRAL,
    MILD_POS,
    SEVERE_POS,
    UNRESOLVED,
}

interface VisualSentimentResolver {
    suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel
    fun close() = Unit
}

class StubVisualSentimentResolver : VisualSentimentResolver {
    override suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel = VisualSentimentLabel.UNRESOLVED
}
