package com.whoareyou.app

import java.time.LocalDate
import kotlin.math.abs

enum class NextQuizReason {
    NEW_COVERAGE,
    RESOLVE_UNCERTAINTY,
    RETAKE_DUE,
    NONE
}

data class NextQuizRecommendation(
    val quizId: String,
    val reason: NextQuizReason,
    val targetedTraitIds: List<String>,
    val informationGainScore: Double,
    val daysSinceLastAttempt: Long? = null
)

object NextQuizRecommendationEngine {
    const val MIN_RETAKE_DAYS = 14L
    const val PREFERRED_RETAKE_DAYS = 30L

    fun recommend(
        catalog: List<Quiz>,
        completedQuizIds: Set<String>,
        coverage: ProfileCoverage,
        timedScoreHistory: Map<String, List<TimedScore>>,
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): NextQuizRecommendation? {
        val coverageByTrait = coverage.traits.associateBy { it.traitId }

        val newQuiz = catalog
            .asSequence()
            .filter { it.id !in completedQuizIds }
            .mapNotNull { quiz ->
                val scored = scoreTraits(quiz, coverageByTrait)
                if (scored.first <= 0.0) null else NextQuizRecommendation(
                    quizId = quiz.id,
                    reason = if (scored.second.any { coverageByTrait[it]?.contradictoryEvidenceCount ?: 0 > 0 })
                        NextQuizReason.RESOLVE_UNCERTAINTY else NextQuizReason.NEW_COVERAGE,
                    targetedTraitIds = scored.second,
                    informationGainScore = scored.first
                )
            }
            .maxByOrNull { it.informationGainScore }

        if (newQuiz != null) return newQuiz

        return catalog
            .asSequence()
            .filter { it.id in completedQuizIds }
            .mapNotNull { quiz ->
                val dated = timedScoreHistory[quiz.id].orEmpty().filter { it.epochDay > 0 }
                val lastDay = dated.maxOfOrNull { it.epochDay } ?: return@mapNotNull null
                val days = (todayEpochDay - lastDay).coerceAtLeast(0)
                if (days < MIN_RETAKE_DAYS) return@mapNotNull null

                val scored = scoreTraits(quiz, coverageByTrait)
                val ageBoost = (days.toDouble() / PREFERRED_RETAKE_DAYS).coerceIn(0.5, 2.0)
                val instabilityBoost = if (dated.size >= 2) {
                    val recent = dated.takeLast(3).map { it.score }
                    if ((recent.maxOrNull() ?: 0) - (recent.minOrNull() ?: 0) >= 12) 1.25 else 1.0
                } else 1.0

                NextQuizRecommendation(
                    quizId = quiz.id,
                    reason = NextQuizReason.RETAKE_DUE,
                    targetedTraitIds = scored.second,
                    informationGainScore = scored.first * ageBoost * instabilityBoost,
                    daysSinceLastAttempt = days
                )
            }
            .maxByOrNull { it.informationGainScore }
    }

    private fun scoreTraits(
        quiz: Quiz,
        coverageByTrait: Map<String, TraitCoverage>
    ): Pair<Double, List<String>> {
        val scored = quiz.traits.map { mapping ->
            val state = coverageByTrait[mapping.id]
            val uncertainty = when {
                state == null || state.status == CoverageStatus.UNKNOWN -> 1.0
                state.contradictoryEvidenceCount > 0 -> 1.15
                state.status == CoverageStatus.LOW -> 0.85
                state.status == CoverageStatus.DEVELOPING -> 0.55
                else -> 0.25
            }
            mapping.id to (uncertainty * abs(mapping.weight))
        }
        return scored.sumOf { it.second } to scored
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
            .take(3)
    }
}
