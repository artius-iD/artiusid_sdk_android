/*
 * File: ClientConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.config

import android.util.Log

/**
 * Client Configuration Manager
 * 
 * Provides centralized access to clientId and clientGroupId configuration
 * Matches iOS SDK AppConstants functionality for multi-client support
 * 
 * This allows different applications to use different client identities:
 * - Sample App: clientId=1, clientGroupId=1
 * - TriNet App: clientId=2, clientGroupId=2
 * - Other Apps: clientId=N, clientGroupId=N
 */
object ClientConfiguration {
    private const val TAG = "ClientConfiguration"
    
    // Current configuration (set during SDK initialization)
    private var currentConfig: SDKConfiguration? = null
    
    /**
     * Initialize the client configuration
     * Called during SDK initialization with the host app's configuration
     */
    fun initialize(config: SDKConfiguration) {
        currentConfig = config
        Log.i(TAG, "🎯 ========================================")
        Log.i(TAG, "🎯 CLIENT CONFIGURATION INITIALIZED")
        Log.i(TAG, "🎯 clientId: ${config.clientId}")
        Log.i(TAG, "🎯 clientGroupId: ${config.clientGroupId}")
        Log.i(TAG, "🎯 environment: ${config.environment}")
        Log.i(TAG, "🎯 ========================================")
    }
    
    /**
     * Get the configured client ID
     * Defaults to 1 if not configured (backward compatibility)
     */
    fun getClientId(): Int {
        val clientId = currentConfig?.clientId ?: 1
        if (currentConfig == null) {
            Log.e(TAG, "🚨 CRITICAL: ClientConfiguration not initialized, using default clientId=1")
            Log.e(TAG, "🚨 This indicates a timing issue - SDK components accessing clientId before initialization")
            // Print stack trace to identify where this is being called from
            Thread.currentThread().stackTrace.take(10).forEach { element ->
                Log.e(TAG, "🚨   at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        } else {
            Log.d(TAG, "✅ Using configured clientId: $clientId")
        }
        return clientId
    }
    
    /**
     * Get the configured client group ID
     * Defaults to 1 if not configured (backward compatibility)
     */
    fun getClientGroupId(): Int {
        val clientGroupId = currentConfig?.clientGroupId ?: 1
        if (currentConfig == null) {
            Log.e(TAG, "🚨 CRITICAL: ClientConfiguration not initialized, using default clientGroupId=1")
            Log.e(TAG, "🚨 This indicates a timing issue - SDK components accessing clientGroupId before initialization")
            // Print stack trace to identify where this is being called from
            Thread.currentThread().stackTrace.take(10).forEach { element ->
                Log.e(TAG, "🚨   at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        } else {
            Log.d(TAG, "✅ Using configured clientGroupId: $clientGroupId")
        }
        return clientGroupId
    }
    
    /**
     * Get the current configuration (for debugging)
     */
    fun getCurrentConfig(): SDKConfiguration? = currentConfig
    
    /**
     * Check if configuration is initialized
     */
    fun isInitialized(): Boolean = currentConfig != null
    
    /**
     * Get whether Okta ID should be included in verification payload (NEW - matches iOS v2.0.12)
     * Defaults to true if not configured
     */
    fun shouldIncludeOktaID(): Boolean {
        val includeOktaID = currentConfig?.includeOktaIDInVerificationPayload ?: true
        Log.d(TAG, "✅ Okta ID inclusion: $includeOktaID")
        return includeOktaID
    }
    
    /**
     * Get a readable description of the current client configuration
     */
    fun getConfigDescription(): String {
        return if (currentConfig != null) {
            "Client(id=${getClientId()}, groupId=${getClientGroupId()}, env=${currentConfig!!.environment})"
        } else {
            "Client(UNINITIALIZED - using defaults: id=1, groupId=1)"
        }
    }
}
