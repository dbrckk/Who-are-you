package com.whoareyou.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
    fun persistedBehaviorGoalSurvivesActivityRecreationWithoutDuplication() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val persistedGoal = BehaviorGoal.stepsAtLeast(
            id = "recreation-goal",
            targetSteps = 9_000L,
            startEpochDay = 20_000L
        )
        val previousOnboardingComplete = runBlocking {
            ProfileStore.observe(context).first().onboardingComplete.also {
                ProfileStore.setOnboardingComplete(context, true)
                BehaviorGoalRepository.clearAll(context)
                BehaviorGoalRepository.upsert(context, persistedGoal)
            }
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForTag("app_screen_discover")
            openHabits(context)
            assertGoalDisplayed(persistedGoal)

            // Seed persistence before launch so recreation validates restoration itself without
            // leaving a numeric IME session active. The navigation route is also expected to
            // survive recreation, so wait for Habits directly instead of incorrectly assuming
            // that MainActivity resets to Discover.
            scenario.recreate()
            waitForTag("app_screen_habits")
            assertGoalDisplayed(persistedGoal)

            val afterRecreation = runBlocking {
                withTimeout(5_000) {
                    BehaviorGoalRepository.observe(context).first { goals ->
                        goals.any { it.id == persistedGoal.id }
                    }
                }
            }
            assertEquals(1, afterRecreation.size)
            assertEquals(persistedGoal, afterRecreation.single())
        } finally {
            scenario.close()
            runBlocking {
                BehaviorGoalRepository.clearAll(context)
                ProfileStore.setOnboardingComplete(context, previousOnboardingComplete)
            }
        }
    }

    private fun assertGoalDisplayed(goal: BehaviorGoal) {
        composeRule.onNodeWithTag("behavior_screen")
            .performScrollToNode(hasTestTag("behavior_goal_${goal.id}"))
        composeRule.onNodeWithTag("behavior_goal_${goal.id}").assertIsDisplayed()
    }

    private fun openHabits(context: android.content.Context) {
        composeRule.onNodeWithText(context.getString(R.string.shell_profile)).performClick()
        waitForTag("app_screen_profile")
        composeRule.onNodeWithTag("profile_open_habits").performScrollTo().performClick()
        waitForTag("app_screen_habits")
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
