package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultProfileConnectionTest {
    private val quiz = Quiz(
        id = "current",
        title = "Current",
        hook = "Hook",
        time = "1 min",
        accent = "cyan",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low",
        midDescription = "Mid",
        highDescription = "High",
        metricLow = "Low metric",
        metricHigh = "High metric",
        questions = listOf(Question("Q", listOf(Answer("A", 0), Answer("B", 1), Answer("C", 2), Answer("D", 3)))),
        traits = listOf(QuizTraitWeight("focus", 1.0))
    )

    @Test fun reinforcing_trait_produces_reinforcing_connection() {
        val graph = TraitGraph(
            traits = listOf(ProfileTrait("focus", 82, 80, emptyList(), 0)),
            evidenceCount = 2
        )

        val result = ResultProfileConnectionEngine.derive(quiz, 84, graph)

        assertEquals(ResultProfileConnectionKind.REINFORCING, result.single().kind)
        assertEquals("focus", result.single().traitId)
    }

    @Test fun opposite_trait_direction_produces_contrasting_connection() {
        val graph = TraitGraph(
            traits = listOf(ProfileTrait("focus", 18, 80, emptyList(), 0)),
            evidenceCount = 2
        )

        val result = ResultProfileConnectionEngine.derive(quiz, 84, graph)

        assertEquals(ResultProfileConnectionKind.CONTRASTING, result.single().kind)
    }

    @Test fun sparse_or_low_confidence_profile_stays_silent() {
        val graph = TraitGraph(
            traits = listOf(ProfileTrait("focus", 82, 20, emptyList(), 0)),
            evidenceCount = 1
        )

        assertTrue(ResultProfileConnectionEngine.derive(quiz, 84, graph).isEmpty())
    }

    @Test fun zero_weight_mapping_does_not_claim_a_profile_connection() {
        val zeroWeightQuiz = quiz.copy(traits = listOf(QuizTraitWeight("focus", 0.0)))
        val graph = TraitGraph(
            traits = listOf(ProfileTrait("focus", 82, 80, emptyList(), 2)),
            evidenceCount = 2
        )

        assertTrue(ResultProfileConnectionEngine.derive(zeroWeightQuiz, 84, graph).isEmpty())
    }
}
