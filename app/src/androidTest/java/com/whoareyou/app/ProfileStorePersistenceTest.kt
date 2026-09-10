package com.whoareyou.app

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
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
}
