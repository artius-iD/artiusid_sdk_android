# ArtiusID Android SDK - Client Implementation Guide

**SDK Version:** v1.2.48  
**Date:** October 29, 2025  
**Author:** Todd Bryant, artius.iD, Inc.

---

## 🚨 CRITICAL ARCHITECTURAL CHANGES

This guide covers the critical changes required for client apps to properly integrate with the latest SDK version.

---

## 1. 📱 FIREBASE ARCHITECTURE - CLIENT CONTROLS EVERYTHING

### **Change Summary:**
The SDK **no longer handles Firebase notifications**. Client apps must manage their own Firebase tokens AND notifications.

### **Why This Change?**
- Clear separation of concerns
- Client apps have full control over notification display
- Eliminates timing issues with SDK initialization
- Matches iOS app architecture

### **Implementation Steps:**

#### **Step 1: Create Your Own Firebase Messaging Service**

Create `YourFirebaseMessagingService.kt`:

```kotlin
package com.yourcompany.yourapp

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

class YourFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "YourFCMService"
        private const val CHANNEL_ID = "your_app_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d(TAG, "🔥 NEW FCM TOKEN RECEIVED: ${token.take(20)}...")
        
        // Save token to your secure storage
        val tokenManager = FirebaseTokenManager.getInstance(applicationContext)
        val currentEnvironment = com.artiusid.sdk.utils.UrlBuilder.getCurrentEnvironment(applicationContext)
        val environmentForStorage = when (currentEnvironment.uppercase()) {
            "SANDBOX" -> "Sandbox"
            "DEVELOPMENT" -> "Development"
            "STAGING" -> "Staging"
            else -> "Sandbox"
        }
        
        tokenManager?.saveToken(token, environmentForStorage)
        
        // ✅ CRITICAL: Provide token to SDK
        ArtiusIDSDK.updateFcmToken(token)
        Log.d(TAG, "✅ FCM token provided to SDK")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "🔔 FCM MESSAGE RECEIVED")
        Log.d(TAG, "🔔 Data: ${remoteMessage.data}")
        
        // Show notification
        showNotification(remoteMessage)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create HIGH PRIORITY notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Your App Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Approval requests and authentication notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open your main activity
        val intent = Intent(this, YourMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Add notification data as extras
            val approvalTitle = remoteMessage.data["approvalTitle"]
            val approvalDescription = remoteMessage.data["approvalDescription"]
            val requestId = remoteMessage.data["requestId"]
            
            if (approvalTitle != null && approvalDescription != null) {
                putExtra("approvalTitle", approvalTitle)
                putExtra("approvalDescription", approvalDescription)
                requestId?.let { putExtra("requestId", it) }
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build HIGH PRIORITY notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(remoteMessage.notification?.title ?: "Your App")
            .setContentText(remoteMessage.notification?.body ?: "You have a new notification.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        val notificationId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }
}
```

#### **Step 2: Update AndroidManifest.xml**

