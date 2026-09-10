package com.whoareyou.app

import java.time.LocalDate

/**
 * Canonicalizes persisted profile data before it reaches product logic or UI.
 *
 * Preferences DataStore protects the file format, but values can still become inconsistent
 * across app upgrades, restores, interrupted writes from older versions, or test fixtures.
 * Keeping the repair policy pure makes those cases deterministic and unit-testable.
 */
object ProfileIntegrity {
    fun sanitize(profile: StoredProfile): StoredProfile {
        val matchCount = profile.matchCount.coerceAtLeast(0)
        val rawBest = profile.bestMatchPercent?.coerceIn(0, 100)
        val rawLowest = profile.lowestMatchPercent?.coerceIn(0, 100)
        val best = if (matchCount == 0) null else listOfNotNull(rawBest, rawLowest).maxOrNull()
        val lowest = if (matchCount == 0) null else listOfNotNull(rawBest, rawLowest).minOrNull()
        val latestScores = cleanScores(profile.latestScores)
        val previousScores = cleanScores(profile.previousScores)
        val completedQuizIds = cleanIds(profile.completedQuizIds).apply {
            addAll(latestScores.keys)
        }

        return profile.copy(
            completedQuizIds = completedQuizIds,
            latestScores = latestScores,
            previousScores = previousScores,
            announcedAchievementIds = cleanIds(profile.announcedAchievementIds),
            pendingAchievementIds = cleanIds(profile.pendingAchievementIds).toList(),
            matchCount = matchCount,
            bestMatchPercent = best,
            lowestMatchPercent = lowest,
            daily = sanitizeDaily(profile.daily)
        )
    }

    private fun sanitizeDaily(state: DailyState): DailyState {
        val answeredDate = validDate(state.answeredDate)
        val questionId = state.questionId?.trim()?.takeIf(String::isNotEmpty)
        val selectedOption = state.selectedOption?.takeIf { it in 0..1 }
        val hasCompleteAnswer = answeredDate != null && questionId != null && selectedOption != null
        val lastActiveDate = validDate(state.lastActiveDate)
        val currentStreak = if (lastActiveDate == null) 0 else state.currentStreak.coerceAtLeast(0)
        val longestStreak = maxOf(state.longestStreak.coerceAtLeast(0), currentStreak)

        return DailyState(
            answeredDate = answeredDate.takeIf { hasCompleteAnswer },
            questionId = questionId.takeIf { hasCompleteAnswer },
            selectedOption = selectedOption.takeIf { hasCompleteAnswer },
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastActiveDate = lastActiveDate
        )
    }

    private fun validDate(raw: String?): String? {
        val value = raw?.trim()?.takeIf(String::isNotEmpty) ?: return null
        return runCatching { LocalDate.parse(value).toString() }.getOrNull()
    }

    private fun cleanIds(ids: Iterable<String>): LinkedHashSet<String> = ids
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .toCollection(linkedSetOf())

    private fun cleanScores(scores: Map<String, Int>): Map<String, Int> = scores.entries
        .mapNotNull { (rawId, rawScore) ->
            rawId.trim().takeIf(String::isNotEmpty)?.let { it to rawScore.coerceIn(0, 100) }
        }
        .toMap()
}
