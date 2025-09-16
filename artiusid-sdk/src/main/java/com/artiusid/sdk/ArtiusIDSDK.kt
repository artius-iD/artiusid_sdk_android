package com.artiusid.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.artiusid.sdk.bridge.StandaloneAppBridge
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.models.SDKErrorCode
import com.artiusid.sdk.services.APIManager
import com.artiusid.sdk.util.DeviceUtils
import com.artiusid.sdk.utils.SharedContextManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ArtiusID SDK - Bridge to Complete Standalone Application
 * 
 * This SDK provides a bridge interface to the complete standalone ArtiusID application.
 * The standalone app runs in its own activity context with full isolation, while the
 * SDK provides seamless integration, theming, and result communication.
 * 
 * Architecture:
 * Host App -> SDK Bridge -> Standalone App Activity -> Results -> SDK Bridge -> Host App
 */
object ArtiusIDSDK {
    
    private const val TAG = "ArtiusIDSDK"
    
    // Bridge to standalone application
    private lateinit var standaloneAppBridge: StandaloneAppBridge
    
    // Callback storage for returning results to host app
    var verificationCallback: VerificationCallback? = null
    var authenticationCallback: AuthenticationCallback? = null
    
    // SDK Configuration
    private var sdkConfiguration: SDKConfiguration? = null
    private var themeConfiguration: SDKThemeConfiguration? = null
    private var isInitialized = false
    
    // Shared context management for mTLS and Firebase
    private var sharedContextManager: SharedContextManager? = null
    
    /**
     * Initialize the SDK with configuration and theming
     * 
     * @param context Application context
     * @param configuration SDK configuration (API keys, environment, etc.)
     * @param theme Theme configuration for branding the standalone app
     */
    fun initialize(
        context: Context, 
        configuration: SDKConfiguration,
        theme: SDKThemeConfiguration
    ) {
        try {
            android.util.Log.i(TAG, "🌉 Initializing ArtiusID SDK Bridge...")

            // Store configurations
            sdkConfiguration = configuration.copy(
                hostAppPackageName = context.packageName
            )
            themeConfiguration = theme

            // Set up environment in SharedPreferences for UrlBuilder
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val environmentName = when (configuration.environment) {
                com.artiusid.sdk.config.Environment.DEVELOPMENT -> "Development"
                com.artiusid.sdk.config.Environment.STAGING -> "Staging"
                com.artiusid.sdk.config.Environment.PRODUCTION -> "Production"
            }
            prefs.edit().putString("environment", environmentName).apply()
            android.util.Log.i(TAG, "🌐 Environment set to: $environmentName")

            // Initialize shared context manager for mTLS and Firebase
            sharedContextManager = SharedContextManager(context, sdkConfiguration!!)
            sharedContextManager!!.logSharedContextStatus()

            // Initialize mTLS certificate using shared context (non-blocking)
            // Certificate registration may fail due to network issues, but app should continue
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    initializeSharedCertificate(context, sdkConfiguration!!)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Certificate initialization failed, but continuing with SDK initialization", e)
                    // App continues without certificate - verification will handle this gracefully
                }
            }

            // Initialize the bridge to standalone application
            standaloneAppBridge = StandaloneAppBridge(context)
            standaloneAppBridge.initialize(sdkConfiguration!!, theme)

            isInitialized = true

