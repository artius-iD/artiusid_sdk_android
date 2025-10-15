# URGENT: ArtiusID SDK v1.2.4 - Corrected Build

**Date:** October 15, 2025  
**Priority:** CRITICAL  
**Action Required:** IMMEDIATE UPGRADE

---

## Issue Identified and Resolved

### The Problem

**v1.2.3 AAR was built from cached artifacts and did NOT contain the Hilt fix.**

Despite the source code being corrected (duplicate `MainActivity.kt` removed), the v1.2.3 AAR published to GitHub was built using Gradle's build cache, which contained the OLD obfuscated classes with the duplicate `StandaloneAppActivity`.

**Checksum Evidence:**
- v1.2.3 AAR: `eb92893132d50742990c705590c68ec67bf4f051ed341fb5d55958b1b882ab2e` ❌
- This was the SAME checksum as the broken AAR

### The Solution

**v1.2.4 has been built and deployed with the correct fix.**

✅ **VERIFIED:** v1.2.4 AAR does NOT contain the duplicate MainActivity  
✅ **CONFIRMED:** v1.2.4 builds successfully with Hilt  
✅ **TESTED:** Sample app compiles and runs without errors

---

## CUSTOMER ACTION REQUIRED

### Download v1.2.4 (NOT v1.2.3)

**GitHub Release:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.4

**File:** `artiusid-sdk-1.2.4.aar`

### Steps to Upgrade

1. **Download the corrected AAR:**
   ```bash
   curl -L -o artiusid-sdk-1.2.4.aar \
     "https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.4/artiusid-sdk-1.2.4.aar"
   ```

2. **Replace in your project:**
   - Delete the old `artiusid-sdk-1.2.3.aar` (or older)
   - Place `artiusid-sdk-1.2.4.aar` in your `libs/` directory

3. **Update build.gradle:**
   ```gradle
   dependencies {
       implementation(files("libs/artiusid-sdk-1.2.4.aar"))
       // ... rest of dependencies
   }
   ```

4. **Clean and rebuild:**
   ```bash
   ./gradlew clean build
   ```

5. **Verify success:**
   - Build should complete WITHOUT the Hilt error:
     ❌ NO MORE: `component method cannot be void: a()`
   - Your app should compile successfully

---

## What Was Fixed

### Root Cause (Same as before)
A duplicate `StandaloneAppActivity` class in the wrong package was causing Hilt component conflicts.

### The Issue with v1.2.3
The v1.2.3 release notes claimed the fix was included, but the AAR was built from Gradle's cached artifacts, which still contained the old obfuscated code with the duplicate class.

### v1.2.4 Verification
- ✅ Built with all caches cleared
- ✅ Verified MainActivity class NOT present in AAR
- ✅ Sample app builds successfully
- ✅ Hilt compilation works perfectly

---

## Version History

| Version | Status | Issue |
|---------|--------|-------|
| v1.2.1 | ❌ Broken | Contains duplicate MainActivity |
| v1.2.2 | ❌ Broken | Contains duplicate MainActivity |
| v1.2.3 | ❌ Broken | Built from cache, still has duplicate |
| **v1.2.4** | ✅ **WORKING** | **Correctly built, fix verified** |

---

## Verification Steps

After upgrading to v1.2.4, verify the fix:

### 1. Check you have the correct AAR
```bash
shasum -a 256 artiusid-sdk-1.2.4.aar
```
**Should NOT be:** `eb92893132d50742990c705590c68ec67bf4f051ed341fb5d55958b1b882ab2e`

### 2. Test build
```bash
./gradlew assembleDebug
```
**Should complete successfully** without Hilt errors

### 3. Verify in logs
No errors containing:
- ❌ `component method cannot be void`
- ❌ `IllegalArgumentException`
- ❌ `hiltJavaCompile` failures

---

## Our Apologies

We sincerely apologize for the confusion with v1.2.3. The release notes accurately described the source code changes, but we failed to verify that the deployed AAR was built from the corrected source rather than cached artifacts.

**v1.2.4 is verified to contain the fix and will resolve your Hilt compilation errors.**

---

## Support

If you continue to experience issues after upgrading to v1.2.4:

1. Verify you're using the v1.2.4 AAR (check filename and checksum)
2. Ensure you've cleaned your project: `./gradlew clean`
3. Delete your local Gradle cache if needed: `rm -rf ~/.gradle/caches/`
4. Contact support with:
   - Confirmation of SDK version
   - Full build error log
   - Your `build.gradle` configuration

---

## Summary

- ❌ **v1.2.3:** Broken (built from cache)
- ✅ **v1.2.4:** Fixed (verified working)
- 🚀 **Action:** Download v1.2.4 immediately
- 🔗 **Link:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.4

**The Hilt compilation error will be resolved once you upgrade to v1.2.4.**

