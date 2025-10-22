# SDK v1.2.24 - Enhanced Approval Request Logging

**Date:** October 22, 2025  
**Priority:** P0 - CRITICAL (Multiple approval requests still occurring)  
**Status:** ✅ DEPLOYED

---

## 🎯 Problem

TriNet reports that **multiple approval requests** are still being sent to the backend when the "Test Approval" button is tapped, despite the v1.2.23 fix that added:
- Guard flag in `ArtiusIDSDK.sendApprovalRequest()`
- `retryOnConnectionFailure(false)` in `TLSSessionManager`
- UUID-based call tracking

---

## 🔍 Root Cause Analysis

The issue could be:
1. **TriNet hasn't updated to v1.2.23 yet** - still using older AAR
2. **Compose recomposition** - button click handler firing multiple times
3. **Guard flag being bypassed** - somehow being reset or not effective
4. **Network layer issue** - Retrofit/OkHttp making duplicate calls despite guard

---

## ✅ Solution: Enhanced Logging + Debounce

### **1. Button-Level Debounce (Sample App)**
**File:** `sample-app/src/main/java/com/artiusid/sample/BridgeMainActivity.kt`

Added **2-second debounce** to prevent rapid button clicks:

```kotlin
// Guard flag to prevent rapid button clicks causing duplicate requests
private var lastApprovalRequestTime = 0L
private val approvalRequestDebounceMs = 2000L // 2 second debounce

private fun sendApprovalRequest() {
    try {
        // Debounce: Prevent multiple clicks within 2 seconds
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastApprovalRequestTime < approvalRequestDebounceMs) {
            android.util.Log.w("BridgeMainActivity", "⚠️ Approval request debounced - too soon after last request (${currentTime - lastApprovalRequestTime}ms)")
            return
        }
        lastApprovalRequestTime = currentTime
        
        isApprovalLoading = true
        android.util.Log.d("BridgeMainActivity", "📋 ========================================")
        android.util.Log.d("BridgeMainActivity", "📋 APPROVAL REQUEST BUTTON CLICKED")
        android.util.Log.d("BridgeMainActivity", "📋 Timestamp: $currentTime")
        android.util.Log.d("BridgeMainActivity", "📋 ========================================")
        
        // ... rest of the function
    }
}
```

**What This Does:**
- ✅ Prevents button from being clicked more than once every 2 seconds
- ✅ Logs button click timestamp for debugging
- ✅ Protects against accidental double-clicks or UI recomposition issues

---

### **2. API Call Tracking (SDK)**
**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/SendApprovalRequest.kt`

Added **UUID-based tracking** for every `send()` call:

```kotlin
suspend fun send(): Pair<Boolean, Int?> {
    // Generate unique call ID for tracking
    val callId = java.util.UUID.randomUUID().toString().substring(0, 8)
    val startTime = System.currentTimeMillis()
    
    return try {
        Log.d(TAG, "📞 [Call $callId] ========================================")
        Log.d(TAG, "📞 [Call $callId] send() STARTED at $startTime")
        Log.d(TAG, "📞 [Call $callId] ========================================")
        
        // ... API call logic ...
        
        Log.d(TAG, "📞 [Call $callId] 🌐 Calling apiService.sendApprovalRequestIOS() via Retrofit...")
        val apiCallStartTime = System.currentTimeMillis()
        
        val response = apiService.sendApprovalRequestIOS(request)
        
        val apiCallDuration = System.currentTimeMillis() - apiCallStartTime
        Log.d(TAG, "📞 [Call $callId] ✅ API call completed in ${apiCallDuration}ms")
        
        // ... response handling with [Call $callId] prefix ...
    }
}
```

**What This Does:**
- ✅ Every call to `send()` gets a unique 8-character ID
- ✅ Logs start time, API call duration, and total duration
- ✅ Makes it easy to count how many times `send()` is actually called
- ✅ Helps identify if the issue is at the SDK layer or network layer

---

### **3. HTTP Request Tracking (Network Layer)**
**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/TLSSessionManager.kt`

