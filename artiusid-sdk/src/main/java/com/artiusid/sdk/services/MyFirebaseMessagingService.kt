/*
 * Author: Todd Bryant
 * Company: artius.iD
 */
package com.artiusid.sdk.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.artiusid.sdk.standalone.StandaloneAppActivity
import com.artiusid.sdk.utils.FirebaseTokenManager
import com.artiusid.sdk.utils.NotificationStateManager
import com.artiusid.sdk.data.model.AppNotificationState

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MyFirebaseMessagingService"
        private const val PREF_NAME = "fcm_prefs"
        private const val TOKEN_KEY = "FCMRegistrationToken" // Match iOS key name
        private const val CHANNEL_ID = "artiusid_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM registration token: $token")
        
        // Save token using FirebaseTokenManager (similar to iOS MessagingDelegate)
        val tokenManager = FirebaseTokenManager.getInstance()
        tokenManager?.saveToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received: ${remoteMessage.data}")
        
        // Handle notification data similar to iOS handleNotification
        handleNotification(remoteMessage.data)
        
        // Show notification if app is in foreground
        showNotification(remoteMessage)
    }

    /**
     * Handle notification data similar to iOS AppDelegate.handleNotification
     */
    private fun handleNotification(userInfo: Map<String, String>) {
        Log.d(TAG, "Handling notification")
        var requestId: Int? = null

        val approvalTitle = userInfo["approvalTitle"]
        val approvalDescription = userInfo["approvalDescription"]

        if (!approvalTitle.isNullOrEmpty() && !approvalDescription.isNullOrEmpty()) {
            userInfo["requestId"]?.let { requestIdString ->
                requestId = requestIdString.toIntOrNull() ?: 0
            }
            
            // Handle the approval message like iOS AppDelegate.handleNotification
            // Update AppNotificationState to trigger automatic navigation
            AppNotificationState.handleApprovalNotification(requestId, approvalTitle, approvalDescription)
            
            Log.d(TAG, "AppNotificationState updated to APPROVAL - will trigger navigation")
        } else {
            AppNotificationState.reset()
            Log.d(TAG, "AppNotificationState updated to DEFAULT")
        }
    }

    private fun saveTokenSecurely(token: String) {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().putString(TOKEN_KEY, token).apply()
            Log.d(TAG, "FCM token saved to EncryptedSharedPreferences")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save FCM token securely: ${e.message}")
        }
    }

    fun getTokenSecurely(context: Context): String? {
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
            Log.e(TAG, "Failed to get FCM token securely: ${e.message}")
            null
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "artius.iD Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(this, StandaloneAppActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(remoteMessage.notification?.title ?: "artius.iD")
            .setContentText(remoteMessage.notification?.body ?: "You have a new notification.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
} 