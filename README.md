# artius.iD Android SDK

A comprehensive Android SDK for identity verification, face liveness detection, document scanning, and NFC passport reading.

## Features

- 🔐 **Face Liveness Detection** - Advanced 3D face liveness with segment-based verification
- 📄 **Document Scanning** - AI-powered document capture and OCR
- 📱 **NFC Passport Reading** - Secure passport data extraction
- 🎨 **Complete Customization** - Colors, fonts, strings, logos, and branding
- 🌍 **Multi-language Support** - Built-in localization system
- ⚡ **Easy Integration** - Simple API with comprehensive callbacks

## Quick Start

### 1. Add Dependency

```gradle
dependencies {
    implementation 'com.artiusid:artiusid-sdk:1.0.0'
}
```

### 2. Initialize SDK

```kotlin
// Initialize with your configuration
val config = ArtiusSDKConfig.Builder()
    .setApiKey("your-api-key")
    .setEnvironment(Environment.PRODUCTION)
    .setBrandingConfig(brandingConfig)
    .setLocalizationConfig(localizationConfig)
    .build()

ArtiusIDSDK.initialize(this, config)
```

### 3. Start Verification

```kotlin
// Complete verification flow
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

## License

Copyright © 2024 artius.iD. All rights reserved.
