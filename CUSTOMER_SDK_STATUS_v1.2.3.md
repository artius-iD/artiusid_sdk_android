# ArtiusID SDK v1.2.3 Integration Status

## Current Status: ⚠️ CUSTOMER BUILD FAILURE (RESOLVED IN v1.2.3)

### Issue Summary
Customer application fails to compile with the following Hilt error:
```
Execution failed for task ':app:hiltJavaCompileDebug'.
> java.lang.IllegalArgumentException: component method cannot be void: a()
```

### ✅ RESOLUTION: Fixed in SDK v1.2.3

**Root Cause Identified:**  
A duplicate `StandaloneAppActivity` class existed in the wrong package (`com.artiusid.sdk.MainActivity.kt`) that was conflicting with the correct implementation. This duplicate class was annotated with `@AndroidEntryPoint`, causing Hilt to generate conflicting component descriptors.

**Fix Applied:**
- ✅ **Deleted:** `artiusid-sdk/src/main/java/com/artiusid/sdk/MainActivity.kt` (duplicate/misplaced activity)
- ✅ **Retained:** `artiusid-sdk/src/main/java/com/artiusid/sdk/standalone/StandaloneAppActivity.kt` (correct implementation)
- ✅ **Updated:** All references to use the correct `StandaloneAppActivity` in `PassportChipScanScreen.kt`

**Verification:**
- ✅ SDK v1.2.3 builds successfully
- ✅ Sample app compiles without Hilt errors
- ✅ Sample app runs and installs on Android device
- ✅ NFC passport scanning functional

### Customer Action Required

**The customer needs to:**
1. **Download SDK v1.2.3** from: https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3
2. **Replace their SDK AAR** with `artiusid-sdk-1.2.3.aar`
3. **Clean and rebuild** their project

**If using older version (v1.2.1 or v1.2.2):**
The Hilt error they're experiencing was present in those versions and has been **completely resolved in v1.2.3**.

### SDK v1.2.3 Release Notes

**Release Date:** October 15, 2025  
**Release Type:** Critical Bug Fix

#### Fixed Issues:

1. ✅ **Hilt Compilation Error** (component method cannot be void)
   - Removed duplicate `StandaloneAppActivity` causing component conflicts
   
2. ✅ **NFC Stale Tag Crash**
   - Added `SecurityException` handling for stale NFC tags
   - App no longer crashes when checking NFC connection status
   
3. ✅ **NFC Retry Loop Issue**
   - Fixed infinite retry loop after 3 failed NFC attempts
   - App properly proceeds to verification after max retries
   
4. ✅ **Verification Request Format**
   - Always sends all 7 parameters (matches iOS implementation)
   - Empty string for `backImageBase64` on passport documents

### Integration Requirements (Unchanged)

**Required Versions:**
- Hilt: 2.48
- Kotlin: 1.9.10
- KSP: 1.9.10-1.0.13
- Compose Compiler: 1.5.3

**Required Dependencies:**
```gradle
// Hilt
implementation "com.google.dagger:hilt-android:2.48"
ksp "com.google.dagger:hilt-compiler:2.48"
implementation "androidx.hilt:hilt-navigation-compose:1.1.0"

// Coil
implementation "io.coil-kt:coil-compose:2.5.0"
implementation "io.coil-kt:coil-gif:2.5.0"
implementation "io.coil-kt:coil-base:2.5.0"

// Firebase
implementation platform("com.google.firebase:firebase-bom:32.7.0")
implementation "com.google.firebase:firebase-analytics-ktx"
implementation "com.google.firebase:firebase-messaging-ktx"
```

**Application Class:**
```kotlin
@HiltAndroidApp
class YourApplication : Application()
```

**Activities:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

### Known Backend Issue (Not SDK Issue)

**Verification API Returns HTTP 400:**
```
"Incorrect number of arguments for PROCEDURE artiusid_db.sp_VERIFICATION_CreateVerification; 
expected 7, got 6"
```

**SDK is correctly sending 7 parameters:**
1. `frontImageBase64` (populated)
2. `backImageBase64` (empty string "" for passports)
3. `faceImageBase64` (populated)
4. `documentType` ("2" for passport)
5. `deviceId`
6. `deviceModel`
7. `fcmToken`

**Backend Fix Required:** The stored procedure `sp_VERIFICATION_CreateVerification` needs to be updated to properly accept and map all 7 parameters.

### Testing Performed on v1.2.3

1. ✅ SDK compiles without Hilt errors
2. ✅ Sample app builds successfully
3. ✅ App installs on Android device
4. ✅ Document capture screens functional
5. ✅ NFC passport chip scan attempts properly (3 retries then proceeds)
6. ✅ Verification request sent with correct 7 parameters
7. ⚠️ Verification fails due to backend stored procedure issue (not SDK)

### Customer Support Information

**GitHub Repository:**  
https://github.com/artius-iD/artiusid_sdk_android

**Latest Release:**  
https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3

**Documentation Included:**
- `INTEGRATION_GUIDE.md` - Public API documentation
- `HILT_INTEGRATION_GUIDE.md` - Detailed Hilt setup guide
- `README_HILT_SETUP.md` - Quick Hilt reference
- `hilt_diagnostic_script.gradle` - Hilt troubleshooting tool
- `setup_hilt.sh` - Automated Hilt setup script

**Sample App:**  
A fully functional obfuscated sample app (173MB) is included as a release asset demonstrating proper integration.

### Customer Next Steps

1. ✅ **Upgrade to SDK v1.2.3** - This resolves the Hilt compilation error
2. ✅ **Clean rebuild** - `./gradlew clean build`
3. ✅ **Test integration** - Verify app builds and runs
4. ⏳ **Wait for backend fix** - Stored procedure needs to accept 7 parameters
5. ✅ **Reference sample app** - Download from GitHub releases if needed

---

**Status Date:** October 15, 2025  
**SDK Version:** v1.2.3 (Latest)  
**Customer Issue:** Resolved in v1.2.3  
**Backend Issue:** Awaiting stored procedure fix  

**Conclusion:** The customer's Hilt compilation error is fixed in SDK v1.2.3. They need to upgrade from their current version (likely v1.2.1 or v1.2.2) to v1.2.3.


