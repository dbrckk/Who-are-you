package com.whoareyou.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalModelCompatibilityTest {
    @Test
    fun `zero weight mapping never becomes personal trait evidence`() {
        val quiz = testQuiz(
            id = "neutral-map",
            traits = listOf(QuizTraitWeight("curiosity", 0.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("neutral-map" to 100)
        )

        assertTrue(summary.personalModel.traits.none { it.traitId == "curiosity" })
    }

    @Test
    fun `personal model does not mutate m771 result connection inputs`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("focus", 82, 80, 2)),
            evidenceCount = 2
        )

        PersonalModelEngine.build(
            graph = graph,
            coverage = coverageFor("focus", 2, 80)
        )

        val quiz = testQuiz(
            id = "focus",
            traits = listOf(QuizTraitWeight("focus", 1.0))
        )
        val connections = ResultProfileConnectionEngine.derive(quiz, 84, graph)

        assertFalse(connections.isEmpty())
    }
}
