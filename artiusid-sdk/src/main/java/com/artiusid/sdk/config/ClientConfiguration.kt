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
        Log.d(TAG, "✅ Client configuration initialized:")
        Log.d(TAG, "   clientId: ${config.clientId}")
        Log.d(TAG, "   clientGroupId: ${config.clientGroupId}")
        Log.d(TAG, "   environment: ${config.environment}")
    }
    
    /**
     * Get the configured client ID
     * Defaults to 1 if not configured (backward compatibility)
     */
    fun getClientId(): Int {
        val clientId = currentConfig?.clientId ?: 1
        if (currentConfig == null) {
            Log.w(TAG, "⚠️ ClientConfiguration not initialized, using default clientId=1")
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
            Log.w(TAG, "⚠️ ClientConfiguration not initialized, using default clientGroupId=1")
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
