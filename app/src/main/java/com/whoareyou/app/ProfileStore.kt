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
    private const val MAX_COMMITTED_QUIZ_ATTEMPTS = 64
    private val completedKey = stringPreferencesKey("completed_quiz_ids")
    private val scoresKey = stringPreferencesKey("latest_scores")
    private val previousScoresKey = stringPreferencesKey("previous_scores")
    // Legacy per-quiz marker retained for migration from versions before M81.
    private val lastQuizAttemptIdsKey = stringPreferencesKey("last_quiz_attempt_ids")
    private val committedQuizAttemptIdsKey = stringPreferencesKey("committed_quiz_attempt_ids")
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
            val legacyAttempts = decodeStringMap(prefs[lastQuizAttemptIdsKey])
            val committedAttempts = decodeList(prefs[committedQuizAttemptIdsKey]).toMutableList().apply {
                legacyAttempts.values.filterNot(::contains).forEach(::add)
            }
            if (normalizedAttemptId != null && normalizedAttemptId in committedAttempts) {
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
                committedAttempts.remove(normalizedAttemptId)
                committedAttempts.add(normalizedAttemptId)
                prefs[committedQuizAttemptIdsKey] = committedAttempts
                    .takeLast(MAX_COMMITTED_QUIZ_ATTEMPTS)
                    .joinToString(",")
                prefs[lastQuizAttemptIdsKey] = encodeStringMap(legacyAttempts + (normalizedQuizId to normalizedAttemptId))
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
        .mapNotNull { (rawId, score) -> rawId.trim().takeIf(String::isNotEmpty)?.let { it to score.coerceIn(0, 100) } }
        .distinctBy { it.first }
        .sortedBy { it.first }
        .joinToString(";") { (id, score) -> "$id:$score" }

    private fun decodeScores(raw: String?): Map<String, Int> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val id = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val score = parts[1].trim().toIntOrNull()?.coerceIn(0, 100) ?: return@mapNotNull null
            id to score
        }
        ?.toMap()
        ?: emptyMap()

    private fun encodeStringMap(values: Map<String, String>): String = values.entries
        .mapNotNull { (rawKey, rawValue) ->
            val key = rawKey.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val value = rawValue.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            key to value
        }
        .distinctBy { it.first }
        .sortedBy { it.first }
        .joinToString(";") { (key, value) -> "$key:$value" }

    private fun decodeStringMap(raw: String?): Map<String, String> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val key = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val value = parts[1].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            key to value
        }
        ?.toMap()
        ?: emptyMap()
}
