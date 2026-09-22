package com.whoareyou.app

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ResultScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun resultShowsScoreAndExposesRetryAndDoneActions() {
        var retryCount = 0
        var doneCount = 0

        composeRule.setContent {
            ResultScreen(
                quiz = testQuiz(),
                score = 75,
                previousScore = null,
                completedCount = 4,
                totalQuizCount = 30,
                catalog = listOf(testQuiz()),
                completed = setOf("result-ui-test"),
                evidence = listOf(ResultEvidence(0, "Question?", "D", 3)),
                coverage = ProfileCoverage(
                    knownTraitCount = 0,
                    totalTraitCount = 0,
                    coveragePercent = 0,
                    averageConfidence = 0,
                    strongTraitCount = 0,
                    uncertainTraitCount = 0,
                    traits = emptyList()
                ),
                onQuizSelected = { },
                onDone = { doneCount++ },
                onRetry = { retryCount++ }
            )
        }

        composeRule.onNodeWithTag("result_score").assertTextEquals("75%")
        composeRule.onNodeWithTag("result_evidence").performScrollTo()
        composeRule.onNodeWithTag("result_strengths_watchouts").performScrollTo()
        composeRule.onNodeWithTag("result_everyday_life").performScrollTo()
        composeRule.onNodeWithTag("result_reflection").performScrollTo()
        composeRule.onNodeWithTag("result_retry").performScrollTo().performClick()
        composeRule.onNodeWithTag("result_done").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryCount)
            assertEquals(1, doneCount)
        }
    }

    private fun testQuiz() = Quiz(
        id = "result-ui-test",
        title = "Result UI Test",
        hook = "Test hook",
        time = "1 min",
        accent = "cyan",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low description",
        midDescription = "Mid description",
        highDescription = "High description",
        metricLow = "Reserved",
        metricHigh = "Expressive",
        resultIntelligence = QuizResultIntelligenceContent(
            low = ResultInsightContent(listOf("Low strength"), listOf("Low watch"), listOf("Low life"), "Low reflection"),
            balanced = ResultInsightContent(listOf("Balanced strength"), listOf("Balanced watch"), listOf("Balanced life"), "Balanced reflection"),
            high = ResultInsightContent(listOf("High strength"), listOf("High watch"), listOf("High life"), "High reflection")
        ),
        questions = listOf(
            Question(
                text = "Question?",
                answers = listOf(
                    Answer("A", 0),
                    Answer("B", 1),
                    Answer("C", 2),
                    Answer("D", 3)
                )
            )
        )
    )
}
