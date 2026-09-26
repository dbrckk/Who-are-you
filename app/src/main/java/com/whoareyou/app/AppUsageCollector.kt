package com.whoareyou.app

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class RawAppUsageEvent(
    val packageName: String,
    val timestamp: Instant,
    val type: Type
) {
    enum class Type { RESUMED, PAUSED }
}

interface AppUsageDataSource {
    fun state(): BehaviorSourceState
    fun events(start: Instant, end: Instant): List<RawAppUsageEvent>
}

class AppUsageCollector(
    private val dataSource: AppUsageDataSource,
    private val zone: ZoneId = ZoneId.systemDefault()
) {
    fun collectDay(date: LocalDate): BehaviorCollectionResult<DailyBehaviorAggregate> {
        val state = runCatching { dataSource.state() }
            .getOrElse { return BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR) }

        if (state != BehaviorSourceState.AVAILABLE) {
            return BehaviorCollectionResult.Unavailable(state)
        }

        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val events = runCatching { dataSource.events(start, end) }
            .getOrElse { return BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR) }

        if (events.isEmpty()) return BehaviorCollectionResult.NoData

        val sessions = reconstructSessions(events)
        if (sessions.isEmpty()) return BehaviorCollectionResult.NoData

        val aggregate = AppUsageAggregator.aggregate(date, zone, sessions)
            ?: return BehaviorCollectionResult.NoData
        return BehaviorCollectionResult.Data(aggregate)
    }

    private fun reconstructSessions(events: List<RawAppUsageEvent>): List<AppUsageSession> {
        val active = mutableMapOf<String, Instant>()
        val sessions = mutableListOf<AppUsageSession>()

        events.asSequence()
            .filter { it.packageName.isNotBlank() }
            .sortedWith(compareBy<RawAppUsageEvent> { it.timestamp }.thenBy { it.packageName })
            .forEach { event ->
                when (event.type) {
                    RawAppUsageEvent.Type.RESUMED -> active.putIfAbsent(event.packageName, event.timestamp)
                    RawAppUsageEvent.Type.PAUSED -> {
                        val started = active.remove(event.packageName) ?: return@forEach
                        if (event.timestamp.isAfter(started)) {
                            sessions += AppUsageSession(
                                packageName = event.packageName,
                                start = started,
                                end = event.timestamp
                            )
                        }
                    }
                }
            }

        return sessions
    }
}

class AndroidAppUsageDataSource(context: Context) : AppUsageDataSource {
    private val appContext = context.applicationContext

    override fun state(): BehaviorSourceState = UsageAccess.state(appContext)

    override fun events(start: Instant, end: Instant): List<RawAppUsageEvent> {
        val manager = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val usageEvents = manager.queryEvents(start.toEpochMilli(), end.toEpochMilli())
        val event = UsageEvents.Event()
        val result = mutableListOf<RawAppUsageEvent>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val type = when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> RawAppUsageEvent.Type.RESUMED
                UsageEvents.Event.ACTIVITY_PAUSED -> RawAppUsageEvent.Type.PAUSED
                else -> null
            } ?: continue

            result += RawAppUsageEvent(
                packageName = event.packageName.orEmpty(),
                timestamp = Instant.ofEpochMilli(event.timeStamp),
                type = type
            )
        }

        return result
    }
}
