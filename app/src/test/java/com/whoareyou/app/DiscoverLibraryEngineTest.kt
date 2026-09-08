package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoverLibraryEngineTest {
    @Test
    fun incompleteThemesAreRankedBeforeCompletedThemes() {
        val social = quiz("social", "Social confidence")
        val energy = quiz("energy", "Sleep and energy")

        val summaries = DiscoverLibraryEngine.summaries(
            quizzes = listOf(social, energy),
            completed = setOf("social")
        )

        assertEquals(QuizVisualTheme.ENERGY, summaries.first().theme)
        assertEquals(0, summaries.first().completionPercent)
        assertEquals(100, summaries.last().completionPercent)
    }

    @Test
    fun searchMatchesTitleHookAndMetrics() {
        val social = quiz("social", "Social confidence", hook = "How you connect", low = "Reserved", high = "Expressive")
        val energy = quiz("energy", "Energy rhythm", hook = "Morning or night", low = "Night owl", high = "Early bird")

        assertEquals(listOf("social"), DiscoverLibraryEngine.search(listOf(social, energy), "connect").map { it.id })
        assertEquals(listOf("energy"), DiscoverLibraryEngine.search(listOf(social, energy), "bird").map { it.id })
    }

    @Test
    fun unfinishedFilterExcludesCompletedTests() {
        val first = quiz("first", "Identity")
        val second = quiz("second", "Values")

        val results = DiscoverLibraryEngine.search(
            quizzes = listOf(first, second),
            query = "",
            completed = setOf("first"),
            unfinishedOnly = true
        )

        assertEquals(listOf("second"), results.map { it.id })
    }

    @Test
    fun uncoveredThemesOnlyContainsThemesWithoutCompletedQuiz() {
        val social = quiz("social", "Social confidence")
        val energy = quiz("energy", "Sleep and energy")

        val uncovered = DiscoverLibraryEngine.uncoveredThemes(
            quizzes = listOf(social, energy),
            completed = setOf("social")
        )

        assertTrue(QuizVisualTheme.ENERGY in uncovered)
        assertTrue(QuizVisualTheme.SOCIAL !in uncovered)
    }

    private fun quiz(
        id: String,
        title: String,
        hook: String = title,
        low: String = "Low",
        high: String = "High"
    ) = Quiz(
        id = id,
        title = title,
        hook = hook,
        time = "2 min",
        accent = "✦",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low",
        midDescription = "Mid",
        highDescription = "High",
        metricLow = low,
        metricHigh = high,
        questions = listOf(
            Question(
                text = "Q",
                answers = listOf(
                    Answer("A", 0), Answer("B", 1), Answer("C", 2), Answer("D", 3)
                )
            )
        )
    )
}
