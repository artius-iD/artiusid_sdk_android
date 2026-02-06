# iOS to Android SDK Migration Status - Updated

**Date:** December 9, 2025  
**Last Updated:** December 9, 2025 (After Okta ID Integration)  
**iOS SDK Version:** v2.0.59  
**Android SDK Version:** v1.2.49

---

## ✅ Recently Completed

### Okta ID Integration (v2.0.12) - ✅ COMPLETE
**Status:** Fully implemented and matches iOS SDK v2.0.12

**What Was Done:**
- ✅ Added `includeOktaIDInVerificationPayload` to `SDKConfiguration` (default: true)
- ✅ Created `CollectOktaIDScreen.kt` - Compose UI matching iOS
- ✅ Created `OktaIDHolder.kt` - State management singleton
- ✅ Added `oktaId` field to `VerificationRequest` model
- ✅ Integrated into verification flow (Face → Document → Okta ID → Processing)
- ✅ Conditional step display in `VerificationStepsScreen`
- ✅ API payload includes oktaId when provided and enabled

**Files Modified:**
- `SDKConfiguration.kt`
- `VerificationRequest.kt`
- `ClientConfiguration.kt`
- `AppNavigation.kt`
- `VerificationProcessingViewModel.kt`
- `VerificationStepsScreen.kt`

**Files Created:**
- `CollectOktaIDScreen.kt`
- `OktaIDHolder.kt`

**Next Step:** End-to-end testing

---

## 🔴 CRITICAL - Needs Immediate Attention

### 1. NFC Reset State Management (iOS v2.0.43)
**Priority:** 🔴 CRITICAL  
**Status:** ⚠️ NEEDS INVESTIGATION

**iOS Problem:**
- Static `nfcStarted` flag persists across view instances
- If `onDisappear()` is not called, NFC can never start again on subsequent attempts
- Caused **100% failure rate** on 2nd+ passport verification attempts

**iOS Solution:**
- Added public static `ScanChipView.resetNFCState()` method
- Resets both `nfcStarted` flag and `retryGuard` state
- Called automatically on verification completion, cancellation, and NFC recapture

**Android Action Required:**
1. Test Android NFC flow with multiple sequential verifications
2. Check if `NfcReadingViewModel` has static state guards
3. If bug exists, add `resetNFCState()` method similar to iOS
4. Ensure reset is called on:
   - Verification completion (success or failure)
   - User cancellation
   - NFC timeout/error
   - Navigation away from NFC screen

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingViewModel.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingScreen.kt`

---

### 2. Verification Screen Default Fix (iOS v2.0.43)
**Priority:** 🔴 CRITICAL  
**Status:** ⚠️ NEEDS INVESTIGATION

**iOS Problem:**
- `VerificationScreenView` defaulted to SUCCESS when result was `nil`
- Line: `let isSuccess = verificationResult?.isSuccessful ?? true`
- Users saw "Verification Complete" success screen when verification actually failed

**iOS Solution:**
- Changed default from `true` to `false`
- Line: `let isSuccess = verificationResult?.isSuccessful ?? false`

**Android Action Required:**
1. Find Android's verification completion/result screen
2. Search for nullable verification result with default values
3. Ensure any null/default verification result defaults to FAILURE, not SUCCESS
4. Add logging when result is null to catch state timing issues

**Search Pattern:**
```kotlin
verificationResult?.isSuccessful ?: true  // ❌ BAD - defaults to success
verificationResult?.isSuccessful ?: false // ✅ GOOD - defaults to failure
```

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationScreenView.kt` (if exists)
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/completion/`
- Any screen showing final verification status

---

### 3. mTLS Certificate Automatic Reload (iOS v2.0.15, v2.0.13)
**Priority:** 🔴 CRITICAL  
**Status:** ⚠️ NEEDS VERIFICATION

**iOS Problem:**
- Certificate generated but not loaded into `TLSSessionManager` memory
- Verification/authentication API calls failed with "No client identity available"
- Environment switches didn't clear old certificate

**iOS Solution:**
1. Automatic reload after generation:
   ```swift
   try await ArtiusIDSDK.shared.ensureCertificateRegistered()
   let reloaded = TLSSessionManager.shared.clearAndReloadIdentity()
   ```
2. Environment switch handling:
   ```swift
   public func configure(...) {
       TLSSessionManager.shared.clearAndReloadIdentity()
   }
   ```

**Android Action Required:**
1. Review certificate generation flow:
   - Check if `CertificateManager.storeCertificatePem()` also loads it into `TLSSessionManager`
   - Verify that `ensureCertificateRegistered()` triggers reload
2. Review environment switch:
   - Check if `ArtiusIDSDK.initialize()` clears `TLSSessionManager` when environment changes
   - Add explicit `clearAndReloadIdentity()` call if missing
3. Add logging:
   - "🔐 Reloading certificate into TLSSessionManager..." when reloading
   - "🔐 Clearing TLS session manager for environment switch" when switching environments
4. Test:
   - Verify API calls work immediately after certificate generation
   - Test environment switching (Sandbox → Staging → Production)
   - Ensure correct certificate is used for each environment

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/CertificateManager.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/TLSSessionManager.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt` (configuration method)

