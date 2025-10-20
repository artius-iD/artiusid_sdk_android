# 🔍 Certificate Registration Investigation - SDK v1.2.12

**Date:** October 17, 2025, 3:30 PM  
**Status:** ✅ Certificate registration IS implemented  
**Issue:** Registration likely failing silently in background  

---

## ✅ Certificate Registration IS Implemented

After analyzing the SDK source code, **certificate registration is fully implemented and should be running automatically**.

### Initialization Flow (Confirmed):

```kotlin
ArtiusIDSDK.initializeWithEnhancedTheme()
  ↓
Line 128-148: Launch background coroutine
  ↓
Line 130: initializeSharedCertificate(context, sdkConfiguration)
  ↓
Line 319: sharedContextManager.ensureSharedCertificate(deviceId)
  ↓
SharedContextManager.ensureSharedCertificate() (Line 81-121)
  ↓
Line 94: Get certificate URL from UrlBuilder
  ↓
Line 110-112: APIManager.loadCertificateFromFullUrl(deviceId, certificateUrl)
  ↓
APIManager.loadCertificateFromFullUrl() (APIManager.kt Line 66-79)
  ↓
Line 82-94: APIManager.loadCertificateFromUrl()
  ↓
Line 97-150: performCertificateRequest()
  ↓
Makes HTTP POST to: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

**Conclusion:** The code path exists and should execute automatically.

---

## 🔬 Why No Logs Are Visible

### Background Coroutine with Silent Failure

The certificate registration happens in a **background coroutine** that catches all exceptions:

```kotlin
// ArtiusIDSDK.kt Line 128-148
CoroutineScope(Dispatchers.IO).launch {
    try {
        initializeSharedCertificate(context, sdkConfiguration!!)
        // ... success ...
    } catch (e: Exception) {
        android.util.Log.e(TAG, "❌ Certificate initialization failed, but continuing with SDK initialization", e)
        // App continues without certificate - verification will handle this gracefully
    }
}
```

**Key Point:** Errors are logged but NOT thrown, allowing the app to continue.

---

## 📋 Expected Log Sequence

TriNet should be seeing these logs (if everything works):

```
I/ArtiusIDSDK: 🌐 Environment set to: Sandbox
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox (.dev domain)
I/ArtiusIDSDK:    Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
I/ArtiusIDSDK:    Certificate: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/SharedContextManager: 📊 Shared Context Status:
D/SharedContextManager:   - Certificate Context Shared: true
D/SharedContextManager:   - Firebase Context Shared: true
D/ArtiusIDSDK: 🔐 Initializing shared mTLS certificate...
D/ArtiusIDSDK: 📱 Device ID: [device_id]
D/ArtiusIDSDK: 🌐 Service URL: [base_url]
D/ArtiusIDSDK: 🏢 Host Package: com.trinet.app
D/SharedContextManager: 🔐 Ensuring shared certificate using host app context...
D/SharedContextManager:   - Host context: com.trinet.app
D/SharedContextManager:   - Device ID: [device_id]
D/SharedContextManager:   - Base URL: [base_url]
D/SharedContextManager:   - Environment in SharedPreferences: Sandbox
D/SharedContextManager:   - Certificate URL from UrlBuilder: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/APIManager: Loading certificate from full URL: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/APIManager: 🔓 Creating plain OkHttpClient for certificate registration (NO mTLS, NO pinning)
D/APIManager: 🔓 Plain client created successfully - will use system trust store only
D/APIManager: 🔒 HTTPS connection verified for certificate registration: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/SharedContextManager: ✅ Shared certificate ready
D/ArtiusIDSDK: ✅ Shared mTLS certificate initialization completed
```

### Or These Logs (if it fails):

```
E/ArtiusIDSDK: ❌ Certificate initialization failed, but continuing with SDK initialization
    [Exception details here]
E/SharedContextManager: ❌ Failed to ensure shared certificate using host context
    [Exception details here]
```

---

## 🎯 What TriNet Needs To Do

### Step 1: Get Full Logcat Output

The certificate registration logs are likely present but not showing in filtered output.

**Request from TriNet:**

```bash
# Get FULL logcat from app launch to verification failure
adb logcat -c  # Clear logs
adb shell am force-stop com.trinet.app  # Stop app
adb logcat > /tmp/trinet_full_logs.txt &  # Start logging
adb shell am start -n com.trinet.app/.MainActivity  # Launch app
# ... perform verification ...
# Stop logging (Ctrl+C)
```

**Then search for:**
```bash
grep -E "(ArtiusIDSDK|SharedContextManager|APIManager|CertificateManager)" /tmp/trinet_full_logs.txt
```

---

### Step 2: Check For These Specific Log Tags

```bash
adb logcat -s ArtiusIDSDK:* SharedContextManager:* APIManager:* CertificateManager:* TLSSessionManager:*
```

These tags will show **all certificate-related activity**.

---

### Step 3: Check For Certificate Registration Errors

```bash
adb logcat -s ArtiusIDSDK:E SharedContextManager:E APIManager:E
```

This shows only ERROR logs from certificate-related components.

---

## 🔍 Possible Failure Scenarios

### Scenario 1: Network Connection Fails
**Symptoms:** No network logs, no HTTP requests

**Possible causes:**
- DNS resolution fails for `sandbox.registration.artiusid.dev`
- Network permissions not granted
- Corporate firewall/proxy blocking
- Wi-Fi/cellular disabled

**Expected logs:**
```
E/APIManager: Failed to load certificate
    java.net.UnknownHostException: Unable to resolve host "sandbox.registration.artiusid.dev"
