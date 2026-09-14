package com.whoareyou.app

import kotlin.math.abs
import kotlin.math.roundToInt

data class TraitCoverage(
    val traitId: String,
    val evidenceCount: Int,
    val confidence: Int,
    val contradictoryEvidenceCount: Int,
    val status: CoverageStatus
)

enum class CoverageStatus {
    UNKNOWN,
    LOW,
    DEVELOPING,
    STRONG
}

data class ProfileCoverage(
    val knownTraitCount: Int,
    val totalTraitCount: Int,
    val coveragePercent: Int,
    val averageConfidence: Int,
    val strongTraitCount: Int,
    val uncertainTraitCount: Int,
    val traits: List<TraitCoverage>
)

object ProfileCoverageEngine {
    fun build(catalog: List<Quiz>, graph: TraitGraph): ProfileCoverage {
        val allTraitIds = catalog
            .flatMap { quiz -> quiz.traits.map { it.id } }
            .distinct()
            .sorted()

        if (allTraitIds.isEmpty()) {
            return ProfileCoverage(
                knownTraitCount = 0,
                totalTraitCount = 0,
                coveragePercent = 0,
                averageConfidence = 0,
                strongTraitCount = 0,
                uncertainTraitCount = 0,
                traits = emptyList()
            )
        }

        val byId = graph.traits.associateBy { it.id }
        val traits = allTraitIds.map { traitId ->
            val trait = byId[traitId]
            if (trait == null) {
                TraitCoverage(
                    traitId = traitId,
                    evidenceCount = 0,
                    confidence = 0,
                    contradictoryEvidenceCount = 0,
                    status = CoverageStatus.UNKNOWN
                )
            } else {
                TraitCoverage(
                    traitId = traitId,
                    evidenceCount = trait.evidenceCount,
                    confidence = trait.confidence,
                    contradictoryEvidenceCount = trait.contradictoryEvidenceCount,
                    status = statusFor(trait)
                )
            }
        }

        val known = traits.count { it.status != CoverageStatus.UNKNOWN }
        val strong = traits.count { it.status == CoverageStatus.STRONG }
        val uncertain = traits.count {
            it.status == CoverageStatus.UNKNOWN ||
                it.status == CoverageStatus.LOW ||
                it.contradictoryEvidenceCount > 0
        }
        val avgConfidence = traits
            .filter { it.evidenceCount > 0 }
            .map { it.confidence }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.roundToInt()
            ?: 0

        return ProfileCoverage(
            knownTraitCount = known,
            totalTraitCount = traits.size,
            coveragePercent = ((known * 100f) / traits.size).roundToInt().coerceIn(0, 100),
            averageConfidence = avgConfidence.coerceIn(0, 100),
            strongTraitCount = strong,
            uncertainTraitCount = uncertain,
            traits = traits
        )
    }

    private fun statusFor(trait: ProfileTrait): CoverageStatus = when {
        trait.evidenceCount >= 2 &&
            trait.confidence >= 60 &&
            trait.contradictoryEvidenceCount == 0 -> CoverageStatus.STRONG
        trait.evidenceCount >= 2 && trait.confidence >= 35 -> CoverageStatus.DEVELOPING
        else -> CoverageStatus.LOW
    }
}

data class CoverageRecommendation(
    val quizId: String,
    val informationGainScore: Double,
    val targetedTraitIds: List<String>
)

object CoverageRecommendationEngine {
    fun recommend(
        catalog: List<Quiz>,
        completedQuizIds: Set<String>,
        coverage: ProfileCoverage
    ): CoverageRecommendation? {
        val coverageByTrait = coverage.traits.associateBy { it.traitId }

        return catalog
            .asSequence()
            .filter { it.id !in completedQuizIds }
            .mapNotNull { quiz ->
                if (quiz.traits.isEmpty()) return@mapNotNull null

                val scoredTraits = quiz.traits.map { mapping ->
                    val state = coverageByTrait[mapping.id]
                    val uncertainty = uncertaintyScore(state)
                    val weight = abs(mapping.weight)
                    Triple(mapping.id, uncertainty * weight, uncertainty)
                }

                val total = scoredTraits.sumOf { it.second }
                if (total <= 0.0) return@mapNotNull null

                CoverageRecommendation(
                    quizId = quiz.id,
                    informationGainScore = total,
                    targetedTraitIds = scoredTraits
                        .sortedByDescending { it.second }
                        .map { it.first }
                        .distinct()
                        .take(3)
                )
            }
            .maxWithOrNull(
                compareBy<CoverageRecommendation> { it.informationGainScore }
                    .thenByDescending { it.targetedTraitIds.size }
            )
    }

    private fun uncertaintyScore(state: TraitCoverage?): Double {
        if (state == null || state.status == CoverageStatus.UNKNOWN) return 1.0

        val confidenceGap = (100 - state.confidence).coerceIn(0, 100) / 100.0
        val evidenceGap = when (state.evidenceCount) {
            0 -> 1.0
            1 -> 0.65
            else -> 0.25
        }
        val contradictionBoost = if (state.contradictoryEvidenceCount > 0) 0.35 else 0.0

        return (confidenceGap * 0.55 + evidenceGap * 0.45 + contradictionBoost)
            .coerceIn(0.0, 1.35)
    }
}
