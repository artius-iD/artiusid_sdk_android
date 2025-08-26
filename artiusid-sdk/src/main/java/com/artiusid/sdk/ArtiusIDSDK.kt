package com.artiusid.sdk

import android.app.Activity
import android.content.Context

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
    private const val TAG = "ArtiusIDSDK"
    
    /**
     * Initialize the SDK with configuration
     * 
     * @param context Application context
     * @param apiKey Your API key
     */
    fun initialize(context: Context, apiKey: String) {
        if (isInitialized) {
            android.util.Log.w(TAG, "SDK already initialized")
            return
        }
        
        try {
            // Initialize SDK components
            isInitialized = true
            android.util.Log.i(TAG, "ArtiusID SDK initialized successfully")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to initialize SDK", e)
            throw RuntimeException("SDK initialization failed: ${e.message}", e)
        }
    }
    
    /**
     * Start face liveness detection
     * 
     * @param activity Calling activity
     */
    fun startFaceLiveness(activity: Activity) {
        ensureInitialized()
        android.util.Log.d(TAG, "Starting face liveness detection")
        // Implementation will be added
    }
    
    /**
     * Start document scanning
     * 
     * @param activity Calling activity
     */
    fun startDocumentScan(activity: Activity) {
        ensureInitialized()
        android.util.Log.d(TAG, "Starting document scanning")
        // Implementation will be added
    }
    
    /**
     * Get SDK version information
     */
    fun getVersionInfo(): String {
        return "1.0.0"
    }
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("SDK not initialized. Call ArtiusIDSDK.initialize() first.")
        }
    }
}
