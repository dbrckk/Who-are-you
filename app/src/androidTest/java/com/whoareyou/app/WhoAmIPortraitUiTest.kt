package com.whoareyou.app

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class WhoAmIPortraitUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun portraitCardsRenderFivePartHierarchyAndSafeLabels() {
        val trait = PersonalTrait(
            traitId = "curiosity",
            score = 78,
            confidence = 72,
            certainty = PersonalCertainty.ESTABLISHED,
            stability = PersonalStability.STABLE,
            contradictionLevel = ContradictionLevel.NONE,
            evidenceCount = 3,
            sourceQuizIds = listOf("q1", "q2", "q3"),
            trend = PersonalTrend.RISING,
            isDistinctive = true
        )
        val portrait = WhoAmIPortrait(
            headlineTraits = listOf(trait),
            stableTraits = listOf(trait),
            nuancedTraits = listOf(trait.copy(stability = PersonalStability.VARIABLE)),
            discoveryGaps = listOf(
                TraitDomainCoverage(
                    domain = TraitDomain.THINKING,
                    knownCount = 1,
                    totalCount = 4,
                    strongCount = 0,
                    coveragePercent = 25
                )
            ),
            evolvingTraits = listOf(trait),
            isDiscoveryState = false
        )

        composeRule.setContent {
            WhoAreYouTheme {
                Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    WhoAmIPortraitCards(portrait)
                }
            }
        }

        composeRule.onNodeWithTag("who_am_i_portrait").assertIsDisplayed()
        composeRule.onNodeWithTag("who_am_i_stable").assertIsDisplayed()
        composeRule.onNodeWithTag("who_am_i_nuances").assertIsDisplayed()
        composeRule.onNodeWithTag("who_am_i_discovery").assertIsDisplayed()
        composeRule.onNodeWithTag("who_am_i_evolution").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("who_am_i_trait_curiosity").assertCountEquals(4)
        composeRule.onNodeWithText("Thinking style").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("This signal has been moving upward over time.").performScrollTo().assertIsDisplayed()
    }
}
