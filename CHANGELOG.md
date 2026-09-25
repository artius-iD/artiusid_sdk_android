# Changelog – ArtiusID Android SDK

High-level version history. Full details in [docs/client/RELEASE_NOTES_*.md](docs/client/).

| Version | Date | Summary |
|---------|------|---------|
| [v1.4.3](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.4.3) | September 2026 | `verifyEnrolledAccount(context)` account-existence check (`ACTIVE` / `INACTIVE` / `UNREACHABLE`). Session binding: a lock caused by the phone being away is reported as such, so a paired browser shows a phone-away notice and resumes when the phone returns. |
| [v1.4.2](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.4.2) | September 2026 | Corrected the Closed and Terminated session-status values to match the service (Terminated = 5, Closed = 6). |
| [v1.4.1](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.4.1) | September 2026 | Enrollment retries go to the correct capture step; a failing document image, low face match or failed identity check is reported as a failure rather than a success. |
| [v1.4.0](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.4.0) | September 2026 | Real-time binding-session status over a WebSocket heartbeat (`connectBindingWebSocket`, `bindingSessionStatusFlow`/`onBindingSessionStatusChange`, automatic reconnect via `BindingWebSocketConfig`) and separate REST/WebSocket client certificates (`CertificatePurpose`, `ensureCertificateRegistered(context, purpose)`). Existing REST behavior unchanged; brings the binding-session API in line with iOS. |
| [v1.3.2](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.3.2) | September 2026 | Fixed the binding-session gateway URL under template configuration (could resolve to a non-existent host, failing binding responses). Enrollment/verification unaffected. |
| [v1.3.1](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.3.1) | September 2026 | Reduced the public API surface: the presence monitor's internal classes are no longer part of the public API. Host-facing binding, enrollment and presence APIs unchanged. |
| [v1.3.0](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.3.0) | September 2026 | Session binding for host apps (`AppNotificationState`, `ArtiusIDSDK.sendBindingResponse`). The binding, enrollment-state and sign-in result types keep their names in the minified AAR. Includes the fixes from 1.2.56 through 1.2.63. |
| [v1.2.56 – v1.2.63](https://github.com/artius-iD/artiusid_sdk_android/releases) | March 2026 | Maintenance releases. |
| [v1.2.55](docs/client/RELEASE_NOTES_v1.2.55.md) | March 2026 | iOS 2.0.138 parity: Sample app approval result card shows localized "Approved"/"Declined" (short) with title "Approval Request Result". SDK version already exposed via `ArtiusIDSDK.getSdkVersion()` (iOS 2.0.139). |
| [v1.2.54](docs/client/RELEASE_NOTES_v1.2.54.md) | March 2026 | iOS parity punch list: API (biometric, FCM set/get, env mapping, authenticate(request), ensureCertificateRegisteredOrThrow, listeners), config (URL template, copyWithFcmToken/Logging), theme (paragraphSpacing, IconCategory), LocalizationKeys (settings_*, sample_*), sample app strings (en/es/fr/de) |
| [v1.2.53](docs/client/RELEASE_NOTES_v1.2.53.md) | March 2026 | ThemeManager, LocalizationManager, SDKResourceBundle (iOS parity) |
| [v1.2.52](docs/client/RELEASE_NOTES_v1.2.52.md) | — | Host app integration fixes (Compose/BOM) |
| [v1.2.51](docs/client/RELEASE_NOTES_v1.2.51.md) | Feb 2026 | ApprovalRequestResult, getCurrentFCMToken, SDKConfiguration, AuthenticationResult (iOS parity) |
| [v1.2.50](docs/client/RELEASE_NOTES_v1.2.50.md) | Feb 2026 | VerificationResult/recapture, Okta & AppConstants config, Test Authentication Request (iOS parity) |
| [v1.2.49](docs/client/RELEASE_NOTES_v1.2.49.md) | — | iOS parity: mTLS clear on env switch, Okta user ID, re-verification, NFC reset |
| [v1.2.48](docs/client/RELEASE_NOTES_v1.2.48.md) | Oct 2025 | Firebase architecture change (client-owned FMS), production-ready verification/recapture |

For integration instructions, see [README.md](README.md).
