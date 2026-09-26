package com.whoareyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BehaviorScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val model = BehaviorUiModelFactory.build(
        BehaviorSnapshot.EMPTY.copy(
            insights = listOf(
                BehaviorInsight(
                    category = BehaviorInsightCategory.LATE_USAGE_PATTERN,
                    evidenceTier = BehaviorEvidenceTier.DEVELOPING
                )
            )
        )
    )

    @Test
    fun habitsScreenSeparatesFactsPatternsSuggestionsAndControls() {
        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorScreen(
                    model = model,
                    onBack = {},
                    onSourceAction = { _, _ -> },
                    onDeleteAll = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_privacy_copy").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_today").assertIsDisplayed()

        val screen = composeRule.onNodeWithTag("behavior_screen")
        screen.performScrollToNode(hasTestTag("behavior_7_days"))
        composeRule.onNodeWithTag("behavior_7_days").assertIsDisplayed()
        screen.performScrollToNode(hasTestTag("behavior_30_days"))
        composeRule.onNodeWithTag("behavior_30_days").assertIsDisplayed()
        screen.performScrollToNode(hasTestTag("behavior_patterns"))
        composeRule.onNodeWithTag("behavior_patterns").assertIsDisplayed()
        screen.performScrollToNode(hasTestTag("behavior_suggestions"))
        composeRule.onNodeWithTag("behavior_suggestions").assertIsDisplayed()
        screen.performScrollToNode(hasTestTag("behavior_delete_all"))
        composeRule.onNodeWithTag("behavior_delete_all").assertHasClickAction()
    }

    @Test
    fun compactLargeFontLayoutKeepsDataControlsReachable() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    WhoAreYouTheme {
                        BehaviorScreen(
                            model = model,
                            onBack = {},
                            onSourceAction = { _, _ -> },
                            onDeleteAll = {}
                        )
                    }
                }
            }
        }

        val screen = composeRule.onNodeWithTag("behavior_screen")
        screen.performScrollToNode(hasTestTag("behavior_delete_all"))
        composeRule.onNodeWithTag("behavior_delete_all")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun deleteAllRequiresExplicitConfirmation() {
        var deletes = 0

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorScreen(
                    model = model,
                    onBack = {},
                    onSourceAction = { _, _ -> },
                    onDeleteAll = { deletes += 1 }
                )
            }
        }

        val screen = composeRule.onNodeWithTag("behavior_screen")
        screen.performScrollToNode(hasTestTag("behavior_delete_all"))
        composeRule.onNodeWithTag("behavior_delete_all").performClick()

        composeRule.runOnIdle { assertEquals(0, deletes) }

        composeRule.onNodeWithTag("behavior_delete_confirm")
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle { assertEquals(1, deletes) }
    }

    @Test
    fun habitsScreenRendersGoalsAndRoutesGoalActions() {
        val goal = BehaviorGoalUiModel(
            id = "screen",
            metricCopy = BehaviorGoalCopyKey.METRIC_SCREEN_TIME,
            statusCopy = BehaviorGoalCopyKey.STATUS_ACTIVE,
            valueKind = BehaviorGoalValueKind.DURATION,
            targetValue = 60L * 60_000L,
            packageName = null,
            observedDays = 2,
            metDays = 1,
            elapsedCompletedDays = 2,
            remainingDays = 5,
            hasMissingEvidence = false
        )
        var paused: Pair<String, Boolean>? = null

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorScreen(
                    model = model,
                    goals = listOf(goal),
                    onBack = {},
                    onSourceAction = { _, _ -> },
                    onDeleteAll = {},
                    onCreateGoal = {},
                    onSetGoalPaused = { id, value -> paused = id to value },
                    onDeleteGoal = {}
                )
            }
        }

        val screen = composeRule.onNodeWithTag("behavior_screen")
        screen.performScrollToNode(hasTestTag("behavior_goals_section"))
        composeRule.onNodeWithTag("behavior_goals_section").assertIsDisplayed()
        composeRule.onNodeWithTag("behavior_goal_screen_pause").performClick()

        composeRule.runOnIdle {
            assertEquals("screen" to true, paused)
        }
    }

    @Test
    fun sourceCtaRoutesTypedAction() {
        var selected: Pair<BehaviorSource, BehaviorSourceAction>? = null

        composeRule.setContent {
            WhoAreYouTheme {
                BehaviorScreen(
                    model = model,
                    onBack = {},
                    onSourceAction = { source, action -> selected = source to action },
                    onDeleteAll = {}
                )
            }
        }

        composeRule.onNodeWithTag("behavior_source_activity")
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                BehaviorSource.ACTIVITY to BehaviorSourceAction.ENABLE,
                selected
            )
        }
    }
}
