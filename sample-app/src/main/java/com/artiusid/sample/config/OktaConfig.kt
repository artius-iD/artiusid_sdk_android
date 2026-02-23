/*
 * Okta OIDC configuration (matches iOS Okta.plist / MFA app AppConstants.Okta)
 * Used for browser sign-in to get Okta user ID and pass to ArtiusID SDK.
 *
 * Optional: Add okta.json to src/main/assets/ with keys from your MFA iOS app Okta.plist:
 *   { "issuer": "...", "clientId": "...", "redirectUri": "...", "scopes": "..." }
 * If okta.json exists, it overrides the defaults below.
 */
package com.artiusid.sample.config

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object OktaConfig {
    private const val TAG = "OktaConfig"
    private const val ASSETS_FILE = "okta.json"

    // Defaults (same values as iOS artiusid_ios_okta_mfa_app Okta.plist)
    private const val DEFAULT_ISSUER = "https://integrator-6977887.okta.com"
    private const val DEFAULT_CLIENT_ID = "0oazutsn89PywJlLo697"
    /** Must be registered in Okta Application's Sign-in redirect URIs. Use this app's scheme so the redirect opens the app. */
    private const val DEFAULT_REDIRECT_URI = "com.artiusid.sampleapp:/callback"
    private const val DEFAULT_SCOPES = "openid profile offline_access okta.myAccount.appAuthenticator.manage okta.myAccount.appAuthenticator.read"

    private var loadedIssuer: String? = null
    private var loadedClientId: String? = null
    private var loadedRedirectUri: String? = null
    private var loadedScopes: String? = null

    /** Issuer URL (from okta.json or default). */
    val ISSUER: String get() = loadedIssuer ?: DEFAULT_ISSUER

    /** Client ID (from okta.json or default). */
    val CLIENT_ID: String get() = loadedClientId ?: DEFAULT_CLIENT_ID

    /** Redirect URI; must match Okta Application Sign-in redirect URIs and app's intent-filter. */
    val REDIRECT_URI: String get() = loadedRedirectUri ?: DEFAULT_REDIRECT_URI

    /** OAuth scopes. */
    val SCOPES: String get() = loadedScopes ?: DEFAULT_SCOPES

    val AUTHORIZE_URL: String get() = "$ISSUER/oauth2/v1/authorize"
    val TOKEN_URL: String get() = "$ISSUER/oauth2/v1/token"

    /**
     * Load Okta config from assets/okta.json if present (matches iOS Okta.plist).
     * Call from Application.onCreate() so MFA app values can be used.
     */
    fun loadFromAssets(context: Context) {
        try {
            val json = context.assets.open(ASSETS_FILE).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            val obj = JSONObject(json)
            loadedIssuer = obj.optString("issuer").takeIf { it.isNotEmpty() }
            loadedClientId = obj.optString("clientId").takeIf { it.isNotEmpty() }
            loadedRedirectUri = obj.optString("redirectUri").takeIf { it.isNotEmpty() }
            loadedScopes = obj.optString("scopes").takeIf { it.isNotEmpty() }
            Log.i(TAG, "Loaded Okta config from $ASSETS_FILE (issuer=${loadedIssuer?.take(30)}...)")
        } catch (e: Exception) {
            if (e is java.io.FileNotFoundException) {
                Log.d(TAG, "No $ASSETS_FILE in assets, using defaults")
            } else {
                Log.w(TAG, "Failed to load $ASSETS_FILE: ${e.message}")
            }
        }
    }
}
