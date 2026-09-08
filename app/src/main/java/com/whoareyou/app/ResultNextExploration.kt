package com.whoareyou.app

enum class ResultNextReason {
    SAME_FACET,
    COMPLEMENTARY_FACET,
    RETAKE_FACET
}

data class ResultNextExploration(
    val quizId: String,
    val reason: ResultNextReason
)

object ResultNextExplorationEngine {
    fun recommend(
        currentQuizId: String,
        orderedQuizIds: List<String>,
        themeByQuizId: Map<String, QuizVisualTheme>,
        completed: Set<String>
    ): ResultNextExploration? {
        val currentTheme = themeByQuizId[currentQuizId] ?: return null

        orderedQuizIds.firstOrNull { id ->
            id != currentQuizId && id !in completed && themeByQuizId[id] == currentTheme
        }?.let { return ResultNextExploration(it, ResultNextReason.SAME_FACET) }

        orderedQuizIds.firstOrNull { id ->
            id != currentQuizId && id !in completed && themeByQuizId[id] != null
        }?.let { return ResultNextExploration(it, ResultNextReason.COMPLEMENTARY_FACET) }

        orderedQuizIds.firstOrNull { id ->
            id != currentQuizId && themeByQuizId[id] == currentTheme
        }?.let { return ResultNextExploration(it, ResultNextReason.RETAKE_FACET) }

        return null
    }
}
