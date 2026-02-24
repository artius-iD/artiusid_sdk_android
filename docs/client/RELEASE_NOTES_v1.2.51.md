# ArtiusID Android SDK v1.2.51 - Release Notes

**Release Date:** February 2026  
**Author:** artius.iD, Inc.

---

## ✨ NEW FEATURES

### **1. ApprovalRequestResult (iOS parity)**
- **sendApprovalRequest()** now returns `ApprovalRequestResult(success, message, requestId)` instead of `Triple<Boolean, String, Int?>`
- New data class `ApprovalRequestResult` in `SDKModels.kt` (Parcelable)
- Sample app updated to use `result.success`, `result.message`, `result.requestId`

### **2. getFCMToken / getCurrentFCMToken**
- **ArtiusIDSDK.getCurrentFCMToken(context)** – returns cached FCM token (iOS parity)
- **ArtiusIDSDK.getFCMToken(context)** – alias for getCurrentFCMToken

### **3. SDKConfiguration (iOS parity)**
- **requestTimeout** – request timeout in seconds (default 30)
- **enableCertificatePinning** – enable certificate pinning for API calls (default false)
- **certificatePins** – list of SHA-256 pins when pinning enabled
- **enableFaceVerification** – enable face verification flow (default true)
- **enableDocumentScanning** – enable document scanning flow (default true)
- **validate()** – returns list of validation error messages
- **isValid** – property indicating configuration is valid

### **4. Environment & LogLevel**
- **Environment.QA** – new QA environment (maps to same URLs as Development for now)
- **LogLevel** – added **NONE** and **VERBOSE** (iOS parity)
- All environment branches (Okta key, configure, VerificationProcessingViewModel) updated for QA

### **5. AuthenticationResult (iOS parity)**
- Optional **message**, **accountInfo**, **errorMessage**, **rawResponse**
- New **AuthenticationAccountInfo(accountNumber, fullName, isActive)** for `accountInfo`
- Existing call sites remain compatible (new fields have defaults)

---

## 🔧 TECHNICAL IMPROVEMENTS

- **SettingsRepository** – sendApprovalRequest() returns ApprovalRequestResult
- **ArtiusIDSDK** – FCM token getters; sendApprovalRequest return type; QA in getOktaUserIdStorageKey and configure()
- **VerificationProcessingViewModel** – QA in environment name mapping
- **SDKConfiguration** – validate() and isValid implementation

---

## 📋 FILES CHANGED

**SDK:** `SDKModels.kt`, `SDKConfiguration.kt`, `ArtiusIDSDK.kt`, `SettingsRepository.kt`, `VerificationProcessingViewModel.kt`

**Sample App:** `BridgeMainActivity.kt` (ApprovalRequestResult usage)

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher

---

## 📦 WHAT'S INCLUDED

- `artiusid-sdk-1.2.51.aar` – Production SDK library
- `RELEASE_NOTES_v1.2.51.md` – This file
- Sample app with ApprovalRequestResult integration

---

## 🚀 DEPLOYMENT

Run the automated publish script (or equivalent) to build and publish the AAR for v1.2.51.
