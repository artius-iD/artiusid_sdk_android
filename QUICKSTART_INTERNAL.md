# ArtiusID SDK - Quick Start for New Developers

**Get up and running in 15 minutes**

---

## 👋 Welcome!

This guide will get you set up and productive as quickly as possible.

---

## ⚡ Speed Run (5 Minutes)

```bash
# 1. Clone the repository
git clone git@gitlab.com:artiusid1/mobile-sdk-android.git
cd mobile-sdk-android

# 2. Open in Android Studio
# File → Open → Select 'mobile-sdk-android' directory
# Wait for Gradle sync (~2 minutes)

# 3. Run the sample app
# Select "sample-app" from run configurations
# Click Run ▶️
```

**Done!** You should see the sample app running.

---

## 🎯 Your First Day

### **Morning: Orientation**

**1. Read the Developer README** (30 minutes)
- [DEVELOPER_README.md](DEVELOPER_README.md) - Your main resource

**2. Understand the Structure** (15 minutes)
```
mobile-sdk-android/
├── artiusid-sdk/          # The SDK itself (you'll work here)
├── sample-app/            # Test application (use this to test)
├── docs/                  # Documentation
└── scripts/               # Build and deployment tools
```

**3. Explore the Sample App** (30 minutes)
- Run the sample app
- Try verification flow
- Test authentication
- Send a test approval request

See [sample-app/README.md](sample-app/README.md) for details.

### **Afternoon: First Task**

**1. Find a Good First Issue**
- Look for issues labeled `good-first-issue` in GitLab
- Or ask your team lead for a starter task

**2. Create a Feature Branch**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/my-first-feature
```

**3. Make Your Change**
- Edit files in `artiusid-sdk/src/main/java/com/artiusid/sdk/`
- Test with the sample app
- Follow the [contribution guidelines](CONTRIBUTING.md)

**4. Submit Your First MR**
```bash
git add .
git commit -m "[feat] My first feature"
git push origin feature/my-first-feature
# Create merge request in GitLab
```

---

## 🛠️ Essential Setup

### **1. IDE Configuration**

**Android Studio Settings:**
- Preferences → Editor → Code Style → Kotlin
  - Set line length: 120
  - Set indent: 4 spaces
  - Enable format on save

**Recommended Plugins:**
- Kotlin
- Jetpack Compose (should be included)
- GitLab Integration
- Rainbow Brackets

### **2. Git Configuration**

```bash
# Set your name and email
git config user.name "Your Name"
git config user.email "your.email@artiusid.com"

# Use SSH (recommended)
git remote set-url origin git@gitlab.com:artiusid1/mobile-sdk-android.git
```

### **3. Local Properties**

Create `local.properties` (if not exists):

```properties
sdk.dir=/Users/your-username/Library/Android/sdk
```

---

## 🧭 Navigation Guide

### **Where is...?**

| Looking for... | Location |
|----------------|----------|
| **SDK source code** | `artiusid-sdk/src/main/java/com/artiusid/sdk/` |
| **Verification logic** | `artiusid-sdk/src/main/java/com/artiusid/sdk/viewmodels/VerificationProcessingViewModel.kt` |
| **Certificate handling** | `artiusid-sdk/src/main/java/com/artiusid/sdk/certificate/` |
| **API calls** | `artiusid-sdk/src/main/java/com/artiusid/sdk/api/` |
| **UI screens** | `artiusid-sdk/src/main/java/com/artiusid/sdk/ui/` |
| **Sample app** | `sample-app/src/main/java/com/artiusid/sample/` |
| **Assets** | `artiusid-sdk/src/main/res/` |
| **ML models** | `artiusid-sdk/src/main/assets/models/` |

### **What does it do?**

| Component | Purpose |
|-----------|---------|
| **ArtiusIDSDK** | Main SDK interface - initialization and flow entry points |
| **VerificationProcessingViewModel** | Orchestrates verification: face detection, document scan, NFC, submission |
| **CertificateManager** | Handles mTLS certificates for secure communication |
| **FirebaseTokenManager** | Manages FCM tokens for notifications |
| **EnvironmentCredentialManager** | Isolates credentials per environment |
| **ApiService** | Retrofit interface for backend API calls |

---

## 🔧 Common Tasks

### **Build the SDK**

```bash
# Debug build (fast, with symbols)
./gradlew :artiusid-sdk:assembleDebug

# Release build (slow, obfuscated)
./gradlew :artiusid-sdk:assembleRelease
```

### **Test Your Changes**

```bash
# 1. Build the SDK
./gradlew :artiusid-sdk:assembleDebug

# 2. Build and install sample app
./gradlew :sample-app:installDebug

# 3. Watch logs
adb logcat | grep -E "ArtiusIDSDK|Sample"
```

### **Run Lint Checks**

```bash
./gradlew lint
```

### **Clear and Rebuild**

```bash
./gradlew clean
./gradlew assemble
```

---

## 🐛 Debugging Tips

### **Enable Verbose Logging**

In `BridgeMainActivity.kt`:

```kotlin
val sdkConfig = SDKConfiguration(
    // ...
    enableLogging = true  // Enable detailed logs
)
```

### **View Logs**

```bash
# All SDK logs
adb logcat | grep ArtiusIDSDK

# Specific component
adb logcat | grep VerificationProcessingVM

