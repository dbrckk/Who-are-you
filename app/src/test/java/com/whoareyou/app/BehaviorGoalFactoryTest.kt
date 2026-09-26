package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class BehaviorGoalFactoryTest {
    @Test
    fun `create request becomes seven day goal starting today`() {
        val request = BehaviorGoalCreateRequest(
            metric = BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
            targetValue = 90L * 60_000L
        )

        val goal = BehaviorGoalFactory.create(
            request = request,
            id = "goal-1",
            startEpochDay = 500L
        )

        assertEquals("goal-1", goal.id)
        assertEquals(BehaviorGoalMetric.SCREEN_TIME_AT_MOST, goal.metric)
        assertEquals(90L * 60_000L, goal.targetValue)
        assertEquals(500L, goal.startEpochDay)
        assertEquals(BehaviorGoal.DEFAULT_DURATION_DAYS, goal.durationDays)
        assertEquals(null, goal.packageName)
    }

    @Test
    fun `app request preserves selected package`() {
        val request = BehaviorGoalCreateRequest(
            metric = BehaviorGoalMetric.APP_USAGE_AT_MOST,
            targetValue = 30L * 60_000L,
            packageName = "com.example.video"
        )

        val goal = BehaviorGoalFactory.create(request, "goal-app", 600L)

        assertEquals("com.example.video", goal.packageName)
    }
}
