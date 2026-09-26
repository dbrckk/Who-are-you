package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Rule

@OptIn(ExperimentalTestApi::class)
class MainActivityRecreationTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun quizProgressSurvivesActivityRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previousOnboardingComplete = runBlocking {
            ProfileStore.observe(context).first().onboardingComplete.also {
                ProfileStore.setOnboardingComplete(context, true)
            }
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForTag("app_screen_discover")
            composeRule.onNodeWithTag("discover_list")
                .performScrollToNode(hasTestTag("discover_next_quiz"))
            composeRule.onNodeWithTag("discover_next_quiz").assertIsDisplayed().performClick()
            waitForTag("app_screen_quiz")
            waitForTag("quiz_question_1")
            composeRule.onNodeWithTag("quiz_answer_0").performClick()
            waitForTag("quiz_question_2")

            scenario.recreate()
            waitForTag("app_screen_quiz")
            waitForTag("quiz_question_2")
        } finally {
            scenario.close()
            runBlocking {
                ProfileStore.setOnboardingComplete(context, previousOnboardingComplete)
            }
        }
    }

    @Test
    fun behaviorGoalSurvivesActivityRecreationWithoutDuplication() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previousOnboardingComplete = runBlocking {
            ProfileStore.observe(context).first().onboardingComplete.also {
                ProfileStore.setOnboardingComplete(context, true)
                BehaviorGoalRepository.clearAll(context)
            }
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForTag("app_screen_discover")
            val tabMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
            composeRule.onAllNodes(tabMatcher)[1].performClick()
            waitForTag("app_screen_profile")
            composeRule.onNodeWithTag("profile_open_habits").performScrollTo().performClick()
            waitForTag("app_screen_habits")

            composeRule.onNodeWithTag("behavior_screen")
                .performScrollToNode(hasTestTag("behavior_goal_create"))
            composeRule.onNodeWithTag("behavior_goal_create").performClick()
            composeRule.onNodeWithTag("behavior_goal_target_input").performTextInput("9000")
            composeRule.onNodeWithTag("behavior_goal_create_confirm").performClick()

            val persistedGoal = runBlocking {
                withTimeout(5_000) {
                    BehaviorGoalRepository.observe(context).first { it.size == 1 }.single()
                }
            }
            assertEquals(9_000L, persistedGoal.targetValue)

            scenario.recreate()
            waitForTag("app_screen_habits")
            composeRule.onNodeWithTag("behavior_screen")
                .performScrollToNode(hasTestTag("behavior_goal_${persistedGoal.id}"))
            composeRule.onNodeWithTag("behavior_goal_${persistedGoal.id}").assertIsDisplayed()

            val afterRecreation = runBlocking {
                withTimeout(5_000) {
                    BehaviorGoalRepository.observe(context).first { goals ->
                        goals.any { it.id == persistedGoal.id }
                    }
                }
            }
            assertEquals(1, afterRecreation.count { it.id == persistedGoal.id })
        } finally {
            scenario.close()
            runBlocking {
                BehaviorGoalRepository.clearAll(context)
                ProfileStore.setOnboardingComplete(context, previousOnboardingComplete)
            }
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
    }
}
