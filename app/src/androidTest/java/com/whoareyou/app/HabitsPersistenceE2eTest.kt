package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HabitsPersistenceE2eTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun habitsScreenAndLocalGoalSurviveActivityRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val goalId = "e2e-persist-goal"
        val goalTag = "behavior_goal_$goalId"

        runBlocking {
            ProfileStore.setOnboardingComplete(context, true)
            BehaviorRepository.clearAll(context)
            BehaviorGoalRepository.clearAll(context)
            BehaviorGoalRepository.upsert(
                context,
                BehaviorGoal.stepsAtLeast(
                    id = goalId,
                    targetSteps = 5_000L,
                    startEpochDay = LocalDate.now().toEpochDay() - 1L
                )
            )
        }

        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_discover")

        val tabMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        composeRule.onAllNodes(tabMatcher)[1].performClick()
        waitForTag("app_screen_profile")

        composeRule.onNodeWithTag("profile_open_habits")
            .performScrollTo()
            .performClick()
        waitForTag("app_screen_habits")

        val habits = composeRule.onNodeWithTag("behavior_screen")
        habits.performScrollToNode(hasTestTag(goalTag))
        composeRule.onNodeWithTag(goalTag).assertExists()

        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_habits")

        composeRule.onNodeWithTag("behavior_screen")
            .performScrollToNode(hasTestTag(goalTag))
        composeRule.onNodeWithTag(goalTag).assertExists()

        val persisted = runBlocking {
            withTimeout(5_000) {
                BehaviorGoalRepository.observe(context).first { goals ->
                    goals.any { it.id == goalId }
                }
            }
        }
        assertTrue(persisted.any { it.id == goalId })

        runBlocking {
            BehaviorGoalRepository.clearAll(context)
            BehaviorRepository.clearAll(context)
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).assertExists()
    }
}
