package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitExplorationEngineTest {
    @Test
    fun `unknown trait recommends strongest unfinished measurement`() {
        val coverage = coverage(TraitCoverage("curiosity", 0, 0, 0, CoverageStatus.UNKNOWN))
        val catalog = listOf(
            quiz("weak", QuizTraitWeight("curiosity", 0.35)),
            quiz("strong", QuizTraitWeight("curiosity", 1.0))
        )

        val result = TraitExplorationEngine.build(
            "curiosity", coverage, TraitGraph(emptyList(), 0), catalog, emptySet()
        )

        assertEquals("strong", result?.recommendedQuizId)
        assertTrue(result!!.evidence.isEmpty())
    }

    @Test
    fun `completed quiz is never recommended again for trait measurement`() {
        val coverage = coverage(TraitCoverage("curiosity", 1, 40, 0, CoverageStatus.LOW))
        val catalog = listOf(quiz("only", QuizTraitWeight("curiosity", 1.0)))

        val result = TraitExplorationEngine.build(
            "curiosity", coverage, TraitGraph(emptyList(), 0), catalog, setOf("only")
        )

        assertNull(result?.recommendedQuizId)
    }

    private fun coverage(trait: TraitCoverage) = ProfileCoverage(
        knownTraitCount = if (trait.status == CoverageStatus.UNKNOWN) 0 else 1,
        totalTraitCount = 1,
        coveragePercent = if (trait.status == CoverageStatus.UNKNOWN) 0 else 100,
        averageConfidence = trait.confidence,
        strongTraitCount = if (trait.status == CoverageStatus.STRONG) 1 else 0,
        uncertainTraitCount = if (trait.status == CoverageStatus.STRONG) 0 else 1,
        traits = listOf(trait)
    )

    private fun quiz(id: String, vararg traits: QuizTraitWeight) = Quiz(
        id, id, id, "1 MIN", "•",
        "low", "mid", "high",
        "low", "mid", "high",
        "LOW", "HIGH",
        listOf(Question("q", listOf(Answer("a", 0), Answer("b", 1), Answer("c", 2), Answer("d", 3)))),
        traits.toList()
    )
}
