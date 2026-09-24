package com.whoareyou.app

import kotlin.math.abs

object BehaviorInsightEngine {
    private const val MIN_PATTERN_DAYS = 5
    private const val CHANGE_RATIO = 0.25
    private const val LATE_SHARE = 0.35
    private const val CONCENTRATION_SHARE = 0.70

    fun build(
        days: List<DailyBehaviorAggregate>,
        sourceStates: Map<BehaviorSource, BehaviorSourceState>
    ): List<BehaviorInsight> {
        val ordered = days.distinctBy { it.epochDay }.sortedBy { it.epochDay }
        if (ordered.size < MIN_PATTERN_DAYS) {
            return listOf(
                BehaviorInsight(
                    BehaviorInsightCategory.INSUFFICIENT_HISTORY,
                    BehaviorEvidenceTier.EARLY,
                    copyTokens = listOf("insufficient_history")
                )
            )
        }

        val insights = mutableListOf<BehaviorInsight>()
        if (sourceStates[BehaviorSource.APP_USAGE] == BehaviorSourceState.AVAILABLE) {
            screenTimeChange(ordered)?.let(insights::add)
            appConcentration(ordered)?.let(insights::add)
            lateUsage(ordered)?.let(insights::add)
            usageRegularity(ordered)?.let(insights::add)
        }
        if (sourceStates[BehaviorSource.ACTIVITY] == BehaviorSourceState.AVAILABLE) {
            activityConsistency(ordered)?.let(insights::add)
            activityChange(ordered)?.let(insights::add)
        }
        return insights.ifEmpty {
            listOf(
                BehaviorInsight(
                    BehaviorInsightCategory.INSUFFICIENT_HISTORY,
                    BehaviorEvidenceTier.DEVELOPING,
                    copyTokens = listOf("building_baseline")
                )
            )
        }
    }

    private fun screenTimeChange(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val measured = days.mapNotNull { d -> d.totalForegroundMillis?.let { d.epochDay to it } }
        if (measured.size < 6) return null
        val split = measured.size / 2
        val baseline = median(measured.take(split).map { it.second })
        val recent = median(measured.drop(split).map { it.second })
        if (baseline <= 0L || abs(recent - baseline).toDouble() / baseline < CHANGE_RATIO) return null
        return BehaviorInsight(
            BehaviorInsightCategory.SCREEN_TIME_CHANGE,
            tier(measured.size),
            listOf(baseline, recent),
            listOf(if (recent > baseline) "screen_time_higher" else "screen_time_lower")
        )
    }

    private fun activityChange(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val measured = days.mapNotNull { d -> d.steps?.let { d.epochDay to it } }
        if (measured.size < 6) return null
        val split = measured.size / 2
        val baseline = median(measured.take(split).map { it.second })
        val recent = median(measured.drop(split).map { it.second })
        if (baseline <= 0L || abs(recent - baseline).toDouble() / baseline < CHANGE_RATIO) return null
        return BehaviorInsight(
            BehaviorInsightCategory.ACTIVITY_CHANGE,
            tier(measured.size),
            listOf(baseline, recent),
            listOf(if (recent > baseline) "activity_higher" else "activity_lower")
        )
    }

    private fun appConcentration(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val shares = days.mapNotNull { day ->
            val total = day.totalForegroundMillis?.takeIf { it > 0L } ?: return@mapNotNull null
            val top = day.topApps.take(2).sumOf { it.foregroundMillis }
            top.toDouble() / total
        }
        if (shares.size < MIN_PATTERN_DAYS || shares.count { it >= CONCENTRATION_SHARE } < MIN_PATTERN_DAYS) return null
        return BehaviorInsight(
            BehaviorInsightCategory.APP_CONCENTRATION,
            tier(shares.size),
            supportingValues = listOf((medianDouble(shares) * 100).toLong()),
            copyTokens = listOf("usage_concentrated")
        )
    }

    private fun lateUsage(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val shares = days.mapNotNull { day ->
            val total = day.daypartUsage.totalMillis.takeIf { it > 0L } ?: return@mapNotNull null
            day.daypartUsage.nightMillis.toDouble() / total
        }
        if (shares.size < MIN_PATTERN_DAYS || shares.count { it >= LATE_SHARE } < MIN_PATTERN_DAYS) return null
        return BehaviorInsight(
            BehaviorInsightCategory.LATE_USAGE_PATTERN,
            tier(shares.size),
            listOf((medianDouble(shares) * 100).toLong()),
            listOf("late_usage_pattern")
        )
    }

    private fun usageRegularity(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val values = days.mapNotNull { it.totalForegroundMillis }
        if (values.size < MIN_PATTERN_DAYS) return null
        val median = median(values)
        if (median <= 0L) return null
        val medianDeviation = median(values.map { abs(it - median) })
        if (medianDeviation.toDouble() / median > 0.20) return null
        return BehaviorInsight(
            BehaviorInsightCategory.USAGE_REGULARITY,
            tier(values.size),
            listOf(median),
            listOf("usage_regular")
        )
    }

    private fun activityConsistency(days: List<DailyBehaviorAggregate>): BehaviorInsight? {
        val values = days.mapNotNull { it.steps }
        if (values.size < MIN_PATTERN_DAYS) return null
        val median = median(values)
        if (median <= 0L) return null
        val medianDeviation = median(values.map { abs(it - median) })
        if (medianDeviation.toDouble() / median > 0.25) return null
        return BehaviorInsight(
            BehaviorInsightCategory.ACTIVITY_CONSISTENCY,
            tier(values.size),
            listOf(median),
            listOf("activity_consistent")
        )
    }

    private fun median(values: List<Long>): Long {
        if (values.isEmpty()) return 0L
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[middle]
        else (sorted[middle - 1] / 2L) + (sorted[middle] / 2L) + ((sorted[middle - 1] % 2L + sorted[middle] % 2L) / 2L)
    }

    private fun medianDouble(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2.0
    }

    private fun tier(count: Int) = when {
        count >= 14 -> BehaviorEvidenceTier.ESTABLISHED
        count >= 7 -> BehaviorEvidenceTier.DEVELOPING
        else -> BehaviorEvidenceTier.EARLY
    }
}
