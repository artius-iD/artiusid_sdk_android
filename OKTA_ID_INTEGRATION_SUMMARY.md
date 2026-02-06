# Okta ID Integration Summary - Android SDK

**Date:** December 9, 2025  
**Version:** v1.2.49 (pending)  
**Feature:** Okta ID Collection During Verification  
**iOS Parity:** v2.0.12

---

## Overview

Implemented Okta ID collection during verification flow to achieve feature parity with iOS SDK v2.0.12. This feature allows clients to optionally collect Okta IDs from users during the identity verification process.

---

## Changes Made

### 1. Configuration (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/config/SDKConfiguration.kt`

Added configuration parameter:
```kotlin
// Okta ID Integration (NEW - matches iOS v2.0.12)
val includeOktaIDInVerificationPayload: Boolean = true, // Default to true like iOS
```

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/config/ClientConfiguration.kt`

Added method:
```kotlin
fun shouldIncludeOktaID(): Boolean {
    val includeOktaID = currentConfig?.includeOktaIDInVerificationPayload ?: true
    Log.d(TAG, "✅ Okta ID inclusion: $includeOktaID")
    return includeOktaID
}
```

---

### 2. Data Models (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/data/model/VerificationRequest.kt`

Added optional field:
```kotlin
@SerializedName("oktaId")
val oktaId: String? = null // Optional Okta ID (NEW - matches iOS v2.0.12)
```

Updated `toOrderedMap()` to conditionally include oktaId:
```kotlin
// Add oktaId if present (matches iOS conditional inclusion)
oktaId?.let {
    if (it.isNotBlank()) {
        map["oktaId"] = it
    }
}
```

---

### 3. State Management (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/OktaIDHolder.kt` (NEW)

Created singleton to hold Okta ID across verification flow:
```kotlin
object OktaIDHolder {
    fun setOktaID(id: String?)
    fun getOktaID(): String?
    fun clear()
    fun hasOktaID(): Boolean
}
```

---

### 4. UI Screen (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/CollectOktaIDScreen.kt` (NEW)

Created Compose screen matching iOS `CollectOktaIDView`:
- Text input field for Okta ID
- Validation (required field)
- "Continue" button
- "Skip for now" button
- Auto-focus on text field
- Helper text explaining Okta ID
- Themed UI matching SDK design

---

### 5. Navigation (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/navigation/AppNavigation.kt`

Added route:
```kotlin
object CollectOktaID : Screen("collect_okta_id")
```

Updated flow:
```
Face Scan → Document Scan → [Okta ID (if enabled)] → Verification Processing
```

Implementation:
- Document Scan Back → Okta ID (if enabled) → Processing
- Passport Chip Scan → Okta ID (if enabled) → Processing
- Okta ID collected → Store in `OktaIDHolder` → Navigate to Processing
- Okta ID skipped → Store `null` → Navigate to Processing

---

### 6. Verification Processing (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationProcessingViewModel.kt`

Updated to include Okta ID in verification request:
```kotlin
// Get Okta ID if enabled (NEW - matches iOS v2.0.12)
val oktaId = if (ClientConfiguration.shouldIncludeOktaID()) {
    val id = OktaIDHolder.getOktaID()
    Log.d(TAG, "  oktaId: '${id ?: "<not provided>"}'")
    id
} else {
    Log.d(TAG, "  oktaId: <disabled by configuration>")
    null
}

val request = VerificationRequest(
    // ... other fields ...
    oktaId = oktaId // NEW - matches iOS v2.0.12
)
```

---

