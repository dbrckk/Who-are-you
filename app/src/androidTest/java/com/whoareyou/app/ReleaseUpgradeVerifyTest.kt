package com.whoareyou.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseUpgradeVerifyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun verifyPersistentState() {
        if (InstrumentationRegistry.getArguments().getString("releaseUpgradePhase") != "verify") return

        val profile = dataStoreBytes("who_are_you_profile")
        assertContains(profile, "onboarding_complete")
        assertContains(profile, "ads_removed")

        val behavior = dataStoreBytes("who_are_you_behavior")
        assertContains(behavior, "daily_behavior_v1")
        assertContains(behavior, "4321")
        assertContains(behavior, "activity_enabled")
        assertContains(behavior, "activity_state")
        assertContains(behavior, "AVAILABLE")

        val goals = dataStoreBytes("who_are_you_behavior_goals")
        assertContains(goals, "goals_v1")
        assertContains(goals, encoded("upgrade-probe-goal"))
        assertContains(goals, "STEPS_AT_LEAST")
        assertContains(goals, "4000")
    }

    private fun dataStoreBytes(name: String): ByteArray {
        val file = File(context.filesDir, "datastore/$name.preferences_pb")
        assertTrue("Missing persisted DataStore file: ${file.path}", file.isFile)
        return file.readBytes()
    }

    private fun assertContains(bytes: ByteArray, expected: String) {
        val raw = String(bytes, StandardCharsets.ISO_8859_1)
        assertTrue("Persisted DataStore missing marker: $expected", raw.contains(expected))
    }

    private fun encoded(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))
}
