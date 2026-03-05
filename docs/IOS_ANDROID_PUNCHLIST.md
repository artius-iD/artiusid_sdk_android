# iOS → Android SDK Parity Punch List

**Purpose:** Make the Android SDK and sample app an exact doppelganger of the iOS SDK and sample app.  
**iOS reference:** `/Users/toddbryant/Documents/mobile-sdk-ios` (or `../mobile-sdk-ios`).  
**Status:** Check off items as you implement; keep this file in sync with [IOS_ANDROID_PARITY.md](IOS_ANDROID_PARITY.md).

---

## A. Public API – iOS has it; Android missing or different

- [x] **A1. URL template / domain configure**  
  iOS: `ArtiusIDSDK.configure(environment:urlTemplate:mobileDomain:registrationUrlTemplate:registrationDomain:clientId:clientGroupId:...)`. Android uses `SDKConfiguration(baseUrl, environment)` only. Add optional URL template + domain parameters or document as platform difference.

- [x] **A2. Present-from-UI helpers**  
  iOS: `ArtiusIDKit.presentVerification(...)`, `presentAuthentication(...)`, View extensions `artiusIDVerification(...)`, `artiusIDAuthentication(...)`. Android: only `startVerification(activity, callback)`. Optionally add Compose/Activity helpers that match “present” semantics.

- [x] **A3. Biometric helpers**  
  iOS: `ArtiusIDKit.isBiometricAuthenticationAvailable()`, `availableBiometricType()`. Android: add equivalent on `ArtiusIDSDK` or a small helper (e.g. `ArtiusIDSDK.isBiometricAvailable(context)`, `getBiometricType(context)`).

- [x] **A4. Dependency / keychain API**  
  iOS: `ArtiusIDSDKDependencies.initialize()`, `verifyDependencies()`, public `Keychain` (set/get/delete/setOktaUserId/getOktaUserId). Android: no public dependency init or keychain type. Either add a minimal public “keychain”/prefs facade for Okta and critical keys or document as internal.

- [x] **A5. SDK info shape**  
  iOS: `getSDKInfo()` includes wrapperVersion, architecture, etc. Android: `getSDKInfo(context)` has sdkVersion, platform, firebase*. Add wrapperVersion and architecture (or equivalent) so debug/info sheet matches.

- [x] **A6. FCM token API**  
  iOS: `setFCMToken`, `getFCMToken`, `updateFCMToken` (no context). Android: `updateFcmToken`, `getCurrentFCMToken(context)`. Consider adding `setFcmToken(token)` if iOS allows setting without Firebase. Align naming (set/get/update) where possible.

- [x] **A7. Language API**  
  iOS: `setLanguage(_ languageCode: String)`. Android: `setLanguage(context, languageCode)`. Keep context on Android; document difference.

- [x] **A8. Verification payload / FCM notifications**  
  iOS: `verificationRequestWillSendNotification`, `fcmTokenUpdatedNotification`. Android: no broadcast/notification. Add optional callbacks or broadcast for “payload will send” and “FCM token updated” if parity required.

- [x] **A9. ensureCertificateRegistered**  
  iOS: `async throws -> Bool`. Android: `suspend`, returns Boolean, no throw. Consider exposing a wrapper that throws on failure for parity, or document.

- [x] **A10. sendApprovalRequest(context)**  
  iOS: no context; uses UIDevice. Android: requires Context. Result type (success, message, requestId) aligned. Document; no change required unless we add device-id abstraction.

- [x] **A11. authenticate(request:) (API-only)**  
  iOS: `ArtiusIDSDK.shared.authenticate(request:)` for API-only auth. Android: only `startAuthentication(activity, callback)`. Add a public `authenticate(context, request)` that calls backend only and returns result, if we want parity.

- [x] **A12. Environment mapping**  
  iOS: `mapToInternalEnvironment(from:)`, `mapToVerificationEnvironment(from:)`. Android: single `Environment` enum. Add public mapping helpers from view-layer environment to internal if we introduce a view-layer enum.