---

### 4. NFC Concurrent Retry Prevention (iOS v2.0.19, v2.0.18)
**Priority:** 🔴 CRITICAL  
**Status:** ⚠️ NEEDS INVESTIGATION

**iOS Problem:**
- Multiple concurrent NFC session creation attempts
- "System resource unavailable" (Error Code 203) on retry
- "SWIFT TASK CONTINUATION MISUSE" errors
- Duplicate log entries indicating race conditions

**iOS Solution:**
1. Thread-safe lock-based guard:
   ```swift
   internal class NFCRetryGuard {
       private var isRetrying = false
       private let lock = NSLock()
       
       func tryAcquire() -> Bool {
           lock.lock()
           defer { lock.unlock() }
           if isRetrying { return false }
           isRetrying = true
           return true
       }
   }
   ```
2. NFC session cleanup via `cancelScan()`
3. Increased retry delay: 1.0s → 1.5s for hardware cleanup

**Android Action Required:**
1. Test NFC retry scenarios:
   - Start NFC scan, let it timeout
   - Trigger multiple retry attempts rapidly
   - Check for "resource unavailable" or similar errors
2. Review NFC implementation:
   - Check if retry guard/lock exists
   - Verify only ONE NFC session can be active at a time
3. Add thread-safe guard (if needed):
   ```kotlin
   class NFCRetryGuard {
       private var isRetrying = false
       private val lock = ReentrantLock()
       
       fun tryAcquire(): Boolean {
           lock.withLock {
               if (isRetrying) return false
               isRetrying = true
               return true
           }
       }
   }
   ```
4. Add proper cleanup:
   - Ensure NFC session is invalidated before retry
   - Add delay between retry attempts (1.5s minimum)

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingViewModel.kt`

---

## 🟠 HIGH - Important for UX

### 5. Dual Authentication Flows (iOS v2.0.21)
**Priority:** 🟠 HIGH  
**Status:** ⚠️ NEEDS DOCUMENTATION

**iOS Implementation:**
Two distinct authentication flows:

1. **Button-Triggered Authentication** (`ArtiusIDAuthenticationView`):
   - Configuration-based API with `clientId`, `accountNumber`, `environment`
   - Returns structured `AuthenticationResult` with account info
   - Used when user initiates authentication (button press)

2. **FCM Notification-Triggered Authentication Request** (`ArtiusIDAuthenticationRequestView`):
   - Callback-based API returning String ("yes"/"no"/"cancelled")
   - Uses `AppNotificationState.shared` for title/description
   - Used when server sends push notification

**Android Action Required:**
1. Review current implementation:
   - Check if button-triggered authentication exists
   - Check if FCM-triggered authentication request exists
   - Verify if they use different view models/screens
2. Document flow separation:
   - Create architecture documentation similar to iOS `AUTHENTICATION_FLOWS_MIGRATION.md`
   - Clarify when each flow should be used
3. Ensure API parity:
   - Button flow should return structured result (success, message, account info)
   - FCM flow should return simple outcome ("yes"/"no"/"cancelled")

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/authentication/AuthenticationViewModel.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ui/activities/ApprovalActivity.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/approval/ApprovalRequestScreen.kt`

---

## ✅ Already Implemented

