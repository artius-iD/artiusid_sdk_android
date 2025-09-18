/*
 * File: FirebaseTestManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Helper class to test and verify Firebase integration for real server notifications
 */
object FirebaseTestManager {
    private const val TAG = "FirebaseTestManager"
    
    /**
     * Check Firebase configuration and get current token info
     */
    suspend fun checkFirebaseStatus(context: Context): String {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                "❌ Firebase not initialized"
            } else {
                val app = FirebaseApp.getInstance()
                val projectId = app.options.projectId
                
                // Get current FCM token
                val token = FirebaseMessaging.getInstance().token.await()
                
                val status = buildString {
                    appendLine("✅ Firebase initialized")
                    appendLine("📱 Project: $projectId")
                    appendLine("🔑 FCM Token: ${token?.take(20)}...")
                    appendLine("📬 Ready for notifications")
                }
                
                Log.d(TAG, "Firebase Status:\n$status")
                Log.d(TAG, "Full FCM Token: $token")
                
                status
            }
        } catch (e: Exception) {
            val error = "❌ Firebase error: ${e.message}"
            Log.e(TAG, error, e)
            error
        }
    }
    
    /**
     * Get the current FCM token for server configuration
     */
    suspend fun getCurrentFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Current FCM Token: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Log Firebase configuration for debugging real server integration
     */
    fun logFirebaseConfig(context: Context) {
        try {
            val app = FirebaseApp.getInstance()
            Log.d(TAG, "=== FIREBASE CONFIGURATION ===")
            Log.d(TAG, "Project ID: ${app.options.projectId}")
            Log.d(TAG, "App ID: ${app.options.applicationId}")
            Log.d(TAG, "API Key: ${app.options.apiKey.take(10)}...")
            Log.d(TAG, "=== READY FOR REAL NOTIFICATIONS ===")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase not configured", e)
        }
    }
}