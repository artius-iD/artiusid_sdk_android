# ArtiusID Android SDK v1.2.55 - Release Notes

**Release Date:** March 2026  
**Author:** artius.iD, Inc.

---

## iOS parity (2.0.138 / 2.0.139)

- **Approval result display (iOS 2.0.138):** Sample app approval result card now shows localized **"Approved"** or **"Declined"** (short form) instead of "Request Approved"/"Request Denied", with card title **"Approval Request Result"**. Added `sample_approved` and `sample_declined` strings in en, es, fr, de.
- **SDK version (iOS 2.0.139):** Already exposed via `ArtiusIDSDK.getSdkVersion()` and in Settings info; no change required.

## Sample app polish

- **artius.iD Default theme:** Primary button and icons use orange (#F58220) instead of blue; text and icons remain readable.
- **Settings:** All labels and dropdowns (Language, Environment, Domain) use dark text on white cards; no white-on-white.
- **Image Overrides:** Selection shown with filled circle (selected) / empty circle (unselected); number badges removed.
