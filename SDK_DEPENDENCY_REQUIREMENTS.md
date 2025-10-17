# ArtiusID SDK - Complete Dependency Requirements

**SDK Version:** v1.2.11+  
**Last Updated:** October 17, 2025  
**Critical:** All host applications MUST include these dependencies

---

## 🚨 **CRITICAL: Why This Matters**

The ArtiusID SDK uses `implementation` (not `api`) for its dependencies. This means:

❌ **Dependencies are NOT automatically available to the host app**  
✅ **Host app MUST explicitly declare ALL dependencies**  
⚠️ **Missing dependencies = Runtime crashes**

**Common Error if missing:**
```
NoClassDefFoundError: androidx.camera.core.CameraX
NoClassDefFoundError: com.google.mlkit.vision.face.FaceDetector
```

---

## 📋 **Complete Dependency List**

Copy this entire block into your `app/build.gradle`:

```gradle
dependencies {
    // ═══════════════════════════════════════════════════════════
    // ARTIUSID SDK
    // ═══════════════════════════════════════════════════════════
    implementation files('libs/artiusid-sdk-1.2.11.aar')
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Core Android Libraries
    // ═══════════════════════════════════════════════════════════
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.2'
    implementation 'androidx.biometric:biometric:1.1.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Jetpack Compose (UI Framework)
    // ═══════════════════════════════════════════════════════════
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material3:material3-window-size-class'
    implementation 'androidx.compose.material:material-icons-extended'
    implementation 'androidx.compose.material:material'
    implementation 'androidx.compose.runtime:runtime'
    implementation 'androidx.compose.runtime:runtime-livedata'
    implementation 'androidx.compose.foundation:foundation'
    implementation 'androidx.compose.animation:animation'
    implementation 'androidx.compose.animation:animation-core'
    implementation 'androidx.navigation:navigation-compose:2.7.6'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: CameraX (Document/Face/NFC Scanning)
    // ═══════════════════════════════════════════════════════════
    def camerax_version = "1.3.1"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-camera2:${camerax_version}"
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    implementation "androidx.camera:camera-view:${camerax_version}"
    implementation "androidx.camera:camera-extensions:${camerax_version}"
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Google ML Kit (Face Detection, OCR, Barcode)
    // ═══════════════════════════════════════════════════════════
    implementation 'com.google.mlkit:face-detection:16.1.5'
    implementation 'com.google.mlkit:text-recognition:16.0.0'
    implementation 'com.google.mlkit:barcode-scanning:17.2.0'
    implementation 'com.google.mlkit:object-detection:17.0.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Lifecycle Components
    // ═══════════════════════════════════════════════════════════
    def lifecycle_version = "2.7.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycle_version}"
    implementation "androidx.lifecycle:lifecycle-runtime-compose:${lifecycle_version}"
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Networking (API Communication)
    // ═══════════════════════════════════════════════════════════
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Data Storage
    // ═══════════════════════════════════════════════════════════
    implementation 'androidx.datastore:datastore-preferences:1.0.0'
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Coroutines (Async Operations)
    // ═══════════════════════════════════════════════════════════
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Serialization
    // ═══════════════════════════════════════════════════════════
    implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Image Processing
    // ═══════════════════════════════════════════════════════════
    implementation 'androidx.exifinterface:exifinterface:1.3.7'
    implementation 'io.coil-kt:coil-compose:2.4.0'
    implementation 'io.coil-kt:coil-gif:2.4.0'
    implementation 'io.coil-kt:coil-base:2.4.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Work Manager (Background Tasks)
    // ═══════════════════════════════════════════════════════════
    implementation 'androidx.work:work-runtime-ktx:2.9.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Barcode Scanning (Driver's License, QR Codes)
    // ═══════════════════════════════════════════════════════════
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Permissions Handling
    // ═══════════════════════════════════════════════════════════
    implementation 'com.google.accompanist:accompanist-permissions:0.32.0'
    implementation 'com.google.accompanist:accompanist-systemuicontroller:0.32.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Hilt Dependency Injection
    // ═══════════════════════════════════════════════════════════
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Koin Dependency Injection
    // ═══════════════════════════════════════════════════════════
    def koin_version = "3.5.0"
    implementation "io.insert-koin:koin-android:${koin_version}"
    implementation "io.insert-koin:koin-androidx-compose:${koin_version}"
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Firebase (Authentication, Analytics, Messaging)
    // ═══════════════════════════════════════════════════════════
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Passport NFC Reading
    // ═══════════════════════════════════════════════════════════
    implementation 'org.jmrtd:jmrtd:0.7.34'
    implementation 'net.sf.scuba:scuba-sc-android:0.0.23'
    implementation 'edu.ucar:jj2000:5.2'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Biometric Processing (Fingerprints)
    // ═══════════════════════════════════════════════════════════
    implementation 'com.github.mhshams:jnbis:1.1.0'
    
    // ═══════════════════════════════════════════════════════════
    // REQUIRED: Cryptography (Passport Security, TLS)
    // ═══════════════════════════════════════════════════════════
    implementation 'com.madgag.spongycastle:core:1.58.0.0'
    implementation 'com.madgag.spongycastle:prov:1.58.0.0'
}
```

