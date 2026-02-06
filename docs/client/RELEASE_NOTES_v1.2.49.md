# ArtiusID Android SDK v1.2.49 - Release Notes

**Release Date:** February 6, 2026  
**Author:** artius.iD, Inc.

---

## ✨ NEW FEATURES

### **1. iOS Parity – mTLS Clear on Environment Switch**
- SDK now clears TLS/certificate state at the start of `initialize()` and `initializeWithEnhancedTheme()`
- Switching environments (e.g., Sandbox → Staging) correctly loads the new certificate
- Matches iOS SDK behavior

### **2. Pre-set Okta User ID**
- New optional `oktaUserId` in `SDKConfiguration` – client can pass Okta user ID at init
- New `ArtiusIDSDK.setOktaUserId(userId)` and `ArtiusIDSDK.getOktaUserId()` – runtime setter/getter (per environment)
- When Okta user ID is pre-set, the CollectOktaID screen is skipped and the value is used in the verification payload
- Matches iOS `ArtiusIDSDKWrapper.configure(oktaUserId:)` and `setOktaUserId()` / `getOktaUserId()`

### **3. Re-verification (accountNumber in VerificationRequest)**
- `VerificationRequest` now includes optional `accountNumber` (member ID from previous verification)
- SDK populates it from `VerificationStateManager` for the current environment
- Enables re-verification flow for existing users (iOS v2.0.17 parity)

### **4. NFC Reset and Retry Guard**
- New `NfcStateManager` with `tryAcquire()` / `release()` / `resetNFCState()`
- Prevents concurrent NFC attempts and stuck state
- NFC state reset when entering PassportChipScan, on back, and on verification completion/failure
- Matches iOS NFC reset behavior (v2.0.43, v2.0.19)

### **5. Okta Login in Sample App**
- Sample app now supports Okta OIDC browser sign-in (matches iOS Okta MFA app)
- "Login with Okta" button in Okta ID Configuration card
- Guidance dialog → browser sign-in → extract Okta user ID from id_token → `ArtiusIDSDK.setOktaUserId()`
- Optional: Okta provisioning offered after verification success when no Okta user ID is set
- Configuration in `sample-app/.../config/OktaConfig.kt` (issuer, clientId, redirectUri, scopes)

---

## 🐛 BUG FIXES

- **Duplicate `onNewIntent` override** – Merged notification and Okta redirect handling into a single override

---

## 🔧 TECHNICAL IMPROVEMENTS

- **SharedContextManager** – New `clearStaticState()` for TLS/certificate reset on environment switch
- **VerificationProcessingViewModel** – Okta ID from `getOktaUserId()` or `OktaIDHolder`; `accountNumber` from `VerificationStateManager`
- **AppNavigation** – Skip CollectOktaID when `getOktaUserId()` is set; NFC reset on PassportChipScan entry/exit
- **Okta OIDC** – PKCE flow, Chrome Custom Tabs, redirect URI `com.artiusid.sampleapp:/callback`

---

## 📋 FILES CHANGED

**SDK:** `SharedContextManager.kt`, `ArtiusIDSDK.kt`, `SDKConfiguration.kt`, `VerificationRequest.kt`, `VerificationProcessingViewModel.kt`, `AppNavigation.kt`, `VerificationStepsScreen.kt`, `NfcStateManager.kt` (new), `NfcReadingViewModel.kt`, `VerificationProcessingScreen.kt`

**Sample App:** `OktaConfig.kt` (new), `OktaLoginHelper.kt` (new), `BridgeMainActivity.kt`, `build.gradle`, `AndroidManifest.xml`

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher

---

## 📦 WHAT'S INCLUDED

- `artiusid-sdk-1.2.49.aar` – Production SDK library
- `RELEASE_NOTES_v1.2.49.md` – This file
- Sample app with Okta login flow

---

## 🚀 DEPLOYMENT

Run the automated publish script:

```bash
./artiusid-sdk/scripts/publish-android-github.sh
```

Or with a specific version:

```bash
./artiusid-sdk/scripts/publish-android-github.sh 1.2.50
```

---

**End of Release Notes**
