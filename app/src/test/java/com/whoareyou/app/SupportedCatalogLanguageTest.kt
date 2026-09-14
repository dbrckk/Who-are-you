package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SupportedCatalogLanguageTest {
    @Test
    fun frenchDeviceUsesFrench() {
        assertEquals("fr", supportedCatalogLanguage("fr"))
        assertEquals("fr", supportedCatalogLanguage("FR"))
    }

    @Test
    fun everyNonFrenchDeviceUsesEnglish() {
        listOf("en", "de", "es", "it", "pt", "ja", "zh", null).forEach { language ->
            assertEquals("en", supportedCatalogLanguage(language))
        }
    }
}
