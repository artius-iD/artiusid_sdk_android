# iOS vs Android SDK Parity Analysis

This document lists differences between the iOS and Android SDKs and tracks parity work. Use it when integrating changes from the **mobile-ios-sdk** GitLab repository into **mobile-sdk-android**.

---

## Integrating changes from mobile-ios-sdk (GitLab)

**iOS repo path (local):** `../mobile-sdk-ios` or `/Users/toddbryant/Documents/mobile-sdk-ios` (clone from GitLab alongside this repo).

When the iOS SDK or sample-app in GitLab receives updates, use this workflow:

1. **Review iOS changes** – Check mobile-ios-sdk repo (and sample-app) for new APIs, config, theme, or flow changes.
2. **Check this parity doc and the punch list** – See which sections below are still marked "Action" or need updating; use [IOS_ANDROID_PUNCHLIST.md](IOS_ANDROID_PUNCHLIST.md) for the itemized checklist and add new rows there when iOS adds features.
3. **Port to Android** – Implement equivalent behavior in `artiusid-sdk` and/or `sample-app`; follow Kotlin/Android conventions and existing patterns in this repo.
4. **Update parity tables** – Mark items as done (✅) in this file and add a brief note if useful.
5. **Update docs** – Sync [CLIENT_IMPLEMENTATION_GUIDE.md](client/CLIENT_IMPLEMENTATION_GUIDE.md) high-level steps if iOS has new integration steps; update [README.md](../README.md) or [RELEASE_NOTES](../client/) as needed.
6. **Comments for developers** – In code, use `// iOS parity: <description>` where behavior intentionally matches iOS. See markdown guides: [THEMING_GUIDE.md](../THEMING_GUIDE.md), [sample-app/README.md](../sample-app/README.md), [CONTRIBUTING.md](../CONTRIBUTING.md).

---

## 1. SDKConfiguration

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| **Convenience inits** | `development(apiKey:)`, `production(apiKey:clientId:clientGroupId:)` | `SDKConfiguration.development(apiKey, baseUrl)`, `production(apiKey, clientId, clientGroupId)` | ✅ Done |
| **Validation** | clientId/clientGroupId ≥ 1, requestTimeout > 0, custom FCM when !handleFirebase | `validate()`: apiKey, clientId/clientGroupId ≥ 1, requestTimeout > 0, certificatePins, customFcmToken when !handleFirebase | ✅ Done |
| **Runtime updates** | `updateFCMToken(_:)`, `updateLogging(enabled:level:)` on config (mutating) | `ArtiusIDSDK.updateFcmToken()` | Optional: add `updateLogging` on SDK |
| **Debug** | `debugDescription`, `printDebugInfo()`, `getFirebaseDebugInfo()` on config | `debugDescription()`, `getFirebaseDebugInfo()` on SDKConfiguration | ✅ Done |
| **hostAppBundleIdentifier** | Auto from Bundle | N/A (Android uses context) | — |
| **Okta / Biometrics** | Okta on wrapper, not config | includeOktaIDInVerificationPayload, oktaUserId, enableBiometrics on config | Keep as-is (Android extends config) |

---

## 2. EnhancedSDKThemeConfiguration

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| **Simple init** | `init(brandName, primaryColorHex, ...)` | Use constructor + defaults; optional overload possible | Optional |
| **Validation** | `validate()`, `isValid` | `validate(): List<String>`, `isValid` | ✅ Done |
| **Copy helpers** | `withBrandName(_:)`, `withColors(_:)`, `withTypography(_:)` | `withBrandName(name)`, `withColors(colors)`, `withTypography(typography)` | ✅ Done |
| **Debug** | `debugDescription`, `printDebugInfo()` | `debugDescription(): String` | ✅ Done |

---

