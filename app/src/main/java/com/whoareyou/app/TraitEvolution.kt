package com.whoareyou.app

import kotlin.math.abs

enum class TraitEvolutionKind {
    NEW_EVIDENCE,
    MOVED,
    STABLE,
    LOW_CONFIDENCE,
    CONTRADICTORY
}

data class TraitEvolution(
    val traitId: String,
    val previousScore: Int?,
    val currentScore: Int,
    val delta: Int?,
    val previousConfidence: Int?,
    val currentConfidence: Int,
    val previousEvidenceCount: Int,
    val currentEvidenceCount: Int,
    val kind: TraitEvolutionKind,
    val newEvidenceQuizIds: List<String> = emptyList()
)

data class TraitEvolutionSummary(
    val traits: List<TraitEvolution>,
    val meaningfulChanges: List<TraitEvolution>,
    val newEvidence: List<TraitEvolution>
)

object TraitEvolutionEngine {
    private const val MEANINGFUL_DELTA = 8
    private const val LOW_CONFIDENCE = 35

    fun build(
        catalog: List<Quiz>,
        latestScores: Map<String, Int>,
        scoreHistory: Map<String, List<Int>>
    ): TraitEvolutionSummary {
        val currentDimensions = dimensionsFromScores(catalog, latestScores)
        val currentGraph = TraitGraphEngine.build(currentDimensions)

        val priorScores = buildMap<String, Int> {
            catalog.forEach { quiz ->
                val history = scoreHistory[quiz.id].orEmpty()
                when {
                    history.size >= 2 -> put(quiz.id, history[history.lastIndex - 1])
                    history.size == 1 -> Unit
                    quiz.id in latestScores -> Unit
                }
            }
        }
        val priorGraph = TraitGraphEngine.build(dimensionsFromScores(catalog, priorScores))
        val priorById = priorGraph.traits.associateBy { it.id }

        val traits = currentGraph.traits.map { current ->
            val previous = priorById[current.id]
            val delta = previous?.let { current.score - it.score }
            val previousEvidenceIds = previous?.evidence?.mapTo(mutableSetOf()) { it.quizId }.orEmpty()
            val newEvidenceQuizIds = current.evidence
                .map { it.quizId }
                .filterNot { it in previousEvidenceIds }
                .distinct()

            val kind = when {
                current.confidence < LOW_CONFIDENCE -> TraitEvolutionKind.LOW_CONFIDENCE
                current.contradictoryEvidenceCount > 0 -> TraitEvolutionKind.CONTRADICTORY
                previous == null || current.evidenceCount > previous.evidenceCount ->
                    TraitEvolutionKind.NEW_EVIDENCE
                delta != null && abs(delta) >= MEANINGFUL_DELTA ->
                    TraitEvolutionKind.MOVED
                else -> TraitEvolutionKind.STABLE
            }

            TraitEvolution(
                traitId = current.id,
                previousScore = previous?.score,
                currentScore = current.score,
                delta = delta,
                previousConfidence = previous?.confidence,
                currentConfidence = current.confidence,
                previousEvidenceCount = previous?.evidenceCount ?: 0,
                currentEvidenceCount = current.evidenceCount,
                kind = kind,
                newEvidenceQuizIds = newEvidenceQuizIds
            )
        }

        return TraitEvolutionSummary(
            traits = traits,
            meaningfulChanges = traits
                .filter { it.kind == TraitEvolutionKind.MOVED }
                .sortedByDescending { abs(it.delta ?: 0) },
            newEvidence = traits
                .filter { it.kind == TraitEvolutionKind.NEW_EVIDENCE }
                .sortedByDescending { it.currentConfidence }
        )
    }

    private fun dimensionsFromScores(
        catalog: List<Quiz>,
        scores: Map<String, Int>
    ): List<ProfileDimension> = catalog.mapNotNull { quiz ->
        scores[quiz.id]?.let { rawScore ->
            val score = rawScore.coerceIn(0, 100)
            ProfileDimension(
                quizId = quiz.id,
                title = quiz.title,
                score = score,
                resultTitle = quiz.resultTitleFor(score),
                metricLabel = if (score >= 50) quiz.metricHigh else quiz.metricLow,
                traitWeights = quiz.traits
            )
        }
    }
}
