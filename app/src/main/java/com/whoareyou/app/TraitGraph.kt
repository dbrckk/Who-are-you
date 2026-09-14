package com.whoareyou.app

import kotlin.math.abs
import kotlin.math.roundToInt

data class TraitEvidence(
    val quizId: String,
    val quizTitle: String,
    val sourceScore: Int,
    val weight: Double,
    val contribution: Int,
    val signalStrength: Double
)

data class ProfileTrait(
    val id: String,
    val score: Int,
    val confidence: Int,
    val evidence: List<TraitEvidence>,
    val contradictoryEvidenceCount: Int
) {
    val evidenceCount: Int get() = evidence.size
}

data class TraitGraph(
    val traits: List<ProfileTrait>,
    val evidenceCount: Int,
    val taxonomyVersion: Int = 2
)

object TraitGraphEngine {
    fun build(dimensions: List<ProfileDimension>): TraitGraph {
        if (dimensions.isEmpty()) return TraitGraph(emptyList(), 0)

        val grouped = linkedMapOf<String, MutableList<TraitEvidence>>()
        dimensions.forEach { dimension ->
            dimension.traitWeights.forEach { mapping ->
                val centered = dimension.score - 50
                val contribution = (
                    50.0 + centered * mapping.weight
                ).roundToInt().coerceIn(0, 100)
                val strength = (abs(centered) / 50.0 * abs(mapping.weight)).coerceIn(0.0, 1.0)
                grouped.getOrPut(mapping.id) { mutableListOf() }.add(
                    TraitEvidence(
                        quizId = dimension.quizId,
                        quizTitle = dimension.title,
                        sourceScore = dimension.score,
                        weight = mapping.weight,
                        contribution = contribution,
                        signalStrength = strength
                    )
                )
            }
        }

        val traits = grouped.mapNotNull { (traitId, evidence) ->
            if (evidence.isEmpty()) return@mapNotNull null
            val totalWeight = evidence.sumOf { (0.25 + it.signalStrength) * abs(it.weight) }
            if (totalWeight <= 0.0) return@mapNotNull null
            val weightedScore = evidence.sumOf {
                it.contribution * (0.25 + it.signalStrength) * abs(it.weight)
            } / totalWeight
            val score = weightedScore.roundToInt().coerceIn(0, 100)
            val confidence = (
                evidence.sumOf { it.signalStrength } / evidence.size * 100.0
            ).roundToInt().coerceIn(0, 100)
            val dominantSide = score >= 50
            val contradictions = evidence.count { (it.contribution >= 50) != dominantSide }
            ProfileTrait(
                id = traitId,
                score = score,
                confidence = confidence,
                evidence = evidence.sortedByDescending { it.signalStrength },
                contradictoryEvidenceCount = contradictions
            )
        }.sortedByDescending { abs(it.score - 50) * (0.5 + it.confidence / 100.0) }

        return TraitGraph(
            traits = traits,
            evidenceCount = traits.sumOf { it.evidenceCount }
        )
    }
}
