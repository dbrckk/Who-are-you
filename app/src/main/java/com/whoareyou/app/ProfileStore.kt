package com.whoareyou.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "who_are_you_profile")

data class StoredProfile(
    val completedQuizIds: Set<String> = emptySet(),
    val latestScores: Map<String, Int> = emptyMap(),
    val adsRemoved: Boolean = false,
    val onboardingComplete: Boolean = false,
    val daily: DailyState = DailyState()
)

object ProfileStore {
    private val completedKey = stringPreferencesKey("completed_quiz_ids")
    private val scoresKey = stringPreferencesKey("latest_scores")
    private val adsRemovedKey = booleanPreferencesKey("ads_removed")
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val dailyAnsweredDateKey = stringPreferencesKey("daily_answered_date")
    private val dailyQuestionIdKey = stringPreferencesKey("daily_question_id")
    private val dailySelectedOptionKey = intPreferencesKey("daily_selected_option")
    private val currentStreakKey = intPreferencesKey("current_streak")
    private val longestStreakKey = intPreferencesKey("longest_streak")
    private val lastActiveDateKey = stringPreferencesKey("last_active_date")

    fun observe(context: Context): Flow<StoredProfile> = context.profileDataStore.data.map { prefs ->
        StoredProfile(
            completedQuizIds = decodeSet(prefs[completedKey]),
            latestScores = decodeScores(prefs[scoresKey]),
            adsRemoved = prefs[adsRemovedKey] ?: false,
            onboardingComplete = prefs[onboardingCompleteKey] ?: false,
            daily = DailyState(
                answeredDate = prefs[dailyAnsweredDateKey],
                questionId = prefs[dailyQuestionIdKey],
                selectedOption = prefs[dailySelectedOptionKey],
                currentStreak = prefs[currentStreakKey] ?: 0,
                longestStreak = prefs[longestStreakKey] ?: 0,
                lastActiveDate = prefs[lastActiveDateKey]
            )
        )
    }

    suspend fun saveQuizResult(context: Context, quizId: String, score: Int) {
        context.profileDataStore.edit { prefs ->
            val completed = decodeSet(prefs[completedKey]).toMutableSet().apply { add(quizId) }
            val scores = decodeScores(prefs[scoresKey]).toMutableMap().apply { put(quizId, score.coerceIn(0, 100)) }
            prefs[completedKey] = completed.sorted().joinToString(",")
            prefs[scoresKey] = scores.entries.sortedBy { it.key }.joinToString(";") { "${it.key}:${it.value}" }
        }
    }

    suspend fun saveDailyAnswer(
        context: Context,
        questionId: String,
        selectedOption: Int,
        date: LocalDate = LocalDate.now()
    ): DailyState {
        var result = DailyState()
        context.profileDataStore.edit { prefs ->
            val current = DailyState(
                answeredDate = prefs[dailyAnsweredDateKey],
                questionId = prefs[dailyQuestionIdKey],
                selectedOption = prefs[dailySelectedOptionKey],
                currentStreak = prefs[currentStreakKey] ?: 0,
                longestStreak = prefs[longestStreakKey] ?: 0,
                lastActiveDate = prefs[lastActiveDateKey]
            )

            if (current.answeredDate == date.toString()) {
                result = current
                return@edit
            }

            val (streak, longest) = StreakEngine.next(current, date)
            result = DailyState(
                answeredDate = date.toString(),
                questionId = questionId,
                selectedOption = selectedOption.coerceIn(0, 1),
                currentStreak = streak,
                longestStreak = longest,
                lastActiveDate = date.toString()
            )

            prefs[dailyAnsweredDateKey] = result.answeredDate!!
            prefs[dailyQuestionIdKey] = result.questionId!!
            prefs[dailySelectedOptionKey] = result.selectedOption!!
            prefs[currentStreakKey] = result.currentStreak
            prefs[longestStreakKey] = result.longestStreak
            prefs[lastActiveDateKey] = result.lastActiveDate!!
        }
        return result
    }

    suspend fun setAdsRemoved(context: Context, removed: Boolean) {
        context.profileDataStore.edit { it[adsRemovedKey] = removed }
    }

    suspend fun setOnboardingComplete(context: Context, complete: Boolean = true) {
        context.profileDataStore.edit { it[onboardingCompleteKey] = complete }
    }

    private fun decodeSet(raw: String?): Set<String> = raw
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

    private fun decodeScores(raw: String?): Map<String, Int> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) null else parts[1].toIntOrNull()?.let { parts[0] to it.coerceIn(0, 100) }
        }
        ?.toMap()
        ?: emptyMap()
}
