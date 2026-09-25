package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

@Composable
fun BehaviorScreen(
    model: BehaviorUiModel,
    onBack: () -> Unit,
    onSourceAction: (BehaviorSource, BehaviorSourceAction) -> Unit,
    onDeleteAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .readableContentWidth()
            .background(V2Colors.Ink)
            .statusBarsPadding()
            .padding(horizontal = V2Spacing.Screen)
            .testTag("behavior_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            AccessibleBackAction(onClick = onBack)
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.habits_title), color = V2Colors.Orchid, style = V2Type.Eyebrow)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.habits_local_only), color = V2Colors.TextSecondary, style = V2Type.Supporting)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                model.sources.forEach { source ->
                    BehaviorSourceCard(source, onSourceAction)
                }
            }
        }
        item { BehaviorPeriodCard(R.string.habits_today, model.today, emptyCopy = R.string.habits_empty_today) }
        item { BehaviorPeriodCard(R.string.habits_7_days, model.last7Days, emptyCopy = R.string.habits_history_building) }
        item { BehaviorPeriodCard(R.string.habits_30_days, model.last30Days, emptyCopy = R.string.habits_history_building) }
        item {
            BehaviorPatternsCard(model.patterns)
        }
        if (model.suggestions.isNotEmpty()) {
            item {
                BehaviorSuggestionsCard(model.suggestions)
            }
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.habits_data_controls), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
                TextButton(onClick = onDeleteAll, modifier = Modifier.testTag("behavior_delete_all")) {
                    Text(stringResource(R.string.habits_delete_all), color = V2Colors.TextSecondary)
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun BehaviorSourceCard(
    source: BehaviorSourceUi,
    onAction: (BehaviorSource, BehaviorSourceAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Compact),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(behaviorCopy(source.title), color = V2Colors.TextPrimary, style = V2Type.SectionTitle)
                Spacer(Modifier.height(3.dp))
                Text(behaviorCopy(source.stateCopy), color = V2Colors.TextSecondary, style = V2Type.Supporting)
            }
            if (source.action != BehaviorSourceAction.NONE) {
                Button(
                    onClick = { onAction(source.source, source.action) },
                    colors = ButtonDefaults.buttonColors(containerColor = V2Colors.Orchid),
                    modifier = Modifier.testTag("behavior_source_${source.source.name.lowercase()}")
                ) {
                    Text(behaviorAction(source.action))
                }
            }
        }
    }
}

@Composable
private fun BehaviorPeriodCard(title: Int, period: BehaviorPeriodUi, emptyCopy: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
            if (period.metrics.isEmpty()) {
                Text(stringResource(emptyCopy), color = V2Colors.TextSecondary, style = V2Type.Supporting)
            } else {
                period.metrics.forEach { metric ->
                    Text(behaviorMetric(metric), color = V2Colors.TextPrimary, style = V2Type.SectionTitle)
                }
            }
        }
    }
}

@Composable
private fun BehaviorPatternsCard(patterns: List<BehaviorPatternUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.habits_patterns), color = V2Colors.Orchid, style = V2Type.Eyebrow)
            if (patterns.isEmpty()) {
                Text(stringResource(R.string.habits_history_building), color = V2Colors.TextSecondary, style = V2Type.Supporting)
            } else {
                patterns.forEach { pattern ->
                    Text(stringResource(R.string.habits_observed_label), color = V2Colors.AccentCyan, style = V2Type.Caption)
                    Text(behaviorCopy(pattern.copy), color = V2Colors.TextPrimary, style = V2Type.Supporting)
                }
            }
        }
    }
}

@Composable
private fun BehaviorSuggestionsCard(suggestions: List<BehaviorSuggestionUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.habits_suggestions), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
            suggestions.forEach { suggestion ->
                Text(stringResource(R.string.habits_suggested_label), color = V2Colors.Orchid, style = V2Type.Caption)
                Text(behaviorCopy(suggestion.copy), color = V2Colors.TextPrimary, style = V2Type.Supporting)
            }
        }
    }
}