            android.util.Log.i(TAG, "✅ ArtiusID SDK Bridge initialized successfully")
            android.util.Log.i(TAG, "🎨 Theme: ${theme.brandName}")
            android.util.Log.i(TAG, "🏢 Environment: ${configuration.environment}")
            android.util.Log.i(TAG, "🌉 Bridge ready to launch standalone application")

        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to initialize ArtiusID SDK Bridge", e)
            throw e
        }
    }
    
    /**
     * Initialize mTLS certificate using shared context for secure API communication
     */
    private fun initializeSharedCertificate(context: Context, configuration: SDKConfiguration) {
        android.util.Log.d(TAG, "🔐 Initializing shared mTLS certificate...")
        
        // Initialize certificate in background to avoid blocking UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = DeviceUtils.getDeviceId(context)
                
                android.util.Log.d(TAG, "📱 Device ID: $deviceId")
                android.util.Log.d(TAG, "🌐 Service URL: ${configuration.baseUrl}")
                android.util.Log.d(TAG, "🏢 Host Package: ${configuration.hostAppPackageName}")
                
                // Ensure certificate exists using shared context
                sharedContextManager?.ensureSharedCertificate(deviceId)
                
                android.util.Log.d(TAG, "✅ Shared mTLS certificate initialization completed")
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "❌ Failed to initialize shared mTLS certificate", e)
                // Don't throw here - let the app continue and handle certificate errors during API calls
            }
        }
    }
    
    /**
     * Start complete verification flow using standalone application
     * 
     * This launches the complete standalone application in its own activity context:
     * - All original UI screens and flows
     * - Complete face liveness detection  
     * - Document scanning with OCR and barcode reading
     * - NFC passport reading
     * - All functionality working exactly as in standalone app
     * - Themed with host app's branding via bridge
     * 
     * @param activity Host activity
     * @param callback Callback to receive verification results
     */
    fun startVerification(activity: Activity, callback: VerificationCallback) {
        try {
            android.util.Log.d(TAG, "🚀 Starting verification via standalone app bridge...")

            if (!isInitialized) {
                callback.onVerificationError(SDKError(
                    code = SDKErrorCode.INVALID_CONFIG,
                    message = "SDK not initialized. Call ArtiusIDSDK.initialize() first."
                ))
                return
            }

            // Store callback for when verification completes
            verificationCallback = callback

            // Ensure certificate is ready before starting verification
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = DeviceUtils.getDeviceId(activity)
                    android.util.Log.d(TAG, "🔐 Ensuring mTLS certificate is ready for verification...")
                    android.util.Log.d(TAG, "📱 Device ID: $deviceId")
                    android.util.Log.d(TAG, "🏢 Host Package: ${activity.packageName}")

                    // Force certificate check/generation before verification
                    val certManager = sharedContextManager?.getSharedCertificateManager() 
                        ?: com.artiusid.sdk.utils.CertificateManager(activity)
                    
                    val existingCert = certManager.loadCertificatePem()
                    if (existingCert == null) {
                        android.util.Log.w(TAG, "⚠️ No certificate found, will be generated during verification process")
                    } else {
                        android.util.Log.d(TAG, "✅ Certificate found, length: ${existingCert.length}")
                        
                        // Verify certificate-key match
                        val keyMatch = try {
                            certManager.verifyCertificateKeyMatch()
                        } catch (e: Exception) {
                            android.util.Log.w(TAG, "⚠️ Certificate key match verification failed: ${e.message}")
                            false
                        }
                        
                        if (!keyMatch) {
                            android.util.Log.w(TAG, "⚠️ Certificate-key mismatch, will regenerate during verification")
                        } else {
                            android.util.Log.d(TAG, "✅ Certificate and key match verified")
                        }
                    }

                    // Always launch verification - certificate will be handled by standalone app
                    CoroutineScope(Dispatchers.Main).launch {
                        standaloneAppBridge.startVerification(activity, callback)
                        android.util.Log.d(TAG, "🚀 Launched standalone application for verification")
                    }

                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Error during certificate check, but proceeding with verification", e)
                    
                    // Still launch verification - let standalone app handle certificate issues
                    CoroutineScope(Dispatchers.Main).launch {
                        standaloneAppBridge.startVerification(activity, callback)
                        android.util.Log.d(TAG, "🚀 Launched standalone application for verification (with cert warning)")
                    }
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to start verification flow", e)
            callback.onVerificationError(SDKError(
                code = SDKErrorCode.UNKNOWN_ERROR,
                message = "Failed to start verification: ${e.message}",
                cause = e
            ))
        }
    }
    
    /**
     * Start complete authentication flow using standalone application
     * 
     * @param activity Host activity
     * @param callback Callback to receive authentication results
     */
    fun startAuthentication(activity: Activity, callback: AuthenticationCallback) {
        try {
            android.util.Log.d(TAG, "🚀 Starting authentication via standalone app bridge...")
            
            if (!isInitialized) {
                callback.onAuthenticationError(SDKError(
                    code = SDKErrorCode.INVALID_CONFIG,
                    message = "SDK not initialized. Call ArtiusIDSDK.initialize() first."
                ))
                return
            }
            
            // Store callback for when authentication completes
            authenticationCallback = callback
            
            // Launch standalone application via bridge
            standaloneAppBridge.startAuthentication(activity, callback)
            
            android.util.Log.d(TAG, "✅ Launched standalone application for authentication")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to start authentication flow", e)
            callback.onAuthenticationError(SDKError(
                code = SDKErrorCode.UNKNOWN_ERROR,
                message = "Failed to start authentication: ${e.message}",
                cause = e
            ))
        }
    }
    
    /**
     * Get current theme configuration
     */
    fun getCurrentTheme(): SDKThemeConfiguration? = themeConfiguration
    
    /**
     * Get current SDK configuration  
     */
    fun getCurrentConfiguration(): SDKConfiguration? = sdkConfiguration
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get shared context manager for mTLS and Firebase context sharing
     * Internal use only - for SDK components that need shared context
     */
    internal fun getSharedContextManager(): SharedContextManager? = sharedContextManager
}