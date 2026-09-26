package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhoAmILegacyCompatibilityTest {
    @Test
    fun `empty personal model stays a discovery portrait`() {
        val portrait = WhoAmIPortraitEngine.build(PersonalModel.EMPTY)

        assertTrue(portrait.isDiscoveryState)
        assertTrue(portrait.headlineTraits.isEmpty())
        assertTrue(portrait.stableTraits.isEmpty())
        assertTrue(portrait.evolvingTraits.isEmpty())
        assertEquals(
            listOf(WhoAmISection.PORTRAIT, WhoAmISection.DISCOVERY),
            WhoAmISectionModel.from(portrait).visibleSections
        )
    }

    @Test
    fun `single-source exploring evidence never becomes headline or stable`() {
        val exploring = PersonalTrait(
            traitId = "curiosity",
            score = 80,
            confidence = 40,
            certainty = PersonalCertainty.EXPLORING,
            stability = PersonalStability.UNKNOWN,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 1,
            sourceQuizIds = listOf("legacy"),
            trend = PersonalTrend.UNKNOWN,
            isDistinctive = false
        )
        val model = PersonalModel.EMPTY.copy(
            traits = listOf(exploring),
            exploringTraits = listOf(exploring)
        )

        val portrait = WhoAmIPortraitEngine.build(model)

        assertTrue(portrait.isDiscoveryState)
        assertTrue(portrait.headlineTraits.isEmpty())
        assertTrue(portrait.stableTraits.isEmpty())
        assertTrue(portrait.evolvingTraits.isEmpty())
    }

    @Test
    fun `unknown trait id requires generic user-facing fallback`() {
        val unknown = PersonalTrait(
            traitId = "internal_future_trait",
            score = 75,
            confidence = 70,
            certainty = PersonalCertainty.LIKELY,
            stability = PersonalStability.MODERATE,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 2,
            sourceQuizIds = listOf("q1", "q2"),
            trend = PersonalTrend.STABLE,
            isDistinctive = true
        )

        assertNull(WhoAmICopy.traitLabelResource(unknown.traitId))
        assertNull(WhoAmIUiModel.traitRow(unknown).labelRes)
    }
}
