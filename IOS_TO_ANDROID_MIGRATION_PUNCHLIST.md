# iOS to Android SDK Feature Migration Punch List

**Date:** December 9, 2025 (Refreshed February 6, 2026)  
**Purpose:** Identify iOS SDK features and fixes that need to be ported to Android SDK  
**iOS SDK Version Reviewed:** v2.0.59 (source at `/Users/toddbryant/Documents/mobile-sdk-ios`)  
**Android SDK Current Version:** v1.2.49 (Okta ID integration completed)

---

## Executive Summary

The iOS SDK has received **significant updates** between v2.0.0 and v2.0.59, including critical bug fixes, feature enhancements, and architectural improvements. This document outlines what needs to be ported to the Android SDK to achieve feature parity.

### Priority Levels
- 🔴 **CRITICAL** - Security, authentication, or major functionality issues
- 🟠 **HIGH** - User experience, important features
- 🟡 **MEDIUM** - Nice-to-have features, minor improvements
- 🟢 **LOW** - Documentation, cosmetic changes

---

## 1. Dynamic Client Configuration (v2.0.59) - 🔴 CRITICAL

### iOS Implementation
- `clientId` and `clientGroupId` are **required parameters** in SDK configuration
- Changed from hardcoded constants to runtime-configurable values
- Removed `AppConstants.clientId` hardcoding from API calls
- Client apps must provide these values during SDK initialization

### Current Android Status
- ✅ **ALREADY IMPLEMENTED** - `SDKConfiguration.kt` includes `clientId` and `clientGroupId`
- ✅ Configuration is passed through `ArtiusIDSDK.initialize()`
- ✅ Used in API requests via `SDKConfiguration`

### Action Required
- ✅ **NO ACTION NEEDED** - Android SDK already has this functionality
- **Verification:** Ensure all API calls use `SDKConfiguration.clientId` and `SDKConfiguration.clientGroupId` instead of hardcoded values

---

## 2. NFC Reset State Management (v2.0.43) - 🔴 CRITICAL

### iOS Problem Fixed
- Static `nfcStarted` flag persists across view instances
- If `onDisappear()` is not called, NFC can never start again on subsequent attempts
- Caused **100% failure rate** on 2nd+ passport verification attempts

### iOS Solution
- Added public static `ScanChipView.resetNFCState()` method
- Resets both `nfcStarted` flag and `retryGuard` state
- Called automatically when:
  - Verification completes (success or failure)
  - User cancels verification
  - NFC recapture is triggered
  - User navigates away from passport flow

### Current Android Status
- ⚠️ **NEEDS INVESTIGATION** - Android has `NfcReadingViewModel.resetState()` but unclear if it handles static guards
- Android uses lifecycle-aware ViewModels which may handle this differently
- Need to verify if Android has similar static state persistence issues

