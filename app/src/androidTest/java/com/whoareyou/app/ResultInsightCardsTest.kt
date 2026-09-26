package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ResultInsightCardsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun insightCardsShowAuthoredStrengthsWatchoutsEverydayLifeAndReflection() {
        composeRule.setContent {
            WhoAreYouTheme {
                ResultInsightCards(
                    insight = ResultInsightSummary(
                        direction = ResultDirection.HIGH,
                        strength = ResultSignalStrength.STRONG,
                        strengths = listOf("Clear strength"),
                        watchOuts = listOf("Watch this"),
                        everydayLife = listOf("Everyday example"),
                        reflection = "Try this reflection"
                    )
                )
            }
        }

        composeRule.onNodeWithTag("result_strengths_watchouts").assertIsDisplayed()
        composeRule.onNodeWithTag("result_everyday_life").assertIsDisplayed()
        composeRule.onNodeWithTag("result_reflection").assertIsDisplayed()
        composeRule.onNodeWithText("Clear strength").assertIsDisplayed()
        composeRule.onNodeWithText("Watch this").assertIsDisplayed()
        composeRule.onNodeWithText("Everyday example").assertIsDisplayed()
        composeRule.onNodeWithText("Try this reflection").assertIsDisplayed()
    }
}
