package com.whoareyou.app

data class TimedTraitSnapshot(
    val epochDay: Long,
    val score: Int,
    val confidence: Int,
    val evidenceCount: Int,
    val contradictoryEvidenceCount: Int,
    val changedQuizIds: List<String>,
    val newEvidenceQuizIds: List<String>,
    val retakeQuizIds: List<String>
)

data class TraitTimelinePeriodComparison(
    val earlierScoreAverage: Int,
    val recentScoreAverage: Int,
    val scoreDelta: Int,
    val earlierConfidenceAverage: Int,
    val recentConfidenceAverage: Int,
    val confidenceDelta: Int
)

data class TraitTimeline(
    val traitId: String,
    val points: List<TimedTraitSnapshot>,
    val trend: LongitudinalTrend,
    val periodComparison: TraitTimelinePeriodComparison?
)

object TraitTimelineEngine {
    fun build(
        catalog: List<Quiz>,
        timedScoreHistory: Map<String, List<TimedScore>>
    ): List<TraitTimeline> {
        if (catalog.isEmpty() || timedScoreHistory.isEmpty()) return emptyList()

        val catalogById = catalog.associateBy { it.id }
        val baselineScores = mutableMapOf<String, Int>()
        val datedEvents = mutableMapOf<Long, MutableMap<String, Int>>()

        timedScoreHistory.forEach { (quizId, rawPoints) ->
            if (quizId !in catalogById) return@forEach
            rawPoints
                .takeLast(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ)
                .forEach { point ->
                    if (point.epochDay <= 0L) {
                        baselineScores[quizId] = point.score.coerceIn(0, 100)
                    } else {
                        datedEvents
                            .getOrPut(point.epochDay) { linkedMapOf() }[quizId] =
                            point.score.coerceIn(0, 100)
                    }
                }
        }

        if (datedEvents.isEmpty()) return emptyList()

        val currentScores = baselineScores.toMutableMap()
        val seenQuizIds = baselineScores.keys.toMutableSet()
        val timelines = linkedMapOf<String, MutableList<TimedTraitSnapshot>>()

        datedEvents.toSortedMap().forEach dayLoop@ { (epochDay, changes) ->
            val wasKnown = changes.keys.associateWith { it in seenQuizIds }
            changes.forEach { (quizId, score) ->
                currentScores[quizId] = score
                seenQuizIds += quizId
            }

            val graph = TraitGraphEngine.build(
                dimensionsFromScores(catalog, currentScores)
            )
            val changedQuizIds = changes.keys.toSet()

            graph.traits.forEach traitLoop@ { trait ->
                val affectedQuizIds = trait.evidence
                    .map { it.quizId }
                    .filter { it in changedQuizIds }
                    .distinct()

                if (affectedQuizIds.isEmpty()) return@traitLoop

                val newEvidence = affectedQuizIds.filter { wasKnown[it] == false }
                val retakes = affectedQuizIds.filter { wasKnown[it] == true }

                timelines.getOrPut(trait.id) { mutableListOf() }.add(
                    TimedTraitSnapshot(
                        epochDay = epochDay,
                        score = trait.score,
                        confidence = trait.confidence,
                        evidenceCount = trait.evidenceCount,
                        contradictoryEvidenceCount = trait.contradictoryEvidenceCount,
                        changedQuizIds = affectedQuizIds,
                        newEvidenceQuizIds = newEvidence,
                        retakeQuizIds = retakes
                    )
                )
            }
        }

        return timelines.map { (traitId, points) ->
            TraitTimeline(
                traitId = traitId,
                points = points,
                trend = LongitudinalTrendEngine.build(
                    quizId = "trait:$traitId",
                    points = points.map { TimedScore(it.score, it.epochDay) }
                ),
                periodComparison = buildPeriodComparison(points)
            )
        }.sortedWith(
            compareByDescending<TraitTimeline> { it.points.size }
                .thenByDescending { kotlin.math.abs(it.trend.netChange) }
                .thenBy { it.traitId }
        )
    }

    private fun buildPeriodComparison(
        points: List<TimedTraitSnapshot>
    ): TraitTimelinePeriodComparison? {
        if (points.size < 2) return null
        val split = (points.size / 2).coerceAtLeast(1)
        val earlier = points.take(split)
        val recent = points.drop(split)
        if (recent.isEmpty()) return null

        val earlierScore = earlier.map { it.score }.average().toInt()
        val recentScore = recent.map { it.score }.average().toInt()
        val earlierConfidence = earlier.map { it.confidence }.average().toInt()
        val recentConfidence = recent.map { it.confidence }.average().toInt()

        return TraitTimelinePeriodComparison(
            earlierScoreAverage = earlierScore,
            recentScoreAverage = recentScore,
            scoreDelta = recentScore - earlierScore,
            earlierConfidenceAverage = earlierConfidence,
            recentConfidenceAverage = recentConfidence,
            confidenceDelta = recentConfidence - earlierConfidence
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