### Action Required
1. **Test NFC Flow:** Verify if Android has the same bug (NFC fails on 2nd+ verification)
2. **Review `NfcReadingViewModel`:** Check if static state guards exist
3. **Add Reset Method:** If needed, create public `resetNFCState()` method similar to iOS
4. **Call Points:** Ensure reset is called on:
   - Verification completion
   - User cancellation
   - NFC timeout/error
   - Navigation away from NFC screen

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingViewModel.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingScreen.kt`

---

## 3. Verification Screen Default Fix (v2.0.43) - 🔴 CRITICAL

### iOS Problem Fixed
- `VerificationScreenView` defaulted to SUCCESS when result was `nil`
- Line: `let isSuccess = verificationResult?.isSuccessful ?? true`
- Users saw "Verification Complete" success screen when verification actually failed

### iOS Solution
- Changed default from `true` to `false`
- Line: `let isSuccess = verificationResult?.isSuccessful ?? false`
- If result state hasn't propagated, show failure instead of success

### Current Android Status
- ✅ **VERIFIED** - Android uses sealed `VerificationProcessingUiState` (Success / Failure). Success is set only when the API returns success (`verificationResult == VerificationResults.SUCCESS`). There is no nullable result that defaults to success; state is set explicitly in `VerificationProcessingViewModel`. No change required.

### Action Required
- ✅ **NO ACTION NEEDED** - Architecture differs from iOS; Android does not have the nil-default bug.

---

## 4. mTLS Certificate Automatic Reload (v2.0.15, v2.0.13) - 🔴 CRITICAL

### iOS Problem Fixed
- Certificate generated but not loaded into `TLSSessionManager` memory
- Verification/authentication API calls failed with "No client identity available"
- Environment switches didn't clear old certificate

### iOS Solution
1. **Automatic Reload After Generation:**
   ```swift
   try await ArtiusIDSDK.shared.ensureCertificateRegistered()
   print("🔐 Reloading certificate into TLSSessionManager...")
   let reloaded = TLSSessionManager.shared.clearAndReloadIdentity()
   ```
2. **Environment Switch Handling:**
   ```swift
   public func configure(...) {
       print("🔐 Clearing TLS session manager for environment switch")
       TLSSessionManager.shared.clearAndReloadIdentity()
   }
   ```

### Current Android Status
- ✅ **PARTIALLY IMPLEMENTED** - Environment-specific cert storage exists
- ⚠️ **NEEDS VERIFICATION** - Unclear if cert is reloaded after generation
- ⚠️ **NEEDS VERIFICATION** - Unclear if cert is cleared on environment switch

### Action Required
1. **Review Certificate Generation Flow:**
   - Check if `CertificateManager.storeCertificatePem()` also loads it into `TLSSessionManager`
   - Verify that `ensureCertificateRegistered()` triggers reload
2. **Environment Switch (CRITICAL):** iOS calls `TLSSessionManager.shared.clearAndReloadIdentity()` at the start of `configure()` every time so that re-initializing with a different environment uses the new certificate.
   - **Android:** In `ArtiusIDSDK.initialize()` and `initializeWithEnhancedTheme()`, at the start (before URL/config and certificate init), call the equivalent of `clearAndReloadIdentity()` (e.g. via `SharedContextManager` or `APIManager`) so that environment switch clears the old certificate and the subsequent `ensureSharedCertificate` loads the correct one for the new environment.
3. **Add Logging:**
   - "🔐 Reloading certificate into TLSSessionManager..." when reloading
   - "🔐 Clearing TLS session manager for environment switch" when switching environments
4. **Test:**
   - Verify API calls work immediately after certificate generation
   - Test environment switching (Sandbox → Staging → Production)
   - Ensure correct certificate is used for each environment

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/CertificateManager.kt` (lines 330-365)
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/TLSSessionManager.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt` (add clear at start of `initialize()`)
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/SharedContextManager.kt` (if it holds TLS state)

---

## 5. NFC Concurrent Retry Prevention (v2.0.19, v2.0.18) - 🔴 CRITICAL

### iOS Problem Fixed
- Multiple concurrent NFC session creation attempts
- "System resource unavailable" (Error Code 203) on retry
- "SWIFT TASK CONTINUATION MISUSE" errors
- Duplicate log entries indicating race conditions

### iOS Solution
1. **Thread-Safe Lock-Based Guard:**
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
       
       func release() {
           lock.lock()
           defer { lock.unlock() }
           isRetrying = false
       }
   }
   ```
2. **NFC Session Cleanup:**
   ```swift
   func cancelScan() {
       session?.invalidate(errorMessage: "Scan cancelled")
       continuation?.resume(throwing: PassportError.UserCanceled)
   }
   ```
3. **Increased Retry Delay:** 1.0s → 1.5s for hardware cleanup

### Current Android Status
- ⚠️ **NEEDS INVESTIGATION** - Unknown if Android has concurrent NFC retry issues
- Android's NFC API is different from iOS's `NFCTagReaderSession`
- Need to verify if multiple NFC intents can be processed concurrently

### Action Required
1. **Test NFC Retry Scenarios:**
   - Start NFC scan, let it timeout
   - Trigger multiple retry attempts rapidly
   - Check for "resource unavailable" or similar errors
2. **Review NFC Implementation:**
   - Check if retry guard/lock exists
   - Verify only ONE NFC session can be active at a time
3. **Add Thread-Safe Guard (if needed):**
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
       
       fun release() {
           lock.withLock {
               isRetrying = false
           }
       }
   }
   ```
