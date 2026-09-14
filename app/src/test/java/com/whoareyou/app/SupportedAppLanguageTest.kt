package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SupportedAppLanguageTest {
    @Test
    fun frenchSystemLocaleUsesFrench() {
        assertEquals("fr", supportedAppLanguage("fr"))
        assertEquals("fr", supportedAppLanguage("FR"))
    }

    @Test
    fun everyOtherSystemLocaleUsesEnglish() {
        listOf("en", "de", "es", "it", "pt", "ja", "zh", "ar", null).forEach { language ->
            assertEquals("en", supportedAppLanguage(language))
        }
    }
}
