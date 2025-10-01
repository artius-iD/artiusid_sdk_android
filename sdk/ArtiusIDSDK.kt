/*
 * File: ArtiusIDSDK.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.artiusid.sdk.bridge.StandaloneAppBridge
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.models.SDKErrorCode
import com.artiusid.sdk.services.APIManager
import com.artiusid.sdk.util.DeviceUtils
import com.artiusid.sdk.utils.SharedContextManager
import com.artiusid.sdk.localization.LocalizationManager
import com.artiusid.sdk.utils.ImageOverrideInitializer
import com.artiusid.sdk.security.SDKSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * artius.iD SDK - Bridge to Complete Standalone Application
 * 
 * This SDK provides a bridge interface to the complete standalone artius.iD application.
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
    private var enhancedThemeConfiguration: EnhancedSDKThemeConfiguration? = null
    private var _isInitialized = false
    
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
            android.util.Log.i(TAG, "🌉 Initializing artius.iD SDK Bridge...")

            // ✅ CRITICAL: Validate security environment first
            if (!SDKSecurityManager.validateSecurityEnvironment(context)) {
                throw SecurityException("SDK initialization blocked due to security violations")
            }

            // Store configurations
            sdkConfiguration = configuration.copy(
                hostAppPackageName = context.packageName
            )
            themeConfiguration = theme
            
            // Initialize localization with overrides from host app
            com.artiusid.sdk.utils.LocalizationManager.initialize(configuration.localizationOverrides)

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
                    
                    // Initialize image override system after certificate is ready
                    android.util.Log.d(TAG, "🖼️ Initializing image override system after certificate setup...")
                    ImageOverrideInitializer.initialize(context, sdkConfiguration!!)
                    
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Certificate initialization failed, but continuing with SDK initialization", e)
                    // App continues without certificate - verification will handle this gracefully
                    
                    // Still initialize image override system even if certificate fails
                    try {
                        android.util.Log.d(TAG, "🖼️ Initializing image override system (certificate failed)...")
                        ImageOverrideInitializer.initialize(context, sdkConfiguration!!)
                    } catch (imageE: Exception) {
                        android.util.Log.e(TAG, "❌ Image override initialization also failed", imageE)
                    }
                }
            }

            // Initialize the bridge to standalone application
            standaloneAppBridge = StandaloneAppBridge(context)
            standaloneAppBridge.initialize(sdkConfiguration!!, theme)

            _isInitialized = true

            android.util.Log.i(TAG, "✅ artius.iD SDK Bridge initialized successfully")
            android.util.Log.i(TAG, "🎨 Theme: ${theme.brandName}")
            android.util.Log.i(TAG, "🏢 Environment: ${configuration.environment}")
            android.util.Log.i(TAG, "🌉 Bridge ready to launch standalone application")

        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to initialize artius.iD SDK Bridge", e)
            throw e
        }
    }
    
    /**
     * Initialize the SDK with enhanced theming configuration
     * @param context Application context
     * @param configuration SDK configuration (API keys, environment, etc.)
     * @param enhancedTheme Enhanced theme configuration with comprehensive theming options
     */
    fun initializeWithEnhancedTheme(
        context: Context, 
        configuration: SDKConfiguration,
        enhancedTheme: EnhancedSDKThemeConfiguration
    ) {
        try {
            android.util.Log.i(TAG, "🌉 Initializing artius.iD SDK Bridge with Enhanced Theming...")

            // Store configurations
            sdkConfiguration = configuration
            enhancedThemeConfiguration = enhancedTheme
            
            // Update the theme manager with the new theme
            com.artiusid.sdk.ui.theme.EnhancedThemeManager.updateCurrentThemeConfig(enhancedTheme)
            
            // Initialize localization with overrides from host app
            com.artiusid.sdk.utils.LocalizationManager.initialize(configuration.localizationOverrides)
            
            // Convert enhanced theme to basic theme for backward compatibility
            themeConfiguration = convertToBasicTheme(enhancedTheme)
            
            // Set environment name for logging
            val environmentName = when (configuration.environment) {
                com.artiusid.sdk.config.Environment.DEVELOPMENT -> "Development"
                com.artiusid.sdk.config.Environment.STAGING -> "Staging"
                com.artiusid.sdk.config.Environment.PRODUCTION -> "Production"
            }
            android.util.Log.i(TAG, "🌐 Environment set to: $environmentName")

            // Initialize shared context manager for mTLS and Firebase
            sharedContextManager = SharedContextManager(context, sdkConfiguration!!)
            sharedContextManager!!.logSharedContextStatus()

            // Initialize mTLS certificate using shared context (non-blocking)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    initializeSharedCertificate(context, sdkConfiguration!!)
                    
                    // Initialize image override system after certificate is ready
                    android.util.Log.d(TAG, "🖼️ Initializing image override system after certificate setup...")
                    ImageOverrideInitializer.initialize(context, sdkConfiguration!!)
                    
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Certificate initialization failed, but continuing with SDK initialization", e)
                    
                    // Still initialize image override system even if certificate fails
                    try {
                        android.util.Log.d(TAG, "🖼️ Initializing image override system (certificate failed)...")
                        ImageOverrideInitializer.initialize(context, sdkConfiguration!!)
                    } catch (imageE: Exception) {
                        android.util.Log.e(TAG, "❌ Image override initialization also failed", imageE)
                    }
                }
            }

            // Initialize the bridge to standalone application
            standaloneAppBridge = StandaloneAppBridge(context)
            standaloneAppBridge.initialize(sdkConfiguration!!, themeConfiguration!!)
            
            // Set enhanced theme if available
            if (enhancedThemeConfiguration != null) {
                standaloneAppBridge.setEnhancedTheme(enhancedThemeConfiguration!!)
            }
            
            android.util.Log.d(TAG, "🎨 Enhanced theme applied: ${enhancedTheme.brandName}")
            android.util.Log.d(TAG, "📱 Host package: ${context.packageName}")
            android.util.Log.d(TAG, "🔧 Environment: ${configuration.environment}")
            
            _isInitialized = true
            android.util.Log.i(TAG, "✅ ArtiusID SDK Bridge initialized successfully with Enhanced Theming")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to initialize ArtiusID SDK Bridge with Enhanced Theming", e)
            throw e
        }
    }
    
    /**
     * Convert enhanced theme configuration to basic theme for backward compatibility
     */
    private fun convertToBasicTheme(enhancedTheme: EnhancedSDKThemeConfiguration): SDKThemeConfiguration {
        return SDKThemeConfiguration(
            brandName = enhancedTheme.brandName,
            brandLogoUrl = enhancedTheme.brandLogoUrl,
            primaryColorHex = enhancedTheme.colorScheme.primaryColorHex,
            secondaryColorHex = enhancedTheme.colorScheme.secondaryColorHex,
            backgroundColorHex = enhancedTheme.colorScheme.backgroundColorHex,
            surfaceColorHex = enhancedTheme.colorScheme.surfaceColorHex,
            onPrimaryColorHex = enhancedTheme.colorScheme.onPrimaryColorHex,
            onSecondaryColorHex = enhancedTheme.colorScheme.onSecondaryColorHex,
            onBackgroundColorHex = enhancedTheme.colorScheme.onBackgroundColorHex,
            onSurfaceColorHex = enhancedTheme.colorScheme.onSurfaceColorHex,
            successColorHex = enhancedTheme.colorScheme.successColorHex,
            errorColorHex = enhancedTheme.colorScheme.errorColorHex,
            warningColorHex = enhancedTheme.colorScheme.warningColorHex,
            faceDetectionOverlayColorHex = enhancedTheme.colorScheme.faceDetectionOverlayColorHex,
            documentScanOverlayColorHex = enhancedTheme.colorScheme.documentScanOverlayColorHex,
            pendingStepColorHex = enhancedTheme.colorScheme.pendingStepColorHex,
            completedStepColorHex = enhancedTheme.colorScheme.completedStepColorHex,
            isDarkMode = enhancedTheme.colorScheme.backgroundColorHex == "#121212" || 
                        enhancedTheme.colorScheme.backgroundColorHex == "#000000"
        )
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

            if (!_isInitialized) {
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
     * Start complete authentication flow matching iOS standalone application exactly
     * Shows biometric prompt + progress screen + API authentication
     * 
     * @param activity Host activity
     * @param callback Callback to receive authentication results
     */
    fun startAuthentication(activity: Activity, callback: AuthenticationCallback) {
        try {
            android.util.Log.d(TAG, "🚀 Starting authentication flow matching iOS standalone app...")
            android.util.Log.d(TAG, "📱 Host activity: ${activity::class.simpleName}")
            android.util.Log.d(TAG, "📱 Host package: ${activity.packageName}")
            
            if (!_isInitialized) {
                callback.onAuthenticationError(SDKError(
                    code = SDKErrorCode.INVALID_CONFIG,
                    message = "SDK not initialized. Call ArtiusIDSDK.initialize() first."
                ))
                return
            }
            
            // Store callback for when authentication completes
            authenticationCallback = callback
            
            // Launch authentication screen that matches iOS flow exactly
            val intent = android.content.Intent(activity, com.artiusid.sdk.ui.activities.AuthenticationActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            
            android.util.Log.d(TAG, "🚀 Launching AuthenticationActivity...")
            activity.startActivity(intent)
            
            android.util.Log.d(TAG, "✅ Launched authentication screen matching iOS standalone app")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to start authentication flow", e)
            android.util.Log.e(TAG, "❌ Error details: ${e.message}")
            e.printStackTrace()
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
     * Get current enhanced theme configuration
     */
    fun getCurrentEnhancedTheme(): EnhancedSDKThemeConfiguration? = enhancedThemeConfiguration
    
    /**
     * Get current SDK configuration  
     */
    fun getCurrentConfiguration(): SDKConfiguration? = sdkConfiguration
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean {
        return _isInitialized
    }
    
    /**
     * Send approval request using the same logic as developer settings
     * @param context Application context
     * @return Triple<Boolean, String, Int?> - (success, message, requestId)
     */
    suspend fun sendApprovalRequest(context: Context): Triple<Boolean, String, Int?> {
        return try {
            if (!_isInitialized) {
                Triple(false, "SDK not initialized", null)
            } else {
                // Create approval API service using shared mTLS context
                val okHttpClient = sharedContextManager?.getSharedOkHttpClient() 
                    ?: throw IllegalStateException("Shared context not available")
                
                val retrofitFactory = com.artiusid.sdk.utils.RetrofitFactory(context)
                val approvalApiService = retrofitFactory.createApprovalRequestApiService(okHttpClient)
                
                // Log the API base URL being used
                val baseUrl = com.artiusid.sdk.utils.UrlBuilder.getApprovalRequestBaseUrl(context)
                android.util.Log.d(TAG, "🌐 Approval API Base URL: $baseUrl")
                android.util.Log.d(TAG, "🌐 Full endpoint: ${baseUrl}ApprovalRequestTestingFunction")
                
                // Create SettingsRepository with proper API service
                val settingsRepository = com.artiusid.sdk.data.repository.SettingsRepository(context, approvalApiService)
                
                // Send approval request
                settingsRepository.sendApprovalRequest()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Error sending approval request", e)
            Triple(false, "Error: ${e.message}", null)
        }
    }
    
    /**
     * Send approval response (approve/deny) using the same logic as iOS ApprovalResponse
     * @param context Application context
     * @param approvalValue "yes" for approve, "no" for deny
     * @return ApprovalResultData or null if failed
     */
    suspend fun sendApprovalResponse(context: Context, approvalValue: String): com.artiusid.sdk.data.model.ApprovalResultData? {
        return try {
            if (!_isInitialized) {
                android.util.Log.e(TAG, "❌ SDK not initialized for approval response")
                null
            } else {
                // Create approval API service using shared mTLS context
                val okHttpClient = sharedContextManager?.getSharedOkHttpClient() 
                    ?: throw IllegalStateException("Shared context not available")
                
                val retrofitFactory = com.artiusid.sdk.utils.RetrofitFactory(context)
                val approvalApiService = retrofitFactory.createApprovalRequestApiService(okHttpClient)
                
                // Create ApprovalResponse utility exactly like iOS
                val approvalResponse = com.artiusid.sdk.utils.ApprovalResponse(context, approvalApiService)
                
                // Send approval response
                approvalResponse.sendApprovalResponse(approvalValue)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Error sending approval response", e)
            null
        }
    }

    /**
     * Get shared context manager for mTLS and Firebase context sharing
     * Internal use only - for SDK components that need shared context
     */
    internal fun getSharedContextManager(): SharedContextManager? = sharedContextManager
}