package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun answeringEveryQuestionAdvancesAndReturnsFinalScore() {
        var finalScore: Int? = null
        val quiz = testQuiz()

        composeRule.setContent {
            QuizScreen(
                quiz = quiz,
                attemptKey = 0,
                onBack = {},
                onFinished = { finalScore = it }
            )
        }

        composeRule.onNodeWithText("Question one?").assertIsDisplayed()
        composeRule.onNodeWithText("First high answer").performClick()

        composeRule.onNodeWithText("Question two?").assertIsDisplayed()
        composeRule.onNodeWithText("Second high answer").performClick()

        composeRule.runOnIdle {
            assertEquals(100, finalScore)
        }
    }

    private fun testQuiz() = Quiz(
        id = "ui-test-quiz",
        title = "UI Test Quiz",
        hook = "Test hook",
        time = "1 min",
        accent = "violet",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low description",
        midDescription = "Mid description",
        highDescription = "High description",
        metricLow = "Low metric",
        metricHigh = "High metric",
        questions = listOf(
            Question(
                text = "Question one?",
                answers = listOf(
                    Answer("First high answer", 3),
                    Answer("First medium answer", 2),
                    Answer("First low answer", 1),
                    Answer("First zero answer", 0)
                )
            ),
            Question(
                text = "Question two?",
                answers = listOf(
                    Answer("Second high answer", 3),
                    Answer("Second medium answer", 2),
                    Answer("Second low answer", 1),
                    Answer("Second zero answer", 0)
                )
            )
        )
    )
}
