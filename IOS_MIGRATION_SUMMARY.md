# iOS to Android SDK Migration - Executive Summary

**Date:** February 6, 2026 (refreshed)  
**iOS SDK Version Reviewed:** v2.0.59 (source: `/Users/toddbryant/Documents/mobile-sdk-ios`)  
**Android SDK Current Version:** v1.2.49

---

## Quick Overview

The iOS SDK was reviewed again and compared with the current Android SDK. The detailed punch list is in `IOS_TO_ANDROID_MIGRATION_PUNCHLIST.md`. Below is a high-level summary.

---

## Already Implemented in Android ✅

These iOS features are **already present** in the Android SDK:

1. **Dynamic Client Configuration** - `clientId` and `clientGroupId` in `SDKConfiguration`
2. **Document Recapture** - Error codes 600-604 trigger recapture with `DocumentRecaptureType`
3. **Template-Based URL Configuration** - `UrlBuilder` has template system
4. **Environment-Specific mTLS Certificate Storage** - Certificates stored per environment
5. **Okta ID Integration (v2.0.12)** - CollectOktaIDScreen, OktaIDHolder, conditional payload
6. **Verification result default** - Android uses sealed Success/Failure state; no null-default-to-success bug

---

## Needs Investigation or Implementation ⚠️ / ❌

### 🔴 CRITICAL

1. **mTLS clear on initialize** - iOS calls `clearAndReloadIdentity()` at start of `configure()`. Android should call equivalent at start of `ArtiusIDSDK.initialize()` so environment switch uses the new certificate.
2. **NFC Reset State (iOS v2.0.43)** - Test Android NFC on 2nd+ attempts; add reset if needed.
3. **NFC Concurrent Retry (iOS v2.0.19)** - Test rapid retries; add thread-safe guard if needed.

### 🟠 HIGH

4. **Pre-set Okta User ID** - iOS has `oktaUserId` in configure and `setOktaUserId()`/`getOktaUserId()`; when set, CollectOktaID is skipped. Android: add config + API + skip CollectOktaID when set.
5. **Dual Authentication Flows** - Document button-triggered vs FCM-triggered; ensure API parity.

### 🟡 MEDIUM

6. **Re-verification (accountNumber in request)** - iOS sends `accountNumber` from keychain in `VerificationRequest`. Android: add field and populate from `VerificationStateManager`.

### 🟢 LOW

7. **Verification Request Payload Capture** - iOS captures last request JSON and summary; add for debugging.
8. **UI/UX and Logging** - Audit theming; add emoji prefixes and success banners.

---

## Recommended Action Plan

### Week 1 - Critical Testing
1. **Test NFC Sequential Verifications**
   - Run 3-5 passport verifications back-to-back
   - Check if NFC fails on 2nd+ attempts

2. **Test Verification Result Handling**
   - Force null verification results
   - Ensure completion screen shows failure (not success)

3. **Test Certificate Reload**
   - Clear credentials → verify → check API calls succeed
   - Switch environments → verify → check correct cert is used

### Week 2 - Implementation
4. **Fix Any Discovered Issues**
   - Add NFC state reset if needed
   - Fix verification default if needed
   - Add cert reload if needed

5. **Document Authentication Flows**
   - Create architecture doc similar to iOS `AUTHENTICATION_FLOWS_MIGRATION.md`

### Week 3 - Enhancement
6. **UI/UX Audit**
   - Review instruction screen theming
   - Ensure icon colors use theme secondary color

7. **Logging Improvements**
   - Add emoji prefixes for easy filtering
   - Add success banners

---

## Key Differences Discovered

### iOS SDK Has (Android May Not Need)
- **Okta ID Collection** - Client-specific feature, may not apply to all Android clients
- **Extensive Documentation** - iOS has 15+ markdown docs, Android has fewer

### Android SDK Has (iOS May Not Have)
- **Hilt Dependency Injection** - Android uses modern DI, iOS doesn't
- **Jetpack Compose** - Android uses declarative UI, iOS uses SwiftUI (similar paradigm)
- **Enhanced Theme Manager** - Android has `EnhancedThemeManager`, iOS has simpler theme system

---

## Files to Review

**Critical Files:**
```
artiusid-sdk/src/main/java/com/artiusid/sdk/
├── presentation/screens/
│   ├── document/NfcReadingViewModel.kt
│   ├── verification/VerificationScreenView.kt (if exists)
│   └── authentication/AuthenticationViewModel.kt
├── utils/
│   ├── CertificateManager.kt
│   └── TLSSessionManager.kt
└── config/
    └── SDKConfiguration.kt
```

---

## Questions for Team

1. **Pre-set Okta ID:** Do clients need to pass Okta user ID at init (or via setOktaUserId) and skip the in-flow collection screen?
2. **NFC Bug:** Have we received reports of NFC failures on 2nd+ verification attempts?
3. **Authentication Flows:** Should we create formal architecture documentation like iOS?
4. **Version Parity:** Should Android SDK bump to v2.x.x to indicate feature parity with iOS?

---

**Next Step:** Implement items from `IOS_TO_ANDROID_MIGRATION_PUNCHLIST.md` in priority order (Critical → High → Medium → Low).

