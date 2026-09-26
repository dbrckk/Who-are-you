package com.whoareyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BehaviorGoalsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val activeGoal = BehaviorGoalUiModel(
        id = "steps",
        metricCopy = BehaviorGoalCopyKey.METRIC_STEPS,
        statusCopy = BehaviorGoalCopyKey.STATUS_ACTIVE,
        valueKind = BehaviorGoalValueKind.STEPS,
        targetValue = 8_000L,
        packageName = null,
        observedDays = 3,
        metDays = 2,
        elapsedCompletedDays = 3,
        remainingDays = 4,
        hasMissingEvidence = false
    )

    @Test
    fun emptyStateExplainsUserDefinedTargetsAndOffersCreate() {
        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorGoalsSection(
                    goals = emptyList(),
                    availableApps = emptyList(),
                    onCreate = {},
                    onSetPaused = { _, _ -> },
                    onDelete = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_goals_section").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_goals_user_defined").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_goal_create").assertIsDisplayed()
    }

    @Test
    fun activeGoalShowsMeasuredProgressAndRoutesPause() {
        var paused: Pair<String, Boolean>? = null

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorGoalsSection(
                    goals = listOf(activeGoal),
                    availableApps = emptyList(),
                    onCreate = {},
                    onSetPaused = { id, value -> paused = id to value },
                    onDelete = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_goal_steps").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_goal_steps_progress")
            .assertTextContains("3")
            .assertTextContains("2")
        composeRule.onNodeWithTag("behavior_goal_steps_pause").performClick()

        composeRule.runOnIdle {
            assertEquals("steps" to true, paused)
        }
    }

    @Test
    fun createStepsGoalUsesExplicitUserValue() {
        var request: BehaviorGoalCreateRequest? = null

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorGoalsSection(
                    goals = emptyList(),
                    availableApps = emptyList(),
                    onCreate = { request = it },
                    onSetPaused = { _, _ -> },
                    onDelete = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_goal_create").performClick()
        composeRule.onNodeWithTag("behavior_goal_target_input").performTextInput("9000")
        composeRule.onNodeWithTag("behavior_goal_create_confirm").performClick()

        composeRule.runOnIdle {
            assertEquals(
                BehaviorGoalCreateRequest(
                    metric = BehaviorGoalMetric.STEPS_AT_LEAST,
                    targetValue = 9_000L
                ),
                request
            )
        }
    }

    @Test
    fun createScreenTimeGoalConvertsMinutesToMillis() {
        var request: BehaviorGoalCreateRequest? = null

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorGoalsSection(
                    goals = emptyList(),
                    availableApps = emptyList(),
                    onCreate = { request = it },
                    onSetPaused = { _, _ -> },
                    onDelete = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_goal_create").performClick()
        composeRule.onNodeWithTag("behavior_goal_metric_screen_time").performClick()
        composeRule.onNodeWithTag("behavior_goal_target_input").performTextInput("90")
        composeRule.onNodeWithTag("behavior_goal_create_confirm").performClick()

        composeRule.runOnIdle {
            assertEquals(
                BehaviorGoalCreateRequest(
                    metric = BehaviorGoalMetric.SCREEN_TIME_AT_MOST,
                    targetValue = 90L * 60_000L
                ),
                request
            )
        }
    }

    @Test
    fun deleteRequiresConfirmation() {
        var deleted = false

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorGoalsSection(
                    goals = listOf(activeGoal),
                    availableApps = emptyList(),
                    onCreate = {},
                    onSetPaused = { _, _ -> },
                    onDelete = { deleted = true }
                )
            }
        }

        composeRule.onNodeWithTag("behavior_goal_steps_delete").performClick()
        composeRule.runOnIdle { assertFalse(deleted) }
        composeRule.onNodeWithTag("behavior_goal_delete_confirm").performClick()
        composeRule.runOnIdle { assertTrue(deleted) }
    }

    @Test
    fun compactLargeFontLayoutKeepsCreateReachable() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    WhoAreYouTheme {
                        BehaviorGoalsSection(
                            goals = emptyList(),
                            availableApps = emptyList(),
                            onCreate = {},
                            onSetPaused = { _, _ -> },
                            onDelete = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("behavior_goal_create").assertIsDisplayed()
    }
}
