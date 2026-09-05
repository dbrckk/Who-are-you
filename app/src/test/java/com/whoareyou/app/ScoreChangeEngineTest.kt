package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScoreChangeEngineTest {
    @Test
    fun firstAttemptHasNoComparison() {
        assertNull(ScoreChangeEngine.compare(null, 72))
    }

    @Test
    fun higherScoreReportsPositiveDelta() {
        val change = ScoreChangeEngine.compare(62, 74)

        assertEquals(12, change?.delta)
        assertEquals(12, change?.absoluteDelta)
        assertEquals(ScoreChangeDirection.HIGHER, change?.direction)
    }

    @Test
    fun lowerScoreReportsNegativeDelta() {
        val change = ScoreChangeEngine.compare(81, 69)

        assertEquals(-12, change?.delta)
        assertEquals(12, change?.absoluteDelta)
        assertEquals(ScoreChangeDirection.LOWER, change?.direction)
    }

    @Test
    fun sameScoreReportsNoMovement() {
        val change = ScoreChangeEngine.compare(55, 55)

        assertEquals(0, change?.delta)
        assertEquals(0, change?.absoluteDelta)
        assertEquals(ScoreChangeDirection.SAME, change?.direction)
    }

    @Test
    fun scoresAreBoundedBeforeComparison() {
        val change = ScoreChangeEngine.compare(-5, 120)

        assertEquals(0, change?.previousScore)
        assertEquals(100, change?.currentScore)
        assertEquals(100, change?.delta)
    }
}
