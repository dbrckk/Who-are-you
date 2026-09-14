package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitGraphEngineTest {
    @Test
    fun `positive weight follows source score`() {
        val graph = TraitGraphEngine.build(
            listOf(
                dimension(
                    id = "a",
                    score = 80,
                    traits = listOf(QuizTraitWeight("assertiveness", 1.0))
                )
            )
        )

        val trait = graph.traits.single()
        assertEquals("assertiveness", trait.id)
        assertEquals(80, trait.score)
        assertEquals(1, trait.evidenceCount)
        assertTrue(trait.confidence > 0)
    }

    @Test
    fun `negative weight reverses contribution around midpoint`() {
        val graph = TraitGraphEngine.build(
            listOf(
                dimension(
                    id = "a",
                    score = 80,
                    traits = listOf(QuizTraitWeight("structure", -1.0))
                )
            )
        )

        assertEquals(20, graph.traits.single().score)
    }

    @Test
    fun `multiple sources preserve contradictory evidence`() {
        val graph = TraitGraphEngine.build(
            listOf(
                dimension("a", 85, listOf(QuizTraitWeight("adaptability", 1.0))),
                dimension("b", 20, listOf(QuizTraitWeight("adaptability", 1.0)))
            )
        )

        val trait = graph.traits.single()
        assertEquals(2, trait.evidenceCount)
        assertTrue(trait.contradictoryEvidenceCount >= 1)
        assertEquals(2, graph.evidenceCount)
    }

    private fun dimension(
        id: String,
        score: Int,
        traits: List<QuizTraitWeight>
    ) = ProfileDimension(
        quizId = id,
        title = id,
        score = score,
        resultTitle = id,
        metricLabel = id,
        traitWeights = traits
    )
}
