# TriNet App: Certificate Architecture Update Guide

**Date:** October 24, 2025  
**SDK Version:** v1.2.45+  
**Priority:** HIGH - Required for proper certificate management  

## 🚨 **Issue Summary**

The current certificate management architecture violates proper SDK encapsulation. Client apps (including TriNet) should **NOT** manage certificates directly or duplicate PEM files. All certificate management must be handled by the ArtiusID SDK.

## 🏗️ **Current vs. Correct Architecture**

### ❌ **Current (Incorrect) Architecture:**
```
TriNet App
├── Has own PEM certificate files
├── Directly accesses SDK internal classes:
│   ├── com.artiusid.sdk.utils.CertificateManager
│   ├── com.artiusid.sdk.services.APIManager
│   └── com.artiusid.sdk.utils.TLSSessionManager
└── Duplicates certificate management logic
```

### ✅ **Correct Architecture:**
```
TriNet App
├── Uses ONLY SDK public APIs
├── NO certificate files
├── Delegates all certificate management to SDK
└── SDK handles all mTLS internally

ArtiusID SDK
├── Contains all PEM certificate files
├── Manages certificate registration/validation
├── Handles mTLS connections
└── Provides public APIs for certificate status
```

## 🔧 **Required Changes**

### **1. Remove Certificate Files from TriNet App**

**Delete these files if they exist:**
```bash
# Remove any PEM files from your app
rm -f app/src/main/res/raw/api_cert_chain.pem
rm -f app/src/main/res/raw/api-cert-chain.pem
rm -f app/src/main/res/raw/validated_ca_chain.pem
rm -f app/src/main/assets/api-cert-chain.pem
```

### **2. Replace Direct SDK Internal Access**

**❌ Remove these imports/usages:**
```kotlin
// DO NOT USE - These are internal SDK classes
import com.artiusid.sdk.utils.CertificateManager
import com.artiusid.sdk.services.APIManager
import com.artiusid.sdk.utils.TLSSessionManager
import com.artiusid.sdk.utils.SharedContextManager

// DO NOT USE - Direct certificate manager access
val certManager = CertificateManager(context)
val cert = certManager.loadCertificatePem()
val apiManager = APIManager(context)
```

**✅ Use these SDK public APIs instead:**
```kotlin
// CORRECT - Use only public SDK APIs
import com.artiusid.sdk.ArtiusIDSDK

// Check if certificate exists and is valid
val hasCertificate = ArtiusIDSDK.hasCertificate(context)

// Get detailed certificate status for debugging
val certStatus = ArtiusIDSDK.getCertificateStatus(context)
val statusText = certStatus["status"] as? String ?: "Unknown"
val certLength = certStatus["certificateLength"] as? Int ?: 0
val hasValidKey = certStatus["hasValidKey"] as? Boolean ?: false

// Ensure certificate is registered (call before verification)
lifecycleScope.launch {
    val isReady = ArtiusIDSDK.ensureCertificateRegistered(context)
    if (isReady) {
        // Certificate is ready, can start verification
        ArtiusIDSDK.startVerificationFlow(...)
    }
}

// Clear certificate for testing/debugging
val cleared = ArtiusIDSDK.clearCertificate(context)
```

### **3. Update Certificate Status UI**

**❌ Old approach:**
```kotlin
// DON'T DO THIS
val certManager = CertificateManager(context)
val hasCert = certManager.loadCertificatePem() != null
val keyMatch = certManager.verifyCertificateKeyMatch()
```

**✅ New approach:**
```kotlin
// CORRECT APPROACH
val hasCertificate = ArtiusIDSDK.hasCertificate(context)
val certStatus = ArtiusIDSDK.getCertificateStatus(context)

// Display certificate status
Text(
    text = if (hasCertificate) "✅ Certificate Ready" else "❌ Certificate Missing",
    color = if (hasCertificate) Color.Green else Color.Red
)

// Show detailed status for debugging
val statusText = certStatus["status"] as? String ?: "Unknown"
Text(text = statusText, fontSize = 12.sp)
```

### **4. Certificate Initialization Sequence**

