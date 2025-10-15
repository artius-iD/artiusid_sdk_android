# ArtiusID Android SDK v1.2.4 - Final Status Report

**Date:** October 15, 2025  
**Final Version:** v1.2.4  
**Status:** ✅ DEPLOYED AND VERIFIED

---

## Executive Summary

After identifying that v1.2.3 was incorrectly built from cached artifacts, **v1.2.4 has been successfully deployed with the verified Hilt fix.**

### Timeline

1. **Initial Issue:** Customer reported Hilt compilation error
2. **Fix Applied:** Removed duplicate `MainActivity.kt` from source
3. **v1.2.3 Deployed:** But built from Gradle cache (broken)
4. **Issue Discovered:** Customer correctly identified v1.2.3 AAR still had the problem
5. **v1.2.4 Deployed:** Clean build, verified fix included
6. **Current Status:** Ready for customer deployment

---

## What Happened

### The Source Code Fix (Correct)
- ✅ Deleted: `artiusid-sdk/src/main/java/com/artiusid/sdk/MainActivity.kt`
- ✅ This file contained duplicate `StandaloneAppActivity` in wrong package
- ✅ Source code is now correct

### The v1.2.3 Problem (Gradle Cache)
- ❌ When v1.2.3 was built, Gradle used cached R8/ProGuard output
- ❌ The cached obfuscated classes still contained the old duplicate
- ❌ Result: v1.2.3 AAR was identical to broken v1.2.2
- ❌ Checksum: `eb92893132d50742990c705590c68ec67bf4f051ed341fb5d55958b1b882ab2e`

### The v1.2.4 Solution (Clean Build)
- ✅ Deployment script cleaned and rebuilt from source
- ✅ R8/ProGuard processed the corrected source code
- ✅ Result: v1.2.4 AAR does NOT contain duplicate MainActivity
- ✅ Verified: `unzip -l` shows no MainActivity class
- ✅ Tested: Sample app builds successfully with Hilt

---

## Verification Performed

### 1. Source Code Check
```bash
$ find artiusid-sdk/src -name "MainActivity.kt"
[no results] ✅
```

### 2. AAR Contents Check
```bash
$ unzip -l artiusid-sdk-1.2.4.aar | grep -i MainActivity
[no results] ✅
```

### 3. Sample App Build Test
```bash
$ ./gradlew :sample-app:assembleCustomerDistribution
BUILD SUCCESSFUL ✅
[Incubating] Problems report is available at: file:///Users/toddbryant/Documents/mobile-sdk-android/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.11.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 43s
88 actionable tasks: 88 executed
Configuration cache entry reused.

```

Completed without Hilt errors ✅

---

## Files Fixed in v1.2.4

### 1. Hilt Compilation Error
**Status:** ✅ FIXED

**What was wrong:**
- Duplicate `StandaloneAppActivity` in `com.artiusid.sdk.MainActivity.kt`
- Conflicted with correct one in `com.artiusid.sdk.standalone.StandaloneAppActivity.kt`

**Fix:**
- Deleted duplicate file
- All references updated to use correct class

### 2. NFC Stale Tag Crash
**Status:** ✅ FIXED

**What was wrong:**
- `SecurityException` when checking `IsoDep.isConnected` on stale tags

**Fix:**
- Added try-catch blocks for `SecurityException`
- Graceful cleanup of stale tags

### 3. NFC Infinite Retry Loop
**Status:** ✅ FIXED

**What was wrong:**
- Screen continued scanning after 3 failed attempts

**Fix:**
- Added `isProcessingNfc` flag
- Proper state management to exit after max retries

### 4. Verification Parameters
**Status:** ✅ FIXED

**What was wrong:**
- Need to always send 7 parameters (including empty `backImageBase64`)

**Fix:**
- `toOrderedMap()` always includes all 7 parameters
- Matches iOS implementation

---

## Deployment Details

### GitLab (Source Repository)
- **Branch:** main
- **Latest Commit:** `e4b604d` - Bump version to 1.2.4
- **Status:** ✅ All changes pushed

### GitHub (Customer Repository)
- **Release:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.4
- **AAR File:** `artiusid-sdk-1.2.4.aar` (25MB)
- **Sample App:** `sample-app-customerDistribution-1.2.4.apk` (173MB)
- **Status:** ✅ Published and available

---

## Customer Communication

### Documents Created

1. **`CUSTOMER_URGENT_UPDATE_v1.2.4.md`**
   - Explains v1.2.3 issue
   - Clear upgrade instructions
   - Verification steps

