package com.whoareyou.app

enum class SignatureProfileKey {
    INDEPENDENT_EXPLORER,
    STRUCTURED_BUILDER,
    OPEN_CONNECTOR,
    ADAPTIVE_DIPLOMAT,
    DRIVEN_CHALLENGER,
    CALM_STRATEGIST
}

data class SignatureProfileMatch(
    val key: SignatureProfileKey,
    val confidence: Int,
    val supportingQuizIds: List<String>
)

object SignatureProfiles {
    private const val MIN_COMPLETED_DIMENSIONS = 8

    private data class Requirement(
        val quizId: String,
        val min: Int? = null,
        val max: Int? = null,
        val weight: Int = 1
    )

    private data class Rule(
        val key: SignatureProfileKey,
        val requirements: List<Requirement>,
        val contradictions: List<Requirement> = emptyList()
    )

    private val rules = listOf(
        Rule(
            key = SignatureProfileKey.INDEPENDENT_EXPLORER,
            requirements = listOf(
                Requirement("learning_drive", min = 65, weight = 2),
                Requirement("novelty_seeker", min = 65, weight = 2),
                Requirement("independence", min = 65, weight = 2)
            ),
            contradictions = listOf(
                Requirement("novelty_seeker", max = 34),
                Requirement("independence", max = 34)
            )
        ),
        Rule(
            key = SignatureProfileKey.STRUCTURED_BUILDER,
            requirements = listOf(
                Requirement("planning_style", min = 65, weight = 2),
                Requirement("self_discipline", min = 65, weight = 2),
                Requirement("communication_style", min = 55)
            ),
            contradictions = listOf(
                Requirement("planning_style", max = 34),
                Requirement("self_discipline", max = 34)
            )
        ),
        Rule(
            key = SignatureProfileKey.OPEN_CONNECTOR,
            requirements = listOf(
                Requirement("emotional_openness", min = 65, weight = 2),
                Requirement("trust_style", min = 60, weight = 2),
                Requirement("communication_style", min = 55)
            ),
            contradictions = listOf(
                Requirement("emotional_openness", max = 34),
                Requirement("trust_style", max = 34)
            )
        ),
        Rule(
            key = SignatureProfileKey.ADAPTIVE_DIPLOMAT,
            requirements = listOf(
                Requirement("adaptability", min = 65, weight = 2),
                Requirement("patience", min = 65, weight = 2),
                Requirement("assertiveness", min = 40, max = 75)
            ),
            contradictions = listOf(
                Requirement("adaptability", max = 34),
                Requirement("patience", max = 34),
                Requirement("assertiveness", min = 90)
            )
        ),
        Rule(
            key = SignatureProfileKey.DRIVEN_CHALLENGER,
            requirements = listOf(
                Requirement("assertiveness", min = 65, weight = 2),
                Requirement("competitiveness", min = 65, weight = 2),
                Requirement("stress_response", min = 60)
            ),
            contradictions = listOf(
                Requirement("assertiveness", max = 34),
                Requirement("competitiveness", max = 34)
            )
        ),
        Rule(
            key = SignatureProfileKey.CALM_STRATEGIST,
            requirements = listOf(
                Requirement("patience", min = 65, weight = 2),
                Requirement("self_discipline", min = 65, weight = 2),
                Requirement("stress_response", max = 45, weight = 2)
            ),
            contradictions = listOf(
                Requirement("patience", max = 34),
                Requirement("self_discipline", max = 34),
                Requirement("stress_response", min = 80)
            )
        )
    )

    fun primary(scores: Map<String, Int>): SignatureProfileMatch? {
        if (scores.size < MIN_COMPLETED_DIMENSIONS) return null
        return matches(scores).maxWithOrNull(
            compareBy<SignatureProfileMatch> { it.confidence }
                .thenByDescending { -rules.indexOfFirst { rule -> rule.key == it.key } }
        )
    }

    fun matches(scores: Map<String, Int>): List<SignatureProfileMatch> {
        if (scores.size < MIN_COMPLETED_DIMENSIONS) return emptyList()
        return rules.mapNotNull { rule ->
            if (rule.requirements.any { scores[it.quizId] == null }) return@mapNotNull null
            if (rule.contradictions.any { requirement ->
                    scores[requirement.quizId]?.let { score -> satisfies(score, requirement) } == true
                }) return@mapNotNull null
            if (rule.requirements.any { requirement -> !satisfies(scores.getValue(requirement.quizId), requirement) }) {
                return@mapNotNull null
            }

            val weighted = rule.requirements.sumOf { requirement ->
                val score = scores.getValue(requirement.quizId).coerceIn(0, 100)
                val strength = requirementStrength(score, requirement)
                strength * requirement.weight
            }
            val totalWeight = rule.requirements.sumOf { it.weight }
            val confidence = (weighted / totalWeight).coerceIn(0, 100)
            SignatureProfileMatch(
                key = rule.key,
                confidence = confidence,
                supportingQuizIds = rule.requirements.map { it.quizId }
            )
        }
    }

    private fun satisfies(rawScore: Int, requirement: Requirement): Boolean {
        val score = rawScore.coerceIn(0, 100)
        return (requirement.min == null || score >= requirement.min) &&
            (requirement.max == null || score <= requirement.max)
    }

    private fun requirementStrength(score: Int, requirement: Requirement): Int = when {
        requirement.min != null && requirement.max != null -> {
            val center = (requirement.min + requirement.max) / 2f
            val halfRange = ((requirement.max - requirement.min) / 2f).coerceAtLeast(1f)
            (100f - (kotlin.math.abs(score - center) / halfRange * 35f)).toInt().coerceIn(65, 100)
        }
        requirement.min != null -> (65 + ((score - requirement.min).coerceAtLeast(0) * 35 / (100 - requirement.min).coerceAtLeast(1))).coerceIn(65, 100)
        requirement.max != null -> (65 + ((requirement.max - score).coerceAtLeast(0) * 35 / requirement.max.coerceAtLeast(1))).coerceIn(65, 100)
        else -> 100
    }
}
