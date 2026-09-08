package com.whoareyou.app

enum class DiscoverProfileStage {
    NEW,
    EMERGING,
    MAPPED
}

enum class DiscoverRecommendationReason {
    NEW_THEME,
    UNFINISHED,
    RETAKE
}

data class DiscoverRecommendation(
    val quiz: Quiz,
    val reason: DiscoverRecommendationReason
)

object DiscoverPersonalization {
    fun recommendation(
        quizzes: List<Quiz>,
        completed: Set<String>,
        dimensions: List<ProfileDimension>
    ): DiscoverRecommendation? {
        if (quizzes.isEmpty()) return null

        val measuredThemes = dimensions.mapNotNull { dimension ->
            quizzes.firstOrNull { it.id == dimension.quizId }?.let(QuizVisuals::themeFor)
        }.toSet()

        quizzes.firstOrNull { quiz ->
            quiz.id !in completed && QuizVisuals.themeFor(quiz) !in measuredThemes
        }?.let { return DiscoverRecommendation(it, DiscoverRecommendationReason.NEW_THEME) }

        quizzes.firstOrNull { it.id !in completed }
            ?.let { return DiscoverRecommendation(it, DiscoverRecommendationReason.UNFINISHED) }

        return quizzes.firstOrNull()?.let { DiscoverRecommendation(it, DiscoverRecommendationReason.RETAKE) }
    }

    fun nextQuiz(
        quizzes: List<Quiz>,
        completed: Set<String>,
        dimensions: List<ProfileDimension>
    ): Quiz? = recommendation(quizzes, completed, dimensions)?.quiz

    fun stage(profile: GlobalProfileSummary): DiscoverProfileStage = when {
        profile.completedCount == 0 -> DiscoverProfileStage.NEW
        profile.signature == null -> DiscoverProfileStage.EMERGING
        else -> DiscoverProfileStage.MAPPED
    }

    fun strongestDimension(dimensions: List<ProfileDimension>): ProfileDimension? =
        dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
}
