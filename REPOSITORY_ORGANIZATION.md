# ArtiusID SDK Repository Organization

**Repository structure and organization guide for internal developers**

---

## 📋 Overview

This repository is organized for **internal artiusID developer use**. It contains:
- Complete SDK source code
- Sample application for testing
- Development tools and scripts
- Documentation for both internal and client use

---

## 🗂️ Repository Structure

### **Root Level Files**

```
mobile-sdk-android/
│
├── 📘 Documentation (Internal - for SDK developers)
│   ├── DEVELOPER_README.md              ⭐ START HERE - Main development guide
│   ├── QUICKSTART_INTERNAL.md           ⚡ Quick start for new developers (15 min)
│   ├── BUILD_GUIDE.md                   🔨 How to build the SDK
│   ├── CONTRIBUTING.md                  🤝 Contribution guidelines
│   ├── DOCUMENTATION_INDEX.md           📚 Complete documentation index
│   ├── QUICK_REFERENCE.md               💡 Quick reference card
│   └── REPOSITORY_ORGANIZATION.md       📁 This file
│
├── 📗 Documentation (Shared - internal + client)
│   ├── SDK_DEPENDENCY_REQUIREMENTS.md   Dependencies and versions
│   ├── HILT_INTEGRATION_GUIDE.md        HILT setup guide
│   ├── README_HILT_SETUP.md            Quick HILT reference
│   ├── TRINET_CERTIFICATE_ARCHITECTURE_UPDATE.md  Certificate system
│   └── TRINET_COMMUNICATION_v1.2.15.md  Historical reference
│
├── 📙 Documentation (Client-facing)
│   └── README.md                        Public-facing SDK documentation
│
├── 🔧 Build Configuration
│   ├── build.gradle                     Root build configuration
│   ├── settings.gradle                  Project settings
│   ├── gradle.properties                Gradle optimization settings
│   ├── local.properties                 Local SDK paths (not tracked)
│   └── .gitignore                       Git ignore rules
│
├── 🛠️ Scripts & Tools
│   ├── setup_hilt.sh                    Automated HILT setup
│   ├── hilt_diagnostic_script.gradle    HILT diagnostics
│   ├── cleanup-sdk-repository.sh        Repository cleanup
│   ├── gradlew                          Gradle wrapper (Unix)
│   └── gradlew.bat                      Gradle wrapper (Windows)
│
├── 📦 Modules
│   ├── artiusid-sdk/                    SDK module (see below)
│   └── sample-app/                      Sample application (see below)
│
└── 📁 Additional Directories
    ├── docs/                            Organized documentation
    │   ├── README.md                    Documentation index
    │   └── client/                      Client-facing docs
    ├── gradle/                          Gradle wrapper files
    ├── .gradle/                         Gradle cache (not tracked)
    └── build/                           Build outputs (not tracked)
```

---

## 📦 SDK Module (artiusid-sdk/)

### **Structure**

```
artiusid-sdk/
│
├── 🔧 Configuration
│   ├── build.gradle                     SDK build configuration
│   ├── proguard-rules.pro              ProGuard obfuscation rules
│   └── consumer-rules.pro              Rules for client apps
│
├── 📜 Source Code
│   └── src/main/
│       ├── AndroidManifest.xml          SDK manifest
│       │
│       ├── java/com/artiusid/sdk/       Kotlin source code
│       │   ├── 🎯 Core
│       │   │   ├── ArtiusIDSDK.kt               Main SDK interface
│       │   │   ├── config/                      Configuration classes
│       │   │   └── models/                      Data models
│       │   │
│       │   ├── 🔐 Security & Auth
│       │   │   ├── certificate/                 mTLS certificate management
│       │   │   ├── credential/                  Credential storage
│       │   │   └── keychain/                    Secure storage (Android Keystore)
│       │   │
│       │   ├── 🌐 Networking
│       │   │   ├── api/                         API service interfaces
│       │   │   ├── services/                    Network services
│       │   │   └── utils/                       Network utilities
│       │   │
│       │   ├── 🎨 UI Components
│       │   │   ├── ui/                          Compose UI screens
│       │   │   ├── viewmodels/                  ViewModels
│       │   │   ├── components/                  Reusable UI components
│       │   │   └── theme/                       Theming system
│       │   │
│       │   ├── 📱 Features
│       │   │   ├── verification/                Verification flow
│       │   │   ├── authentication/              Authentication flow
│       │   │   ├── approval/                    Approval requests
│       │   │   ├── face/                        Face detection/liveness
│       │   │   ├── document/                    Document scanning
│       │   │   └── nfc/                         NFC passport reading
│       │   │
│       │   ├── 🔥 Firebase
│       │   │   ├── FirebaseTokenManager.kt      FCM token management
│       │   │   └── utils/                       Firebase utilities
│       │   │
│       │   ├── 🌍 Environment
│       │   │   └── EnvironmentCredentialManager.kt  Per-environment credentials
│       │   │
│       │   └── 📚 Documentation
│       │       └── documentation/               Technical architecture docs
│       │
│       ├── assets/                      Binary assets
│       │   ├── models/                  ML models (face detection/recognition)
│       │   └── trusted_cert.pem         Root certificate for mTLS
│       │
│       └── res/                         Resources
│           ├── drawable/                Images (overlays, icons, animations)
│           ├── drawable-*/              Density-specific images
│           ├── font/                    Custom fonts (Gotham, Lato)
│           ├── raw/                     Audio files, GIF animations
│           ├── values/                  Strings, colors, styles
│           ├── values-en/               English strings
│           ├── values-es/               Spanish strings
│           └── xml/                     XML resources
│
├── 🛠️ Scripts
│   └── scripts/
│       ├── version-manager.sh           Version bumping utility
│       └── publish-android-github-essential.sh  GitHub publishing
│
└── 📦 Build Output (not tracked)
    └── build/
        └── outputs/
            ├── aar/                     Built AAR files
            └── mapping/                 ProGuard mapping files
```

