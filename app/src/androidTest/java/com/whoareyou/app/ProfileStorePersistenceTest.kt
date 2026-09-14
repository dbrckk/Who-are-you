package com.whoareyou.app

import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileStorePersistenceTest {
    @Test
    fun blankQuizIdIsRejectedWithoutWrite() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val changed = runBlocking {
            ProfileStore.saveQuizResult(
                context = context,
                quizId = "   ",
                score = 75,
                attemptId = "invalid-id-test"
            )
        }

        assertFalse(changed)
    }

    @Test
    fun staleAttemptReplayIsRejectedAfterNewerAttempt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quizId = "idempotence-${UUID.randomUUID()}"
        val firstAttempt = UUID.randomUUID().toString()
        val secondAttempt = UUID.randomUUID().toString()

        runBlocking {
            assertTrue(ProfileStore.saveQuizResult(context, quizId, 41, firstAttempt))
            assertTrue(ProfileStore.saveQuizResult(context, quizId, 83, secondAttempt))
            assertFalse(ProfileStore.saveQuizResult(context, quizId, 41, firstAttempt))

            val profile = ProfileStore.observe(context).first()
            assertEquals(83, profile.latestScores[quizId])
            assertEquals(41, profile.previousScores[quizId])
        }
    }

    @Test
    fun immediateAttemptReplayCannotOverwritePersistedScore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quizId = "duplicate-${UUID.randomUUID()}"
        val attemptId = UUID.randomUUID().toString()

        runBlocking {
            assertTrue(ProfileStore.saveQuizResult(context, quizId, 52, attemptId))
            assertFalse(ProfileStore.saveQuizResult(context, quizId, 99, attemptId))

            val profile = ProfileStore.observe(context).first()
            assertEquals(52, profile.latestScores[quizId])
            assertFalse(quizId in profile.previousScores)
        }
    }

    @Test
    fun atomicCommitReturnsPersistedScoreAndRejectsReplay() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quizId = "atomic-${UUID.randomUUID()}"
        val attemptId = UUID.randomUUID().toString()

        runBlocking {
            val first = ProfileStore.commitQuizResult(context, quizId, 140, attemptId)
            assertEquals(true, first?.changed)
            assertEquals(100, first?.persistedScore)

            val replay = ProfileStore.commitQuizResult(context, quizId, 5, attemptId)
            assertEquals(false, replay?.changed)
            assertEquals(100, replay?.persistedScore)
        }
    }

    @Test
    fun mismatchedReplayCannotFabricatePersistedScore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val firstQuiz = "replay-source-${UUID.randomUUID()}"
        val secondQuiz = "replay-target-${UUID.randomUUID()}"
        val attemptId = UUID.randomUUID().toString()

        runBlocking {
            val first = ProfileStore.commitQuizResult(context, firstQuiz, 64, attemptId)
            assertEquals(true, first?.changed)
            assertEquals(64, first?.persistedScore)

            val mismatched = ProfileStore.commitQuizResult(context, secondQuiz, 91, attemptId)
            assertEquals(null, mismatched)

            val profile = ProfileStore.observe(context).first()
            assertFalse(secondQuiz in profile.latestScores)
        }
    }

    @Test
    fun quizResultNormalizesIdAndScoreBeforePersistence() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quizId = "normalized-${UUID.randomUUID()}"

        runBlocking {
            assertTrue(
                ProfileStore.saveQuizResult(
                    context = context,
                    quizId = "  $quizId  ",
                    score = 140,
                    attemptId = "  ${UUID.randomUUID()}  "
                )
            )

            val profile = ProfileStore.observe(context).first()
            assertTrue(quizId in profile.completedQuizIds)
            assertEquals(100, profile.latestScores[quizId])
        }
    }
}
