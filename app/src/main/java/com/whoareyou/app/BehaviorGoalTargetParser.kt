package com.whoareyou.app

object BehaviorGoalTargetParser {
    fun parse(metric: BehaviorGoalMetric, text: String): Long? {
        val value = text.toLongOrNull()?.takeIf { it >= 0L } ?: return null
        return when (metric) {
            BehaviorGoalMetric.STEPS_AT_LEAST -> value
            BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
            BehaviorGoalMetric.EVENING_USAGE_AT_MOST,
            BehaviorGoalMetric.APP_USAGE_AT_MOST ->
                runCatching { Math.multiplyExact(value, 60_000L) }.getOrNull()
        }
    }
}
