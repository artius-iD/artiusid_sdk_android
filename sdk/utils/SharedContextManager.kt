/*
 * File: SharedContextManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.services.APIManager

/**
 * Manages shared context between host application and SDK
 * Ensures mTLS certificates and Firebase tokens are shared properly
 */
class SharedContextManager(
    private val hostContext: Context,
    private val sdkConfiguration: SDKConfiguration
) {
    companion object {
        private const val TAG = "SharedContextManager"
        private var sharedTLSManager: TLSSessionManager? = null
        private var sharedCertManager: CertificateManager? = null
        private var sharedFirebaseManager: FirebaseTokenManager? = null
    }
    
    /**
     * Get shared TLS session manager using host app's certificate context
     */
    fun getSharedTLSManager(): TLSSessionManager {
        if (sharedTLSManager == null || !sdkConfiguration.sharedCertificateContext) {
            Log.d(TAG, "Creating shared TLS manager using host app context")
            sharedTLSManager = TLSSessionManager(hostContext)
        }
        return sharedTLSManager!!
    }
    
    /**
     * Get shared certificate manager using host app's certificate storage
     */
    fun getSharedCertificateManager(): CertificateManager {
        if (sharedCertManager == null || !sdkConfiguration.sharedCertificateContext) {
            Log.d(TAG, "Creating shared certificate manager using host app context")
            sharedCertManager = CertificateManager(hostContext)
        }
        return sharedCertManager!!
    }
    
    /**
     * Get shared Firebase token manager using host app's Firebase context
     */
    fun getSharedFirebaseManager(): FirebaseTokenManager? {
        if (sharedFirebaseManager == null || !sdkConfiguration.sharedFirebaseContext) {
            Log.d(TAG, "Creating shared Firebase manager using host app context")
            sharedFirebaseManager = FirebaseTokenManager.getInstance(hostContext)
        }
        return sharedFirebaseManager
    }
    
    /**
     * Alias for getSharedFirebaseManager() for consistency
     */
    fun getSharedFirebaseTokenManager(): FirebaseTokenManager? {
        return getSharedFirebaseManager()
    }
    
    /**
     * Get OkHttpClient configured with shared mTLS context
     */
    fun getSharedOkHttpClient(): OkHttpClient {
        Log.d(TAG, "🔐 Creating OkHttpClient with shared mTLS context from host app")
        return getSharedTLSManager().getOkHttpClient()
    }
    
    /**
     * Initialize shared certificate using host app's context
     */
    suspend fun ensureSharedCertificate(deviceId: String) {
        try {
            Log.d(TAG, "🔐 Ensuring shared certificate using host app context...")
            Log.d(TAG, "  - Host context: ${hostContext.packageName}")
            Log.d(TAG, "  - Device ID: $deviceId")
            Log.d(TAG, "  - Base URL: ${sdkConfiguration.baseUrl}")
            
            // Check environment settings in SharedPreferences
            val prefs = hostContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val environment = prefs.getString("environment", "NOT_SET")
            Log.d(TAG, "  - Environment in SharedPreferences: $environment")
            
            // Use UrlBuilder to get the correct certificate URL
            val certificateUrl = com.artiusid.sdk.utils.UrlBuilder.getLoadCertificateBaseUrl(hostContext)
            Log.d(TAG, "  - Certificate URL from UrlBuilder: $certificateUrl")
            
            // Check if certificate files are accessible in host context
            try {
                val assets = hostContext.assets
                val assetFiles = assets.list("") ?: emptyArray()
                Log.d(TAG, "  - Host app assets: ${assetFiles.joinToString()}")

                val resources = hostContext.resources
                val rawResourceId = resources.getIdentifier("api_cert_chain", "raw", hostContext.packageName)
                Log.d(TAG, "  - Host app raw resource ID for api_cert_chain: $rawResourceId")
            } catch (e: Exception) {
                Log.w(TAG, "  - Could not check host app resources: ${e.message}")
            }
            
            val apiManager = APIManager(hostContext)
            // Use the certificate URL from UrlBuilder instead of the base URL
            apiManager.ensureCertificate(deviceId, certificateUrl.removeSuffix("/"))
            
            Log.d(TAG, "✅ Shared certificate ready")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to ensure shared certificate using host context", e)
            // Don't use fallback - the host context should have the certificate files
            // If this fails, it means the certificate files are not properly copied to the sample app
            throw e
        }
    }
    
    /**
     * Clear shared contexts (useful for testing or environment changes)
     */
    fun clearSharedContexts() {
        Log.d(TAG, "🧹 Clearing shared contexts")
        sharedTLSManager = null
        sharedCertManager = null
        sharedFirebaseManager = null
    }
    
    /**
     * Log shared context status for debugging
     */
    fun logSharedContextStatus() {
        Log.d(TAG, "📊 Shared Context Status:")
        Log.d(TAG, "  - Certificate Context Shared: ${sdkConfiguration.sharedCertificateContext}")
        Log.d(TAG, "  - Firebase Context Shared: ${sdkConfiguration.sharedFirebaseContext}")
        Log.d(TAG, "  - Host Package: ${sdkConfiguration.hostAppPackageName}")
        Log.d(TAG, "  - TLS Manager: ${if (sharedTLSManager != null) "✅ Active" else "❌ Not initialized"}")
        Log.d(TAG, "  - Cert Manager: ${if (sharedCertManager != null) "✅ Active" else "❌ Not initialized"}")
        Log.d(TAG, "  - Firebase Manager: ${if (sharedFirebaseManager != null) "✅ Active" else "❌ Not initialized"}")
    }
}
