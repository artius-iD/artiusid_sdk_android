# ArtiusID SDK - Sample Application

**For Internal Testing and Reference Implementation**

---

## 📱 Overview

This sample application demonstrates a complete integration of the ArtiusID Android SDK. Use it to:

- **Test SDK functionality** across different environments
- **Reference implementation** when building client apps
- **Debug SDK issues** with live logging
- **Validate releases** before deploying to clients

---

## 🚀 Quick Start

### **1. Prerequisites**

- Android Studio Jellyfish (2023.3.1) or newer
- Android device or emulator (API 24+)
- Firebase account (already configured)

### **2. Build and Run**

```bash
# From the project root
./gradlew :sample-app:assembleDebug

# Or run directly from Android Studio
# Select "sample-app" configuration and click Run
```

### **3. First Launch**

1. **Select Environment:** Choose Sandbox (default), Development, or Staging
2. **Initialize SDK:** Click "Initialize SDK"
3. **Start Verification:** Click "Start Verification" to test the complete flow
4. **Test Features:** Try verification, authentication, and approval requests

---

## 🏗️ Architecture

### **Main Components**

#### **BridgeMainActivity.kt**
Main activity demonstrating:
- SDK initialization with custom branding
- Environment management (Sandbox/Development/Staging)
- Verification flow integration
- Authentication flow integration
- Approval request system
- FCM token management
- Credential handling per environment

#### **SampleFirebaseMessagingService.kt**
Custom Firebase service showing:
- FCM token generation and storage
- Token updates provided to SDK
- Notification display (high priority)
- Data payload parsing

#### **SampleApplication.kt**
Application class with:
- Hilt dependency injection setup
- App-level initialization

#### **config/AppUrlConfig.kt**
Environment configuration:
- Sandbox, Development, and Staging URLs
- Certificate registration URLs
- Environment detection logic

#### **theme/SampleAppThemes.kt**
Theme configurations demonstrating:
- Multiple brand themes
- Custom colors and fonts
- Asset overrides

#### **localization/SampleAppLocalization.kt**
String customization examples:
- Custom welcome messages
- Branded instruction text
- Error message overrides

---

## 🌐 Environments

The sample app supports three environments with runtime switching:

| Environment | Purpose | Backend URL |
|------------|---------|-------------|
| **Sandbox** | Default testing | `sandbox.mobile.artiusid.dev` |
| **Development** | Dev environment | `service-mobile.dev.artiusid.dev` |
| **Staging** | Pre-production | `service-mobile.stage.artiusid.dev` |

### **Environment Switching**

Use the dropdown in the main screen to switch environments. Note:
- **Credentials are isolated** per environment
- **Switching clears UI state** from the previous environment
- **Each environment requires its own verification**
- **FCM tokens are stored per environment**

---

## 🔥 Firebase Configuration

### **google-services.json**

The app includes Firebase configuration for:

```json
{
  "project_id": "artiusid",
  "package_name": "com.artiusid.sampleapp"
}
```

**Configured for:**
- Firebase Cloud Messaging (FCM)
- Firebase Authentication (if needed in future)

### **Firebase Messaging Service**

The custom `SampleFirebaseMessagingService` handles:

1. **Token Generation**
   ```kotlin
   override fun onNewToken(token: String) {
       // Save to secure storage
       tokenManager.saveToken(token, currentEnvironment)
       // Provide to SDK
       ArtiusIDSDK.updateFcmToken(token)
   }
   ```

2. **Notification Display**
   - HIGH importance channel
   - Sound, vibration, LED lights
   - Heads-up notifications
   - Custom notification actions

---

## 🎨 Theme Customization

The sample app demonstrates multiple theme configurations:

### **Default Theme**
```kotlin
val defaultTheme = SDKThemeConfiguration(
    brandName = "artius.iD",
    primaryColorHex = "#003DA5",
    secondaryColorHex = "#00B4D8",
    logoResourceName = "logo_ios"
)
```

### **Modern Theme**
```kotlin
val modernTheme = SDKThemeConfiguration(
    brandName = "ModernBrand",
    primaryColorHex = "#6366F1",
    secondaryColorHex = "#EC4899",
    logoResourceName = "custom_logo"
)
```

