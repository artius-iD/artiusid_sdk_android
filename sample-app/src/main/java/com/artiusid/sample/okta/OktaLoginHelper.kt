/*
 * Okta OIDC login helper (matches iOS OktaProvisioningCoordinator sign-in flow).
 * Uses PKCE; no client secret. Launches browser, handles redirect, exchanges code for tokens, extracts Okta user ID from id_token.
 */
package com.artiusid.sample.okta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.artiusid.sample.config.OktaConfig
import com.artiusid.sdk.ArtiusIDSDK
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
    private const val PREFS_OKTA = "okta_pkce"
    private const val KEY_VERIFIER = "pkce_verifier"
    private const val KEY_STATE = "pkce_state"

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
     * All query values are URL-encoded so custom scheme redirect_uri and PKCE params are valid.
     */
    fun buildAuthorizeUrl(state: String, codeChallenge: String): String {
        fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
        val params = listOf(
            "client_id" to enc(OktaConfig.CLIENT_ID),
            "redirect_uri" to enc(OktaConfig.REDIRECT_URI),
            "response_type" to "code",
            "scope" to enc(OktaConfig.SCOPES),
            "state" to enc(state),
            "code_challenge" to enc(codeChallenge),
            "code_challenge_method" to CODE_CHALLENGE_METHOD
        )
        val query = params.joinToString("&") { "${it.first}=${it.second}" }
        return "${OktaConfig.AUTHORIZE_URL}?$query"
    }

    /**
     * Launch in-app browser (OktaLoginActivity) for Okta sign-in. The WebView loads the authorize URL
     * and intercepts the redirect so the Okta ID can be captured for the member without leaving the app.
     */
    fun launchOktaLoginInApp(activity: Activity): String {
        val (verifier, challenge) = generatePkce()
        val state = Base64.encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) }, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        PkceState.verifier = verifier
        PkceState.state = state
        savePkceToPrefs(activity, verifier, state)
        val url = buildAuthorizeUrl(state, challenge)
        Log.i(TAG, "Launching Okta login in-app (WebView)")
        return url
    }

    /**
     * Handle redirect URI from in-app WebView (OktaLoginActivity). Exchanges code for tokens and
     * invokes onComplete on the main thread with the result.
     */
    fun handleRedirectFromWebView(
        uri: Uri,
        context: Context,
        onComplete: (Result<OktaLoginResult>) -> Unit
    ) {
        if (uri.scheme != "com.artiusid.sampleapp" || uri.host != "callback") return
        Log.i(TAG, "Redirect from WebView: scheme=${uri.scheme}, host=${uri.host}")
        val code = uri.getQueryParameter("code") ?: run {
            val error = uri.getQueryParameter("error") ?: "unknown"
            Log.w(TAG, "Okta redirect error: $error")
            (context as? Activity)?.runOnUiThread { onComplete(Result.failure(Exception("Okta error: $error"))) }
            return
        }
        val stateParam = uri.getQueryParameter("state")
        var state = PkceState.state
        var verifier = PkceState.verifier
        if (state == null || verifier == null) {
            val loaded = loadPkceFromPrefs(context)
            if (loaded != null) {
                state = loaded.first
                verifier = loaded.second
                Log.i(TAG, "Using persisted PKCE state/verifier")
            }
        }
        if (stateParam != state) {
            Log.w(TAG, "State mismatch")
            clearPkcePrefs(context)
            (context as? Activity)?.runOnUiThread { onComplete(Result.failure(Exception("State mismatch"))) }
            return
        }
        val verifierToUse = verifier
        if (verifierToUse == null) {
            clearPkcePrefs(context)
            (context as? Activity)?.runOnUiThread { onComplete(Result.failure(Exception("No PKCE verifier"))) }
            return
        }
        clearPkcePrefs(context)
        Thread {
            try {
                val result = exchangeCodeForTokens(code, verifierToUse)
                Log.i(TAG, "Token exchange success (in-app)")
                (context as? Activity)?.runOnUiThread { onComplete(Result.success(result)) }
            } catch (e: Exception) {
                Log.e(TAG, "Token exchange failed", e)
                (context as? Activity)?.runOnUiThread { onComplete(Result.failure(e)) }
            }
        }.start()
    }


    /**
     * Call this from Activity.onNewIntent/onCreate when the redirect URI is received (e.g. com.artiusid.sampleapp:/callback?code=...&state=...).
     * Pass the Activity context so the result callback runs on the main thread (and so we can set Okta user ID on process-death recovery).
     */
    fun handleRedirect(intent: Intent?, context: Context?): Boolean {
        val data = intent?.data ?: return false
        if (data.scheme != "com.artiusid.sampleapp" || data.host != "callback") return false
        Log.i(TAG, "Redirect received: scheme=${data.scheme}, host=${data.host}")
        val code = data.getQueryParameter("code") ?: run {
            val error = data.getQueryParameter("error") ?: "unknown"
            Log.w(TAG, "Okta redirect error: $error")
            deliverResult(Result.failure(Exception("Okta error: $error")), context)
            return true
        }
        val stateParam = data.getQueryParameter("state")
        var state = PkceState.state
        var verifier = PkceState.verifier
        if (state == null || verifier == null) {
            val loaded = context?.let { loadPkceFromPrefs(it) }
            if (loaded != null) {
                state = loaded.first
                verifier = loaded.second
                Log.i(TAG, "Using persisted PKCE state/verifier (process was recreated)")
            }
        }
        if (stateParam != state) {
            Log.w(TAG, "State mismatch (expected=${state?.take(8)}..., got=${stateParam?.take(8)}...)")
            clearPkcePrefs(context)
            deliverResult(Result.failure(Exception("State mismatch")), context)
            return true
        }
        val verifierToUse = verifier
        if (verifierToUse == null) {
            Log.w(TAG, "No PKCE verifier in memory or prefs")
            clearPkcePrefs(context)
            deliverResult(Result.failure(Exception("No PKCE verifier")), context)
            return true
        }
        clearPkcePrefs(context)
        Thread {
            try {
                val result = exchangeCodeForTokens(code, verifierToUse)
                Log.i(TAG, "Token exchange success, delivering result")
                deliverResult(Result.success(result), context)
            } catch (e: Exception) {
                Log.e(TAG, "Token exchange failed", e)
                deliverResult(Result.failure(e), context)
            } finally {
                pendingContinuation = null
            }
        }.start()
        return true
    }

    private fun savePkceToPrefs(context: Context, verifier: String, state: String) {
        context.getSharedPreferences(PREFS_OKTA, Context.MODE_PRIVATE).edit()
            .putString(KEY_VERIFIER, verifier)
            .putString(KEY_STATE, state)
            .apply()
    }

    private fun loadPkceFromPrefs(context: Context): Pair<String?, String?>? {
        val prefs = context.getSharedPreferences(PREFS_OKTA, Context.MODE_PRIVATE)
        val verifier = prefs.getString(KEY_VERIFIER, null)
        val state = prefs.getString(KEY_STATE, null)
        return if (verifier != null && state != null) Pair(state, verifier) else null
    }

    private fun clearPkcePrefs(context: Context?) {
        context?.getSharedPreferences(PREFS_OKTA, Context.MODE_PRIVATE)?.edit()?.remove(KEY_VERIFIER)?.remove(KEY_STATE)?.apply()
    }

    private fun deliverResult(result: Result<OktaLoginResult>, context: Context?) {
        val cont = pendingContinuation
        val run: () -> Unit = {
            cont?.invoke(result)
            if (cont == null && result.isSuccess) {
                result.getOrNull()?.let { r ->
                    ArtiusIDSDK.setOktaUserId(r.oktaUserId)
                    Log.i(TAG, "Okta user ID set after process recovery: ${r.oktaUserId.take(10)}...")
                }
            }
        }
        val activity = context as? Activity
        if (activity != null) {
            activity.runOnUiThread(run)
        } else {
            run()
        }
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
