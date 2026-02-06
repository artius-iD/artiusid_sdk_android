/*
 * Okta OIDC login helper (matches iOS OktaProvisioningCoordinator sign-in flow).
 * Uses PKCE; no client secret. Launches browser, handles redirect, exchanges code for tokens, extracts Okta user ID from id_token.
 */
package com.artiusid.sample.okta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.artiusid.sample.config.OktaConfig
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object OktaLoginHelper {
    private const val TAG = "OktaLoginHelper"
    private const val CODE_CHALLENGE_METHOD = "S256"

    private var pendingContinuation: ((Result<OktaLoginResult>) -> Unit)? = null

    /**
     * Generate PKCE code_verifier and code_challenge.
     */
    fun generatePkce(): Pair<String, String> {
        val verifier = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val verifierBase64 = Base64.encodeToString(verifier, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val digest = MessageDigest.getInstance("SHA-256").digest(verifierBase64.toByteArray(StandardCharsets.UTF_8))
        val challenge = Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return verifierBase64 to challenge
    }

    /**
     * Build the Okta authorization URL for browser sign-in.
     */
    fun buildAuthorizeUrl(state: String, codeChallenge: String): String {
        val params = listOf(
            "client_id" to OktaConfig.CLIENT_ID,
            "redirect_uri" to OktaConfig.REDIRECT_URI,
            "response_type" to "code",
            "scope" to URLEncoder.encode(OktaConfig.SCOPES, "UTF-8"),
            "state" to state,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to CODE_CHALLENGE_METHOD
        )
        val query = params.joinToString("&") { "${it.first}=${it.second}" }
        return "${OktaConfig.AUTHORIZE_URL}?$query"
    }

    /**
     * Launch Chrome Custom Tabs for Okta sign-in. Call handleRedirect when the app receives the redirect intent.
     */
    fun launchOktaLogin(context: Context, onResult: (Result<OktaLoginResult>) -> Unit) {
        val (verifier, challenge) = generatePkce()
        val state = Base64.encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) }, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        PkceState.verifier = verifier
        PkceState.state = state
        pendingContinuation = onResult
        val url = buildAuthorizeUrl(state, challenge)
        Log.d(TAG, "Launching Okta login: $url")
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    /**
     * Call this from Activity.onNewIntent when the redirect URI is received (e.g. com.artiusid.sampleapp:/callback?code=...&state=...).
     */
    fun handleRedirect(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        if (data.scheme != "com.artiusid.sampleapp" || data.host != "callback") return false
        val code = data.getQueryParameter("code") ?: run {
            val error = data.getQueryParameter("error") ?: "unknown"
            pendingContinuation?.invoke(Result.failure(Exception("Okta error: $error")))
            pendingContinuation = null
            return true
        }
        val state = data.getQueryParameter("state")
        if (state != PkceState.state) {
            pendingContinuation?.invoke(Result.failure(Exception("State mismatch")))
            pendingContinuation = null
            return true
        }
        val verifier = PkceState.verifier ?: run {
            pendingContinuation?.invoke(Result.failure(Exception("No PKCE verifier")))
            pendingContinuation = null
            return true
        }
        Thread {
            try {
                val result = exchangeCodeForTokens(code, verifier)
                pendingContinuation?.invoke(Result.success(result))
            } catch (e: Exception) {
                Log.e(TAG, "Token exchange failed", e)
                pendingContinuation?.invoke(Result.failure(e))
            }
            pendingContinuation = null
        }.start()
        return true
    }

    private fun exchangeCodeForTokens(code: String, codeVerifier: String): OktaLoginResult {
        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", OktaConfig.REDIRECT_URI)
            .add("client_id", OktaConfig.CLIENT_ID)
            .add("code_verifier", codeVerifier)
            .build()
        val request = Request.Builder()
            .url(OktaConfig.TOKEN_URL)
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Token exchange failed: ${response.code} ${response.body?.string()}")
        }
        val json = JSONObject(response.body?.string() ?: "{}")
        val idToken = json.optString("id_token")
        val accessToken = json.optString("access_token")
        val oktaUserId = extractSubFromIdToken(idToken) ?: throw Exception("Could not extract Okta user ID from id_token")
        Log.d(TAG, "Okta login success, user ID: ${oktaUserId.take(10)}...")
        return OktaLoginResult(oktaUserId = oktaUserId, idToken = idToken, accessToken = accessToken)
    }

    /**
     * Extract "sub" (Okta user ID) from JWT id_token (matches iOS extractOktaUserId).
     */
    fun extractSubFromIdToken(idToken: String): String? {
        val parts = idToken.split(".")
        if (parts.size != 3) return null
        var payload = parts[1]
        val remainder = payload.length % 4
        if (remainder > 0) payload += "====".take(4 - remainder)
        val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            ?: return null
        val json = JSONObject(String(decoded, StandardCharsets.UTF_8))
        return json.optString("sub").takeIf { it.isNotEmpty() }
    }

    data class OktaLoginResult(val oktaUserId: String, val idToken: String, val accessToken: String)

    private object PkceState {
        var verifier: String? = null
        var state: String? = null
    }
}
