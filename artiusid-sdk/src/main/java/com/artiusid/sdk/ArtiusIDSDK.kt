package com.artiusid.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.callbacks.LivenessCallback
import com.artiusid.sdk.callbacks.DocumentScanCallback
import com.artiusid.sdk.callbacks.NFCReadingCallback
import com.artiusid.sdk.config.ArtiusSDKConfig
import com.artiusid.sdk.managers.SDKConfigManager
import com.artiusid.sdk.managers.AnalyticsManager
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.models.SDKErrorCode
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.LivenessResult
import com.artiusid.sdk.models.DocumentScanResult
import com.artiusid.sdk.models.NFCPassportResult
import com.artiusid.sdk.ui.activities.SDKMainActivity

/**
 * Main entry point for the ArtiusID Android SDK
 * 
 * This SDK provides complete identity verification and authentication flows
 * by wrapping the entire standalone application experience. When the host app
 * calls the SDK, it launches the complete standalone app UI/UX internally.
 * 
 * The SDK acts as a wrapper around the sophisticated standalone application,
 * providing the exact same user experience while allowing host apps to 
 * integrate with just a few method calls.
 * 
 * @author ArtiusID Team
 * @version 2.0.0
 */
object ArtiusIDSDK {
    
    // Callback storage for returning results to host app
    var verificationCallback: VerificationCallback? = null
    var authenticationCallback: AuthenticationCallback? = null
    var livenessCallback: LivenessCallback? = null
    var documentScanCallback: DocumentScanCallback? = null
    var nfcReadingCallback: NFCReadingCallback? = null
    
    private const val TAG = "ArtiusIDSDK"
    
    /**
     * Initialize the SDK with comprehensive configuration
     * 
     * This sets up the SDK to use the host app's branding, theme, and configuration
     * while providing the complete standalone application experience.
     * 
     * @param context Application context
     * @param config Complete SDK configuration including theme, security, and API settings
     */
    fun initialize(context: Context, config: ArtiusSDKConfig) {
        try {
            android.util.Log.i(TAG, "Initializing ArtiusID SDK v2.0.0...")
            
            // Initialize configuration manager with host app settings
            SDKConfigManager.initialize(context, config)
            
            android.util.Log.i(TAG, "ArtiusID SDK initialized successfully")
            android.util.Log.i(TAG, "SDK will provide complete standalone app experience")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to initialize ArtiusID SDK", e)
            throw e
        }
    }
    
    /**
     * Start complete verification flow with standalone app UI/UX
     * 
     * This launches the COMPLETE standalone application verification experience:
     * - All the sophisticated UI screens from standalone app
     * - Face liveness detection with advanced ML Kit integration
     * - Document type selection with beautiful UI
     * - Document capture with real camera, OCR, barcode scanning
     * - NFC passport reading with chip authentication
     * - Complete verification processing and results
     * - All themed with host app's branding
     * 
     * The host app will see the SDK launch and then receive results when complete.
     * 
     * @param activity Calling activity
     * @param callback Result callback for verification completion
     */
    fun startVerificationFlow(
        activity: Activity,
        callback: VerificationCallback
    ) {
        try {
            android.util.Log.d(TAG, "Starting COMPLETE verification flow with standalone UI/UX...")
            
            if (!SDKConfigManager.isInitialized()) {
                callback.onVerificationError(SDKError(
                    code = SDKErrorCode.INVALID_CONFIG,
                    message = "SDK not initialized. Call ArtiusIDSDK.initialize() first."
                ))
                return
            }
            
            // Track analytics
            AnalyticsManager.trackVerificationStarted()
            
            // Store callback for when standalone app completes
            verificationCallback = callback
            
            // Launch the COMPLETE standalone app experience
            val intent = Intent(activity, SDKMainActivity::class.java).apply {
                putExtra(SDKMainActivity.EXTRA_FLOW_TYPE, SDKMainActivity.FLOW_TYPE_VERIFICATION)
                putExtra("start_time", System.currentTimeMillis())
            }
            
            activity.startActivity(intent)
            android.util.Log.d(TAG, "Launched complete standalone verification experience")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to start verification flow", e)
            callback.onVerificationError(SDKError(
                code = SDKErrorCode.UNKNOWN_ERROR,
                message = "Failed to start verification: ${e.message}",
                cause = e
            ))
        }
    }
    
