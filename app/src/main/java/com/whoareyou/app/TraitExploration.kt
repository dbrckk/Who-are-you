package com.whoareyou.app

import kotlin.math.abs

data class TraitExploration(
    val trait: TraitCoverage,
    val evidence: List<TraitEvidence>,
    val recommendedQuizId: String?,
    val recommendationGain: Double
)

object TraitExplorationEngine {
    fun build(
        traitId: String,
        coverage: ProfileCoverage,
        graph: TraitGraph,
        catalog: List<Quiz>,
        completedQuizIds: Set<String>
    ): TraitExploration? {
        val trait = coverage.traits.firstOrNull { it.traitId == traitId } ?: return null
        val evidence = graph.traits
            .firstOrNull { it.id == traitId }
            ?.evidence
            .orEmpty()
            .sortedByDescending { it.signalStrength }

        val recommendation = catalog
            .asSequence()
            .filter { it.id !in completedQuizIds }
            .mapNotNull { quiz ->
                val mapping = quiz.traits.firstOrNull { it.id == traitId } ?: return@mapNotNull null
                quiz.id to abs(mapping.weight)
            }
            .maxByOrNull { it.second }

        return TraitExploration(
            trait = trait,
            evidence = evidence,
            recommendedQuizId = recommendation?.first,
            recommendationGain = recommendation?.second ?: 0.0
        )
    }
}
