# Backend Stored Procedure Issue - sp_VERIFICATION_CreateVerification

## Issue Summary

**Date:** October 15, 2025  
**SDK Version:** v1.2.3  
**Procedure:** `artiusid_db.sp_VERIFICATION_CreateVerification`

## Error Received

```
HTTP 400 Bad Request
Error: "Incorrect number of arguments for PROCEDURE artiusid_db.sp_VERIFICATION_CreateVerification; expected 7, got 6"
```

## Analysis

### Android SDK is Sending 7 Parameters

The Android SDK v1.2.3 is correctly sending **all 7 parameters** in the verification request:

```json
{
  "frontImageBase64": "[BASE64_IMAGE_DATA]",
  "backImageBase64": "",
  "faceImageBase64": "[BASE64_IMAGE_DATA]",
  "documentType": "2",
  "deviceId": "[DEVICE_ID]",
  "deviceModel": "[DEVICE_MODEL]",
  "fcmToken": "[FCM_TOKEN]"
}
```

### Parameter Details

| # | Parameter Name | Type | Value for Passports | Notes |
|---|----------------|------|---------------------|-------|
| 1 | `frontImageBase64` | String | Populated | Required |
| 2 | `backImageBase64` | String | Empty string `""` | **Required even for passports** |
| 3 | `faceImageBase64` | String | Populated | Required |
| 4 | `documentType` | String | `"2"` | 2 = Passport |
| 5 | `deviceId` | String | Populated | Device identifier |
| 6 | `deviceModel` | String | Populated | Device model name |
| 7 | `fcmToken` | String | Populated | Firebase token |

### Key Point: backImageBase64

**For passport documents (documentType=2):**
- `backImageBase64` is sent as an **empty string** `""`
- It is **NOT omitted** from the JSON
- This matches the iOS SDK implementation exactly

## iOS Implementation Reference

The iOS SDK (working correctly) sends the request body as:

```swift
let body: [String: Any] = [
    "frontImageBase64": verificationRequest.frontImageBase64! as String,
    "backImageBase64": (verificationRequest.backImageBase64 ?? "") as String, // Empty string if nil
    "faceImageBase64": (verificationRequest.faceImageBase64 ?? "") as String,
    "documentType": (verificationRequest.documentType ?? 1) as Int,
    "deviceId": (verificationRequest.deviceId) as String,
    "deviceModel": (verificationRequest.deviceModel ?? "") as String,
    "fcmToken": (verificationRequest.fcmToken ?? "") as String
]
```

**iOS always includes all 7 parameters, using empty string for optional/missing values.**

## Possible Root Causes

### 1. Empty String Treated as NULL
The stored procedure might be treating the empty string `""` for `backImageBase64` as NULL or missing, causing it to count only 6 parameters instead of 7.

**Fix:** Modify the stored procedure to accept empty string as a valid value.

### 2. Parameter Mapping Issue
The procedure might have incorrect parameter mapping that skips empty string values.

**Fix:** Review the parameter mapping logic to ensure all 7 parameters are properly extracted from the JSON payload.

### 3. JSON Deserialization Problem
The backend JSON parser might be omitting keys with empty string values when converting to procedure parameters.

**Fix:** Ensure JSON deserialization preserves all keys, regardless of value content.

### 4. Procedure Signature Mismatch
The stored procedure signature might expect only 6 parameters for passport documents, but Android/iOS are sending 7.

**Fix:** Update the procedure signature to accept all 7 parameters, handling empty `backImageBase64` for passports.

## Expected Behavior

The stored procedure should:
1. ✅ Accept all 7 parameters
2. ✅ Handle `backImageBase64` as empty string `""` for passports
3. ✅ Process passport verifications successfully with empty back image
4. ✅ Match iOS behavior (which is working correctly)

## Request for Backend Team

Please investigate and fix the stored procedure to:
1. Accept all 7 parameters from the JSON payload
2. Properly handle empty string `""` for `backImageBase64` when `documentType` is `2` (passport)
3. Ensure the parameter count logic recognizes all 7 parameters as present

## Sample Logcat Output

```
VerifProcessVM: ❌ Verification failed: HTTP 400 Client Error
ArtiusVerificationRepository: ❌ Verification error: Response.error()
RETROFIT: {"error":"Incorrect number of arguments for PROCEDURE artiusid_db.sp_VERIFICATION_CreateVerification; expected 7, got 6"}
```

## Verification Request Debug Log

```kotlin
toOrderedMap() output:
1. frontImageBase64: [BASE64_DATA...] (length: 245876 chars)
2. backImageBase64: "" (length: 0 chars) ← Empty but PRESENT
3. faceImageBase64: [BASE64_DATA...] (length: 189234 chars)
4. documentType: "2"
5. deviceId: "android_device_12345"
6. deviceModel: "Pixel 7 Pro"
7. fcmToken: "firebase_token_abc123..."
```

## Timeline

- **Oct 15, 2025:** Issue identified during passport verification testing
- **Oct 15, 2025:** Android SDK v1.2.3 verified to send all 7 parameters correctly
- **Oct 15, 2025:** iOS SDK confirmed to use identical parameter structure
- **Awaiting:** Backend stored procedure fix

## Contact

For questions about the Android SDK implementation, reference:
- File: `artiusid-sdk/src/main/java/com/artiusid/sdk/data/model/VerificationRequest.kt`
- Method: `toOrderedMap()`
- Lines: 24-43

---

**Action Required:** Backend team to update `sp_VERIFICATION_CreateVerification` to properly accept and map all 7 parameters, including empty string for `backImageBase64` on passport documents.