## 3. SDKComponentStyling (theme)

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| dialogCornerRadius | 16 | 16f | ✅ Done |
| bottomSheetCornerRadius | 20 | 20f | ✅ Done |
| smallButtonHeight | 36 | 36f | ✅ Done |
| largeButtonHeight | 56 | 56f | ✅ Done |
| toolbarHeight | 56 | 56f | ✅ Done |
| dialogElevation | 8 | 8f | ✅ Done |
| bottomSheetElevation | 16 | 16f | ✅ Done |
| thickBorderWidth | 2 | 2f | ✅ Done |
| inputFieldBorderWidth | 1 | 1f | ✅ Done |
| buttonHorizontalPadding | 16 | 16f | ✅ Done |
| buttonVerticalPadding | 12 | 12f | ✅ Done |
| cardPadding | 16 | 16f | ✅ Done |
| dialogPadding | 24 | 24f | ✅ Done |
| disabledOpacity | 0.4 | 0.4f | ✅ Done |
| pressedOpacity | 0.8 | 0.8f | ✅ Done |
| dividerThickness | 1 | 1f | ✅ Done |

---

## 4. SDKLayoutConfig (theme)

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| screenTopPadding, screenBottomPadding | 16 each | 16f each | ✅ Done |
| extraSmallSpacing | 4 | 4f | ✅ Done |
| extraLargeSpacing | 32 | 32f | ✅ Done |
| sectionHeaderSpacing | 12 | 12f | ✅ Done |
| sectionContentSpacing | 8 | 8f | ✅ Done |
| betweenSectionsSpacing | 24 | 24f | ✅ Done |
| listItemSpacing | 12 | 12f | ✅ Done |
| gridItemSpacing | 16 | 16f | ✅ Done |
| gridColumns | 2 | 2 | ✅ Done |
| minTouchTargetSize | 44 | minTouchTarget 48f | ✅ Done (Android 48dp) |
| cameraOverlayHorizontalInset | 24 | 24f | ✅ Done |
| cameraOverlayVerticalInset | 80 | 80f | ✅ Done |
| faceOverlaySizeRatio | 0.7 | 0.7f (+ faceOverlaySize) | ✅ Done |

---

## 5. SDKAnimationConfig (theme)

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| enableLoadingAnimations | true | true | ✅ Done |
| enableStatusAnimations | true | true | ✅ Done |
| fastAnimationDuration | 150 | 150 | ✅ Done |
| extraLongAnimationDuration | 1000 | 1000 | ✅ Done |
| pageTransitionStyle | "slide" | "slide" | ✅ Done |
| modalTransitionStyle | "fade" | "fade" | ✅ Done |
| defaultAnimationCurve | "easeInOut" | defaultAnimationCurve + animationEasing | ✅ Done |
| buttonAnimationCurve | "spring" | "spring" | ✅ Done |
| springResponse, springDampingFraction, springBlendDuration | 0.3, 0.7, 0.3 | 0.3, 0.7, 0.3 | ✅ Done |

---

## 6. LocalizationManager

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| addOverride(key, value) | Yes | addOverride(key, value) | ✅ Done |
| removeOverride(key) | Yes | removeOverride(key) | ✅ Done |
| clearOverrides() | Yes | clearOverrides() | ✅ Done |
| overrideCount, overrideKeys | Yes | getOverrideCount(), getOverrideKeys() | ✅ Done |
| getDebugInfo | Yes | getDebugInfo() | ✅ Done |
| Per-locale lookup | string(forKey:locale:fallback:) | setLanguage + getString | Different design, OK |

---

## 7. ImageOverrideManager

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| clearOverrides(), cancelPreloading() | Yes | clearOverrides(), cancelPreloading() | ✅ Done |
| clearExpiredCache(), getCacheStatistics() | Yes | clearExpiredCache(), getCacheStatistics() | ✅ Done |
| getDebugInfo() | Yes | getDebugInfo() | ✅ Done |
| Async load API | loadImageAsync(forKey:) | Coil-based internal | OK |

---

## 8. ThemeManager (unified)

