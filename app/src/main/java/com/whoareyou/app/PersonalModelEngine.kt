package com.whoareyou.app

import kotlin.math.abs

object PersonalModelEngine {
    private val personalTraitComparator =
        compareByDescending<PersonalTrait> { it.certainty.ordinal }
            .thenByDescending { it.confidence }
            .thenByDescending { abs(it.score - 50) }
            .thenBy { it.traitId }

    fun build(
        graph: TraitGraph,
        coverage: ProfileCoverage,
        timelines: List<TraitTimeline> = emptyList(),
        knowledgeMap: ProfileKnowledgeMap = ProfileKnowledgeMap(
            strong = emptyList(),
            developing = emptyList(),
            unknown = emptyList(),
            domains = emptyList()
        )
    ): PersonalModel {
        val timelinesByTraitId = timelines.associateBy { it.traitId }

        val traits = graph.traits
            .map { trait ->
                val contradiction = contradictionLevel(trait)
                val timeline = timelinesByTraitId[trait.id]
                val stability = stabilityFor(timeline)
                val certainty = certaintyFor(
                    trait = trait,
                    contradiction = contradiction,
                    stability = stability
                )
                PersonalTrait(
                    traitId = trait.id,
                    score = trait.score,
                    confidence = trait.confidence,
                    certainty = certainty,
                    stability = stability,
                    contradictionLevel = contradiction,
                    evidenceCount = trait.evidenceCount,
                    sourceQuizIds = trait.evidence.map { it.quizId }.distinct().sorted(),
                    trend = trendFor(timeline),
                    isDistinctive = certainty.ordinal >= PersonalCertainty.LIKELY.ordinal &&
                        abs(trait.score - 50) >= 15
                )
            }
            .sortedWith(personalTraitComparator)

        return aggregate(traits, coverage, knowledgeMap)
    }

    private fun stabilityFor(timeline: TraitTimeline?): PersonalStability = when {
        timeline == null || timeline.points.size < 2 -> PersonalStability.UNKNOWN
        timeline.trend.kind == LongitudinalTrendKind.VOLATILE ||
            timeline.trend.kind == LongitudinalTrendKind.OUTLIER -> PersonalStability.VARIABLE
        timeline.trend.kind == LongitudinalTrendKind.STABLE &&
            timeline.points.size >= 3 -> PersonalStability.STABLE
        else -> PersonalStability.MODERATE
    }

    private fun trendFor(timeline: TraitTimeline?): PersonalTrend = when (timeline?.trend?.kind) {
        null, LongitudinalTrendKind.INSUFFICIENT -> PersonalTrend.UNKNOWN
        LongitudinalTrendKind.RISING -> PersonalTrend.RISING
        LongitudinalTrendKind.FALLING -> PersonalTrend.FALLING
        LongitudinalTrendKind.VOLATILE,
        LongitudinalTrendKind.OUTLIER -> PersonalTrend.VARIABLE
        LongitudinalTrendKind.STABLE -> PersonalTrend.STABLE
    }

    private fun certaintyFor(
        trait: ProfileTrait,
        contradiction: ContradictionLevel,
        stability: PersonalStability
    ): PersonalCertainty = when {
        trait.evidenceCount == 0 -> PersonalCertainty.UNKNOWN
        trait.evidenceCount == 1 -> PersonalCertainty.EXPLORING
        contradiction == ContradictionLevel.HIGH -> PersonalCertainty.EXPLORING
        trait.evidenceCount >= 3 &&
            trait.confidence >= 65 &&
            stability != PersonalStability.VARIABLE -> PersonalCertainty.ESTABLISHED
        trait.evidenceCount >= 2 && trait.confidence >= 45 ->
            PersonalCertainty.LIKELY
        else -> PersonalCertainty.EXPLORING
    }

    private fun contradictionLevel(trait: ProfileTrait): ContradictionLevel {
        if (trait.evidence.isEmpty() || trait.contradictoryEvidenceCount == 0) {
            return ContradictionLevel.NONE
        }

        val dominantHigh = trait.score >= 50
        val totalStrength = trait.evidence.sumOf {
            (it.signalStrength * abs(it.weight)).coerceAtLeast(0.0)
        }
        if (totalStrength <= 0.0) return ContradictionLevel.NONE

        val opposingStrength = trait.evidence
            .filter { (it.contribution >= 50) != dominantHigh }
            .sumOf { (it.signalStrength * abs(it.weight)).coerceAtLeast(0.0) }

        val ratio = opposingStrength / totalStrength
        return when {
            ratio >= 0.40 -> ContradictionLevel.HIGH
            ratio >= 0.25 -> ContradictionLevel.MODERATE
            ratio > 0.0 -> ContradictionLevel.LOW
            else -> ContradictionLevel.NONE
        }
    }

    private fun aggregate(
        traits: List<PersonalTrait>,
        coverage: ProfileCoverage,
        knowledgeMap: ProfileKnowledgeMap
    ): PersonalModel = PersonalModel(
        traits = traits,
        establishedTraits = traits
            .filter { it.certainty == PersonalCertainty.ESTABLISHED }
            .sortedWith(personalTraitComparator),
        likelyTraits = traits
            .filter { it.certainty == PersonalCertainty.LIKELY }
            .sortedWith(personalTraitComparator),
        exploringTraits = traits
            .filter { it.certainty == PersonalCertainty.EXPLORING }
            .sortedWith(personalTraitComparator),
        unknownTraitIds = coverage.traits
            .filter { it.status == CoverageStatus.UNKNOWN }
            .map { it.traitId }
            .sorted(),
        stableTraits = traits
            .filter { it.stability == PersonalStability.STABLE }
            .sortedWith(personalTraitComparator),
        variableTraits = traits
            .filter { it.stability == PersonalStability.VARIABLE }
            .sortedWith(personalTraitComparator),
        contradictoryTraits = traits
            .filter { it.contradictionLevel.ordinal >= ContradictionLevel.MODERATE.ordinal }
            .sortedWith(personalTraitComparator),
        strongestKnowledgeDomains = knowledgeMap.domains
            .sortedWith(
                compareByDescending<TraitDomainCoverage> { it.coveragePercent }
                    .thenBy { it.domain.name }
            )
            .take(3),
        knowledgeGaps = knowledgeMap.domains
            .sortedWith(
                compareBy<TraitDomainCoverage> { it.coveragePercent }
                    .thenBy { it.domain.name }
            )
            .take(3)
    )
}