---

## 🔧 **Required Gradle Configuration**

### **1. Add to `android {}` block:**

```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
    
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}
```

### **2. Add to `plugins {}` block:**

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
    id 'kotlinx-serialization'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'  // For Firebase
}
```

### **3. Add to project-level `build.gradle`:**

```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
        classpath 'com.google.dagger:hilt-android-gradle-plugin:2.48'
    }
}

plugins {
    id 'com.google.devtools.ksp' version '1.9.10-1.0.13' apply false
}
```

---

## 📊 **Dependency Categories Explained**

### **Category 1: Camera & Scanning (CRITICAL)**

**Why needed:** Document scanning, face detection, NFC passport reading

```gradle
// CameraX - ALL 5 libraries required
androidx.camera:camera-core:1.3.1
androidx.camera:camera-camera2:1.3.1
androidx.camera:camera-lifecycle:1.3.1
androidx.camera:camera-view:1.3.1
androidx.camera:camera-extensions:1.3.1
```

**If missing:**
```
❌ Document scan crashes
❌ Face scan crashes
❌ Camera won't initialize
Error: NoClassDefFoundError: androidx.camera.core.CameraX
```

---

### **Category 2: ML Kit (CRITICAL)**

**Why needed:** Face liveness detection, OCR (text extraction), barcode/QR scanning

```gradle
// ML Kit - ALL 4 libraries required
com.google.mlkit:face-detection:16.1.5       // Face liveness
com.google.mlkit:text-recognition:16.0.0     // OCR/MRZ reading
com.google.mlkit:barcode-scanning:17.2.0     // QR codes, PDF417
com.google.mlkit:object-detection:17.0.0     // Document detection
```

**If missing:**
```
❌ Face scan fails
❌ Document text extraction fails
❌ License barcode scanning fails
Error: NoClassDefFoundError: com.google.mlkit.vision.face.FaceDetector
```

---

### **Category 3: Compose UI (CRITICAL)**

**Why needed:** The SDK is built with Jetpack Compose

```gradle
// Compose BOM and libraries
implementation platform('androidx.compose:compose-bom:2023.10.01')
// + 12 compose libraries
```

**If missing:**
```
❌ SDK screens won't display
❌ Crashes on activity start
Error: NoClassDefFoundError: androidx.compose.runtime.Composer
```

---

### **Category 4: Networking (CRITICAL)**

**Why needed:** API communication with verification backend

```gradle
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0
```

**If missing:**
```
❌ Verification requests fail
❌ Cannot submit documents
Error: NoClassDefFoundError: retrofit2.Retrofit
```

---

### **Category 5: Firebase (CRITICAL)**

**Why needed:** Authentication, analytics, push notifications

```gradle
implementation platform('com.google.firebase:firebase-bom:32.7.2')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-analytics'
implementation 'com.google.firebase:firebase-messaging:23.4.1'
```

**If missing:**
```
❌ Authentication fails
❌ Analytics not reported
❌ Push notifications don't work
Error: NoClassDefFoundError: com.google.firebase.auth.FirebaseAuth
```

---

### **Category 6: Hilt DI (CRITICAL)**

**Why needed:** Dependency injection framework

```gradle
com.google.dagger:hilt-android:2.48
ksp com.google.dagger:hilt-android-compiler:2.48
androidx.hilt:hilt-navigation-compose:1.1.0
```

**If missing:**
```
❌ SDK won't initialize
❌ ViewModels won't inject
Error: Could not find class 'dagger.hilt.android.HiltAndroidApp'
```

**See:** `HILT_INTEGRATION_GUIDE.md` for complete setup

---

### **Category 7: NFC/Passport (Required for Passport Scanning)**

**Why needed:** Reading passport chips via NFC

```gradle
org.jmrtd:jmrtd:0.7.34
net.sf.scuba:scuba-sc-android:0.0.23
edu.ucar:jj2000:5.2
com.madgag.spongycastle:core:1.58.0.0
com.madgag.spongycastle:prov:1.58.0.0
```

**If missing:**
```
❌ Passport NFC reading fails
❌ Chip data cannot be read
Error: NoClassDefFoundError: org.jmrtd.lds.icao.MRZInfo
```

---

### **Category 8: Image Processing**

**Why needed:** Image loading, manipulation, compression

```gradle
io.coil-kt:coil-compose:2.4.0
io.coil-kt:coil-gif:2.4.0
io.coil-kt:coil-base:2.4.0
androidx.exifinterface:exifinterface:1.3.7
```

**If missing:**
```
❌ Images won't load
❌ Logos won't display
Error: NoClassDefFoundError: coil.compose.AsyncImage
```

---

### **Category 9: Barcode Scanning**

**Why needed:** Driver's license PDF417 barcodes, QR codes

```gradle
com.google.zxing:core:3.5.2
com.journeyapps:zxing-android-embedded:4.3.0
```

**If missing:**
```
❌ License barcode scanning fails
❌ QR code scanning fails
Error: NoClassDefFoundError: com.google.zxing.BarcodeFormat
```

---

## ⚠️ **Common Integration Errors**

### **Error 1: NoClassDefFoundError**

```
java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/camera/core/CameraX;
```

**Cause:** Missing CameraX dependency  
**Fix:** Add all 5 CameraX libraries listed above

---

### **Error 2: MethodNotFoundException**

```
java.lang.NoSuchMethodError: No virtual method process(Lcom/google/mlkit/vision/common/InputImage;)
```

**Cause:** Wrong ML Kit version  
**Fix:** Use exact versions listed (16.1.5, 16.0.0, 17.2.0, 17.0.0)

---

### **Error 3: Native Library Errors**

```
java.lang.UnsatisfiedLinkError: dlopen failed: library "libjnbis.so" not found
```

**Cause:** Gradle not packaging native libraries  
**Fix:** Ensure these lines in `android {}`:

```gradle
packaging {
    resources {
        excludes += '/META-INF/{AL2.0,LGPL2.1}'
    }
}
```

---

### **Error 4: Firebase Initialization**

```
FirebaseApp initialization unsuccessful
```

**Cause:** Missing `google-services.json`  
**Fix:**
1. Download from Firebase Console
2. Place in `app/` directory
3. Add plugin: `id 'com.google.gms.google-services'`

---

### **Error 5: Hilt Compilation**

```
error: [Hilt] Processing did not complete. See error above for details.
```

**Cause:** Incomplete Hilt setup  
**Fix:** See `HILT_INTEGRATION_GUIDE.md` for complete setup

---

## ✅ **Verification Checklist**

Use this to verify your integration:

### **Step 1: Build.gradle Dependencies**

- [ ] All 70+ dependencies added
- [ ] Versions match exactly
- [ ] No dependency version conflicts
- [ ] Firebase BOM included
- [ ] Compose BOM included

### **Step 2: Build Configuration**

- [ ] `composeOptions` configured
- [ ] `kotlinOptions` set to Java 8
- [ ] `packaging` excludes configured
- [ ] All plugins added

### **Step 3: Firebase Setup**

- [ ] `google-services.json` in `app/` directory
- [ ] `google-services` plugin applied
- [ ] Firebase dependencies added

### **Step 4: Hilt Setup**

- [ ] Hilt plugin applied
- [ ] KSP plugin applied
- [ ] Application class has `@HiltAndroidApp`
- [ ] Activities have `@AndroidEntryPoint`

### **Step 5: Build and Test**

- [ ] Clean build succeeds
- [ ] APK installs on device
- [ ] SDK initializes without errors
- [ ] Camera opens successfully
- [ ] Face scan works
- [ ] Document scan works

---

## 🚀 **Quick Start Template**

**Minimal working `app/build.gradle`:**

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
    id 'kotlinx-serialization'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.yourcompany.yourapp'
    compileSdk 34
    
    defaultConfig {
        applicationId "com.yourcompany.yourapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    // SDK
    implementation files('libs/artiusid-sdk-1.2.11.aar')
    
    // PASTE ALL DEPENDENCIES FROM ABOVE HERE
    // (See complete list in "Complete Dependency List" section)
}
```

