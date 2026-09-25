package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BehaviorGoalEngineTest {
    private fun day(
        epochDay: Long,
        steps: Long? = null,
        totalForegroundMillis: Long? = null,
        eveningMillis: Long = 0L,
        apps: List<AppUsageAggregate> = emptyList()
    ) = DailyBehaviorAggregate(
        epochDay = epochDay,
        steps = steps,
        totalForegroundMillis = totalForegroundMillis,
        topApps = apps,
        launchesOrSessions = null,
        daypartUsage = DaypartUsage(
            morningMillis = 0L,
            afternoonMillis = 0L,
            eveningMillis = eveningMillis,
            nightMillis = 0L
        )
    )

    @Test
    fun `steps goal meets user target when observed value is at least target`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 8_000L, startEpochDay = 10L)
        val progress = BehaviorGoalEngine.evaluate(goal, listOf(day(10, steps = 8_500L)), currentEpochDay = 11L)

        assertEquals(1, progress.observedDays)
        assertEquals(1, progress.metDays)
        assertEquals(8_500L, progress.days.single().measuredValue)
        assertEquals(true, progress.days.single().met)
    }

    @Test
    fun `screen time goal meets target when observed duration is at most target`() {
        val goal = BehaviorGoal.screenTimeAtMost("screen", 2 * 60 * 60 * 1000L, startEpochDay = 20L)
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(20, totalForegroundMillis = 90 * 60 * 1000L)),
            currentEpochDay = 21L
        )

        assertEquals(1, progress.metDays)
    }

    @Test
    fun `evening goal requires known app usage and does not treat missing usage as zero`() {
        val goal = BehaviorGoal.eveningUsageAtMost("evening", 30 * 60 * 1000L, startEpochDay = 30L)
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(30, totalForegroundMillis = null, eveningMillis = 0L)),
            currentEpochDay = 31L
        )

        assertEquals(0, progress.observedDays)
        assertNull(progress.days.single().measuredValue)
        assertNull(progress.days.single().met)
    }

    @Test
    fun `selected app absent from retained top apps remains missing`() {
        val goal = BehaviorGoal.appUsageAtMost(
            id = "app",
            packageName = "com.example.target",
            targetMillis = 20 * 60 * 1000L,
            startEpochDay = 40L
        )
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(
                day(
                    40,
                    totalForegroundMillis = 60 * 60 * 1000L,
                    apps = listOf(AppUsageAggregate("com.example.other", 60 * 60 * 1000L, 2))
                )
            ),
            currentEpochDay = 41L
        )

        assertEquals(0, progress.observedDays)
        assertNull(progress.days.single().measuredValue)
        assertNull(progress.days.single().met)
    }

    @Test
    fun `selected app absent on unknown usage day remains missing`() {
        val goal = BehaviorGoal.appUsageAtMost(
            id = "app",
            packageName = "com.example.target",
            targetMillis = 20 * 60 * 1000L,
            startEpochDay = 50L
        )
        val progress = BehaviorGoalEngine.evaluate(goal, listOf(day(50)), currentEpochDay = 51L)

        assertEquals(0, progress.observedDays)
        assertNull(progress.days.single().measuredValue)
    }

    @Test
    fun `current partial local day is excluded from experiment evidence`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 5_000L, startEpochDay = 60L)
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(60, steps = 6_000L), day(61, steps = 100L)),
            currentEpochDay = 61L
        )

        assertEquals(listOf(60L), progress.days.map { it.epochDay })
        assertEquals(1, progress.metDays)
    }

    @Test
    fun `missing completed day is represented as missing and not as failed target`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 5_000L, startEpochDay = 70L)
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(70, steps = 6_000L), day(72, steps = 7_000L)),
            currentEpochDay = 73L
        )

        assertEquals(listOf(70L, 71L, 72L), progress.days.map { it.epochDay })
        assertNull(progress.days[1].measuredValue)
        assertNull(progress.days[1].met)
        assertEquals(2, progress.observedDays)
        assertEquals(2, progress.metDays)
    }

    @Test
    fun `evaluation is deterministic across input ordering and duplicate days`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 5_000L, startEpochDay = 80L)
        val a = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(81, steps = 6_000L), day(80, steps = 5_500L), day(80, steps = 5_500L)),
            currentEpochDay = 82L
        )
        val b = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(80, steps = 5_500L), day(81, steps = 6_000L)),
            currentEpochDay = 82L
        )

        assertEquals(b, a)
    }

    @Test
    fun `experiment window clips evidence after configured duration`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 1_000L, startEpochDay = 90L, durationDays = 2)
        val progress = BehaviorGoalEngine.evaluate(
            goal,
            listOf(day(90, steps = 2_000L), day(91, steps = 2_000L), day(92, steps = 2_000L)),
            currentEpochDay = 94L
        )

        assertEquals(listOf(90L, 91L), progress.days.map { it.epochDay })
        assertEquals(BehaviorGoalStatus.COMPLETED, progress.status)
    }

    @Test
    fun `paused goal preserves evidence but reports paused status`() {
        val goal = BehaviorGoal.stepsAtLeast("steps", 1_000L, startEpochDay = 100L).copy(paused = true)
        val progress = BehaviorGoalEngine.evaluate(goal, listOf(day(100, steps = 2_000L)), currentEpochDay = 101L)

        assertEquals(BehaviorGoalStatus.PAUSED, progress.status)
        assertEquals(1, progress.observedDays)
    }

    @Test
    fun `invalid goal definitions are rejected`() {
        expectIllegalArgument { BehaviorGoal.stepsAtLeast("", 1_000L, startEpochDay = 1L) }
        expectIllegalArgument { BehaviorGoal.screenTimeAtMost("screen", -1L, startEpochDay = 1L) }
        expectIllegalArgument { BehaviorGoal.appUsageAtMost("app", "", 1_000L, startEpochDay = 1L) }
        expectIllegalArgument { BehaviorGoal.stepsAtLeast("steps", 1_000L, startEpochDay = 1L, durationDays = 0) }
    }

    private fun expectIllegalArgument(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            Unit
        }
    }
}