---

## 📱 Sample App Module (sample-app/)

### **Structure**

```
sample-app/
│
├── 🔧 Configuration
│   ├── build.gradle                     App build configuration
│   ├── google-services.json            Firebase configuration
│   ├── proguard-rules-customer.pro     Customer ProGuard rules
│   └── proguard-rules-obfuscated.pro   Obfuscation rules
│
├── 📚 Documentation
│   ├── README.md                        ⭐ Sample app guide
│   └── LOCALIZATION_GUIDE.md           String customization guide
│
├── 📜 Source Code
│   └── src/main/
│       ├── AndroidManifest.xml          App manifest
│       │
│       ├── java/com/artiusid/sample/   Kotlin source code
│       │   ├── BridgeMainActivity.kt            Main activity
│       │   ├── SampleApplication.kt             Application class (Hilt)
│       │   ├── SampleFirebaseMessagingService.kt  Firebase service
│       │   ├── VerificationResultsScreen.kt     Results screen
│       │   │
│       │   ├── config/
│       │   │   ├── AppUrlConfig.kt              Environment URLs
│       │   │   └── SampleImageOverrides.kt      Image overrides
│       │   │
│       │   ├── localization/
│       │   │   └── SampleAppLocalization.kt     String overrides
│       │   │
│       │   └── theme/
│       │       └── SampleAppThemes.kt           Theme configurations
│       │
│       ├── assets/                      Asset overrides
│       │   ├── README.md                        Asset documentation
│       │   ├── test_override_working.txt        Test file
│       │   ├── corporate/               Corporate theme assets
│       │   │   ├── README.md
│       │   │   ├── animations/          GIF animations
│       │   │   └── images/              PNG images
│       │   └── modern/                  Modern theme assets
│       │       └── README.md
│       │
│       └── res/                         Resources
│           ├── mipmap-*/                App icons
│           ├── raw/                     (Empty)
│           ├── values/                  Colors, strings, themes
│           ├── values-night/            Dark theme
│           └── xml/                     Backup rules, shortcuts
│
└── 📦 Build Output (not tracked)
    └── build/
        └── outputs/
            └── apk/                     Built APK files
                ├── debug/
                ├── release/
                └── customerDistribution/
```

---

## 📁 Documentation Directory (docs/)

### **Structure**

```
docs/
│
├── README.md                            Documentation organization guide
│
└── client/                              Client-facing documentation
    ├── CLIENT_IMPLEMENTATION_GUIDE.md   Client integration guide
    ├── RELEASE_NOTES_v1.2.48.md        Latest release notes
    ├── DEPLOYMENT_SUMMARY_v1.2.48.md   Deployment information
    └── TRINET_DEPLOYMENT_EMAIL.md      Client communication example
```

**Purpose:** Separates client-facing documentation from internal development docs.

---

## 🔍 Finding Things

### **I need to...**

| Task | Location |
|------|----------|
| **Find SDK source code** | `artiusid-sdk/src/main/java/com/artiusid/sdk/` |
| **Find verification logic** | `artiusid-sdk/src/main/java/com/artiusid/sdk/viewmodels/VerificationProcessingViewModel.kt` |
| **Find certificate code** | `artiusid-sdk/src/main/java/com/artiusid/sdk/certificate/` |
| **Find API services** | `artiusid-sdk/src/main/java/com/artiusid/sdk/api/` |
| **Find UI screens** | `artiusid-sdk/src/main/java/com/artiusid/sdk/ui/` |
| **Find ML models** | `artiusid-sdk/src/main/assets/models/` |
| **Find sample app code** | `sample-app/src/main/java/com/artiusid/sample/` |
| **Find Firebase service** | `sample-app/src/main/java/com/artiusid/sample/SampleFirebaseMessagingService.kt` |
| **Find asset overrides** | `sample-app/src/main/assets/` |
| **Find client docs** | `docs/client/` |
| **Find build output (SDK)** | `artiusid-sdk/build/outputs/aar/` |
| **Find build output (App)** | `sample-app/build/outputs/apk/` |

