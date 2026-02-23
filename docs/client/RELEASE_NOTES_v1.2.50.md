# ArtiusID Android SDK v1.2.50 - Release Notes

**Release Date:** February 2026  
**Author:** artius.iD, Inc.

---

## ✨ NEW FEATURES

### **1. iOS Parity – VerificationResult & Document Recapture**
- **VerificationResult** now includes `requiresRecapture`, `recaptureType`, and full iOS-style fields (`accountNumber`, `fullName`, `verificationScore`, `documentStatus`, `faceMatchScore`, etc.)
- **DocumentRecaptureType** – `fromHttpErrorCode()` returns nullable; 605 = permanent failure (not recapture)
- When API returns recapture (600–604), SDK returns `VerificationResult(requiresRecapture=true, recaptureType=...)` to host; host can call `startVerification()` again (iOS parity)
- Recapture-able errors 600, 602, 603, 604 set appropriate recapture state; 605 remains permanent failure

### **2. Sample App – Test Authentication Request**
- New “Test Authentication Request” button and screen (matches iOS SampleAppView)
- Local Approve/Deny/Cancel UI for testing auth request flow
- Strings added for en, de, es, fr

### **3. Sample App – Okta.plist & AppConstants Config**
- **Okta:** Optional `okta.json` in `sample-app/src/main/assets/` overrides Okta config (issuer, clientId, redirectUri, scopes) – same values as MFA iOS app Okta.plist
- **AppConstants:** New `AppConstants.kt` and optional `appconstants.json` for apiKey, clientId, clientGroupId, app name, feature flags – matches iOS AppConstants
- Example files: `okta.json.example`, `appconstants.json.example`; copy to `okta.json` / `appconstants.json` and fill in. Loaded in `SampleApplication.onCreate()`
- SDK initialization uses `AppConstants.apiKey`, `AppConstants.clientId`, `AppConstants.clientGroupId`

---

## 🔧 TECHNICAL IMPROVEMENTS

- **StandaloneAppActivity** – Builds full `VerificationResult` from `VerificationResultData` (iOS parity); handles recapture result from flow
- **AppNavigation** – `onCompleteWithRecapture` callback returns recapture result to host
- **VerificationProcessingScreen** – LaunchedEffect invokes `onCompleteWithRecapture` when state is RecaptureRequired
- **.gitignore** – `sample-app/src/main/assets/okta.json` and `appconstants.json` ignored so secrets aren’t committed
- **Gradle** – `org.gradle.java.home` for Android Studio JDK (optional)

---

## 📋 FILES CHANGED

**SDK:** `SDKModels.kt`, `DocumentRecaptureType.kt`, `StandaloneAppActivity.kt`, `AppNavigation.kt`, `VerificationProcessingScreen.kt`, `VerificationProcessingViewModel.kt`

**Sample App:** `BridgeMainActivity.kt`, `SampleApplication.kt`, `OktaConfig.kt`, `AppConstants.kt` (new), `okta.json.example`, `appconstants.json.example`, strings (en/de/es/fr), README

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher

---

## 📦 WHAT'S INCLUDED

- `artiusid-sdk-1.2.50.aar` – Production SDK library
- `RELEASE_NOTES_v1.2.50.md` – This file
- Sample app with Test Authentication Request and Okta/AppConstants config from assets

---

## 🚀 DEPLOYMENT

Run the automated publish script (or equivalent) to build and publish the AAR for v1.2.50.