Added **HTTP-level logging** in the OkHttp interceptor:

```kotlin
.addInterceptor { chain ->
    val originalRequest = chain.request()
    
    // Generate unique request ID for tracking
    val requestId = java.util.UUID.randomUUID().toString().substring(0, 8)
    val requestTime = System.currentTimeMillis()
    
    Log.d(TAG, "🌐 [HTTP $requestId] ========================================")
    Log.d(TAG, "🌐 [HTTP $requestId] HTTP REQUEST STARTED")
    Log.d(TAG, "🌐 [HTTP $requestId] Method: ${originalRequest.method}")
    Log.d(TAG, "🌐 [HTTP $requestId] URL: ${originalRequest.url}")
    Log.d(TAG, "🌐 [HTTP $requestId] Time: $requestTime")
    Log.d(TAG, "🌐 [HTTP $requestId] ========================================")
    
    // ... execute request ...
    
    val response = chain.proceed(newRequest)
    val responseTime = System.currentTimeMillis()
    val duration = responseTime - requestTime
    
    Log.d(TAG, "🌐 [HTTP $requestId] ========================================")
    Log.d(TAG, "🌐 [HTTP $requestId] HTTP RESPONSE RECEIVED")
    Log.d(TAG, "🌐 [HTTP $requestId] Status: ${response.code}")
    Log.d(TAG, "🌐 [HTTP $requestId] Duration: ${duration}ms")
    Log.d(TAG, "🌐 [HTTP $requestId] ========================================")
    
    response
}
```

**What This Does:**
- ✅ Every HTTP request gets a unique 8-character ID
- ✅ Logs the exact moment the HTTP request is sent
- ✅ Logs the HTTP response code and duration
- ✅ **CRITICAL:** Shows if OkHttp is making multiple HTTP requests despite our guard flags

---

## 📊 Expected Log Output (Single Button Click)

### **✅ CORRECT - Single Request:**
```
BridgeMainActivity: 📋 ======================================== 
BridgeMainActivity: 📋 APPROVAL REQUEST BUTTON CLICKED
BridgeMainActivity: 📋 Timestamp: 1729612345678
BridgeMainActivity: 📋 ========================================
BridgeMainActivity: 📋 Calling ArtiusIDSDK.sendApprovalRequest()...
ArtiusIDSDK: 📞 [Call abc123de] sendApprovalRequest() STARTED
SendApprovalRequest: 📞 [Call xyz789ab] ========================================
SendApprovalRequest: 📞 [Call xyz789ab] send() STARTED at 1729612345680
SendApprovalRequest: 📞 [Call xyz789ab] ========================================
SendApprovalRequest: 📞 [Call xyz789ab] 🌐 Calling apiService.sendApprovalRequestIOS() via Retrofit...
TLSSessionManager: 🌐 [HTTP req456gh] ========================================
TLSSessionManager: 🌐 [HTTP req456gh] HTTP REQUEST STARTED
TLSSessionManager: 🌐 [HTTP req456gh] Method: POST
TLSSessionManager: 🌐 [HTTP req456gh] URL: https://sandbox.mobile.artiusid.dev/ApprovalRequestTestingFunction
TLSSessionManager: 🌐 [HTTP req456gh] Time: 1729612345682
TLSSessionManager: 🌐 [HTTP req456gh] ========================================
TLSSessionManager: 🌐 [HTTP req456gh] ========================================
TLSSessionManager: 🌐 [HTTP req456gh] HTTP RESPONSE RECEIVED
TLSSessionManager: 🌐 [HTTP req456gh] Status: 200
TLSSessionManager: 🌐 [HTTP req456gh] Duration: 1234ms
TLSSessionManager: 🌐 [HTTP req456gh] ========================================
SendApprovalRequest: 📞 [Call xyz789ab] ✅ API call completed in 1234ms
SendApprovalRequest: 📞 [Call xyz789ab] ✅ Approval request sent successfully
SendApprovalRequest: 📞 [Call xyz789ab] ✅ Total duration: 1236ms
ArtiusIDSDK: 📞 [Call abc123de] ✅ sendApprovalRequest() COMPLETED successfully
```

