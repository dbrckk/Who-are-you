package com.whoareyou.app

import android.content.Context
import android.content.res.Resources
import java.util.Locale

internal fun supportedAppLanguage(systemLanguage: String?): String =
    if (systemLanguage.equals("fr", ignoreCase = true)) "fr" else "en"

internal fun localizedAppContext(base: Context): Context {
    val systemLanguage = Resources.getSystem().configuration.locales[0]?.language
        ?: Locale.getDefault().language
    val locale = Locale.forLanguageTag(supportedAppLanguage(systemLanguage))
    val configuration = base.resources.configuration
    if (configuration.locales[0]?.language == locale.language) return base
    return base.createConfigurationContext(
        android.content.res.Configuration(configuration).apply { setLocale(locale) }
    )
}