4. **Add Proper Cleanup:**
   - Ensure NFC session is invalidated before retry
   - Add delay between retry attempts (1.5s minimum)

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/NfcReadingViewModel.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/nfc/` (if exists)

---

## 6. Dual Authentication Flows (v2.0.21) - 🟠 HIGH

### iOS Implementation
Two distinct authentication flows:

1. **Button-Triggered Authentication** (`ArtiusIDAuthenticationView`):
   - Configuration-based API with `clientId`, `accountNumber`, `environment`
   - Returns structured `AuthenticationResult` with account info
   - Used when user initiates authentication (button press)

2. **FCM Notification-Triggered Authentication Request** (`ArtiusIDAuthenticationRequestView`):
   - Callback-based API returning String ("yes"/"no"/"cancelled")
   - Uses `AppNotificationState.shared` for title/description
   - Used when server sends push notification

### Current Android Status
- ✅ **PARTIALLY IMPLEMENTED** - Android has approval/authentication flows
- ⚠️ **NEEDS CLARIFICATION** - Are flows clearly separated like iOS?
- ⚠️ **NEEDS VERIFICATION** - Does Android have both button-triggered and FCM-triggered authentication?

### Action Required
1. **Review Current Implementation:**
   - Check if button-triggered authentication exists
   - Check if FCM-triggered authentication request exists
   - Verify if they use different view models/screens
2. **Document Flow Separation:**
   - Create architecture documentation similar to iOS `AUTHENTICATION_FLOWS_MIGRATION.md`
   - Clarify when each flow should be used
3. **Ensure API Parity:**
   - Button flow should return structured result (success, message, account info)
   - FCM flow should return simple outcome ("yes"/"no"/"cancelled")

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/authentication/AuthenticationViewModel.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ui/activities/ApprovalActivity.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/approval/ApprovalRequestScreen.kt`

---

## 7. Document Recapture (v2.0.12) - 🟠 HIGH

### iOS Implementation
- Error codes 600-604 trigger recapture screens instead of permanent failure
- `DocumentRecaptureType` enum with specific error types
- `DocumentRecaptureNotificationView` with user-friendly instructions
- Automatic navigation back to appropriate scan screen for retry

### Current Android Status
- ✅ **ALREADY IMPLEMENTED** - Android has `DocumentRecaptureType` enum
- ✅ Android has `DocumentRecaptureNotificationView` component
- ✅ Auto-navigation after delay exists

### Action Required
- ✅ **VERIFY FEATURE COMPLETENESS:**
  1. Confirm error codes 600-604 trigger recapture (not permanent failure)
  2. Test recapture flow for each document type (passport, state ID front/back)
  3. Ensure user sees helpful error messages (not generic errors)
  4. Verify previous scan data is cleared on recapture

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/data/model/DocumentRecaptureType.kt` ✅
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/components/DocumentRecaptureNotificationView.kt` ✅
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationProcessingScreen.kt` ✅

---

## 8. Okta ID Integration (v2.0.12) - ✅ COMPLETED

### iOS Implementation
- Optional Okta ID collection during verification flow
- `includeOktaIDInVerificationPayload` configuration parameter (default: true)
- `CollectOktaIDView` for user input
- Okta ID field added to `VerificationResult`
- Conditionally included in verification API payload

### Current Android Status
- ✅ **COMPLETED** - Full implementation matching iOS v2.0.12
- ✅ `includeOktaIDInVerificationPayload` added to `SDKConfiguration`
- ✅ `CollectOktaIDScreen.kt` created (Compose UI)
- ✅ `OktaIDHolder.kt` singleton for state management
- ✅ Okta ID field added to `VerificationRequest`
- ✅ Conditional navigation step in verification flow
- ✅ VerificationStepsScreen shows Okta ID step conditionally
- ✅ API payload includes oktaId when provided

### Action Required
- ✅ **NO ACTION NEEDED** - Feature complete
- **Testing Required:** End-to-end testing of Okta ID collection flow

**Files Created/Modified:**
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/config/SDKConfiguration.kt`
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/data/model/VerificationRequest.kt`
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/CollectOktaIDScreen.kt` (NEW)
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/OktaIDHolder.kt` (NEW)
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/navigation/AppNavigation.kt`
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationProcessingViewModel.kt`
- ✅ `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationStepsScreen.kt`

---

## 9. Pre-set Okta User ID (iOS configure / setOktaUserId) - 🟠 HIGH

### iOS Implementation
- `ArtiusIDSDKWrapper.configure(..., oktaUserId: String? = nil)` — client can pass Okta user ID at init; stored in keychain (environment-specific).
- `setOktaUserId(_ userId: String?)` / `getOktaUserId(for environment: String?)` — runtime setter/getter; explicit value overrides keychain.
- When Okta ID is already set (keychain or explicit), `CollectOktaIDView` is skipped and the stored ID is used in the verification payload.

### Current Android Status
- ❌ **NOT IMPLEMENTED** — No `oktaUserId` in `SDKConfiguration` or `ArtiusIDSDK`. Okta ID is only collected in-flow via `CollectOktaIDScreen` and `OktaIDHolder`.

### Action Required
1. **Add to configuration:** Optional `oktaUserId: String? = null` to `SDKConfiguration` (or equivalent) and pass through `ArtiusIDSDK.initialize()`.
2. **Persist per environment:** Store in encrypted prefs/keychain keyed by environment (e.g. `oktaUserId_sandbox`) so re-verification can use it.
3. **Add runtime API:** `ArtiusIDSDK.setOktaUserId(userId: String?)` and `ArtiusIDSDK.getOktaUserId(): String?` that update/read the same storage.
4. **Skip CollectOktaID when set:** In verification flow, if `getOktaUserId()` is non-null/non-empty, skip navigation to `CollectOktaIDScreen` and use that value in `VerificationRequest` (same as iOS `shouldCollectOktaID()` / keychain check).

**Files to Update:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/config/SDKConfiguration.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/navigation/AppNavigation.kt` (conditional navigation)
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationProcessingViewModel.kt` (source of oktaId)
- Consider `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/EnvironmentCredentialManager.kt` or new helper for env-keyed Okta ID storage

---

## 10. Re-verification / accountNumber in VerificationRequest (iOS v2.0.17) - 🟡 MEDIUM

### iOS Implementation
- `VerificationRequest` includes `accountNumber` (member ID from keychain from previous verification).
- Enables re-verification for existing users; key is environment-specific (`verification-{environment}`).

### Current Android Status
- ❌ **NOT IN REQUEST** — `VerificationRequest.kt` has no `accountNumber` field. Android stores account number in `VerificationStateManager` after success but does not include it in the verification API request body.

### Action Required
1. **Add field:** Add `accountNumber: String? = null` (or empty string) to `VerificationRequest` and include in `toOrderedMap()` when non-empty.
2. **Populate from storage:** In `VerificationProcessingViewModel`, before building the request, retrieve existing account number for current environment from `VerificationStateManager` (or equivalent) and set on the request when available.
3. **Match API:** Ensure request body matches backend expectation (same key name as iOS: `accountNumber`).

**Files to Update:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/data/model/VerificationRequest.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationProcessingViewModel.kt`

