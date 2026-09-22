package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ResultScreenIntelligenceTest {
    @Test
    fun `derives authored insight and preserves evidence for result screen`() {
        val quiz = Quiz(
            id = "screen-intelligence",
            title = "Test",
            hook = "Hook",
            time = "1 min",
            accent = "cyan",
            lowTitle = "Low",
            midTitle = "Mid",
            highTitle = "High",
            lowDescription = "Low",
            midDescription = "Mid",
            highDescription = "High",
            metricLow = "Reserved",
            metricHigh = "Expressive",
            questions = listOf(Question("Q?", listOf(Answer("A", 0), Answer("D", 3)))),
            resultIntelligence = QuizResultIntelligenceContent(
                low = ResultInsightContent(listOf("L"), listOf("LW"), listOf("LL"), "LR"),
                balanced = ResultInsightContent(listOf("B"), listOf("BW"), listOf("BL"), "BR"),
                high = ResultInsightContent(listOf("H"), listOf("HW"), listOf("HL"), "HR")
            )
        )
        val evidence = listOf(ResultEvidence(0, "Q?", "D", 3))

        val result = ResultScreenIntelligence.derive(quiz, 80, evidence)

        assertNotNull(result.insight)
        assertEquals("HR", result.insight!!.reflection)
        assertEquals(evidence, result.evidence)
    }
}
