package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultEvidenceTest {
    private val questions = listOf(
        Question("Q1", listOf(Answer("low", 0), Answer("high", 3))),
        Question("Q2", listOf(Answer("low", 0), Answer("high", 3))),
        Question("Q3", listOf(Answer("low", 0), Answer("high", 3))),
        Question("Q4", listOf(Answer("low", 0), Answer("high", 3)))
    )

    @Test fun `ranks strongest contributions and caps evidence at three`() {
        val evidence = ResultEvidenceEngine.derive(
            questions,
            listOf(
                AnswerContribution(0, 0, -3),
                AnswerContribution(1, 1, 3),
                AnswerContribution(2, 1, 2),
                AnswerContribution(3, 0, -1)
            )
        )
        assertEquals(listOf(0, 1, 2), evidence.map { it.questionIndex })
        assertEquals(listOf(-3, 3, 2), evidence.map { it.contribution })
    }

    @Test fun `ties preserve original question order`() {
        val evidence = ResultEvidenceEngine.derive(
            questions,
            listOf(AnswerContribution(2, 1, 3), AnswerContribution(0, 1, 3), AnswerContribution(1, 0, -3))
        )
        assertEquals(listOf(0, 1, 2), evidence.map { it.questionIndex })
    }

    @Test fun `missing contributions produce no speculative evidence`() {
        assertTrue(ResultEvidenceEngine.derive(questions, emptyList()).isEmpty())
    }

    @Test fun `invalid indices are ignored safely`() {
        val evidence = ResultEvidenceEngine.derive(
            questions,
            listOf(AnswerContribution(99, 0, 9), AnswerContribution(0, 99, 8), AnswerContribution(1, 1, 2))
        )
        assertEquals(listOf(1), evidence.map { it.questionIndex })
        assertEquals("high", evidence.single().answerText)
    }
}
