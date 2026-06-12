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
    suspend fun warmUp(): Boolean = true
    suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel
    suspend fun close() = Unit
}

class StubVisualSentimentResolver : VisualSentimentResolver {
    override suspend fun resolveNoTextItem(frames: List<ByteArray>): VisualSentimentLabel = VisualSentimentLabel.UNRESOLVED
}
