package com.whoareyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertExists
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

        composeRule.onNodeWithTag("behavior_screen").assertExists()
        composeRule.onNodeWithTag("behavior_privacy_copy").assertExists()
        composeRule.onNodeWithTag("behavior_today").assertExists()

        val screen = composeRule.onNodeWithTag("behavior_screen")
        screen.performScrollToNode(hasTestTag("behavior_7_days"))
        composeRule.onNodeWithTag("behavior_7_days").assertExists()
        screen.performScrollToNode(hasTestTag("behavior_30_days"))
        composeRule.onNodeWithTag("behavior_30_days").assertExists()
        screen.performScrollToNode(hasTestTag("behavior_patterns"))
        composeRule.onNodeWithTag("behavior_patterns").assertExists()
        screen.performScrollToNode(hasTestTag("behavior_suggestions"))
        composeRule.onNodeWithTag("behavior_suggestions").assertExists()
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
            .assertExists()
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
