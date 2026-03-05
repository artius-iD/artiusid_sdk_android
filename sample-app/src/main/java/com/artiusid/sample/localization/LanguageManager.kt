/*
 * File: LanguageManager.kt
 * Company: artius.iD, Inc.
 * Purpose: Runtime language selection and persistence (iOS LanguageManager parity).
 */

package com.artiusid.sample.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.ui.theme.ThemeManager
import java.util.Locale

/**
 * Manages app language at runtime. Persists selection and syncs with ArtiusIDSDK.setLanguage().
 * Supported: en, es, de, fr.
 */
object LanguageManager {

    private const val PREFS_NAME = "sample_app_prefs"
    private const val KEY_LANGUAGE = "app_language_code"

    private val supportedLanguages = listOf(
        "en" to "English",
        "es" to "Español",
        "de" to "Deutsch",
        "fr" to "Français"
    )

    fun getSupportedLanguages(): List<Pair<String, String>> = supportedLanguages

    fun getStoredLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
    }

    /**
     * Set app language: persist, sync SDK, and return a new Context with the locale applied.
     * Caller should use the returned context for resources, or recreate the activity so UI updates.
     */
    fun setLanguage(context: Context, languageCode: String): Context {
        val code = if (languageCode in supportedLanguages.map { it.first }) languageCode else "en"
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, code)
            .apply()
        ArtiusIDSDK.setLanguage(context, code)
        ThemeManager.setLocale(code)
        return createLocaleContext(context, code)
    }

    /**
     * Create a context that uses the given language for resources.
     * Use this in attachBaseContext so getString() returns localized strings.
     */
    fun createLocaleContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setLocales(LocaleList(locale))
            }
        }
        return context.createConfigurationContext(config)
    }

    /**
     * Wrap the base context with the stored locale so resources use the saved language.
     * Call from Activity.attachBaseContext().
     */
    fun wrapWithStoredLocale(base: Context): Context {
        val code = getStoredLanguage(base)
        return createLocaleContext(base, code)
    }
}
