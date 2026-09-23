package com.whoareyou.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class ProfileScreenWhoAmITest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileScreenContainsWhoAmIPortraitAndDiscovery() {
        val trait = PersonalTrait(
            traitId = "curiosity",
            score = 78,
            confidence = 72,
            certainty = PersonalCertainty.ESTABLISHED,
            stability = PersonalStability.STABLE,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 3,
            sourceQuizIds = listOf("q1", "q2", "q3"),
            trend = PersonalTrend.STABLE,
            isDistinctive = true
        )
        val model = PersonalModel.EMPTY.copy(
            traits = listOf(trait),
            establishedTraits = listOf(trait),
            stableTraits = listOf(trait)
        )
        val summary = GlobalProfileSummary(
            dominantArchetype = "Explorer",
            completionPercent = 0,
            completedCount = 0,
            totalCount = 0,
            dimensions = emptyList(),
            personalModel = model
        )

        composeRule.setContent {
            WhoAreYouTheme {
                ProfileScreen(
                    summary = summary,
                    catalog = emptyList(),
                    onQuizSelected = {},
                    onBack = {},
                    onResetLocalData = {}
                )
            }
        }

        composeRule.onNodeWithTag("who_am_i_portrait").assertIsDisplayed()
        composeRule.onNodeWithTag("who_am_i_discovery").assertIsDisplayed()
    }
}
