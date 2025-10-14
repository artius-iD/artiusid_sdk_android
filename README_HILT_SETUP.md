# 🔧 HILT Configuration for ArtiusID SDK

## 📋 Quick Start

The ArtiusID SDK uses HILT for dependency injection. Follow these steps to properly configure HILT in your host application:

### 1. 🚀 Automated Setup (Recommended)

Run the automated setup script:

```bash
./setup_hilt.sh
```

This script will:
- ✅ Add required HILT plugins to your `build.gradle`
- ✅ Add HILT dependencies with correct versions
- ✅ Create backup of your build files
- ✅ Run diagnostic checks

### 2. 📖 Manual Setup

If you prefer manual setup, follow the detailed guide in [`HILT_INTEGRATION_GUIDE.md`](HILT_INTEGRATION_GUIDE.md).

### 3. 🔍 Diagnostic Tool

Run the diagnostic tool to check your HILT configuration:

```bash
./gradlew diagnoseHilt
```

## 🚨 Common Issues & Quick Fixes

### Issue: "Hilt Android App class not found"

**Quick Fix:**
```kotlin
@HiltAndroidApp
class YourApplication : Application() {
    // Your app initialization
}
```

### Issue: "Cannot find symbol: HiltViewModel"

**Quick Fix:** Add to `app/build.gradle`:
```gradle
ksp "com.google.dagger:hilt-android-compiler:2.48"
```

### Issue: "Duplicate class found"

**Quick Fix:** Ensure all HILT dependencies use version `2.48`:
```gradle
def hilt_version = "2.48"
implementation "com.google.dagger:hilt-android:${hilt_version}"
ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
```

### Issue: SDK Activities Not Working

**Quick Fix:** Add to your activities:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Your activity code
}
```

## 📦 Required Dependencies

The SDK requires these dependencies in your `app/build.gradle`:

```gradle
// HILT (version 2.48 required)
def hilt_version = "2.48"
implementation "com.google.dagger:hilt-android:${hilt_version}"
ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'

// Firebase (for FCM functionality)
implementation platform('com.google.firebase:firebase-bom:32.7.2')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-messaging:23.4.1'

// Coil (for SDK animations)
implementation 'io.coil-kt:coil-compose:2.5.0'
implementation 'io.coil-kt:coil-gif:2.5.0'

// Compose (for SDK UI)
implementation platform('androidx.compose:compose-bom:2023.10.01')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
```

## 🔧 Required Plugins

Add these plugins to your `app/build.gradle`:

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'  // ✅ Required
    id 'com.google.devtools.ksp'         // ✅ Required
    id 'com.google.gms.google-services'  // ✅ Required for Firebase
}
```

## 📱 Minimum Requirements

- **Min SDK**: 24
- **Target SDK**: 34
- **Compile SDK**: 34
- **HILT Version**: 2.48
- **Kotlin**: 1.9.0+

## 🛠️ Tools Provided

1. **`setup_hilt.sh`** - Automated HILT configuration script
2. **`hilt_diagnostic_script.gradle`** - Diagnostic tool for troubleshooting
3. **`HILT_INTEGRATION_GUIDE.md`** - Comprehensive integration guide

## 📞 Getting Help

If you're still experiencing issues:

1. Run the diagnostic tool: `./gradlew diagnoseHilt`
2. Check the detailed guide: [`HILT_INTEGRATION_GUIDE.md`](HILT_INTEGRATION_GUIDE.md)
3. Verify your configuration matches the sample app
4. Ensure all version numbers match exactly

## 🎯 Key Points

- ✅ **HILT Version 2.48** is required (exact match)
- ✅ **@HiltAndroidApp** must be on your Application class
- ✅ **@AndroidEntryPoint** must be on activities using the SDK
- ✅ **KSP** must be configured for HILT compilation
- ✅ **Firebase** must be initialized for FCM functionality
- ✅ **Coil** is required for SDK animations and GIFs

## 🔄 Migration from Older Versions

If upgrading from an older SDK version:

1. Update HILT to version 2.48
2. Ensure KSP is used instead of kapt
3. Update Compose BOM to 2023.10.01
4. Run `./gradlew clean` and rebuild

---

**Need more help?** Check the complete integration guide or run the diagnostic tool for detailed analysis of your configuration.
