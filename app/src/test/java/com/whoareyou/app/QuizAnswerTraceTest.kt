package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizAnswerTraceTest {
    @Test
    fun appendAddsOneAnswerWithoutMutatingPreviousTrace() {
        val previous = arrayListOf(1)

        val updated = QuizAnswerTrace.append(previous, 2)

        assertEquals(listOf(1), previous)
        assertEquals(listOf(1, 2), updated)
    }

    @Test
    fun completeIncludesFinalAnswerExactlyOnce() {
        val trace = arrayListOf(0, 2, 1)

        val completed = QuizAnswerTrace.complete(
            previous = trace,
            finalAnswerIndex = 3,
            expectedQuestionCount = 4
        )

        assertEquals(listOf(0, 2, 1, 3), completed)
    }

    @Test
    fun completeBoundsCorruptTraceToExpectedQuestionCount() {
        val completed = QuizAnswerTrace.complete(
            previous = arrayListOf(0, 1, 2, 3, 0),
            finalAnswerIndex = 2,
            expectedQuestionCount = 4
        )

        assertEquals(listOf(0, 1, 2, 2), completed)
    }
}
