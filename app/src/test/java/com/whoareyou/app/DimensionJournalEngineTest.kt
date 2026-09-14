package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DimensionJournalEngineTest {
    @Test
    fun `journal keeps ordered dated entries and total change`() {
        val journal = DimensionJournalEngine.build(
            quizId = "curiosity",
            points = listOf(
                TimedScore(55, 10),
                TimedScore(61, 20),
                TimedScore(74, 30)
            )
        )!!

        assertEquals(3, journal.entries.size)
        assertEquals(55, journal.firstScore)
        assertEquals(74, journal.latestScore)
        assertEquals(19, journal.totalChange)
        assertEquals(10, journal.entries.first().epochDay)
        assertEquals(30, journal.entries.last().epochDay)
        assertEquals(55, journal.periodComparison?.earlierAverage)
        assertEquals(67, journal.periodComparison?.recentAverage)
        assertEquals(12, journal.periodComparison?.delta)
    }

    @Test
    fun `journal distinguishes first measurement and retake change`() {
        val journal = DimensionJournalEngine.build(
            quizId = "curiosity",
            points = listOf(
                TimedScore(50, 1),
                TimedScore(72, 2)
            )
        )!!

        assertEquals(JournalChangeKind.FIRST_MEASUREMENT, journal.entries[0].kind)
        assertEquals(JournalChangeKind.RETAKE_CHANGE, journal.entries[1].kind)
        assertEquals(22, journal.entries[1].deltaFromPrevious)
    }

    @Test
    fun `outlier classification reaches latest journal entry`() {
        val journal = DimensionJournalEngine.build(
            quizId = "curiosity",
            points = listOf(
                TimedScore(50, 1),
                TimedScore(51, 2),
                TimedScore(49, 3),
                TimedScore(91, 4)
            )
        )!!

        assertEquals(LongitudinalTrendKind.OUTLIER, journal.trendKind)
        assertEquals(JournalChangeKind.OUTLIER, journal.entries.last().kind)
        assertTrue(journal.totalChange > 0)
    }
}
