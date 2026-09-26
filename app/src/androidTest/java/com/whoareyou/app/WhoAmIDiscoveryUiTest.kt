package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class WhoAmIDiscoveryUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun discoveryOwnsTheSingleNextQuizRecommendation() {
        val quiz = Quiz(
            id = "next",
            title = "Next quiz",
            hook = "Learn more",
            time = "2 min",
            accent = "#7C4DFF",
            lowTitle = "Low",
            midTitle = "Mid",
            highTitle = "High",
            lowDescription = "Low",
            midDescription = "Mid",
            highDescription = "High",
            metricLow = "Low",
            metricHigh = "High",
            questions = listOf(
                Question(
                    text = "Question",
                    answers = listOf(
                        Answer("A", 0),
                        Answer("B", 1),
                        Answer("C", 2),
                        Answer("D", 3)
                    )
                )
            ),
            traits = listOf(QuizTraitWeight("curiosity", 1.0))
        )
        val recommendation = NextQuizRecommendation(
            quizId = quiz.id,
            reason = NextQuizReason.NEW_COVERAGE,
            targetedTraitIds = listOf("curiosity"),
            informationGainScore = 1.0
        )

        composeRule.setContent {
            WhoAreYouTheme {
                WhoAmIPortraitCards(
                    portrait = WhoAmIPortraitEngine.build(PersonalModel.EMPTY),
                    recommendation = recommendation,
                    catalog = listOf(quiz),
                    onQuizSelected = {}
                )
            }
        }

        composeRule.onNodeWithTag("who_am_i_next_quiz").assertIsDisplayed()
    }
}
