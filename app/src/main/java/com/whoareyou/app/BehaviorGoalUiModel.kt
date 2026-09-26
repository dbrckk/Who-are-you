package com.whoareyou.app

enum class BehaviorGoalCopyKey {
    METRIC_STEPS,
    METRIC_SCREEN_TIME,
    METRIC_EVENING_USAGE,
    METRIC_APP_USAGE,
    STATUS_ACTIVE,
    STATUS_PAUSED,
    STATUS_COMPLETED,
    USER_DEFINED_TARGET,
    OBSERVED_DAYS,
    TARGET_MET_DAYS,
    MISSING_EVIDENCE
}

enum class BehaviorGoalValueKind {
    STEPS,
    DURATION
}

data class BehaviorGoalUiModel(
    val id: String,
    val metricCopy: BehaviorGoalCopyKey,
    val statusCopy: BehaviorGoalCopyKey,
    val valueKind: BehaviorGoalValueKind,
    val targetValue: Long,
    val packageName: String?,
    val observedDays: Int,
    val metDays: Int,
    val elapsedCompletedDays: Int,
    val remainingDays: Int,
    val hasMissingEvidence: Boolean
)

object BehaviorGoalUiModelFactory {
    fun build(
        progress: BehaviorGoalProgress,
        currentEpochDay: Long
    ): BehaviorGoalUiModel {
        val goal = progress.goal
        val endExclusive = goal.startEpochDay + goal.durationDays.toLong()
        val currentOrStart = maxOf(currentEpochDay, goal.startEpochDay)
        val remainingDays = (endExclusive - currentOrStart)
            .coerceAtLeast(0L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

        return BehaviorGoalUiModel(
            id = goal.id,
            metricCopy = when (goal.metric) {
                BehaviorGoalMetric.STEPS_AT_LEAST -> BehaviorGoalCopyKey.METRIC_STEPS
                BehaviorGoalMetric.SCREEN_TIME_AT_MOST -> BehaviorGoalCopyKey.METRIC_SCREEN_TIME
                BehaviorGoalMetric.EVENING_USAGE_AT_MOST -> BehaviorGoalCopyKey.METRIC_EVENING_USAGE
                BehaviorGoalMetric.APP_USAGE_AT_MOST -> BehaviorGoalCopyKey.METRIC_APP_USAGE
            },
            statusCopy = when (progress.status) {
                BehaviorGoalStatus.ACTIVE -> BehaviorGoalCopyKey.STATUS_ACTIVE
                BehaviorGoalStatus.PAUSED -> BehaviorGoalCopyKey.STATUS_PAUSED
                BehaviorGoalStatus.COMPLETED -> BehaviorGoalCopyKey.STATUS_COMPLETED
            },
            valueKind = when (goal.metric) {
                BehaviorGoalMetric.STEPS_AT_LEAST -> BehaviorGoalValueKind.STEPS
                BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
                BehaviorGoalMetric.EVENING_USAGE_AT_MOST,
                BehaviorGoalMetric.APP_USAGE_AT_MOST -> BehaviorGoalValueKind.DURATION
            },
            targetValue = goal.targetValue,
            packageName = goal.packageName,
            observedDays = progress.observedDays,
            metDays = progress.metDays,
            elapsedCompletedDays = progress.days.size,
            remainingDays = remainingDays,
            hasMissingEvidence = progress.days.any { it.measuredValue == null }
        )
    }
}
