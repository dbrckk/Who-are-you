package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultIntelligenceTest {
    private fun quiz(): Quiz = Quiz(
        id = "test",
        title = "Test dimension",
        hook = "Hook",
        time = "1 min",
        accent = "T",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low description",
        midDescription = "Mid description",
        highDescription = "High description",
        metricLow = "Reflective",
        metricHigh = "Expressive",
        questions = listOf(
            Question("Q1", listOf(Answer("A0", 0), Answer("A1", 1), Answer("A2", 2), Answer("A3", 3))),
            Question("Q2", listOf(Answer("B0", 0), Answer("B1", 1), Answer("B2", 2), Answer("B3", 3))),
            Question("Q3", listOf(Answer("C0", 0), Answer("C1", 1), Answer("C2", 2), Answer("C3", 3))),
            Question("Q4", listOf(Answer("D0", 0), Answer("D1", 1), Answer("D2", 2), Answer("D3", 3)))
        ),
        traits = listOf(QuizTraitWeight("trait", 1.0))
    )

    @Test
    fun strongestEvidencePrioritizesExtremeSelectedAnswers() {
        val summary = ResultIntelligenceEngine.derive(
            quiz = quiz(),
            score = 67,
            selectedAnswerIndexes = listOf(2, 3, 1, 0)
        )

        assertEquals(listOf(1, 3, 0), summary.evidence.map { it.questionIndex })
        assertEquals(listOf(3, 3, 1), summary.evidence.map { it.influence })
        assertEquals("B3", summary.evidence.first().answerText)
    }

    @Test
    fun invalidOrMissingAnswerIndexesAreIgnoredSafely() {
        val summary = ResultIntelligenceEngine.derive(
            quiz = quiz(),
            score = 40,
            selectedAnswerIndexes = listOf(0, 99)
        )

        assertEquals(1, summary.evidence.size)
        assertEquals(0, summary.evidence.single().questionIndex)
        assertEquals("A0", summary.evidence.single().answerText)
    }

    @Test
    fun neutralResultUsesBalancedInsightCues() {
        val summary = ResultIntelligenceEngine.derive(
            quiz = quiz(),
            score = 50,
            selectedAnswerIndexes = emptyList()
        )

        assertEquals(ResultIntelligenceFrame.BALANCED, summary.frame)
        assertEquals(false, summary.hasDirectionalLean)
        assertTrue(ResultInsightCue.FLEXIBILITY in summary.strengthCues)
        assertEquals(ResultInsightCue.NOTICE_CONTEXT, summary.actionCue)
    }

    @Test
    fun strongHighResultUsesPronouncedDeterministicCues() {
        val summary = ResultIntelligenceEngine.derive(
            quiz = quiz(),
            score = 92,
            selectedAnswerIndexes = listOf(3, 3, 3, 2)
        )

        assertEquals(ResultIntelligenceFrame.PRONOUNCED, summary.frame)
        assertEquals(true, summary.hasDirectionalLean)
        assertEquals(ResultDirection.HIGH, summary.direction)
        assertEquals(
            listOf(ResultInsightCue.DECISIVENESS, ResultInsightCue.CONSISTENCY),
            summary.strengthCues
        )
        assertEquals(listOf(ResultInsightCue.OVEREXTENSION), summary.watchOutCues)
        assertEquals(ResultInsightCue.TEST_OPPOSITE, summary.actionCue)
    }

    @Test
    fun clearLowResultKeepsLowDirectionWithoutChangingScore() {
        val summary = ResultIntelligenceEngine.derive(
            quiz = quiz(),
            score = 30,
            selectedAnswerIndexes = listOf(0, 1, 1, 0)
        )

        assertEquals(30, summary.score)
        assertEquals(ResultDirection.LOW, summary.direction)
        assertEquals(ResultIntelligenceFrame.LEANING, summary.frame)
        assertEquals(ResultInsightCue.USE_STRENGTH_DELIBERATELY, summary.actionCue)
    }
}
