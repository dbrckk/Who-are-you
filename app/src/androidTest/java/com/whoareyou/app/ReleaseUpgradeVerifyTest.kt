package com.whoareyou.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
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

        val profile = ProfileStore.observe(context).first()
        assertTrue(profile.onboardingComplete)
        assertTrue(profile.adsRemoved)

        val behavior = BehaviorRepository.observe(context).first()
        assertEquals(4_321L, behavior.today?.steps)
        assertEquals(
            BehaviorSourceState.AVAILABLE,
            behavior.sourceStates[BehaviorSource.ACTIVITY]
        )

        val goals = BehaviorGoalRepository.observe(context).first()
        assertEquals(1, goals.size)
        val goal = goals.single()
        assertEquals("upgrade-probe-goal", goal.id)
        assertEquals(BehaviorGoalMetric.STEPS_AT_LEAST, goal.metric)
        assertEquals(4_000L, goal.target)
    }
}
