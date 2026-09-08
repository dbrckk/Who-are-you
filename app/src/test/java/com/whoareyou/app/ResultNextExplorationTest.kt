package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultNextExplorationTest {
    private val themes = mapOf(
        "a" to QuizVisualTheme.MIND,
        "b" to QuizVisualTheme.MIND,
        "c" to QuizVisualTheme.SOCIAL,
        "d" to QuizVisualTheme.EMOTION
    )

    @Test
    fun `prefers unfinished quiz from same facet`() {
        val result = ResultNextExplorationEngine.recommend(
            currentQuizId = "a",
            orderedQuizIds = listOf("a", "b", "c", "d"),
            themeByQuizId = themes,
            completed = setOf("a")
        )

        assertEquals("b", result?.quizId)
        assertEquals(ResultNextReason.SAME_FACET, result?.reason)
    }

    @Test
    fun `falls back to complementary unfinished facet`() {
        val result = ResultNextExplorationEngine.recommend(
            currentQuizId = "a",
            orderedQuizIds = listOf("a", "b", "c", "d"),
            themeByQuizId = themes,
            completed = setOf("a", "b")
        )

        assertEquals("c", result?.quizId)
        assertEquals(ResultNextReason.COMPLEMENTARY_FACET, result?.reason)
    }

    @Test
    fun `uses same facet retake when catalog is complete`() {
        val result = ResultNextExplorationEngine.recommend(
            currentQuizId = "a",
            orderedQuizIds = listOf("a", "b", "c", "d"),
            themeByQuizId = themes,
            completed = setOf("a", "b", "c", "d")
        )

        assertEquals("b", result?.quizId)
        assertEquals(ResultNextReason.RETAKE_FACET, result?.reason)
    }

    @Test
    fun `returns null when current theme is unknown`() {
        assertNull(
            ResultNextExplorationEngine.recommend(
                currentQuizId = "unknown",
                orderedQuizIds = themes.keys.toList(),
                themeByQuizId = themes,
                completed = emptySet()
            )
        )
    }
}
