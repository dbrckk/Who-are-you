package com.whoareyou.app

enum class WhoAmISection {
    PORTRAIT,
    STABLE,
    NUANCES,
    DISCOVERY,
    EVOLUTION
}

data class WhoAmISectionModel(
    val visibleSections: List<WhoAmISection>
) {
    companion object {
        fun from(portrait: WhoAmIPortrait): WhoAmISectionModel {
            val sections = buildList {
                add(WhoAmISection.PORTRAIT)
                if (portrait.stableTraits.isNotEmpty()) add(WhoAmISection.STABLE)
                if (portrait.nuancedTraits.isNotEmpty()) add(WhoAmISection.NUANCES)
                add(WhoAmISection.DISCOVERY)
                if (portrait.evolvingTraits.isNotEmpty()) add(WhoAmISection.EVOLUTION)
            }
            return WhoAmISectionModel(sections)
        }
    }
}
