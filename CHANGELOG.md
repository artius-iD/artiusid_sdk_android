# Changelog – ArtiusID Android SDK

High-level version history. Full details in [docs/client/RELEASE_NOTES_*.md](docs/client/).

| Version | Date | Summary |
|---------|------|---------|
| [v1.2.55](docs/client/RELEASE_NOTES_v1.2.55.md) | March 2026 | iOS 2.0.138 parity: Sample app approval result card shows localized "Approved"/"Declined" (short) with title "Approval Request Result". SDK version already exposed via `ArtiusIDSDK.getSdkVersion()` (iOS 2.0.139). |
| [v1.2.54](docs/client/RELEASE_NOTES_v1.2.54.md) | March 2026 | iOS parity punch list: API (biometric, FCM set/get, env mapping, authenticate(request), ensureCertificateRegisteredOrThrow, listeners), config (URL template, copyWithFcmToken/Logging), theme (paragraphSpacing, IconCategory), LocalizationKeys (settings_*, sample_*), sample app strings (en/es/fr/de) |
| [v1.2.53](docs/client/RELEASE_NOTES_v1.2.53.md) | March 2026 | ThemeManager, LocalizationManager, SDKResourceBundle (iOS parity) |
| [v1.2.52](docs/client/RELEASE_NOTES_v1.2.52.md) | — | Host app integration fixes (Compose/BOM) |
| [v1.2.51](docs/client/RELEASE_NOTES_v1.2.51.md) | Feb 2026 | ApprovalRequestResult, getCurrentFCMToken, SDKConfiguration, AuthenticationResult (iOS parity) |
| [v1.2.50](docs/client/RELEASE_NOTES_v1.2.50.md) | Feb 2026 | VerificationResult/recapture, Okta & AppConstants config, Test Authentication Request (iOS parity) |
| [v1.2.49](docs/client/RELEASE_NOTES_v1.2.49.md) | — | iOS parity: mTLS clear on env switch, Okta user ID, re-verification, NFC reset |
| [v1.2.48](docs/client/RELEASE_NOTES_v1.2.48.md) | Oct 2025 | Firebase architecture change (client-owned FMS), production-ready verification/recapture |

**Integrating iOS changes:** See [docs/IOS_ANDROID_PARITY.md](docs/IOS_ANDROID_PARITY.md) and [DEVELOPER_README.md](DEVELOPER_README.md).
