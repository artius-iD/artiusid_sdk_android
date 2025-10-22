# TriNet: SDK v1.2.24 - Diagnostic Release for Multiple Approval Requests

**Date:** October 22, 2025  
**Priority:** P0 - CRITICAL  
**Status:** ✅ DEPLOYED TO GITHUB

---

## 📋 Executive Summary

We've deployed **SDK v1.2.24** with **enhanced diagnostic logging** to help identify why multiple approval requests are still being sent. This release includes:

✅ **Button-level debounce** (2-second cooldown)  
✅ **UUID-based call tracking** at every layer  
✅ **HTTP request/response logging** to track actual network calls  

This will help us pinpoint **exactly where** the duplicate requests are originating.

---

## 🔗 Download Links

### **SDK v1.2.24:**
- **Release Page:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.24
- **AAR Direct Download:** https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.24/artiusid-sdk-1.2.24.aar
- **Sample App:** https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.24/sample-app-customer-distribution.apk

### **Release Details:**
- **Created:** October 22, 2025 at 16:14:24 UTC
- **Published:** October 22, 2025 at 16:14:27 UTC
- **AAR Size:** 25 MB
- **Sample App Size:** 173 MB

---

## 🆕 What's New in v1.2.24

### **1. Button Debounce Protection**
- **2-second cooldown** between button clicks
- Prevents accidental double-clicks
- Logs every button press with timestamp

### **2. Multi-Layer Call Tracking**
Every approval request now has **3 unique IDs** for tracking:

| Layer | ID Format | Purpose |
|-------|-----------|---------|
| **UI Layer** | Button timestamp | Tracks when button is clicked |
| **SDK Layer** | `[Call xxxxxxxx]` | Tracks `sendApprovalRequest()` calls |
| **API Layer** | `[Call xxxxxxxx]` | Tracks `send()` function calls |
| **Network Layer** | `[HTTP xxxxxxxx]` | Tracks actual HTTP requests |

### **3. Enhanced Logging**
- **Start time** and **duration** for every call
- **HTTP request method** and **URL**
- **HTTP response code** and **timing**
- Clear **separator lines** for easy log parsing

---

## 🧪 Testing Instructions

### **Step 1: Update to v1.2.24**

1. Download the new AAR:
   ```
   https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.24/artiusid-sdk-1.2.24.aar
   ```

2. Replace the old AAR in your project:
   ```
   app/libs/artiusid-sdk-1.2.24.aar
   ```

3. Update your `build.gradle`:
   ```gradle
   implementation files('libs/artiusid-sdk-1.2.24.aar')
   ```

4. Clean and rebuild:
   ```bash
   ./gradlew clean assembleDebug
   ```

---

### **Step 2: Test Approval Request**

