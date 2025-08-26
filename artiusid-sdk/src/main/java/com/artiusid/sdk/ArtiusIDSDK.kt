package com.artiusid.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.artiusid.sdk.callbacks.*
import com.artiusid.sdk.config.*
import com.artiusid.sdk.models.*
import com.artiusid.sdk.ui.activities.*

/**
 * Main entry point for the ArtiusID Android SDK
 * 
 * This class provides the primary interface for integrating identity verification
 * capabilities into your Android application.
 * 
 * @author ArtiusID Team
 * @version 1.0.0
 */
object ArtiusIDSDK {
    
    private var isInitialized = false
    private var config: ArtiusSDKConfig? = null
    private const val TAG = "ArtiusIDSDK"
    
    // Callback storage
    private var verificationCallback: VerificationCallback? = null
    private var livenessCallback: LivenessCallback? = null
    private var documentScanCallback: DocumentScanCallback? = null
    private var nfcCallback: NFCCallback? = null
    private var authenticationCallback: AuthenticationCallback? = null
    
    /**
     * Initialize the SDK with configuration
     * 
     * @param context Application context
     * @param config SDK configuration
     */
    fun initialize(context: Context, config: ArtiusSDKConfig) {
        if (isInitialized) {
            android.util.Log.w(TAG, "SDK already initialized")
            return
        }
        
        try {
            this.config = config
            
            // Initialize SDK components here
            // - Configuration managers
            // - Firebase if provided
            // - Device capability checks
            
            isInitialized = true
            android.util.Log.i(TAG, "ArtiusID SDK initialized successfully")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to initialize SDK", e)
            throw RuntimeException("SDK initialization failed: ${e.message}", e)
        }
    }
    
    /**
     * Initialize the SDK with simple API key (for basic usage)
     * 
     * @param context Application context
     * @param apiKey Your API key
     */
    fun initialize(context: Context, apiKey: String) {
        val config = ArtiusSDKConfig.Builder()
            .setApiKey(apiKey)
            .setEnvironment(Environment.PRODUCTION)
            .build()
        
        initialize(context, config)
    }
    
    /**
     * Start complete verification flow (Face + Document + NFC)
     * 
     * @param activity Calling activity
     * @param config Verification configuration
     * @param callback Result callback
     */
    fun startVerificationFlow(
        activity: Activity,
        config: VerificationConfig? = null,
        callback: VerificationCallback
    ) {
        ensureInitialized()
        
        verificationCallback = callback
        
        val intent = Intent(activity, VerificationFlowActivity::class.java).apply {
            config?.let { putExtra("verification_config", it) }
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Start face liveness detection only
     * 
     * @param activity Calling activity
     * @param config Liveness configuration
     * @param callback Result callback
     */
    fun startFaceLiveness(
        activity: Activity,
        config: LivenessConfig? = null,
        callback: LivenessCallback
    ) {
        ensureInitialized()
        
        livenessCallback = callback
        
        val intent = Intent(activity, FaceLivenessActivity::class.java).apply {
            config?.let { putExtra("liveness_config", it) }
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Start face liveness detection (simple version)
     * 
     * @param activity Calling activity
     */
    fun startFaceLiveness(activity: Activity) {
        startFaceLiveness(activity, null, object : LivenessCallback {
            override fun onSuccess(result: LivenessResult) {
                android.util.Log.d(TAG, "Face liveness completed successfully")
            }
            
            override fun onError(error: SDKError) {
                android.util.Log.e(TAG, "Face liveness failed: ${error.message}")
            }
            
            override fun onCancelled() {
                android.util.Log.d(TAG, "Face liveness cancelled")
            }
        })
    }
    
    /**
     * Start document scanning only
     * 
     * @param activity Calling activity
     * @param documentType Type of document to scan
     * @param config Document configuration
     * @param callback Result callback
     */
    fun startDocumentScan(
        activity: Activity,
        documentType: DocumentType = DocumentType.ID_CARD,
        config: DocumentConfig? = null,
        callback: DocumentScanCallback
    ) {
        ensureInitialized()
        
        documentScanCallback = callback
        
        val intent = Intent(activity, DocumentScanActivity::class.java).apply {
            putExtra("document_type", documentType.name)
            config?.let { putExtra("document_config", it) }
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Start document scanning (simple version)
     * 
     * @param activity Calling activity
     */
    fun startDocumentScan(activity: Activity) {
        startDocumentScan(activity, DocumentType.ID_CARD, null, object : DocumentScanCallback {
            override fun onSuccess(result: DocumentScanResult) {
                android.util.Log.d(TAG, "Document scan completed successfully")
            }
            
            override fun onError(error: SDKError) {
                android.util.Log.e(TAG, "Document scan failed: ${error.message}")
            }
            
            override fun onCancelled() {
                android.util.Log.d(TAG, "Document scan cancelled")
            }
        })
    }
    
    /**
     * Get SDK version information
     */
    fun getVersionInfo(): String {
        return "1.0.0"
    }
    
    /**
     * Get detailed SDK version information
     */
    fun getDetailedVersionInfo(): SDKVersionInfo {
        return SDKVersionInfo(
            version = "1.0.0",
            buildNumber = "1",
            buildDate = "2024-08-26",
            features = listOf("FaceLiveness", "DocumentScan", "NFCReading", "Authentication")
        )
    }
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get current SDK configuration
     */
    fun getConfiguration(): ArtiusSDKConfig? = config
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        ensureInitialized()
        
        // Clear image storage
        com.artiusid.sdk.utils.ImageStorage.clearAll()
        
        android.util.Log.i(TAG, "SDK cache cleared")
    }
    
    // Internal callback getters for activities
    internal fun getVerificationCallback(): VerificationCallback? = verificationCallback
    internal fun getLivenessCallback(): LivenessCallback? = livenessCallback
    internal fun getDocumentScanCallback(): DocumentScanCallback? = documentScanCallback
    internal fun getNFCCallback(): NFCCallback? = nfcCallback
    internal fun getAuthenticationCallback(): AuthenticationCallback? = authenticationCallback
    
    // Internal callback clearers
    internal fun clearVerificationCallback() { verificationCallback = null }
    internal fun clearLivenessCallback() { livenessCallback = null }
    internal fun clearDocumentScanCallback() { documentScanCallback = null }
    internal fun clearNFCCallback() { nfcCallback = null }
    internal fun clearAuthenticationCallback() { authenticationCallback = null }
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("SDK not initialized. Call ArtiusIDSDK.initialize() first.")
        }
    }
}
