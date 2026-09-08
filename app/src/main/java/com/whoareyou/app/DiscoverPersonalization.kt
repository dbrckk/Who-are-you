package com.whoareyou.app

object DiscoverPersonalization {
    fun nextQuiz(
        quizzes: List<Quiz>,
        completed: Set<String>,
        dimensions: List<ProfileDimension>
    ): Quiz? {
        if (quizzes.isEmpty()) return null

        val measuredThemes = dimensions.mapNotNull { dimension ->
            quizzes.firstOrNull { it.id == dimension.quizId }?.let(QuizVisuals::themeFor)
        }.toSet()

        return quizzes.firstOrNull { quiz ->
            quiz.id !in completed && QuizVisuals.themeFor(quiz) !in measuredThemes
        } ?: quizzes.firstOrNull { it.id !in completed }
            ?: quizzes.firstOrNull()
    }

    fun strongestDimension(dimensions: List<ProfileDimension>): ProfileDimension? =
        dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
}
