package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainFlowE2eTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingQuizPersistenceRestartAndProfileFlow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        runBlocking {
            ProfileStore.setOnboardingComplete(context, false)
            ProfileStore.setAdsRemoved(context, false)
        }
        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("onboarding_start").assertExists().performClick()
        waitForTag("app_screen_discover")

        val catalog = QuizRepository.load(context)
        val storedBeforeQuiz = runBlocking { ProfileStore.observe(context).first { it.onboardingComplete } }
        val summary = GlobalProfileEngine.build(catalog, storedBeforeQuiz.latestScores, storedBeforeQuiz.previousScores)
        val nextQuiz = requireNotNull(
            DiscoverPersonalization.recommendation(catalog, storedBeforeQuiz.completedQuizIds, summary.dimensions)?.quiz
        )
        val expectedScore = Scoring.quizPercent(
            nextQuiz.questions.sumOf { question -> question.answers.first().score },
            nextQuiz.questions.size
        )

        openRecommendedQuiz()
        waitForTag("app_screen_quiz")

        nextQuiz.questions.indices.forEach { index ->
            waitForTag("quiz_question_${index + 1}")
            composeRule.onNodeWithTag("quiz_answer_0").assertExists().performClick()
        }

        waitForTag("app_screen_result")
        composeRule.onNodeWithTag("result_score").assertExists()

        // Result navigation is intentionally downstream of the DataStore commit. If the
        // result screen exists, completion must already be durable without polling/retries.
        val persisted = runBlocking { ProfileStore.observe(context).first() }
        assertTrue(nextQuiz.id in persisted.completedQuizIds)
        assertEquals(expectedScore, persisted.latestScores[nextQuiz.id])

        composeRule.onNodeWithTag("result_done").performScrollTo().performClick()
        waitForTag("app_screen_discover")

        runBlocking { ProfileStore.setAdsRemoved(context, true) }
        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_discover")

        val afterRestart = runBlocking {
            withTimeout(5_000) {
                ProfileStore.observe(context).first {
                    it.onboardingComplete && it.adsRemoved && it.latestScores[nextQuiz.id] == expectedScore
                }
            }
        }
        assertTrue(afterRestart.adsRemoved)
        assertEquals(expectedScore, afterRestart.latestScores[nextQuiz.id])

        val tabMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        composeRule.onAllNodes(tabMatcher)[1].performClick()
        waitForTag("app_screen_profile")

        pressSystemBack()
        waitForTag("app_screen_discover")
    }

    @Test
    fun interruptedQuizKeepsQuestionAcrossRecreationAndBackReturnsDiscover() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { ProfileStore.setOnboardingComplete(context, true) }
        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_discover")

        openRecommendedQuiz()
        waitForTag("app_screen_quiz")
        waitForTag("quiz_question_1")

        composeRule.onNodeWithTag("quiz_answer_0").performClick()
        waitForTag("quiz_question_2")

        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_quiz")
        waitForTag("quiz_question_2")
        composeRule.waitForIdle()

        pressSystemBack()
        waitForTag("app_screen_discover")
    }

    @Test
    fun resultCommitSurvivesImmediateRecreationAndRetryPreservesScoreHistory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { ProfileStore.setOnboardingComplete(context, true) }
        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_discover")

        val catalog = QuizRepository.load(context)
        val stored = runBlocking { ProfileStore.observe(context).first { it.onboardingComplete } }
        val summary = GlobalProfileEngine.build(catalog, stored.latestScores, stored.previousScores)
        val quiz = requireNotNull(
            DiscoverPersonalization.recommendation(catalog, stored.completedQuizIds, summary.dimensions)?.quiz
        )
        val firstScore = Scoring.quizPercent(
            quiz.questions.sumOf { it.answers.first().score },
            quiz.questions.size
        )
        val retryAnswerIndices = quiz.questions.map { question -> if (question.answers.size > 1) 1 else 0 }
        val retryScore = Scoring.quizPercent(
            quiz.questions.mapIndexed { index, question -> question.answers[retryAnswerIndices[index]].score }.sum(),
            quiz.questions.size
        )

        openRecommendedQuiz()
        waitForTag("app_screen_quiz")
        quiz.questions.indices.forEach { index ->
            waitForTag("quiz_question_${index + 1}")
            composeRule.onNodeWithTag("quiz_answer_0").assertExists().performClick()
        }

        // Recreate immediately rather than waiting for Result. This exercises the handoff
        // where a final answer has been accepted but its durable commit may still be running.
        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_result")
        composeRule.onNodeWithTag("result_score").assertExists()

        composeRule.activityRule.scenario.recreate()
        waitForTag("app_screen_result")
        composeRule.onNodeWithTag("result_score").assertExists()

        val afterFirstAttempt = runBlocking { ProfileStore.observe(context).first() }
        assertEquals(firstScore, afterFirstAttempt.latestScores[quiz.id])

        composeRule.onNodeWithTag("result_retry").performScrollTo().performClick()
        waitForTag("app_screen_quiz")
        waitForTag("quiz_question_1")

        quiz.questions.indices.forEach { index ->
            waitForTag("quiz_question_${index + 1}")
            composeRule.onNodeWithTag("quiz_answer_${retryAnswerIndices[index]}").assertExists().performClick()
        }

        waitForTag("app_screen_result")
        val afterRetry = runBlocking { ProfileStore.observe(context).first() }
        assertEquals(retryScore, afterRetry.latestScores[quiz.id])
        assertEquals(firstScore, afterRetry.previousScores[quiz.id])

        pressSystemBack()
        waitForTag("app_screen_discover")
    }

    private fun openRecommendedQuiz() {
        composeRule.onNodeWithTag("discover_next_quiz")
            .assertExists()
            .performScrollTo()
            .performClick()
    }

    private fun pressSystemBack() {
        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasTestTag(tag), useUnmergedTree = true).assertExists()
    }
}
