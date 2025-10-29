# ArtiusID Android SDK - Developer Guide

**For Internal ArtiusID Developers**

---

## 🏗️ Repository Structure

This is the **internal development repository** for the ArtiusID Android SDK. This repository contains:

- **SDK source code** (`artiusid-sdk/`)
- **Sample application** for testing (`sample-app/`)
- **Build scripts** and automation
- **Development tools** and diagnostics
- **Documentation** (both internal and client-facing)

---

## 🚀 Quick Start for New Developers

### **1. Prerequisites**

- **Android Studio:** Jellyfish (2023.3.1) or newer
- **JDK:** 17 or higher
- **Gradle:** 8.0+ (included via wrapper)
- **Kotlin:** 1.9.0+
- **Git:** For version control

### **2. Clone the Repository**

```bash
git clone git@gitlab.com:artiusid1/mobile-sdk-android.git
cd mobile-sdk-android
```

### **3. Open in Android Studio**

1. Open Android Studio
2. File → Open → Select `mobile-sdk-android` directory
3. Wait for Gradle sync to complete
4. Build → Make Project

### **4. Run the Sample App**

1. Select `sample-app` configuration from the run menu
2. Connect an Android device or start an emulator
3. Click Run (▶️)

---

## 📦 Project Modules

### **artiusid-sdk/**
The main SDK library module.

- **Language:** Kotlin
- **Output:** AAR file (Android Archive)
- **Build:** `./gradlew :artiusid-sdk:assembleRelease`
- **Output Location:** `artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar`

**Key Components:**
- Face liveness detection
- Document scanning (Passport, State ID)
- NFC passport reading
- Biometric authentication
- Approval request system
- mTLS certificate management
- Firebase integration utilities

### **sample-app/**
Test application for SDK integration.

- **Language:** Kotlin
- **Package:** `com.artiusid.sampleapp`
- **Purpose:** Reference implementation and testing
- **Build Variants:**
  - `debug` - Development builds
  - `release` - Obfuscated builds
  - `customerDistribution` - Client-ready APK

**Features Demonstrated:**
- SDK initialization with custom branding
- Verification flow
- Authentication flow  
- Approval requests
- Environment switching (Sandbox/Development/Staging)
- Firebase messaging integration
- Theme customization
- Asset overrides

---

## 🔧 Development Workflow

### **Building the SDK**

```bash
# Clean build
./gradlew clean

# Build release AAR (obfuscated)
./gradlew :artiusid-sdk:assembleRelease

# Build debug AAR (for development)
./gradlew :artiusid-sdk:assembleDebug

# Output location
ls -lh artiusid-sdk/build/outputs/aar/
```

### **Building the Sample App**

```bash
# Debug build (for testing)
./gradlew :sample-app:assembleDebug

# Customer distribution build (obfuscated)
./gradlew :sample-app:assembleCustomerDistribution

# Output location
ls -lh sample-app/build/outputs/apk/
```

### **Running Tests**

```bash
# Run unit tests
./gradlew test

# Run connected tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### **Code Quality**

```bash
# Lint checks
./gradlew lint