---

## 📝 File Naming Conventions

### **Documentation**

| Pattern | Purpose | Example |
|---------|---------|---------|
| `ALLCAPS.md` | Major documentation | `README.md`, `CONTRIBUTING.md` |
| `ALLCAPS_TOPIC.md` | Specific guides | `BUILD_GUIDE.md`, `QUICK_REFERENCE.md` |
| `RELEASE_NOTES_vX.X.XX.md` | Release notes | `RELEASE_NOTES_v1.2.48.md` |
| `DEPLOYMENT_SUMMARY_vX.X.XX.md` | Deployment docs | `DEPLOYMENT_SUMMARY_v1.2.48.md` |

### **Source Code**

| Pattern | Purpose | Example |
|---------|---------|---------|
| `PascalCase.kt` | Classes | `VerificationManager.kt` |
| `camelCase.kt` | Files with utils | `certificateUtils.kt` |
| `PascalCaseScreen.kt` | Compose screens | `VerificationScreen.kt` |
| `PascalCaseViewModel.kt` | ViewModels | `VerificationProcessingViewModel.kt` |

### **Configuration**

| Pattern | Purpose | Example |
|---------|---------|---------|
| `lowercase.gradle` | Gradle files | `build.gradle`, `settings.gradle` |
| `lowercase.properties` | Properties | `gradle.properties`, `local.properties` |
| `lowercase-rules.pro` | ProGuard | `proguard-rules.pro` |
| `kebab-case.json` | JSON configs | `google-services.json` |

---

## 🔒 What's Tracked vs Not Tracked

### **Tracked in Git (Committed)**

✅ Source code (`.kt`, `.java`)  
✅ Resources (`.xml`, `.png`, fonts)  
✅ Documentation (`.md`)  
✅ Configuration (`.gradle`, `.properties` except `local.properties`)  
✅ Scripts (`.sh`)  
✅ Assets (ML models, certificates, animations)  
✅ Firebase config (`google-services.json`) - **for internal use**  
✅ ProGuard rules (`.pro`)

### **Not Tracked (Gitignored)**

❌ Build outputs (`build/`, `*.apk`, `*.aar`)  
❌ Gradle cache (`.gradle/`)  
❌ IDE files (`.idea/`, `*.iml`)  
❌ Local config (`local.properties`)  
❌ Temporary files (`*.log`, `.DS_Store`)  
❌ Distribution archives (`*.tar.gz`)

---

## 🎯 Organization Principles

### **1. Separation of Concerns**
- **SDK code** in `artiusid-sdk/`
- **Sample app** in `sample-app/`
- **Documentation** organized by audience
- **Scripts** in dedicated locations

### **2. Clear Documentation Hierarchy**
- **Internal docs** at root level
- **Client docs** in `docs/client/`
- **Module docs** in respective modules
- **Index files** for navigation

### **3. Logical Grouping**
- **Related code** together (e.g., all certificate code in `certificate/`)
- **Similar files** in same directory (e.g., all ViewModels in `viewmodels/`)
- **Documentation** near what it documents

### **4. Consistent Naming**
- **PascalCase** for classes
- **camelCase** for functions/variables
- **UPPER_SNAKE_CASE** for constants
- **kebab-case** for files/directories

---

## 💡 Best Practices

### **For Developers**

1. **Know the structure** - Understand where things belong
2. **Follow conventions** - Use established patterns
3. **Document changes** - Update relevant docs
4. **Keep organized** - Don't create random files

### **Adding New Code**

1. **Choose right location** based on functionality
2. **Follow existing patterns** in that area
3. **Update documentation** if needed
4. **Add tests** in appropriate location

### **Creating Documentation**

1. **Choose audience** (internal vs client)
2. **Pick right location** (root vs `docs/client/`)
3. **Follow naming conventions**
4. **Link from index files**

---

## 🔄 Regular Maintenance

### **What to Keep Clean**

- [ ] Remove obsolete documentation
- [ ] Archive old release notes (keep recent 5 versions)
- [ ] Clean up unused assets
- [ ] Update version references
- [ ] Verify links in documentation

### **When Releasing**

- [ ] Create version-specific docs in `docs/client/`
- [ ] Update main `README.md` with new version
- [ ] Update `DEVELOPER_README.md` version history
- [ ] Archive previous deployment summaries if needed

---

## 📞 Questions?

If you're unsure where something belongs:
1. Check this guide
2. Look at similar existing files
3. Ask the team
4. Follow the principle of least surprise

---

**This organization makes the repository maintainable and intuitive for all developers.**

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

