# ArtiusID Android SDK Integration Guide

## Quick Start

### 1. Add AAR to Your Project

1. Download `artiusid-sdk-1.0.5.aar` from the releases page
2. Copy it to your app's `libs` directory
3. Add to your app's `build.gradle`:

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
}

dependencies {
    // Add the SDK AAR
    implementation files('libs/artiusid-sdk-1.0.5.aar')
    
    // Required dependencies
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    kapt "com.google.dagger:hilt-android-compiler:${hilt_version}"
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

### 2. Add Required Plugins

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'
}
```

### 3. Application Class Setup

```kotlin
import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp
class YourApplication : Application() {
    // Your application setup
}
```

### 4. Initialize SDK

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val config = SDKConfiguration.Builder()
            .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
            .build()
            
        ArtiusIDSDK.initialize(this, config)
    }
}
```

### 5. Start Verification

```kotlin
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

ArtiusIDSDK.startVerificationFlow(
    activity = this,
    config = verificationConfig,
    callback = object : VerificationCallback {
        override fun onSuccess(result: VerificationResult) {
            // Handle successful verification
        }
        
        override fun onError(error: SDKError) {
            // Handle error
        }
        
        override fun onCancelled() {
            // Handle cancellation
        }
    }
)
```

## Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.0+
- **Firebase Project**: Required for authentication and messaging

## ProGuard Configuration

The SDK includes consumer ProGuard rules that are automatically applied to your app. No additional configuration needed.

## Support

For technical support, please contact: support@artiusid.com