| Item | iOS | Android | Action |
|------|-----|---------|--------|
| Single entry | ThemeManager.shared (theme + config + locale + images) | ColorManager + EnhancedThemeManager | Document; optional facade later |

---

## 9. SDKResourceBundle

| Item | iOS | Android | Action |
|------|-----|---------|--------|
| Dedicated bundle type | SDKResourceBundle.shared (strings, images, URLs) | Context + getIdentifier + res/ | Different approach; no change required |

---

## 10. Documentation

| Doc | iOS | Android | Action |
|-----|-----|---------|--------|
| THEMING_GUIDE | Long; Branded Header, examples, troubleshooting | Shorter | Expand Android THEMING_GUIDE as needed |
| CLIENT_IMPLEMENTATION_GUIDE | SPM, Swift | Gradle, Kotlin | Keep both; sync high-level steps |
| CHANGELOG | CHANGELOG.md | RELEASE_NOTES_*.md | Consider CHANGELOG.md for Android |

---

## 11. Verification payload & Okta registration (parity added)

| Item | iOS | Android | Status |
|------|-----|---------|--------|
| **lastVerificationRequestPayload** | Raw JSON string of last verification request | `ArtiusIDSDK.getLastVerificationRequestPayload()` | ✅ Added |
| **lastVerificationRequestPayloadSummary** | Summary map (no full base64) | `getLastVerificationRequestPayloadSummary()`, `getVerificationRequestSummaryJSON()` | ✅ Existed |
| **captureVerificationRequestPayload** | Summary + raw | `captureVerificationRequestPayload(summary, rawPayload)` | ✅ Updated |
| **OktaRegistrationManager.registerOktaUser** | userId, userEmail, phoneNumber, memberId → backend | `ArtiusIDSDK.registerOktaUser(context, ...)` + `OktaRegistrationManager` | ✅ Added |
| **Okta registration API** | POST okta-registration (mTLS) | `ApiService.registerOkta(Map)`, approval-response base URL + mTLS | ✅ Added |
| **VerificationResult.requiresRecapture / recaptureType** | Shown in sample | `VerificationResultData.requiresRecapture`, `recaptureType`; results screen shows "Requires Recapture" / "Recapture Type" | ✅ Added |
| **Sample: Ensure certificate** | Before verification | `ArtiusIDSDK.ensureCertificateRegistered()` before verification flow | ✅ In use |
| **Sample: Send test approval request** | Button to send | `ArtiusIDSDK.sendApprovalRequest()` from sample app | ✅ In use |

---

## Summary: Integration status

All recommended parity items above are **implemented** in mobile-sdk-android. When pulling new changes from mobile-ios-sdk:

1. **SDKConfiguration** – ✅ validate(), development(), production(), debugDescription(), getFirebaseDebugInfo().
2. **EnhancedSDKThemeConfiguration** – ✅ validate(), isValid, withBrandName/withColors/withTypography, debugDescription().
3. **SDKComponentStyling** – ✅ All iOS fields (dialog, bottom sheet, padding, opacity, divider, etc.).
4. **SDKLayoutConfig** – ✅ All spacing, section, grid, overlay inset fields; minTouchTarget 48dp.
5. **SDKAnimationConfig** – ✅ Loading/status toggles, transition styles, curves, spring params.
6. **LocalizationManager** – ✅ addOverride, removeOverride, clearOverrides, getOverrideCount, getOverrideKeys, getDebugInfo.
7. **ImageOverrideManager** – ✅ clearOverrides, cancelPreloading, clearExpiredCache, getCacheStatistics, getDebugInfo.

**Next:** When iOS adds new APIs or config, add a row to the relevant section above with status "Action", implement in Android, then set status to "✅ Done".

**Approval punch list:** For a detailed, itemized list of changes needed to make Android an exact doppelganger of the iOS SDK and sample app (public API, config, theme, sample app UX), see **[IOS_ANDROID_PUNCHLIST.md](IOS_ANDROID_PUNCHLIST.md)**. Use that document to approve and track each change.
