package com.whoareyou.app

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Compact deterministic codec for bounded local behavioral aggregates.
 *
 * This deliberately uses only JDK/Kotlin primitives so pure JVM tests exercise the same codec as
 * Android. Android's org.json classes are framework stubs in local unit tests unless Robolectric is
 * introduced, which is unnecessary for this storage boundary.
 */
object BehaviorStoreCodec {
    private const val RETENTION_PREVIOUS_DAYS = 30L
    private const val VERSION = "v1"
    private const val NULL = "~"

    fun encode(days: List<DailyBehaviorAggregate>): String = buildString {
        append(VERSION)
        days.sortedBy { it.epochDay }.forEach { day ->
            append('\n')
            append(
                listOf(
                    day.epochDay.toString(),
                    nullableLong(day.steps),
                    nullableLong(day.totalForegroundMillis),
                    nullableInt(day.launchesOrSessions),
                    day.daypartUsage.morningMillis.toString(),
                    day.daypartUsage.afternoonMillis.toString(),
                    day.daypartUsage.eveningMillis.toString(),
                    day.daypartUsage.nightMillis.toString(),
                    encodeApps(day.topApps)
                ).joinToString("|")
            )
        }
    }

    fun decode(payload: String?): List<DailyBehaviorAggregate> {
        if (payload.isNullOrBlank()) return emptyList()
        return runCatching {
            val lines = payload.lineSequence().toList()
            require(lines.firstOrNull() == VERSION)
            lines.drop(1)
                .filter { it.isNotBlank() }
                .map(::decodeDay)
                .distinctBy { it.epochDay }
                .sortedBy { it.epochDay }
        }.getOrElse { emptyList() }
    }

    fun retain(days: List<DailyBehaviorAggregate>, currentEpochDay: Long): List<DailyBehaviorAggregate> {
        val firstRetainedDay = currentEpochDay - RETENTION_PREVIOUS_DAYS
        return days
            .filter { it.epochDay in firstRetainedDay..currentEpochDay }
            .distinctBy { it.epochDay }
            .sortedBy { it.epochDay }
    }

    fun upsert(
        days: List<DailyBehaviorAggregate>,
        day: DailyBehaviorAggregate,
        currentEpochDay: Long
    ): List<DailyBehaviorAggregate> = retain(
        days.filterNot { it.epochDay == day.epochDay } + day,
        currentEpochDay
    )

    fun clearSource(
        days: List<DailyBehaviorAggregate>,
        source: BehaviorSource
    ): List<DailyBehaviorAggregate> = days.map { day ->
        when (source) {
            BehaviorSource.ACTIVITY -> day.copy(steps = null)
            BehaviorSource.APP_USAGE -> day.copy(
                totalForegroundMillis = null,
                topApps = emptyList(),
                launchesOrSessions = null,
                daypartUsage = DaypartUsage.EMPTY
            )
        }
    }

    private fun decodeDay(line: String): DailyBehaviorAggregate {
        val fields = line.split('|', limit = 9)
        require(fields.size == 9)
        return DailyBehaviorAggregate(
            epochDay = fields[0].toLong(),
            steps = parseNullableLong(fields[1]),
            totalForegroundMillis = parseNullableLong(fields[2]),
            topApps = decodeApps(fields[8]),
            launchesOrSessions = parseNullableInt(fields[3]),
            daypartUsage = DaypartUsage(
                morningMillis = fields[4].toLong(),
                afternoonMillis = fields[5].toLong(),
                eveningMillis = fields[6].toLong(),
                nightMillis = fields[7].toLong()
            )
        )
    }

    private fun encodeApps(apps: List<AppUsageAggregate>): String = apps
        .sortedWith(compareByDescending<AppUsageAggregate> { it.foregroundMillis }.thenBy { it.packageName })
        .joinToString(",") { app ->
            listOf(
                encodeText(app.packageName),
                app.foregroundMillis.toString(),
                nullableInt(app.launchesOrSessions)
            ).joinToString(":")
        }

    private fun decodeApps(encoded: String): List<AppUsageAggregate> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(',').map { item ->
            val fields = item.split(':', limit = 3)
            require(fields.size == 3)
            AppUsageAggregate(
                packageName = decodeText(fields[0]),
                foregroundMillis = fields[1].toLong(),
                launchesOrSessions = parseNullableInt(fields[2])
            )
        }
    }

    private fun encodeText(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )

    private fun nullableLong(value: Long?): String = value?.toString() ?: NULL
    private fun nullableInt(value: Int?): String = value?.toString() ?: NULL
    private fun parseNullableLong(value: String): Long? = if (value == NULL) null else value.toLong()
    private fun parseNullableInt(value: String): Int? = if (value == NULL) null else value.toInt()
}
