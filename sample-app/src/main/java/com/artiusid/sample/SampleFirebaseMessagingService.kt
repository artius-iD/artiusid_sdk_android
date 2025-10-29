/*
 * File: SampleFirebaseMessagingService.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.utils.FirebaseTokenManager

/**
 * Sample App's Firebase Messaging Service
 * 
 * ARCHITECTURAL DECISION:
 * - Sample app manages its own Firebase tokens AND notifications
 * - SDK receives FCM token from sample app via customFcmToken
 * - Sample app has full control over notification display and handling
 */
class SampleFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "SampleFCMService"
        private const val CHANNEL_ID = "sample_app_notifications"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥 ========================================")
        Log.d(TAG, "🔥 Sample App Firebase Messaging Service created")
        Log.d(TAG, "🔥 Sample app controls ALL Firebase functionality")
        Log.d(TAG, "🔥 ========================================")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d(TAG, "🔥 ========================================")
        Log.d(TAG, "🔥 NEW FCM TOKEN RECEIVED BY SAMPLE APP")
        Log.d(TAG, "🔥 Token: ${token.take(20)}...")
        Log.d(TAG, "🔥 Token length: ${token.length} characters")
        Log.d(TAG, "🔥 ========================================")

        // Save token to sample app's secure storage
        val tokenManager = FirebaseTokenManager.getInstance(applicationContext)
        
        // Get current environment from UrlBuilder
        val currentEnvironment = com.artiusid.sdk.utils.UrlBuilder.getCurrentEnvironment(applicationContext)
        val environmentForStorage = when (currentEnvironment.uppercase()) {
            "SANDBOX" -> "Sandbox"
            "DEVELOPMENT" -> "Development"
            "STAGING" -> "Staging"
            else -> "Sandbox"
        }
        
        tokenManager?.saveToken(token, environmentForStorage)
        Log.d(TAG, "✅ FCM token saved to sample app's secure storage for environment: $environmentForStorage")
        
        // Provide token to SDK
        ArtiusIDSDK.updateFcmToken(token)
        Log.d(TAG, "✅ FCM token provided to SDK via updateFcmToken()")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "🔔 ========================================")
        Log.d(TAG, "🔔 FCM MESSAGE RECEIVED BY SAMPLE APP")
        Log.d(TAG, "🔔 Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "🔔 From: ${remoteMessage.from}")
        Log.d(TAG, "🔔 Data payload: ${remoteMessage.data}")
        Log.d(TAG, "🔔 Notification payload: ${remoteMessage.notification}")
        Log.d(TAG, "🔔 ========================================")

        // Check if this is an approval notification
        val approvalTitle = remoteMessage.data["approvalTitle"]
        val approvalDescription = remoteMessage.data["approvalDescription"]
        val requestId = remoteMessage.data["requestId"]
        
        if (!approvalTitle.isNullOrEmpty() && !approvalDescription.isNullOrEmpty()) {
            Log.d(TAG, "🔔 APPROVAL NOTIFICATION DETECTED")
            Log.d(TAG, "🔔 Title: $approvalTitle")
            Log.d(TAG, "🔔 Description: $approvalDescription")
            Log.d(TAG, "🔔 Request ID: $requestId")
        }

        // Show notification
        showNotification(remoteMessage)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create high-priority notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sample App Notifications",
                NotificationManager.IMPORTANCE_HIGH // HIGH importance for approval notifications
            ).apply {
                description = "Approval requests and authentication notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                Log.d(TAG, "🔔 Created HIGH importance notification channel: $CHANNEL_ID")
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "🔔 Notification channel registered with NotificationManager")
        }
        
        // Create intent to open sample app
        val intent = Intent(this, BridgeMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Add notification data as extras
            val approvalTitle = remoteMessage.data["approvalTitle"]
            val approvalDescription = remoteMessage.data["approvalDescription"]
            val requestId = remoteMessage.data["requestId"]
            
            if (approvalTitle != null && approvalDescription != null) {
                putExtra("approvalTitle", approvalTitle)
                putExtra("approvalDescription", approvalDescription)
                requestId?.let { putExtra("requestId", it) }
                Log.d(TAG, "🚀 Adding approval data to intent - Title: $approvalTitle")
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build high-priority notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(remoteMessage.notification?.title ?: "artius.iD Sample App")
            .setContentText(remoteMessage.notification?.body ?: "You have a new notification.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notification
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable sound, vibration, lights
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Message category for approval requests
        
        val notificationId = System.currentTimeMillis().toInt()
        Log.d(TAG, "🔔 ========================================")
        Log.d(TAG, "🔔 DISPLAYING NOTIFICATION")
        Log.d(TAG, "🔔 Notification ID: $notificationId")
        Log.d(TAG, "🔔 Title: ${remoteMessage.notification?.title ?: "artius.iD Sample App"}")
        Log.d(TAG, "🔔 Body: ${remoteMessage.notification?.body ?: "You have a new notification."}")
        Log.d(TAG, "🔔 Priority: HIGH, Sound: ENABLED, Vibration: ENABLED")
        Log.d(TAG, "🔔 ========================================")
        
        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }
}

