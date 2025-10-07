# ArtiusID Android SDK Integration Guide

## Quick Start

### 1. Add AAR to Your Project

Download the latest AAR file and add it to your project:

```gradle
dependencies {
    implementation files('libs/artiusid-sdk-1.0.2.aar')
    
    // Required dependencies
    implementation 'androidx.compose.ui:ui:1.5.8'
    implementation 'androidx.compose.material3:material3:1.1.2'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    implementation 'com.google.mlkit:face-detection:16.1.5'
    implementation 'com.google.dagger:hilt-android:2.48'
}
```

### 2. Initialize SDK

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration

val config = SDKConfiguration.Builder()
    .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
    .build()

ArtiusIDSDK.initialize(context, config)
```

### 3. Start Verification

```kotlin
ArtiusIDSDK.startVerification(
    activity = this,
    callback = object : VerificationCallback {
        override fun onSuccess(result: VerificationResult) {
            // Handle success
        }
        
        override fun onError(error: SDKError) {
            // Handle error
        }
    }
)
```

## Security Requirements

- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- ProGuard: Enabled (recommended)
- Network Security: HTTPS only

## Support

For technical support, please contact: support@artiusid.com
