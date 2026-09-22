package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizAttemptEvidenceTest {
    @Test fun `records selected answers as ephemeral contributions`() {
        val state = QuizAttemptEvidence()
        state.record(questionIndex = 0, answerIndex = 1, score = 3)
        state.record(questionIndex = 1, answerIndex = 0, score = 0)

        assertEquals(
            listOf(
                AnswerContribution(0, 1, 3),
                AnswerContribution(1, 0, 0)
            ),
            state.snapshot()
        )
    }

    @Test fun `re-answering a question replaces its prior contribution`() {
        val state = QuizAttemptEvidence()
        state.record(0, 0, 0)
        state.record(0, 1, 3)

        assertEquals(listOf(AnswerContribution(0, 1, 3)), state.snapshot())
    }

    @Test fun `clear removes all attempt evidence`() {
        val state = QuizAttemptEvidence()
        state.record(0, 1, 3)
        state.clear()

        assertTrue(state.snapshot().isEmpty())
    }
}
