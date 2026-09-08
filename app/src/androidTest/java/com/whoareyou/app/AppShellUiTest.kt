package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppShellUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shellExposesExactlyTwoTabsAndSelectedState() {
        var selectedTab: AppShellTab? = null

        composeRule.setContent {
            PremiumAppShellBar(
                selected = AppShellTab.DISCOVER,
                onSelect = { selectedTab = it }
            )
        }

        val tabMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        composeRule.onAllNodes(tabMatcher).assertCountEquals(2)
        composeRule.onAllNodes(tabMatcher)[0].assertIsSelected()

        composeRule.runOnIdle {
            assertEquals(null, selectedTab)
        }
    }
}
