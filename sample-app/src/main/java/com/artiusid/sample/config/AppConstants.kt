/*
 * Application constants (matches iOS MFA app AppConstants / SDK AppConstants)
 * Central place for ArtiusID credentials and app config.
 *
 * Optional: Add appconstants.json to src/main/assets/ to override from your MFA iOS app:
 *   { "apiKey": "...", "clientId": 1, "clientGroupId": 1, "appName": "...", "enableDebugLogging": true }
 * If appconstants.json exists, it overrides the defaults below.
 */
package com.artiusid.sample.config

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object AppConstants {
    private const val TAG = "AppConstants"
    private const val ASSETS_FILE = "appconstants.json"

    // ArtiusID credentials (matches iOS AppConstants)
    private const val DEFAULT_API_KEY = "demo_api_key_12345"
    private const val DEFAULT_CLIENT_ID = 1
    private const val DEFAULT_CLIENT_GROUP_ID = 1

    // App info
    private const val DEFAULT_APP_NAME = "ArtiusID Sample"
    private const val DEFAULT_APP_VERSION = "1.0.0"

    // Feature flags
    private const val DEFAULT_ENABLE_DEBUG_LOGGING = true
    private const val DEFAULT_ENABLE_FIREBASE = true
    private const val DEFAULT_ENABLE_ENVIRONMENT_SWITCHING = true

    private var loadedApiKey: String? = null
    private var loadedClientId: Int? = null
    private var loadedClientGroupId: Int? = null
    private var loadedAppName: String? = null
    private var loadedAppVersion: String? = null
    private var loadedEnableDebugLogging: Boolean? = null
    private var loadedEnableFirebase: Boolean? = null
    private var loadedEnableEnvironmentSwitching: Boolean? = null

    val apiKey: String get() = loadedApiKey ?: DEFAULT_API_KEY
    val clientId: Int get() = loadedClientId ?: DEFAULT_CLIENT_ID
    val clientGroupId: Int get() = loadedClientGroupId ?: DEFAULT_CLIENT_GROUP_ID
    val appName: String get() = loadedAppName ?: DEFAULT_APP_NAME
    val appVersion: String get() = loadedAppVersion ?: DEFAULT_APP_VERSION
    val enableDebugLogging: Boolean get() = loadedEnableDebugLogging ?: DEFAULT_ENABLE_DEBUG_LOGGING
    val enableFirebase: Boolean get() = loadedEnableFirebase ?: DEFAULT_ENABLE_FIREBASE
    val enableEnvironmentSwitching: Boolean get() = loadedEnableEnvironmentSwitching ?: DEFAULT_ENABLE_ENVIRONMENT_SWITCHING

    /**
     * Load overrides from assets/appconstants.json if present (matches iOS AppConstants / plist).
     * Call from Application.onCreate() so MFA app values can be used.
     */
    fun loadFromAssets(context: Context) {
        try {
            val json = context.assets.open(ASSETS_FILE).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            val obj = JSONObject(json)
            loadedApiKey = obj.optString("apiKey").takeIf { it.isNotEmpty() }
            if (obj.has("clientId")) loadedClientId = obj.optInt("clientId", DEFAULT_CLIENT_ID)
            if (obj.has("clientGroupId")) loadedClientGroupId = obj.optInt("clientGroupId", DEFAULT_CLIENT_GROUP_ID)
            loadedAppName = obj.optString("appName").takeIf { it.isNotEmpty() }
            loadedAppVersion = obj.optString("appVersion").takeIf { it.isNotEmpty() }
            if (obj.has("enableDebugLogging")) loadedEnableDebugLogging = obj.optBoolean("enableDebugLogging", DEFAULT_ENABLE_DEBUG_LOGGING)
            if (obj.has("enableFirebase")) loadedEnableFirebase = obj.optBoolean("enableFirebase", DEFAULT_ENABLE_FIREBASE)
            if (obj.has("enableEnvironmentSwitching")) loadedEnableEnvironmentSwitching = obj.optBoolean("enableEnvironmentSwitching", DEFAULT_ENABLE_ENVIRONMENT_SWITCHING)
            Log.i(TAG, "Loaded AppConstants from $ASSETS_FILE (clientId=$clientId, clientGroupId=$clientGroupId)")
        } catch (e: Exception) {
            if (e is java.io.FileNotFoundException) {
                Log.d(TAG, "No $ASSETS_FILE in assets, using defaults")
            } else {
                Log.w(TAG, "Failed to load $ASSETS_FILE: ${e.message}")
            }
        }
    }
}
