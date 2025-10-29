# ArtiusID SDK - Quick Reference Card

**Essential commands and locations at a glance**

---

## ⚡ Quick Commands

### **Build SDK**
```bash
./gradlew :artiusid-sdk:assembleRelease
```
Output: `artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar`

### **Run Sample App**
```bash
./gradlew :sample-app:installDebug
```

### **View Logs**
```bash
adb logcat | grep ArtiusIDSDK
```

### **Clear App Data**
```bash
adb shell pm clear com.artiusid.sampleapp
```

### **Lint Check**
```bash
./gradlew lint
```

### **Clean Build**
```bash
./gradlew clean && ./gradlew assemble
```

---

## 📚 Essential Docs

| Need | Document | Time |
|------|----------|------|
| **Get started** | [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md) | 15min |
| **Full guide** | [DEVELOPER_README.md](DEVELOPER_README.md) | 30min |
| **Build SDK** | [BUILD_GUIDE.md](BUILD_GUIDE.md) | 20min |
| **Contribute** | [CONTRIBUTING.md](CONTRIBUTING.md) | 15min |
| **Test** | [sample-app/README.md](sample-app/README.md) | 20min |
| **Find anything** | [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | 5min |

---

## 📁 Key Locations

### **SDK Source Code**
```
artiusid-sdk/src/main/java/com/artiusid/sdk/
```

### **Verification Logic**
```
artiusid-sdk/src/main/java/com/artiusid/sdk/viewmodels/VerificationProcessingViewModel.kt
```

### **Sample App**
```
sample-app/src/main/java/com/artiusid/sample/
```

### **Build Output (SDK)**
```
artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
```

### **Build Output (Sample App)**
```
sample-app/build/outputs/apk/debug/sample-app-debug.apk
```

---

## 🔧 Common Tasks

### **Build Release**
```bash
# 1. Update version
./artiusid-sdk/scripts/version-manager.sh

# 2. Clean and build
./gradlew clean
./gradlew :artiusid-sdk:assembleRelease

# 3. Copy AAR
cp artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar ./artiusid-sdk-vX.X.XX.aar
```

### **Test Changes**
```bash
# 1. Build SDK
./gradlew :artiusid-sdk:assembleDebug

# 2. Install sample app
./gradlew :sample-app:installDebug

# 3. Watch logs
adb logcat -c && adb logcat | grep -E "ArtiusIDSDK|Sample"
```

### **Create Branch**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/my-feature
```

### **Submit Changes**
```bash
git add .
git commit -m "[feat] Description"
git push origin feature/my-feature
# Create MR in GitLab
```

---

## 🐛 Debugging

### **Enable SDK Logging**
In `BridgeMainActivity.kt`:
```kotlin
SDKConfiguration(
    enableLogging = true
)
```

### **Key Log Tags**
- `ArtiusIDSDK` - Main SDK
- `VerificationProcessingVM` - Verification
- `CertificateManager` - Certificates
- `FirebaseTokenManager` - FCM tokens

### **Common Fixes**
```bash
# App won't start
adb shell pm clear com.artiusid.sampleapp

# Gradle issues
./gradlew --stop
./gradlew clean

# HILT issues
./gradlew :artiusid-sdk:kaptDebug
```

---

## 🌐 Environments

| Environment | URL | Purpose |
|-------------|-----|---------|
| **Sandbox** | `sandbox.mobile.artiusid.dev` | Default testing |
| **Development** | `service-mobile.dev.artiusid.dev` | Dev environment |
| **Staging** | `service-mobile.stage.artiusid.dev` | Pre-production |

---

## 📦 Version Info

**Current Version:** 1.2.48  
**Version Code:** 56  
**Min SDK:** 24 (Android 7.0)  
**Target SDK:** 34 (Android 14)

**Location:** `artiusid-sdk/build.gradle`

---

## 🔑 Key Files

| File | Purpose |
|------|---------|
| `artiusid-sdk/build.gradle` | SDK build config |
| `sample-app/google-services.json` | Firebase config |
| `local.properties` | Local SDK paths |
| `gradle.properties` | Gradle settings |
| `proguard-rules.pro` | ProGuard config |

---

## 🚀 Git Workflow

```bash
# Update develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/name

# Make changes, commit
git add .
git commit -m "[type] Description"

# Push and create MR
git push origin feature/name
```

**Branch Types:** `feature/`, `fix/`, `hotfix/`, `release/`

**Commit Types:** `[feat]`, `[fix]`, `[docs]`, `[refactor]`, `[test]`, `[chore]`

---

## 🧪 Testing Checklist

Before submitting:
- [ ] `./gradlew lint` passes
- [ ] `./gradlew test` passes
- [ ] SDK builds successfully
- [ ] Sample app runs without crashes
- [ ] Tested in Sandbox environment
- [ ] No console errors or warnings

---

## 📞 Quick Help

| Issue | Solution |
|-------|----------|
| **Build fails** | `./gradlew clean` then rebuild |
| **Can't find class** | Sync Gradle, check imports |
| **HILT error** | `./gradlew diagnoseHilt` |
| **App crashes** | Check logs: `adb logcat \| grep ArtiusIDSDK` |
| **Wrong version** | Check `artiusid-sdk/build.gradle` |

---

## 🔗 Important Links

**GitLab:** git@gitlab.com:artiusid1/mobile-sdk-android.git  
**GitHub:** https://github.com/artius-iD/artiusid_sdk_android  
**Firebase:** https://console.firebase.google.com/project/artiusid

---

## 💡 Pro Tips

1. **Use Android Studio's search:** Cmd+Shift+F (Mac) / Ctrl+Shift+F (Win)
2. **Run lint before committing:** Catches issues early
3. **Test with cleared app data:** Ensures clean state
4. **Keep commits small:** Easier to review and debug
5. **Check logcat regularly:** Catch issues early

---

## 📋 Pre-Release Checklist

- [ ] Version updated in `build.gradle`
- [ ] Release notes created
- [ ] Deployment summary created
- [ ] All tests pass
- [ ] Sample app tested thoroughly
- [ ] ProGuard mapping saved
- [ ] Documentation updated
- [ ] GitHub release prepared

---

## ⚙️ IDE Shortcuts (Mac)

| Action | Shortcut |
|--------|----------|
| **Find class** | Cmd+O |
| **Find file** | Cmd+Shift+O |
| **Find action** | Cmd+Shift+A |
| **Find in files** | Cmd+Shift+F |
| **Format code** | Cmd+Alt+L |
| **Comment line** | Cmd+/ |
| **Build project** | Cmd+F9 |
| **Run** | Ctrl+R |

---

**Keep this card bookmarked for quick reference!**

**Last Updated:** October 29, 2025