### Dynamic Client Configuration (v2.0.59)
- ✅ `SDKConfiguration.kt` includes `clientId` and `clientGroupId`
- ✅ Configuration passed through `ArtiusIDSDK.initialize()`
- ✅ Used in API requests via `SDKConfiguration`

### Document Recapture (v2.0.12)
- ✅ `DocumentRecaptureType` enum exists
- ✅ `DocumentRecaptureNotificationView` component exists
- ✅ Auto-navigation after delay exists
- ⚠️ **Verify:** Error codes 600-604 trigger recapture (not permanent failure)

### Template-Based URL Configuration (v2.0.5)
- ✅ `UrlBuilder` has template system
- ✅ Environment-specific URL construction exists
- ⚠️ **Verify:** URL templates support both mobile and registration services

---

## 🟡 MEDIUM - Nice-to-have

### Verification Request Payload Capture (v2.0.59)
**Priority:** 🟡 MEDIUM  
**Status:** ❌ NOT IMPLEMENTED

**iOS Implementation:**
- `lastVerificationRequestPayload` property captures full JSON request
- `lastVerificationRequestPayloadSummary` replaces base64 images with size indicators
- `verificationRequestWillSendNotification` posted before API call
- Useful for debugging and sharing with teams

**Android Action Required:**
1. Add payload capture (nice-to-have):
   - Capture verification request JSON before sending to API
   - Replace large base64 image strings with size indicators
   - Provide public method to get payload summary as JSON string
   - Add notification/callback mechanism for debugging tools

**Files to Update:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/data/repository/VerificationRepository.kt`

---

## 🟢 LOW - Documentation/Polish

### UI/UX Improvements
**Priority:** 🟢 LOW  
**Status:** ⚠️ NEEDS AUDIT

**iOS Changes:**
1. Navigation back button redesign (v2.0.13)
2. Icon theming consistency (v2.0.19, v2.0.27)
3. Text sizing consistency (v2.0.16, v2.0.5)

**Android Action Required:**
1. Audit instruction screens:
   - Face scan intro
   - Passport scan intro
   - Government ID front/back intro
   - Check if all icons use theme's secondary color
2. Review text sizing:
   - Ensure tip cells have consistent height
   - Check font sizes are readable (minimum 14sp, prefer 16sp)
3. Review back button:
   - Ensure back button uses theme colors
   - Remove unnecessary container styling if present

### Comprehensive Logging (v2.0.15)
**Priority:** 🟢 LOW  
**Status:** ⚠️ NEEDS IMPROVEMENT

**iOS Implementation:**
- Detailed logging at every step of certificate generation and loading
- Clear error messages with troubleshooting hints
- Success banners for easy verification
- Emoji prefixes: 🔐 (cert/TLS), ✅ (success), ❌ (error), 🔍 (debug)

**Android Action Required:**
1. Audit logging consistency:
   - Add emoji prefixes for easy log filtering
   - Ensure all critical operations have start/end logs
   - Add success banners (e.g., "🔐 mTLS READY - Client certificate loaded")
2. Add troubleshooting hints:
   - Include next steps in error messages
   - Reference specific error codes with solutions

---

## Summary Statistics

| Status | Count | Percentage |
|--------|-------|------------|
| ✅ Completed | 4 | 33% |
| ⚠️ Needs Investigation | 5 | 42% |
| ❌ Not Implemented | 2 | 17% |
| 🟢 Low Priority | 1 | 8% |

**Total Items:** 12

---

## Recommended Action Plan

### Week 1 - Critical Testing
1. **Test NFC Sequential Verifications**
   - Run 3-5 passport verifications back-to-back
   - Check if NFC fails on 2nd+ attempts
   - Document findings

2. **Test Verification Result Handling**
   - Force null verification results
   - Ensure completion screen shows failure (not success)
   - Fix if needed

3. **Test Certificate Reload**
   - Clear credentials → verify → check API calls succeed
   - Switch environments → verify → check correct cert is used
   - Fix if needed

### Week 2 - Implementation
4. **Fix Any Discovered Issues**
   - Add NFC state reset if needed
   - Fix verification default if needed
   - Add cert reload if needed
   - Add NFC retry guard if needed

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

**Document Version:** 2.0  
**Last Updated:** December 9, 2025 (After Okta ID Integration)

