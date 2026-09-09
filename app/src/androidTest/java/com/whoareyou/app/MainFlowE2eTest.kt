package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
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

        composeRule.onNodeWithTag("discover_next_quiz").assertExists().performClick()
        waitForTag("app_screen_quiz")

        repeat(nextQuiz.questions.size) {
            composeRule.onNodeWithTag("quiz_answer_0").assertExists().performClick()
        }

        waitForTag("app_screen_result")
        composeRule.onNodeWithTag("result_score").assertExists()

        val persisted = runBlocking {
            withTimeout(5_000) {
                ProfileStore.observe(context).first { it.latestScores[nextQuiz.id] == expectedScore }
            }
        }
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
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasTestTag(tag), useUnmergedTree = true).assertExists()
    }
}
