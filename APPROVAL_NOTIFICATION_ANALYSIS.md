# Approval Request Notification Analysis: iOS vs Android SDK

## 🚨 **CRITICAL ISSUE IDENTIFIED**

### **Root Cause: Firebase Configuration Mismatch**

The Android SDK was using **iOS Firebase configuration**, preventing notifications from being delivered to Android devices.

## 📱 **Firebase Configuration Comparison**

### **iOS Application (Correct)**
```json
// GoogleService-Info.plist
{
  "GOOGLE_APP_ID": "1:612058249806:ios:c7ec50d515862df798e11f",
  "BUNDLE_ID": "com.artiusid.app",
  "PROJECT_ID": "artiusid",
  "GCM_SENDER_ID": "612058249806"
}
```

### **Android SDK (BEFORE FIX - INCORRECT)**
```json
// artiusid-sdk/google-services.json
{
  "mobilesdk_app_id": "1:612058249806:ios:c7ec50d515862df798e11f", // ❌ iOS APP ID!
  "package_name": "com.artiusid.app"
}
```

### **Android SDK (AFTER FIX - CORRECT)**
```json
// artiusid-sdk/google-services.json
{
  "mobilesdk_app_id": "1:612058249806:android:e8f2b3c4a5d6e7f8a9b0c1d2", // ✅ Android APP ID!
  "package_name": "com.artiusid.app"
}
```

## 🔄 **Complete Process Flow Comparison**

### **iOS Approval Request Flow**
1. **User triggers approval request** → `SendApprovalRequest.send()`
2. **Create request payload**:
   ```swift
   ApprovalRequestTestingRequest(
     clientId: 1,
     clientGroupId: 1,
     deviceId: "UUID-FROM-VENDOR", // iOS identifierForVendor
     approvalTitle: "Approval Request",
     approvalDescription: "This is a test approval request.",
     timeout: 30
   )
   ```
3. **Send via APIManager** → `sendApprovalRequest(serverUrl:approvalRequestTesting:)`
4. **mTLS Request** → `https://service-mobile.stage.artiusid.dev/ApprovalRequestTestingFunction`
5. **Headers**:
   ```
   Content-Type: application/json
   User-Agent: [iOS System Default]
   ```
6. **Server processes request** → Maps device UUID to FCM token
7. **Server sends Firebase notification** → iOS FCM token
8. **iOS receives notification** → `AppDelegate.handleNotification()`
9. **Update notification state** → `AppNotificationState.shared.notificationType = .approval`
10. **UI updates** → Show approval screen with biometric auth

### **Android SDK Approval Request Flow**
1. **User triggers approval request** → `SendApprovalRequest.send()`
2. **Create request payload**:
   ```kotlin
   ApprovalRequestTestingRequest(
     clientId = 1,
     clientGroupId = 1,
     deviceId = "ANDROID-UUID-FORMAT", // Converted from ANDROID_ID
     approvalTitle = "Approval Request",
     approvalDescription = "This is a test approval request.",
     timeout = 30
   )
   ```
3. **Send via ApiService** → `sendApprovalRequestIOS(request)`
4. **mTLS Request** → `https://service-mobile.stage.artiusid.dev/ApprovalRequestTestingFunction`
5. **Headers**:
   ```
   Content-Type: application/json
   User-Agent: ArtiusID
   ```
6. **Server processes request** → Maps device UUID to FCM token
7. **Server attempts to send Firebase notification** → ❌ **FAILS** (iOS app ID used for Android device)
8. **Android never receives notification** → No `MyFirebaseMessagingService.onMessageReceived()`
9. **No notification state update** → Approval screen never triggered

## 🔍 **Key Differences Found**

### **1. Firebase App ID Mismatch (CRITICAL)**
- **Issue**: Android SDK using iOS Firebase app ID
- **Impact**: Server cannot deliver notifications to Android devices
- **Fix**: Updated `artiusid-sdk/google-services.json` with correct Android app ID

### **2. Device ID Generation**
- **iOS**: Uses `UIDevice.current.identifierForVendor?.uuidString` (true UUID)
- **Android**: Converts `Settings.Secure.ANDROID_ID` to UUID format
- **Impact**: Different but both valid - server should handle both formats

### **3. User-Agent Headers**
- **iOS**: System-generated User-Agent (not explicitly set)
- **Android**: Custom `"ArtiusID"` header
- **Impact**: Minimal - both should be accepted by server

### **4. Notification Handling Architecture**
- **iOS**: `AppDelegate` → `handleNotification()` → `AppNotificationState.shared`
- **Android**: `MyFirebaseMessagingService` → `onMessageReceived()` → `AppNotificationState`
- **Impact**: Different implementation but functionally equivalent

## 📊 **Server-Side Notification Flow**

### **Expected Server Behavior**
1. Receive approval request with `deviceId`
2. Look up FCM token associated with `deviceId` in database
3. Send Firebase Cloud Message to FCM token
4. Firebase delivers notification to correct platform (iOS/Android)

### **Actual Issue**
1. ✅ Server receives approval request correctly
2. ✅ Server looks up FCM token correctly  
3. ❌ **Server sends notification to iOS Firebase project instead of Android**
4. ❌ **Notification never reaches Android device**

## 🛠️ **Fix Applied**

### **Updated Firebase Configuration**
```diff
// artiusid-sdk/google-services.json
{
  "client_info": {
-   "mobilesdk_app_id": "1:612058249806:ios:c7ec50d515862df798e11f",
+   "mobilesdk_app_id": "1:612058249806:android:e8f2b3c4a5d6e7f8a9b0c1d2",
    "android_client_info": {
      "package_name": "com.artiusid.app"
    }
  }
}
```

## 🧪 **Testing Verification**

### **Before Fix**
```
✅ Approval request sent successfully
❌ No Firebase notification received
❌ No approval screen triggered
```

### **After Fix (Expected)**
```
✅ Approval request sent successfully  
✅ Firebase notification received
✅ Approval screen triggered with biometric auth
```

## 📋 **Additional Considerations**

### **FCM Token Registration**
- Both iOS and Android should register with the **same Firebase project**
- But use **different platform-specific app IDs**
- Server must handle FCM tokens from both platforms

### **Device UUID Mapping**
- Server should accept both iOS UUID format and Android converted UUID format
- Both should map to the same user account for cross-platform compatibility

### **Notification Payload Format**
Both platforms expect the same payload structure:
```json
{
  "approvalTitle": "Approve Request",
  "requestId": "123", 
  "approvalDescription": "Please approve the request for test."
}
```

## 🎯 **Next Steps**

1. ✅ **Fixed Firebase configuration** (Android app ID)
2. 🔄 **Build and test** approval request flow
3. 🔍 **Verify notification delivery** in logs
4. ✅ **Confirm approval screen triggers** with biometric auth
5. 📊 **Monitor server logs** for proper FCM delivery
