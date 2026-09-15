package com.whoareyou.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
    }
}