- [x] **A13. Approval / Auth Request views**  
  iOS: SwiftUI `ArtiusID.ApprovalView`, `ArtiusID.AuthenticationRequestEntryView` with `onCompletion((String)?)`. Android uses activities/callbacks. Document; optional: expose a “view model” or fragment that can be embedded with same callback contract.

- [x] **A14. VerificationResult fields**  
  iOS: firstName, lastName, documentStatus, documentScore, antiSpoofingFaceScore, personScore, personResult, personRating, riskInformationScore/Result/Rating, rawResponse, etc. Audit Android `VerificationResult` and `VerificationResultData`; add any missing fields and align names.

- [x] **A15. DocumentRecaptureType**  
  Align naming (e.g. iOS camelCase vs Android UPPER_SNAKE) and ensure fromHttpErrorCode / fromErrorMessage behavior matches.

- [x] **A16. AuthenticationResult.AccountInfo**  
  iOS: accountNumber, fullName?, isActive. Ensure Android callback result type has same shape (accountNumber, fullName, isActive).

---

## B. Config / theme / localization

- [x] **B1. SDKConfiguration**  
  - `hostAppBundleIdentifier` (iOS) vs `hostAppPackageName` (Android): document or add alias.  
  - iOS mutating `updateFCMToken`, `updateLogging` on config; Android config immutable. Optional: add `copy(updateFcmToken=…)` or similar builder.  
  - iOS `imageOverrides?` optional; Android non-null. Acceptable; document.

- [x] **B2. AppConstants-style API**  
  iOS exposes AppConstants (serverURL, verificationBaseUrl, approvalResponseUrl, etc.). Android uses UrlBuilder/internal. Expose a read-only “AppConstants” or debug API that returns same logical values for info/settings screen parity.

- [x] **B3. Theme – color scheme**  
  Android has extra keys (primaryContainer, onSuccessColorHex, step colors, scrim, overlay). iOS has fewer. Add any iOS-only keys to Android if missing; document Android extras.

- [x] **B4. Theme – typography**  
  iOS: single `label`, `caption`, `paragraphSpacing`. Android: labelLarge/Medium/Small, no paragraphSpacing. Add paragraphSpacing to Android typography; consider label/caption alias.

- [x] **B5. Theme – text content**  
  iOS: all optional String?; Android: required with defaults. Ensure every iOS key exists on Android and vice versa; align default copy where needed.

- [x] **B6. Theme – animation**  
  Align enableSuccessAnimations vs enableStatusAnimations; add shortAnimationDuration to iOS or document.

- [x] **B7. Theme – layout**  
  Align maxContentWidth (iOS 600 vs Android 400), documentOverlayAspectRatio (0.63 vs 1.6); add overlayStrokeWidth to iOS if needed.

- [x] **B8. Theme – brand logo**  
  iOS: brandLogo (resource name). Android: brandLogoUrl + brandLogoResourceName. Document; no change required.

- [x] **B9. Localization keys**  
  Audit LocalizationKeys.kt vs iOS LocalizationKeys; add any missing keys; ensure allKeys() / isValidKey() match.

---

## C. Models / enums

- [x] **C1. LogLevel**  
  LogLevel.shouldShow(currentMaxLevel: LogLevel) added.

- [x] **C2. Environment naming**  
  Environment.fromViewLayer(name), ArtiusIDSDK.mapToInternalEnvironment/mapToVerificationEnvironment added.

- [x] **C3. DocumentType**  
  DocumentType(ID_CARD, PASSPORT) with displayName "Photo ID" / "Passport".

- [x] **C4. VerificationResult (full field list)**  
  All fields present (see A14).

- [x] **C5. IconCategory**  
  IconCategory object with CAMERA, FACE, DOCUMENT, NFC, SUCCESS, ERROR, WARNING, BACK, CLOSE, NAVIGATION, ACTION.

---

## D. Sample app – feature and UX parity

