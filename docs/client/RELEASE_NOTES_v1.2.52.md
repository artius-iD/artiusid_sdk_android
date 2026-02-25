# ArtiusID Android SDK v1.2.52 - Release Notes

**Release Date:** February 2026  
**Author:** artius.iD, Inc.

---

## 🔧 Host app integration fixes

This patch addresses issues reported by host apps (e.g. Okta MFA) integrating the SDK AAR with their own Compose/BOM versions.

### **1. Compose version alignment (documentation)**

- **Documented** the exact Compose BOM (`compose-bom:2023.10.01`) and Compose compiler (`1.5.3`) used to build the SDK.
- **README**, **SDK_DEPENDENCY_REQUIREMENTS.md**, and **RELEASE_NOTES** now instruct host apps to use the same BOM and compiler to avoid runtime `NoSuchMethodError` (e.g. `performImeAction$default` in `SemanticsPropertiesKt` when SDK screens with `TextField` are composed).
- **Troubleshooting** sections added with the error message and fix (match BOM/compiler).

### **2. NFC chip scan – 3 failures then proceed**

- After **three** failed NFC read attempts, the flow now proceeds directly to the next step (verification) **without** showing the “Scan failed” error screen.
- Completion callback `onChipScanComplete(null)` is invoked immediately; OCR/passport data is preserved in `DocumentDataHolder` when available.
- Applied consistently on both code paths: **IsoDep** (StandaloneAppActivity) and **tag-based** NFC.
- Avoids composing the error screen when at max failures, reducing risk of Compose semantics crashes in host apps.

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher

---

## 🎨 Compose build compatibility (host app alignment)

The SDK AAR is built with the following Compose versions. **Host apps must use the same BOM and compiler** to avoid runtime `NoSuchMethodError`.

| Component | Version |
|-----------|---------|
| **Compose BOM** | `androidx.compose:compose-bom:2023.10.01` |
| **Compose compiler** | `1.5.3` (Kotlin 1.9.x) |

In your app's `build.gradle`:

```gradle
android {
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
}
dependencies {
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.foundation:foundation'
    implementation 'androidx.compose.material3:material3'
    // ... other Compose deps as needed
}
```

---

## 📦 WHAT'S INCLUDED

- `artiusid-sdk-1.2.52.aar` – Production SDK library
- `RELEASE_NOTES_v1.2.52.md` – This file

---

## 🚀 DEPLOYMENT

Run the automated publish script (or equivalent) to build and publish the AAR for v1.2.52.
