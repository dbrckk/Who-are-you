package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorInsightEngineTest {
    @Test
    fun `no history produces insufficient history only`() {
        val insights = BehaviorInsightEngine.build(
            days = emptyList(),
            sourceStates = mapOf(
                BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
                BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
            )
        )
        assertEquals(listOf(BehaviorInsightCategory.INSUFFICIENT_HISTORY), insights.map { it.category })
    }

    @Test
    fun `missing measurements are not converted to zero`() {
        val days = listOf(
            day(1, screen = 3_600_000L),
            day(2, screen = null),
            day(3, screen = 3_900_000L)
        )
        val insights = BehaviorInsightEngine.build(days, availableStates())
        assertFalse(insights.any { it.supportingValues.any { value -> value == 0L } })
    }

    @Test
    fun `input ordering does not change semantic output`() {
        val days = (1L..7L).map { day(it, screen = it * 1_000_000L, steps = 5_000L + it) }
        assertEquals(
            BehaviorInsightEngine.build(days, availableStates()),
            BehaviorInsightEngine.build(days.reversed(), availableStates())
        )
    }

    @Test
    fun `single extreme day does not become usual baseline`() {
        val days = listOf(
            day(1, screen = 3_600_000L), day(2, screen = 3_700_000L),
            day(3, screen = 3_500_000L), day(4, screen = 36_000_000L),
            day(5, screen = 3_600_000L), day(6, screen = 3_650_000L), day(7, screen = 3_550_000L)
        )
        val insights = BehaviorInsightEngine.build(days, availableStates())
        assertFalse(insights.any { it.category == BehaviorInsightCategory.SCREEN_TIME_CHANGE })
    }

    @Test
    fun `disabled app usage source emits no app usage conclusions`() {
        val days = (1L..7L).map { day(it, screen = 8_000_000L) }
        val insights = BehaviorInsightEngine.build(
            days,
            mapOf(
                BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
                BehaviorSource.APP_USAGE to BehaviorSourceState.DISABLED
            )
        )
        assertFalse(insights.any { it.category in setOf(
            BehaviorInsightCategory.SCREEN_TIME_CHANGE,
            BehaviorInsightCategory.APP_CONCENTRATION,
            BehaviorInsightCategory.LATE_USAGE_PATTERN,
            BehaviorInsightCategory.USAGE_REGULARITY
        ) })
    }

    @Test
    fun `insight vocabulary never emits clinical or personality labels`() {
        val days = (1L..10L).map { day(it, screen = 4_000_000L, steps = 7_000L) }
        val forbidden = listOf("addict", "depress", "anxious", "introvert", "lazy")
        val text = BehaviorInsightEngine.build(days, availableStates())
            .flatMap { it.copyTokens }
            .joinToString(" ")
            .lowercase()
        assertTrue(forbidden.none { it in text })
    }

    private fun availableStates() = mapOf(
        BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
        BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
    )

    private fun day(
        epochDay: Long,
        screen: Long? = null,
        steps: Long? = null
    ) = DailyBehaviorAggregate(
        epochDay = epochDay,
        steps = steps,
        totalForegroundMillis = screen,
        topApps = emptyList(),
        launchesOrSessions = null,
        daypartUsage = DaypartUsage.EMPTY
    )
}
