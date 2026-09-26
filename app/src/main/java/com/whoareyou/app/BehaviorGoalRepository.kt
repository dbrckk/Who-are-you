package com.whoareyou.app

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.behaviorGoalDataStore by preferencesDataStore(
    name = "who_are_you_behavior_goals",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

object BehaviorGoalRepository {
    private val goalsKey = stringPreferencesKey("goals_v1")

    fun observe(context: Context): Flow<List<BehaviorGoal>> = context.behaviorGoalDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            BehaviorGoalStoreCodec.decode(prefs[goalsKey])
        }

    suspend fun upsert(context: Context, goal: BehaviorGoal) {
        context.behaviorGoalDataStore.edit { prefs ->
            val current = BehaviorGoalStoreCodec.decode(prefs[goalsKey])
            prefs[goalsKey] = BehaviorGoalStoreCodec.encode(
                BehaviorGoalStoreCodec.upsert(current, goal)
            )
        }
    }

    suspend fun setPaused(context: Context, id: String, paused: Boolean) {
        context.behaviorGoalDataStore.edit { prefs ->
            val current = BehaviorGoalStoreCodec.decode(prefs[goalsKey])
            prefs[goalsKey] = BehaviorGoalStoreCodec.encode(
                BehaviorGoalStoreCodec.setPaused(current, id, paused)
            )
        }
    }

    suspend fun remove(context: Context, id: String) {
        context.behaviorGoalDataStore.edit { prefs ->
            val current = BehaviorGoalStoreCodec.decode(prefs[goalsKey])
            prefs[goalsKey] = BehaviorGoalStoreCodec.encode(
                BehaviorGoalStoreCodec.remove(current, id)
            )
        }
    }

    suspend fun clearAll(context: Context) {
        context.behaviorGoalDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
