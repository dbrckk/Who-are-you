package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorStoreCodecTest {
    @Test
    fun `round trip preserves nullable measurements and app aggregates`() {
        val days = listOf(
            day(100, steps = null, screen = 3_600_000L),
            day(101, steps = 7_321L, screen = null)
        )

        val decoded = BehaviorStoreCodec.decode(BehaviorStoreCodec.encode(days))

        assertEquals(days, decoded)
        assertNull(decoded.first().steps)
        assertNull(decoded.last().totalForegroundMillis)
    }

    @Test
    fun `retention keeps current local day plus previous thirty days`() {
        val days = (1L..50L).map { day(it, steps = it, screen = it * 1000L) }

        val retained = BehaviorStoreCodec.retain(days, currentEpochDay = 50L)

        assertEquals(31, retained.size)
        assertEquals(20L, retained.first().epochDay)
        assertEquals(50L, retained.last().epochDay)
    }

    @Test
    fun `completed history excludes the current partial local day`() {
        val days = listOf(day(48), day(49), day(50))

        val completed = BehaviorStoreCodec.completed(days, currentEpochDay = 50L)

        assertEquals(listOf(48L, 49L), completed.map { it.epochDay })
    }

    @Test
    fun `upsert replaces same day and keeps deterministic day order`() {
        val days = listOf(day(3), day(1), day(2, steps = 10L))
        val updated = BehaviorStoreCodec.upsert(days, day(2, steps = 20L), currentEpochDay = 3L)

        assertEquals(listOf(1L, 2L, 3L), updated.map { it.epochDay })
        assertEquals(20L, updated.first { it.epochDay == 2L }.steps)
    }

    @Test
    fun `clearing activity removes only activity measurements`() {
        val original = listOf(day(7, steps = 9_000L, screen = 5_000L))

        val cleared = BehaviorStoreCodec.clearSource(original, BehaviorSource.ACTIVITY)

        assertNull(cleared.single().steps)
        assertEquals(5_000L, cleared.single().totalForegroundMillis)
        assertTrue(cleared.single().topApps.isNotEmpty())
    }

    @Test
    fun `clearing app usage removes all app usage derived fields`() {
        val original = listOf(day(7, steps = 9_000L, screen = 5_000L))

        val cleared = BehaviorStoreCodec.clearSource(original, BehaviorSource.APP_USAGE).single()

        assertEquals(9_000L, cleared.steps)
        assertNull(cleared.totalForegroundMillis)
        assertNull(cleared.launchesOrSessions)
        assertTrue(cleared.topApps.isEmpty())
        assertEquals(DaypartUsage.EMPTY, cleared.daypartUsage)
    }

    @Test
    fun `corrupt payload decodes to empty history`() {
        assertEquals(emptyList<DailyBehaviorAggregate>(), BehaviorStoreCodec.decode("not-json\u0000broken"))
    }

    private fun day(
        epochDay: Long,
        steps: Long? = null,
        screen: Long? = null
    ) = DailyBehaviorAggregate(
        epochDay = epochDay,
        steps = steps,
        totalForegroundMillis = screen,
        topApps = if (screen == null) emptyList() else listOf(AppUsageAggregate("example.app", screen, 2)),
        launchesOrSessions = if (screen == null) null else 2,
        daypartUsage = if (screen == null) DaypartUsage.EMPTY else DaypartUsage(screen, 0L, 0L, 0L)
    )
}
