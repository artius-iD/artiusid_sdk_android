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

## Installation

### Gradle
Add the AAR file to your project:

1. Download `artiusid-sdk-1.0.2.aar` from the releases page
2. Copy it to your app's `libs` directory
3. Add to your app's `build.gradle`:

```gradle
dependencies {
    implementation files('libs/artiusid-sdk-1.0.2.aar')
}
```

## Version
Current version: **1.0.2**

## Documentation
- [Integration Guide](INTEGRATION_GUIDE.md)
- [Security Guide](docs/SDK_DISTRIBUTION_SECURITY.md)
- [Image Override System](docs/Image_Override_System_Documentation.md)

## Changelog
### v1.0.2
- Latest release with enhanced security and performance improvements
- Fully obfuscated AAR for IP protection
- Comprehensive documentation and integration guides

