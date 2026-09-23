package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalModelContradictionTest {
    @Test
    fun `weak opposing signals stay low contradiction even when they are numerous`() {
        val trait = ProfileTrait(
            id = "independence",
            score = 76,
            confidence = 78,
            evidence = listOf(
                evidence("a", contribution = 82, strength = 0.9),
                evidence("b", contribution = 77, strength = 0.85),
                evidence("c", contribution = 48, strength = 0.05),
                evidence("d", contribution = 47, strength = 0.05)
            ),
            contradictoryEvidenceCount = 2
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 4),
            coverageFor("independence", 4, 78)
        )

        assertEquals(ContradictionLevel.LOW, model.traits.single().contradictionLevel)
    }

    @Test
    fun `strong opposing signals can produce high contradiction`() {
        val trait = ProfileTrait(
            id = "independence",
            score = 56,
            confidence = 70,
            evidence = listOf(
                evidence("a", contribution = 85, strength = 0.9),
                evidence("b", contribution = 80, strength = 0.85),
                evidence("c", contribution = 20, strength = 0.9),
                evidence("d", contribution = 18, strength = 0.85)
            ),
            contradictoryEvidenceCount = 2
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 4),
            coverageFor("independence", 4, 70)
        )

        assertEquals(ContradictionLevel.HIGH, model.traits.single().contradictionLevel)
        assertEquals(PersonalCertainty.EXPLORING, model.traits.single().certainty)
    }
}