@Composable
private fun behaviorMetric(metric: BehaviorMetricUi): String = when (metric.kind) {
    BehaviorMetricKind.STEPS -> stringResource(R.string.habits_steps, metric.value)
    BehaviorMetricKind.SCREEN_TIME -> stringResource(R.string.habits_screen_time, durationLabel(metric.value))
    BehaviorMetricKind.SESSIONS -> stringResource(R.string.habits_sessions, metric.value)
    BehaviorMetricKind.AVERAGE_STEPS -> stringResource(R.string.habits_average_steps, metric.value)
    BehaviorMetricKind.AVERAGE_SCREEN_TIME -> stringResource(R.string.habits_average_screen_time, durationLabel(metric.value))
}

private fun durationLabel(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.coerceAtLeast(0L))
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
}

@Composable
private fun behaviorAction(action: BehaviorSourceAction): String = when (action) {
    BehaviorSourceAction.ENABLE -> stringResource(R.string.habits_enable)
    BehaviorSourceAction.AUTHORIZE -> stringResource(R.string.habits_authorize)
    BehaviorSourceAction.DISABLE -> stringResource(R.string.habits_disable)
    BehaviorSourceAction.NONE -> ""
}

@Composable
private fun behaviorCopy(key: BehaviorCopyKey): String = stringResource(
    when (key) {
        BehaviorCopyKey.LOCAL_ONLY_BODY -> R.string.habits_local_only
        BehaviorCopyKey.SOURCE_ACTIVITY -> R.string.habits_source_activity
        BehaviorCopyKey.SOURCE_APP_USAGE -> R.string.habits_source_app_usage
        BehaviorCopyKey.SOURCE_DISABLED -> R.string.habits_source_disabled
        BehaviorCopyKey.SOURCE_PERMISSION_REQUIRED -> R.string.habits_source_permission_required
        BehaviorCopyKey.SOURCE_AVAILABLE -> R.string.habits_source_available
        BehaviorCopyKey.SOURCE_UNSUPPORTED -> R.string.habits_source_unsupported
        BehaviorCopyKey.SOURCE_ERROR -> R.string.habits_source_error
        BehaviorCopyKey.TODAY_EMPTY -> R.string.habits_empty_today
        BehaviorCopyKey.HISTORY_BUILDING -> R.string.habits_history_building
        BehaviorCopyKey.PATTERN_ACTIVITY_CONSISTENT -> R.string.habits_pattern_activity_consistent
        BehaviorCopyKey.PATTERN_ACTIVITY_CHANGE -> R.string.habits_pattern_activity_change
        BehaviorCopyKey.PATTERN_SCREEN_TIME_CHANGE -> R.string.habits_pattern_screen_time_change
        BehaviorCopyKey.PATTERN_APP_CONCENTRATION -> R.string.habits_pattern_app_concentration
        BehaviorCopyKey.PATTERN_LATE_USAGE -> R.string.habits_pattern_late_usage
        BehaviorCopyKey.PATTERN_USAGE_REGULARITY -> R.string.habits_pattern_usage_regular
        BehaviorCopyKey.PATTERN_INSUFFICIENT_HISTORY -> R.string.habits_pattern_insufficient
        BehaviorCopyKey.SUGGESTION_KEEP_PERSONAL_BASELINE -> R.string.habits_suggestion_keep_personal_baseline
        BehaviorCopyKey.SUGGESTION_REVIEW_APP_BALANCE -> R.string.habits_suggestion_review_app_balance
        BehaviorCopyKey.SUGGESTION_REDUCE_EVENING_USE -> R.string.habits_suggestion_reduce_evening_use
        BehaviorCopyKey.SUGGESTION_REVIEW_RECENT_CHANGE -> R.string.habits_suggestion_review_recent_change
    }
)
