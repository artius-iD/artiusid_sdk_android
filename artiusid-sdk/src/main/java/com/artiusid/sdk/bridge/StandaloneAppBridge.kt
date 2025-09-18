/*
 * File: StandaloneAppBridge.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.bridge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.standalone.StandaloneAppActivity

/**
 * Bridge interface to the complete standalone ArtiusID application
 * 
 * This bridge provides seamless communication between the host app and the
 * standalone application while maintaining complete isolation and functionality.
 */
class StandaloneAppBridge(private val context: Context) {
    
    private val TAG = "StandaloneAppBridge"
    
    private var sdkConfiguration: SDKConfiguration? = null
    private var themeConfiguration: SDKThemeConfiguration? = null
    private var enhancedThemeConfiguration: EnhancedSDKThemeConfiguration? = null
    
    /**
     * Initialize the bridge with configuration and theming
     */
    fun initialize(configuration: SDKConfiguration, theme: SDKThemeConfiguration) {
        android.util.Log.d(TAG, "🌉 Initializing bridge to standalone application...")
        
        sdkConfiguration = configuration
        themeConfiguration = theme
        
        android.util.Log.d(TAG, "✅ Bridge initialized with theme: ${theme.brandName}")
    }
    
    /**
     * Set enhanced theme configuration
     */
    fun setEnhancedTheme(enhancedTheme: EnhancedSDKThemeConfiguration) {
        enhancedThemeConfiguration = enhancedTheme
        android.util.Log.d(TAG, "🎨 Enhanced theme set: ${enhancedTheme.brandName}")
    }
    
    /**
     * Start verification flow in standalone application
     */
    fun startVerification(activity: Activity, callback: VerificationCallback) {
        android.util.Log.d(TAG, "🚀 Launching standalone app for verification...")
        
        val intent = Intent(activity, StandaloneAppActivity::class.java).apply {
            putExtra(EXTRA_FLOW_TYPE, FLOW_TYPE_VERIFICATION)
            putExtra(EXTRA_SDK_CONFIG, sdkConfiguration)
            putExtra(EXTRA_THEME_CONFIG, themeConfiguration)
            putExtra(EXTRA_ENHANCED_THEME_CONFIG, enhancedThemeConfiguration)
            putExtra(EXTRA_START_TIME, System.currentTimeMillis())
        }
        
        // Store callback in bridge registry for result handling
        BridgeCallbackRegistry.registerVerificationCallback(callback)
        
        activity.startActivity(intent)
    }
    
    /**
     * Start authentication flow in standalone application
     */
    fun startAuthentication(activity: Activity, callback: AuthenticationCallback) {
        android.util.Log.d(TAG, "🚀 Launching standalone app for authentication...")
        
        val intent = Intent(activity, StandaloneAppActivity::class.java).apply {
            putExtra(EXTRA_FLOW_TYPE, FLOW_TYPE_AUTHENTICATION)
            putExtra(EXTRA_SDK_CONFIG, sdkConfiguration)
            putExtra(EXTRA_THEME_CONFIG, themeConfiguration)
            putExtra(EXTRA_ENHANCED_THEME_CONFIG, enhancedThemeConfiguration)
            putExtra(EXTRA_START_TIME, System.currentTimeMillis())
        }
        
        // Store callback in bridge registry for result handling
        BridgeCallbackRegistry.registerAuthenticationCallback(callback)
        
        activity.startActivity(intent)
    }
    
    companion object {
        // Intent extras for communication
        const val EXTRA_FLOW_TYPE = "flow_type"
        const val EXTRA_SDK_CONFIG = "sdk_config"
        const val EXTRA_THEME_CONFIG = "theme_config"
        const val EXTRA_ENHANCED_THEME_CONFIG = "enhanced_theme_config"
        const val EXTRA_START_TIME = "start_time"
        
        // Flow types
        const val FLOW_TYPE_VERIFICATION = "verification"
        const val FLOW_TYPE_AUTHENTICATION = "authentication"
    }
}

/**
 * Registry for managing callbacks between bridge and standalone app
 */
object BridgeCallbackRegistry {
    
    private var verificationCallback: VerificationCallback? = null
    private var authenticationCallback: AuthenticationCallback? = null
    
    fun registerVerificationCallback(callback: VerificationCallback) {
        verificationCallback = callback
    }
    
    fun registerAuthenticationCallback(callback: AuthenticationCallback) {
        authenticationCallback = callback
    }
    
    fun getVerificationCallback(): VerificationCallback? = verificationCallback
    
    fun getAuthenticationCallback(): AuthenticationCallback? = authenticationCallback
    
    fun clearCallbacks() {
        verificationCallback = null
        authenticationCallback = null
    }
}
