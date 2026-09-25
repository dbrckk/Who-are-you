package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorRefreshCoordinatorTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val now = Instant.parse("2026-09-25T10:00:00Z")
    private val date = now.atZone(zone).toLocalDate()

    @Test
    fun `disabled sources are never queried`() = runBlocking {
        val store = FakeStore(states = mapOf(
            BehaviorSource.ACTIVITY to BehaviorSourceState.DISABLED,
            BehaviorSource.APP_USAGE to BehaviorSourceState.DISABLED
        ))
        var activityCalls = 0
        var usageCalls = 0
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = {
                activityCalls += 1
                BehaviorCollectionResult.Data(ActivityDay(it.toEpochDay(), 1000L))
            },
            appUsageCollector = {
                usageCalls += 1
                BehaviorCollectionResult.NoData
            },
            zone = zone
        )

        coordinator.refresh(now)

        assertEquals(0, activityCalls)
        assertEquals(0, usageCalls)
        assertTrue(store.days.isEmpty())
    }

    @Test
    fun `revocation changes source state without fabricating zero`() = runBlocking {
        val existing = day(steps = 4200L, foreground = 60_000L)
        val store = FakeStore(
            states = mapOf(
                BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
                BehaviorSource.APP_USAGE to BehaviorSourceState.DISABLED
            ),
            initialDay = existing
        )
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = {
                BehaviorCollectionResult.Unavailable(BehaviorSourceState.PERMISSION_REQUIRED)
            },
            appUsageCollector = { BehaviorCollectionResult.NoData },
            zone = zone
        )

        coordinator.refresh(now)

        assertEquals(BehaviorSourceState.PERMISSION_REQUIRED, store.states[BehaviorSource.ACTIVITY])
        assertEquals(4200L, store.days[date.toEpochDay()]?.steps)
    }

    @Test
    fun `one collector failure does not erase successful other source data`() = runBlocking {
        val store = FakeStore(states = mapOf(
            BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
            BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
        ))
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = {
                BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR)
            },
            appUsageCollector = { usageDay(foreground = 120_000L) },
            zone = zone
        )

        coordinator.refresh(now)

        val saved = store.days[date.toEpochDay()]!!
        assertNull(saved.steps)
        assertEquals(120_000L, saved.totalForegroundMillis)
        assertEquals(BehaviorSourceState.ERROR, store.states[BehaviorSource.ACTIVITY])
        assertEquals(BehaviorSourceState.AVAILABLE, store.states[BehaviorSource.APP_USAGE])
    }

    @Test
    fun `repeated refresh is idempotent for same local day`() = runBlocking {
        val store = FakeStore(states = mapOf(
            BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
            BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
        ))
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = {
                BehaviorCollectionResult.Data(ActivityDay(it.toEpochDay(), 5000L))
            },
            appUsageCollector = { usageDay(foreground = 90_000L) },
            zone = zone
        )

        coordinator.refresh(now)
        coordinator.refresh(now)

        assertEquals(1, store.days.size)
        val saved = store.days.values.single()
        assertEquals(5000L, saved.steps)
        assertEquals(90_000L, saved.totalForegroundMillis)
    }

    @Test
    fun `missing data leaves previous measurement untouched and does not create a day`() = runBlocking {
        val store = FakeStore(states = mapOf(
            BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
            BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
        ))
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = { BehaviorCollectionResult.NoData },
            appUsageCollector = { BehaviorCollectionResult.NoData },
            zone = zone
        )

        coordinator.refresh(now)

        assertTrue(store.days.isEmpty())
        assertEquals(BehaviorSourceState.AVAILABLE, store.states[BehaviorSource.ACTIVITY])
        assertEquals(BehaviorSourceState.AVAILABLE, store.states[BehaviorSource.APP_USAGE])
    }

    @Test
    fun `source merges preserve data collected by the other source`() = runBlocking {
        val store = FakeStore(states = mapOf(
            BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
            BehaviorSource.APP_USAGE to BehaviorSourceState.AVAILABLE
        ))
        val coordinator = BehaviorRefreshCoordinator(
            store = store,
            activityCollector = {
                BehaviorCollectionResult.Data(ActivityDay(it.toEpochDay(), 7000L))
            },
            appUsageCollector = { usageDay(foreground = 180_000L) },
            zone = zone
        )

        coordinator.refresh(now)

        val saved = store.days.values.single()
        assertEquals(7000L, saved.steps)
        assertEquals(180_000L, saved.totalForegroundMillis)
        assertEquals(2, saved.topApps.size)
    }

    private fun usageDay(foreground: Long): BehaviorCollectionResult<DailyBehaviorAggregate> =
        BehaviorCollectionResult.Data(
            DailyBehaviorAggregate(
                epochDay = date.toEpochDay(),
                steps = null,
                totalForegroundMillis = foreground,
                topApps = listOf(
                    AppUsageAggregate("a.app", foreground / 2, 1),
                    AppUsageAggregate("b.app", foreground / 2, 1)
                ),
                launchesOrSessions = 2,
                daypartUsage = DaypartUsage(foreground, 0L, 0L, 0L)
            )
        )

    private fun day(steps: Long?, foreground: Long?): DailyBehaviorAggregate =
        DailyBehaviorAggregate(
            epochDay = date.toEpochDay(),
            steps = steps,
            totalForegroundMillis = foreground,
            topApps = emptyList(),
            launchesOrSessions = null,
            daypartUsage = DaypartUsage.EMPTY
        )

    private class FakeStore(
        states: Map<BehaviorSource, BehaviorSourceState>,
        initialDay: DailyBehaviorAggregate? = null
    ) : BehaviorRefreshStore {
        val states = states.toMutableMap()
        val days = mutableMapOf<Long, DailyBehaviorAggregate>().apply {
            initialDay?.let { put(it.epochDay, it) }
        }

        override suspend fun snapshot(epochDay: Long): BehaviorRefreshSnapshot =
            BehaviorRefreshSnapshot(
                day = days[epochDay],
                sourceStates = states.toMap()
            )

        override suspend fun upsert(day: DailyBehaviorAggregate, currentEpochDay: Long) {
            days[day.epochDay] = day
        }

        override suspend fun setSourceState(source: BehaviorSource, state: BehaviorSourceState) {
            states[source] = state
        }
    }
}
