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
