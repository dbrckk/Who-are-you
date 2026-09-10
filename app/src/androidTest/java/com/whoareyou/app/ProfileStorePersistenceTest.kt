package com.whoareyou.app

import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlinx.coroutines.runBlocking
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
        }
    }
}
