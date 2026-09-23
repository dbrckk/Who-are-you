package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PersonalModelContractTest {
    @Test
    fun `personal trait exposes certainty stability contradiction and trend`() {
        val trait = PersonalTrait(
            traitId = "curiosity",
            score = 78,
            confidence = 72,
            certainty = PersonalCertainty.LIKELY,
            stability = PersonalStability.STABLE,
            contradictionLevel = ContradictionLevel.LOW,
            evidenceCount = 3,
            sourceQuizIds = listOf("a", "b", "c"),
            trend = PersonalTrend.STABLE,
            isDistinctive = true
        )

        assertEquals(PersonalCertainty.LIKELY, trait.certainty)
        assertEquals(PersonalStability.STABLE, trait.stability)
        assertEquals(ContradictionLevel.LOW, trait.contradictionLevel)
        assertEquals(PersonalTrend.STABLE, trait.trend)
        assertFalse(trait.sourceQuizIds.isEmpty())
    }
}