```

---

### Scenario 2: HTTP 400/500 Error
**Symptoms:** HTTP request succeeds, but server returns error

**Possible causes:**
- Device ID format incorrect
- CSR generation fails
- Backend service down
- Backend rejects request

**Expected logs:**
```
E/APIManager: Certificate registration failed with HTTP 400
```

---

### Scenario 3: Certificate Already Exists
**Symptoms:** No registration attempt, uses existing certificate

**This is NORMAL** - Certificate registration only happens once per device.

**Expected logs:**
```
D/CertificateManager: Existing certificate PEM found
```

**To force new registration:**
```kotlin
// In TriNet app, before SDK initialization:
context.getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
    .edit()
    .clear()
    .apply()
```

---

### Scenario 4: UrlBuilder Not Configured
**Symptoms:** Certificate URL is incorrect

**Expected logs:**
```
D/SharedContextManager:   - Certificate URL from UrlBuilder: [wrong URL]
```

**SDK v1.2.12 fix:** Automatic URL configuration based on `Environment.SANDBOX`

---

## 🧪 Debugging Steps for TriNet

### Debug Step 1: Enable Verbose Logging

Ensure verbose logging is enabled:

```kotlin
val config = SDKConfiguration(
    environment = Environment.SANDBOX,
    enableLogging = true,  // ← Ensure this is true
    // ... other config
)
```

---

### Debug Step 2: Check Logcat Immediately After SDK Init

Right after calling `initializeWithEnhancedTheme()`, check logs:

```kotlin
ArtiusIDSDK.initializeWithEnhancedTheme(this, config, theme)

// Give background coroutine time to run
Thread.sleep(5000)  // Wait 5 seconds

Log.d("TriNetDebug", "Check logcat now for certificate registration")
```

---

### Debug Step 3: Check Certificate Storage

After app launch, check if certificate was stored:

```kotlin
val prefs = getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
val hasCert = prefs.contains("CERTIFICATE_PEM")
Log.d("TriNetDebug", "Certificate stored: $hasCert")
```

---

### Debug Step 4: Manually Trigger Registration

To explicitly test certificate registration:

```kotlin
// After SDK initialization
lifecycleScope.launch {
    try {
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        val apiManager = com.artiusid.sdk.services.APIManager(this@MainActivity)
        val certUrl = "https://sandbox.registration.artiusid.dev/LoadCertificateFunction"
        
        Log.d("TriNetDebug", "Manually triggering certificate registration")
        apiManager.loadCertificateFromFullUrl(deviceId, certUrl)
        Log.d("TriNetDebug", "Manual certificate registration succeeded")
    } catch (e: Exception) {
        Log.e("TriNetDebug", "Manual certificate registration failed", e)
    }
}
```

---

## 📊 Certificate Registration Checklist

| Check | Expected | Command/Code |
|-------|----------|--------------|
| **1. SDK Initialized** | ✅ "SDK Bridge initialized successfully" | `adb logcat -s ArtiusIDSDK:I` |
| **2. Environment Set** | ✅ "Environment set to: Sandbox" | `adb logcat -s ArtiusIDSDK:I` |
| **3. URLs Configured** | ✅ Certificate URL logged | `adb logcat -s ArtiusIDSDK:I` |
| **4. Shared Context Created** | ✅ "Certificate Context Shared: true" | `adb logcat -s SharedContextManager:D` |
| **5. Certificate Init Started** | ✅ "Initializing shared mTLS certificate" | `adb logcat -s ArtiusIDSDK:D` |
| **6. Device ID Retrieved** | ✅ "Device ID: [id]" | `adb logcat -s ArtiusIDSDK:D` |
| **7. Certificate URL Built** | ✅ "Certificate URL from UrlBuilder: [url]" | `adb logcat -s SharedContextManager:D` |
| **8. HTTP Request Made** | ✅ "Loading certificate from full URL" | `adb logcat -s APIManager:D` |
| **9. OkHttp Client Created** | ✅ "Creating plain OkHttpClient" | `adb logcat -s APIManager:D` |
| **10. HTTPS Verified** | ✅ "HTTPS connection verified" | `adb logcat -s APIManager:D` |
| **11. Certificate Stored** | ✅ "Shared certificate ready" | `adb logcat -s SharedContextManager:D` |
| **12. Init Complete** | ✅ "Shared mTLS certificate initialization completed" | `adb logcat -s ArtiusIDSDK:D` |

**All 12 checks must pass for successful certificate registration.**

---

## 🚨 Common Failure Points

### Failure Point #1: Background Coroutine Not Completing
**Symptom:** See "Initializing shared mTLS certificate" but no "completed" log

**Cause:** Coroutine is stuck or crashed

**Fix:** Check for exceptions in logcat

---

### Failure Point #2: UrlBuilder Returns Wrong URL
**Symptom:** Certificate URL is not `sandbox.registration.artiusid.dev`

**Cause:** Environment not set correctly in SharedPreferences

**Fix:** Verify environment setting:
```kotlin
val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
val env = prefs.getString("environment", "NOT_SET")
Log.d("TriNetDebug", "Environment: $env")  // Should be "Sandbox"
```

---

### Failure Point #3: Network Request Fails
**Symptom:** "Creating plain OkHttpClient" but no "HTTPS connection verified"

**Cause:** Network error, DNS failure, or firewall

**Fix:** Test network connectivity:
```bash
adb shell ping sandbox.registration.artiusid.dev
adb shell curl -v https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

