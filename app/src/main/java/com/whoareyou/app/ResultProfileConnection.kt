package com.whoareyou.app

enum class ResultProfileConnectionKind {
    REINFORCING,
    CONTRASTING,
    CONTEXTUAL
}

data class ResultProfileConnection(
    val traitId: String,
    val kind: ResultProfileConnectionKind,
    val traitScore: Int,
    val confidence: Int
)

object ResultProfileConnectionEngine {
    private const val MIN_CONFIDENCE = 35

    fun derive(
        quiz: Quiz,
        score: Int,
        graph: TraitGraph
    ): List<ResultProfileConnection> {
        if (quiz.traits.isEmpty()) return emptyList()

        val centeredResult = score.coerceIn(0, 100) - 50
        if (centeredResult == 0) return emptyList()

        val traitsById = graph.traits.associateBy { it.id }

        return quiz.traits
            .mapNotNull { mapping ->
                val trait = traitsById[mapping.id] ?: return@mapNotNull null
                if (trait.confidence < MIN_CONFIDENCE) return@mapNotNull null
                if (trait.evidenceCount == 0 && graph.evidenceCount < 2) return@mapNotNull null

                val currentDirection = centeredResult * mapping.weight
                val profileDirection = trait.score - 50
                val kind = when {
                    currentDirection == 0.0 || profileDirection == 0 ->
                        ResultProfileConnectionKind.CONTEXTUAL
                    (currentDirection > 0) == (profileDirection > 0) ->
                        ResultProfileConnectionKind.REINFORCING
                    else ->
                        ResultProfileConnectionKind.CONTRASTING
                }

                ResultProfileConnection(
                    traitId = mapping.id,
                    kind = kind,
                    traitScore = trait.score,
                    confidence = trait.confidence
                )
            }
            .sortedWith(
                compareByDescending<ResultProfileConnection> { it.confidence }
                    .thenBy { it.traitId }
            )
            .take(3)
    }
}
