/*
 * File: ThemeManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.utils.ImageOverrideManager
import com.artiusid.sdk.utils.LocalizationManager

/**
 * Unified facade for SDK theming: theme configuration, locale, and image overrides.
 * Delegates to [EnhancedThemeManager], [ColorManager], [LocalizationManager], and [ImageOverrideManager].
 * iOS parity: single entry for setTheme, setLocale, getDebugInfo.
 */
object ThemeManager {

    /**
     * Set the current theme configuration (delegates to EnhancedThemeManager and ColorManager).
     */
    fun setTheme(config: EnhancedSDKThemeConfiguration) {
        EnhancedThemeManager.setThemeConfiguration(config)
    }

    /**
     * Get the current theme configuration.
     */
    fun getCurrentTheme(): EnhancedSDKThemeConfiguration = EnhancedThemeManager.getCurrentThemeConfig()

    /**
     * Set the SDK display language (e.g. "en", "es", "fr"). Delegates to LocalizationManager.
     */
    fun setLocale(languageCode: String) {
        LocalizationManager.setLanguage(languageCode)
    }

    /**
     * Get current language code if set.
     */
    fun getCurrentLocale(): String? = LocalizationManager.getCurrentLanguageCode()

    /**
     * Combined debug description for theme, locale, and image overrides.
     */
    fun getDebugInfo(): String = buildString {
        append(LocalizationManager.getDebugInfo())
        append("\n")
        append("ThemeManager theme: ${EnhancedThemeManager.getCurrentThemeConfig().brandName}\n")
        if (ImageOverrideManager.isInitialized()) {
            append(ImageOverrideManager.getInstance().getDebugInfo())
        } else {
            append("ImageOverrideManager: not initialized\n")
        }
    }
}