### **Corporate Theme**
```kotlin
val corporateTheme = SDKThemeConfiguration(
    brandName = "CorpID",
    primaryColorHex = "#1E293B",
    secondaryColorHex = "#3B82F6",
    logoResourceName = "corporate_logo"
)
```

**Switch themes** by changing the configuration in `BridgeMainActivity.kt`.

---

## 📦 Asset Overrides

The sample app includes custom assets to demonstrate SDK customization:

### **Asset Directory Structure**

```
sample-app/src/main/assets/
├── README.md                    # Asset documentation
├── test_override_working.txt    # Test file
├── corporate/                   # Corporate theme assets
│   ├── README.md
│   ├── animations/
│   │   ├── face_rotation.gif
│   │   ├── passport_animation.gif
│   │   └── stateid_animation.gif
│   └── images/
│       ├── logo.png
│       ├── face_overlay.png
│       ├── passport_overlay.png
│       └── ... (more images)
└── modern/                      # Modern theme assets
    └── README.md
```

### **How Asset Overrides Work**

1. Place assets in `sample-app/src/main/assets/corporate/` (or other theme folder)
2. Use exact filenames matching SDK resources
3. Configure theme with `assetThemeDirectory = "corporate"`
4. SDK automatically loads custom assets

See [`assets/README.md`](src/main/assets/README.md) for complete documentation.

---

## 🔐 Testing Flows

### **1. Verification Flow**

**Test Steps:**
1. Click "Start Verification"
2. Complete face liveness detection
3. Scan document (Passport or State ID)
4. For passport: complete NFC reading
5. Review and submit
6. Verify success and member ID saved

**What to Check:**
- [ ] All screens display correctly
- [ ] Camera works properly
- [ ] NFC reading succeeds (for passport)
- [ ] Member ID is displayed after completion
- [ ] Member ID persists across app restarts

### **2. Authentication Flow**

**Test Steps:**
1. Complete verification first (required)
2. Click "Authenticate"
3. Complete face authentication
4. Verify success

**What to Check:**
- [ ] Authentication requires prior verification
- [ ] Face matching works correctly
- [ ] Authentication completes successfully

### **3. Approval Requests**

**Test Steps:**
1. Complete verification first (required)
2. Enter approval title and description
3. Click "Send Approval Request"
4. Wait for notification

**What to Check:**
- [ ] Approval request submits successfully
- [ ] Notification is received (sound + vibration)
- [ ] Notification displays correct title/description
- [ ] Tapping notification opens app

**Troubleshooting:**
- Ensure verification was completed in current environment
- Check that FCM token was sent with verification
- Verify Firebase service is registered in manifest

### **4. Environment Switching**

**Test Steps:**
1. Complete verification in Sandbox
2. Switch to Development environment
3. Note that member ID is cleared
4. Complete verification in Development
5. Switch back to Sandbox
6. Verify original member ID is restored

**What to Check:**
- [ ] Credentials isolated per environment
- [ ] UI updates correctly on switch
- [ ] Returning to previous environment restores data
- [ ] Certificates registered per environment

---

## 🐛 Debugging

### **Enable Verbose Logging**

In `BridgeMainActivity.kt`:

```kotlin
val sdkConfig = SDKConfiguration(
    // ...
    enableLogging = true  // Set to true for verbose logs
)
```

### **View Logs**

```bash
# All SDK logs
adb logcat | grep -E "ArtiusIDSDK|Sample"

# Verification flow only
adb logcat | grep VerificationProcessingVM

# Firebase logs
adb logcat | grep -E "FCM|Firebase"

# Certificate logs
adb logcat | grep CertificateManager
```

### **Clear App Data**

```bash
# Clear all data (credentials, preferences, etc.)
adb shell pm clear com.artiusid.sampleapp

# Reinstall app
adb uninstall com.artiusid.sampleapp
adb install sample-app/build/outputs/apk/debug/sample-app-debug.apk
```

### **Common Issues**

| Issue | Solution |
|-------|----------|
| **App crashes on startup** | Check `google-services.json` exists |
| **No FCM token** | Verify Firebase is initialized |
| **Verification stuck** | Clear app data and retry |
| **Certificate error** | Clear certificate and re-register |
| **Wrong member ID after env switch** | Update to latest SDK version |

---