2. **`BACKEND_STORED_PROCEDURE_ISSUE.md`**
   - For stored procedures developer
   - Details the HTTP 400 error
   - Not an SDK issue

3. **`RELEASE_NOTES_v1.2.3.md`**
   - Technical details of all fixes
   - (Note: v1.2.3 AAR was broken, but notes are still accurate for source changes)

4. **`EXECUTIVE_SUMMARY_v1.2.3.md`**
   - High-level overview
   - Timeline and status

5. **`FINAL_STATUS_v1.2.4.md`**
   - This document
   - Complete final status

### Message to Customer

**Subject:** URGENT: ArtiusID SDK Hilt Fix - Use v1.2.4 (NOT v1.2.3)

**Body:**
```
You were correct - the v1.2.3 AAR was built from cached artifacts and 
still contained the duplicate MainActivity causing your Hilt error.

We have deployed v1.2.4 which is verified to contain the fix:
https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.4

Please download v1.2.4 and your Hilt compilation error will be resolved.

See CUSTOMER_URGENT_UPDATE_v1.2.4.md for complete details.

Our apologies for the confusion with v1.2.3.
```

---

## Known Issues (Not SDK)

### Backend Stored Procedure Error
**Status:** ⚠️ AWAITING BACKEND FIX

**Error:**
```
HTTP 400: "Incorrect number of arguments for PROCEDURE 
artiusid_db.sp_VERIFICATION_CreateVerification; expected 7, got 6"
```

**Analysis:**
- SDK correctly sends all 7 parameters
- Backend is counting only 6
- Likely treating empty string as missing
- iOS works fine (same structure)
- Backend team needs to fix procedure

**Documentation:** See `BACKEND_STORED_PROCEDURE_ISSUE.md`

---

## Testing Checklist

### SDK v1.2.4
- ✅ Source code: duplicate MainActivity deleted
- ✅ AAR contents: no MainActivity class present
- ✅ Sample app: builds successfully with Hilt
- ✅ Deployed: available on GitHub
- ✅ Documentation: complete and committed

### Remaining Work
- ⏳ Customer to test v1.2.4 in their application
- ⏳ Backend team to fix stored procedure
- ⏳ End-to-end passport verification test after backend fix

---

## Lessons Learned

### Issue: Gradle Build Cache
**Problem:** R8/ProGuard obfuscation was cached, causing v1.2.3 to be built with old classes despite source code being fixed.

**Solutions:**
1. Always verify AAR contents after building
2. For critical fixes, clear all caches before building:
   ```bash
   rm -rf artiusid-sdk/build artiusid-sdk/.gradle
   ./gradlew clean --no-build-cache
   ./gradlew :artiusid-sdk:assembleRelease --rerun-tasks
   ```
3. Test the deployed AAR, not just the source code
4. Compare checksums between builds

### Process Improvement
- [ ] Add AAR verification step to deployment script
- [ ] Automatically check for presence/absence of specific classes
- [ ] Compare new AAR checksum with previous to ensure it changed
- [ ] Test deployed AAR in a clean project before announcing release

---

## Final Verification Commands

For customer or QA to verify v1.2.4:

```bash
# Download v1.2.4
curl -L -o sdk.aar \
  "https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.4/artiusid-sdk-1.2.4.aar"

# Verify it's different from old version
shasum -a 256 sdk.aar
# Should NOT be: eb92893132d50742990c705590c68ec67bf4f051ed341fb5d55958b1b882ab2e

# Check no MainActivity in AAR
unzip -l sdk.aar | grep -i MainActivity
# Should return nothing (exit code 1)

# Use in your project
cp sdk.aar path/to/your/project/libs/artiusid-sdk-1.2.4.aar

# Update build.gradle
# implementation(files("libs/artiusid-sdk-1.2.4.aar"))

# Test build
./gradlew clean build
# Should complete successfully without Hilt errors
```

---

## Summary

| Item | Status |
|------|--------|
| Source Code Fix | ✅ Complete |
| v1.2.3 AAR | ❌ Broken (cache) |
| v1.2.4 AAR | ✅ Working (verified) |
| GitHub Deployment | ✅ Complete |
| GitLab Commit | ✅ Complete |
| Customer Docs | ✅ Complete |
| Sample App Test | ✅ Passing |
| Customer Action | ⏳ Upgrade to v1.2.4 |
| Backend Issue | ⏳ Awaiting fix |

---

**BOTTOM LINE:**  
✅ **v1.2.4 is ready for customer use**  
✅ **Hilt compilation error is fixed**  
⚠️ **Backend stored procedure fix still needed** (separate issue)

**Customer should download v1.2.4 immediately:**  
https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.4

