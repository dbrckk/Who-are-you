package com.whoareyou.app

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseUpgradeVerifyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun verifyPersistentState() = runBlocking {
        if (InstrumentationRegistry.getArguments().getString("releaseUpgradePhase") != "verify") {
            return@runBlocking
        }

        val profile = preferences("who_are_you_profile")
        assertEquals(true, profile[booleanPreferencesKey("onboarding_complete")])
        assertEquals(true, profile[booleanPreferencesKey("ads_removed")])

        val behavior = preferences("who_are_you_behavior")
        assertEquals(true, behavior[booleanPreferencesKey("activity_enabled")])
        assertEquals("AVAILABLE", behavior[stringPreferencesKey("activity_state")])
        val history = requireNotNull(behavior[stringPreferencesKey("daily_behavior_v1")])
        val dayFields = history.lineSequence()
            .drop(1)
            .single()
            .split('|', limit = 9)
        assertEquals(9, dayFields.size)
        assertEquals(4_321L, dayFields[1].toLong())

        val goals = preferences("who_are_you_behavior_goals")
        val encodedGoals = requireNotNull(goals[stringPreferencesKey("goals_v1")])
        val goalFields = encodedGoals.lineSequence()
            .drop(1)
            .single()
            .split('|', limit = 7)
        assertEquals(7, goalFields.size)
        assertEquals("upgrade-probe-goal", decodeText(goalFields[0]))
        assertEquals("STEPS_AT_LEAST", goalFields[1])
        assertEquals(4_000L, goalFields[2].toLong())
    }

    private suspend fun preferences(name: String): Preferences {
        val store = PreferenceDataStoreFactory.create(
            produceFile = { File(context.filesDir, "datastore/$name.preferences_pb") }
        )
        return store.data.first()
    }

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )
}