**Analysis:**
- ✅ **1 button click** → 1 log entry
- ✅ **1 ArtiusIDSDK call** → 1 `[Call abc123de]` ID
- ✅ **1 send() call** → 1 `[Call xyz789ab]` ID
- ✅ **1 HTTP request** → 1 `[HTTP req456gh]` ID
- ✅ **Result:** 1 approval request sent to backend

---

### **❌ INCORRECT - Multiple Requests:**

If we see **multiple HTTP IDs** for a single button click, we know the issue is at the network layer.

If we see **multiple Call IDs** for a single button click, we know the issue is at the SDK layer.

If we see **multiple button click logs**, we know the issue is at the UI layer (which the debounce should prevent).

---

## 🧪 Testing Instructions

### **For TriNet:**

1. **Update to v1.2.24:**
   - Download the new AAR from GitHub: https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.24
   - Replace the old AAR in your project
   - Clean and rebuild your app

2. **Test Approval Request:**
   - Open the app
   - Complete verification (if not already done)
   - Tap "Test Approval" button **ONCE**
   - Wait for the response

3. **Collect Logs:**
   ```bash
   adb logcat | grep -E "BridgeMainActivity|ArtiusIDSDK|SendApprovalRequest|TLSSessionManager"
   ```

4. **Analyze Logs:**
   - Count how many times you see `📋 APPROVAL REQUEST BUTTON CLICKED`
   - Count how many times you see `📞 [Call xxxxxxxx] send() STARTED`
   - Count how many times you see `🌐 [HTTP xxxxxxxx] HTTP REQUEST STARTED`
   - **All three should be 1 for a single button click**

5. **Check Backend:**
   - How many approval requests were received?
   - How many notifications were sent?

---

## 🔧 What Changed in v1.2.24

| File | Change | Purpose |
|------|--------|---------|
| `BridgeMainActivity.kt` | Added 2-second debounce | Prevent rapid button clicks |
| `BridgeMainActivity.kt` | Enhanced button click logging | Track when button is actually clicked |
| `SendApprovalRequest.kt` | Added UUID tracking per call | Track each `send()` invocation |
| `SendApprovalRequest.kt` | Added timing logs | Measure API call duration |
| `TLSSessionManager.kt` | Added HTTP request/response logging | Track actual network requests |
| `gradle.properties` | Version → 1.2.24 | New release |

---

## 📋 Deployment Checklist

- [x] Code changes implemented
- [x] Version updated to 1.2.24
- [x] Linter checks passed
- [ ] SDK built and tested locally
- [ ] GitHub release created
- [ ] TriNet notified
- [ ] Logs collected and analyzed

---

## 🎯 Success Criteria

**v1.2.24 is successful if:**
1. ✅ Logs show **exactly 1 HTTP request** per button click
2. ✅ Backend receives **exactly 1 approval request** per button click
3. ✅ User receives **exactly 1 notification** per button click
4. ✅ Logs clearly show where duplicate requests originate (if any)

---

## 📞 Next Steps

1. **Build and deploy v1.2.24**
2. **TriNet tests with enhanced logging**
3. **Analyze logs to identify exact source of duplicates**
4. **If duplicates persist, we'll know exactly which layer is causing them:**
   - **UI Layer:** Multiple button click logs → Compose recomposition issue
   - **SDK Layer:** Multiple `send()` call IDs → Guard flag not working
   - **Network Layer:** Multiple HTTP request IDs → OkHttp retry despite `retryOnConnectionFailure(false)`

---

## 🔗 Related Issues

- SDK v1.2.23: Initial triple approval fix (guard flag + retry disable)
- This issue: Enhanced logging to diagnose why duplicates still occur