---

## 11. Template-Based URL Configuration (v2.0.5) - 🟡 MEDIUM

### iOS Implementation
- Flexible URL template system with `#env#` and `#domain#` placeholders
- Separate templates for mobile and registration services
- Example: `https://#env#.#domain#` → `https://sandbox.mobile.artiusid.dev`

### Current Android Status
- ✅ **ALREADY IMPLEMENTED** - `UrlBuilder` has template system
- ✅ Environment-specific URL construction exists

### Action Required
- ✅ **VERIFY FEATURE COMPLETENESS:**
  1. Confirm URL templates support both mobile and registration services
  2. Test environment switching updates URLs correctly
  3. Verify legacy environment formats are supported

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/UrlBuilder.kt`

---

## 12. UI/UX Improvements - 🟢 LOW

### iOS Changes
1. **Navigation Back Button Redesign (v2.0.13):**
   - Removed container styling, frame constraints, backgrounds
   - Simplified to HStack with padding only
   - Back button uses `secondaryColor` for theming

2. **Icon Theming Consistency (v2.0.19, v2.0.27):**
   - All instruction icons use `secondaryColor` from theme
   - Face scan, passport scan, government ID scan icons
   - Consistent color across all SDK screens

3. **Text Sizing Consistency (v2.0.16, v2.0.5):**
   - Fixed small text in instruction tips
   - Uniform cell height (50pt) for tip cells
   - Increased font size from 14 to 16 for readability
   - Fixed empty cells to match height of cells with content

### Current Android Status
- ⚠️ **NEEDS REVIEW** - Unclear if Android has equivalent theming consistency

### Action Required
1. **Audit Instruction Screens:**
   - Face scan intro
   - Passport scan intro
   - Government ID front/back intro
   - Check if all icons use theme's secondary color
2. **Review Text Sizing:**
   - Ensure tip cells have consistent height
   - Check font sizes are readable (minimum 14sp, prefer 16sp)
3. **Review Back Button:**
   - Ensure back button uses theme colors
   - Remove unnecessary container styling if present

**Files to Review:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/face/`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/document/`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/passport/`

---

## 13. Verification Request Payload Capture (iOS v2.0.59) - 🟢 LOW

