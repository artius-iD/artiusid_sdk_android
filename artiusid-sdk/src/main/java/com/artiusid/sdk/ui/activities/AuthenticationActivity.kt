/*
 * File: AuthenticationActivity.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Matches iOS AuthenticationProgressView.swift exactly
 */

package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.*
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.models.SDKErrorCode
import com.artiusid.sdk.ui.screens.auth.AuthenticationProgressScreen
import com.artiusid.sdk.ui.theme.EnhancedSDKTheme

/**
 * Authentication Activity that matches iOS AuthenticationProgressView exactly
 * Shows biometric prompt + progress + API authentication
 */
class AuthenticationActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "AuthenticationActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d(TAG, "🚀 AuthenticationActivity onCreate() called")
        
        try {
            val themeConfig = com.artiusid.sdk.ui.theme.EnhancedThemeManager.getCurrentThemeConfig()
            android.util.Log.d(TAG, "📱 Theme config loaded: ${themeConfig.brandName}")
            
            setContent {
                EnhancedSDKTheme(
                    themeConfig = themeConfig
                ) {
                AuthenticationProgressScreen(
                    onAuthenticationSuccess = {
                        // Match iOS: successful authentication
                        ArtiusIDSDK.authenticationCallback?.onAuthenticationSuccess(
                            AuthenticationResult(
                                authenticationId = "auth_${System.currentTimeMillis()}",
                                confidence = 1.0f,
                                processingTime = 3000L,
                                sessionId = "session_${System.currentTimeMillis()}",
                                success = true
                            )
                        )
                        finish()
                    },
                    onAuthenticationFailure = {
                        // Match iOS: failed authentication
                        ArtiusIDSDK.authenticationCallback?.onAuthenticationError(
                            SDKError(
                                code = SDKErrorCode.AUTHENTICATION_FAILED,
                                message = "Authentication failed"
                            )
                        )
                        finish()
                    },
                    onBack = {
                        // Match iOS: back to home (cancelled)
                        ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
                        finish()
                    }
                )
            }
        }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Error in AuthenticationActivity onCreate", e)
            // Call failure callback and finish
            ArtiusIDSDK.authenticationCallback?.onAuthenticationError(
                SDKError(
                    code = SDKErrorCode.UNKNOWN_ERROR,
                    message = "Authentication activity failed to start: ${e.message}",
                    cause = e
                )
            )
            finish()
        }
    }
    
    override fun onBackPressed() {
        // Match iOS: back button cancels authentication
        android.util.Log.d(TAG, "🔙 Back button pressed - cancelling authentication")
        ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
        super.onBackPressed()
    }
}
