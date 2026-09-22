package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ResultEvidenceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun evidenceCardShowsWhyResultAndTopEvidence() {
        composeRule.setContent {
            WhoAreYouTheme {
                ResultEvidenceCard(
                    evidence = listOf(
                        ResultEvidence(0, "I recharge alone", "Very true", 3),
                        ResultEvidence(1, "I seek novelty", "Sometimes", -2)
                    )
                )
            }
        }

        composeRule.onNodeWithTag("result_evidence").assertIsDisplayed()
        composeRule.onNodeWithText("I recharge alone").assertIsDisplayed()
        composeRule.onNodeWithText("Very true").assertIsDisplayed()
    }
}
