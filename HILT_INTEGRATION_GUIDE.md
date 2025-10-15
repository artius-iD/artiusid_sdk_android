# HILT Integration Guide for ArtiusID SDK

This guide provides step-by-step instructions for properly configuring HILT dependency injection when using the ArtiusID SDK in your Android application.

## 🚨 Common HILT Issues & Solutions

### Issue 1: "Hilt Android App class not found"
**Problem**: Host app doesn't have `@HiltAndroidApp` annotation
**Solution**: Add the annotation to your Application class

### Issue 2: "Cannot find symbol: HiltViewModel"
**Problem**: Missing HILT compiler dependencies
**Solution**: Add proper HILT dependencies and KSP configuration

### Issue 3: "Duplicate class found in modules"
**Problem**: Version conflicts between SDK and host app HILT versions
**Solution**: Use compatible versions (see version matrix below)

### Issue 4: "Entry point not found"
**Problem**: SDK activities not properly configured with `@AndroidEntryPoint`
**Solution**: Ensure proper HILT setup in host app

## 📋 Required Configuration

### 1. Project-level `build.gradle`

```gradle
buildscript {
    ext {
        hilt_version = '2.48'
        compose_compiler_version = '1.5.4'
    }
    dependencies {
        classpath "com.google.dagger:hilt-android-gradle-plugin:$hilt_version"
    }
}
```

### 2. App-level `build.gradle`

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'  // ✅ Required
    id 'com.google.devtools.ksp'         // ✅ Required for HILT compiler
    id 'com.google.gms.google-services'  // ✅ Required for Firebase
}

android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24  // ✅ SDK minimum requirement
        targetSdk 34
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        compose true  // ✅ Required for SDK UI
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion compose_compiler_version
    }
}

dependencies {
    // ✅ ArtiusID SDK
    implementation project(':artiusid-sdk')
    // OR if using published version:
    // implementation 'com.artiusid:sdk:1.0.0'
    
    // ✅ Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.2'
    
    // ✅ Compose BOM (manages all Compose library versions)
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    // ✅ HILT - CRITICAL: Must match SDK version (2.48)
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // ✅ Image Loading - Required for SDK animations
    implementation 'io.coil-kt:coil-compose:2.5.0'
    implementation 'io.coil-kt:coil-gif:2.5.0'
    implementation 'io.coil-kt:coil-base:2.5.0'
    
    // ✅ Networking - Required for Coil
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // ✅ Firebase - Required for FCM functionality
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    
    // ✅ Biometric - Required for authentication
    implementation 'androidx.biometric:biometric:1.1.0'
}
```

### 3. Application Class Setup

Create or update your Application class:

```kotlin
package com.yourcompany.yourapp

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.util.DebugLogger
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

/**
 * Application class with HILT support for ArtiusID SDK
 */
@HiltAndroidApp  // ✅ CRITICAL: This annotation is required
class YourApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("YourApplication", "🚀 Starting application initialization...")
        
        // ✅ Initialize Firebase (required for SDK FCM functionality)
        try {
            FirebaseApp.initializeApp(this)
            Log.d("YourApplication", "🔥 Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("YourApplication", "❌ Firebase initialization failed", e)
        }
        
        Log.d("YourApplication", "✅ Application onCreate completed")
    }
    
    // ✅ Required for SDK image loading (GIFs, animations)
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .build()
            }
            .logger(DebugLogger())
            .build()
    }
}
```

### 4. AndroidManifest.xml Configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- ✅ Required permissions for SDK -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.USE_FINGERPRINT" />

    <application
        android:name=".YourApplication"  <!-- ✅ Reference your @HiltAndroidApp class -->
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.YourApp">
        
        <!-- ✅ Your main activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.YourApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- ✅ Firebase Messaging Service -->
        <service
            android:name="com.artiusid.sdk.services.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
    </application>
</manifest>
```

### 5. MainActivity Setup

```kotlin
package com.yourcompany.yourapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.SDKError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // ✅ CRITICAL: Required for HILT injection
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ Initialize ArtiusID SDK
        initializeSDK()
        
        setContent {
            YourAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your app content
                    MainScreen()
                }
            }
        }
    }
    
    private fun initializeSDK() {
        try {
            val configuration = SDKConfiguration(
                apiKey = "your-api-key",
                baseUrl = "https://api.artiusid.com",
                environment = Environment.PRODUCTION,
                enableLogging = BuildConfig.DEBUG,
                hostAppPackageName = packageName
            )
            
            val theme = SDKThemeConfiguration(
                brandName = "YourBrand",  // ✅ Your custom branding
                primaryColorHex = "#YOUR_PRIMARY_COLOR",
                secondaryColorHex = "#YOUR_SECONDARY_COLOR"
            )
            
            ArtiusIDSDK.initialize(this, configuration, theme)
            
        } catch (e: Exception) {
            // Handle initialization error
            android.util.Log.e("MainActivity", "SDK initialization failed", e)
        }
    }
}
```

## 🔧 Version Compatibility Matrix

| SDK Version | HILT Version | Compose BOM | Min SDK | Target SDK |
|-------------|--------------|-------------|---------|------------|
| 1.0.x       | 2.48         | 2023.10.01  | 24      | 34         |

## 🚨 ProGuard Configuration

If you're using ProGuard/R8, add these rules to your `proguard-rules.pro`:

```proguard
# ✅ Keep HILT components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ✅ Keep SDK classes (automatically included via consumer-rules.pro)
-keep class com.artiusid.sdk.** { *; }

# ✅ Keep Compose
-keep class androidx.compose.** { *; }

# ✅ Keep Firebase
-keep class com.google.firebase.** { *; }
```

## 🐛 Troubleshooting

### Build Error: "Hilt processor not found"
**Solution**: Ensure KSP is properly configured:
```gradle
plugins {
    id 'com.google.devtools.ksp'  // Must be present
}
```

### Runtime Error: "No injector factory found"
**Solution**: Verify `@HiltAndroidApp` is on your Application class and referenced in AndroidManifest.xml

### Compilation Error: "Duplicate class"
**Solution**: Check for version conflicts in dependencies. Use the exact versions specified in this guide.

### Runtime Error: "Cannot create ViewModel"
**Solution**: Ensure your Activity/Fragment has `@AndroidEntryPoint` annotation

## 📞 Support

If you continue to experience HILT integration issues:

1. Verify all versions match the compatibility matrix
2. Clean and rebuild your project
3. Check that all required annotations are present
4. Ensure Firebase is properly initialized
5. Verify ProGuard rules if using code obfuscation

## 📝 Example Integration

For a complete working example, see the `sample-app` module in the SDK repository, which demonstrates proper HILT configuration with the ArtiusID SDK.
