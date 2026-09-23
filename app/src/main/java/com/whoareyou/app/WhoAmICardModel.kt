package com.whoareyou.app

data class WhoAmICard(
    val section: WhoAmISection,
    val traits: List<PersonalTrait> = emptyList(),
    val discoveryGaps: List<TraitDomainCoverage> = emptyList()
)

object WhoAmICardModel {
    private const val MAX_TRAITS_PER_CARD = 3

    fun from(portrait: WhoAmIPortrait): List<WhoAmICard> = buildList {
        add(WhoAmICard(WhoAmISection.PORTRAIT, portrait.headlineTraits.take(MAX_TRAITS_PER_CARD)))
        if (portrait.stableTraits.isNotEmpty()) {
            add(WhoAmICard(WhoAmISection.STABLE, portrait.stableTraits.take(MAX_TRAITS_PER_CARD)))
        }
        if (portrait.nuancedTraits.isNotEmpty()) {
            add(WhoAmICard(WhoAmISection.NUANCES, portrait.nuancedTraits.take(MAX_TRAITS_PER_CARD)))
        }
        add(WhoAmICard(WhoAmISection.DISCOVERY, discoveryGaps = portrait.discoveryGaps))
        if (portrait.evolvingTraits.isNotEmpty()) {
            add(WhoAmICard(WhoAmISection.EVOLUTION, portrait.evolvingTraits.take(MAX_TRAITS_PER_CARD)))
        }
    }
}
