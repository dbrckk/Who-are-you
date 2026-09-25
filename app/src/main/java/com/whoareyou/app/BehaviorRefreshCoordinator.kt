package com.whoareyou.app

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

data class BehaviorRefreshSnapshot(
    val day: DailyBehaviorAggregate?,
    val sourceStates: Map<BehaviorSource, BehaviorSourceState>
)

interface BehaviorRefreshStore {
    suspend fun snapshot(epochDay: Long): BehaviorRefreshSnapshot
    suspend fun upsert(day: DailyBehaviorAggregate, currentEpochDay: Long)
    suspend fun setSourceState(source: BehaviorSource, state: BehaviorSourceState)
}

data class BehaviorRefreshResult(
    val epochDay: Long,
    val sourceStates: Map<BehaviorSource, BehaviorSourceState>,
    val updatedSources: Set<BehaviorSource>
)

class BehaviorRefreshCoordinator(
    private val store: BehaviorRefreshStore,
    private val activityCollector: suspend (LocalDate) -> BehaviorCollectionResult<ActivityDay>,
    private val appUsageCollector: suspend (LocalDate) -> BehaviorCollectionResult<DailyBehaviorAggregate>,
    private val zone: ZoneId = ZoneId.systemDefault()
) {
    suspend fun refresh(now: Instant): BehaviorRefreshResult {
        val date = now.atZone(zone).toLocalDate()
        val epochDay = date.toEpochDay()
        val initial = store.snapshot(epochDay)
        val states = initial.sourceStates.toMutableMap()
        val updated = linkedSetOf<BehaviorSource>()
        var day = initial.day

        if (states[BehaviorSource.ACTIVITY] != BehaviorSourceState.DISABLED) {
            when (val result = activityCollector(date)) {
                is BehaviorCollectionResult.Data -> {
                    day = mergeActivity(day, result.value)
                    store.upsert(requireNotNull(day), epochDay)
                    store.setSourceState(BehaviorSource.ACTIVITY, BehaviorSourceState.AVAILABLE)
                    states[BehaviorSource.ACTIVITY] = BehaviorSourceState.AVAILABLE
                    updated += BehaviorSource.ACTIVITY
                }
                BehaviorCollectionResult.NoData -> {
                    store.setSourceState(BehaviorSource.ACTIVITY, BehaviorSourceState.AVAILABLE)
                    states[BehaviorSource.ACTIVITY] = BehaviorSourceState.AVAILABLE
                }
                is BehaviorCollectionResult.Unavailable -> {
                    store.setSourceState(BehaviorSource.ACTIVITY, result.state)
                    states[BehaviorSource.ACTIVITY] = result.state
                }
            }
        }

        if (states[BehaviorSource.APP_USAGE] != BehaviorSourceState.DISABLED) {
            when (val result = appUsageCollector(date)) {
                is BehaviorCollectionResult.Data -> {
                    day = mergeAppUsage(day, result.value)
                    store.upsert(requireNotNull(day), epochDay)
                    store.setSourceState(BehaviorSource.APP_USAGE, BehaviorSourceState.AVAILABLE)
                    states[BehaviorSource.APP_USAGE] = BehaviorSourceState.AVAILABLE
                    updated += BehaviorSource.APP_USAGE
                }
                BehaviorCollectionResult.NoData -> {
                    store.setSourceState(BehaviorSource.APP_USAGE, BehaviorSourceState.AVAILABLE)
                    states[BehaviorSource.APP_USAGE] = BehaviorSourceState.AVAILABLE
                }
                is BehaviorCollectionResult.Unavailable -> {
                    store.setSourceState(BehaviorSource.APP_USAGE, result.state)
                    states[BehaviorSource.APP_USAGE] = result.state
                }
            }
        }

        return BehaviorRefreshResult(
            epochDay = epochDay,
            sourceStates = states.toMap(),
            updatedSources = updated
        )
    }

    private fun mergeActivity(
        existing: DailyBehaviorAggregate?,
        activity: ActivityDay
    ): DailyBehaviorAggregate {
        require(existing == null || existing.epochDay == activity.epochDay)
        return (existing ?: emptyDay(activity.epochDay)).copy(steps = activity.steps)
    }

    private fun mergeAppUsage(
        existing: DailyBehaviorAggregate?,
        usage: DailyBehaviorAggregate
    ): DailyBehaviorAggregate {
        require(existing == null || existing.epochDay == usage.epochDay)
        return (existing ?: emptyDay(usage.epochDay)).copy(
            totalForegroundMillis = usage.totalForegroundMillis,
            topApps = usage.topApps,
            launchesOrSessions = usage.launchesOrSessions,
            daypartUsage = usage.daypartUsage
        )
    }

    private fun emptyDay(epochDay: Long) = DailyBehaviorAggregate(
        epochDay = epochDay,
        steps = null,
        totalForegroundMillis = null,
        topApps = emptyList(),
        launchesOrSessions = null,
        daypartUsage = DaypartUsage.EMPTY
    )
}

class AndroidBehaviorRefreshStore(
    context: Context
) : BehaviorRefreshStore {
    private val appContext = context.applicationContext

    override suspend fun snapshot(epochDay: Long): BehaviorRefreshSnapshot {
        val snapshot = BehaviorRepository.observe(appContext).first()
        val day = (snapshot.last30Days + listOfNotNull(snapshot.today))
            .firstOrNull { it.epochDay == epochDay }
        return BehaviorRefreshSnapshot(
            day = day,
            sourceStates = snapshot.sourceStates
        )
    }

    override suspend fun upsert(day: DailyBehaviorAggregate, currentEpochDay: Long) {
        BehaviorRepository.upsert(appContext, day, currentEpochDay)
    }

    override suspend fun setSourceState(source: BehaviorSource, state: BehaviorSourceState) {
        BehaviorRepository.setSourceState(appContext, source, state)
    }
}
