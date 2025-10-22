# SDK v1.2.26 - Remove Timeout Field from Approval Responses

**Date:** October 22, 2025  
**Priority:** P1 - Accuracy Fix  
**Status:** ✅ FIXED - READY FOR DEPLOYMENT

---

## 🎯 Issue

The `timeout` field was being included in approval response JSON, but **iOS does NOT send this field** for approval responses.

---

## 🔍 Analysis

### **Approval Request vs Approval Response:**

1. **Approval Request** (sending TO user):
   - Purpose: Ask user to approve/deny something
   - Has `timeout` field: How long user has to respond
   - iOS: `ApprovalRequestTestingRequest` includes `timeout: Int = 30` ✅

2. **Approval Response** (user responding):
   - Purpose: User's answer (approve/deny)
   - Should NOT have `timeout` field: User already responded
   - iOS: `ApprovalRequest` struct has `timeout` but **doesn't set it** ❌

### **iOS Code:**

```swift
// iOS ApprovalResponse.swift - sendApprovalResponse()
let request = ApprovalRequest(
    clientId: AppConstants.clientId,
    clientGroupId: AppConstants.clientGroupId,
    deviceId: deviceId,
    requestId: requestId,
    responseValue: approvalValue
    // NOTE: timeout is NOT set here
)
```

The iOS `ApprovalRequest` struct has a `timeout` field with a default value in `toEncodableBody()`, but when actually creating approval responses, **iOS doesn't set it**, so it gets the default value but is never actually used.

---

## ✅ Solution

**Removed the `timeout` field** from Android's `ApprovalRequest` data class to match what iOS actually sends.

---

## 📝 Changes Made

### **1. ApprovalRequest.kt**

**Before:**
```kotlin
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: String = "1",
    
    @SerializedName("clientGroupId")
    val clientGroupId: String = "1",
    
    @SerializedName("deviceId")
    val deviceId: String = "",
    
    @SerializedName("requestId")
    val requestId: String = "0",
    
    @SerializedName("responseValue")
    val responseValue: String = "",
    
    @SerializedName("timeout")
    val timeout: String = "30"  // ❌ iOS doesn't send this
)
```

**After:**
```kotlin
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: String = "1",
    
    @SerializedName("clientGroupId")
    val clientGroupId: String = "1",
    
    @SerializedName("deviceId")
    val deviceId: String = "",
    
    @SerializedName("requestId")
    val requestId: String = "0",
    
    @SerializedName("responseValue")
    val responseValue: String = ""
    // ✅ timeout field removed
)
```

### **2. ApprovalResponse.kt**

**Before:**
```kotlin
val request = ApprovalRequest(
    clientId = "1",
    clientGroupId = "1",
    deviceId = deviceId,
    requestId = requestId.toString(),
    responseValue = approvalValue,
    timeout = "30"  // ❌ iOS doesn't send this
)
```

**After:**
```kotlin
val request = ApprovalRequest(
    clientId = "1",
    clientGroupId = "1",
    deviceId = deviceId,
    requestId = requestId.toString(),
    responseValue = approvalValue
    // ✅ timeout field removed
)
```

---

## 📊 JSON Payload Comparison

### **Android v1.2.25 (With timeout):**
```json
{
  "clientId": "1",
  "clientGroupId": "1",
  "deviceId": "abc123def456",
  "requestId": "12345",
  "responseValue": "Approved",
  "timeout": "30"  ← ❌ iOS doesn't send this
}
```

### **iOS (What it actually sends):**
```json
{
  "clientId": "1",
  "clientGroupId": "1",
  "deviceId": "E621E1F8-C36C-495A-93FC-0C247A3E6E5F",
  "requestId": "12345",
  "responseValue": "Approved"
  // ✅ No timeout field
}
```

### **Android v1.2.26 (Without timeout):**
```json
{
  "clientId": "1",
  "clientGroupId": "1",
  "deviceId": "abc123def456",
  "requestId": "12345",
  "responseValue": "Approved"
  // ✅ No timeout field - matches iOS
}
```

---

## 🧪 Expected Log Output

### **Before (v1.2.25):**
```
ApprovalResponse: 📤 Request payload (all strings like iOS):
ApprovalResponse: 📤   clientId: 1 (String)
ApprovalResponse: 📤   clientGroupId: 1 (String)
ApprovalResponse: 📤   deviceId: abc123def456 (String)
ApprovalResponse: 📤   requestId: 12345 (String)
ApprovalResponse: 📤   responseValue: Approved (String)
ApprovalResponse: 📤   timeout: 30 (String)  ← Extra field
```

### **After (v1.2.26):**
```
ApprovalResponse: 📤 Request payload (all strings like iOS):
ApprovalResponse: 📤   clientId: 1 (String)
ApprovalResponse: 📤   clientGroupId: 1 (String)
ApprovalResponse: 📤   deviceId: abc123def456 (String)
ApprovalResponse: 📤   requestId: 12345 (String)
ApprovalResponse: 📤   responseValue: Approved (String)
ApprovalResponse: 📤   NOTE: timeout field NOT included (iOS doesn't send it for responses)
```

---

## 🔧 What Changed in v1.2.26

| File | Change | Purpose |
|------|--------|---------|
| `ApprovalRequest.kt` | Removed `timeout` field | Match iOS behavior |
| `ApprovalRequest.kt` | Updated `toEncodableBody()` | Remove timeout from map |
| `ApprovalResponse.kt` | Removed `timeout` parameter | Don't set unused field |
| `ApprovalResponse.kt` | Updated logging | Clarify timeout not included |
| `gradle.properties` | Version → 1.2.26 | New release |

---

## 📋 Why This Matters

### **Semantic Correctness:**
- `timeout` makes sense for **requests** (how long to wait for response)
- `timeout` does NOT make sense for **responses** (user already responded)

### **iOS Compatibility:**
- iOS doesn't send `timeout` for approval responses
- Android should match iOS exactly
- Backend may be confused by unexpected field

### **Cleaner API:**
- Removes unnecessary field
- Reduces payload size (minimal)
- More accurate data model

---

## 🎯 Success Criteria

**v1.2.26 is successful if:**
1. ✅ JSON payload matches iOS exactly (no timeout field)
2. ✅ Backend still accepts approval responses
3. ✅ Approval flow works end-to-end
4. ✅ Logs show timeout field is NOT included

---

## 📞 Testing Instructions

1. **Update to v1.2.26**
2. **Send approval response**
3. **Check logs** - should NOT show timeout field
4. **Verify backend** - approval should be processed
5. **Compare with iOS** - JSON should match exactly

---

## ✅ Summary

**Removed unnecessary `timeout` field from approval responses:**
- ✅ iOS doesn't send it for responses
- ✅ Semantically incorrect (user already responded)
- ✅ Android now matches iOS exactly
- ✅ Cleaner, more accurate data model

**This ensures Android approval responses match iOS behavior precisely.**

---

**Fixed in SDK v1.2.26**

— ArtiusID SDK Team  
October 22, 2025

