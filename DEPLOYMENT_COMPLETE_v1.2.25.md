# ✅ SDK v1.2.25 - Deployment Complete

**Date:** October 22, 2025  
**Time:** 16:54:26 UTC  
**Status:** ✅ **DEPLOYED TO GITHUB**

---

## 📦 Release Information

### **Version Details:**
- **Version:** 1.2.25
- **Version Code:** 33
- **Release Tag:** v1.2.25
- **Release Type:** Critical Bug Fix

### **GitHub Release:**
- **URL:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.25
- **Created:** October 22, 2025 at 16:54:23 UTC
- **Published:** October 22, 2025 at 16:54:26 UTC

### **Download Links:**
- **AAR:** https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.25/artiusid-sdk-1.2.25.aar (25 MB)
- **Sample App:** https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.25/sample-app-customer-distribution.apk (173 MB)

---

## 🎯 What Was Fixed

### **Problem:**
Approval response JSON format from Android SDK didn't match iOS standalone app, causing backend to reject approval responses.

### **Root Cause:**
- **iOS:** Converts all Int fields to String via `toEncodableBody()`
- **Android:** Was sending Int values directly (1, 12345)
- **Backend:** Expected strings (like iOS) ("1", "12345")
- **Result:** Android approval responses rejected

### **Solution:**
Changed `ApprovalRequest` data class to send **ALL fields as strings**, matching iOS exactly.

---

## 📝 Changes Made

### **Files Modified:**

| File | Changes |
|------|---------|
| `ApprovalRequest.kt` | Changed all Int fields to String type |
| `ApprovalRequest.kt` | Updated `toEncodableBody()` to return `Map<String, String>` |
| `ApprovalResponse.kt` | Convert `requestId` to String before creating request |
| `ApprovalResponse.kt` | Added detailed logging for JSON payload |
| `gradle.properties` | Version bumped to 1.2.25 (code 33) |

### **Commit:**
```
SDK v1.2.25: Fix approval response JSON format to match iOS

- Changed ApprovalRequest fields from Int to String types
- iOS toEncodableBody() converts all Int fields to String
- Backend expects all fields as strings (like iOS)
- Android was sending Int values, causing backend rejection
- Now matches iOS JSON format exactly
```

---

## 📊 JSON Payload Comparison

### **iOS JSON (Correct):**
```json
{
  "clientId": "1",
  "clientGroupId": "1",
  "deviceId": "E621E1F8-C36C-495A-93FC-0C247A3E6E5F",
  "requestId": "12345",
  "responseValue": "Approved",
  "timeout": "30"
}
```

### **Android JSON BEFORE Fix (Incorrect):**
```json
{
  "clientId": 1,           ← ❌ Number
  "clientGroupId": 1,      ← ❌ Number
  "deviceId": "abc123def456",
  "requestId": 12345,      ← ❌ Number
  "responseValue": "Approved",
  "timeout": "30"
}
```

### **Android JSON AFTER Fix (Correct):**
```json
{
  "clientId": "1",         ← ✅ String
  "clientGroupId": "1",    ← ✅ String
  "deviceId": "abc123def456",
  "requestId": "12345",    ← ✅ String
  "responseValue": "Approved",
  "timeout": "30"
}
```

---

## 🔧 Code Changes

### **ApprovalRequest.kt - BEFORE:**
```kotlin
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: Int? = 1,        // ❌ Int
    
    @SerializedName("requestId")
    val requestId: Int? = 0,       // ❌ Int
    ...
)
```

### **ApprovalRequest.kt - AFTER:**
```kotlin
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: String = "1",        // ✅ String
    
    @SerializedName("requestId")
    val requestId: String = "0",       // ✅ String
    ...
)
```

