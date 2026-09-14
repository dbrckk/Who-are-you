package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalTrendEngineTest {
    @Test
    fun `rising series is detected`() {
        val trend = LongitudinalTrendEngine.build(
            quizId = "q",
            points = listOf(
                TimedScore(40, 1),
                TimedScore(50, 2),
                TimedScore(60, 3),
                TimedScore(72, 4)
            )
        )

        assertEquals(LongitudinalTrendKind.RISING, trend.kind)
        assertEquals(32, trend.netChange)
        assertTrue(trend.slopePerStep > 2.0)
    }

    @Test
    fun `volatile series is detected`() {
        val trend = LongitudinalTrendEngine.build(
            quizId = "q",
            points = listOf(
                TimedScore(20, 1),
                TimedScore(85, 2),
                TimedScore(25, 3),
                TimedScore(80, 4)
            )
        )

        assertEquals(LongitudinalTrendKind.VOLATILE, trend.kind)
        assertTrue(trend.volatility >= 14.0)
    }

    @Test
    fun `isolated last score is flagged as outlier`() {
        val trend = LongitudinalTrendEngine.build(
            quizId = "q",
            points = listOf(
                TimedScore(50, 1),
                TimedScore(52, 2),
                TimedScore(49, 3),
                TimedScore(90, 4)
            )
        )

        assertEquals(LongitudinalTrendKind.OUTLIER, trend.kind)
    }

    @Test
    fun `series remains bounded to recent history`() {
        val points = (1L..12L).map { day -> TimedScore((day * 5).toInt(), day) }
        val trend = LongitudinalTrendEngine.build("q", points)

        assertEquals(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ, trend.points.size)
    }
}
