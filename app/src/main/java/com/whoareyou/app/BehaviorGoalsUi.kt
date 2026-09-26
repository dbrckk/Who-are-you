package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.util.concurrent.TimeUnit

data class BehaviorGoalCreateRequest(
    val metric: BehaviorGoalMetric,
    val targetValue: Long,
    val packageName: String? = null
)

@Composable
fun BehaviorGoalsSection(
    goals: List<BehaviorGoalUiModel>,
    availableApps: List<BehaviorAppUsageUi>,
    onCreate: (BehaviorGoalCreateRequest) -> Unit,
    onEdit: (String, BehaviorGoalCreateRequest) -> Unit = { _, _ -> },
    onSetPaused: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreate by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<BehaviorGoalUiModel?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("behavior_goals_section"),
        verticalArrangement = Arrangement.spacedBy(V2Spacing.Compact)
    ) {
        Text(
            stringResource(R.string.goals_title),
            color = V2Colors.AccentCyan,
            style = V2Type.Eyebrow,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            stringResource(R.string.goals_user_defined),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting,
            modifier = Modifier.testTag("behavior_goals_user_defined")
        )

        goals.forEach { goal ->
            BehaviorGoalCard(
                goal = goal,
                onEdit = { editingGoal = goal },
                onSetPaused = onSetPaused,
                onDelete = { pendingDeleteId = goal.id }
            )
        }

        Button(
            onClick = { showCreate = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("behavior_goal_create"),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.Orchid)
        ) {
            Text(stringResource(R.string.goals_create))
        }
    }

    if (showCreate) {
        BehaviorGoalEditorDialog(
            initial = null,
            availableApps = availableApps,
            onDismiss = { showCreate = false },
            onSubmit = {
                showCreate = false
                onCreate(it)
            }
        )
    }

    editingGoal?.let { goal ->
        BehaviorGoalEditorDialog(
            initial = goal.toCreateRequest(),
            availableApps = availableApps,
            onDismiss = { editingGoal = null },
            onSubmit = { request ->
                editingGoal = null
                onEdit(goal.id, request)
            }
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.goals_delete)) },
            text = { Text(stringResource(R.string.goals_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId = null
                        onDelete(id)
                    },
                    modifier = Modifier.testTag("behavior_goal_delete_confirm")
                ) {
                    Text(stringResource(R.string.goals_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun BehaviorGoalCard(
    goal: BehaviorGoalUiModel,
    onEdit: () -> Unit,
    onSetPaused: (String, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val tagId = goal.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("behavior_goal_" + tagId),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Column(
            Modifier.padding(V2Spacing.Card),
            verticalArrangement = Arrangement.spacedBy(V2Spacing.Compact)
        ) {
            Text(
                behaviorGoalCopy(goal.metricCopy),
                color = V2Colors.TextPrimary,
                style = V2Type.SectionTitle,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                behaviorGoalCopy(goal.statusCopy),
                color = V2Colors.Orchid,
                style = V2Type.Caption
            )
            goal.packageName?.let {
                Text(it, color = V2Colors.TextSecondary, style = V2Type.Caption)
            }
            Text(
                stringResource(
                    R.string.goals_target_format,
                    behaviorGoalTarget(goal)
                ),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong
            )
            Text(
                stringResource(
                    R.string.goals_progress_format,
                    goal.observedDays,
                    goal.metDays
                ),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                modifier = Modifier.testTag("behavior_goal_" + tagId + "_progress")
            )
            Text(
                stringResource(R.string.goals_days_remaining, goal.remainingDays),
                color = V2Colors.TextSecondary,
                style = V2Type.Caption
            )
            if (goal.hasMissingEvidence) {
                Text(
                    stringResource(R.string.goals_missing_evidence),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Supporting
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(V2Spacing.Compact)) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("behavior_goal_" + tagId + "_edit")
                ) {
                    Text(stringResource(R.string.goals_edit))
                }
                if (goal.statusCopy != BehaviorGoalCopyKey.STATUS_COMPLETED) {
                    val paused = goal.statusCopy == BehaviorGoalCopyKey.STATUS_PAUSED
                    TextButton(
                        onClick = { onSetPaused(goal.id, !paused) },
                        modifier = Modifier.testTag("behavior_goal_" + tagId + "_pause")
                    ) {
                        Text(
                            stringResource(if (paused) R.string.goals_resume else R.string.goals_pause)
                        )
                    }
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("behavior_goal_" + tagId + "_delete")
                ) {
                    Text(stringResource(R.string.goals_delete), color = V2Colors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BehaviorGoalEditorDialog(
    initial: BehaviorGoalCreateRequest?,
    availableApps: List<BehaviorAppUsageUi>,
    onDismiss: () -> Unit,
    onSubmit: (BehaviorGoalCreateRequest) -> Unit
) {
    var metric by remember(initial) {
        mutableStateOf(initial?.metric ?: BehaviorGoalMetric.STEPS_AT_LEAST)
    }
    var targetText by remember(initial) {
        mutableStateOf(initial?.targetText().orEmpty())
    }
    var selectedPackage by remember(initial) {
        mutableStateOf(initial?.packageName)
    }

    val numericTarget = targetText.toLongOrNull()?.takeIf { it >= 0L }
    val targetValue = numericTarget?.let { value ->
        if (metric == BehaviorGoalMetric.STEPS_AT_LEAST) value
        else value * 60_000L
    }
    val canSubmit = targetValue != null &&
        (metric != BehaviorGoalMetric.APP_USAGE_AT_MOST || selectedPackage != null)
    val editing = initial != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (editing) R.string.goals_edit else R.string.goals_create))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(V2Spacing.Compact)) {
                Text(
                    stringResource(R.string.goals_user_defined),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Supporting
                )
                Text(
                    stringResource(R.string.goals_select_metric),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Caption
                )
                BehaviorGoalMetric.entries.forEach { option ->
                    TextButton(
                        onClick = {
                            metric = option
                            if (option != BehaviorGoalMetric.APP_USAGE_AT_MOST) {
                                selectedPackage = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("behavior_goal_metric_" + option.metricTag())
                    ) {
                        Text(
                            behaviorGoalMetricLabel(option),
                            color = if (metric == option) V2Colors.AccentCyan else V2Colors.TextSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { value ->
                        targetText = value.filter(Char::isDigit)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("behavior_goal_target_input"),
                    label = {
                        Text(
                            stringResource(
                                if (metric == BehaviorGoalMetric.STEPS_AT_LEAST) {
                                    R.string.goals_target_steps_input
                                } else {
                                    R.string.goals_target_minutes_input
                                }
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (metric == BehaviorGoalMetric.APP_USAGE_AT_MOST) {
                    Text(
                        stringResource(R.string.goals_select_app),
                        color = V2Colors.TextPrimary,
                        style = V2Type.Caption
                    )
                    if (availableApps.isEmpty()) {
                        Text(
                            stringResource(R.string.goals_missing_evidence),
                            color = V2Colors.TextSecondary,
                            style = V2Type.Supporting
                        )
                    } else {
                        availableApps.forEach { app ->
                            TextButton(
                                onClick = { selectedPackage = app.packageName },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("behavior_goal_app_" + app.packageName.safeTag())
                            ) {
                                Text(
                                    app.packageName,
                                    color = if (selectedPackage == app.packageName) {
                                        V2Colors.AccentCyan
                                    } else {
                                        V2Colors.TextSecondary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    val value = targetValue ?: return@TextButton
                    onSubmit(
                        BehaviorGoalCreateRequest(
                            metric = metric,
                            targetValue = value,
                            packageName = selectedPackage
                        )
                    )
                },
                modifier = Modifier.testTag(
                    if (editing) "behavior_goal_edit_confirm" else "behavior_goal_create_confirm"
                )
            ) {
                Text(stringResource(if (editing) R.string.goals_update else R.string.goals_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun BehaviorGoalCreateRequest.targetText(): String =
    if (metric == BehaviorGoalMetric.STEPS_AT_LEAST) {
        targetValue.toString()
    } else {
        TimeUnit.MILLISECONDS.toMinutes(targetValue).toString()
    }

private fun BehaviorGoalUiModel.toCreateRequest(): BehaviorGoalCreateRequest =
    BehaviorGoalCreateRequest(
        metric = when (metricCopy) {
            BehaviorGoalCopyKey.METRIC_STEPS -> BehaviorGoalMetric.STEPS_AT_LEAST
            BehaviorGoalCopyKey.METRIC_SCREEN_TIME -> BehaviorGoalMetric.SCREEN_TIME_AT_MOST
            BehaviorGoalCopyKey.METRIC_EVENING_USAGE -> BehaviorGoalMetric.EVENING_USAGE_AT_MOST
            BehaviorGoalCopyKey.METRIC_APP_USAGE -> BehaviorGoalMetric.APP_USAGE_AT_MOST
            else -> error("Goal metric copy expected")
        },
        targetValue = targetValue,
        packageName = packageName
    )

@Composable
private fun behaviorGoalMetricLabel(metric: BehaviorGoalMetric): String = stringResource(
    when (metric) {
        BehaviorGoalMetric.STEPS_AT_LEAST -> R.string.goals_metric_steps
        BehaviorGoalMetric.SCREEN_TIME_AT_MOST -> R.string.goals_metric_screen_time
        BehaviorGoalMetric.EVENING_USAGE_AT_MOST -> R.string.goals_metric_evening_usage
        BehaviorGoalMetric.APP_USAGE_AT_MOST -> R.string.goals_metric_app_usage
    }
)

private fun BehaviorGoalMetric.metricTag(): String = when (this) {
    BehaviorGoalMetric.STEPS_AT_LEAST -> "steps"
    BehaviorGoalMetric.SCREEN_TIME_AT_MOST -> "screen_time"
    BehaviorGoalMetric.EVENING_USAGE_AT_MOST -> "evening_usage"
    BehaviorGoalMetric.APP_USAGE_AT_MOST -> "app_usage"
}

private fun String.safeTag(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")

@Composable
private fun behaviorGoalTarget(goal: BehaviorGoalUiModel): String = when (goal.valueKind) {
    BehaviorGoalValueKind.STEPS -> goal.targetValue.toString()
    BehaviorGoalValueKind.DURATION -> goalDurationLabel(goal.targetValue)
}

@Composable
private fun behaviorGoalCopy(key: BehaviorGoalCopyKey): String = stringResource(
    when (key) {
        BehaviorGoalCopyKey.METRIC_STEPS -> R.string.goals_metric_steps
        BehaviorGoalCopyKey.METRIC_SCREEN_TIME -> R.string.goals_metric_screen_time
        BehaviorGoalCopyKey.METRIC_EVENING_USAGE -> R.string.goals_metric_evening_usage
        BehaviorGoalCopyKey.METRIC_APP_USAGE -> R.string.goals_metric_app_usage
        BehaviorGoalCopyKey.STATUS_ACTIVE -> R.string.goals_status_active
        BehaviorGoalCopyKey.STATUS_PAUSED -> R.string.goals_status_paused
        BehaviorGoalCopyKey.STATUS_COMPLETED -> R.string.goals_status_completed
        BehaviorGoalCopyKey.USER_DEFINED_TARGET -> R.string.goals_your_target
        BehaviorGoalCopyKey.OBSERVED_DAYS -> R.string.goals_observed_days
        BehaviorGoalCopyKey.TARGET_MET_DAYS -> R.string.goals_target_met_days
        BehaviorGoalCopyKey.MISSING_EVIDENCE -> R.string.goals_missing_evidence
    }
)

private fun goalDurationLabel(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.coerceAtLeast(0L))
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (hours > 0) hours.toString() + "h " + remainder + "m" else remainder.toString() + "m"
}
