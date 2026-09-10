package com.whoareyou.app

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(
    name = "who_are_you_profile",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

data class StoredProfile(
    val completedQuizIds: Set<String> = emptySet(),
    val latestScores: Map<String, Int> = emptyMap(),
    val previousScores: Map<String, Int> = emptyMap(),
    val adsRemoved: Boolean = false,
    // Optimistic only for Compose's pre-DataStore initial frame. Fresh installs receive false from DataStore immediately after load.
    val onboardingComplete: Boolean = true,
    val announcedAchievementIds: Set<String> = emptySet(),
    val pendingAchievementIds: List<String> = emptyList(),
    val matchCount: Int = 0,
    val bestMatchPercent: Int? = null,
    val lowestMatchPercent: Int? = null,
    val daily: DailyState = DailyState()
)

object ProfileStore {
    private val completedKey = stringPreferencesKey("completed_quiz_ids")
    private val scoresKey = stringPreferencesKey("latest_scores")
    private val previousScoresKey = stringPreferencesKey("previous_scores")
    private val lastQuizAttemptIdsKey = stringPreferencesKey("last_quiz_attempt_ids")
    private val adsRemovedKey = booleanPreferencesKey("ads_removed")
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val announcedAchievementsKey = stringPreferencesKey("announced_achievement_ids")
    private val pendingAchievementsKey = stringPreferencesKey("pending_achievement_ids")
    private val matchCountKey = intPreferencesKey("match_count")
    private val bestMatchKey = intPreferencesKey("best_match_percent")
    private val lowestMatchKey = intPreferencesKey("lowest_match_percent")
    private val dailyAnsweredDateKey = stringPreferencesKey("daily_answered_date")
    private val dailyQuestionIdKey = stringPreferencesKey("daily_question_id")
    private val dailySelectedOptionKey = intPreferencesKey("daily_selected_option")
    private val currentStreakKey = intPreferencesKey("current_streak")
    private val longestStreakKey = intPreferencesKey("longest_streak")
    private val lastActiveDateKey = stringPreferencesKey("last_active_date")

    fun observe(context: Context): Flow<StoredProfile> = context.profileDataStore.data.map(::decodeProfile)

    suspend fun saveQuizResult(context: Context, quizId: String, score: Int, attemptId: String? = null): Boolean {
        val normalizedQuizId = quizId.trim()
        if (normalizedQuizId.isEmpty()) return false

        var changed = false
        context.profileDataStore.edit { prefs ->
            val normalizedAttemptId = attemptId?.trim()?.takeIf { it.isNotEmpty() }
            val previousAttempts = decodeStringMap(prefs[lastQuizAttemptIdsKey])
            if (normalizedAttemptId != null && previousAttempts[normalizedQuizId] == normalizedAttemptId) {
                return@edit
            }

            val completed = decodeSet(prefs[completedKey]).toMutableSet().apply { add(normalizedQuizId) }
            val history = ScoreHistoryEngine.update(
                latestScores = decodeScores(prefs[scoresKey]),
                previousScores = decodeScores(prefs[previousScoresKey]),
                quizId = normalizedQuizId,
                score = score
            )
            prefs[completedKey] = completed.sorted().joinToString(",")
            prefs[scoresKey] = encodeScores(history.latestScores)
            prefs[previousScoresKey] = encodeScores(history.previousScores)
            if (normalizedAttemptId != null) {
                prefs[lastQuizAttemptIdsKey] = encodeStringMap(previousAttempts + (normalizedQuizId to normalizedAttemptId))
            }
            changed = true
        }
        if (changed) announceNewAchievements(context)
        return changed
    }

    suspend fun saveMatchResult(context: Context, compatibility: Int) {
        val score = compatibility.coerceIn(0, 100)
        context.profileDataStore.edit { prefs ->
            val count = (prefs[matchCountKey] ?: 0).coerceAtLeast(0) + 1
            val best = prefs[bestMatchKey]?.coerceIn(0, 100)
            val lowest = prefs[lowestMatchKey]?.coerceIn(0, 100)
            prefs[matchCountKey] = count
            prefs[bestMatchKey] = maxOf(best ?: score, score)
            prefs[lowestMatchKey] = minOf(lowest ?: score, score)
        }
        announceNewAchievements(context)
    }

    suspend fun saveDailyAnswer(
        context: Context,
        questionId: String,
        selectedOption: Int,
        date: LocalDate = LocalDate.now()
    ): DailyState {
        var result = DailyState()
        var changed = false
        context.profileDataStore.edit { prefs ->
            val current = DailyState(
                answeredDate = prefs[dailyAnsweredDateKey],
                questionId = prefs[dailyQuestionIdKey],
                selectedOption = prefs[dailySelectedOptionKey],
                currentStreak = (prefs[currentStreakKey] ?: 0).coerceAtLeast(0),
                longestStreak = (prefs[longestStreakKey] ?: 0).coerceAtLeast(0),
                lastActiveDate = prefs[lastActiveDateKey]
            )

            if (current.answeredDate == date.toString()) {
                result = ProfileIntegrity.sanitize(StoredProfile(daily = current)).daily
                return@edit
            }

            val (streak, longest) = StreakEngine.next(current, date)
            result = DailyState(
                answeredDate = date.toString(),
                questionId = questionId.trim().ifEmpty { DailyQuestionEngine.forDate(date).id },
                selectedOption = selectedOption.coerceIn(0, 1),
                currentStreak = streak,
                longestStreak = longest,
                lastActiveDate = date.toString()
            )
            changed = true

            prefs[dailyAnsweredDateKey] = result.answeredDate!!
            prefs[dailyQuestionIdKey] = result.questionId!!
            prefs[dailySelectedOptionKey] = result.selectedOption!!
            prefs[currentStreakKey] = result.currentStreak
            prefs[longestStreakKey] = result.longestStreak
            prefs[lastActiveDateKey] = result.lastActiveDate!!
        }
        if (changed) announceNewAchievements(context)
        return result
    }

    suspend fun setAdsRemoved(context: Context, removed: Boolean) {
        context.profileDataStore.edit { it[adsRemovedKey] = removed }
    }

    suspend fun setOnboardingComplete(context: Context, complete: Boolean = true) {
        context.profileDataStore.edit { it[onboardingCompleteKey] = complete }
    }

    suspend fun consumeAchievementUnlock(context: Context, achievementId: String) {
        context.profileDataStore.edit { prefs ->
            val pending = decodeList(prefs[pendingAchievementsKey])
            prefs[pendingAchievementsKey] = AchievementUnlockQueue.consume(pending, achievementId).joinToString(",")
        }
    }

    private suspend fun announceNewAchievements(context: Context) {
        val newlyUnlocked = mutableListOf<String>()
        val totalQuizCount = QuizRepository.load(context).size
        context.profileDataStore.edit { prefs ->
            val profile = decodeProfile(prefs)
            val announced = profile.announcedAchievementIds
            newlyUnlocked += AchievementEngine.unlocked(profile, totalQuizCount)
                .map { it.id }
                .filterNot { it in announced }
            if (newlyUnlocked.isNotEmpty()) {
                prefs[announcedAchievementsKey] = (announced + newlyUnlocked).sorted().joinToString(",")
                prefs[pendingAchievementsKey] = AchievementUnlockQueue.enqueue(
                    pendingIds = profile.pendingAchievementIds,
                    newlyUnlockedIds = newlyUnlocked
                ).joinToString(",")
            }
        }
        newlyUnlocked.forEach(AppEvents::achievementUnlock)
    }

    private fun decodeProfile(prefs: androidx.datastore.preferences.core.Preferences): StoredProfile = ProfileIntegrity.sanitize(
        StoredProfile(
            completedQuizIds = decodeSet(prefs[completedKey]),
            latestScores = decodeScores(prefs[scoresKey]),
            previousScores = decodeScores(prefs[previousScoresKey]),
            adsRemoved = prefs[adsRemovedKey] ?: false,
            onboardingComplete = prefs[onboardingCompleteKey] ?: false,
            announcedAchievementIds = decodeSet(prefs[announcedAchievementsKey]),
            pendingAchievementIds = decodeList(prefs[pendingAchievementsKey]),
            matchCount = prefs[matchCountKey] ?: 0,
            bestMatchPercent = prefs[bestMatchKey],
            lowestMatchPercent = prefs[lowestMatchKey],
            daily = DailyState(
                answeredDate = prefs[dailyAnsweredDateKey],
                questionId = prefs[dailyQuestionIdKey],
                selectedOption = prefs[dailySelectedOptionKey],
                currentStreak = prefs[currentStreakKey] ?: 0,
                longestStreak = prefs[longestStreakKey] ?: 0,
                lastActiveDate = prefs[lastActiveDateKey]
            )
        )
    )

    private fun decodeSet(raw: String?): Set<String> = decodeList(raw).toSet()

    private fun decodeList(raw: String?): List<String> = raw
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.distinct()
        ?: emptyList()

    private fun encodeScores(scores: Map<String, Int>): String = scores.entries
        .sortedBy { it.key }
        .joinToString(";") { "${it.key}:${it.value.coerceIn(0, 100)}" }

    private fun decodeScores(raw: String?): Map<String, Int> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) null else parts[1].toIntOrNull()?.let { parts[0] to it.coerceIn(0, 100) }
        }
        ?.toMap()
        ?: emptyMap()

    private fun encodeStringMap(values: Map<String, String>): String = values.entries
        .filter { it.key.isNotBlank() && it.value.isNotBlank() }
        .sortedBy { it.key }
        .joinToString(";") { "${it.key}:${it.value}" }

    private fun decodeStringMap(raw: String?): Map<String, String> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) null else parts[0] to parts[1]
        }
        ?.toMap()
        ?: emptyMap()
}
