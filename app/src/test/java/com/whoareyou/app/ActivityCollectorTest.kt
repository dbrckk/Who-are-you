package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityCollectorTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val date = LocalDate.of(2026, 9, 25)

    @Test
    fun `unsupported Health Connect source is not queried`() = runBlocking {
        val source = FakeSource(BehaviorSourceState.UNSUPPORTED, 9_000L)

        val result = ActivityCollector(source, zone).collectDay(date)

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.UNSUPPORTED),
            result
        )
        assertEquals(0, source.readCalls)
    }

    @Test
    fun `revoked activity permission is not treated as zero steps`() = runBlocking {
        val source = FakeSource(BehaviorSourceState.PERMISSION_REQUIRED, 0L)

        val result = ActivityCollector(source, zone).collectDay(date)

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.PERMISSION_REQUIRED),
            result
        )
        assertEquals(0, source.readCalls)
    }

    @Test
    fun `available source with no aggregate remains unknown`() = runBlocking {
        val source = FakeSource(BehaviorSourceState.AVAILABLE, null)

        assertEquals(
            BehaviorCollectionResult.NoData,
            ActivityCollector(source, zone).collectDay(date)
        )
    }

    @Test
    fun `measured zero steps remains a real measurement`() = runBlocking {
        val source = FakeSource(BehaviorSourceState.AVAILABLE, 0L)

        assertEquals(
            BehaviorCollectionResult.Data(ActivityDay(date.toEpochDay(), 0L)),
            ActivityCollector(source, zone).collectDay(date)
        )
    }

    @Test
    fun `provider failure degrades to error without measurement`() = runBlocking {
        val source = object : ActivityDataSource {
            override suspend fun state() = BehaviorSourceState.AVAILABLE
            override suspend fun readSteps(start: Instant, end: Instant): Long? {
                error("provider failure")
            }
        }

        assertEquals(
            BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR),
            ActivityCollector(source, zone).collectDay(date)
        )
    }

    private class FakeSource(
        private val sourceState: BehaviorSourceState,
        private val steps: Long?
    ) : ActivityDataSource {
        var readCalls = 0

        override suspend fun state(): BehaviorSourceState = sourceState

        override suspend fun readSteps(start: Instant, end: Instant): Long? {
            readCalls += 1
            return steps
        }
    }
}