    /**
     * Start complete authentication flow with standalone app UI/UX
     * 
     * This launches the COMPLETE standalone application authentication experience:
     * - All the sophisticated UI screens from standalone app
     * - Biometric authentication with advanced face recognition
     * - Device binding and security checks
     * - Secure token exchange with mTLS
     * - Complete authentication processing and results
     * - All themed with host app's branding
     * 
     * The host app will see the SDK launch and then receive results when complete.
     * 
     * @param activity Calling activity
     * @param callback Result callback for authentication completion
     */
    fun startAuthenticationFlow(
        activity: Activity,
        callback: AuthenticationCallback
    ) {
        try {
            android.util.Log.d(TAG, "Starting COMPLETE authentication flow with standalone UI/UX...")
            
            if (!SDKConfigManager.isInitialized()) {
                callback.onAuthenticationError(SDKError(
                    code = SDKErrorCode.INVALID_CONFIG,
                    message = "SDK not initialized. Call ArtiusIDSDK.initialize() first."
                ))
                return
            }
            
            // Track analytics
            AnalyticsManager.trackAuthenticationStarted()
            
            // Store callback for when standalone app completes
            authenticationCallback = callback
            
            // Launch the COMPLETE standalone app experience
            val intent = Intent(activity, SDKMainActivity::class.java).apply {
                putExtra(SDKMainActivity.EXTRA_FLOW_TYPE, SDKMainActivity.FLOW_TYPE_AUTHENTICATION)
                putExtra("start_time", System.currentTimeMillis())
            }
            
            activity.startActivity(intent)
            android.util.Log.d(TAG, "Launched complete standalone authentication experience")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to start authentication flow", e)
            callback.onAuthenticationError(SDKError(
                code = SDKErrorCode.UNKNOWN_ERROR,
                message = "Failed to start authentication: ${e.message}",
                cause = e
            ))
        }
    }
    
    /**
     * Get SDK version information
     */
    fun getVersion(): String = "2.0.0"
    
    /**
     * Get SDK build information
     */
    fun getBuildInfo(): Map<String, String> = mapOf(
        "version" to "2.0.0",
        "buildDate" to "2024-01-01",
        "platform" to "Android",
        "type" to "Complete Standalone Wrapper",
        "minSdkVersion" to "24",
        "targetSdkVersion" to "34"
    )
    
    /**
     * Check if SDK is properly initialized
     */
    fun isInitialized(): Boolean = SDKConfigManager.isInitialized()
    
    /**
     * Get current SDK configuration (for debugging)
     */
    fun getConfiguration(): ArtiusSDKConfig? = try {
        SDKConfigManager.getConfig()
    } catch (e: Exception) {
        null
    }
    
    // Internal methods for the standalone app to call back to host app
    
    /**
     * Internal method called by standalone app when verification completes
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyVerificationComplete(result: VerificationResult) {
        android.util.Log.d(TAG, "Verification completed - notifying host app")
        AnalyticsManager.trackVerificationCompleted(result.success, result.confidence)
        verificationCallback?.onVerificationComplete(result)
        verificationCallback = null
    }
    
    /**
     * Internal method called by standalone app when verification fails
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyVerificationError(error: SDKError) {
        android.util.Log.d(TAG, "Verification failed - notifying host app")
        AnalyticsManager.trackVerificationCompleted(false, 0.0f)
        verificationCallback?.onVerificationError(error)
        verificationCallback = null
    }
    
    /**
     * Internal method called by standalone app when verification is cancelled
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyVerificationCancelled() {
        android.util.Log.d(TAG, "Verification cancelled - notifying host app")
        verificationCallback?.onVerificationCancelled()
        verificationCallback = null
    }
    
    /**
     * Internal method called by standalone app when authentication completes
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyAuthenticationComplete(result: AuthenticationResult) {
        android.util.Log.d(TAG, "Authentication completed - notifying host app")
        AnalyticsManager.trackAuthenticationCompleted(result.success)
        authenticationCallback?.onAuthenticationComplete(result)
        authenticationCallback = null
    }
    
    /**
     * Internal method called by standalone app when authentication fails
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyAuthenticationError(error: SDKError) {
        android.util.Log.d(TAG, "Authentication failed - notifying host app")
        AnalyticsManager.trackAuthenticationCompleted(false)
        authenticationCallback?.onAuthenticationError(error)
        authenticationCallback = null
    }
    
    /**
     * Internal method called by standalone app when authentication is cancelled
     * This should only be called by the SDK's internal navigation system
     */
    internal fun notifyAuthenticationCancelled() {
        android.util.Log.d(TAG, "Authentication cancelled - notifying host app")
        authenticationCallback?.onAuthenticationCancelled()
        authenticationCallback = null
    }
}