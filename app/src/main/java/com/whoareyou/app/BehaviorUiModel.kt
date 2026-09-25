package com.whoareyou.app

enum class BehaviorMetricKind {
    STEPS,
    SCREEN_TIME,
    SESSIONS,
    AVERAGE_STEPS,
    AVERAGE_SCREEN_TIME
}

enum class BehaviorSourceAction {
    ENABLE,
    AUTHORIZE,
    DISABLE,
    NONE
}

enum class BehaviorCopyKey {
    LOCAL_ONLY_BODY,
    SOURCE_ACTIVITY,
    SOURCE_APP_USAGE,
    SOURCE_ACTIVITY_DETAIL,
    SOURCE_APP_USAGE_DETAIL,
    SOURCE_DISABLED,
    SOURCE_PERMISSION_REQUIRED,
    SOURCE_AVAILABLE,
    SOURCE_UNSUPPORTED,
    SOURCE_ERROR,
    TODAY_EMPTY,
    HISTORY_BUILDING,
    PATTERN_ACTIVITY_CONSISTENT,
    PATTERN_ACTIVITY_CHANGE,
    PATTERN_SCREEN_TIME_CHANGE,
    PATTERN_APP_CONCENTRATION,
    PATTERN_LATE_USAGE,
    PATTERN_USAGE_REGULARITY,
    PATTERN_INSUFFICIENT_HISTORY,
    SUGGESTION_KEEP_PERSONAL_BASELINE,
    SUGGESTION_REVIEW_APP_BALANCE,
    SUGGESTION_REDUCE_EVENING_USE,
    SUGGESTION_REVIEW_RECENT_CHANGE
}

data class BehaviorMetricUi(
    val kind: BehaviorMetricKind,
    val value: Long
)

data class BehaviorPeriodUi(
    val metrics: List<BehaviorMetricUi>
)

data class BehaviorSourceUi(
    val source: BehaviorSource,
    val state: BehaviorSourceState,
    val title: BehaviorCopyKey,
    val detail: BehaviorCopyKey,
    val stateCopy: BehaviorCopyKey,
    val action: BehaviorSourceAction
)

data class BehaviorPatternUi(
    val category: BehaviorInsightCategory,
    val evidenceTier: BehaviorEvidenceTier,
    val copy: BehaviorCopyKey,
    val supportingValues: List<Long>
)

data class BehaviorSuggestionUi(
    val copy: BehaviorCopyKey
)

data class BehaviorUiModel(
    val privacyCopy: BehaviorCopyKey,
    val sources: List<BehaviorSourceUi>,
    val today: BehaviorPeriodUi,
    val last7Days: BehaviorPeriodUi,
    val last30Days: BehaviorPeriodUi,
    val patterns: List<BehaviorPatternUi>,
    val suggestions: List<BehaviorSuggestionUi>
)

object BehaviorUiModelFactory {
    fun build(snapshot: BehaviorSnapshot): BehaviorUiModel = BehaviorUiModel(
        privacyCopy = BehaviorCopyKey.LOCAL_ONLY_BODY,
        sources = BehaviorSource.entries.map { source ->
            val state = snapshot.sourceStates[source] ?: BehaviorSourceState.DISABLED
            BehaviorSourceUi(
                source = source,
                state = state,
                title = when (source) {
                    BehaviorSource.ACTIVITY -> BehaviorCopyKey.SOURCE_ACTIVITY
                    BehaviorSource.APP_USAGE -> BehaviorCopyKey.SOURCE_APP_USAGE
                },
                detail = when (source) {
                    BehaviorSource.ACTIVITY -> BehaviorCopyKey.SOURCE_ACTIVITY_DETAIL
                    BehaviorSource.APP_USAGE -> BehaviorCopyKey.SOURCE_APP_USAGE_DETAIL
                },
                stateCopy = state.copyKey(),
                action = state.action()
            )
        },
        today = BehaviorPeriodUi(
            metrics = buildList {
                snapshot.today?.steps?.let { add(BehaviorMetricUi(BehaviorMetricKind.STEPS, it)) }
                snapshot.today?.totalForegroundMillis?.let {
                    add(BehaviorMetricUi(BehaviorMetricKind.SCREEN_TIME, it))
                }
                snapshot.today?.launchesOrSessions?.let {
                    add(BehaviorMetricUi(BehaviorMetricKind.SESSIONS, it.toLong()))
                }
            }
        ),
        last7Days = history(snapshot.last7Days),
        last30Days = history(snapshot.last30Days),
        patterns = snapshot.insights.map { insight ->
            BehaviorPatternUi(
                category = insight.category,
                evidenceTier = insight.evidenceTier,
                copy = insight.category.copyKey(),
                supportingValues = insight.supportingValues
            )
        },
        suggestions = snapshot.insights
            .mapNotNull { it.category.suggestionKey() }
            .distinct()
            .map(::BehaviorSuggestionUi)
    )

