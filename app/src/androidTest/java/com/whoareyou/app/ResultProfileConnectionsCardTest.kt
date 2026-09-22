package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class ResultProfileConnectionsCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileConnectionsCardRendersWhenConnectionsExist() {
        composeRule.setContent {
            WhoAreYouTheme {
                ResultProfileConnectionsCard(
                    connections = listOf(
                        ResultProfileConnection(
                            traitId = "focus",
                            kind = ResultProfileConnectionKind.REINFORCING,
                            traitScore = 82,
                            confidence = 80
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithTag("result_profile_connections").assertIsDisplayed()
    }
}
