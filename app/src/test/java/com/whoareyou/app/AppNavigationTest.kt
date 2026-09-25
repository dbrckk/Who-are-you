package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationTest {
    @Test
    fun discoverLetsSystemBackExit() {
        assertNull(AppNavigation.backDestination(AppScreen.DISCOVER))
    }

    @Test
    fun internalScreensReturnToDiscover() {
        assertEquals(AppScreen.DISCOVER, AppNavigation.backDestination(AppScreen.PROFILE))
        assertEquals(AppScreen.DISCOVER, AppNavigation.backDestination(AppScreen.QUIZ))
        assertEquals(AppScreen.DISCOVER, AppNavigation.backDestination(AppScreen.RESULT))
    }

    @Test
    fun emptyCatalogIsRejected() {
        assertFalse(AppNavigation.hasUsableCatalog(0))
        assertFalse(AppNavigation.hasUsableCatalog(-1))
    }

    @Test
    fun nonEmptyCatalogIsUsable() {
        assertTrue(AppNavigation.hasUsableCatalog(1))
        assertTrue(AppNavigation.hasUsableCatalog(30))
    }
}
