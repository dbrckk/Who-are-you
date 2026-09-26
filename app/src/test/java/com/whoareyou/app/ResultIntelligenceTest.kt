package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultIntelligenceTest {
    private val quiz = Quiz(
        id = "intelligence-test",
        title = "Test",
        hook = "Hook",
        time = "1 min",
        accent = "cyan",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low description",
        midDescription = "Mid description",
        highDescription = "High description",
        metricLow = "Reserved",
        metricHigh = "Expressive",
        questions = listOf(Question("Question?", listOf(Answer("A",0), Answer("B",1), Answer("C",2), Answer("D",3)))),
        resultIntelligence = QuizResultIntelligenceContent(
            low = ResultInsightContent(listOf("low-strength-1"), listOf("low-watch"), listOf("low-life"), "low-reflection"),
            balanced = ResultInsightContent(listOf("balanced-strength"), listOf("balanced-watch"), listOf("balanced-life"), "balanced-reflection"),
            high = ResultInsightContent(listOf("high-strength-1","high-strength-2","high-strength-3","ignored"), listOf("high-watch"), listOf("high-life"), "high-reflection")
        )
    )

    @Test fun `strong high result selects high content and caps lists`() {
        val result = ResultInsightEngine.derive(quiz, 88)!!
        assertEquals(ResultSignalStrength.STRONG, result.strength)
        assertEquals(ResultDirection.HIGH, result.direction)
        assertEquals(listOf("high-strength-1","high-strength-2","high-strength-3"), result.strengths)
        assertTrue(result.watchOuts.size <= 3)
        assertEquals("high-reflection", result.reflection)
    }

    @Test fun `strong low result selects low content`() {
        val result = ResultInsightEngine.derive(quiz, 12)!!
        assertEquals(ResultDirection.LOW, result.direction)
        assertEquals("low-reflection", result.reflection)
    }

    @Test fun `neutral result uses balanced content rather than high content`() {
        val result = ResultInsightEngine.derive(quiz, 50)!!
        assertEquals(ResultSignalStrength.BALANCED, result.strength)
        assertEquals("balanced-reflection", result.reflection)
    }

    @Test fun `missing authored intelligence degrades gracefully`() {
        assertEquals(null, ResultInsightEngine.derive(quiz.copy(resultIntelligence = null), 80))
    }
}
