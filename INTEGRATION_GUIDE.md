# ArtiusID Android SDK Integration Guide

## Quick Start

### 1. Add AAR to Your Project

1. Download `artiusid-sdk-1.2.7.aar` from the releases page
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
    implementation files('libs/artiusid-sdk-1.2.7.aar')
    
    // Required dependencies
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"  // ✅ Use KSP, not kapt
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
import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp  // ✅ Required for HILT
class YourApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (required for SDK)
        FirebaseApp.initializeApp(this)
    }
    
    // Required for SDK animations and GIFs
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .okHttpClient {
                OkHttpClient.Builder().build()
            }
            .build()
    }
}
```

### 4. Initialize SDK

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // ✅ Required for HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK with dynamic branding
        val config = SDKConfiguration(
            apiKey = "your-api-key",
            environment = Environment.PRODUCTION,
            enableLogging = BuildConfig.DEBUG
        )
        
        val theme = SDKThemeConfiguration(
            brandName = "YourBrand",  // ✅ Your custom branding
            primaryColorHex = "#YOUR_PRIMARY_COLOR",
            secondaryColorHex = "#YOUR_SECONDARY_COLOR"
        )
            
        ArtiusIDSDK.initialize(this, config, theme)
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

## 🔧 HILT Setup Tools

### Automated Setup (Recommended)
```bash
# Download and run the automated setup script
./setup_hilt.sh
```

### Diagnostic Tool
```bash
# Check your HILT configuration
./gradlew diagnoseHilt
```

### Manual Setup
Follow the step-by-step guide in `HILT_INTEGRATION_GUIDE.md`

## 🎨 Dynamic Branding

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

## Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Kotlin**: 1.9.0+
- **HILT**: 2.48 (exact version required)
- **Gradle**: 8.0+
- **Firebase Project**: Required for authentication and messaging

## ProGuard Configuration

The SDK includes consumer ProGuard rules that are automatically applied to your app. No additional configuration needed.

## Troubleshooting

### HILT Issues
1. Run `./gradlew diagnoseHilt` for automated diagnosis
2. Check `HILT_INTEGRATION_GUIDE.md` for detailed setup
3. Ensure exact HILT version 2.48 is used

### Branding Issues
- Verify `SDKThemeConfiguration.brandName` is set
- Check that `@AndroidEntryPoint` is on your Activity
- Ensure Firebase is properly initialized

## Support

For technical support, please contact: support@artiusid.com
