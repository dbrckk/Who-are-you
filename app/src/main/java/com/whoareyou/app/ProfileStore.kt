package com.whoareyou.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "who_are_you_profile")

data class StoredProfile(
    val completedQuizIds: Set<String> = emptySet(),
    val latestScores: Map<String, Int> = emptyMap(),
    val adsRemoved: Boolean = false
)

object ProfileStore {
    private val completedKey = stringPreferencesKey("completed_quiz_ids")
    private val scoresKey = stringPreferencesKey("latest_scores")
    private val adsRemovedKey = booleanPreferencesKey("ads_removed")

    fun observe(context: Context): Flow<StoredProfile> = context.profileDataStore.data.map { prefs ->
        StoredProfile(
            completedQuizIds = decodeSet(prefs[completedKey]),
            latestScores = decodeScores(prefs[scoresKey]),
            adsRemoved = prefs[adsRemovedKey] ?: false
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

    suspend fun setAdsRemoved(context: Context, removed: Boolean) {
        context.profileDataStore.edit { it[adsRemovedKey] = removed }
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
