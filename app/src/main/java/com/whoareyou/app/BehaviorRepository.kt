package com.whoareyou.app

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.behaviorDataStore by preferencesDataStore(
    name = "who_are_you_behavior",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

/** Local-only persistence boundary for M774 behavioral observations. */
object BehaviorRepository {
    private val historyKey = stringPreferencesKey("daily_behavior_v1")
    private val activityEnabledKey = booleanPreferencesKey("activity_enabled")
    private val appUsageEnabledKey = booleanPreferencesKey("app_usage_enabled")
    private val activityStateKey = stringPreferencesKey("activity_state")
    private val appUsageStateKey = stringPreferencesKey("app_usage_state")

    fun observe(context: Context): Flow<BehaviorSnapshot> = context.behaviorDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            val today = LocalDate.now().toEpochDay()
            val history = BehaviorStoreCodec.retain(
                BehaviorStoreCodec.decode(prefs[historyKey]),
                today
            )
            val states = mapOf(
                BehaviorSource.ACTIVITY to resolvedState(
                    prefs[activityEnabledKey] ?: false,
                    prefs[activityStateKey]
                ),
                BehaviorSource.APP_USAGE to resolvedState(
                    prefs[appUsageEnabledKey] ?: false,
                    prefs[appUsageStateKey]
                )
            )
            val completed = BehaviorStoreCodec.completed(history, today)
            BehaviorSnapshot(
                today = history.firstOrNull { it.epochDay == today },
                last7Days = completed.filter { it.epochDay >= today - 7L },
                last30Days = completed.filter { it.epochDay >= today - 30L },
                sourceStates = states,
                insights = BehaviorInsightEngine.build(completed, states)
            )
        }

    suspend fun upsert(
        context: Context,
        day: DailyBehaviorAggregate,
        currentEpochDay: Long = LocalDate.now().toEpochDay()
    ) {
        context.behaviorDataStore.edit { prefs ->
            val history = BehaviorStoreCodec.decode(prefs[historyKey])
            prefs[historyKey] = BehaviorStoreCodec.encode(
                BehaviorStoreCodec.upsert(history, day, currentEpochDay)
            )
        }
    }

    suspend fun setSourceEnabled(context: Context, source: BehaviorSource, enabled: Boolean) {
        context.behaviorDataStore.edit { prefs ->
            prefs[enabledKey(source)] = enabled
            if (!enabled) prefs[stateKey(source)] = BehaviorSourceState.DISABLED.name
        }
    }

    suspend fun setSourceState(context: Context, source: BehaviorSource, state: BehaviorSourceState) {
        context.behaviorDataStore.edit { prefs ->
            prefs[stateKey(source)] = state.name
        }
    }

    suspend fun clearSource(context: Context, source: BehaviorSource) {
        context.behaviorDataStore.edit { prefs ->
            val history = BehaviorStoreCodec.decode(prefs[historyKey])
            prefs[historyKey] = BehaviorStoreCodec.encode(BehaviorStoreCodec.clearSource(history, source))
            prefs[enabledKey(source)] = false
            prefs[stateKey(source)] = BehaviorSourceState.DISABLED.name
        }
    }

    suspend fun clearAll(context: Context) {
        context.behaviorDataStore.edit { it.clear() }
    }

    private fun resolvedState(enabled: Boolean, stored: String?): BehaviorSourceState {
        if (!enabled) return BehaviorSourceState.DISABLED
        return stored?.let { raw ->
            BehaviorSourceState.entries.firstOrNull { it.name == raw }
        } ?: BehaviorSourceState.PERMISSION_REQUIRED
    }

    private fun enabledKey(source: BehaviorSource) = when (source) {
        BehaviorSource.ACTIVITY -> activityEnabledKey
        BehaviorSource.APP_USAGE -> appUsageEnabledKey
    }

    private fun stateKey(source: BehaviorSource) = when (source) {
        BehaviorSource.ACTIVITY -> activityStateKey
        BehaviorSource.APP_USAGE -> appUsageStateKey
    }
}
