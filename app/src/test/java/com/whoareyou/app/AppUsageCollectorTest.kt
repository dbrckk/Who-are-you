package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUsageCollectorTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val date = LocalDate.of(2026, 9, 25)

    @Test
    fun `permission required does not query events`() {
        val source = FakeSource(BehaviorSourceState.PERMISSION_REQUIRED)
        val result = AppUsageCollector(source, zone).collectDay(date)

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.PERMISSION_REQUIRED),
            result
        )
        assertEquals(0, source.eventCalls)
    }

    @Test
    fun `empty usage event stream is unknown rather than zero`() {
        val source = FakeSource(BehaviorSourceState.AVAILABLE, emptyList())
        val result = AppUsageCollector(source, zone).collectDay(date)

        assertEquals(BehaviorCollectionResult.NoData, result)
    }

    @Test
    fun `resume pause events become deterministic sessions`() {
        val source = FakeSource(
            BehaviorSourceState.AVAILABLE,
            listOf(
                resumed("b.app", "2026-09-25T08:00:00Z"),
                paused("b.app", "2026-09-25T08:10:00Z"),
                resumed("a.app", "2026-09-25T09:00:00Z"),
                paused("a.app", "2026-09-25T09:10:00Z")
            )
        )

        val result = AppUsageCollector(source, zone).collectDay(date)
        assertTrue(result is BehaviorCollectionResult.Data)
        val aggregate = (result as BehaviorCollectionResult.Data).value

        assertEquals(2, aggregate.launchesOrSessions)
        assertEquals(listOf("a.app", "b.app"), aggregate.topApps.map { it.packageName })
        assertEquals(20L * 60L * 1000L, aggregate.totalForegroundMillis)
    }

    @Test
    fun `unmatched events do not fabricate foreground time`() {
        val source = FakeSource(
            BehaviorSourceState.AVAILABLE,
            listOf(
                resumed("a.app", "2026-09-25T08:00:00Z"),
                paused("b.app", "2026-09-25T09:00:00Z")
            )
        )

        assertEquals(
            BehaviorCollectionResult.NoData,
            AppUsageCollector(source, zone).collectDay(date)
        )
    }

    @Test
    fun `duplicate resume keeps earliest open timestamp`() {
        val source = FakeSource(
            BehaviorSourceState.AVAILABLE,
            listOf(
                resumed("a.app", "2026-09-25T08:00:00Z"),
                resumed("a.app", "2026-09-25T08:05:00Z"),
                paused("a.app", "2026-09-25T08:10:00Z")
            )
        )

        val result = AppUsageCollector(source, zone).collectDay(date) as BehaviorCollectionResult.Data
        assertEquals(10L * 60L * 1000L, result.value.totalForegroundMillis)
        assertEquals(1, result.value.launchesOrSessions)
    }

    @Test
    fun `source failures surface as error state`() {
        val source = object : AppUsageDataSource {
            override fun state() = BehaviorSourceState.AVAILABLE
            override fun events(start: Instant, end: Instant): List<RawAppUsageEvent> {
                error("usage service unavailable")
            }
        }

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR),
            AppUsageCollector(source, zone).collectDay(date)
        )
    }

    private class FakeSource(
        private val currentState: BehaviorSourceState,
        private val items: List<RawAppUsageEvent> = emptyList()
    ) : AppUsageDataSource {
        var eventCalls = 0

        override fun state(): BehaviorSourceState = currentState

        override fun events(start: Instant, end: Instant): List<RawAppUsageEvent> {
            eventCalls += 1
            return items
        }
    }

    private fun resumed(packageName: String, timestamp: String) = RawAppUsageEvent(
        packageName,
        Instant.parse(timestamp),
        RawAppUsageEvent.Type.RESUMED
    )

    private fun paused(packageName: String, timestamp: String) = RawAppUsageEvent(
        packageName,
        Instant.parse(timestamp),
        RawAppUsageEvent.Type.PAUSED
    )
}