---

## 📞 **Support**

**Having issues?**

1. **Check this guide first** - Most issues are missing dependencies
2. **Run diagnostic:** `./gradlew :app:dependencies > deps.txt`
3. **Check Hilt setup:** See `HILT_INTEGRATION_GUIDE.md`
4. **Contact support** with:
   - Full error message
   - `app/build.gradle` file
   - Output of `./gradlew :app:dependencies`

---

## 📊 **Summary**

| Category | Libraries | Critical? |
|----------|-----------|-----------|
| **Core Android** | 4 | ✅ YES |
| **Compose UI** | 13 | ✅ YES |
| **CameraX** | 5 | ✅ YES |
| **ML Kit** | 4 | ✅ YES |
| **Networking** | 4 | ✅ YES |
| **Firebase** | 4 | ✅ YES |
| **Hilt DI** | 3 | ✅ YES |
| **NFC/Passport** | 5 | ✅ YES |
| **Image Processing** | 4 | ✅ YES |
| **Barcode** | 2 | ✅ YES |
| **Others** | 20+ | ✅ YES |
| **TOTAL** | **70+** | **ALL REQUIRED** |

---

**Version:** 1.0  
**Last Updated:** October 17, 2025  
**SDK Compatibility:** v1.2.11+  
**Status:** ✅ Production Ready

