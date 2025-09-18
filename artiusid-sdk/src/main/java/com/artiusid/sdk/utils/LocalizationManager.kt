/*
 * File: LocalizationManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * LocalizationManager handles string overrides from the host application
 * 
 * This allows the host app to customize any text displayed in the SDK
 * by providing string overrides in the SDKConfiguration.
 */
object LocalizationManager {
    private var stringOverrides: Map<String, String> = emptyMap()
    
    /**
     * Initialize with string overrides from the host application
     */
    fun initialize(overrides: Map<String, String>) {
        stringOverrides = overrides
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
        
        // Try to get from SDK resources
        try {
            val resId = context.resources.getIdentifier(resourceName, "string", context.packageName)
            if (resId != 0) {
                val sdkString = context.getString(resId)
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
            
            // Fall back to the original resource
            val sdkString = context.getString(resourceId)
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
     * Get all current overrides (for debugging)
     */
    fun getAllOverrides(): Map<String, String> {
        return stringOverrides.toMap()
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