1. **Open your app**
2. **Complete verification** (if not already done)
3. **Tap "Test Approval" button ONCE**
4. **Wait for the response** (don't tap again)

---

### **Step 3: Collect Logs**

Run this command to capture all relevant logs:

```bash
adb logcat -c && adb logcat | grep -E "BridgeMainActivity|ArtiusIDSDK|SendApprovalRequest|TLSSessionManager" > approval_logs.txt
```

Or use Android Studio's Logcat with these filters:
- `BridgeMainActivity`
- `ArtiusIDSDK`
- `SendApprovalRequest`
- `TLSSessionManager`

---

### **Step 4: Analyze Logs**

Look for these key indicators:

#### **✅ CORRECT - Single Request:**
```
BridgeMainActivity: 📋 APPROVAL REQUEST BUTTON CLICKED
BridgeMainActivity: 📋 Timestamp: 1729612345678
ArtiusIDSDK: 📞 [Call abc123de] sendApprovalRequest() STARTED
SendApprovalRequest: 📞 [Call xyz789ab] send() STARTED at 1729612345680
TLSSessionManager: 🌐 [HTTP req456gh] HTTP REQUEST STARTED
TLSSessionManager: 🌐 [HTTP req456gh] Method: POST
TLSSessionManager: 🌐 [HTTP req456gh] URL: https://sandbox.mobile.artiusid.dev/ApprovalRequestTestingFunction
TLSSessionManager: 🌐 [HTTP req456gh] HTTP RESPONSE RECEIVED
TLSSessionManager: 🌐 [HTTP req456gh] Status: 200
SendApprovalRequest: 📞 [Call xyz789ab] ✅ Approval request sent successfully
ArtiusIDSDK: 📞 [Call abc123de] ✅ sendApprovalRequest() COMPLETED successfully
```

**Count the unique IDs:**
- ✅ **1 button click** timestamp
- ✅ **1 ArtiusIDSDK call** ID (`abc123de`)
- ✅ **1 send() call** ID (`xyz789ab`)
- ✅ **1 HTTP request** ID (`req456gh`)

---

#### **❌ INCORRECT - Multiple Requests:**

If you see **multiple HTTP IDs**, that means OkHttp is making duplicate requests:
```
TLSSessionManager: 🌐 [HTTP req456gh] HTTP REQUEST STARTED
TLSSessionManager: 🌐 [HTTP abc789de] HTTP REQUEST STARTED  ← DUPLICATE!
TLSSessionManager: 🌐 [HTTP xyz123fg] HTTP REQUEST STARTED  ← DUPLICATE!
```

If you see **multiple send() IDs**, that means the SDK is calling `send()` multiple times:
```
SendApprovalRequest: 📞 [Call xyz789ab] send() STARTED
SendApprovalRequest: 📞 [Call abc123de] send() STARTED  ← DUPLICATE!
```

If you see **multiple button clicks**, that means the UI is triggering multiple times (debounce should prevent this):
```
BridgeMainActivity: 📋 APPROVAL REQUEST BUTTON CLICKED
BridgeMainActivity: 📋 Timestamp: 1729612345678
BridgeMainActivity: 📋 APPROVAL REQUEST BUTTON CLICKED  ← DUPLICATE!
BridgeMainActivity: 📋 Timestamp: 1729612345680
```

---

### **Step 5: Check Backend**

After tapping the button **once**, check your backend:

1. **How many approval requests were received?**
2. **How many notifications were sent?**
3. **What are the request IDs?**

---

### **Step 6: Send Us the Results**

Please send us:

1. ✅ **Full logs** from Step 3 (`approval_logs.txt`)
2. ✅ **Count of unique IDs** you found:
   - Button clicks: ___
   - ArtiusIDSDK calls: ___
   - send() calls: ___
   - HTTP requests: ___
3. ✅ **Backend data:**
   - Approval requests received: ___
   - Notifications sent: ___
4. ✅ **Screenshots** of your Logcat (optional but helpful)

---

## 🔍 What We'll Learn

Based on the logs, we'll know **exactly** where the duplicates originate:

| Symptom | Root Cause | Next Fix |
|---------|------------|----------|
| Multiple button click logs | UI recomposition issue | Add `LaunchedEffect` guard |
| Multiple `send()` call IDs | SDK guard flag not working | Investigate Hilt scope |
| Multiple HTTP request IDs | OkHttp retry despite disable | Add custom interceptor |
| Single log, multiple backend requests | Backend processing issue | Backend team investigation |

---

## 📊 Expected Outcome

**If v1.2.24 works correctly:**
- ✅ Logs show **exactly 1 HTTP request** per button click
- ✅ Backend receives **exactly 1 approval request**
- ✅ User receives **exactly 1 notification**

**If duplicates persist:**
- ✅ Logs will show **exactly where** the duplicates originate
- ✅ We can implement a **targeted fix** in v1.2.25

---

## 🚀 Timeline

1. **Now:** v1.2.24 deployed to GitHub
2. **Next:** TriNet updates and tests
3. **Then:** TriNet sends logs and backend data
4. **Finally:** We analyze and implement targeted fix if needed

---

## 📞 Support

If you have any questions or need help:

1. **Check the logs** using the commands above
2. **Send us the full log output**
3. **Include backend request counts**
4. **We'll respond with analysis and next steps**

---

## 🔗 Related Documentation

- **SDK v1.2.23:** Initial triple approval fix (guard flag + retry disable)
- **SDK v1.2.24:** Enhanced diagnostic logging (this release)
- **Integration Guide:** https://github.com/artius-iD/artiusid_sdk_android/blob/main/INTEGRATION_GUIDE.md

---

## ✅ Summary

**SDK v1.2.24 is ready for testing!**

- ✅ Enhanced logging at all layers
- ✅ Button debounce protection
- ✅ UUID-based call tracking
- ✅ Clear log separators for easy analysis

**Next step:** Update to v1.2.24, test, and send us the logs!

We're committed to resolving this issue completely. The enhanced logging in v1.2.24 will give us the visibility we need to identify and fix the root cause.

---

**Thank you for your patience and collaboration!**

— ArtiusID SDK Team

