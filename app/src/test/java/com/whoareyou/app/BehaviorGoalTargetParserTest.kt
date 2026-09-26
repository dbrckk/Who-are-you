package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BehaviorGoalTargetParserTest {
    @Test
    fun `steps target keeps explicit integer value`() {
        assertEquals(
            9_000L,
            BehaviorGoalTargetParser.parse(BehaviorGoalMetric.STEPS_AT_LEAST, "9000")
        )
    }

    @Test
    fun `duration target converts minutes to millis`() {
        assertEquals(
            90L * 60_000L,
            BehaviorGoalTargetParser.parse(BehaviorGoalMetric.SCREEN_TIME_AT_MOST, "90")
        )
    }

    @Test
    fun `duration overflow is rejected instead of wrapping negative`() {
        assertNull(
            BehaviorGoalTargetParser.parse(
                BehaviorGoalMetric.EVENING_USAGE_AT_MOST,
                Long.MAX_VALUE.toString()
            )
        )
    }

    @Test
    fun `blank and non numeric target are rejected`() {
        assertNull(BehaviorGoalTargetParser.parse(BehaviorGoalMetric.STEPS_AT_LEAST, ""))
        assertNull(BehaviorGoalTargetParser.parse(BehaviorGoalMetric.APP_USAGE_AT_MOST, "abc"))
    }
}
