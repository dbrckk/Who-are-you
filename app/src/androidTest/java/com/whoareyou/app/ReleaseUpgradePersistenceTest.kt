package com.whoareyou.app

import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseUpgradePersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun seedPersistentState() = runBlocking {
        if (InstrumentationRegistry.getArguments().getString("releaseUpgradePhase") != "seed") return@runBlocking
        val today = LocalDate.now().toEpochDay()

        ProfileStore.setOnboardingComplete(context, true)
        ProfileStore.setAdsRemoved(context, true)

        BehaviorRepository.setSourceEnabled(context, BehaviorSource.ACTIVITY, true)
        BehaviorRepository.setSourceState(context, BehaviorSource.ACTIVITY, BehaviorSourceState.AVAILABLE)
        BehaviorRepository.upsert(
            context = context,
            day = DailyBehaviorAggregate(
                epochDay = today,
                steps = 4_321L,
                totalForegroundMillis = null,
                topApps = emptyList(),
                launchesOrSessions = null,
                daypartUsage = DaypartUsage.EMPTY
            ),
            currentEpochDay = today
        )

        BehaviorGoalRepository.upsert(
            context,
            BehaviorGoal.stepsAtLeast(
                id = "upgrade-probe-goal",
                targetSteps = 4_000L,
                startEpochDay = today
            )
        )

        assertTrue(ProfileStore.observe(context).first().onboardingComplete)
        assertEquals(4_321L, BehaviorRepository.observe(context).first().today?.steps)
        assertEquals("upgrade-probe-goal", BehaviorGoalRepository.observe(context).first().single().id)
    }

    @Test
    fun verifyPersistentState() = runBlocking {
        if (InstrumentationRegistry.getArguments().getString("releaseUpgradePhase") != "verify") return@runBlocking
        val profile = ProfileStore.observe(context).first()
        assertTrue(profile.onboardingComplete)
        assertTrue(profile.adsRemoved)

        val behavior = BehaviorRepository.observe(context).first()
        assertEquals(4_321L, behavior.today?.steps)
        assertEquals(BehaviorSourceState.AVAILABLE, behavior.sourceStates[BehaviorSource.ACTIVITY])

        val goals = BehaviorGoalRepository.observe(context).first()
        assertEquals(1, goals.size)
        assertEquals("upgrade-probe-goal", goals.single().id)
        assertEquals(4_000L, goals.single().targetValue)
    }
}