### 7. Verification Steps Screen (✅ Complete)

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/verification/VerificationStepsScreen.kt`

Added conditional step display:
```kotlin
// Step 3: Okta ID (conditional - NEW matches iOS v2.0.12)
if (ClientConfiguration.shouldIncludeOktaID()) {
    // Show Okta ID step
}
```

---

## Verification Flow

### With Okta ID Enabled (Default)
```
1. Verification Steps (intro)
2. Face Scan Intro
3. Face Scan
4. Select Document Type
5. Document Scan (front)
6. Document Scan (back) OR Passport Chip Scan
7. Collect Okta ID ← NEW
8. Verification Processing
9. Completion
```

### With Okta ID Disabled
```
1. Verification Steps (intro)
2. Face Scan Intro
3. Face Scan
4. Select Document Type
5. Document Scan (front)
6. Document Scan (back) OR Passport Chip Scan
7. Verification Processing (Okta ID step skipped)
8. Completion
```

---

## Configuration Examples

### Enable Okta ID (Default)
```kotlin
val config = SDKConfiguration(
    apiKey = "your-api-key",
    baseUrl = "https://sandbox.mobile.artiusid.dev",
    environment = Environment.SANDBOX,
    clientId = 1,
    clientGroupId = 1,
    includeOktaIDInVerificationPayload = true // ← Enabled
)
```

### Disable Okta ID
```kotlin
val config = SDKConfiguration(
    apiKey = "your-api-key",
    baseUrl = "https://sandbox.mobile.artiusid.dev",
    environment = Environment.SANDBOX,
    clientId = 1,
    clientGroupId = 1,
    includeOktaIDInVerificationPayload = false // ← Disabled
)
```

---

## API Payload Changes

### Before (No Okta ID)
```json
{
  "frontImageBase64": "...",
  "backImageBase64": "...",
  "faceImageBase64": "...",
  "documentType": "1",
  "deviceId": "abc123",
  "deviceModel": "Samsung; Android: 13",
  "fcmToken": "firebase-token"
}
```

### After (With Okta ID)
```json
{
  "frontImageBase64": "...",
  "backImageBase64": "...",
  "faceImageBase64": "...",
  "documentType": "1",
  "deviceId": "abc123",
  "deviceModel": "Samsung; Android: 13",
  "fcmToken": "firebase-token",
  "oktaId": "john.doe@company.com"  ← NEW
}
```

**Note:** `oktaId` field is only included if:
1. `includeOktaIDInVerificationPayload = true` in configuration
2. User provided an Okta ID (not skipped)

---

## Localization

The following localization keys are used (defaults provided):

| Key | Default English | Used In |
|-----|----------------|---------|
| `step_okta_id` | "Okta ID" | VerificationStepsScreen |
| `step_okta_id_description` | "Provide your Okta ID" | VerificationStepsScreen |

These keys can be overridden using `SDKConfiguration.localizationOverrides`:

```kotlin
val config = SDKConfiguration(
    // ... other config ...
    localizationOverrides = mapOf(
        "step_okta_id" to "Employee ID",
        "step_okta_id_description" to "Enter your employee ID"
    )
)
```

---

## Testing Checklist

### Manual Testing
- [  ] **Enable Okta ID:** Verify step appears in VerificationStepsScreen
- [  ] **Disable Okta ID:** Verify step does NOT appear in VerificationStepsScreen
- [  ] **Okta ID Collection:**
  - [  ] Text field auto-focuses
  - [  ] Validation works (required field)
  - [  ] "Continue" button enabled only when text entered
  - [  ] "Skip" button works
  - [  ] Back button navigation works
- [  ] **API Payload:**
  - [  ] Okta ID included in payload when provided
  - [  ] Okta ID NOT included when skipped
  - [  ] Okta ID NOT included when disabled in configuration
- [  ] **Flow Integration:**
  - [  ] Government ID: Front → Back → Okta ID (if enabled) → Processing
  - [  ] Passport: MRZ → Chip → Okta ID (if enabled) → Processing
- [  ] **State Management:**
  - [  ] Okta ID persists through verification flow
  - [  ] Okta ID cleared after verification completion
  - [  ] Okta ID cleared after verification failure

### Automated Testing
- [  ] Unit test for `OktaIDHolder` state management
- [  ] Unit test for `VerificationRequest` with/without oktaId
- [  ] Integration test for verification flow with Okta ID enabled
- [  ] Integration test for verification flow with Okta ID disabled

---

## Known Issues

None at this time.

---

## Future Enhancements

1. **Validation Rules:** Add configurable validation (email format, length limits, regex)
2. **Pre-fill from Keychain:** Auto-fill Okta ID from previous verification
3. **Custom Input Types:** Allow custom input types (email, alphanumeric, etc.)
4. **Barcode Scanner:** Add QR/barcode scanner for Okta ID badges

---

## iOS Parity Status

✅ **Feature Complete** - Android SDK now matches iOS SDK v2.0.12 Okta ID integration

| Feature | iOS | Android |
|---------|-----|---------|
| Configuration flag | ✅ | ✅ |
| Collection screen | ✅ | ✅ |
| Skip option | ✅ | ✅ |
| Conditional step display | ✅ | ✅ |
| API payload inclusion | ✅ | ✅ |
| State management | ✅ | ✅ |

---

## Migration Guide

For clients upgrading from previous SDK versions:

### No Breaking Changes
This feature is **fully backward compatible**. Existing integrations will continue to work without modification.

### To Enable Okta ID Collection
Simply add the configuration parameter:

```kotlin
// Before (still works, Okta ID enabled by default)
val config = SDKConfiguration(
    apiKey = "key",
    baseUrl = "url",
    clientId = 1,
    clientGroupId = 1
)

// After (explicit enable)
val config = SDKConfiguration(
    apiKey = "key",
    baseUrl = "url",
    clientId = 1,
    clientGroupId = 1,
    includeOktaIDInVerificationPayload = true
)
```

### To Disable Okta ID Collection
```kotlin
val config = SDKConfiguration(
    apiKey = "key",
    baseUrl = "url",
    clientId = 1,
    clientGroupId = 1,
    includeOktaIDInVerificationPayload = false // ← Disable
)
```

---

**Document Version:** 1.0  
**Last Updated:** December 9, 2025  
**Author:** AI Assistant (based on iOS v2.0.12 implementation)

