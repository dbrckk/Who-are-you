package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscoverPersonalizationTest {
    @Test
    fun prefersUnmeasuredThemeBeforeAnotherKnownTheme() {
        val social = quiz("social", "Social confidence")
        val socialAgain = quiz("social_2", "Friend groups")
        val energy = quiz("energy", "Sleep and energy")
        val dimensions = listOf(dimension("social", 82))

        val next = DiscoverPersonalization.nextQuiz(
            quizzes = listOf(social, socialAgain, energy),
            completed = setOf("social"),
            dimensions = dimensions
        )

        assertEquals("energy", next?.id)
    }

    @Test
    fun fallsBackToAnyUnfinishedQuizWhenThemesAreAlreadyMeasured() {
        val social = quiz("social", "Social confidence")
        val socialAgain = quiz("social_2", "Friend groups")
        val dimensions = listOf(dimension("social", 80))

        val next = DiscoverPersonalization.nextQuiz(
            quizzes = listOf(social, socialAgain),
            completed = setOf("social"),
            dimensions = dimensions
        )

        assertEquals("social_2", next?.id)
    }

    @Test
    fun allCompletedFallsBackToFirstQuiz() {
        val first = quiz("first", "Identity")
        val second = quiz("second", "Values")

        val next = DiscoverPersonalization.nextQuiz(
            quizzes = listOf(first, second),
            completed = setOf("first", "second"),
            dimensions = emptyList()
        )

        assertEquals("first", next?.id)
    }

    @Test
    fun emptyCatalogReturnsNull() {
        assertNull(DiscoverPersonalization.nextQuiz(emptyList(), emptySet(), emptyList()))
    }

    @Test
    fun strongestDimensionIsFarthestFromNeutral() {
        val strongest = DiscoverPersonalization.strongestDimension(
            listOf(
                dimension("a", 55),
                dimension("b", 12),
                dimension("c", 72)
            )
        )

        assertEquals("b", strongest?.quizId)
    }

    private fun dimension(id: String, score: Int) = ProfileDimension(
        quizId = id,
        title = id,
        score = score,
        resultTitle = id,
        metricLabel = id
    )

    private fun quiz(id: String, title: String) = Quiz(
        id = id,
        title = title,
        hook = title,
        time = "2 min",
        accent = "✦",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low",
        midDescription = "Mid",
        highDescription = "High",
        metricLow = "Low",
        metricHigh = "High",
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