---

### Failure Point #4: Backend Returns Error
**Symptom:** HTTPS request succeeds but certificate not stored

**Cause:** Backend rejects the CSR or device ID

**Fix:** Check HTTP response code in logs

---

## 📋 Summary for TriNet

### What We Know:
1. ✅ **Certificate registration IS implemented** in SDK v1.2.12
2. ✅ **Automatic execution** during `initializeWithEnhancedTheme()`
3. ✅ **Correct URL** configured for Sandbox environment
4. ✅ **Background execution** with error handling (doesn't crash app)

### What TriNet Needs To Provide:

**Request #1: Full Certificate-Related Logs**
```bash
adb logcat -c
adb logcat -s ArtiusIDSDK:* SharedContextManager:* APIManager:* CertificateManager:* > full_logs.txt
# Launch app and attempt verification
# Send us full_logs.txt
```

**Request #2: Check Certificate Storage**
```kotlin
val prefs = getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
val allKeys = prefs.all.keys
Log.d("TriNetDebug", "Certificate storage keys: $allKeys")
```

**Request #3: Network Connectivity Test**
```bash
adb shell ping -c 4 sandbox.registration.artiusid.dev
adb shell curl -v https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

---

## 🎯 Expected Outcome

Once TriNet provides the full logs, we will be able to determine:

1. **Is registration being attempted?** (Should see "Initializing shared mTLS certificate")
2. **Is registration succeeding?** (Should see "Shared certificate ready")
3. **If failing, why?** (Will see error logs with exception details)

**Without the full logs, we cannot diagnose further.**

---

## 🔧 Temporary Workaround (If Needed)

If certificate registration is failing, TriNet can manually trigger it:

```kotlin
// In MainActivity.onCreate(), after SDK initialization
lifecycleScope.launch {
    delay(2000)  // Wait for SDK init
    
    try {
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        val sharedPrefs = getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
        if (!sharedPrefs.contains("CERTIFICATE_PEM")) {
            Log.w("TriNet", "Certificate not registered, attempting manual registration...")
            
            val apiManager = com.artiusid.sdk.services.APIManager(this@MainActivity)
            apiManager.loadCertificateFromFullUrl(
                deviceId,
                "https://sandbox.registration.artiusid.dev/LoadCertificateFunction"
            )
            
            Log.i("TriNet", "✅ Manual certificate registration succeeded")
        } else {
            Log.i("TriNet", "✅ Certificate already registered")
        }
    } catch (e: Exception) {
        Log.e("TriNet", "❌ Manual certificate registration failed", e)
    }
}
```

---

## 📞 Next Steps

**TriNet Action Required:**

1. **Get full logcat output** using commands above
2. **Check certificate storage** using SharedPreferences inspection
3. **Test network connectivity** to `sandbox.registration.artiusid.dev`
4. **Send results** to SDK team for analysis

**SDK Team Action (After Receiving Logs):**

1. Analyze why registration is failing
2. Identify specific error (network, backend, or other)
3. Provide fix or workaround

---

**Status:** ⏸️ Awaiting TriNet's full logcat output  
**Priority:** P0 - CRITICAL  
**Blocking:** Verification cannot work without certificate registration  

---

*Analysis Date: October 17, 2025, 3:30 PM*  
*SDK Version: v1.2.12*  
*Code Paths Verified: ✅ All certificate registration paths confirmed present*


