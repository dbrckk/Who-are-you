package com.whoareyou.app

import kotlin.math.abs

enum class ProfileNarrativeKind {
    REPEATED_MOVEMENT,
    STABLE_WITH_MORE_EVIDENCE,
    BETTER_DOCUMENTED,
    VOLATILE,
    CONTRADICTORY,
    LOW_CONFIDENCE
}

data class ProfileNarrativeInsight(
    val traitId: String,
    val kind: ProfileNarrativeKind,
    val scoreDelta: Int,
    val confidenceDelta: Int,
    val currentScore: Int,
    val currentConfidence: Int,
    val evidenceCount: Int,
    val sourceQuizIds: List<String>,
    val priority: Int
)

data class ProfileNarrativeSummary(
    val insights: List<ProfileNarrativeInsight>
)

object ProfileNarrativeEngine {
    private const val MEANINGFUL_SCORE_DELTA = 8
    private const val MEANINGFUL_CONFIDENCE_DELTA = 12
    private const val LOW_CONFIDENCE = 35

    fun build(
        graph: TraitGraph,
        timelines: List<TraitTimeline>
    ): ProfileNarrativeSummary {
        val graphById = graph.traits.associateBy { it.id }

        val insights = timelines.mapNotNull { timeline ->
            val current = graphById[timeline.traitId] ?: return@mapNotNull null
            val comparison = timeline.periodComparison
            val scoreDelta = comparison?.scoreDelta ?: 0
            val confidenceDelta = comparison?.confidenceDelta ?: 0
            val repeated = timeline.points.size >= 3
            val sourceIds = current.evidence.map { it.quizId }.distinct()

            val kind = when {
                current.contradictoryEvidenceCount > 0 ->
                    ProfileNarrativeKind.CONTRADICTORY
                current.confidence < LOW_CONFIDENCE ->
                    ProfileNarrativeKind.LOW_CONFIDENCE
                timeline.trend.kind == LongitudinalTrendKind.VOLATILE ||
                    timeline.trend.kind == LongitudinalTrendKind.OUTLIER ->
                    ProfileNarrativeKind.VOLATILE
                repeated && comparison != null &&
                    abs(scoreDelta) >= MEANINGFUL_SCORE_DELTA ->
                    ProfileNarrativeKind.REPEATED_MOVEMENT
                comparison != null &&
                    abs(scoreDelta) < MEANINGFUL_SCORE_DELTA &&
                    confidenceDelta >= MEANINGFUL_CONFIDENCE_DELTA ->
                    ProfileNarrativeKind.STABLE_WITH_MORE_EVIDENCE
                confidenceDelta >= MEANINGFUL_CONFIDENCE_DELTA ||
                    timeline.points.any { it.newEvidenceQuizIds.isNotEmpty() } ->
                    ProfileNarrativeKind.BETTER_DOCUMENTED
                else -> return@mapNotNull null
            }

            val priority = when (kind) {
                ProfileNarrativeKind.REPEATED_MOVEMENT ->
                    500 + abs(scoreDelta) * 4 + current.confidence
                ProfileNarrativeKind.STABLE_WITH_MORE_EVIDENCE ->
                    420 + confidenceDelta * 3 + current.evidenceCount * 10
                ProfileNarrativeKind.BETTER_DOCUMENTED ->
                    350 + confidenceDelta * 2 + current.evidenceCount * 10
                ProfileNarrativeKind.VOLATILE ->
                    300 + timeline.points.size * 10
                ProfileNarrativeKind.CONTRADICTORY ->
                    250 + current.contradictoryEvidenceCount * 20
                ProfileNarrativeKind.LOW_CONFIDENCE ->
                    150 + timeline.points.size * 5
            }

            ProfileNarrativeInsight(
                traitId = timeline.traitId,
                kind = kind,
                scoreDelta = scoreDelta,
                confidenceDelta = confidenceDelta,
                currentScore = current.score,
                currentConfidence = current.confidence,
                evidenceCount = current.evidenceCount,
                sourceQuizIds = sourceIds,
                priority = priority
            )
        }
            .sortedByDescending { it.priority }
            .take(4)

        return ProfileNarrativeSummary(insights)
    }
}
