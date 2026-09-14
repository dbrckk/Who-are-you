package com.whoareyou.app

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A transparent cross-quiz trait graph. Traits are derived only from completed
 * quiz dimensions and keep their evidence provenance; they are not diagnoses.
 */
data class TraitEvidence(
    val quizId: String,
    val quizTitle: String,
    val score: Int,
    val contribution: Int
)

data class ProfileTrait(
    val id: String,
    val label: String,
    val score: Int,
    val evidence: List<TraitEvidence>
) {
    val evidenceCount: Int get() = evidence.size
}

data class TraitGraph(
    val traits: List<ProfileTrait>,
    val evidenceCount: Int
)

private data class TraitRule(
    val id: String,
    val label: String,
    val tokens: Set<String>,
    val invert: Boolean = false
)

object TraitGraphEngine {
    private val rules = listOf(
        TraitRule("social_energy", "Social energy", setOf("social", "extrav", "introver", "sociab")),
        TraitRule("structure", "Structure", setOf("structure", "plan", "discipline", "organization", "organisation")),
        TraitRule("openness", "Openness", setOf("open", "curios", "creative", "novel", "imagin")),
        TraitRule("emotional_intensity", "Emotional intensity", setOf("emotion", "sensitive", "stress", "anx", "calm"), invert = false),
        TraitRule("assertiveness", "Assertiveness", setOf("assert", "leader", "confidence", "confiance", "decision")),
        TraitRule("empathy", "Empathy", setOf("empathy", "empath", "compassion", "warm", "chaleur")),
        TraitRule("independence", "Independence", setOf("independ", "autonom", "self-reli", "solitude")),
        TraitRule("adaptability", "Adaptability", setOf("adapt", "flexib", "spontan", "change", "changement"))
    )

    fun build(dimensions: List<ProfileDimension>): TraitGraph {
        if (dimensions.isEmpty()) return TraitGraph(emptyList(), 0)

        val evidenceByTrait = linkedMapOf<String, MutableList<TraitEvidence>>()
        val labels = rules.associate { it.id to it.label }

        dimensions.forEach { dimension ->
            val searchable = listOf(
                dimension.quizId,
                dimension.title,
                dimension.metricLabel,
                dimension.resultTitle
            ).joinToString(" ").lowercase()

            rules.forEach { rule ->
                if (rule.tokens.none(searchable::contains)) return@forEach
                val normalized = if (rule.invert) 100 - dimension.score else dimension.score
                val contribution = normalized.coerceIn(0, 100)
                evidenceByTrait.getOrPut(rule.id) { mutableListOf() }.add(
                    TraitEvidence(
                        quizId = dimension.quizId,
                        quizTitle = dimension.title,
                        score = dimension.score,
                        contribution = contribution
                    )
                )
            }
        }

        val traits = evidenceByTrait.mapNotNull { (traitId, evidence) ->
            if (evidence.isEmpty()) return@mapNotNull null
            val weighted = evidence.map { item ->
                val confidenceWeight = 0.5 + abs(item.score - 50) / 100.0
                item.contribution * confidenceWeight to confidenceWeight
            }
            val score = (
                weighted.sumOf { it.first } / weighted.sumOf { it.second }
            ).roundToInt().coerceIn(0, 100)
            ProfileTrait(
                id = traitId,
                label = labels.getValue(traitId),
                score = score,
                evidence = evidence.sortedByDescending { abs(it.score - 50) }
            )
        }.sortedByDescending { abs(it.score - 50) }

        return TraitGraph(
            traits = traits,
            evidenceCount = traits.sumOf { it.evidenceCount }
        )
    }
}
