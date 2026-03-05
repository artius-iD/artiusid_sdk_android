/*
 * In-app browser for Okta OIDC login. Loads the Okta authorize URL in a WebView
 * and intercepts the redirect (com.artiusid.sampleapp:/callback?code=...) to exchange
 * the code for tokens and capture the Okta user ID for the member.
 */
package com.artiusid.sample.okta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import com.artiusid.sample.R

class OktaLoginActivity : FragmentActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okta_login)
        title = getString(R.string.okta_login_title)

        webView = findViewById(R.id.okta_webview)
        progressBar = findViewById(R.id.okta_progress)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        })

        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL)
        if (authUrl.isNullOrBlank()) {
            Toast.makeText(this, "Missing auth URL", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setupWebView()
        webView.loadUrl(authUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                if (url.scheme == "com.artiusid.sampleapp" && url.host == "callback") {
                    handleRedirect(url)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleRedirect(uri: android.net.Uri) {
        progressBar.visibility = View.VISIBLE
        OktaLoginHelper.handleRedirectFromWebView(uri, this) { result ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                result.fold(
                    onSuccess = { loginResult ->
                        val data = Intent().putExtra(EXTRA_OKTA_USER_ID, loginResult.oktaUserId)
                        setResult(Activity.RESULT_OK, data)
                        Toast.makeText(this@OktaLoginActivity, getString(R.string.okta_login_success), Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onFailure = { e ->
                        setResult(Activity.RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, e.message))
                        Toast.makeText(this@OktaLoginActivity, e.message ?: "Login failed", Toast.LENGTH_LONG).show()
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_AUTH_URL = "auth_url"
        const val EXTRA_OKTA_USER_ID = "okta_user_id"
        const val EXTRA_ERROR = "error"
    }
}
