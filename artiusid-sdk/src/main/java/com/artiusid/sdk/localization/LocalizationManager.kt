/*
 * File: LocalizationManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.localization

import android.content.Context
import androidx.annotation.StringRes
import com.artiusid.sdk.R

/**
 * Manages localization for the SDK, allowing host applications to override string resources
 */
object LocalizationManager {
    
    private var hostContext: Context? = null
    private var customStringOverrides: Map<String, String> = emptyMap()
    
    /**
     * Initialize the localization manager with the host application context
     * This allows the SDK to access the host app's string resources
     */
    fun initialize(context: Context, stringOverrides: Map<String, String> = emptyMap()) {
        hostContext = context
        customStringOverrides = stringOverrides
    }
    
    /**
     * Get a localized string, first checking custom overrides, then host app, then SDK defaults
     */
    fun getString(context: Context, @StringRes resId: Int): String {
        val resourceName = context.resources.getResourceEntryName(resId)
        
        // 1. Check custom string overrides first
        customStringOverrides[resourceName]?.let { return it }
        
        // 2. Check host application strings
        hostContext?.let { hostCtx ->
            try {
                val hostResId = hostCtx.resources.getIdentifier(resourceName, "string", hostCtx.packageName)
                if (hostResId != 0) {
                    return hostCtx.getString(hostResId)
                }
            } catch (e: Exception) {
                // Fall through to SDK default
            }
        }
        
        // 3. Fall back to SDK default
        return context.getString(resId)
    }
    
    /**
     * Get a formatted localized string
     */
    fun getString(context: Context, @StringRes resId: Int, vararg formatArgs: Any): String {
        val resourceName = context.resources.getResourceEntryName(resId)
        
        // 1. Check custom string overrides first
        customStringOverrides[resourceName]?.let { 
            return String.format(it, *formatArgs)
        }
        
        // 2. Check host application strings
        hostContext?.let { hostCtx ->
            try {
                val hostResId = hostCtx.resources.getIdentifier(resourceName, "string", hostCtx.packageName)
                if (hostResId != 0) {
                    return hostCtx.getString(hostResId, *formatArgs)
                }
            } catch (e: Exception) {
                // Fall through to SDK default
            }
        }
        
        // 3. Fall back to SDK default
        return context.getString(resId, *formatArgs)
    }
    
    /**
     * Update custom string overrides at runtime
     */
    fun updateStringOverrides(overrides: Map<String, String>) {
        customStringOverrides = overrides
    }
    
    /**
     * Clear all overrides and reset to defaults
     */
    fun clearOverrides() {
        customStringOverrides = emptyMap()
        hostContext = null
    }
}

/**
 * Extension function for easy access to localized strings from Composables
 */
fun Context.getLocalizedString(@StringRes resId: Int): String {
    return LocalizationManager.getString(this, resId)
}

/**
 * Extension function for formatted localized strings from Composables
 */
fun Context.getLocalizedString(@StringRes resId: Int, vararg formatArgs: Any): String {
    return LocalizationManager.getString(this, resId, *formatArgs)
}
