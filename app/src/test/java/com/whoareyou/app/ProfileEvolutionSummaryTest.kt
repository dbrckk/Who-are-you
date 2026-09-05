package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileEvolutionSummaryTest {
    private fun dimension(id: String, current: Int, previous: Int? = null) = ProfileDimension(
        quizId = id,
        title = id,
        score = current,
        resultTitle = "result",
        metricLabel = "metric",
        change = ScoreChangeEngine.compare(previous, current)
    )

    @Test
    fun returnsNullWithoutRealHistory() {
        assertNull(ProfileEvolutionSummary.derive(listOf(
            dimension("a", 60),
            dimension("b", 40)
        )))
    }

    @Test
    fun reportsCoverageAndLargestRecentChange() {
        val result = ProfileEvolutionSummary.derive(listOf(
            dimension("a", 60, 55),
            dimension("b", 80, 50),
            dimension("c", 45)
        ))!!

        assertEquals(2, result.trackedCount)
        assertEquals(3, result.totalDimensions)
        assertEquals(66, result.coveragePercent)
        assertEquals("b", result.mostChanged.quizId)
        assertEquals(30, result.mostChanged.scoreChange?.absoluteDelta)
    }

    @Test
    fun tiesKeepStableDimensionOrder() {
        val result = ProfileEvolutionSummary.derive(listOf(
            dimension("first", 70, 60),
            dimension("second", 40, 50)
        ))!!

        assertEquals("first", result.mostChanged.quizId)
    }

    @Test
    fun unchangedRetakeStillCountsAsTracked() {
        val result = ProfileEvolutionSummary.derive(listOf(
            dimension("stable", 50, 50),
            dimension("new", 60)
        ))!!

        assertEquals(1, result.trackedCount)
        assertEquals("stable", result.mostChanged.quizId)
        assertEquals(ScoreChangeDirection.SAME, result.mostChanged.scoreChange?.direction)
    }
}