- [x] **D1. Main screen structure**  
  iOS: Header, Action Buttons (Verification, Authentication, Approval), then single “most recent” result (verification **or** authentication **or** approval **or** clear/fcm). Android: ensure same “one result at a time” and same order of sections (header → actions → result card).

- [x] **D2. Approval result card**  
  iOS: Card title “Approval Request Result”, body shows “Approved” or “Declined” (localized) via getApprovalDisplayValue. Android: ensure approval result card uses same title string key and displays localized “Approved”/“Declined” (already done in SDK screen; add card on main screen if missing).

- [x] **D3. Verification result section**  
  iOS: verificationResultsSection(result) with personal info, scores, risk. Android: ensure same fields and layout (account number, full name, verification score, document status, face match, recapture, etc.).

- [x] **D4. Authentication result section**  
  iOS: authenticationResultsSection(result) with status and response. Android: match layout and localized status strings (e.g. success/failed/cancelled).

- [x] **D5. Settings – sections**  
  iOS: Language, Environment (unlock via long-press), Theme Preview, Theme, Image Overrides; Info sheet (Client ID, Account Number, FCM Token, APNs Token, Device ID, SDK Version, App Version, Certificate status/loaded date, Device model, iOS version). Android: same sections; replace APNs with FCM-only; add “Android version” and certificate loaded date; show SDK version via ArtiusIDSDK.getSdkVersion().

- [x] **D6. Settings – fully localized**  
  iOS 2.0.138: “Fully localized SampleAppSettingsView”. Ensure every settings label and info row uses a localized string (settings_title, settings_language, settings_environment, settings_theme, settings_imageOverrides, settings_config_info, settings_account_number, settings_fcm_token, settings_device_id, settings_sdk_version, settings_app_version, settings_certificate, settings_status, settings_loaded_date, settings_device, settings_model, settings_ios_version → Android: settings_android_version), and sample_done, sample_theme_preview, sample_primary_color, etc.

- [x] **D7. Settings – environment unlock**  
  iOS: long-press on “Settings” title (3s) to unlock environment. Android: replicate (long-press on settings title or toolbar to show environment picker).

- [x] **D8. Settings – copy to clipboard**  
  iOS: copyable rows for Account Number, FCM Token, Device ID with toast. Android: same copyable fields and toast/snackbar.

- [x] **D9. Settings – theme preview**  
  iOS: Theme Preview section with Primary, Secondary, Background color swatches. Android: match (Theme Preview with same three swatches and labels).

- [x] **D10. Okta in sample app**  
  iOS: Include Okta toggle and Okta login in sample; Android: already has in-app WebView Okta. Ensure toggle default (off) and flow match; ensure “Okta ID” section and “Login with Okta” / “Re-login” match iOS wording and behavior.

- [x] **D11. Sample app strings**  
  Add any missing sample_* and settings_* keys so every user-visible string is localized (en, es, fr, de) to match iOS.

---

## E. Documentation and release

- [x] **E1. CLIENT_IMPLEMENTATION_GUIDE**  
  Sync high-level steps (init, verification, authentication, approval, Okta, errors) between iOS and Android guides; keep platform-specific details (Swift/Kotlin, SPM/Gradle).

- [x] **E2. THEMING_GUIDE**  
  Ensure Android THEMING_GUIDE lists the same theme concepts and keys as iOS (color scheme, typography, component styling, layout, animation, text content, icon theme).

- [x] **E3. CHANGELOG**  
  After implementing items, update CHANGELOG.md and RELEASE_NOTES with “iOS parity: …” for each change.

---

## How to use this punch list

1. **Approve:** Review each unchecked item; confirm you want it implemented (or mark “N/A – platform difference” in a comment).
2. **Implement:** For each approved item, implement in `artiusid-sdk` and/or `sample-app`; then check the box.
3. **Sync:** Update [IOS_ANDROID_PARITY.md](IOS_ANDROID_PARITY.md) when a section is fully done (e.g. “VerificationResult fields”).
4. **Reference:** iOS repo path for diffing: `/Users/toddbryant/Documents/mobile-sdk-ios` (or `../mobile-sdk-ios`).
