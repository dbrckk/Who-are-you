package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellNavigationTest {
    @Test
    fun shellTabsMapToPersistentDestinations() {
        assertEquals(AppScreen.DISCOVER, AppShellNavigation.destination(AppShellTab.DISCOVER))
        assertEquals(AppScreen.PROFILE, AppShellNavigation.destination(AppShellTab.PROFILE))
    }

    @Test
    fun discoverAndProfileExposeShell() {
        assertEquals(AppShellTab.DISCOVER, AppShellNavigation.tabFor(AppScreen.DISCOVER))
        assertEquals(AppShellTab.PROFILE, AppShellNavigation.tabFor(AppScreen.PROFILE))
        assertTrue(AppShellNavigation.isShellVisible(AppScreen.DISCOVER))
        assertTrue(AppShellNavigation.isShellVisible(AppScreen.PROFILE))
    }

    @Test
    fun focusedQuizFlowsHideShell() {
        assertNull(AppShellNavigation.tabFor(AppScreen.QUIZ))
        assertNull(AppShellNavigation.tabFor(AppScreen.RESULT))
        assertFalse(AppShellNavigation.isShellVisible(AppScreen.QUIZ))
        assertFalse(AppShellNavigation.isShellVisible(AppScreen.RESULT))
    }
}
