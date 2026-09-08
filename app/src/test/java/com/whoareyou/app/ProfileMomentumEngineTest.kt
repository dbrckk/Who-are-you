package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileMomentumEngineTest {
    @Test
    fun separatesMeaningfulMovementFromStableDimensions() {
        val result = ProfileMomentumEngine.derive(
            listOf(
                dimension("a", 60, 12),
                dimension("b", 55, -3),
                dimension("c", 40, -8),
                dimension("d", 50, null)
            )
        )

        assertEquals(2, result.changingCount)
        assertEquals(1, result.stableCount)
        assertEquals("a", result.strongestChange?.quizId)
    }

    @Test
    fun noHistoryProducesEmptyMomentum() {
        val result = ProfileMomentumEngine.derive(listOf(dimension("a", 60, null)))

        assertEquals(0, result.changingCount)
        assertEquals(0, result.stableCount)
        assertNull(result.strongestChange)
    }

    private fun dimension(id: String, score: Int, delta: Int?): ProfileDimension {
        val change = delta?.let {
            ScoreChange(
                previousScore = score - it,
                currentScore = score,
                delta = it,
                absoluteDelta = kotlin.math.abs(it),
                direction = when {
                    it > 0 -> ScoreChangeDirection.HIGHER
                    it < 0 -> ScoreChangeDirection.LOWER
                    else -> ScoreChangeDirection.SAME
                }
            )
        }
        return ProfileDimension(
            quizId = id,
            title = id,
            score = score,
            resultTitle = id,
            metricLabel = id,
            change = change
        )
    }
}
