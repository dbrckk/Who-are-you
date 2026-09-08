package com.whoareyou.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppShellUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shellExposesTwoTabsAndRoutesSelection() {
        var selectedTab: AppShellTab? = null

        composeRule.setContent {
            PremiumAppShellBar(
                selected = AppShellTab.DISCOVER,
                onSelect = { selectedTab = it }
            )
        }

        val tabMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        val tabs = composeRule.onAllNodes(tabMatcher)
        tabs.assertCountEquals(2)
        tabs[0].assertIsSelected()
        tabs[1].performClick()

        composeRule.runOnIdle {
            assertEquals(AppShellTab.PROFILE, selectedTab)
        }
    }
}
