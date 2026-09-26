package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorGoalPresentationTest {
    @Test
    fun `goals are recomputed from completed M774 history`() {
        val goal = BehaviorGoal.stepsAtLeast(
            id = "steps",
            targetSteps = 8_000L,
            startEpochDay = 100L
        )
        val snapshot = BehaviorSnapshot.EMPTY.copy(
            last30Days = listOf(
                DailyBehaviorAggregate(
                    epochDay = 100L,
                    steps = 9_000L,
                    totalForegroundMillis = null,
                    topApps = emptyList(),
                    launchesOrSessions = null,
                    daypartUsage = DaypartUsage.EMPTY
                ),
                DailyBehaviorAggregate(
                    epochDay = 101L,
                    steps = 7_000L,
                    totalForegroundMillis = null,
                    topApps = emptyList(),
                    launchesOrSessions = null,
                    daypartUsage = DaypartUsage.EMPTY
                )
            )
        )

        val models = BehaviorGoalPresentation.build(
            goals = listOf(goal),
            snapshot = snapshot,
            currentEpochDay = 102L
        )

        assertEquals(1, models.size)
        assertEquals(2, models.single().observedDays)
        assertEquals(1, models.single().metDays)
    }

    @Test
    fun `cleared behavior history leaves goal with missing progress`() {
        val goal = BehaviorGoal.screenTimeAtMost(
            id = "screen",
            targetMillis = 60L * 60_000L,
            startEpochDay = 200L
        )

        val models = BehaviorGoalPresentation.build(
            goals = listOf(goal),
            snapshot = BehaviorSnapshot.EMPTY,
            currentEpochDay = 203L
        )

        assertEquals(1, models.size)
        assertEquals(0, models.single().observedDays)
        assertTrue(models.single().hasMissingEvidence)
    }

    @Test
    fun `presentation is deterministic regardless of stored goal order`() {
        val first = BehaviorGoal.stepsAtLeast("b", 5_000L, 300L)
        val second = BehaviorGoal.stepsAtLeast("a", 6_000L, 300L)

        val ids = BehaviorGoalPresentation.build(
            goals = listOf(first, second).reversed(),
            snapshot = BehaviorSnapshot.EMPTY,
            currentEpochDay = 301L
        ).map { it.id }

        assertEquals(listOf("a", "b"), ids)
    }
}
