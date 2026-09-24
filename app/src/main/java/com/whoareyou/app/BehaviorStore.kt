package com.whoareyou.app

import org.json.JSONArray
import org.json.JSONObject

object BehaviorStoreCodec {
    private const val RETENTION_PREVIOUS_DAYS = 30L

    fun encode(days: List<DailyBehaviorAggregate>): String {
        val array = JSONArray()
        days.sortedBy { it.epochDay }.forEach { day ->
            array.put(JSONObject().apply {
                put("day", day.epochDay)
                putNullable("steps", day.steps)
                putNullable("screen", day.totalForegroundMillis)
                putNullable("sessions", day.launchesOrSessions)
                put("morning", day.daypartUsage.morningMillis)
                put("afternoon", day.daypartUsage.afternoonMillis)
                put("evening", day.daypartUsage.eveningMillis)
                put("night", day.daypartUsage.nightMillis)
                put("apps", JSONArray().apply {
                    day.topApps.sortedWith(compareByDescending<AppUsageAggregate> { it.foregroundMillis }.thenBy { it.packageName })
                        .forEach { app ->
                            put(JSONObject().apply {
                                put("package", app.packageName)
                                put("foreground", app.foregroundMillis)
                                putNullable("sessions", app.launchesOrSessions)
                            })
                        }
                })
            })
        }
        return array.toString()
    }

    fun decode(payload: String?): List<DailyBehaviorAggregate> {
        if (payload.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(payload)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val apps = item.optJSONArray("apps") ?: JSONArray()
                    val topApps = buildList {
                        for (appIndex in 0 until apps.length()) {
                            val app = apps.getJSONObject(appIndex)
                            add(
                                AppUsageAggregate(
                                    packageName = app.getString("package"),
                                    foregroundMillis = app.getLong("foreground"),
                                    launchesOrSessions = app.optNullableInt("sessions")
                                )
                            )
                        }
                    }
                    add(
                        DailyBehaviorAggregate(
                            epochDay = item.getLong("day"),
                            steps = item.optNullableLong("steps"),
                            totalForegroundMillis = item.optNullableLong("screen"),
                            topApps = topApps,
                            launchesOrSessions = item.optNullableInt("sessions"),
                            daypartUsage = DaypartUsage(
                                morningMillis = item.optLong("morning", 0L),
                                afternoonMillis = item.optLong("afternoon", 0L),
                                eveningMillis = item.optLong("evening", 0L),
                                nightMillis = item.optLong("night", 0L)
                            )
                        )
                    )
                }
            }.distinctBy { it.epochDay }.sortedBy { it.epochDay }
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

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)
}