**Update your app initialization to use proper SDK APIs:**

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize SDK first
        ArtiusIDSDK.initialize(
            context = this,
            configuration = SDKConfiguration(
                apiKey = "your_api_key",
                environment = Environment.PRODUCTION, // or SANDBOX
                clientId = 2, // TriNet's client ID
                clientGroupId = 2, // TriNet's client group ID
                enableLogging = true,
                hostAppPackageName = packageName
            )
        )
        
        // 2. Ensure certificate is ready (async)
        lifecycleScope.launch {
            try {
                val certificateReady = ArtiusIDSDK.ensureCertificateRegistered(this@MainActivity)
                if (certificateReady) {
                    Log.d("TriNet", "✅ Certificate ready for mTLS")
                    // Now safe to use verification/approval flows
                } else {
                    Log.e("TriNet", "❌ Certificate registration failed")
                    // Handle certificate error
                }
            } catch (e: Exception) {
                Log.e("TriNet", "Certificate initialization error", e)
            }
        }
    }
}
```

### **5. Remove Certificate Management Logic**

**Delete any custom certificate management code:**

```kotlin
// REMOVE ALL OF THIS - SDK handles it internally
// - Certificate generation logic
// - CSR creation
// - Certificate storage/retrieval
// - mTLS configuration
// - Certificate validation
// - Key pair management
```

## 📋 **Available SDK Public APIs**

### **Certificate Management APIs:**
```kotlin
// Check if certificate exists (quick check)
ArtiusIDSDK.isCertificateRegistered(context): Boolean

// Check if certificate exists and is valid (thorough check)
ArtiusIDSDK.hasCertificate(context): Boolean

// Get detailed certificate status for debugging
ArtiusIDSDK.getCertificateStatus(context): Map<String, Any>
// Returns: hasCertificate, certificateLength, hasValidKey, status

// Ensure certificate is registered (async)
suspend ArtiusIDSDK.ensureCertificateRegistered(context): Boolean

// Clear certificate for testing
ArtiusIDSDK.clearCertificate(context): Boolean
```

### **Main SDK APIs:**
```kotlin
// Initialize SDK
ArtiusIDSDK.initialize(context, configuration)

// Start verification flow
ArtiusIDSDK.startVerificationFlow(context, callback)

// Start approval flow  
ArtiusIDSDK.startApprovalFlow(context, callback)

// Send approval response
ArtiusIDSDK.sendApprovalResponse(context, approved)
```

## 🔍 **Testing & Validation**

### **1. Verify Certificate Removal**
```bash
# Ensure no PEM files in your app
find your_app_directory -name "*.pem" -type f
# Should return no results
```

### **2. Test Certificate APIs**
```kotlin
// Test certificate status
val status = ArtiusIDSDK.getCertificateStatus(this)
Log.d("TriNet", "Certificate status: $status")

// Test certificate check
val hasCert = ArtiusIDSDK.hasCertificate(this)
Log.d("TriNet", "Has certificate: $hasCert")
```

### **3. Test Complete Flow**
```kotlin
lifecycleScope.launch {
    // 1. Ensure certificate
    val ready = ArtiusIDSDK.ensureCertificateRegistered(this@MainActivity)
    Log.d("TriNet", "Certificate ready: $ready")
    
    // 2. Test verification (if certificate ready)
    if (ready) {
        ArtiusIDSDK.startVerificationFlow(this@MainActivity) { result ->
            Log.d("TriNet", "Verification result: $result")
        }
    }
}
```

## ⚠️ **Important Notes**

1. **Client ID Configuration:** Ensure TriNet uses `clientId = 2` and `clientGroupId = 2` in SDK configuration
2. **Environment:** Use `Environment.PRODUCTION` for production, `Environment.SANDBOX` for testing
3. **Certificate Domains:** SDK certificates cover all required ArtiusID domains automatically
4. **mTLS:** All mTLS configuration is handled internally by the SDK
5. **Error Handling:** Always check certificate status before starting verification/approval flows

## 🚀 **Benefits of Correct Architecture**

- **Security:** Centralized certificate management reduces security risks
- **Maintenance:** No need to update certificates in multiple apps
- **Reliability:** SDK handles certificate rotation and validation automatically  
- **Consistency:** All ArtiusID integrations use the same certificate management
- **Updates:** Certificate updates only require SDK updates, not app changes

## 📞 **Support**

If you encounter issues during implementation:
1. Check SDK logs for certificate-related errors
2. Verify SDK configuration (clientId, environment)
3. Test certificate APIs individually before full integration
4. Contact ArtiusID support with specific error messages and logs

---

**This update is required for proper SDK integration and must be implemented before production deployment.**