# HILT diagnostics
./gradlew diagnoseHilt
```

---

## 🌐 Environments

The SDK supports three environments:

| Environment | Purpose | Backend URL |
|------------|---------|-------------|
| **Sandbox** | Default testing | `sandbox.mobile.artiusid.dev` |
| **Development** | Dev environment | `service-mobile.dev.artiusid.dev` |
| **Staging** | Pre-production | `service-mobile.stage.artiusid.dev` |

**Environment Configuration:**
- Sample app allows runtime environment switching
- Each environment has isolated credentials
- Firebase tokens stored per-environment

---

## 🔑 Configuration Files

### **google-services.json**
Firebase configuration for the sample app.

**Location:** `sample-app/google-services.json`

**Contains:**
- Firebase project ID: `artiusid`
- App configurations for:
  - `com.artiusid.sampleapp` (sample app)
  - `com.artiusid.trinet` (TriNet client)

**⚠️ Important:** This file is tracked in Git for internal development but should be excluded from client distributions.

### **local.properties**
Local machine configuration (not tracked in Git).

**Location:** `local.properties`

**Contents:**
```properties
sdk.dir=/Users/your-username/Library/Android/sdk
```

---

## 🎨 SDK Architecture

### **Core Components**

1. **ArtiusIDSDK** - Main SDK interface
   - Initialization
   - Verification flow entry
   - Authentication flow entry
   - Configuration management

2. **VerificationProcessingViewModel** - Verification orchestration
   - Face liveness detection
   - Document scanning
   - NFC passport reading
   - Backend submission

3. **CertificateManager** - mTLS certificate handling
   - Certificate registration
   - Certificate storage (Android Keystore)
   - Certificate validation

4. **FirebaseTokenManager** - FCM token management
   - Token storage per environment
   - Token retrieval
   - Token updates

5. **EnvironmentCredentialManager** - Credential isolation
   - Per-environment storage
   - Environment detection
   - Credential migration

### **UI Framework**

- **Jetpack Compose** - Modern declarative UI
- **Material3** - Material Design components
- **Navigation Compose** - Screen navigation
- **Coil** - Image loading (GIF support for animations)

### **Dependencies**

- **Hilt** - Dependency injection (version 2.48 - exact version required)
- **Retrofit** - HTTP client
- **OkHttp** - Network layer
- **Room** - Local database (if needed in future)
- **Firebase** - Authentication, Messaging, Analytics

See `artiusid-sdk/build.gradle` for complete dependency list.

---

## 📱 Sample App Architecture

### **Main Activity: BridgeMainActivity**

The sample app's main activity demonstrates:

1. **SDK Initialization**
   ```kotlin
   ArtiusIDSDK.initializeWithEnhancedTheme(
       context = this,
       configuration = sdkConfig,
       themeConfig = themeConfig
   )
   ```

2. **Environment Management**
   - Runtime environment switching
   - Credential isolation per environment
   - UI updates on environment change

3. **Firebase Integration**
   - Custom `SampleFirebaseMessagingService`
   - FCM token retrieval and storage
   - Notification display

4. **Verification Flow**
   ```kotlin
   ArtiusIDSDK.startVerification(
       activity = this,
       callback = verificationCallback
   )
   ```

5. **Authentication Flow**
   ```kotlin
   ArtiusIDSDK.startAuthentication(
       activity = this,
       callback = authenticationCallback
   )
   ```

### **Custom Firebase Service: SampleFirebaseMessagingService**

Demonstrates client-controlled Firebase messaging:

- Token generation and storage
- Token updates provided to SDK
- Notification display
- Data payload parsing

---

## 🛠️ Development Tools

### **Version Management**

**Script:** `artiusid-sdk/scripts/version-manager.sh`

```bash
# Bump patch version (1.2.48 → 1.2.49)
./artiusid-sdk/scripts/version-manager.sh

# Bump minor version (1.2.48 → 1.3.0)
./artiusid-sdk/scripts/version-manager.sh minor

# Bump major version (1.2.48 → 2.0.0)
./artiusid-sdk/scripts/version-manager.sh major
```

### **HILT Diagnostics**

```bash
# Run HILT diagnostic tool
./gradlew diagnoseHilt

# Automated HILT setup
./setup_hilt.sh
```

### **Repository Cleanup**

**Script:** `cleanup-sdk-repository.sh`

Removes build artifacts, logs, and temporary files:

```bash
./cleanup-sdk-repository.sh
```

---

## 📦 Release Process

### **1. Prepare Release**

```bash
# Update version
./artiusid-sdk/scripts/version-manager.sh

# Update version in documentation
# - README.md
# - RELEASE_NOTES_vX.X.XX.md
# - DEPLOYMENT_SUMMARY_vX.X.XX.md
```

### **2. Build Release Artifacts**

```bash
# Clean build
./gradlew clean

# Build SDK AAR
./gradlew :artiusid-sdk:assembleRelease

# Build sample app
./gradlew :sample-app:assembleCustomerDistribution

# Copy AAR to root
cp artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar ./artiusid-sdk-vX.X.XX.aar
```

### **3. Create Documentation**

Create or update:
- `RELEASE_NOTES_vX.X.XX.md` - Technical release notes
- `DEPLOYMENT_SUMMARY_vX.X.XX.md` - Deployment summary
- `CLIENT_IMPLEMENTATION_GUIDE.md` - Client integration guide (if needed)

### **4. Commit and Tag**

```bash
# Commit changes
git add .
git commit -m "Release v1.2.XX"

# Create tag
git tag -a v1.2.XX -m "Release v1.2.XX"

