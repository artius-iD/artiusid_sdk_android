/*
 * File: FirebaseTokenManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Firebase Token Manager
 * Handles FCM token retrieval and secure storage similar to iOS keychain implementation
 */
class FirebaseTokenManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "FirebaseTokenManager"
        private const val PREF_NAME = "fcm_prefs"
        private const val TOKEN_KEY = "FCMRegistrationToken"
        
        @Volatile
        private var INSTANCE: FirebaseTokenManager? = null
        
        fun getInstance(context: Context? = null): FirebaseTokenManager? {
            return context?.let {
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: FirebaseTokenManager(it.applicationContext).also { INSTANCE = it }
                }
            }
        }
    }
    
    /**
     * Get cached FCM token synchronously (for immediate use)
     * Returns cached token or empty string if not available
     */
    fun getFCMToken(): String? {
        return try {
            // Only return cached token synchronously
            val cachedToken = getCachedToken()
            if (!cachedToken.isNullOrEmpty()) {
                Log.d(TAG, "✅ Using cached FCM token from keychain")
                return cachedToken
            } else {
                Log.d(TAG, "⚠️ No cached FCM token available in keychain")
                return ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting cached FCM token from keychain: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Get FCM token asynchronously, either from cache or by requesting a new one
     * Matches iOS behavior of checking cached token first
     */
    suspend fun getFCMTokenAsync(): String? {
        return try {
            // First try to get cached token
            val cachedToken = getCachedToken()
            if (!cachedToken.isNullOrEmpty()) {
                Log.d(TAG, "✅ Using cached FCM token from keychain")
                return cachedToken
            }
            
            // If no cached token, request new one
            Log.d(TAG, "🔄 No cached token found, requesting new FCM token")
            val newToken = FirebaseMessaging.getInstance().token.await()
            if (!newToken.isNullOrEmpty()) {
                saveToken(newToken)
                Log.d(TAG, "✅ Generated and saved new FCM token to keychain")
                newToken
            } else {
                Log.w(TAG, "⚠️ Failed to get FCM token")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting FCM token: ${e.message}", e)
            null
        }
    }
    
    /**
     * Save FCM token securely using EncryptedSharedPreferences
     * Equivalent to iOS keychain storage
     */
    fun saveToken(token: String) {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().putString(TOKEN_KEY, token).apply()
            Log.d(TAG, "✅ FCM token saved securely to keychain")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save FCM token to keychain: ${e.message}", e)
        }
    }
    
    /**
     * Get cached FCM token from secure storage
     */
    private fun getCachedToken(): String? {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.getString(TOKEN_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get cached FCM token from keychain: ${e.message}", e)
            null
        }
    }
    
    /**
     * Clear stored FCM token
     */
    fun clearToken() {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().remove(TOKEN_KEY).apply()
            Log.d(TAG, "✅ FCM token cleared from keychain")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to clear FCM token from keychain: ${e.message}", e)
        }
    }
    
    /**
     * Force fresh FCM token generation (clears cache and requests new token)
     */
    suspend fun forceFreshToken(): String? {
        return try {
            Log.d(TAG, "🔄 Forcing fresh FCM token generation")
            
            // Clear any cached token
            clearToken()
            
            // Request fresh token from Firebase
            val newToken = FirebaseMessaging.getInstance().token.await()
            if (!newToken.isNullOrEmpty()) {
                saveToken(newToken)
                Log.d(TAG, "✅ Generated and saved fresh FCM token")
                newToken
            } else {
                Log.w(TAG, "⚠️ Failed to generate fresh FCM token")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating fresh FCM token: ${e.message}", e)
            null
        }
    }
}