## 📦 Build Variants

### **debug**
Development builds with:
- Debug symbols
- Verbose logging
- No obfuscation
- Larger APK size (~30MB)

```bash
./gradlew :sample-app:assembleDebug
```

### **release**
Release builds with:
- ProGuard obfuscation
- Optimized code
- Smaller APK size (~20MB)
- Requires keystore

```bash
./gradlew :sample-app:assembleRelease
```

### **customerDistribution**
Special build for client distribution:
- Heavy obfuscation
- All optimizations enabled
- Stripped symbols
- Customer-ready APK (~173MB with all assets)

```bash
./gradlew :sample-app:assembleCustomerDistribution
```

---

## 📄 Configuration Files

### **build.gradle**
App-level Gradle configuration with:
- SDK dependency (local AAR)
- Firebase dependencies
- Hilt setup
- Build variants

### **google-services.json**
Firebase configuration (tracked in Git for internal use).

### **AndroidManifest.xml**
App manifest declaring:
- Permissions (camera, internet, NFC, etc.)
- Activities
- Firebase messaging service
- Intent filters

### **proguard-rules-*.pro**
ProGuard rules for release and customer distribution builds:
- `proguard-rules-customer.pro` – Used for release and customer distribution variants.

---

## 🧪 Testing Checklist

Use this checklist when validating SDK changes:

### **Basic Functionality**
- [ ] App builds and installs successfully
- [ ] SDK initializes without errors
- [ ] All three environments work
- [ ] Environment switching works correctly

### **Verification Flow**
- [ ] Face liveness detection works
- [ ] Passport scanning works
- [ ] State ID scanning works
- [ ] NFC reading works
- [ ] Verification submits successfully
- [ ] Member ID is saved and displayed

### **Authentication Flow**
- [ ] Authentication requires verification
- [ ] Face authentication works
- [ ] Authentication submits successfully

### **Approval Requests**
- [ ] Approval request submits successfully
- [ ] Notification is received
- [ ] Notification displays correctly
- [ ] Notification opens app

### **UI/UX**
- [ ] All screens display correctly
- [ ] Animations play smoothly
- [ ] Colors and branding correct
- [ ] Text is readable and clear
- [ ] Back navigation works

### **Error Handling**
- [ ] Network errors handled gracefully
- [ ] Permission denials handled
- [ ] Invalid data handled
- [ ] Timeout scenarios handled

---

## 📚 Related Documentation

### **SDK Documentation**
- **[DEVELOPER_README.md](../DEVELOPER_README.md)** - Internal developer guide
- **[SDK Dependency Requirements](../SDK_DEPENDENCY_REQUIREMENTS.md)** - Required dependencies
- **[HILT Integration Guide](../HILT_INTEGRATION_GUIDE.md)** - HILT setup

### **Sample App Documentation**
- **[Localization Guide](LOCALIZATION_GUIDE.md)** - String customization
- **[Asset Documentation](src/main/assets/README.md)** - Asset overrides

### **Client Documentation**
- **[Client Implementation Guide](../docs/client/CLIENT_IMPLEMENTATION_GUIDE.md)** - For external clients
- **[Release Notes](../docs/client/)** - Version-specific notes

---

## 🤝 Contributing

When making changes to the sample app:

1. **Test thoroughly** using the testing checklist above
2. **Update documentation** if you change functionality
3. **Test all environments** (Sandbox, Development, Staging)
4. **Verify builds** (debug, release, customerDistribution)
5. **Check logs** for any warnings or errors

---

## 📞 Support

For issues with the sample app:
- Check the troubleshooting section above
- Review logs with `adb logcat`
- Consult the SDK team

---

## 🎯 Use Cases

### **For SDK Developers**
Use the sample app to:
- Test new SDK features
- Validate bug fixes
- Verify integrations
- Debug issues

### **For QA Testing**
Use the sample app to:
- Execute test plans
- Validate releases
- Reproduce bugs
- Performance testing

### **For Client Support**
Use the sample app to:
- Demonstrate proper integration
- Troubleshoot client issues
- Validate client implementations
- Answer integration questions

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

**Package:** `com.artiusid.sampleapp`  
**Min SDK:** 24 (Android 7.0)  
**Target SDK:** 34 (Android 14)

