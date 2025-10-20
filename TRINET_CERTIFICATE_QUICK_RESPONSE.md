# 🔍 Certificate Registration - Quick Response

**Date:** October 17, 2025  
**Issue:** "SDK not performing certificate registration"  
**Status:** ✅ **Certificate registration IS implemented**  

---

## ✅ Good News: Registration IS Implemented

After analyzing SDK v1.2.12 source code, **certificate registration is fully implemented and runs automatically** during SDK initialization.

**The issue is:** Registration is likely **failing silently** in a background thread.

---

## 🚨 What We Need From You Immediately

### Request #1: Full Certificate Logs

Run these commands to capture all certificate-related activity:

```bash
# Stop the app and clear logs
adb shell am force-stop com.trinet.app
adb logcat -c

# Start capturing logs
adb logcat -s ArtiusIDSDK:* SharedContextManager:* APIManager:* CertificateManager:* TLSSessionManager:* > /tmp/cert_logs.txt &

# Launch the app
adb shell am start -n com.trinet.app/.MainActivity

# Wait 10 seconds for background registration
sleep 10

# Stop capturing (Ctrl+C or kill the logcat process)
```

**Send us `/tmp/cert_logs.txt`**

---

### Request #2: Check Certificate Storage

Add this to your `MainActivity.onCreate()` after SDK initialization:

```kotlin
// Give background coroutine time to complete
Handler(Looper.getMainLooper()).postDelayed({
    val prefs = getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
    val hasCert = prefs.contains("CERTIFICATE_PEM")
    Log.e("TRINET_DEBUG", "========================================")
    Log.e("TRINET_DEBUG", "Certificate stored: $hasCert")
    Log.e("TRINET_DEBUG", "Certificate storage keys: ${prefs.all.keys}")
    Log.e("TRINET_DEBUG", "========================================")
}, 5000)  // Wait 5 seconds
```

**Send us the output of this log.**

---

### Request #3: Network Connectivity Test

Test if your device can reach the certificate server:

```bash
adb shell ping -c 4 sandbox.registration.artiusid.dev
```

**Send us the output.**

---

## 🔍 What Logs To Look For

### Success Logs (What We SHOULD See):

```
I/ArtiusIDSDK: 🌐 Environment set to: Sandbox
I/ArtiusIDSDK:    Certificate: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/SharedContextManager: 📊 Shared Context Status:
D/SharedContextManager:   - Certificate Context Shared: true
D/ArtiusIDSDK: 🔐 Initializing shared mTLS certificate...
D/SharedContextManager: 🔐 Ensuring shared certificate using host app context...
D/SharedContextManager:   - Certificate URL from UrlBuilder: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/APIManager: Loading certificate from full URL: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D/APIManager: 🔓 Creating plain OkHttpClient for certificate registration
D/APIManager: 🔒 HTTPS connection verified for certificate registration
D/SharedContextManager: ✅ Shared certificate ready
D/ArtiusIDSDK: ✅ Shared mTLS certificate initialization completed
```

### Failure Logs (What's Probably Happening):

```
E/ArtiusIDSDK: ❌ Certificate initialization failed, but continuing with SDK initialization
E/SharedContextManager: ❌ Failed to ensure shared certificate using host context
[Exception details]
```

---

## 🔧 Temporary Manual Workaround

While we wait for your logs, try this manual registration in your `MainActivity`:

```kotlin
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ... existing SDK initialization ...
        
        // TEMPORARY: Manually trigger certificate registration
        lifecycleScope.launch {
            delay(2000)  // Wait for SDK init
            
            try {
                val deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                
                val sharedPrefs = getSharedPreferences("certificate_prefs", Context.MODE_PRIVATE)
                if (!sharedPrefs.contains("CERTIFICATE_PEM")) {
                    Log.e("TRINET_DEBUG", "🔴 Certificate NOT registered, attempting manual registration...")
                    
                    val apiManager = com.artiusid.sdk.services.APIManager(this@MainActivity)
                    apiManager.loadCertificateFromFullUrl(
                        deviceId,
                        "https://sandbox.registration.artiusid.dev/LoadCertificateFunction"
                    )
                    
                    Log.e("TRINET_DEBUG", "✅ Manual certificate registration SUCCEEDED")
                } else {
                    Log.e("TRINET_DEBUG", "✅ Certificate already registered")
                }
            } catch (e: Exception) {
                Log.e("TRINET_DEBUG", "❌ Manual certificate registration FAILED", e)
                Log.e("TRINET_DEBUG", "Error: ${e.message}")
                Log.e("TRINET_DEBUG", "Stack trace: ${e.stackTraceToString()}")
            }
        }
    }
}
```

**This will:**
1. Tell us if certificate is missing
2. Attempt to register it manually
3. Show the exact error if registration fails

---

## 📊 Summary

| Item | Status |
|------|--------|
| **Certificate registration code** | ✅ Present in SDK v1.2.12 |
| **Automatic execution** | ✅ Runs during `initializeWithEnhancedTheme()` |
| **Sandbox URL configuration** | ✅ Correct: `sandbox.registration.artiusid.dev` |
| **Why it's not working** | ❓ Need logs to determine |

---

## 🎯 Next Steps

**Your Action:**
1. Run the 3 commands above
2. Add manual registration code to your app
3. Send us the outputs

**Our Action (Once We Have Logs):**
1. Identify exact failure point
2. Provide specific fix
3. Rebuild SDK if needed

---

## ⏱️ Timeline

**Immediate (Next 30 minutes):**
- You: Run commands and send logs

**Short-term (1-2 hours):**
- Us: Analyze logs and identify root cause

**Resolution (Same day):**
- Us: Provide fix, workaround, or new SDK build

---

**Status:** ⏸️ Awaiting your logs  
**Priority:** P0 - CRITICAL  

**Documentation:** See `CERTIFICATE_REGISTRATION_INVESTIGATION.md` for full technical analysis.

---

*Generated: October 17, 2025, 3:35 PM*  
*SDK Version: v1.2.12*