```xml
<!-- YOUR Firebase Messaging Service (NOT SDK's) -->
<service
    android:name=".YourFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

#### **Step 3: Update SDK Configuration**

```kotlin
val sdkConfig = SDKConfiguration(
    apiKey = "your_api_key",
    baseUrl = "https://api.artiusid.com",
    environment = sdkEnvironment,
    clientId = 1,
    clientGroupId = 1,
    
    // ✅ CRITICAL: Disable SDK Firebase handling
    handleFirebaseNotifications = false,
    customFcmToken = null  // Will be provided via ArtiusIDSDK.updateFcmToken()
)
```

#### **Step 4: Retrieve and Provide FCM Token**

```kotlin
private fun setupFirebaseTokenManagement() {
    try {
        // Check keystore for existing FCM token first
        val tokenManager = FirebaseTokenManager.getInstance(this)
        val currentEnvironment = UrlBuilder.getCurrentEnvironment(this)
        val storedToken = tokenManager?.getFCMToken(currentEnvironment)
        
        if (!storedToken.isNullOrEmpty()) {
            // ✅ Use stored token
            ArtiusIDSDK.updateFcmToken(storedToken)
            Log.d(TAG, "✅ Using stored FCM token")
        } else {
            // Generate new token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    tokenManager?.saveToken(token, currentEnvironment)
                    ArtiusIDSDK.updateFcmToken(token)
                    Log.d(TAG, "✅ Generated new FCM token")
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Error managing FCM token", e)
    }
}
```

---

## 2. 🔐 ENVIRONMENT-SPECIFIC CREDENTIALS

### **Change Summary:**
All credentials (verification, FCM tokens, certificates) are now stored per-environment.

### **Why This Change?**
- Prevents cross-environment credential contamination
- Allows switching between environments without losing credentials
- Each environment maintains independent state

### **Implementation:**

#### **Auto-Detect Environment on Startup**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Auto-detect environment from stored credentials
    val environmentCredentialManager = EnvironmentCredentialManager.getInstance(this)
    val detectedEnvironment = environmentCredentialManager.autoDetectEnvironmentFromCredentials()
    
    // Use detected environment or default to Sandbox
    selectedEnvironment = if (!detectedEnvironment.isNullOrEmpty()) {
        Log.d(TAG, "✅ Using detected environment: $detectedEnvironment")
        environmentCredentialManager.setEnvironmentForAllCredentials(detectedEnvironment)
        detectedEnvironment
    } else {
        Log.d(TAG, "⚠️ No credentials found, using Sandbox")
        "Sandbox"
    }
}
```

#### **Handle Environment Changes**

```kotlin
private fun handleEnvironmentChange(oldEnvironment: String, newEnvironment: String) {
    Log.d(TAG, "🚨 Environment change: $oldEnvironment → $newEnvironment")
    
    // Update environment for all credential managers
    val environmentCredentialManager = EnvironmentCredentialManager.getInstance(this)
    environmentCredentialManager.setEnvironmentForAllCredentials(newEnvironment)
    
    // Clear old environment credentials
    environmentCredentialManager.clearCredentialsForEnvironment(oldEnvironment)
    
    // 🚨 CRITICAL: Clear UI state to prevent showing old data
    verificationResultData = null
    memberIdStatus = "❌ Verification Required"
    memberIdPreview = "Not verified"
    
    // Re-initialize SDK with new environment
    initializeSDK()
    
    // Check if credentials exist in new environment
    val verificationStateManager = VerificationStateManager(this)
    val hasVerification = verificationStateManager.isVerified(newEnvironment)
    val accountNumber = verificationStateManager.getAccountNumber(newEnvironment)
    
    if (hasVerification && !accountNumber.isNullOrEmpty()) {
        // User has credentials in new environment
        memberIdStatus = "✅ Verified"
        memberIdPreview = accountNumber
    } else {
        // User needs to verify in new environment
        memberIdStatus = "❌ Verification Required"
    }
    
    // Refresh FCM token for new environment
    refreshFCMTokenForNewEnvironment()
    
    // Get certificate for new environment
    ArtiusIDSDK.ensureCertificateRegistered(this)
}
```

---

## 3. 🌐 ENVIRONMENT CONFIGURATION

### **Available Environments:**
- **Sandbox** (default)
- **Development**
- **Staging**

### **Environment URLs:**

| Environment | Approval/Auth/Verification | Certificate Registration |
|-------------|---------------------------|-------------------------|
| **Sandbox** | `sandbox.mobile.artiusid.dev` | `sandbox.registration.artiusid.dev` |
| **Development** | `service-mobile.dev.artiusid.dev` | `service-registration.dev.artiusid.dev` |
| **Staging** | `service-mobile.stage.artiusid.dev` | `service-registration.stage.artiusid.dev` |

### **Set Environment:**

```kotlin
val sdkEnvironment = when (selectedEnvironment.uppercase()) {
    "SANDBOX" -> Environment.SANDBOX
    "DEVELOPMENT" -> Environment.DEVELOPMENT
    "STAGING" -> Environment.STAGING
    else -> Environment.SANDBOX
}

val sdkConfig = SDKConfiguration(
    environment = sdkEnvironment,
    // ... other config
)
```

---

## 4. 🔔 NOTIFICATION REQUIREMENTS

### **Notification Channel Settings:**

- **Importance:** `IMPORTANCE_HIGH` (for heads-up notifications)
- **Sound:** Enabled via `DEFAULT_ALL`
- **Vibration:** Enabled with pattern `[0, 500, 200, 500]`
- **LED Lights:** Enabled
- **Badge:** Enabled (shows notification count on app icon)

### **Why High Priority?**
Approval requests are time-sensitive and require immediate user attention.

---

## 5. 🔄 VERIFICATION FLOW CHANGES

### **What Changed:**
- Verification UI now uses simplified processing → success flow
- Enhanced state logging for debugging
- Better spacing in UI (progress circle + text)

### **No Action Required:**
These are internal SDK improvements. Your verification flow remains the same.

---

## 6. ✅ TESTING CHECKLIST

### **Environment Testing:**
- [ ] Switch from Sandbox → Development → Staging
- [ ] Verify credentials are isolated per environment
- [ ] Confirm UI shows correct environment
- [ ] Test that switching preserves per-environment credentials

### **Firebase Testing:**
- [ ] Verify FCM token is saved on app startup
- [ ] Confirm FCM token is provided to SDK via `updateFcmToken()`
- [ ] Test notifications appear with high priority (sound + vibration)
- [ ] Verify notifications work after environment change

### **Verification Testing:**
- [ ] Complete verification in each environment
- [ ] Confirm new member ID is displayed after verification
- [ ] Test that environment change clears old member ID from UI
- [ ] Verify approval requests use correct environment-specific member ID

### **Approval Testing:**
- [ ] Send test approval request
- [ ] Verify notification is received
- [ ] Confirm notification uses correct member ID
- [ ] Test approval in different environments

---

## 7. 🚨 COMMON ISSUES & SOLUTIONS

### **Issue: "Approval notification not received"**
**Solution:**
1. Verify you completed verification in current environment (backend needs FCM token from verification)
2. Check FCM token is being provided to SDK via `updateFcmToken()`
3. Confirm your Firebase service is registered in `AndroidManifest.xml`

### **Issue: "Wrong member ID displayed after environment change"**
**Solution:**
Clear `verificationResultData` when handling environment changes (see Step 2 above).

### **Issue: "Verification UI stuck at 'Processing'"**
**Solution:**
This has been fixed in SDK v1.2.48. Update to latest SDK version.

---

## 8. 📝 MIGRATION SUMMARY

### **Before (Old SDK):**
- SDK controlled Firebase messaging service
- SDK handled notifications
- Single environment credentials
- Client provided FCM token in config

### **After (New SDK v1.2.48):**
- ✅ **Client controls Firebase messaging service**
- ✅ **Client handles notifications**
- ✅ **Environment-specific credentials**
- ✅ **Client provides FCM token via `updateFcmToken()`**

---

## 9. 📞 SUPPORT

For issues or questions:
- **Email:** support@artiusid.com
- **Documentation:** [SDK Documentation](https://docs.artiusid.com)

---

## 10. 📦 SAMPLE IMPLEMENTATION

See `sample-app/src/main/java/com/artiusid/sample/BridgeMainActivity.kt` for a complete reference implementation demonstrating:
- Firebase messaging service setup
- Environment management
- FCM token handling
- Credential isolation
- UI state management

---

**End of Implementation Guide**

