package com.whoareyou.app

data class WhoAmIPortrait(
    val headlineTraits: List<PersonalTrait>,
    val stableTraits: List<PersonalTrait>,
    val nuancedTraits: List<PersonalTrait>,
    val discoveryGaps: List<TraitDomainCoverage>,
    val evolvingTraits: List<PersonalTrait>,
    val isDiscoveryState: Boolean
)

object WhoAmIPortraitEngine {
    private val portraitTraitComparator =
        compareByDescending<PersonalTrait> { it.certainty.ordinal }
            .thenByDescending { it.confidence }
            .thenByDescending { kotlin.math.abs(it.score - 50) }
            .thenBy { it.traitId }

    fun build(model: PersonalModel): WhoAmIPortrait {
        val headline = model.traits
            .filter {
                it.isDistinctive &&
                    it.certainty.ordinal >= PersonalCertainty.LIKELY.ordinal
            }
            .sortedWith(portraitTraitComparator)
            .take(3)

        val stable = model.traits
            .filter {
                it.stability == PersonalStability.STABLE &&
                    it.certainty.ordinal >= PersonalCertainty.LIKELY.ordinal
            }
            .sortedWith(portraitTraitComparator)

        val nuanced = (model.variableTraits + model.contradictoryTraits)
            .distinctBy { it.traitId }
            .sortedWith(portraitTraitComparator)

        val evolving = model.traits
            .filter {
                it.trend == PersonalTrend.RISING ||
                    it.trend == PersonalTrend.FALLING ||
                    it.trend == PersonalTrend.VARIABLE
            }
            .sortedWith(portraitTraitComparator)

        return WhoAmIPortrait(
            headlineTraits = headline,
            stableTraits = stable,
            nuancedTraits = nuanced,
            discoveryGaps = model.knowledgeGaps,
            evolvingTraits = evolving,
            isDiscoveryState = model.traits.isEmpty() || headline.isEmpty()
        )
    }
}