# Push to GitLab
git push origin main
git push origin v1.2.XX
```

### **5. Publish to GitHub**

**Script:** `artiusid-sdk/scripts/publish-android-github-essential.sh`

```bash
cd artiusid-sdk/scripts
./publish-android-github-essential.sh
```

This script:
1. Copies AAR to release directory
2. Creates GitHub release
3. Uploads AAR and documentation
4. Tags the release

**GitHub Repository:** https://github.com/artius-iD/artiusid_sdk_android

---

## 📚 Documentation Structure

### **Internal Documentation** (For Developers)
- `DEVELOPER_README.md` (this file) - Development guide
- `artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/` - Technical docs
- `HILT_INTEGRATION_GUIDE.md` - HILT setup (also for clients)
- `SDK_DEPENDENCY_REQUIREMENTS.md` - Dependencies (also for clients)

### **Client-Facing Documentation** (For External Clients)
Located in `docs/client/`:
- `README.md` - Client integration guide
- `CLIENT_IMPLEMENTATION_GUIDE.md` - Detailed implementation steps
- `RELEASE_NOTES_vX.X.XX.md` - Version-specific release notes
- `DEPLOYMENT_SUMMARY_vX.X.XX.md` - Deployment information

### **Sample App Documentation**
- `sample-app/LOCALIZATION_GUIDE.md` - String customization
- `sample-app/src/main/assets/README.md` - Asset override guide

---

## 🔒 Security & Obfuscation

### **ProGuard/R8 Configuration**

SDK uses ProGuard for:
- Code obfuscation (IP protection)
- Code shrinking (size optimization)
- Resource optimization

**Configuration Files:**
- `artiusid-sdk/proguard-rules.pro` - SDK ProGuard rules
- `artiusid-sdk/consumer-rules.pro` - Rules for client apps
- `sample-app/proguard-rules-obfuscated.pro` - Sample app obfuscation

**Mapping Files:**
After release build, mapping files are generated:
- `artiusid-sdk/build/outputs/mapping/release/mapping.txt`

**⚠️ Important:** Store mapping files for each release to decode stack traces from production crashes.

### **Sensitive Data**

Files containing sensitive data:
- `google-services.json` - Firebase configuration
- `local.properties` - Local SDK paths
- Mapping files - For de-obfuscating crashes

**Note:** `google-services.json` is tracked for internal development but should be excluded from client distributions.

---

## 🧪 Testing

### **Manual Testing Checklist**

#### **Verification Flow**
- [ ] Face liveness detection works
- [ ] Passport scanning with NFC works
- [ ] State ID front/back scanning works
- [ ] Verification submission succeeds
- [ ] Member ID is saved and displayed

#### **Authentication Flow**
- [ ] Face authentication works
- [ ] Authentication submission succeeds
- [ ] Approval UI shows correct data

#### **Approval Requests**
- [ ] Send approval request succeeds
- [ ] Notification is received (sound + vibration)
- [ ] Notification opens app correctly
- [ ] Approval/decline actions work

#### **Environment Testing**
- [ ] Switch between Sandbox/Development/Staging
- [ ] Credentials isolated per environment
- [ ] UI updates correctly on environment change
- [ ] Certificates registered per environment

#### **Firebase Testing**
- [ ] FCM token generated on first launch
- [ ] FCM token provided to SDK
- [ ] Token persists across app restarts
- [ ] Token updates on token refresh

---

## 🐛 Debugging

### **Enable SDK Logging**

```kotlin
val sdkConfig = SDKConfiguration(
    // ...
    enableLogging = true  // Enable verbose logging
)
```

### **Common Log Tags**

- `ArtiusIDSDK` - Main SDK operations
- `VerificationProcessingVM` - Verification flow
- `CertificateManager` - mTLS certificates
- `FirebaseTokenManager` - FCM tokens
- `EnvironmentCredentialManager` - Credential storage

### **ADB Commands**

```bash
# View logs
adb logcat | grep -E "ArtiusIDSDK|VerificationProcessingVM"

# Clear app data
adb shell pm clear com.artiusid.sampleapp

# Uninstall app
adb uninstall com.artiusid.sampleapp

