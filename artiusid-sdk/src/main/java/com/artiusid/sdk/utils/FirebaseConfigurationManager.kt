/*
 * File: FirebaseConfigurationManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.config.SDKConfiguration

/**
 * Firebase Configuration Manager
 * Handles optional Firebase functionality based on SDK configuration
 * 
 * NEW in SDK v1.2.43:
 * - Allows clients to disable SDK's Firebase notification handling
 * - Supports client-provided FCM tokens
 * - Maintains backward compatibility with existing Firebase integration
 */
object FirebaseConfigurationManager {
    
    private const val TAG = "FirebaseConfigManager"
    
    @Volatile
    private var sdkConfiguration: SDKConfiguration? = null
    
    @Volatile
    private var clientProvidedToken: String? = null
    
    /**
     * Initialize Firebase configuration from SDK settings
     */
    fun initialize(configuration: SDKConfiguration) {
        sdkConfiguration = configuration
        clientProvidedToken = configuration.customFcmToken
        
        Log.i(TAG, "🔥 ========================================")
        Log.i(TAG, "🔥 Firebase Configuration Initialized")
        Log.i(TAG, "🔥 Handle Notifications: ${configuration.handleFirebaseNotifications}")
        Log.i(TAG, "🔥 Custom FCM Token: ${if (configuration.customFcmToken != null) "PROVIDED" else "NOT PROVIDED"}")
        Log.i(TAG, "🔥 Shared Firebase Context: ${configuration.sharedFirebaseContext}")
        Log.i(TAG, "🔥 ========================================")
    }
    
    /**
     * Check if SDK should handle Firebase notifications
     */
    fun shouldHandleNotifications(): Boolean {
        return sdkConfiguration?.handleFirebaseNotifications ?: true
    }
    
    /**
     * Get FCM token based on configuration
     * Priority: 1) Client-provided token, 2) SDK's Firebase token, 3) Empty string
     */
    suspend fun getFcmToken(context: Context): String {
        return try {
            // Priority 1: Use client-provided token if available
            clientProvidedToken?.let { token ->
                if (token.isNotEmpty()) {
                    Log.d(TAG, "✅ Using client-provided FCM token")
                    return token
                }
            }
            
            // Priority 2: Use SDK's Firebase token if notifications are enabled
            if (shouldHandleNotifications()) {
                val firebaseTokenManager = FirebaseTokenManager.getInstance(context)
                val sdkToken = firebaseTokenManager?.getFCMTokenAsync()
                if (!sdkToken.isNullOrEmpty()) {
                    Log.d(TAG, "✅ Using SDK's Firebase FCM token")
                    return sdkToken
                } else {
                    Log.w(TAG, "⚠️ SDK Firebase enabled but no token available")
                }
            } else {
                Log.d(TAG, "ℹ️ SDK Firebase notifications disabled - no token from SDK")
            }
            
            // Priority 3: Return empty string if no token available
            Log.w(TAG, "⚠️ No FCM token available (client or SDK)")
            ""
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting FCM token: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Get FCM token synchronously (cached only)
     */
    fun getFcmTokenSync(context: Context): String {
        return try {
            // Priority 1: Use client-provided token if available
            clientProvidedToken?.let { token ->
                if (token.isNotEmpty()) {
                    Log.d(TAG, "✅ Using client-provided FCM token (sync)")
                    return token
                }
            }
            
            // Priority 2: Use SDK's cached Firebase token if notifications are enabled
            if (shouldHandleNotifications()) {
                val firebaseTokenManager = FirebaseTokenManager.getInstance(context)
                val sdkToken = firebaseTokenManager?.getFCMToken()
                if (!sdkToken.isNullOrEmpty()) {
                    Log.d(TAG, "✅ Using SDK's cached Firebase FCM token (sync)")
                    return sdkToken
                } else {
                    Log.w(TAG, "⚠️ SDK Firebase enabled but no cached token available")
                }
            } else {
                Log.d(TAG, "ℹ️ SDK Firebase notifications disabled - no token from SDK (sync)")
            }
            
            // Priority 3: Return empty string if no token available
            Log.w(TAG, "⚠️ No FCM token available (client or SDK) - sync")
            ""
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting FCM token (sync): ${e.message}", e)
            ""
        }
    }
    
    /**
     * Update client-provided FCM token at runtime
     * Useful when client's FCM token changes
     */
    fun updateClientFcmToken(token: String?) {
        clientProvidedToken = token
        Log.i(TAG, "🔄 Client FCM token updated: ${if (token != null) "PROVIDED" else "REMOVED"}")
    }
    
    /**
     * Check if Firebase messaging service should be active
     */
    fun shouldActivateMessagingService(): Boolean {
        return shouldHandleNotifications() && (sdkConfiguration?.sharedFirebaseContext ?: true)
    }
    
    /**
     * Get debug information about Firebase configuration
     */
    fun getDebugInfo(): String {
        val config = sdkConfiguration
        return if (config != null) {
            """
            Firebase Configuration:
            - Handle Notifications: ${config.handleFirebaseNotifications}
            - Custom FCM Token: ${if (config.customFcmToken != null) "PROVIDED (${config.customFcmToken?.take(20)}...)" else "NOT PROVIDED"}
            - Client Runtime Token: ${if (clientProvidedToken != null) "PROVIDED (${clientProvidedToken?.take(20)}...)" else "NOT PROVIDED"}
            - Shared Firebase Context: ${config.sharedFirebaseContext}
            - Should Handle Notifications: ${shouldHandleNotifications()}
            - Should Activate Messaging Service: ${shouldActivateMessagingService()}
            """.trimIndent()
        } else {
            "Firebase Configuration: NOT INITIALIZED"
        }
    }
}
