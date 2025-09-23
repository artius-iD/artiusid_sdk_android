# artius.iD SDK - Secure Distribution Model

## 🚀 **How to Securely Distribute Your SDK**

### **1. Build Secure Release AAR**

```bash
# Build obfuscated release version
./gradlew :artiusid-sdk:assembleRelease

# This creates: artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
```

**What customers receive:**
- ✅ **Compiled AAR file** (not source code)
- ✅ **Obfuscated bytecode** (unreadable internal implementation)  
- ✅ **Public API documentation** (integration guide only)
- ✅ **Consumer ProGuard rules** (automatic security for their apps)

### **2. Private Maven Repository (Recommended)**

```gradle
// Your private repository setup
repositories {
    maven {
        url "https://your-private-repo.com/maven"
        credentials {
            username = project.findProperty("repo.username")
            password = project.findProperty("repo.password")
        }
    }
}

// Customer integration (they add this to their build.gradle)
dependencies {
    implementation 'com.artiusid:sdk:1.0.0'
}
```

**Benefits:**
- 🔐 **Access Control**: Only authorized customers can download
- 📊 **Usage Tracking**: Monitor who's using which versions
- 🔄 **Version Management**: Control updates and rollbacks
- 🛡️ **License Enforcement**: Tie access to licensing agreements

### **3. Customer Signature Whitelisting**

**Before distribution, get customer's app signature:**

```bash
# Customer runs this to get their signature hash
keytool -list -v -keystore app-release.keystore -alias app-key | grep SHA256
```

**You add their hash to the SDK:**

```kotlin
// In SDKSecurityManager.kt
private val ALLOWED_SIGNATURE_HASHES = setOf(
    "customer1_production_signature_hash",
    "customer2_production_signature_hash",
    "customer3_production_signature_hash"
)
```

**Result:** SDK only works in their specific signed app.

### **4. What Customers Get vs. What They Don't**

#### **✅ Customers Receive:**
```
customer-sdk-package/
├── artiusid-sdk-release.aar          # Obfuscated compiled SDK
├── integration-guide.md              # Public API documentation  
├── sample-integration/               # Example integration code
│   ├── build.gradle                  # Dependencies and setup
│   └── MainActivity.kt               # Basic integration example
├── consumer-proguard-rules.pro       # Security rules for their app
└── LICENSE.txt                       # Usage license agreement
```

#### **❌ Customers DO NOT Receive:**
```
# These stay on your development servers only:
artiusid-sdk/src/                     # Source code
proguard-rules.pro                    # Your obfuscation config
SDK_SECURITY_GUIDE.md                 # Internal security docs
artiusid-sdk/build.gradle             # Build configuration
google-services.json                  # Your Firebase config
```

### **5. Integration Example for Customers**

**Customer's build.gradle:**
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                         'proguard-rules.pro'
            // SDK consumer rules applied automatically
        }
    }
}

dependencies {
    implementation 'com.artiusid:sdk:1.0.0'
    // All SDK dependencies included automatically
}
```

**Customer's integration code:**
```kotlin
// This is ALL they can access - public API only
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK with their branding
        val config = SDKConfiguration(
            environment = Environment.PRODUCTION,
            baseUrl = "https://api.artiusid.com"
        )
        
        val theme = EnhancedSDKThemeConfiguration(
            brandName = "Customer Brand",
            primaryColor = "#FF6B35",
            // ... their customizations
        )
        
        ArtiusIDSDK.initializeWithEnhancedTheme(this, config, theme)
    }
    
    private fun startVerification() {
        ArtiusIDSDK.startVerification(this, object : VerificationCallback {
            override fun onVerificationSuccess(result: VerificationResult) {
                // Handle success
            }
            override fun onVerificationError(error: SDKError) {
                // Handle error  
            }
        })
    }
}
```

### **6. Additional Security Measures**

#### **License Key Validation (Optional):**
```kotlin
// Add to SDK initialization
val config = SDKConfiguration(
    licenseKey = "customer-specific-license-key",
    environment = Environment.PRODUCTION
)
```

#### **Server-Side Validation:**
- Your backend validates client certificates
- Monitor for unusual usage patterns
- Revoke access for license violations

#### **Legal Protection:**
- Software license agreement
- Terms of service
- Anti-reverse engineering clauses
- Intellectual property protection

### **7. Security Monitoring**

**You can monitor:**
- 📊 Which customers are using the SDK
- 🔍 Attempted security violations
- 📈 Usage patterns and API calls
- ⚠️ Unauthorized access attempts

**Customer cannot:**
- 🚫 See your internal implementation
- 🚫 Modify SDK behavior
- 🚫 Extract sensitive logic
- 🚫 Use SDK without proper licensing

---

## **🎯 Bottom Line**

With this security implementation:

1. **Customers get a black box** - they can use it but can't see inside
2. **You maintain full control** - over functionality, updates, and access
3. **IP is protected** - obfuscation + runtime security prevents reverse engineering
4. **Licensing is enforced** - signature validation ensures only authorized apps work
5. **Updates are controlled** - you decide when/what customers receive

Your SDK is now **enterprise-grade secure** and ready for commercial distribution! 🛡️
