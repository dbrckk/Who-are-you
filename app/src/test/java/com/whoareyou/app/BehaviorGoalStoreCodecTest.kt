package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorGoalStoreCodecTest {
    @Test
    fun `round trip preserves all supported goal fields deterministically`() {
        val goals = listOf(
            BehaviorGoal.stepsAtLeast("steps", 8_000L, startEpochDay = 10L),
            BehaviorGoal.screenTimeAtMost("screen", 7_200_000L, startEpochDay = 11L).copy(paused = true),
            BehaviorGoal.eveningUsageAtMost("evening", 1_800_000L, startEpochDay = 12L, durationDays = 14),
            BehaviorGoal.appUsageAtMost("app", "com.example.video", 1_200_000L, startEpochDay = 13L)
        )

        val encoded = BehaviorGoalStoreCodec.encode(goals)
        val decoded = BehaviorGoalStoreCodec.decode(encoded)

        assertEquals(BehaviorGoalStoreCodec.retain(goals), decoded)
        assertEquals(encoded, BehaviorGoalStoreCodec.encode(decoded))
    }

    @Test
    fun `corrupt payload falls back safely to empty goals`() {
        assertEquals(emptyList<BehaviorGoal>(), BehaviorGoalStoreCodec.decode("not-a-valid-payload"))
        assertEquals(emptyList<BehaviorGoal>(), BehaviorGoalStoreCodec.decode("v1\ninvalid|row"))
    }

    @Test
    fun `retention keeps only the newest bounded number of goals`() {
        val goals = (0 until 25).map { index ->
            BehaviorGoal.stepsAtLeast(
                id = "goal-$index",
                targetSteps = 1_000L + index,
                startEpochDay = index.toLong()
            )
        }

        val retained = BehaviorGoalStoreCodec.retain(goals)

        assertEquals(20, retained.size)
        assertEquals("goal-24", retained.first().id)
        assertEquals("goal-5", retained.last().id)
    }

    @Test
    fun `upsert replaces same id without duplicating goal`() {
        val original = BehaviorGoal.stepsAtLeast("same", 5_000L, startEpochDay = 1L)
        val replacement = BehaviorGoal.stepsAtLeast("same", 7_000L, startEpochDay = 2L)

        val result = BehaviorGoalStoreCodec.upsert(listOf(original), replacement)

        assertEquals(1, result.size)
        assertEquals(7_000L, result.single().targetValue)
        assertEquals(2L, result.single().startEpochDay)
    }

    @Test
    fun `set paused changes only selected goal`() {
        val first = BehaviorGoal.stepsAtLeast("first", 5_000L, startEpochDay = 1L)
        val second = BehaviorGoal.screenTimeAtMost("second", 3_600_000L, startEpochDay = 2L)

        val paused = BehaviorGoalStoreCodec.setPaused(listOf(first, second), "first", true)

        assertTrue(paused.first { it.id == "first" }.paused)
        assertFalse(paused.first { it.id == "second" }.paused)
    }

    @Test
    fun `remove deletes only selected goal`() {
        val first = BehaviorGoal.stepsAtLeast("first", 5_000L, startEpochDay = 1L)
        val second = BehaviorGoal.screenTimeAtMost("second", 3_600_000L, startEpochDay = 2L)

        val result = BehaviorGoalStoreCodec.remove(listOf(first, second), "first")

        assertEquals(listOf("second"), result.map { it.id })
    }
}
