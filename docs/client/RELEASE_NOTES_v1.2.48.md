# ArtiusID Android SDK v1.2.48 - Release Notes

**Release Date:** October 29, 2025  
**Author:** Todd Bryant, artius.iD, Inc.

---

## 🚨 BREAKING CHANGES

### **1. Firebase Architecture Change**
**Impact:** HIGH - Requires client implementation changes

**What Changed:**
- SDK no longer manages Firebase Messaging Service
- Client apps must create their own `FirebaseMessagingService`
- Client apps must handle notification display
- FCM token provided via `ArtiusIDSDK.updateFcmToken()` instead of SDK config

**Why:**
- Clear separation of concerns
- Eliminates SDK/client Firebase initialization race conditions
- Gives clients full control over notification appearance and behavior
- Matches iOS app architecture

**Migration Required:** YES - See `CLIENT_IMPLEMENTATION_GUIDE.md`

---

## ✨ NEW FEATURES

### **1. Environment-Specific Credential Storage**
- All credentials (verification, FCM tokens, certificates) now stored per-environment
- Prevents cross-environment credential contamination
- Auto-detects environment from stored credentials on app startup
- Switching environments preserves environment-specific credentials

**Benefits:**
- No need to re-verify when switching back to previously used environment
- Clean isolation between Sandbox, Development, and Staging
- Reduced verification friction for testing across environments

### **2. Enhanced Notification System**
- HIGH importance notification channel for approval requests
- Sound, vibration, and LED lights enabled by default
- Heads-up notification support for time-sensitive approvals
- Customizable vibration pattern

**Benefits:**
- Approval requests are immediately noticeable
- Better user experience for time-sensitive actions
- Consistent with modern messaging app notification behavior

### **3. Simplified Verification UI**
- Streamlined processing flow: Processing → Success
- Removed complex progress tracking
- Better progress circle positioning (no text overlap)
- Enhanced state logging for debugging

**Benefits:**
- Cleaner, more reliable UI
- Easier to debug verification issues
- Improved user experience

---

## 🐛 BUG FIXES

### **Critical Fixes**

1. **Environment Change UI State Bug** (🚨 CRITICAL)
   - **Issue:** Member ID from old environment displayed after switching environments
   - **Fix:** Clear `verificationResultData` when changing environments
   - **Impact:** Prevents showing incorrect member ID in UI

2. **Verification UI Stuck at Processing** (🚨 CRITICAL)
   - **Issue:** UI stuck showing "Processing..." even after verification completed
   - **Fix:** Enhanced state flow management and guard cleanup
   - **Impact:** Verification now reliably progresses to Success state

3. **Approval Notifications Not Received**
   - **Issue:** Backend missing FCM token from verification
   - **Fix:** Firebase architecture change ensures FCM token sent with verification
   - **Impact:** Approval notifications now work reliably

### **Enhancement Fixes**

4. **Progress Circle Text Overlap**
   - **Issue:** "Processing..." text overlapped with progress indicator
   - **Fix:** Added proper spacing (80dp top spacer + 40dp bottom padding)
   - **Impact:** Improved visual clarity

5. **Environment Synchronization**
   - **Issue:** SDK components using inconsistent environment references
   - **Fix:** Centralized environment management via `UrlBuilder`
   - **Impact:** All SDK operations use correct environment

---

## 🔧 TECHNICAL IMPROVEMENTS

### **Architecture**

1. **Firebase Responsibility Separation**
   - Client apps now fully control Firebase integration
   - SDK receives FCM token as a parameter
   - Eliminates initialization timing issues

2. **Environment Credential Manager**
   - New `EnvironmentCredentialManager` utility class
   - Coordinates credential storage across all managers
   - Provides auto-detection and environment switching logic

3. **Enhanced State Management**
   - Improved ViewModel state flow handling
   - Better Compose recomposition management
   - Reduced duplicate trigger issues

### **Code Quality**

1. **Enhanced Logging**
   - Comprehensive state change logging
   - Environment tracking throughout SDK
   - Easier debugging of verification and approval flows

2. **ProGuard Optimization**
   - Minification enabled for release builds
   - Code obfuscation for enhanced security
   - Consumer ProGuard rules for host apps

---

## 📋 SUPPORTED ENVIRONMENTS

| Environment | Verification/Approval URL | Certificate URL |
|-------------|--------------------------|----------------|
| **Sandbox** | `sandbox.mobile.artiusid.dev` | `sandbox.registration.artiusid.dev` |
| **Development** | `service-mobile.dev.artiusid.dev` | `service-registration.dev.artiusid.dev` |
| **Staging** | `service-mobile.stage.artiusid.dev` | `service-registration.stage.artiusid.dev` |

