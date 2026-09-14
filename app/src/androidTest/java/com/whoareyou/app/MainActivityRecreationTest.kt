package com.whoareyou.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
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
        runBlocking {
            ProfileStore.setOnboardingComplete(context, true)
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            composeRule.onNodeWithTag("discover_next_quiz").performScrollTo().assertIsDisplayed().performClick()
            composeRule.onNodeWithTag("quiz_question_1").assertIsDisplayed()
            composeRule.onNodeWithTag("quiz_answer_0").performClick()
            composeRule.onNodeWithTag("quiz_question_2").assertIsDisplayed()

            scenario.recreate()
            composeRule.waitForIdle()

            composeRule.onNodeWithTag("quiz_question_2").assertIsDisplayed()
        } finally {
            scenario.close()
        }
    }
}
