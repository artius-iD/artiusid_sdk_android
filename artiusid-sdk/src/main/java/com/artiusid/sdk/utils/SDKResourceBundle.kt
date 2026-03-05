/*
 * File: SDKResourceBundle.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import androidx.annotation.DrawableRes
import java.util.Locale

/**
 * Helper for SDK string and image lookup with override and locale support.
 * Wraps [Context] and delegates to [LocalizationManager] for strings and
 * to SDK/host resources for drawables. iOS parity: localizedString(key, fallback), image(named).
 */
class SDKResourceBundle(private val context: Context) {

    /**
     * Get a localized string by key, with optional fallback.
     * Uses [LocalizationManager] (overrides + SDK resources + optional locale).
     */
    fun localizedString(key: String, fallback: String? = null): String {
        return LocalizationManager.getString(context, key, fallback)
    }

    /**
     * Get a localized string for a specific locale (creates a configuration context for that locale).
     * Does not change [LocalizationManager]'s global language; only affects this lookup.
     */
    fun localizedString(key: String, locale: Locale, fallback: String? = null): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) {
            val config = android.content.res.Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
            val localeContext = context.createConfigurationContext(config)
            return try {
                localeContext.getString(resId)
            } catch (e: Exception) {
                fallback ?: key
            }
        }
        return LocalizationManager.getString(context, key, fallback)
    }

    /**
     * Get drawable resource ID by name (e.g. "brand_logo", "success_icon").
     * Looks up in SDK package. For override-aware loading use [ImageOverrideManager] instead.
     */
    @DrawableRes
    fun image(named: String): Int {
        return context.resources.getIdentifier(named, "drawable", context.packageName)
    }

    /**
     * Get drawable resource ID by name, with optional fallback resource ID.
     */
    @DrawableRes
    fun image(named: String, fallbackResId: Int): Int {
        val id = context.resources.getIdentifier(named, "drawable", context.packageName)
        return if (id != 0) id else fallbackResId
    }
}