# Install APK
adb install sample-app/build/outputs/apk/debug/sample-app-debug.apk
```

---

## 🤝 Team Collaboration

### **GitLab Repository**
**Source Repository:** git@gitlab.com:artiusid1/mobile-sdk-android.git

### **GitHub Repository** (Public Releases)
**Public Repository:** https://github.com/artius-iD/artiusid_sdk_android

### **Branch Strategy**
- `main` - Stable, production-ready code
- `develop` - Active development
- `feature/*` - Feature branches
- `hotfix/*` - Critical fixes

### **Commit Message Format**
```
[TYPE] Short description

Detailed description if needed

Type: feat, fix, docs, refactor, test, chore
```

Example:
```
[feat] Add environment-specific credential storage

Implements per-environment storage for verification credentials,
FCM tokens, and certificates. Adds auto-detection logic.
```

---

## 📞 Support & Resources

### **Internal Team**
- **Technical Lead:** Todd Bryant
- **Repository:** git@gitlab.com:artiusid1/mobile-sdk-android.git
- **Documentation:** This file and `docs/` directory

### **External Resources**
- **Kotlin Documentation:** https://kotlinlang.org/docs/home.html
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Hilt:** https://dagger.dev/hilt/
- **Firebase:** https://firebase.google.com/docs/android/setup

---

## 🎯 Development Tips

### **IDE Configuration**
- Enable auto-import for Kotlin
- Set line length to 120 characters
- Use Kotlin coding conventions
- Enable format on save

### **Performance Tips**
- Use `@Composable` functions sparingly
- Avoid recomposition triggers in hot paths
- Use `remember` and `LaunchedEffect` correctly
- Profile with Android Studio Profiler

### **Code Style**
- Follow Kotlin conventions
- Use descriptive variable names
- Document public APIs with KDoc
- Keep functions small and focused

---

## 📝 Frequently Asked Questions

### **Q: How do I test on a physical device?**
A: Enable USB debugging on your device, connect via USB, and select your device in Android Studio's run configuration.

### **Q: The sample app crashes on startup. What do I do?**
A: Check that `google-services.json` exists in `sample-app/`. Clear app data: `adb shell pm clear com.artiusid.sampleapp`

### **Q: How do I switch environments in the sample app?**
A: Use the environment dropdown in the main screen. Note: Switching clears credentials from the previous environment.

### **Q: Where are credentials stored?**
A: Credentials are stored in Android's EncryptedSharedPreferences, with separate storage per environment.

### **Q: How do I regenerate a certificate?**
A: Clear app data or use the "Clear Certificate" button in the sample app, then restart verification.

### **Q: The AAR file is 25MB. Why so large?**
A: The SDK includes ML models for face detection/recognition (~15MB), plus all SDK code and resources.

### **Q: How do I add a new feature?**
A: 
1. Create feature branch: `git checkout -b feature/my-feature`
2. Implement feature in `artiusid-sdk/`
3. Test using `sample-app/`
4. Update documentation
5. Create merge request to `develop`

---

## 🎓 Learning Resources

### **For New Android Developers**
1. [Android Basics](https://developer.android.com/courses/android-basics-compose/course)
2. [Kotlin for Android](https://developer.android.com/kotlin)
3. [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

### **For SDK Development**
1. [Creating Android Libraries](https://developer.android.com/studio/projects/android-library)
2. [ProGuard and R8](https://developer.android.com/studio/build/shrink-code)
3. [Publishing Libraries](https://developer.android.com/studio/build/maven-publish-plugin)

---

## 📅 Version History

| Version | Date | Major Changes |
|---------|------|---------------|
| **1.2.48** | Oct 29, 2025 | Firebase architecture overhaul, environment-specific credentials |
| **1.2.43** | Oct 23, 2025 | Optional Firebase handling, FCM token pass-through |
| **1.2.41** | Oct 23, 2025 | Verification fixes, iOS-compliant approval API |
| **1.2.38** | Oct 23, 2025 | EncryptedSharedPreferences corruption fix |
| **1.2.37** | Oct 23, 2025 | Configurable client ID |

See `docs/client/RELEASE_NOTES_vX.X.XX.md` files for detailed changelogs.

---

## 🚀 Next Steps

1. **Set up your development environment** (see Quick Start above)
2. **Run the sample app** to understand the SDK flows
3. **Read the architecture documentation** in `artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/`
4. **Make a small change** to get familiar with the codebase
5. **Review recent commits** to see what's been worked on

---

**Welcome to the ArtiusID SDK team! 🎉**

For questions or issues, reach out to the team or check the GitLab issues page.

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

