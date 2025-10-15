# ArtiusID Android SDK v1.2.3 Release Notes

**Release Date:** October 15, 2025
**Release Type:** Critical Bug Fix

## 🐛 Critical Fixes

### 1. NFC Stale Tag Crash Fix
**Issue:** App crashed when pressing "Scan Passport Chip" button
- **Error:** `SecurityException: Permission Denial: Tag is out of date`
- **Root Cause:** Attempting to check `IsoDep.isConnected` on stale NFC tag references
- **Fix:** Added comprehensive try-catch blocks for `SecurityException` in all NFC connection checks
- **Impact:** App no longer crashes; gracefully handles stale tags and continues polling

**Files Modified:**
- `PassportChipScanScreen.kt` (lines 553-564, 274-283, 298-303)

### 2. NFC Retry Loop Fix
**Issue:** NFC scan screen continued scanning indefinitely after 3 failed attempts
- **Root Cause:** Multiple overlapping retry coroutines due to screen recomposition
- **Fix:** 
  - Added `isProcessingNfc` flag to prevent multiple retry loops
  - Removed state changes that restarted the outer polling loop
  - Ensured proper exit after max retries
- **Impact:** After 3 failed NFC attempts, app now properly stops and proceeds to verification

**Files Modified:**
- `PassportChipScanScreen.kt` (lines 545, 584-640)

### 3. Verification Request Parameter Alignment
**Issue:** Backend stored procedure receiving incorrect parameter count
- **Fix:** Ensured `backImageBase64` always included in JSON (empty string for passports)
- **Implementation:** Matches iOS SDK exactly - always sends 7 parameters
- **Impact:** Android SDK now sends identical payload format as iOS

**Files Modified:**
- `VerificationRequest.kt` - `toOrderedMap()` function

## ⚠️ Known Backend Issue

**Verification still failing with HTTP 400:**
```
Error: Incorrect number of arguments for PROCEDURE artiusid_db.sp_VERIFICATION_CreateVerification; 
expected 7, got 6
```

**Android SDK is sending 7 parameters:**
1. `frontImageBase64` - ✅ Populated
2. `backImageBase64` - ✅ Empty string for passports
3. `faceImageBase64` - ✅ Populated
4. `documentType` - ✅ "2" for passport
5. `deviceId` - ✅ Device ID
6. `deviceModel` - ✅ Device model
7. `fcmToken` - ✅ FCM token

**Backend team must fix:** The stored procedure parameter mapping or count mismatch.

## 📦 Deployment

- **GitLab:** Committed and pushed to `main` branch
- **GitHub:** Published to https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3
- **AAR Size:** 25MB (obfuscated)
- **Sample App Size:** 173MB (obfuscated)

## 🔧 Technical Details

### NFC Error Handling
```kotlin
val isConnected = try {
    currentIsoDep?.isConnected == true
} catch (e: SecurityException) {
    Log.w("PassportChipScan", "⚠️ Stale NFC tag detected, clearing...")
    StandaloneAppActivity.setIsoDep(null)
    StandaloneAppActivity.currentNfcTag = null
    false
} catch (e: Exception) {
    Log.w("PassportChipScan", "⚠️ Error checking IsoDep connection: ${e.message}")
    false
}
```

### Retry Loop Prevention
```kotlin
var isProcessingNfc by remember { mutableStateOf(false) }

if (isConnected && currentIsoDep != null && !isProcessingNfc) {
    isProcessingNfc = true // Prevent re-entry
    // Start retry loop...
}
```

## 📋 Testing Performed

1. ✅ NFC scan no longer crashes on stale tags
2. ✅ After 3 failed NFC attempts, properly proceeds to verification
3. ✅ Verification request contains all 7 parameters
4. ⚠️ Verification fails due to backend stored procedure issue (not SDK issue)

## 🚀 Next Steps

1. **Backend Team:** Fix `sp_VERIFICATION_CreateVerification` parameter count/mapping
2. **QA:** Test full passport verification flow after backend fix
3. **Documentation:** Update customer docs with NFC troubleshooting tips

---

**Git Commits:**
- `1f2e99f` - Fix NFC passport chip scanning issues and verification parameter handling
- `e523bdd` - Bump version to 1.2.3

**GitHub Release:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3
