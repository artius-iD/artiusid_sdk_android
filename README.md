# ArtiusID Android SDK

A secure Android SDK for identity verification, face liveness detection, document scanning, and NFC passport reading.

---

## 📦 **Latest Release**

**Version:** 1.2.41 ✅ STABLE RELEASE  
**Release Date:** October 23, 2025  
**Download:** [GitHub Releases](https://github.com/artius-iD/artiusid_sdk_android/releases)

---

## ✅ **STABLE RELEASE - v1.2.41**

**PRODUCTION READY** - This version resolves all critical verification issues and includes iOS API compatibility improvements.

**What's Fixed:**
- ✅ **Duplicate verification call timing issue** - Fixed millisecond-level duplicate calls
- ✅ **iOS-compliant approval API models** - Perfect compatibility with iOS backend responses
- ✅ **VerificationGuard stability** - Comprehensive stuck state prevention and recovery
- ✅ **Enhanced debugging** - Stack trace logging for duplicate call investigation
- ✅ **Robust error handling** - Automatic recovery from all known edge cases

**Upgrade Priority:** **RECOMMENDED** - Stable production release with all critical fixes.

---

## 🚀 **Quick Start**

### **1. Download the SDK**

Download the latest AAR from the [releases page](https://github.com/artius-iD/artiusid_sdk_android/releases):
```bash
# Download SDK v1.2.41 (STABLE RELEASE)
curl -L -o artiusid-sdk-1.2.41.aar \
  https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.41/artiusid-sdk-1.2.41.aar
```

### **2. Add to Your Project**

Copy the AAR to your app's `libs` directory:
```bash
cp artiusid-sdk-1.2.41.aar your-app/app/libs/
```

### **3. Configure Dependencies**

Add to your app's `build.gradle`:
```gradle
dependencies {
    implementation files('libs/artiusid-sdk-1.2.41.aar')
    
    // Required dependencies
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // Compose
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    
    // Image loading (required for SDK animations)
    implementation 'io.coil-kt:coil-compose:2.5.0'
    implementation 'io.coil-kt:coil-gif:2.5.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Firebase (required for FCM functionality)
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    
    // Biometric authentication
    implementation 'androidx.biometric:biometric:1.1.0'
}
```

### **4. Initialize the SDK**

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val config = SDKConfiguration(
            apiKey = "your-api-key",
            environment = Environment.PRODUCTION,
            
            // ✅ NEW: Configurable client ID (v1.2.37+)
            clientId = 1,        // Your unique client ID
            clientGroupId = 1,   // Your client group ID
            
            enableLogging = BuildConfig.DEBUG
        )
        
        val theme = SDKThemeConfiguration(
            brandName = "YourBrand",
            primaryColorHex = "#YOUR_PRIMARY_COLOR",
            secondaryColorHex = "#YOUR_SECONDARY_COLOR"
        )
        
        ArtiusIDSDK.initializeWithEnhancedTheme(this, config, theme)
    }
}
```

### **5. Start Verification**

```kotlin
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

