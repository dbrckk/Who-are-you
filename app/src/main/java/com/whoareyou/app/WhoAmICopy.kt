package com.whoareyou.app

import androidx.annotation.StringRes

object WhoAmICopy {
    @StringRes
    fun traitLabelResource(traitId: String): Int? = when (traitId) {
        "social_energy" -> R.string.trait_social_energy
        "emotional_orientation" -> R.string.trait_emotional_orientation
        "analytical_style" -> R.string.trait_analytical_style
        "rumination" -> R.string.trait_rumination
        "structure" -> R.string.trait_structure
        "adaptability" -> R.string.trait_adaptability
        "risk_tolerance" -> R.string.trait_risk_tolerance
        "opportunity_orientation" -> R.string.trait_opportunity_orientation
        "assertiveness" -> R.string.trait_assertiveness
        "deliberation" -> R.string.trait_deliberation
        "emotional_expression" -> R.string.trait_emotional_expression
        "emotional_openness" -> R.string.trait_emotional_openness
        "social_breadth" -> R.string.trait_social_breadth
        "circadian_lateness" -> R.string.trait_circadian_lateness
        "conflict_directness" -> R.string.trait_conflict_directness
        "curiosity" -> R.string.trait_curiosity
        "openness" -> R.string.trait_openness
        "novelty_seeking" -> R.string.trait_novelty_seeking
        "learning_depth" -> R.string.trait_learning_depth
        "planning" -> R.string.trait_planning
        "boundaries" -> R.string.trait_boundaries
        "competitiveness" -> R.string.trait_competitiveness
        "patience" -> R.string.trait_patience
        "independence" -> R.string.trait_independence
        "communication_directness" -> R.string.trait_communication_directness
        "trust_openness" -> R.string.trait_trust_openness
        "self_discipline" -> R.string.trait_self_discipline
        "optimism" -> R.string.trait_optimism
        "stress_mobilization" -> R.string.trait_stress_mobilization
        else -> null
    }
}