# Clear logcat first (useful)
adb logcat -c
adb logcat | grep ArtiusIDSDK
```

### **Clear App Data**

```bash
adb shell pm clear com.artiusid.sampleapp
```

### **Uninstall and Reinstall**

```bash
adb uninstall com.artiusid.sampleapp
./gradlew :sample-app:installDebug
```

---

## 📚 Essential Reading

### **Must Read (Day 1)**
1. [DEVELOPER_README.md](DEVELOPER_README.md) - Complete development guide
2. [sample-app/README.md](sample-app/README.md) - How to use the sample app
3. [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute

### **Should Read (Week 1)**
4. [BUILD_GUIDE.md](BUILD_GUIDE.md) - How to build from source
5. [SDK_DEPENDENCY_REQUIREMENTS.md](SDK_DEPENDENCY_REQUIREMENTS.md) - Dependencies
6. [docs/client/CLIENT_IMPLEMENTATION_GUIDE.md](docs/client/CLIENT_IMPLEMENTATION_GUIDE.md) - Client integration (understand client perspective)

### **Nice to Read (Month 1)**
7. Architecture docs in `artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/`
8. [HILT_INTEGRATION_GUIDE.md](HILT_INTEGRATION_GUIDE.md) - Dependency injection
9. Release notes in `docs/client/` - Learn what changed over time

---

## 🎓 Learning Path

### **Week 1: Orientation**
- [ ] Set up development environment
- [ ] Run the sample app
- [ ] Read essential documentation
- [ ] Complete a small bug fix or documentation update

### **Week 2: Exploration**
- [ ] Understand verification flow
- [ ] Understand authentication flow
- [ ] Learn about certificate management
- [ ] Test in all three environments

### **Week 3: Contribution**
- [ ] Complete a feature implementation
- [ ] Write or improve documentation
- [ ] Review someone else's code
- [ ] Help with testing

### **Month 1: Proficiency**
- [ ] Understand overall architecture
- [ ] Know where to find things
- [ ] Can build and test independently
- [ ] Can help onboard the next developer

---

## 💡 Pro Tips

### **Development Workflow**

1. **Always work on `develop` branch**, not `main`
2. **Test in sample app** before submitting MR
3. **Run lint** before committing: `./gradlew lint`
4. **Keep commits small** - easier to review
5. **Write good commit messages** - see [CONTRIBUTING.md](CONTRIBUTING.md)

### **IDE Tips**

- **Cmd+Shift+A** (Mac) / **Ctrl+Shift+A** (Win) - Find any action
- **Cmd+O** - Find class
- **Cmd+Shift+O** - Find file
- **Cmd+Alt+L** - Format code
- **Cmd+/** - Comment/uncomment line

### **Debugging**

- Use Android Studio's debugger with breakpoints
- Check logcat with filters (set to "Debug" level)
- Use "Analyze APK" to inspect AAR files
- Use Android Profiler for performance issues

### **Testing**

- Test in Sandbox first, always
- Test environment switching
- Test with cleared app data (fresh state)
- Test on physical device (not just emulator)

---

## ❓ FAQ

### **Q: How do I test my changes?**
Build the SDK (`./gradlew :artiusid-sdk:assembleDebug`), then build and run the sample app.

### **Q: Where do I find the SDK version?**
In `artiusid-sdk/build.gradle`, look for `versionName`.

### **Q: How do I switch environments in the sample app?**
Use the dropdown at the top of the main screen.

### **Q: The app crashes on startup. What do I do?**
1. Check `google-services.json` exists in `sample-app/`
2. Clear app data: `adb shell pm clear com.artiusid.sampleapp`
3. Check logs: `adb logcat | grep ArtiusIDSDK`

### **Q: How do I add a new dependency?**
Add to `artiusid-sdk/build.gradle` in the `dependencies` block. Sync Gradle.

### **Q: Where are credentials stored?**
In EncryptedSharedPreferences, managed by various manager classes (see `artiusid-sdk/src/main/java/com/artiusid/sdk/credential/`).

### **Q: How do I create a release?**
See [DEVELOPER_README.md - Release Process](DEVELOPER_README.md#-release-process).

---

## 🤝 Getting Help

### **Got Questions?**
- **Team lead:** Ask your assigned mentor
- **GitLab Issues:** Search existing issues or create new one
- **Documentation:** Check DEVELOPER_README.md first
- **Team chat:** Ask in the SDK team channel

### **Stuck?**
Don't spend more than 30 minutes stuck on something. Ask for help!

---

## ✅ Day 1 Checklist

Before you leave your first day:

- [ ] Development environment set up
- [ ] Sample app runs successfully
- [ ] Can build the SDK (`./gradlew :artiusid-sdk:assembleDebug`)
- [ ] Completed a verification flow in sample app
- [ ] Read DEVELOPER_README.md
- [ ] Joined team communication channels
- [ ] Know who to ask for help

---

## 🎉 Next Steps

**You're ready to start contributing!**

1. **Find your first task** - Ask your team lead or check GitLab issues
2. **Create a branch** - `git checkout -b feature/my-task`
3. **Make your changes** - Edit code in `artiusid-sdk/`
4. **Test** - Run sample app and verify it works
5. **Submit MR** - Push and create merge request in GitLab

**Welcome to the team! 🚀**

---

## 📞 Resources

| Resource | Link |
|----------|------|
| **Developer Guide** | [DEVELOPER_README.md](DEVELOPER_README.md) |
| **Build Guide** | [BUILD_GUIDE.md](BUILD_GUIDE.md) |
| **Contributing** | [CONTRIBUTING.md](CONTRIBUTING.md) |
| **Sample App** | [sample-app/README.md](sample-app/README.md) |
| **GitLab** | git@gitlab.com:artiusid1/mobile-sdk-android.git |
| **GitHub** | https://github.com/artius-iD/artiusid_sdk_android |

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

**Questions?** Ask your team lead or the SDK team!

