package com.whoareyou.app

data class ScoreHistoryUpdate(
    val latestScores: Map<String, Int>,
    val previousScores: Map<String, Int>,
    val scoreHistory: Map<String, List<Int>>
)

object ScoreHistoryEngine {
    const val MAX_SCORES_PER_QUIZ = 8

    fun update(
        latestScores: Map<String, Int>,
        previousScores: Map<String, Int>,
        quizId: String,
        score: Int,
        scoreHistory: Map<String, List<Int>> = emptyMap()
    ): ScoreHistoryUpdate {
        val latest = latestScores.toMutableMap()
        val previous = previousScores.toMutableMap()
        val history = scoreHistory
            .mapValues { (_, scores) -> scores.map { it.coerceIn(0, 100) }.takeLast(MAX_SCORES_PER_QUIZ) }
            .toMutableMap()

        val normalizedScore = score.coerceIn(0, 100)
        latest[quizId]?.let { previous[quizId] = it.coerceIn(0, 100) }
        latest[quizId] = normalizedScore
        history[quizId] = (history[quizId].orEmpty() + normalizedScore).takeLast(MAX_SCORES_PER_QUIZ)

        return ScoreHistoryUpdate(
            latestScores = latest.toMap(),
            previousScores = previous.toMap(),
            scoreHistory = history.toMap()
        )
    }
}
