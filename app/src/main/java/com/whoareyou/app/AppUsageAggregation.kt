package com.whoareyou.app

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class AppUsageSession(
    val packageName: String,
    val start: Instant,
    val end: Instant
)

object AppUsageAggregator {
    fun aggregate(
        date: LocalDate,
        zone: ZoneId,
        sessions: List<AppUsageSession>,
        topLimit: Int = 5
    ): DailyBehaviorAggregate? {
        if (sessions.isEmpty()) return null

        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        val clipped = sessions.mapNotNull { session ->
            if (session.packageName.isBlank() || !session.end.isAfter(session.start)) return@mapNotNull null
            val start = maxOf(session.start, dayStart)
            val end = minOf(session.end, dayEnd)
            if (!end.isAfter(start)) null else session.copy(start = start, end = end)
        }
        if (clipped.isEmpty()) return null

        val byPackage = clipped.groupBy { it.packageName }.map { (packageName, appSessions) ->
            AppUsageAggregate(
                packageName = packageName,
                foregroundMillis = appSessions.sumOf { it.end.toEpochMilli() - it.start.toEpochMilli() },
                launchesOrSessions = appSessions.size
            )
        }.sortedWith(compareByDescending<AppUsageAggregate> { it.foregroundMillis }.thenBy { it.packageName })

        val total = byPackage.sumOf { it.foregroundMillis }
        val dayparts = clipped.fold(DaypartUsage.EMPTY) { acc, session -> acc + splitIntoDayparts(session, zone) }

        return DailyBehaviorAggregate(
            epochDay = date.toEpochDay(),
            steps = null,
            totalForegroundMillis = total,
            topApps = byPackage.take(topLimit.coerceAtLeast(0)),
            launchesOrSessions = clipped.size,
            daypartUsage = dayparts
        )
    }

    private fun splitIntoDayparts(session: AppUsageSession, zone: ZoneId): DaypartUsage {
        var cursor = session.start
        var result = DaypartUsage.EMPTY
        while (cursor.isBefore(session.end)) {
            val local = cursor.atZone(zone)
            val boundary = nextBoundary(local)
            val segmentEnd = minOf(session.end, boundary.toInstant())
            val millis = segmentEnd.toEpochMilli() - cursor.toEpochMilli()
            result = result.add(classify(local.toLocalTime()), millis)
            cursor = segmentEnd
        }
        return result
    }

    private fun nextBoundary(time: ZonedDateTime): ZonedDateTime {
        val date = time.toLocalDate()
        val localTime = time.toLocalTime()
        val next = when {
            localTime < MORNING -> MORNING
            localTime < AFTERNOON -> AFTERNOON
            localTime < EVENING -> EVENING
            localTime < NIGHT -> NIGHT
            else -> null
        }
        return if (next != null) ZonedDateTime.of(date, next, time.zone)
        else date.plusDays(1).atStartOfDay(time.zone)
    }

    private fun classify(time: LocalTime): Daypart = when {
        time >= MORNING && time < AFTERNOON -> Daypart.MORNING
        time >= AFTERNOON && time < EVENING -> Daypart.AFTERNOON
        time >= EVENING && time < NIGHT -> Daypart.EVENING
        else -> Daypart.NIGHT
    }

    private fun DaypartUsage.add(daypart: Daypart, millis: Long) = when (daypart) {
        Daypart.MORNING -> copy(morningMillis = morningMillis + millis)
        Daypart.AFTERNOON -> copy(afternoonMillis = afternoonMillis + millis)
        Daypart.EVENING -> copy(eveningMillis = eveningMillis + millis)
        Daypart.NIGHT -> copy(nightMillis = nightMillis + millis)
    }

    private operator fun DaypartUsage.plus(other: DaypartUsage) = DaypartUsage(
        morningMillis + other.morningMillis,
        afternoonMillis + other.afternoonMillis,
        eveningMillis + other.eveningMillis,
        nightMillis + other.nightMillis
    )

    private enum class Daypart { MORNING, AFTERNOON, EVENING, NIGHT }

    private val MORNING = LocalTime.of(6, 0)
    private val AFTERNOON = LocalTime.of(12, 0)
    private val EVENING = LocalTime.of(18, 0)
    private val NIGHT = LocalTime.of(22, 0)
}