package com.whoareyou.app

enum class BehaviorGoalMetric {
    STEPS_AT_LEAST,
    SCREEN_TIME_AT_MOST,
    EVENING_USAGE_AT_MOST,
    APP_USAGE_AT_MOST
}

data class BehaviorGoal(
    val id: String,
    val metric: BehaviorGoalMetric,
    val targetValue: Long,
    val startEpochDay: Long,
    val durationDays: Int = DEFAULT_DURATION_DAYS,
    val packageName: String? = null,
    val paused: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Goal id must not be blank" }
        require(targetValue >= 0L) { "Goal target must not be negative" }
        require(durationDays > 0) { "Goal duration must be positive" }
        if (metric == BehaviorGoalMetric.APP_USAGE_AT_MOST) {
            require(!packageName.isNullOrBlank()) { "App usage goal requires a package name" }
        }
    }

    companion object {
        const val DEFAULT_DURATION_DAYS = 7

        fun stepsAtLeast(
            id: String,
            targetSteps: Long,
            startEpochDay: Long,
            durationDays: Int = DEFAULT_DURATION_DAYS
        ) = BehaviorGoal(
            id = id,
            metric = BehaviorGoalMetric.STEPS_AT_LEAST,
            targetValue = targetSteps,
            startEpochDay = startEpochDay,
            durationDays = durationDays
        )

        fun screenTimeAtMost(
            id: String,
            targetMillis: Long,
            startEpochDay: Long,
            durationDays: Int = DEFAULT_DURATION_DAYS
        ) = BehaviorGoal(
            id = id,
            metric = BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
            targetValue = targetMillis,
            startEpochDay = startEpochDay,
            durationDays = durationDays
        )

        fun eveningUsageAtMost(
            id: String,
            targetMillis: Long,
            startEpochDay: Long,
            durationDays: Int = DEFAULT_DURATION_DAYS
        ) = BehaviorGoal(
            id = id,
            metric = BehaviorGoalMetric.EVENING_USAGE_AT_MOST,
            targetValue = targetMillis,
            startEpochDay = startEpochDay,
            durationDays = durationDays
        )

        fun appUsageAtMost(
            id: String,
            packageName: String,
            targetMillis: Long,
            startEpochDay: Long,
            durationDays: Int = DEFAULT_DURATION_DAYS
        ) = BehaviorGoal(
            id = id,
            metric = BehaviorGoalMetric.APP_USAGE_AT_MOST,
            targetValue = targetMillis,
            startEpochDay = startEpochDay,
            durationDays = durationDays,
            packageName = packageName
        )
    }
}

enum class BehaviorGoalStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}

data class BehaviorGoalDayResult(
    val epochDay: Long,
    val measuredValue: Long?,
    val met: Boolean?
)

data class BehaviorGoalProgress(
    val goal: BehaviorGoal,
    val status: BehaviorGoalStatus,
    val days: List<BehaviorGoalDayResult>,
    val observedDays: Int,
    val metDays: Int
)

object BehaviorGoalEngine {
    fun evaluate(
        goal: BehaviorGoal,
        days: List<DailyBehaviorAggregate>,
        currentEpochDay: Long
    ): BehaviorGoalProgress {
        val endExclusive = goal.startEpochDay + goal.durationDays.toLong()
        val completedEndExclusive = minOf(currentEpochDay, endExclusive)
        val byDay = days
            .groupBy { it.epochDay }
            .mapValues { (_, duplicates) -> duplicates.maxBy(::canonicalDayKey) }

        val results = if (completedEndExclusive <= goal.startEpochDay) {
            emptyList()
        } else {
            (goal.startEpochDay until completedEndExclusive).map { epochDay ->
                val value = measuredValue(goal, byDay[epochDay])
                BehaviorGoalDayResult(
                    epochDay = epochDay,
                    measuredValue = value,
                    met = value?.let { meetsTarget(goal.metric, it, goal.targetValue) }
                )
            }
        }

        val status = when {
            goal.paused -> BehaviorGoalStatus.PAUSED
            currentEpochDay >= endExclusive -> BehaviorGoalStatus.COMPLETED
            else -> BehaviorGoalStatus.ACTIVE
        }

        return BehaviorGoalProgress(
            goal = goal,
            status = status,
            days = results,
            observedDays = results.count { it.measuredValue != null },
            metDays = results.count { it.met == true }
        )
    }

    private fun measuredValue(goal: BehaviorGoal, day: DailyBehaviorAggregate?): Long? = when (goal.metric) {
        BehaviorGoalMetric.STEPS_AT_LEAST -> day?.steps
        BehaviorGoalMetric.SCREEN_TIME_AT_MOST -> day?.totalForegroundMillis
        BehaviorGoalMetric.EVENING_USAGE_AT_MOST ->
            day?.takeIf { it.totalForegroundMillis != null }?.daypartUsage?.eveningMillis
        BehaviorGoalMetric.APP_USAGE_AT_MOST ->
            day?.topApps
                ?.firstOrNull { it.packageName == goal.packageName }
                ?.foregroundMillis
    }

    private fun meetsTarget(metric: BehaviorGoalMetric, value: Long, target: Long): Boolean = when (metric) {
        BehaviorGoalMetric.STEPS_AT_LEAST -> value >= target
        BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
        BehaviorGoalMetric.EVENING_USAGE_AT_MOST,
        BehaviorGoalMetric.APP_USAGE_AT_MOST -> value <= target
    }

    private fun canonicalDayKey(day: DailyBehaviorAggregate): String = buildString {
        append(day.steps ?: Long.MIN_VALUE)
        append('|').append(day.totalForegroundMillis ?: Long.MIN_VALUE)
        append('|').append(day.launchesOrSessions ?: Int.MIN_VALUE)
        append('|').append(day.daypartUsage.morningMillis)
        append('|').append(day.daypartUsage.afternoonMillis)
        append('|').append(day.daypartUsage.eveningMillis)
        append('|').append(day.daypartUsage.nightMillis)
        day.topApps
            .sortedWith(compareBy<AppUsageAggregate> { it.packageName }.thenBy { it.foregroundMillis })
            .forEach { app ->
                append('|').append(app.packageName)
                append(':').append(app.foregroundMillis)
                append(':').append(app.launchesOrSessions ?: Int.MIN_VALUE)
            }
    }
}
