package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RetakeRecommendationEngineTest {
    @Test
    fun emptyCatalogHasNoRetakeRecommendation() {
        assertNull(RetakeRecommendationEngine.recommend(emptyList(), emptyMap(), emptyMap()))
    }

    @Test
    fun prioritizesFirstCompletedDimensionWithoutHistory() {
        val result = RetakeRecommendationEngine.recommend(
            quizIds = listOf("a", "b", "c"),
            latestScores = mapOf("a" to 60, "b" to 70, "c" to 80),
            previousScores = mapOf("a" to 55)
        )

        assertEquals("b", result?.quizId)
        assertEquals(RetakeRecommendationReason.START_TRACKING, result?.reason)
    }

    @Test
    fun fullyTrackedProfileChoosesLargestRecentChange() {
        val result = RetakeRecommendationEngine.recommend(
            quizIds = listOf("a", "b", "c"),
            latestScores = mapOf("a" to 60, "b" to 80, "c" to 45),
            previousScores = mapOf("a" to 55, "b" to 60, "c" to 50)
        )

        assertEquals("b", result?.quizId)
        assertEquals(RetakeRecommendationReason.RECHECK_CHANGE, result?.reason)
    }

    @Test
    fun equalChangeUsesStableCatalogOrder() {
        val result = RetakeRecommendationEngine.recommend(
            quizIds = listOf("first", "second"),
            latestScores = mapOf("first" to 65, "second" to 35),
            previousScores = mapOf("first" to 50, "second" to 50)
        )

        assertEquals("first", result?.quizId)
    }

    @Test
    fun ignoresCatalogEntriesWithoutLatestScores() {
        val result = RetakeRecommendationEngine.recommend(
            quizIds = listOf("missing", "tracked"),
            latestScores = mapOf("tracked" to 70),
            previousScores = mapOf("tracked" to 60)
        )

        assertEquals("tracked", result?.quizId)
        assertEquals(RetakeRecommendationReason.RECHECK_CHANGE, result?.reason)
    }
}
