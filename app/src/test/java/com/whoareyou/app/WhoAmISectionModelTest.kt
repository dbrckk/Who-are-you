package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhoAmISectionModelTest {
    @Test
    fun `portrait sections expose five-part hierarchy when evidence supports it`() {
        val model = WhoAmISectionModel.from(
            WhoAmIPortrait(
                headlineTraits = listOf(trait("curiosity", PersonalStability.STABLE, PersonalTrend.STABLE)),
                stableTraits = listOf(trait("curiosity", PersonalStability.STABLE, PersonalTrend.STABLE)),
                nuancedTraits = listOf(trait("adaptability", PersonalStability.VARIABLE, PersonalTrend.VARIABLE)),
                discoveryGaps = listOf(TraitDomainCoverage(TraitDomain.SOCIAL, 1, 3, 0, 33)),
                evolvingTraits = listOf(trait("adaptability", PersonalStability.VARIABLE, PersonalTrend.VARIABLE)),
                isDiscoveryState = false
            )
        )
        assertEquals(listOf(
            WhoAmISection.PORTRAIT, WhoAmISection.STABLE, WhoAmISection.NUANCES,
            WhoAmISection.DISCOVERY, WhoAmISection.EVOLUTION
        ), model.visibleSections)
    }

    @Test
    fun `sparse portrait hides false stable and evolution conclusions`() {
        val model = WhoAmISectionModel.from(PersonalModel.EMPTY.let(WhoAmIPortraitEngine::build))
        assertTrue(model.visibleSections.contains(WhoAmISection.PORTRAIT))
        assertTrue(model.visibleSections.contains(WhoAmISection.DISCOVERY))
        assertFalse(model.visibleSections.contains(WhoAmISection.STABLE))
        assertFalse(model.visibleSections.contains(WhoAmISection.EVOLUTION))
    }

    private fun trait(id: String, stability: PersonalStability, trend: PersonalTrend) = PersonalTrait(
        traitId = id, score = 75, confidence = 70, certainty = PersonalCertainty.LIKELY,
        stability = stability, contradictionLevel = ContradictionLevel.NONE,
        evidenceCount = 2, sourceQuizIds = listOf("q1", "q2"), trend = trend, isDistinctive = true
    )
}
