# Email to TriNet - SDK v1.2.48 Deployment

---

**Subject:** ArtiusID Android SDK v1.2.48 - Critical Update & Implementation Required

---

**To:** TriNet Development Team  
**From:** Todd Bryant, artius.iD, Inc.  
**Date:** October 29, 2025  
**Priority:** HIGH

---

## 🚨 ACTION REQUIRED: SDK v1.2.48 Update

We've released **SDK v1.2.48** with critical fixes and architectural improvements. This release includes **breaking changes** that require implementation updates on your end.

---

## 📦 What You Need to Download

**GitHub Release:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.48

Download these files:
1. **artiusid-sdk-v1.2.48.aar** (25MB) - The SDK library
2. **sample-app-customerDistribution.apk** (173MB) - Reference implementation

---

## 🚨 CRITICAL CHANGE: Firebase Architecture

**What Changed:**
- The SDK **no longer handles Firebase notifications**
- Your app must now create its own `FirebaseMessagingService`
- FCM tokens are provided to the SDK via `ArtiusIDSDK.updateFcmToken()`

**Why This Matters:**
- Fixes the notification timing issues you've experienced
- Gives you full control over notification display
- Eliminates SDK initialization race conditions

**What You Must Do:**
1. Create `YourFirebaseMessagingService.kt` (see guide Section 1)
2. Update `AndroidManifest.xml` to register your service
3. Set `handleFirebaseNotifications = false` in SDK config
4. Call `ArtiusIDSDK.updateFcmToken(token)` when you receive FCM tokens

**⚠️ If you don't implement this, you will NOT receive approval notifications.**

---

## ✅ What's Fixed in v1.2.48

1. ✅ **Approval notifications now working** - Backend receives FCM token correctly
2. ✅ **Environment-specific credentials** - No more cross-environment contamination
3. ✅ **Verification UI stuck issue** - UI now progresses to completion reliably
4. ✅ **Member ID display bug** - Correct member ID shown after environment changes
5. ✅ **Enhanced notifications** - HIGH priority with sound, vibration, and lights

---

## 📋 Implementation Steps (Detailed in Attached Guide)

### **Step 1: Create Firebase Messaging Service** (30 minutes)
- Create `YourFirebaseMessagingService.kt`
- Implement `onNewToken()` and `onMessageReceived()`
- Register service in `AndroidManifest.xml`

### **Step 2: Update SDK Configuration** (5 minutes)
```kotlin
val sdkConfig = SDKConfiguration(
    // ... your existing config ...
    handleFirebaseNotifications = false,  // NEW: Disable SDK Firebase handling
    customFcmToken = null  // NEW: Will be provided via updateFcmToken()
)
```

### **Step 3: Provide FCM Token to SDK** (10 minutes)
```kotlin
// When you receive FCM token
ArtiusIDSDK.updateFcmToken(token)
```

### **Step 4: Test in Sandbox** (1-2 hours)
- Complete verification
- Send test approval
- Verify notification is received

**Total Implementation Time: ~2-3 hours**

---

## 📚 Documentation Attached

**CLIENT_IMPLEMENTATION_GUIDE.md** - Complete step-by-step guide covering:
- Firebase messaging service implementation (with full code examples)
- Environment-specific credential management
- Testing checklist
- Common issues and solutions
- Migration summary

**Please read Section 1 carefully - it contains all the code you need.**

---

## 🎯 Recommended Timeline

| Phase | Timeline | Action |
|-------|----------|--------|
| **Review** | This week | Read implementation guide, review code examples |
| **Implement** | Next week | Create Firebase service, update SDK config |
| **Test (Sandbox)** | Next week | Verify notifications work in Sandbox |
| **Test (Development)** | Week after | Test in Development environment |
| **Production** | TBD | Deploy after successful testing |

---

## ⚠️ Breaking Changes Summary

| Old Behavior | New Behavior |
|--------------|--------------|
| SDK handled Firebase service | **Your app handles Firebase service** |
| SDK displayed notifications | **Your app displays notifications** |
| FCM token in SDK config | **FCM token via `updateFcmToken()`** |
| Single credential storage | **Per-environment credential storage** |

---

## 🔍 Testing Requirements

Before deploying to production, please verify:

1. ✅ Verification completes successfully in Sandbox
2. ✅ Test approval notification is received
3. ✅ Notification has sound and vibration
4. ✅ Tapping notification opens your app correctly
5. ✅ Environment switching works (Sandbox ↔ Development ↔ Staging)
6. ✅ Member ID displays correctly after verification

---

## 📞 Support

**For Implementation Questions:**
- Email: todd@artiusid.com
- Include "SDK v1.2.48 Implementation" in subject line

**For Technical Issues:**
- Email: support@artiusid.com
- Include SDK version (v1.2.48) and error logs

**For Urgent Issues:**
- We're available for a screen-share session if needed

---

## 🚀 Next Steps for TriNet

1. **Download** SDK v1.2.48 from GitHub (link above)
2. **Read** attached `CLIENT_IMPLEMENTATION_GUIDE.md` (focus on Section 1)
3. **Review** code examples in the guide
4. **Implement** Firebase messaging service in your app
5. **Test** in Sandbox environment
6. **Report** any issues or questions

---

## ❓ Quick FAQ

**Q: Can we continue using the old SDK?**  
A: Not recommended. The old SDK has known issues with notifications that are fixed in v1.2.48.

**Q: How long will implementation take?**  
A: Approximately 2-3 hours for implementation and testing.

**Q: Will this break our existing app?**  
A: Yes, if you don't implement the Firebase service. Follow the guide to ensure smooth transition.

**Q: Do we need to update our Firebase configuration?**  
A: No, your existing Firebase project and `google-services.json` remain the same.

**Q: What if we have issues during implementation?**  
A: Contact us immediately. We can schedule a screen-share session to help.

---

## 📎 Attachments

1. **CLIENT_IMPLEMENTATION_GUIDE.md** - Complete implementation guide
2. **RELEASE_NOTES_v1.2.48.md** - Technical release notes (optional reading)

---

**Please confirm receipt of this email and let us know your estimated timeline for implementation.**

We're here to support you through this update. Don't hesitate to reach out with questions.

Best regards,

**Todd Bryant**  
artius.iD, Inc.  
todd@artiusid.com

---

**P.S.** The sample app (173MB) demonstrates the complete implementation. Install it on a test device to see how Firebase notifications should work with the new architecture.


