package com.whoareyou.app

data class DiscoverThemeSummary(
    val theme: QuizVisualTheme,
    val quizzes: List<Quiz>,
    val completedCount: Int,
    val totalCount: Int,
    val completionPercent: Int,
    val nextQuiz: Quiz?
)

object DiscoverLibraryEngine {
    fun summaries(
        quizzes: List<Quiz>,
        completed: Set<String>
    ): List<DiscoverThemeSummary> {
        return QuizVisualTheme.entries.mapNotNull { theme ->
            val matching = quizzes.filter { QuizVisuals.themeFor(it) == theme }
            if (matching.isEmpty()) return@mapNotNull null

            val completedCount = matching.count { it.id in completed }
            val percent = ((completedCount * 100f) / matching.size).toInt().coerceIn(0, 100)
            DiscoverThemeSummary(
                theme = theme,
                quizzes = matching,
                completedCount = completedCount,
                totalCount = matching.size,
                completionPercent = percent,
                nextQuiz = matching.firstOrNull { it.id !in completed } ?: matching.firstOrNull()
            )
        }.sortedWith(
            compareBy<DiscoverThemeSummary> { it.completionPercent == 100 }
                .thenBy { it.completionPercent }
                .thenByDescending { it.totalCount }
        )
    }

    fun search(
        quizzes: List<Quiz>,
        query: String,
        theme: QuizVisualTheme? = null,
        completed: Set<String> = emptySet(),
        unfinishedOnly: Boolean = false
    ): List<Quiz> {
        val normalized = query.trim().lowercase()
        return quizzes.asSequence()
            .filter { theme == null || QuizVisuals.themeFor(it) == theme }
            .filter { !unfinishedOnly || it.id !in completed }
            .filter { quiz ->
                normalized.isBlank() ||
                    quiz.title.lowercase().contains(normalized) ||
                    quiz.hook.lowercase().contains(normalized) ||
                    quiz.metricLow.lowercase().contains(normalized) ||
                    quiz.metricHigh.lowercase().contains(normalized)
            }
            .sortedWith(compareBy<Quiz> { it.id in completed }.thenBy { it.title })
            .toList()
    }

    fun uncoveredThemes(
        quizzes: List<Quiz>,
        completed: Set<String>
    ): Set<QuizVisualTheme> = summaries(quizzes, completed)
        .filter { it.completedCount == 0 }
        .mapTo(linkedSetOf()) { it.theme }
}
