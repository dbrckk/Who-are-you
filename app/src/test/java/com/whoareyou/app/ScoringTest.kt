package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {
    @Test
    fun quizPercent_handlesBoundsAndMidpoints() {
        assertEquals(0, Scoring.quizPercent(0, 5))
        assertEquals(100, Scoring.quizPercent(15, 5))
        assertEquals(53, Scoring.quizPercent(8, 5))
        assertEquals(0, Scoring.quizPercent(-5, 5))
        assertEquals(100, Scoring.quizPercent(99, 5))
        assertEquals(0, Scoring.quizPercent(5, 0))
    }

    @Test
    fun compatibility_isSymmetricAndClamped() {
        assertEquals(100, Scoring.compatibility(72, 72))
        assertEquals(80, Scoring.compatibility(30, 50))
        assertEquals(80, Scoring.compatibility(50, 30))
        assertEquals(0, Scoring.compatibility(0, 100))
        assertEquals(100, Scoring.compatibility(-20, -10))
        assertEquals(100, Scoring.compatibility(120, 150))
    }
}
