package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WhoAmIUiModelTest {
    @Test
    fun `portrait UI model keeps safe labels and certainty`() {
        val trait = PersonalTrait(
            traitId = "curiosity", score = 82, confidence = 78,
            certainty = PersonalCertainty.ESTABLISHED,
            stability = PersonalStability.STABLE,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 3, sourceQuizIds = listOf("q1", "q2"),
            trend = PersonalTrend.STABLE, isDistinctive = true
        )
        val row = WhoAmIUiModel.traitRow(trait)
        assertEquals(R.string.trait_curiosity, row.labelRes)
        assertEquals(R.string.who_am_i_certainty_established, row.certaintyRes)
    }

    @Test
    fun `unknown trait IDs never become UI labels`() {
        val trait = PersonalTrait(
            traitId = "internal_secret_trait", score = 80, confidence = 70,
            certainty = PersonalCertainty.LIKELY,
            stability = PersonalStability.MODERATE,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 2, sourceQuizIds = listOf("q1", "q2"),
            trend = PersonalTrend.UNKNOWN, isDistinctive = true
        )
        assertNull(WhoAmIUiModel.traitRow(trait).labelRes)
    }
}