ArtiusIDSDK.startVerification(
    activity = this,
    callback = object : VerificationCallback {
        override fun onVerificationSuccess(result: VerificationResult) {
            // Handle successful verification
            Log.d("App", "Verification successful: ${result.verificationId}")
        }
        
        override fun onVerificationError(error: SDKError) {
            // Handle error
            Log.e("App", "Verification error: ${error.message}")
        }
        
        override fun onVerificationCancelled() {
            // Handle cancellation
            Log.d("App", "Verification cancelled by user")
        }
    }
)
```

---

## 📚 **Documentation**

### **Essential Guides:**
- **[HILT Integration Guide](HILT_INTEGRATION_GUIDE.md)** - Complete HILT setup instructions
- **[HILT Quick Setup](README_HILT_SETUP.md)** - Quick reference for HILT configuration
- **[SDK Dependencies](SDK_DEPENDENCY_REQUIREMENTS.md)** - Required dependencies and versions

### **Sample App:**
- **[Localization Guide](sample-app/LOCALIZATION_GUIDE.md)** - How to customize SDK strings
- **[Asset Documentation](sample-app/src/main/assets/README.md)** - Theme assets and customization

### **Technical Documentation:**
- **[Enhanced Autofocus Guide](artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/EnhancedAutofocusGuide.md)** - Camera autofocus implementation

---

## 🔧 **HILT Setup**

### **Automated Setup (Recommended):**
```bash
./setup_hilt.sh
```

### **Diagnostic Tool:**
```bash
./gradlew diagnoseHilt
```

### **Manual Setup:**
Follow the step-by-step guide in [HILT_INTEGRATION_GUIDE.md](HILT_INTEGRATION_GUIDE.md)

---

## 🎯 **Configurable Client ID (NEW in v1.2.37)**

Configure unique client identities for different applications:

```kotlin
val config = SDKConfiguration(
    apiKey = "your-api-key",
    environment = Environment.PRODUCTION,
    
    // Different apps use different client IDs
    clientId = 2,        // TriNet app uses clientId=2
    clientGroupId = 2,   // TriNet app uses clientGroupId=2
    
    enableLogging = BuildConfig.DEBUG
)
```

### **Benefits:**
- ✅ **Fixes FCM notification routing** - No more cross-app notifications
- ✅ **Backend client separation** - Each app has unique identity
- ✅ **iOS SDK compatibility** - Matches iOS AppConstants functionality
- ✅ **Multi-client architecture** - Supports enterprise deployments

### **Usage Examples:**
- **Sample App:** `clientId = 1` (default/demo)
- **TriNet App:** `clientId = 2` (production client)
- **Enterprise App:** `clientId = 100` (custom client)

---

## 🎨 **Dynamic Branding**

Configure your brand name in the SDK theme:

```kotlin
val theme = SDKThemeConfiguration(
    brandName = "YourBrand",  // Replaces "artius.iD" throughout the UI
    primaryColorHex = "#YOUR_COLOR",
    secondaryColorHex = "#YOUR_ACCENT_COLOR"
)
```

The SDK will automatically:
- Display your brand name in all UI components
- Use your brand in Firebase notifications
- Apply intelligent text splitting (e.g., "Your.Brand" → "Your" + "Brand")

---

## 📋 **Requirements**

- **Minimum SDK:** Android 7.0 (API level 24)
- **Target SDK:** Android 14 (API level 34)
- **Kotlin:** 1.9.0+
- **HILT:** 2.48 (exact version required)
- **Gradle:** 8.0+
- **Firebase Project:** Required for authentication and messaging

---

## 🔒 **Security Features**

- Fully obfuscated AAR for IP protection
- Hardware-backed encryption
- Certificate pinning support
- Anti-tampering protection
- Secure keychain storage (iOS Keychain equivalent)

---

## 🐛 **Troubleshooting**

### **HILT Issues:**
1. Run `./gradlew diagnoseHilt` for automated diagnosis
2. Check [HILT_INTEGRATION_GUIDE.md](HILT_INTEGRATION_GUIDE.md) for detailed setup
3. Ensure exact HILT version 2.48 is used

### **Branding Issues:**
- Verify `SDKThemeConfiguration.brandName` is set
- Check that `@AndroidEntryPoint` is on your Activity
- Ensure Firebase is properly initialized

### **Certificate Issues:**
- Clear app data: `adb shell pm clear your.package.name`
- Check logs for certificate registration errors
- Verify network connectivity

---

## 📞 **Support**

### **Technical Support:**
- Email: support@artiusid.com
- GitHub: https://github.com/artius-iD/artiusid_sdk_android

### **Licensing:**
- Email: legal@artiusid.com

---

## 📝 **Changelog**

### **v1.2.41 (October 23, 2025) - ✅ STABLE RELEASE**
- ✅ **FIXED:** Duplicate verification call timing issue causing millisecond-level conflicts
- ✅ **NEW:** iOS-compliant approval API models (ApprovalRequestData, ApprovalRequestTestingResponse)
- ✅ **IMPROVED:** Updated SendApprovalRequest to use nested response structure (response.approvalData.*)
- ✅ **ENHANCED:** Stack trace logging for duplicate call investigation and debugging
- ✅ **STABLE:** All critical verification issues resolved - production ready

### **v1.2.40 (October 23, 2025) - 🔍 DIAGNOSTIC BUILD**
- 🔍 **DIAGNOSTIC:** Added comprehensive stack trace logging to identify duplicate call sources
- 🔍 **ENHANCED:** Detailed timing and thread ID logging in VerificationGuard
- 🔍 **DEBUG:** Enhanced LaunchedEffect and VerificationProcessingViewModel logging
- 🔍 **INVESTIGATION:** Millisecond-precision timing to track duplicate call origins

### **v1.2.39 (October 23, 2025) - 🚨 CRITICAL FIX**
- 🚨 **CRITICAL:** Fixed VerificationGuard stuck state persisting across app restarts
- ✅ Added initialization block to ensure clean state on app startup
- ✅ Fixed timeout calculation handling edge cases (0L timestamps)
- ✅ Added comprehensive state validation and automatic recovery
- ✅ Enhanced logging with detailed state information for debugging
- ✅ Improved DisposableEffect cleanup with robust error handling
- ✅ Added getDebugState() and forceReset() methods for troubleshooting
- ✅ Resolves permanent verification blocking and (0s) elapsed time issues

### **v1.2.38 (October 23, 2025) - 🚨 CRITICAL FIX**
- 🚨 **CRITICAL:** Fixed EncryptedSharedPreferences corruption after certificate clearing
- ✅ Added automatic corruption detection and recovery
- ✅ New EncryptedPreferencesManager utility class
- ✅ Updated all certificate managers with safe methods
- ✅ Comprehensive logging and error handling
- ✅ Users can clear and re-register certificates without app data loss
- ✅ No more permanent AEADBadTagException blocking verification
- ✅ Production-ready recovery mechanisms

### **v1.2.37 (October 23, 2025)**
- 🎯 **NEW: Configurable Client ID** - Match iOS SDK functionality
- ✅ Fixes FCM notification routing between multiple apps
- ✅ Enables proper multi-client backend architecture
- ✅ Backward compatible (defaults to clientId=1)
- ✅ Centralized ClientConfiguration management

### **v1.2.36 (October 22, 2025)**
- 🐛 **CRITICAL FIX:** VerificationGuard singleton stuck state
- ✅ Added 2-minute timeout safety mechanism
- ✅ Fixed DisposableEffect guard reset
- ✅ Prevents permanent verification blocking

### **v1.2.35 (October 22, 2025)**
- 🐛 Fixed duplicate verification submissions
- ✅ Singleton VerificationGuard implementation
- ✅ Cross-ViewModel instance protection

### **v1.2.21 (October 21, 2025)**
- 🐛 Fixed duplicate verification requests
- 🐛 Fixed duplicate authentication requests
- ✅ Only ONE verification request per flow
- ✅ 50% reduction in backend load

---

## 📦 **Package Contents**

- `artiusid-sdk-1.2.41.aar` - Main SDK library (25MB)
- `HILT_INTEGRATION_GUIDE.md` - Complete HILT setup guide
- `README_HILT_SETUP.md` - Quick HILT reference
- `SDK_DEPENDENCY_REQUIREMENTS.md` - Required dependencies
- `setup_hilt.sh` - Automated HILT configuration script
- `hilt_diagnostic_script.gradle` - HILT diagnostic tool
- Sample app with integration examples

---

## 🚀 **Example Integration**

Check out the `sample-app` directory for a complete working example that demonstrates:
- SDK initialization with custom branding
- Verification flow integration
- Authentication flow integration
- Theme customization
- Image overrides
- Localization
- Error handling
- Approval notifications

---

## 📄 **License**

Copyright © 2024-2025 artius.iD, Inc. All rights reserved.

This SDK is provided under license. See LICENSE.txt for full terms and conditions.

---

**Version:** 1.2.38  
**Release Date:** October 23, 2025  
**Package Size:** 25MB  
**Status:** Production Ready

