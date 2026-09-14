package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileIntegrityTest {
    @Test
    fun sanitizeClampsScoresAndMatchStats() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                completedQuizIds = setOf(" quiz-a ", "", "quiz-b"),
                latestScores = mapOf(" quiz-a " to 140, "quiz-b" to -20, " " to 50),
                previousScores = mapOf("quiz-a" to 101),
                matchCount = -4,
                bestMatchPercent = 180,
                lowestMatchPercent = -30
            )
        )

        assertEquals(setOf("quiz-a", "quiz-b"), sanitized.completedQuizIds)
        assertEquals(mapOf("quiz-a" to 100, "quiz-b" to 0), sanitized.latestScores)
        assertEquals(mapOf("quiz-a" to 100), sanitized.previousScores)
        assertEquals(0, sanitized.matchCount)
        assertNull(sanitized.bestMatchPercent)
        assertNull(sanitized.lowestMatchPercent)
    }

    @Test
    fun sanitizeRepairsCompletionFromPersistedLatestScores() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                completedQuizIds = setOf("quiz-a"),
                latestScores = mapOf(" quiz-b " to 72)
            )
        )

        assertEquals(setOf("quiz-a", "quiz-b"), sanitized.completedQuizIds)
        assertEquals(mapOf("quiz-b" to 72), sanitized.latestScores)
    }

    @Test
    fun sanitizeDropsPreviousScoresWithoutCurrentScore() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                latestScores = mapOf("quiz-a" to 70),
                previousScores = mapOf("quiz-a" to 60, "quiz-orphan" to 45)
            )
        )

        assertEquals(mapOf("quiz-a" to 60), sanitized.previousScores)
    }

    @Test
    fun sanitizeRepairsAndBoundsPersistedHistories() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                latestScores = mapOf("quiz-a" to 70),
                scoreHistory = mapOf(
                    " quiz-a " to listOf(-10, 10, 20, 30, 40, 50, 60, 70, 80, 120),
                    "orphan" to listOf(50)
                ),
                timedScoreHistory = mapOf(
                    "quiz-a" to listOf(
                        TimedScore(-5, -10),
                        TimedScore(50, 12),
                        TimedScore(130, 20)
                    ),
                    "orphan" to listOf(TimedScore(60, 30))
                )
            )
        )

        assertEquals(
            listOf(20, 30, 40, 50, 60, 70, 80, 100),
            sanitized.scoreHistory["quiz-a"]
        )
        assertEquals(
            listOf(
                TimedScore(0, 0),
                TimedScore(50, 12),
                TimedScore(100, 20)
            ),
            sanitized.timedScoreHistory["quiz-a"]
        )
        assertTrue("orphan" !in sanitized.scoreHistory)
        assertTrue("orphan" !in sanitized.timedScoreHistory)
    }

    @Test
    fun sanitizeRepairsReversedMatchBounds() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                matchCount = 3,
                bestMatchPercent = 24,
                lowestMatchPercent = 91
            )
        )

        assertEquals(91, sanitized.bestMatchPercent)
        assertEquals(24, sanitized.lowestMatchPercent)
    }

    @Test
    fun sanitizeDropsIncompleteDailyAnswerAndInvalidDate() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                daily = DailyState(
                    answeredDate = "not-a-date",
                    questionId = " question ",
                    selectedOption = 9,
                    currentStreak = -8,
                    longestStreak = -2,
                    lastActiveDate = "also-invalid"
                )
            )
        )

        assertNull(sanitized.daily.answeredDate)
        assertNull(sanitized.daily.questionId)
        assertNull(sanitized.daily.selectedOption)
        assertNull(sanitized.daily.lastActiveDate)
        assertEquals(0, sanitized.daily.currentStreak)
        assertEquals(0, sanitized.daily.longestStreak)
    }

    @Test
    fun sanitizeKeepsValidDailyStateAndLifetimeStreak() {
        val sanitized = ProfileIntegrity.sanitize(
            StoredProfile(
                pendingAchievementIds = listOf(" first ", "first", "", "second"),
                daily = DailyState(
                    answeredDate = "2026-09-09",
                    questionId = " daily-id ",
                    selectedOption = 1,
                    currentStreak = 7,
                    longestStreak = 3,
                    lastActiveDate = "2026-09-09"
                )
            )
        )

        assertEquals(listOf("first", "second"), sanitized.pendingAchievementIds)
        assertEquals("2026-09-09", sanitized.daily.answeredDate)
        assertEquals("daily-id", sanitized.daily.questionId)
        assertEquals(1, sanitized.daily.selectedOption)
        assertEquals(7, sanitized.daily.currentStreak)
        assertEquals(7, sanitized.daily.longestStreak)
        assertTrue(sanitized.daily.answeredToday(java.time.LocalDate.of(2026, 9, 9)))
    }
}
