# iOS to Android SDK Feature Migration Punch List

**Date:** December 9, 2025 (Updated)  
**Purpose:** Identify iOS SDK features and fixes that need to be ported to Android SDK  
**iOS SDK Version Reviewed:** v2.0.59  
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
- ⚠️ **NEEDS INVESTIGATION** - Need to check verification result handling in completion screen

### Action Required
1. **Find Equivalent Screen:** Locate Android's verification completion/result screen
2. **Review Default Logic:** Search for nullable verification result with default values
3. **Fix Default:** Ensure any null/default verification result defaults to FAILURE, not SUCCESS
4. **Add Logging:** Add clear logging when result is null to catch state timing issues

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
2. **Review Environment Switch:**
   - Check if `ArtiusIDSDK.initialize()` clears `TLSSessionManager` when environment changes
   - Add explicit `clearAndReloadIdentity()` call if missing
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
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt` (configuration method)

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

## 9. Template-Based URL Configuration (v2.0.5) - 🟡 MEDIUM

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

## 10. UI/UX Improvements - 🟢 LOW

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

## 11. Verification Request Payload Capture (iOS v2.0.59) - 🟢 LOW

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

## 12. Comprehensive Logging (v2.0.15) - 🟢 LOW

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
3. ⚠️ **Verification Screen Default Fix** - Find and fix null result handling
4. ⚠️ **mTLS Certificate Automatic Reload** - Verify reload after generation and env switch
5. ⚠️ **NFC Concurrent Retry Prevention** - Test and add thread-safe guard if needed

### 🟠 HIGH (Important for UX)
6. ⚠️ **Dual Authentication Flows** - Document flow separation, ensure API parity
7. ✅ **Document Recapture** - Already implemented, verify completeness

### 🟡 MEDIUM (Nice-to-have)
8. ✅ **Okta ID Integration** - ✅ **COMPLETED** - Full implementation matching iOS v2.0.12
9. ✅ **Template-Based URL Configuration** - Already implemented, verify

### 🟢 LOW (Documentation/Polish)
10. ⚠️ **UI/UX Improvements** - Audit theming consistency
11. ❌ **Verification Request Payload Capture** - Add for debugging
12. ⚠️ **Comprehensive Logging** - Improve log formatting and consistency

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

**Document Version:** 1.0  
**Last Updated:** December 9, 2025  
**Author:** AI Assistant (based on iOS SDK review)

