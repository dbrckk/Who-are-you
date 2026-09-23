package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GlobalProfilePersonalModelTest {
    @Test
    fun `global profile exposes personal model`() {
        val quiz = testQuiz(
            id = "curiosity-quiz",
            traits = listOf(QuizTraitWeight("curiosity", 1.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("curiosity-quiz" to 82),
            scoreHistory = mapOf("curiosity-quiz" to listOf(82)),
            timedScoreHistory = emptyMap()
        )

        assertNotNull(summary.personalModel)
        assertEquals(PersonalCertainty.EXPLORING, summary.personalModel.traits.single().certainty)
        assertEquals(PersonalStability.UNKNOWN, summary.personalModel.traits.single().stability)
    }

    @Test
    fun `historic sparse profile builds without timed history`() {
        val quiz = testQuiz(
            id = "planning-quiz",
            traits = listOf(QuizTraitWeight("planning", 1.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("planning-quiz" to 68)
        )

        assertEquals(1, summary.personalModel.traits.size)
        assertEquals(PersonalStability.UNKNOWN, summary.personalModel.traits.single().stability)
    }
}
