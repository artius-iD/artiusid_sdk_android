# Approval Request Comparison: iOS vs Android SDK

## 📋 **JSON Payload Structure**

### **iOS Application (ApprovalRequestTestingRequest.swift)**
```json
{
  "clientId": 1,
  "clientGroupId": 1,
  "deviceId": "A1B2C3D4-E5F6-7890-ABCD-EF1234567890",
  "approvalTitle": "Approval Request",
  "approvalDescription": "This is a test approval request.",
  "timeout": 30
}
```

### **Android SDK (ApprovalRequestTestingRequest.kt)**
```json
{
  "clientId": 1,
  "clientGroupId": 1,
  "deviceId": "B911B2B9-BF90-76AD-0000-000000000000",
  "approvalTitle": "Approval Request", 
  "approvalDescription": "This is a test approval request.",
  "timeout": 30
}
```

## 🌐 **HTTP Headers**

### **iOS Application Headers**
```
Content-Type: application/json
User-Agent: [iOS System Generated]
```

### **Android SDK Headers** 
```
Content-Type: application/json
User-Agent: ArtiusID/5 (Android; API 34; Pixel 7)
```

## 🔗 **API Endpoint**

### **Both Applications**
- **Method**: `POST`
- **Endpoint**: `ApprovalRequestTestingFunction`
- **Base URL**: `https://service-mobile.stage.artiusid.dev`
- **Full URL**: `https://service-mobile.stage.artiusid.dev/ApprovalRequestTestingFunction`

## 🔐 **Security Configuration**

### **iOS Application**
- **mTLS**: Required (`requiresTLS: true`)
- **Certificate Pinning**: Yes
- **Lambda Response**: Yes (`isLambdaResponse: true`)

### **Android SDK**
- **mTLS**: Required (via `TLSSessionManager`)
- **Certificate Pinning**: Yes (via `CertificateManager`)
- **Lambda Response**: Yes (via `ApprovalRequestTestingResponse`)

## 🆔 **Device ID Generation**

### **iOS Application**
```swift
let deviceId = await device.identifierForVendor?.uuidString ?? ""
// Format: "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
```

### **Android SDK**
```kotlin
val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
val deviceId = convertAndroidIdToUUID(androidId)
// Converts: "b911b2b9bf9076ad" -> "B911B2B9-BF90-76AD-0000-000000000000"
```

## 📤 **Request Flow**

### **iOS Application**
1. `SendApprovalRequest.send()` creates `ApprovalRequestTestingRequest`
2. `APIManager.sendApprovalRequest()` calls `makeRequest()`
3. `makeRequest()` sets headers: `Content-Type: application/json`
4. Uses `TLSSessionManager` for mTLS with certificate pinning
5. Expects `ApprovalRequestTestingResponse` with Lambda format

### **Android SDK**
1. `SendApprovalRequest.send()` creates `ApprovalRequestTestingRequest`
2. `ApiService.sendApprovalRequestIOS()` via Retrofit
3. `TLSSessionManager` interceptor adds headers:
   - `Content-Type: application/json`
   - `User-Agent: ArtiusID/5 (Android; API 34; Pixel 7)`
4. Uses mTLS with certificate pinning via `CertificateManager`
5. Expects `ApprovalRequestTestingResponse` with Lambda format

## ⚠️ **Key Differences**

### **1. User-Agent Header**
- **iOS**: System-generated User-Agent (not explicitly set)
- **Android**: Custom User-Agent: `ArtiusID/5 (Android; API 34; Pixel 7)`

### **2. Device ID Format**
- **iOS**: Uses `identifierForVendor` (true UUID format)
- **Android**: Converts `ANDROID_ID` to UUID format with padding

### **3. HTTP Client Implementation**
- **iOS**: Uses `URLSession` with custom `TLSSessionManager`
- **Android**: Uses `OkHttp` with `Retrofit` and `TLSSessionManager`

## 🔍 **Debugging Information**

### **Android SDK Logging**
```kotlin
Log.d(TAG, "📤 Request being sent (body only, exactly like iOS):")
Log.d(TAG, "📤   ClientId: ${request.clientId}")
Log.d(TAG, "📤   ClientGroupId: ${request.clientGroupId}")
Log.d(TAG, "📤   DeviceId: ${request.deviceId}")
Log.d(TAG, "📤   ApprovalTitle: ${request.approvalTitle}")
Log.d(TAG, "📤   ApprovalDescription: ${request.approvalDescription}")
Log.d(TAG, "📤   Timeout: ${request.timeout} (iOS field)")
```

### **Headers Logging**
```kotlin
Log.d(TAG, "📤 Sending headers:")
Log.d(TAG, "📤   User-Agent: ArtiusID/5 (Android; API ${Build.VERSION.SDK_INT}; ${Build.MODEL})")
Log.d(TAG, "📤   Content-Type: application/json")
```

## 📊 **Expected Response Format**

### **Both Applications**
```json
{
  "statusCode": 200,
  "body": "{\"approvalData\":{\"requestId\":123,\"success\":true}}"
}
```

### **Parsed Response**
```json
{
  "approvalData": {
    "requestId": 123,
    "success": true
  }
}
```

## 🔧 **Testing Commands**

### **Capture Android SDK Network Traffic**
```bash
adb logcat -s "SendApprovalRequest" -s "TLSSessionManager" -s "BridgeMainActivity"
```

### **Monitor Approval Request Flow**
```bash
adb logcat -s "SendApprovalRequest" | grep "📤\|📋"
```
