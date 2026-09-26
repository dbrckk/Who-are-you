package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityAggregationTest {
    private val zone = ZoneId.of("Europe/Paris")

    @Test
    fun `unsupported source is reported without reading data`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.UNSUPPORTED, 1234L)
        val result = ActivityCollector(source, zone).collectDay(LocalDate.of(2026, 9, 24))

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.UNSUPPORTED),
            result
        )
        assertEquals(0, source.readCount)
    }

    @Test
    fun `denied authorization is reported without writing a zero`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.PERMISSION_REQUIRED, 0L)
        val result = ActivityCollector(source, zone).collectDay(LocalDate.of(2026, 9, 24))

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.PERMISSION_REQUIRED),
            result
        )
        assertEquals(0, source.readCount)
    }

    @Test
    fun `measured zero remains a real measurement`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.AVAILABLE, 0L)
        val result = ActivityCollector(source, zone).collectDay(LocalDate.of(2026, 9, 24))

        assertEquals(
            BehaviorCollectionResult.Data(
                ActivityDay(LocalDate.of(2026, 9, 24).toEpochDay(), 0L)
            ),
            result
        )
    }

    @Test
    fun `missing history stays missing rather than becoming zero`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.AVAILABLE, null)
        val result = ActivityCollector(source, zone).collectDay(LocalDate.of(2026, 9, 24))

        assertEquals(BehaviorCollectionResult.NoData, result)
    }

    @Test
    fun `local day boundaries follow timezone including DST`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.AVAILABLE, 8000L)
        val date = LocalDate.of(2026, 10, 25)
        ActivityCollector(source, zone).collectDay(date)

        val expectedStart = date.atStartOfDay(zone).toInstant()
        val expectedEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        assertEquals(expectedStart, source.lastStart)
        assertEquals(expectedEnd, source.lastEnd)
        assertEquals(25L * 60L * 60L, expectedEnd.epochSecond - expectedStart.epochSecond)
    }

    @Test
    fun `permission revocation is observed on every collection`() = runBlocking {
        val source = FakeActivityDataSource(BehaviorSourceState.AVAILABLE, 5000L)
        val collector = ActivityCollector(source, zone)
        val date = LocalDate.of(2026, 9, 24)

        assertTrue(collector.collectDay(date) is BehaviorCollectionResult.Data)
        source.currentState = BehaviorSourceState.PERMISSION_REQUIRED

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.PERMISSION_REQUIRED),
            collector.collectDay(date)
        )
        assertEquals(1, source.readCount)
    }

    @Test
    fun `collector failures surface as error state`() = runBlocking {
        val source = object : ActivityDataSource {
            override suspend fun state() = BehaviorSourceState.AVAILABLE
            override suspend fun readSteps(start: Instant, end: Instant): Long? {
                error("provider unavailable")
            }
        }

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR),
            ActivityCollector(source, zone).collectDay(LocalDate.of(2026, 9, 24))
        )
    }

    private class FakeActivityDataSource(
        var currentState: BehaviorSourceState,
        private var steps: Long?
    ) : ActivityDataSource {
        var readCount = 0
        var lastStart: Instant? = null
        var lastEnd: Instant? = null

        override suspend fun state(): BehaviorSourceState = currentState

        override suspend fun readSteps(start: Instant, end: Instant): Long? {
            readCount += 1
            lastStart = start
            lastEnd = end
            return steps
        }
    }
}
