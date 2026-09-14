package com.whoareyou.app

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.clear
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.profileDataStore by preferencesDataStore(
    name = "who_are_you_profile",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

data class QuizResultCommit(
    val changed: Boolean,
    val persistedScore: Int
)

data class StoredProfile(
    val completedQuizIds: Set<String> = emptySet(),
    val latestScores: Map<String, Int> = emptyMap(),
    val previousScores: Map<String, Int> = emptyMap(),
    val scoreHistory: Map<String, List<Int>> = emptyMap(),
    val timedScoreHistory: Map<String, List<TimedScore>> = emptyMap(),
    val adsRemoved: Boolean = false,
    val onboardingComplete: Boolean = false,
    val announcedAchievementIds: Set<String> = emptySet(),
    val pendingAchievementIds: List<String> = emptyList(),
    val matchCount: Int = 0,
    val bestMatchPercent: Int? = null,
    val lowestMatchPercent: Int? = null,
    val daily: DailyState = DailyState()
)

object ProfileStore {
    // Bounded replay window: large enough to survive long-lived/restored sessions without unbounded DataStore growth.
    private const val MAX_COMMITTED_QUIZ_ATTEMPTS = 256
    private val completedKey = stringPreferencesKey("completed_quiz_ids")
    private val scoresKey = stringPreferencesKey("latest_scores")
    private val previousScoresKey = stringPreferencesKey("previous_scores")
    private val scoreHistoryKey = stringPreferencesKey("score_history_v2")
    private val timedScoreHistoryKey = stringPreferencesKey("timed_score_history_v1")
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

    fun observe(context: Context): Flow<StoredProfile> = context.profileDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map(::decodeProfile)

    suspend fun commitQuizResult(
        context: Context,
        quizId: String,
        score: Int,
        attemptId: String? = null
    ): QuizResultCommit? {
        val normalizedQuizId = quizId.trim()
        if (normalizedQuizId.isEmpty()) return null

        val normalizedScore = score.coerceIn(0, 100)
        var result = QuizResultCommit(changed = false, persistedScore = normalizedScore)

        context.profileDataStore.edit { prefs ->
            val normalizedAttemptId = attemptId?.trim()?.takeIf { it.isNotEmpty() }
            val legacyAttempts = ProfilePersistenceCodec.decodeStringMap(prefs[lastQuizAttemptIdsKey])
            val committedAttempts = ProfilePersistenceCodec.decodeList(prefs[committedQuizAttemptIdsKey]).toMutableList().apply {
                legacyAttempts.values.filterNot(::contains).forEach(::add)
            }
            val latestScores = ProfilePersistenceCodec.decodeScores(prefs[scoresKey])

            if (normalizedAttemptId != null && normalizedAttemptId in committedAttempts) {
                val persistedScore = latestScores[normalizedQuizId]
                if (persistedScore == null) {
                    result = QuizResultCommit(changed = false, persistedScore = -1)
                    return@edit
                }
                result = QuizResultCommit(
                    changed = false,
                    persistedScore = persistedScore
                )
                return@edit
            }

            val completed = ProfilePersistenceCodec.decodeSet(prefs[completedKey]).toMutableSet().apply { add(normalizedQuizId) }
            val history = ScoreHistoryEngine.update(
                latestScores = latestScores,
                previousScores = ProfilePersistenceCodec.decodeScores(prefs[previousScoresKey]),
                scoreHistory = ProfilePersistenceCodec.decodeScoreHistory(prefs[scoreHistoryKey]),
                quizId = normalizedQuizId,
                score = normalizedScore
            )
            prefs[completedKey] = completed.sorted().joinToString(",")
            prefs[scoresKey] = ProfilePersistenceCodec.encodeScores(history.latestScores)
            prefs[previousScoresKey] = ProfilePersistenceCodec.encodeScores(history.previousScores)
            prefs[scoreHistoryKey] = ProfilePersistenceCodec.encodeScoreHistory(history.scoreHistory)

            val timedHistory = ProfilePersistenceCodec.decodeTimedScoreHistory(
                prefs[timedScoreHistoryKey]
            ).toMutableMap()
            val existingTimed = timedHistory[normalizedQuizId].orEmpty()
            val migratedTimed = if (existingTimed.isEmpty()) {
                history.scoreHistory[normalizedQuizId]
                    .orEmpty()
                    .dropLast(1)
                    .map { TimedScore(it, 0) }
            } else {
                existingTimed
            }
            timedHistory[normalizedQuizId] = (
                migratedTimed + TimedScore(normalizedScore, LocalDate.now().toEpochDay())
            ).takeLast(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ)
            prefs[timedScoreHistoryKey] = ProfilePersistenceCodec.encodeTimedScoreHistory(timedHistory)
            if (normalizedAttemptId != null) {
                committedAttempts.remove(normalizedAttemptId)
                committedAttempts.add(normalizedAttemptId)
                prefs[committedQuizAttemptIdsKey] = committedAttempts
                    .takeLast(MAX_COMMITTED_QUIZ_ATTEMPTS)
                    .joinToString(",")
                prefs[lastQuizAttemptIdsKey] = ProfilePersistenceCodec.encodeStringMap(
                    legacyAttempts + (normalizedQuizId to normalizedAttemptId)
                )
            }
            result = QuizResultCommit(
                changed = true,
                persistedScore = history.latestScores[normalizedQuizId] ?: normalizedScore
            )
        }

        if (!result.changed && result.persistedScore < 0) return null
        if (result.changed) announceNewAchievements(context)
        return result
    }

    suspend fun saveQuizResult(context: Context, quizId: String, score: Int, attemptId: String? = null): Boolean =
        commitQuizResult(context, quizId, score, attemptId)?.changed ?: false

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

    suspend fun clearLocalProfile(context: Context) {
        context.profileDataStore.edit { it.clear() }
    }

    suspend fun setOnboardingComplete(context: Context, complete: Boolean = true) {
        context.profileDataStore.edit { it[onboardingCompleteKey] = complete }
    }

    suspend fun consumeAchievementUnlock(context: Context, achievementId: String) {
        context.profileDataStore.edit { prefs ->
            val pending = ProfilePersistenceCodec.decodeList(prefs[pendingAchievementsKey])
            prefs[pendingAchievementsKey] = AchievementUnlockQueue.consume(pending, achievementId).joinToString(",")
        }
    }

    private suspend fun announceNewAchievements(context: Context) {
        val newlyUnlocked = mutableListOf<String>()
        val totalQuizCount = withContext(Dispatchers.IO) {
            QuizRepository.load(context.applicationContext).size
        }
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
            completedQuizIds = ProfilePersistenceCodec.decodeSet(prefs[completedKey]),
            latestScores = ProfilePersistenceCodec.decodeScores(prefs[scoresKey]),
            previousScores = ProfilePersistenceCodec.decodeScores(prefs[previousScoresKey]),
            scoreHistory = ProfilePersistenceCodec.decodeScoreHistory(prefs[scoreHistoryKey]),
            timedScoreHistory = ProfilePersistenceCodec.decodeTimedScoreHistory(prefs[timedScoreHistoryKey]),
            adsRemoved = prefs[adsRemovedKey] ?: false,
            onboardingComplete = prefs[onboardingCompleteKey] ?: false,
            announcedAchievementIds = ProfilePersistenceCodec.decodeSet(prefs[announcedAchievementsKey]),
            pendingAchievementIds = ProfilePersistenceCodec.decodeList(prefs[pendingAchievementsKey]),
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
}
