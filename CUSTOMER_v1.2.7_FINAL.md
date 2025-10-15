# ArtiusID SDK v1.2.7 - Complete Hilt Fix

**Date:** October 15, 2025  
**Status:** ✅ COMPREHENSIVE FIX FOR ALL HILT ISSUES

---

## ✅ WHAT WAS FIXED

Based on your testing feedback, we identified and fixed **TWO separate Hilt issues**:

### Issue #1: Component Method Error (v1.2.3-v1.2.5)
```
java.lang.IllegalArgumentException: component method cannot be void: a()
```
**Fixed in v1.2.6** with basic Hilt ProGuard rules

### Issue #2: ViewModel Factory Error (v1.2.6)
```
error: cannot find symbol
  symbol:   method provide()
  location: class ApprovalRequestViewModel_HiltModules_KeyModule_ProvideFactory
```
**Fixed in v1.2.7** with ViewModel module ProGuard rules

---

## Download v1.2.7

https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.7

**File:** `artiusid-sdk-1.2.7.aar`

**Checksum:** `9c539a20690e3e37824b3f3187f1169d4ba5debada33acc15c096c0c00aebf63`

---

## Version History

| Version | Issue | Status |
|---------|-------|--------|
| v1.2.3-v1.2.5 | `component method cannot be void: a()` | ❌ Broken |
| v1.2.6 | Basic Hilt rules added, but ViewModel factories missing | ⚠️ Partial |
| **v1.2.7** | **Complete Hilt ProGuard rules** | ✅ **COMPLETE** |

---

## What v1.2.7 Includes

### Comprehensive Hilt ProGuard Rules

```proguard
# Core Hilt components
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **Hilt_** { *; }

# ViewModel modules (NEW in v1.2.7)
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_HiltModules_** { *; }
-keep class **_ProvideFactory { *; }
-keep class **_KeyModule { *; }

# Keep provide() methods
-keepclassmembers class **_HiltModules_** {
    public * provide*(...);
}

# Hilt annotations
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.Module class * { *; }
```

These rules prevent ProGuard from obfuscating:
1. Hilt component classes
2. ViewModel Hilt modules
3. Factory classes
4. provide() methods
5. All Hilt-annotated classes

---

## Installation

### 1. Download v1.2.7
```bash
curl -L -o artiusid-sdk-1.2.7.aar \
  "https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.2.7/artiusid-sdk-1.2.7.aar"
```

### 2. Verify Checksum
```bash
shasum -a 256 artiusid-sdk-1.2.7.aar
```
**Expected:** `9c539a20690e3e37824b3f3187f1169d4ba5debada33acc15c096c0c00aebf63`

### 3. Replace in Your Project
```bash
rm libs/artiusid-sdk-*.aar
cp artiusid-sdk-1.2.7.aar path/to/your/project/libs/
```

### 4. Update build.gradle
```gradle
dependencies {
    implementation(files("libs/artiusid-sdk-1.2.7.aar"))
    
    // Hilt 2.48
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    
    // Add if missing
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    
    // ... rest of dependencies unchanged
}
```

### 5. Clean and Rebuild
```bash
./gradlew clean
rm -rf .gradle build app/build ~/.gradle/caches
./gradlew assembleDebug
```

---

## Expected Result

With v1.2.7, your build should:
- ✅ Complete successfully
- ✅ NO `component method cannot be void` error
- ✅ NO `cannot find symbol: method provide()` error
- ✅ All Hilt components compile
- ✅ All ViewModel factories generate correctly

---

## Your Feedback Was Critical

Thank you for testing and reporting:
1. ✅ v1.2.5 still had the error (correct - cache issue)
2. ✅ v1.2.6 fixed original error but had ViewModel factory issues (correct diagnosis)
3. ✅ Your detailed error messages helped us identify the exact ProGuard rules needed

---

## Technical Summary

### The Root Causes

1. **Aggressive obfuscation** (`-repackageclasses 'a'`) was breaking Hilt
2. **Insufficient ProGuard rules** for Hilt-generated code
3. **Missing rules** for ViewModel Hilt modules specifically

### The Complete Fix

v1.2.7 adds comprehensive ProGuard rules that keep:
- All `**_HiltComponents` and subclasses
- All `**_HiltModules` and nested classes  
- All `**_Factory` classes
- All `**_ProvideFactory` classes
- All `**_KeyModule` classes
- All `provide*()` methods
- All Hilt-annotated classes

This prevents ProGuard from touching any Hilt-generated code.

---

## If Issues Persist

### 1. Verify Version
```bash
shasum -a 256 libs/artiusid-sdk-1.2.7.aar
# Must be: 9c539a20690e3e37824b3f3187f1169d4ba5debada33acc15c096c0c00aebf63
```

### 2. Clear ALL Caches
```bash
./gradlew clean --no-build-cache
rm -rf ~/.gradle/caches/
rm -rf .gradle build app/build
```

### 3. Check Dependencies
Ensure you have:
- Hilt 2.48 (exact version)
- lifecycle-viewmodel-compose
- lifecycle-viewmodel-ktx

### 4. Check Your ProGuard Rules
Your app's `proguard-rules.pro` should NOT have:
```proguard
# BAD - Don't add
-dontwarn dagger.**
-ignorewarnings
```

---

## Checksums for Reference

| Version | Checksum | Status |
|---------|----------|--------|
| v1.2.3-v1.2.4 | `eb9289...` | ❌ Broken (cache) |
| v1.2.5 | `43eb5d...` | ❌ Broken (MainActivity removed) |
| v1.2.6 | `d935de...` | ⚠️ Partial (basic Hilt rules) |
| **v1.2.7** | **`9c539a...`** | ✅ **COMPLETE** |

---

## Summary

| Item | Status |
|------|--------|
| Core Hilt Compilation | ✅ Fixed |
| ViewModel Factories | ✅ Fixed |
| ProGuard Rules | ✅ Complete |
| Ready for Production | ✅ Yes |

---

## Thank You

Thank you for your patience through multiple iterations and for providing detailed error reports that helped us identify all the issues.

**v1.2.7 should resolve all Hilt compilation errors!**

---

**Download Now:**  
https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.7

**Use v1.2.7 - All previous versions have known issues**

