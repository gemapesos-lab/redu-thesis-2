package edu.feutech.redu.sentiment

object OovCalculator {
    const val UNRELIABLE_THRESHOLD = 0.50

    fun ratio(total: Int, recognized: Int): Double {
        if (total <= 0) return 1.0
        return ((total - recognized).coerceAtLeast(0)).toDouble() / total.toDouble()
    }
}