    private fun history(days: List<DailyBehaviorAggregate>): BehaviorPeriodUi {
        val stepValues = days.mapNotNull { it.steps }
        val screenValues = days.mapNotNull { it.totalForegroundMillis }
        return BehaviorPeriodUi(
            metrics = buildList {
                average(stepValues)?.let {
                    add(BehaviorMetricUi(BehaviorMetricKind.AVERAGE_STEPS, it))
                }
                average(screenValues)?.let {
                    add(BehaviorMetricUi(BehaviorMetricKind.AVERAGE_SCREEN_TIME, it))
                }
            }
        )
    }

    private fun average(values: List<Long>): Long? {
        if (values.isEmpty()) return null
        return values.sum() / values.size
    }

    private fun BehaviorSourceState.copyKey(): BehaviorCopyKey = when (this) {
        BehaviorSourceState.DISABLED -> BehaviorCopyKey.SOURCE_DISABLED
        BehaviorSourceState.PERMISSION_REQUIRED -> BehaviorCopyKey.SOURCE_PERMISSION_REQUIRED
        BehaviorSourceState.AVAILABLE -> BehaviorCopyKey.SOURCE_AVAILABLE
        BehaviorSourceState.UNSUPPORTED -> BehaviorCopyKey.SOURCE_UNSUPPORTED
        BehaviorSourceState.ERROR -> BehaviorCopyKey.SOURCE_ERROR
    }

    private fun BehaviorSourceState.action(): BehaviorSourceAction = when (this) {
        BehaviorSourceState.DISABLED -> BehaviorSourceAction.ENABLE
        BehaviorSourceState.PERMISSION_REQUIRED -> BehaviorSourceAction.AUTHORIZE
        BehaviorSourceState.AVAILABLE -> BehaviorSourceAction.DISABLE
        BehaviorSourceState.UNSUPPORTED,
        BehaviorSourceState.ERROR -> BehaviorSourceAction.NONE
    }

    private fun BehaviorInsightCategory.suggestionKey(): BehaviorCopyKey? = when (this) {
        BehaviorInsightCategory.ACTIVITY_CONSISTENCY,
        BehaviorInsightCategory.USAGE_REGULARITY -> BehaviorCopyKey.SUGGESTION_KEEP_PERSONAL_BASELINE
        BehaviorInsightCategory.ACTIVITY_CHANGE,
        BehaviorInsightCategory.SCREEN_TIME_CHANGE -> BehaviorCopyKey.SUGGESTION_REVIEW_RECENT_CHANGE
        BehaviorInsightCategory.APP_CONCENTRATION -> BehaviorCopyKey.SUGGESTION_REVIEW_APP_BALANCE
        BehaviorInsightCategory.LATE_USAGE_PATTERN -> BehaviorCopyKey.SUGGESTION_REDUCE_EVENING_USE
        BehaviorInsightCategory.INSUFFICIENT_HISTORY -> null
    }

    private fun BehaviorInsightCategory.copyKey(): BehaviorCopyKey = when (this) {
        BehaviorInsightCategory.ACTIVITY_CONSISTENCY -> BehaviorCopyKey.PATTERN_ACTIVITY_CONSISTENT
        BehaviorInsightCategory.ACTIVITY_CHANGE -> BehaviorCopyKey.PATTERN_ACTIVITY_CHANGE
        BehaviorInsightCategory.SCREEN_TIME_CHANGE -> BehaviorCopyKey.PATTERN_SCREEN_TIME_CHANGE
        BehaviorInsightCategory.APP_CONCENTRATION -> BehaviorCopyKey.PATTERN_APP_CONCENTRATION
        BehaviorInsightCategory.LATE_USAGE_PATTERN -> BehaviorCopyKey.PATTERN_LATE_USAGE
        BehaviorInsightCategory.USAGE_REGULARITY -> BehaviorCopyKey.PATTERN_USAGE_REGULARITY
        BehaviorInsightCategory.INSUFFICIENT_HISTORY -> BehaviorCopyKey.PATTERN_INSUFFICIENT_HISTORY
    }
}
