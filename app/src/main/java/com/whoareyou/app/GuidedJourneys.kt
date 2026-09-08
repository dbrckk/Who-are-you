package com.whoareyou.app

enum class GuidedJourneyId {
    PERSONALITY,
    RELATIONSHIPS,
    INNER_WORLD,
    VALUES_AND_DIRECTION
}

data class GuidedJourneyDefinition(
    val id: GuidedJourneyId,
    val themes: Set<QuizVisualTheme>
)

data class GuidedJourneyProgress(
    val definition: GuidedJourneyDefinition,
    val quizIds: List<String>,
    val completedCount: Int,
    val nextQuizId: String?
) {
    val totalCount: Int get() = quizIds.size
    val progressPercent: Int get() = if (totalCount == 0) 0 else (completedCount * 100 / totalCount)
    val isComplete: Boolean get() = totalCount > 0 && completedCount == totalCount
}

object GuidedJourneyEngine {
    val definitions = listOf(
        GuidedJourneyDefinition(
            GuidedJourneyId.PERSONALITY,
            setOf(QuizVisualTheme.IDENTITY, QuizVisualTheme.MIND, QuizVisualTheme.LIFESTYLE)
        ),
        GuidedJourneyDefinition(
            GuidedJourneyId.RELATIONSHIPS,
            setOf(QuizVisualTheme.EMOTION, QuizVisualTheme.SOCIAL)
        ),
        GuidedJourneyDefinition(
            GuidedJourneyId.INNER_WORLD,
            setOf(QuizVisualTheme.EMOTION, QuizVisualTheme.MIND, QuizVisualTheme.ENERGY, QuizVisualTheme.CONTROL)
        ),
        GuidedJourneyDefinition(
            GuidedJourneyId.VALUES_AND_DIRECTION,
            setOf(QuizVisualTheme.VALUES, QuizVisualTheme.GROWTH, QuizVisualTheme.CONTROL)
        )
    )

    fun build(
        quizzes: List<Quiz>,
        completed: Set<String>,
        maxQuizzesPerJourney: Int = 4
    ): List<GuidedJourneyProgress> = definitions.mapNotNull { definition ->
        val matching = quizzes
            .filter { QuizVisuals.themeFor(it) in definition.themes }
            .sortedWith(
                compareBy<Quiz> { it.id in completed }
                    .thenBy { definition.themes.indexOf(QuizVisuals.themeFor(it)) }
                    .thenBy { it.id }
            )
            .take(maxQuizzesPerJourney.coerceAtLeast(1))

        if (matching.isEmpty()) return@mapNotNull null
        val ids = matching.map { it.id }
        GuidedJourneyProgress(
            definition = definition,
            quizIds = ids,
            completedCount = ids.count { it in completed },
            nextQuizId = ids.firstOrNull { it !in completed } ?: ids.firstOrNull()
        )
    }

    private fun <T> Set<T>.indexOf(value: T): Int = indexOfFirst { it == value }.let { if (it < 0) Int.MAX_VALUE else it }
}