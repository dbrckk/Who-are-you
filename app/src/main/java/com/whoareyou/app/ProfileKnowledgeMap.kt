package com.whoareyou.app

enum class TraitDomain {
    SOCIAL,
    EMOTIONAL,
    THINKING,
    GROWTH,
    SELF_MANAGEMENT
}

data class TraitDomainCoverage(
    val domain: TraitDomain,
    val knownCount: Int,
    val totalCount: Int,
    val strongCount: Int,
    val coveragePercent: Int
)

data class ProfileKnowledgeMap(
    val strong: List<TraitCoverage>,
    val developing: List<TraitCoverage>,
    val unknown: List<TraitCoverage>,
    val domains: List<TraitDomainCoverage>
)

object TraitDomainCatalog {
    fun domainFor(traitId: String): TraitDomain = when (traitId) {
        "social_energy", "social_breadth", "assertiveness", "conflict_directness",
        "communication_directness", "trust_openness", "boundaries" -> TraitDomain.SOCIAL

        "emotional_orientation", "emotional_expression", "emotional_openness",
        "rumination", "optimism", "stress_mobilization" -> TraitDomain.EMOTIONAL

        "analytical_style", "deliberation", "curiosity", "openness",
        "learning_depth", "novelty_seeking" -> TraitDomain.THINKING

        "adaptability", "risk_tolerance", "opportunity_orientation",
        "competitiveness", "independence" -> TraitDomain.GROWTH

        else -> TraitDomain.SELF_MANAGEMENT
    }
}

object ProfileKnowledgeMapEngine {
    fun build(coverage: ProfileCoverage): ProfileKnowledgeMap {
        val strong = coverage.traits
            .filter { it.status == CoverageStatus.STRONG }
            .sortedWith(compareByDescending<TraitCoverage> { it.confidence }.thenBy { it.traitId })

        val developing = coverage.traits
            .filter { it.status == CoverageStatus.LOW || it.status == CoverageStatus.DEVELOPING }
            .sortedWith(
                compareByDescending<TraitCoverage> { it.contradictoryEvidenceCount }
                    .thenBy { it.confidence }
                    .thenBy { it.traitId }
            )

        val unknown = coverage.traits
            .filter { it.status == CoverageStatus.UNKNOWN }
            .sortedBy { it.traitId }

        val domains = TraitDomain.entries.map { domain ->
            val traits = coverage.traits.filter { TraitDomainCatalog.domainFor(it.traitId) == domain }
            val known = traits.count { it.status != CoverageStatus.UNKNOWN }
            TraitDomainCoverage(
                domain = domain,
                knownCount = known,
                totalCount = traits.size,
                strongCount = traits.count { it.status == CoverageStatus.STRONG },
                coveragePercent = if (traits.isEmpty()) 0 else (known * 100 / traits.size).coerceIn(0, 100)
            )
        }.filter { it.totalCount > 0 }

        return ProfileKnowledgeMap(
            strong = strong,
            developing = developing,
            unknown = unknown,
            domains = domains
        )
    }
}