### iOS Implementation
- `lastVerificationRequestPayload` property captures full JSON request
- `lastVerificationRequestPayloadSummary` replaces base64 images with size indicators
- `verificationRequestWillSendNotification` posted before API call
- Useful for debugging and sharing with teams

### Current Android Status
- ❌ **NOT FOUND** - No evidence of verification payload capture

### Action Required
1. **Add Payload Capture (Nice-to-have):**
   - Capture verification request JSON before sending to API
   - Replace large base64 image strings with size indicators
   - Provide public method to get payload summary as JSON string
   - Add notification/callback mechanism for debugging tools
2. **Use Cases:**
   - Debugging verification failures
   - Sharing request structure with iOS team
   - Client app debugging tools

**Files to Update:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/data/repository/VerificationRepository.kt`

---

## 14. Comprehensive Logging (v2.0.15) - 🟢 LOW

### iOS Implementation
- Detailed logging at every step of certificate generation and loading
- Clear error messages with troubleshooting hints
- Success banners for easy verification
- Emoji prefixes: 🔐 (cert/TLS), ✅ (success), ❌ (error), 🔍 (debug)

### Current Android Status
- ✅ **PARTIALLY IMPLEMENTED** - Android has logging but may not be as comprehensive

### Action Required
1. **Audit Logging Consistency:**
   - Add emoji prefixes for easy log filtering
   - Ensure all critical operations have start/end logs
   - Add success banners (e.g., "🔐 mTLS READY - Client certificate loaded")
2. **Add Troubleshooting Hints:**
   - Include next steps in error messages
   - Reference specific error codes with solutions

**Example:**
```kotlin
Log.d(TAG, "🔐 ========================================")
Log.d(TAG, "🔐 Starting certificate generation...")
Log.d(TAG, "🔐 Environment: ${currentEnvironment}")
Log.d(TAG, "🔐 ========================================")
// ... operation ...
Log.d(TAG, "✅ Certificate and private key stored successfully")
Log.d(TAG, "🔐 mTLS READY - Client certificate loaded")
```

**Files to Update:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/CertificateManager.kt`
- `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/TLSSessionManager.kt`

---

## Priority Summary

### 🔴 CRITICAL (Immediate Action Required)
1. ✅ **Dynamic Client Configuration** - Already implemented, verify usage
2. ⚠️ **NFC Reset State Management** - Investigate if Android has same bug
3. ✅ **Verification Screen Default Fix** - Verified: Android uses sealed state; no null-default success
4. ⚠️ **mTLS Certificate Automatic Reload** - Add clearAndReload at start of initialize() for env switch
5. ⚠️ **NFC Concurrent Retry Prevention** - Test and add thread-safe guard if needed

### 🟠 HIGH (Important for UX)
6. ⚠️ **Dual Authentication Flows** - Document flow separation, ensure API parity
7. ✅ **Document Recapture** - Already implemented, verify completeness
8. ❌ **Pre-set Okta User ID** - Add oktaUserId to config + setOktaUserId/getOktaUserId; skip CollectOktaID when set

### 🟡 MEDIUM (Nice-to-have)
9. ✅ **Okta ID Integration** - ✅ **COMPLETED** - Full implementation matching iOS v2.0.12
10. ❌ **Re-verification (accountNumber in VerificationRequest)** - Add field and populate from storage
11. ✅ **Template-Based URL Configuration** - Already implemented, verify

### 🟢 LOW (Documentation/Polish)
12. ⚠️ **UI/UX Improvements** - Audit theming consistency
13. ❌ **Verification Request Payload Capture** - Add for debugging
14. ⚠️ **Comprehensive Logging** - Improve log formatting and consistency

---

## Next Steps

1. **Create GitHub Issues:**
   - One issue per critical/high priority item
   - Tag with labels: `ios-parity`, `bug`, `enhancement`
2. **Assign Priorities:**
   - Critical items → Sprint 1
   - High items → Sprint 2
   - Medium/Low items → Backlog
3. **Testing Plan:**
   - Create test cases for each critical item
   - Compare Android behavior with iOS behavior
   - Document any platform-specific differences
4. **Documentation:**
   - Update `CLIENT_IMPLEMENTATION_GUIDE.md` with new features
   - Add architecture documentation for authentication flows
   - Create migration guide for clients upgrading from older versions

---

**Document Version:** 1.1  
**Last Updated:** February 6, 2026  
**Author:** AI Assistant (based on iOS SDK review; refreshed against `/Users/toddbryant/Documents/mobile-sdk-ios`)

