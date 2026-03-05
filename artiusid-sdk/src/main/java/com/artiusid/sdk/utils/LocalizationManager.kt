/*
 * File: LocalizationManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * LocalizationManager handles string overrides from the host application
 *
 * This allows the host app to customize any text displayed in the SDK
 * by providing string overrides in the SDKConfiguration.
 * Supports runtime language change via setLanguage (iOS parity).
 * Supports add/remove/clear overrides and debug info (iOS parity).
 */
object LocalizationManager {
    private val stringOverrides = ConcurrentHashMap<String, String>()
    @Volatile
    private var currentLanguageCode: String? = null

    /**
     * Set the SDK display language at runtime (e.g. "en", "es", "fr").
     */
    fun setLanguage(languageCode: String) {
        currentLanguageCode = if (languageCode.isBlank()) null else languageCode
    }

    /**
     * Get current language code if set.
     */
    fun getCurrentLanguageCode(): String? = currentLanguageCode

    /**
     * Initialize with string overrides from the host application (replaces all overrides).
     */
    fun initialize(overrides: Map<String, String>) {
        stringOverrides.clear()
        stringOverrides.putAll(overrides)
        android.util.Log.d("LocalizationManager", "🌐 Initialized with ${overrides.size} string overrides")
        overrides.forEach { (key, value) ->
            android.util.Log.d("LocalizationManager", "  📝 $key = $value")
        }
    }
    
    /**
     * Get a localized string, checking overrides first, then falling back to SDK resources
     */
    fun getString(context: Context, resourceName: String, defaultValue: String? = null): String {
        // Check if we have an override for this string
        stringOverrides[resourceName]?.let { override ->
            android.util.Log.d("LocalizationManager", "✅ Using override for '$resourceName': $override")
            return override
        }
        
        // Try to get from SDK resources (with optional locale override)
        try {
            val resId = context.resources.getIdentifier(resourceName, "string", context.packageName)
            if (resId != 0) {
                val ctx = currentLanguageCode?.let { code ->
                    val config = Configuration(context.resources.configuration).apply { setLocale(Locale(code)) }
                    context.createConfigurationContext(config)
                } ?: context
                val sdkString = ctx.getString(resId)
                android.util.Log.d("LocalizationManager", "📚 Using SDK string for '$resourceName': $sdkString")
                return sdkString
            }
        } catch (e: Exception) {
            android.util.Log.w("LocalizationManager", "⚠️ Failed to get SDK string for '$resourceName'", e)
        }
        
        // Return default value or the resource name as fallback
        val fallback = defaultValue ?: resourceName
        android.util.Log.d("LocalizationManager", "🔄 Using fallback for '$resourceName': $fallback")
        return fallback
    }
    
    /**
     * Get a localized string using resource ID, checking overrides first
     */
    fun getString(context: Context, resourceId: Int): String {
        try {
            // Get the resource name from the ID
            val resourceName = context.resources.getResourceEntryName(resourceId)
            
            // Check if we have an override for this string
            stringOverrides[resourceName]?.let { override ->
                android.util.Log.d("LocalizationManager", "✅ Using override for resource ID $resourceId ('$resourceName'): $override")
                return override
            }
            
            // Fall back to the original resource (with optional locale override)
            val ctx = currentLanguageCode?.let { code ->
                val config = Configuration(context.resources.configuration).apply { setLocale(Locale(code)) }
                context.createConfigurationContext(config)
            } ?: context
            val sdkString = ctx.getString(resourceId)
            android.util.Log.d("LocalizationManager", "📚 Using SDK string for resource ID $resourceId ('$resourceName'): $sdkString")
            return sdkString
            
        } catch (e: Exception) {
            android.util.Log.w("LocalizationManager", "⚠️ Failed to get string for resource ID $resourceId", e)
            return "String not found"
        }
    }
    
    /**
     * Check if a string override exists
     */
    fun hasOverride(resourceName: String): Boolean {
        return stringOverrides.containsKey(resourceName)
    }
    
    /**
     * Get all current overrides (for debugging).
     */
    fun getAllOverrides(): Map<String, String> = stringOverrides.toMap()

    /**
     * Set all overrides at once (replaces existing). iOS parity: setOverrides.
     */
    fun setOverrides(overrides: Map<String, String>) {
        stringOverrides.clear()
        stringOverrides.putAll(overrides)
    }

    /**
     * Add or update a single override. iOS parity: addOverride(key:value:).
     */
    fun addOverride(key: String, value: String) {
        stringOverrides[key] = value
    }

    /**
     * Remove a single override by key. iOS parity: removeOverride(key:).
     */
    fun removeOverride(key: String) {
        stringOverrides.remove(key)
    }

    /**
     * Clear all overrides. iOS parity: clearOverrides.
     */
    fun clearOverrides() {
        stringOverrides.clear()
    }

    /**
     * Number of active overrides. iOS parity: overrideCount.
     */
    fun getOverrideCount(): Int = stringOverrides.size

    /**
     * Set of override keys. iOS parity: overrideKeys.
     */
    fun getOverrideKeys(): Set<String> = stringOverrides.keys.toSet()

    /**
     * Debug description (iOS parity: getDebugInfo).
     */
    fun getDebugInfo(): String = buildString {
        append("LocalizationManager:\n")
        append("  - Override count: ${stringOverrides.size}\n")
        append("  - Current language: ${currentLanguageCode ?: "system"}\n")
        if (stringOverrides.isNotEmpty()) {
            append("  - Keys: ${stringOverrides.keys.take(10).joinToString()}")
            if (stringOverrides.size > 10) append(" ... (+${stringOverrides.size - 10} more)")
            append("\n")
        }
    }
}

/**
 * Composable function to get localized strings in Compose UI
 */
@Composable
fun LocalizedText(resourceName: String, defaultValue: String? = null): String {
    val context = LocalContext.current
    return LocalizationManager.getString(context, resourceName, defaultValue)
}
