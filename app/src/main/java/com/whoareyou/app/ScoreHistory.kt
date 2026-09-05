package com.whoareyou.app

data class ScoreHistoryUpdate(
    val latestScores: Map<String, Int>,
    val previousScores: Map<String, Int>
)

object ScoreHistoryEngine {
    fun update(
        latestScores: Map<String, Int>,
        previousScores: Map<String, Int>,
        quizId: String,
        score: Int
    ): ScoreHistoryUpdate {
        val latest = latestScores.toMutableMap()
        val previous = previousScores.toMutableMap()
        latest[quizId]?.let { previous[quizId] = it.coerceIn(0, 100) }
        latest[quizId] = score.coerceIn(0, 100)
        return ScoreHistoryUpdate(latest.toMap(), previous.toMap())
    }
}
