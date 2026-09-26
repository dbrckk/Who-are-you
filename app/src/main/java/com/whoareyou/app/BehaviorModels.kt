package com.whoareyou.app

data class AppUsageAggregate(
    val packageName: String,
    val foregroundMillis: Long,
    val launchesOrSessions: Int?
)

data class DaypartUsage(
    val morningMillis: Long,
    val afternoonMillis: Long,
    val eveningMillis: Long,
    val nightMillis: Long
) {
    val totalMillis: Long
        get() = morningMillis + afternoonMillis + eveningMillis + nightMillis

    companion object {
        val EMPTY = DaypartUsage(0L, 0L, 0L, 0L)
    }
}

data class DailyBehaviorAggregate(
    val epochDay: Long,
    val steps: Long?,
    val totalForegroundMillis: Long?,
    val topApps: List<AppUsageAggregate>,
    val launchesOrSessions: Int?,
    val daypartUsage: DaypartUsage
)

enum class BehaviorSource { ACTIVITY, APP_USAGE }

enum class BehaviorSourceState {
    DISABLED,
    PERMISSION_REQUIRED,
    AVAILABLE,
    UNSUPPORTED,
    ERROR
}

sealed interface BehaviorCollectionResult<out T> {
    data class Data<T>(val value: T) : BehaviorCollectionResult<T>
    data object NoData : BehaviorCollectionResult<Nothing>
    data class Unavailable(val state: BehaviorSourceState) : BehaviorCollectionResult<Nothing>
}

data class ActivityDay(
    val epochDay: Long,
    val steps: Long
)

enum class BehaviorEvidenceTier { EARLY, DEVELOPING, ESTABLISHED }

enum class BehaviorInsightCategory {
    ACTIVITY_CONSISTENCY,
    ACTIVITY_CHANGE,
    SCREEN_TIME_CHANGE,
    APP_CONCENTRATION,
    LATE_USAGE_PATTERN,
    USAGE_REGULARITY,
    INSUFFICIENT_HISTORY
}

data class BehaviorInsight(
    val category: BehaviorInsightCategory,
    val evidenceTier: BehaviorEvidenceTier,
    val supportingValues: List<Long> = emptyList(),
    val copyTokens: List<String> = emptyList()
)

data class BehaviorSnapshot(
    val today: DailyBehaviorAggregate?,
    val last7Days: List<DailyBehaviorAggregate>,
    val last30Days: List<DailyBehaviorAggregate>,
    val sourceStates: Map<BehaviorSource, BehaviorSourceState>,
    val insights: List<BehaviorInsight>
) {
    companion object {
        val EMPTY = BehaviorSnapshot(
            today = null,
            last7Days = emptyList(),
            last30Days = emptyList(),
            sourceStates = BehaviorSource.entries.associateWith { BehaviorSourceState.DISABLED },
            insights = emptyList()
        )
    }
}
