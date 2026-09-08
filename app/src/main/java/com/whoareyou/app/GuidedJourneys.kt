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
        val selected = selectBalanced(
            quizzes = quizzes,
            themes = definition.themes.toList(),
            limit = maxQuizzesPerJourney.coerceAtLeast(1)
        )

        if (selected.isEmpty()) return@mapNotNull null
        val ids = selected.map { it.id }
        GuidedJourneyProgress(
            definition = definition,
            quizIds = ids,
            completedCount = ids.count { it in completed },
            nextQuizId = ids.firstOrNull { it !in completed } ?: ids.firstOrNull()
        )
    }

    private fun selectBalanced(
        quizzes: List<Quiz>,
        themes: List<QuizVisualTheme>,
        limit: Int
    ): List<Quiz> {
        val buckets = themes.associateWith { theme ->
            quizzes
                .filter { QuizVisuals.themeFor(it) == theme }
                .sortedBy { it.id }
        }
        val selected = mutableListOf<Quiz>()
        var depth = 0

        while (selected.size < limit) {
            var added = false
            for (theme in themes) {
                val candidate = buckets[theme]?.getOrNull(depth) ?: continue
                selected += candidate
                added = true
                if (selected.size == limit) break
            }
            if (!added) break
            depth += 1
        }

        return selected
    }
}