package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class WhoAmICardModelTest {
    @Test
    fun `cards preserve portrait section order and cap dense trait lists`() {
        val traits = (1..6).map { i ->
            PersonalTrait(
                traitId = if (i == 1) "curiosity" else "trait_$i",
                score = 75, confidence = 70, certainty = PersonalCertainty.LIKELY,
                stability = PersonalStability.STABLE, contradictionLevel = ContradictionLevel.NONE,
                evidenceCount = 2, sourceQuizIds = listOf("q1", "q2"),
                trend = PersonalTrend.STABLE, isDistinctive = true
            )
        }
        val cards = WhoAmICardModel.from(
            WhoAmIPortrait(traits.take(3), traits, emptyList(), emptyList(), emptyList(), false)
        )
        assertEquals(WhoAmISection.PORTRAIT, cards.first().section)
        assertEquals(3, cards.first { it.section == WhoAmISection.STABLE }.traits.size)
    }

    @Test
    fun `discovery card exists for empty portrait without false conclusions`() {
        val cards = WhoAmICardModel.from(WhoAmIPortraitEngine.build(PersonalModel.EMPTY))
        assertEquals(listOf(WhoAmISection.PORTRAIT, WhoAmISection.DISCOVERY), cards.map { it.section })
    }
}
