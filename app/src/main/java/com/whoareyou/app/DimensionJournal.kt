package com.whoareyou.app

import kotlin.math.abs

enum class JournalChangeKind {
    FIRST_MEASUREMENT,
    RETAKE_CHANGE,
    RETAKE_STABLE,
    OUTLIER,
    VOLATILE
}

data class JournalEntry(
    val score: Int,
    val epochDay: Long,
    val deltaFromPrevious: Int?,
    val kind: JournalChangeKind
)

data class DimensionJournal(
    val quizId: String,
    val entries: List<JournalEntry>,
    val firstScore: Int,
    val latestScore: Int,
    val totalChange: Int,
    val trendKind: LongitudinalTrendKind
)

object DimensionJournalEngine {
    fun build(
        quizId: String,
        points: List<TimedScore>
    ): DimensionJournal? {
        val normalized = points
            .takeLast(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ)
            .map { TimedScore(it.score.coerceIn(0, 100), it.epochDay.coerceAtLeast(0)) }

        if (normalized.isEmpty()) return null

        val trend = LongitudinalTrendEngine.build(quizId, normalized)
        val entries = normalized.mapIndexed { index, point ->
            val previous = normalized.getOrNull(index - 1)
            val delta = previous?.let { point.score - it.score }
            val kind = when {
                index == 0 -> JournalChangeKind.FIRST_MEASUREMENT
                index == normalized.lastIndex && trend.kind == LongitudinalTrendKind.OUTLIER ->
                    JournalChangeKind.OUTLIER
                trend.kind == LongitudinalTrendKind.VOLATILE ->
                    JournalChangeKind.VOLATILE
                delta != null && abs(delta) >= 8 ->
                    JournalChangeKind.RETAKE_CHANGE
                else ->
                    JournalChangeKind.RETAKE_STABLE
            }
            JournalEntry(
                score = point.score,
                epochDay = point.epochDay,
                deltaFromPrevious = delta,
                kind = kind
            )
        }

        return DimensionJournal(
            quizId = quizId,
            entries = entries,
            firstScore = normalized.first().score,
            latestScore = normalized.last().score,
            totalChange = normalized.last().score - normalized.first().score,
            trendKind = trend.kind
        )
    }
}
