package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalModelCertaintyTest {
    @Test
    fun `one evidence source can never become established`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 80, 90, evidenceCount = 1)),
            evidenceCount = 1
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 1, 90))

        assertEquals(PersonalCertainty.EXPLORING, model.traits.single().certainty)
    }

    @Test
    fun `coherent multi source evidence can become established`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 80, 75, evidenceCount = 3)),
            evidenceCount = 3
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 3, 75))

        assertEquals(PersonalCertainty.ESTABLISHED, model.traits.single().certainty)
    }

    @Test
    fun `well known neutral trait is not distinctive`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 52, 85, evidenceCount = 4)),
            evidenceCount = 4
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 4, 85))

        assertFalse(model.traits.single().isDistinctive)
    }

    @Test
    fun `directional likely trait is distinctive`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 72, 55, evidenceCount = 2)),
            evidenceCount = 2
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 2, 55))

        assertTrue(model.traits.single().isDistinctive)
    }

    private fun profileTrait(
        id: String,
        score: Int,
        confidence: Int,
        evidenceCount: Int
    ): ProfileTrait = ProfileTrait(
        id = id,
        score = score,
        confidence = confidence,
        evidence = List(evidenceCount) { index ->
            TraitEvidence(
                quizId = "q$index",
                quizTitle = "Q$index",
                sourceScore = score,
                weight = 1.0,
                contribution = score,
                signalStrength = 0.8
            )
        },
        contradictoryEvidenceCount = 0
    )

    private fun coverageFor(
        id: String,
        evidenceCount: Int,
        confidence: Int
    ): ProfileCoverage = ProfileCoverage(
        knownTraitCount = 1,
        totalTraitCount = 1,
        coveragePercent = 100,
        averageConfidence = confidence,
        strongTraitCount = 1,
        uncertainTraitCount = 0,
        traits = listOf(
            TraitCoverage(
                traitId = id,
                evidenceCount = evidenceCount,
                confidence = confidence,
                contradictoryEvidenceCount = 0,
                status = CoverageStatus.STRONG
            )
        )
    )
}
