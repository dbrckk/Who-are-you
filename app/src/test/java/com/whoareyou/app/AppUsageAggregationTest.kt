package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUsageAggregationTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val date = LocalDate.of(2026, 9, 24)

    @Test
    fun `empty event stream is unknown rather than measured zero`() {
        assertNull(AppUsageAggregator.aggregate(date, zone, emptyList()))
    }

    @Test
    fun `sessions are clipped to requested local day`() {
        val events = listOf(
            session("a", "2026-09-23T23:50:00Z", "2026-09-24T00:30:00Z"),
            session("a", "2026-09-24T21:30:00Z", "2026-09-24T23:30:00Z")
        )
        val result = AppUsageAggregator.aggregate(date, zone, events)!!
        assertTrue(result.totalForegroundMillis > 0L)
        assertEquals(result.totalForegroundMillis, result.topApps.sumOf { it.foregroundMillis })
    }

    @Test
    fun `repeated sessions increment frequency and aggregate package time`() {
        val events = listOf(
            session("b", "2026-09-24T08:00:00Z", "2026-09-24T08:10:00Z"),
            session("b", "2026-09-24T09:00:00Z", "2026-09-24T09:20:00Z")
        )
        val result = AppUsageAggregator.aggregate(date, zone, events)!!
        assertEquals(2, result.launchesOrSessions)
        assertEquals(2, result.topApps.single().launchesOrSessions)
    }

    @Test
    fun `top apps use deterministic duration then package ordering`() {
        val events = listOf(
            session("z.app", "2026-09-24T08:00:00Z", "2026-09-24T08:10:00Z"),
            session("a.app", "2026-09-24T09:00:00Z", "2026-09-24T09:10:00Z")
        )
        val result = AppUsageAggregator.aggregate(date, zone, events)!!
        assertEquals(listOf("a.app", "z.app"), result.topApps.map { it.packageName })
    }

    @Test
    fun `daypart totals exactly equal total foreground time`() {
        val events = listOf(
            session("a", "2026-09-24T04:30:00Z", "2026-09-24T05:30:00Z"),
            session("b", "2026-09-24T11:30:00Z", "2026-09-24T12:30:00Z"),
            session("c", "2026-09-24T17:30:00Z", "2026-09-24T18:30:00Z"),
            session("d", "2026-09-24T21:30:00Z", "2026-09-24T22:30:00Z")
        )
        val result = AppUsageAggregator.aggregate(date, zone, events)!!
        assertEquals(result.totalForegroundMillis, result.daypartUsage.totalMillis)
    }

    @Test
    fun `DST local day does not duplicate elapsed time`() {
        val dstDate = LocalDate.of(2026, 10, 25)
        val events = listOf(session("a", "2026-10-25T00:30:00Z", "2026-10-25T02:30:00Z"))
        val result = AppUsageAggregator.aggregate(dstDate, zone, events)!!
        assertEquals(2 * 60 * 60 * 1000L, result.totalForegroundMillis)
        assertEquals(result.totalForegroundMillis, result.daypartUsage.totalMillis)
    }

    private fun session(packageName: String, start: String, end: String) = AppUsageSession(
        packageName = packageName,
        start = Instant.parse(start),
        end = Instant.parse(end)
    )
}