### **ApprovalResponse.kt - AFTER:**
```kotlin
// Create request exactly like iOS - ALL FIELDS AS STRINGS
val request = ApprovalRequest(
    clientId = "1",                    // ✅ String
    clientGroupId = "1",               // ✅ String
    deviceId = deviceId,
    requestId = requestId.toString(),  // ✅ Convert Int to String
    responseValue = approvalValue,
    timeout = "30"
)

Log.d(TAG, "📤 Request payload (all strings like iOS):")
Log.d(TAG, "📤   clientId: ${request.clientId} (String)")
Log.d(TAG, "📤   clientGroupId: ${request.clientGroupId} (String)")
Log.d(TAG, "📤   deviceId: ${request.deviceId} (String)")
Log.d(TAG, "📤   requestId: ${request.requestId} (String)")
Log.d(TAG, "📤   responseValue: ${request.responseValue} (String)")
Log.d(TAG, "📤   timeout: ${request.timeout} (String)")
```

---

## 🧪 Expected Behavior

### **Approval Response Flow:**

1. ✅ User receives approval request notification
2. ✅ User taps notification → opens approval screen
3. ✅ User taps "Approve" or "Deny"
4. ✅ SDK sends approval response with **ALL STRING fields**
5. ✅ Backend accepts the request
6. ✅ User sees success message

### **Expected Log Output:**

```
ApprovalResponse: 📤 Sending approval response:
ApprovalResponse: 📤   Device ID: abc123def456 (native Android format)
ApprovalResponse: 📤   Request ID: 12345
ApprovalResponse: 📤   Response Value: Approved
ApprovalResponse: 📤 Request payload (all strings like iOS):
ApprovalResponse: 📤   clientId: 1 (String)
ApprovalResponse: 📤   clientGroupId: 1 (String)
ApprovalResponse: 📤   deviceId: abc123def456 (String)
ApprovalResponse: 📤   requestId: 12345 (String)
ApprovalResponse: 📤   responseValue: Approved (String)
ApprovalResponse: 📤   timeout: 30 (String)
ApprovalResponse: ✅ Approval response sent successfully
```

---

## 📋 Deployment Checklist

- [x] Code changes implemented
- [x] Version updated to 1.2.25
- [x] Linter checks passed
- [x] Committed to GitLab (origin/main)
- [x] SDK built successfully
- [x] GitHub release created (v1.2.25)
- [x] AAR uploaded (25 MB)
- [x] Sample app uploaded (173 MB)
- [x] Documentation created:
  - [x] SDK_v1.2.25_APPROVAL_RESPONSE_JSON_FIX.md
  - [x] APPROVAL_RESPONSE_JSON_COMPARISON.md
  - [x] DEPLOYMENT_COMPLETE_v1.2.25.md
- [x] Ready for TriNet testing

---

## 🎯 Success Criteria

**v1.2.25 is successful if:**
1. ✅ Backend accepts Android approval responses
2. ✅ JSON payload matches iOS format exactly (all strings)
3. ✅ Users can successfully approve or deny requests
4. ✅ Success messages display correctly

---

## 📞 Next Steps for TriNet

1. **Download v1.2.25** from GitHub
2. **Update project and rebuild**
3. **Test approval response flow:**
   - Send approval request
   - Receive notification
   - Tap notification
   - Tap "Approve" or "Deny"
   - Verify success message
4. **Check backend logs:**
   - Verify request is accepted
   - Verify JSON format is correct
5. **Report results**

---

## 🔗 Related Issues

- **SDK v1.2.23:** Triple approval request fix (guard flag + retry disable)
- **SDK v1.2.24:** Enhanced diagnostic logging
- **SDK v1.2.25:** Approval response JSON format fix (this release)

---

## ✅ Summary

**Critical fix for approval response JSON format:**
- ✅ Changed all Int fields to String in `ApprovalRequest`
- ✅ Matches iOS `toEncodableBody()` behavior exactly
- ✅ Backend will now accept Android approval responses
- ✅ Enhanced logging for debugging

**This ensures Android SDK approval responses match iOS format exactly, resolving backend rejection issues.**

---

## 📦 What's Included

### **SDK AAR (25 MB):**
- ✅ Approval response JSON fix
- ✅ Enhanced diagnostic logging (v1.2.24)
- ✅ Triple approval request fix (v1.2.23)
- ✅ All previous fixes and features

### **Sample App (173 MB):**
- ✅ Fully functional demo
- ✅ All SDK features
- ✅ Obfuscated for IP protection

---

**Deployment completed successfully!**

— ArtiusID SDK Team  
October 22, 2025

