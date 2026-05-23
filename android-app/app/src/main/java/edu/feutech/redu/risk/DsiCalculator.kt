package edu.feutech.redu.risk

import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity

object DsiCalculator {
    fun weekDsi(sessions: List<SessionEntity>): Double? {
        val reliable = sessions.filter { it.sentimentReliability == SentimentReliability.RELIABLE }
        if (reliable.isEmpty()) return null
        return reliable.sumOf { it.riskScore } / reliable.size
    }
}
