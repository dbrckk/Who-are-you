package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorGoalUiModelTest {
    private fun progress(
        goal: BehaviorGoal,
        status: BehaviorGoalStatus = BehaviorGoalStatus.ACTIVE,
        observedDays: Int = 3,
        metDays: Int = 2,
        days: Int = 3
    ) = BehaviorGoalProgress(
        goal = goal,
        status = status,
        days = (0 until days).map { index ->
            BehaviorGoalDayResult(
                epochDay = goal.startEpochDay + index,
                measuredValue = if (index < observedDays) goal.targetValue else null,
                met = if (index < observedDays) index < metDays else null
            )
        },
        observedDays = observedDays,
        metDays = metDays
    )

    @Test
    fun `steps goal exposes user target and measured progress`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 8_000L, startEpochDay = 100L)
        val model = BehaviorGoalUiModelFactory.build(progress(goal), currentEpochDay = 104L)

        assertEquals(BehaviorGoalCopyKey.METRIC_STEPS, model.metricCopy)
        assertEquals(BehaviorGoalCopyKey.STATUS_ACTIVE, model.statusCopy)
        assertEquals(BehaviorGoalValueKind.STEPS, model.valueKind)
        assertEquals(8_000L, model.targetValue)
        assertEquals(3, model.observedDays)
        assertEquals(2, model.metDays)
        assertEquals(3, model.elapsedCompletedDays)
        assertEquals(3, model.remainingDays)
        assertFalse(model.hasMissingEvidence)
    }

    @Test
    fun `partial evidence is explicitly marked missing without counting failure`() {
        val goal = BehaviorGoal.screenTimeAtMost("screen", 3_600_000L, startEpochDay = 200L)
        val model = BehaviorGoalUiModelFactory.build(
            progress(goal, observedDays = 2, metDays = 1, days = 4),
            currentEpochDay = 204L
        )

        assertTrue(model.hasMissingEvidence)
        assertEquals(2, model.observedDays)
        assertEquals(1, model.metDays)
        assertEquals(4, model.elapsedCompletedDays)
    }

    @Test
    fun `paused and completed states are separate presentation states`() {
        val goal = BehaviorGoal.eveningUsageAtMost("evening", 1_800_000L, startEpochDay = 300L)

        val paused = BehaviorGoalUiModelFactory.build(
            progress(goal.copy(paused = true), BehaviorGoalStatus.PAUSED),
            currentEpochDay = 304L
        )
        val completed = BehaviorGoalUiModelFactory.build(
            progress(goal, BehaviorGoalStatus.COMPLETED, days = 7),
            currentEpochDay = 308L
        )

        assertEquals(BehaviorGoalCopyKey.STATUS_PAUSED, paused.statusCopy)
        assertEquals(BehaviorGoalCopyKey.STATUS_COMPLETED, completed.statusCopy)
        assertEquals(0, completed.remainingDays)
    }

    @Test
    fun `app goal keeps package identity for later safe label resolution`() {
        val goal = BehaviorGoal.appUsageAtMost(
            id = "app",
            packageName = "com.example.video",
            targetMillis = 1_200_000L,
            startEpochDay = 400L
        )

        val model = BehaviorGoalUiModelFactory.build(progress(goal), currentEpochDay = 404L)

        assertEquals(BehaviorGoalCopyKey.METRIC_APP_USAGE, model.metricCopy)
        assertEquals(BehaviorGoalValueKind.DURATION, model.valueKind)
        assertEquals("com.example.video", model.packageName)
    }

    @Test
    fun `presentation vocabulary contains no health personality or moral judgment`() {
        val forbidden = listOf(
            "healthy", "unhealthy", "good", "bad", "addict", "diagnos",
            "personality", "depress", "anxiety", "lazy", "success", "failure"
        )

        BehaviorGoalCopyKey.entries.forEach { key ->
            val normalized = key.name.lowercase()
            forbidden.forEach { word ->
                assertFalse("$key contains $word", normalized.contains(word))
            }
        }
    }
}
