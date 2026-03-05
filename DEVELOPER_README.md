# ArtiusID Android SDK – Developer Guide

**For internal developers.** Client-facing docs: [README.md](README.md).

---

## Quick start

```bash
git clone git@gitlab.com:artiusid1/mobile-sdk-android.git
cd mobile-sdk-android
./gradlew :sample-app:installDebug   # Build and install sample app
```

- **SDK build:** `./gradlew :artiusid-sdk:assembleRelease`
- **Sample app:** [sample-app/README.md](sample-app/README.md)
- **Full build & CI:** [BUILD_GUIDE.md](BUILD_GUIDE.md)
- **Contributing & branching:** [CONTRIBUTING.md](CONTRIBUTING.md)

---

## Integrating changes from mobile-ios-sdk (GitLab)

**iOS repo (local):** Clone `mobile-sdk-ios` from GitLab and open it alongside this repo, e.g. `../mobile-sdk-ios` or `~/Documents/mobile-sdk-ios`.

When the **mobile-ios-sdk** repo (or its sample-app) is updated:

1. **Parity checklist** – Use [docs/IOS_ANDROID_PARITY.md](docs/IOS_ANDROID_PARITY.md):
   - Lists iOS vs Android feature parity.
   - Workflow: review iOS changes → port to Android → update parity tables.
   - Mark new items as "Action", then "✅ Done" when implemented.
2. **Code comments** – Use `// iOS parity: <description>` where behavior intentionally matches iOS.
3. **Docs to sync** – [docs/client/CLIENT_IMPLEMENTATION_GUIDE.md](docs/client/CLIENT_IMPLEMENTATION_GUIDE.md) (high-level steps), [THEMING_GUIDE.md](THEMING_GUIDE.md), and release notes under `docs/client/RELEASE_NOTES_*.md`.
4. **Sample app** – If iOS sample app gets new flows (e.g. Okta, approval, theme), mirror in `sample-app` and note in sample-app README.

---

## Release process

1. **Version** – Update with `./artiusid-sdk/scripts/version-manager.sh` (or edit version in build files).
2. **Build** – `./gradlew clean && ./gradlew :artiusid-sdk:assembleRelease`.
3. **Docs** – Add `docs/client/RELEASE_NOTES_vX.X.XX.md`; update [CHANGELOG.md](CHANGELOG.md) if used.
4. **Commit & tag** – e.g. `git tag -a v1.2.XX -m "Release v1.2.XX"` and push.
5. **Publish** – Use `artiusid-sdk/scripts/publish-android-github-essential.sh` (or your standard publish flow).

Details: [CONTRIBUTING.md – Creating a Release](CONTRIBUTING.md).

---

## Key docs

| Doc | Purpose |
|-----|---------|
| [BUILD_GUIDE.md](BUILD_GUIDE.md) | Build commands, variants, CI, integration testing |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Branching, PRs, release, security |
| [docs/IOS_ANDROID_PARITY.md](docs/IOS_ANDROID_PARITY.md) | iOS ↔ Android parity and integration workflow |
| [docs/IOS_ANDROID_PUNCHLIST.md](docs/IOS_ANDROID_PUNCHLIST.md) | **Approval punch list** – itemized changes for exact iOS doppelganger |
| [docs/client/CLIENT_IMPLEMENTATION_GUIDE.md](docs/client/CLIENT_IMPLEMENTATION_GUIDE.md) | Client integration (Firebase, SDK init, etc.) |
| [THEMING_GUIDE.md](THEMING_GUIDE.md) | Theme/localization and iOS parity |
| [HILT_INTEGRATION_GUIDE.md](HILT_INTEGRATION_GUIDE.md) | HILT/DI setup for host apps |
| [CHANGELOG.md](CHANGELOG.md) | Version history and links to release notes |