---

## 🔐 SECURITY ENHANCEMENTS

1. **Environment Isolation**
   - Credentials tagged with environment
   - Prevents accidental cross-environment usage
   - Environment-specific keystore storage

2. **mTLS Support**
   - Client certificate authentication maintained
   - Certificate pinning (optional)
   - Secure communication for all API calls

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher
- **Firebase:** 33.5.1 or higher

---

## 📦 WHAT'S INCLUDED

### **SDK Components**
- `artiusid-sdk-release.aar` - Production SDK library
- `CLIENT_IMPLEMENTATION_GUIDE.md` - Client integration guide
- `RELEASE_NOTES_v1.2.48.md` - This file

### **Sample App**
- Updated sample app demonstrating:
  - Firebase messaging service implementation
  - Environment management
  - Credential handling
  - Notification display

---

## 🚀 DEPLOYMENT INSTRUCTIONS

### **For GitHub Release:**

```bash
# Run the deployment script
cd scripts
./deploy-sdk-to-github.sh
```

The script will:
1. Copy SDK AAR to release directory
2. Create GitHub release v1.2.48
3. Upload AAR and documentation
4. Tag the release

### **For Client Integration:**

1. Download `artiusid-sdk-v1.2.48.aar` from GitHub releases
2. Follow `CLIENT_IMPLEMENTATION_GUIDE.md` for integration steps
3. Update your Firebase implementation per the guide
4. Test in Sandbox environment first

---

## 📝 MIGRATION GUIDE

### **From v1.2.46 → v1.2.48**

**Required Changes:**

1. **Create Firebase Messaging Service** (see guide Section 1)
2. **Update SDK Configuration:**
   ```kotlin
   handleFirebaseNotifications = false  // NEW
   customFcmToken = null  // NEW
   ```
3. **Implement FCM Token Management** (see guide Section 1, Step 4)
4. **Update AndroidManifest.xml** (see guide Section 1, Step 2)

**Optional But Recommended:**

1. Implement environment auto-detection (see guide Section 2)
2. Add environment change handler (see guide Section 2)
3. Update notification channel to HIGH importance (see guide Section 4)

---

## ✅ TESTING PERFORMED

### **Functional Testing**
- ✅ Verification flow in all environments
- ✅ Approval requests in all environments  
- ✅ Environment switching (Sandbox ↔ Development ↔ Staging)
- ✅ Firebase notifications (sound, vibration, display)
- ✅ Credential isolation per environment
- ✅ Auto-environment detection on app startup

### **Integration Testing**
- ✅ Sample app with new Firebase architecture
- ✅ mTLS certificate authentication
- ✅ Certificate registration per environment
- ✅ FCM token management across environments

### **UI Testing**
- ✅ Verification UI progression
- ✅ Progress circle layout
- ✅ Member ID display after verification
- ✅ Environment change UI updates

---

## 🐞 KNOWN ISSUES

None at this time.

---

## 📞 SUPPORT

**For Implementation Questions:**
- Review `CLIENT_IMPLEMENTATION_GUIDE.md`
- Check sample app for reference implementation

**For Technical Issues:**
- Email: support@artiusid.com
- Include SDK version (v1.2.48) in subject line

**For Documentation:**
- [SDK Documentation](https://docs.artiusid.com)
- [API Reference](https://docs.artiusid.com/api)

---

## 🎯 NEXT STEPS FOR CLIENTS

1. **Review** `CLIENT_IMPLEMENTATION_GUIDE.md`
2. **Implement** Firebase messaging service
3. **Update** SDK configuration
4. **Test** in Sandbox environment
5. **Deploy** to Development/Staging when validated
6. **Monitor** logs for first week post-deployment

---

## 📊 CHANGELOG SUMMARY

**Added:**
- Environment-specific credential storage
- Auto-environment detection
- Client-controlled Firebase messaging
- Enhanced notification system
- Simplified verification UI

**Changed:**
- Firebase architecture (SDK → Client responsibility)
- FCM token provision (config → runtime method)
- Environment handling (single → per-environment storage)
- Notification priority (DEFAULT → HIGH)

**Fixed:**
- Member ID display after environment change
- Verification UI stuck at processing
- Approval notifications not received
- Progress circle text overlap
- Environment synchronization issues

**Deprecated:**
- SDK's internal Firebase messaging service
- `handleFirebaseNotifications = true` configuration

**Removed:**
- QA and PRODUCTION environments (Sandbox, Development, Staging only)

---

**End of Release Notes**

For detailed technical implementation, see `CLIENT_IMPLEMENTATION_GUIDE.md`.

