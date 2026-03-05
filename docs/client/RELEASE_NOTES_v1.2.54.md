# ArtiusID Android SDK v1.2.54 - Release Notes

**Release Date:** March 2026  
**Author:** artius.iD, Inc.

---

## iOS parity punch list

This release implements the iOS → Android parity punch list (see [docs/IOS_ANDROID_PUNCHLIST.md](../IOS_ANDROID_PUNCHLIST.md)).

### Public API (A)

- **URL template / domain:** `SDKConfiguration` optional `urlTemplate`, `mobileDomain`, `registrationUrlTemplate`, `registrationDomain`; applied in `initialize()`.
- **Biometric helpers:** `ArtiusIDSDK.isBiometricAvailable(context)`, `getBiometricType(context)`.
- **SDK info:** `getSDKInfo(context)` now includes `wrapperVersion` and `architecture`.
- **FCM:** `setFcmToken(token)` added; `updateFcmToken`, `getFCMToken`/`getCurrentFCMToken` aligned.
- **Verification / FCM listeners:** `verificationRequestWillSendListener`, `fcmTokenUpdatedListener`.
- **Certificate:** `ensureCertificateRegisteredOrThrow(context)` suspend; throws on failure.
- **API-only auth:** `ArtiusIDSDK.authenticate(context, accountNumber, request)` returns `Result<AuthenticationResult>`.
- **Environment mapping:** `mapToInternalEnvironment(viewLayerName)`, `mapToVerificationEnvironment(viewLayerName)`, `Environment.fromViewLayer(name)`.

### Config / theme / localization (B, C)

- **SDKConfiguration:** `copyWithFcmToken()`, `copyWithLogging()`; `hostAppPackageName` documented as iOS `hostAppBundleIdentifier` equivalent.
- **AppConstants-style:** `UrlBuilder.getAppConstantsStyle(context)` returns serverURL, verificationBaseUrl, approvalResponseUrl, etc.
- **Typography:** `SDKTypography.paragraphSpacing` added.
- **LocalizationKeys:** `approval_response_*`, `settings_*`, `sample_*` keys; `allKeys()`/`isValidKey()` updated.
- **LogLevel:** `shouldShow(currentMaxLevel)` added.
- **DocumentType:** `displayName` ("Photo ID" / "Passport").
- **IconCategory:** Object with CAMERA, FACE, DOCUMENT, NFC, SUCCESS, ERROR, WARNING, BACK, CLOSE, NAVIGATION, ACTION.

### Sample app (D)

- **Strings:** `settings_*` and `sample_*` added in en, es, fr, de.
- Main screen structure, approval result card, verification/authentication result sections, settings sections (language, environment, theme preview, theme, image overrides), and Okta wording already aligned; new localization keys support full parity.

### Documentation (E)

- **CLIENT_IMPLEMENTATION_GUIDE:** iOS parity note; version 1.2.54.
- **THEMING_GUIDE:** Theme concepts and keys (color scheme, typography, component styling, layout, animation, text content, icon theme) aligned with iOS; paragraphSpacing and IconCategory noted.

---

## MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher
