package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhoAmIPortraitTest {
    @Test
    fun `distinctive likely and established traits become capped headlines`() {
        val model = personalModel(
            traits = listOf(
                trait("a", 90, PersonalCertainty.ESTABLISHED, distinctive = true),
                trait("b", 10, PersonalCertainty.LIKELY, distinctive = true),
                trait("c", 85, PersonalCertainty.LIKELY, distinctive = true),
                trait("d", 80, PersonalCertainty.LIKELY, distinctive = true)
            )
        )

        val portrait = WhoAmIPortraitEngine.build(model)

        assertEquals(listOf("a", "b", "c"), portrait.headlineTraits.map { it.traitId })
    }

    @Test
    fun `exploring traits never become headline conclusions`() {
        val model = personalModel(
            traits = listOf(
                trait("curiosity", 90, PersonalCertainty.EXPLORING, distinctive = true)
            )
        )

        assertTrue(WhoAmIPortraitEngine.build(model).headlineTraits.isEmpty())
    }

    @Test
    fun `stable section requires stable history and likely certainty`() {
        val likelyStable = trait(
            "stable",
            75,
            PersonalCertainty.LIKELY,
            stability = PersonalStability.STABLE
        )
        val exploringStable = trait(
            "exploring",
            75,
            PersonalCertainty.EXPLORING,
            stability = PersonalStability.STABLE
        )
        val likelyVariable = trait(
            "variable",
            75,
            PersonalCertainty.LIKELY,
            stability = PersonalStability.VARIABLE
        )

        val portrait = WhoAmIPortraitEngine.build(
            personalModel(traits = listOf(likelyStable, exploringStable, likelyVariable))
        )

        assertEquals(listOf("stable"), portrait.stableTraits.map { it.traitId })
    }

    @Test
    fun `nuances deduplicate contradictory and variable traits`() {
        val both = trait(
            "both",
            70,
            PersonalCertainty.LIKELY,
            stability = PersonalStability.VARIABLE,
            contradiction = ContradictionLevel.MODERATE
        )
        val model = personalModel(
            traits = listOf(both),
            variableTraits = listOf(both),
            contradictoryTraits = listOf(both)
        )

        assertEquals(
            listOf("both"),
            WhoAmIPortraitEngine.build(model).nuancedTraits.map { it.traitId }
        )
    }

    @Test
    fun `empty model creates discovery state without conclusions`() {
        val portrait = WhoAmIPortraitEngine.build(PersonalModel.EMPTY)

        assertTrue(portrait.headlineTraits.isEmpty())
        assertTrue(portrait.stableTraits.isEmpty())
        assertTrue(portrait.nuancedTraits.isEmpty())
        assertTrue(portrait.evolvingTraits.isEmpty())
        assertTrue(portrait.isDiscoveryState)
    }

    private fun trait(
        id: String,
        score: Int,
        certainty: PersonalCertainty,
        stability: PersonalStability = PersonalStability.MODERATE,
        contradiction: ContradictionLevel = ContradictionLevel.NONE,
        distinctive: Boolean = false,
        confidence: Int = 70,
        trend: PersonalTrend = PersonalTrend.STABLE
    ) = PersonalTrait(
        traitId = id,
        score = score,
        confidence = confidence,
        certainty = certainty,
        stability = stability,
        contradictionLevel = contradiction,
        evidenceCount = 3,
        sourceQuizIds = listOf("quiz-a", "quiz-b", "quiz-c"),
        trend = trend,
        isDistinctive = distinctive
    )

    private fun personalModel(
        traits: List<PersonalTrait> = emptyList(),
        variableTraits: List<PersonalTrait> = emptyList(),
        contradictoryTraits: List<PersonalTrait> = emptyList(),
        knowledgeGaps: List<TraitDomainCoverage> = emptyList()
    ) = PersonalModel(
        traits = traits,
        establishedTraits = traits.filter { it.certainty == PersonalCertainty.ESTABLISHED },
        likelyTraits = traits.filter { it.certainty == PersonalCertainty.LIKELY },
        exploringTraits = traits.filter { it.certainty == PersonalCertainty.EXPLORING },
        unknownTraitIds = emptyList(),
        stableTraits = traits.filter { it.stability == PersonalStability.STABLE },
        variableTraits = variableTraits,
        contradictoryTraits = contradictoryTraits,
        strongestKnowledgeDomains = emptyList(),
        knowledgeGaps = knowledgeGaps
    )
}
