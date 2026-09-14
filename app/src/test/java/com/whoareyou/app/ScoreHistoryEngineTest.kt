package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScoreHistoryEngineTest {
    @Test
    fun firstCompletionStoresOnlyLatestScore() {
        val result = ScoreHistoryEngine.update(emptyMap(), emptyMap(), "planning_style", 72)

        assertEquals(72, result.latestScores["planning_style"])
        assertFalse("planning_style" in result.previousScores)
    }

    @Test
    fun retakeMovesExistingLatestIntoPrevious() {
        val result = ScoreHistoryEngine.update(
            latestScores = mapOf("planning_style" to 61),
            previousScores = emptyMap(),
            quizId = "planning_style",
            score = 78
        )

        assertEquals(78, result.latestScores["planning_style"])
        assertEquals(61, result.previousScores["planning_style"])
    }

    @Test
    fun repeatedRetakeKeepsOnlyImmediatelyPreviousScore() {
        val result = ScoreHistoryEngine.update(
            latestScores = mapOf("planning_style" to 78),
            previousScores = mapOf("planning_style" to 61),
            quizId = "planning_style",
            score = 66
        )

        assertEquals(66, result.latestScores["planning_style"])
        assertEquals(78, result.previousScores["planning_style"])
    }

    @Test
    fun legacyPreviousAndLatestSeedHistoryOnFirstRetake() {
        val result = ScoreHistoryEngine.update(
            latestScores = mapOf("planning_style" to 72),
            previousScores = mapOf("planning_style" to 61),
            quizId = "planning_style",
            score = 80
        )

        assertEquals(listOf(61, 72, 80), result.scoreHistory["planning_style"])
    }

    @Test
    fun repeatedScoresAreKeptInBoundedSeries() {
        var history = emptyMap<String, List<Int>>()
        var latest = emptyMap<String, Int>()
        var previous = emptyMap<String, Int>()

        repeat(12) { index ->
            val result = ScoreHistoryEngine.update(
                latestScores = latest,
                previousScores = previous,
                quizId = "planning_style",
                score = index * 10,
                scoreHistory = history
            )
            latest = result.latestScores
            previous = result.previousScores
            history = result.scoreHistory
        }

        val series = history.getValue("planning_style")
        assertEquals(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ, series.size)
        assertEquals(listOf(40, 50, 60, 70, 80, 90, 100, 100), series)
    }

    @Test
    fun updateDoesNotDisturbOtherDimensions() {
        val result = ScoreHistoryEngine.update(
            latestScores = mapOf("planning_style" to 61, "adaptability" to 84),
            previousScores = mapOf("adaptability" to 70),
            quizId = "planning_style",
            score = 78
        )

        assertEquals(84, result.latestScores["adaptability"])
        assertEquals(70, result.previousScores["adaptability"])
    }
}
