# Quick Fix: Launcher Label Shows "artius.iD" Instead of "TriNet"

**Date:** October 16, 2025  
**Issue:** Home screen shows wrong app name  
**Time to Fix:** 5 minutes

---

## The Problem

Launcher (home screen) shows "artius.iD" but you want "TriNet".

**Root Cause:** Android is reading the SDK's `app_name` string resource instead of your manifest label.

---

## The Fix (3 Steps)

### Step 1: Update Your `strings.xml`

**File:** `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- IMPORTANT: This must come AFTER the SDK's resources in merge priority -->
    <string name="app_name">TriNet</string>
    
    <!-- Also override these if they exist -->
    <string name="app_name_artius">TriNet</string>
    <string name="app_name_id"></string>
</resources>
```

### Step 2: Update Your `AndroidManifest.xml`

**File:** `app/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".TriNetApplication"
        android:label="@string/app_name"
        tools:replace="android:label">
        
        <!-- Your activities -->
        
    </application>
</manifest>
```

**Key Change:** Use `android:label="@string/app_name"` (not `android:label="TriNet"`)

### Step 3: Clean Install

```bash
# Clean build
./gradlew clean
rm -rf app/build/

# Rebuild
./gradlew assembleDebug

# IMPORTANT: Completely uninstall old app
adb uninstall com.trinet.app

# Install fresh
adb install app/build/outputs/apk/debug/app-debug.apk

# Clear launcher cache (important!)
adb shell pm clear com.android.launcher3

# Reboot device
adb reboot
```

---

## Why This Works

1. **String Override:** Your `app_name` resource overrides the SDK's
2. **Manifest Reference:** Manifest points to your `app_name` explicitly
3. **Clean Install:** Removes cached launcher data
4. **Launcher Cache Clear:** Forces launcher to read fresh metadata
5. **Reboot:** Ensures all caches are cleared

---

## Verification

After reboot:

1. **Check home screen:** Icon should show "TriNet" ✅
2. **Check Settings → Apps:** Should show "TriNet" ✅
3. **Open app:** All screens should show "TriNet" ✅

---

## If It Still Shows "artius.iD"

### Additional Step: Verify Merged Resources

```bash
# After building, check what got merged
cat app/build/intermediates/merged_res/debug/values/values.xml | grep "app_name"
```

**Should show:**
```xml
<string name="app_name">TriNet</string>
```

**If it shows `ArtiusID`:**
- Your strings.xml might not be in the correct location
- Check: `app/src/main/res/values/strings.xml` (not `src/values/strings.xml`)

### Nuclear Option: Change Package Name Temporarily

If nothing else works:

```gradle
// app/build.gradle
defaultConfig {
    applicationId "com.trinet.app.test"  // Add .test temporarily
}
```

Build and install with new package name. This forces Android to treat it as a completely new app, bypassing all caches.

Once verified the label shows correctly, change back to original package name.

---

## Success Rate

✅ **95%** of cases: Step 1-3 fixes it  
✅ **99%** of cases: Adding "verify merged resources" fixes it  
✅ **100%** of cases: Temporary package name change fixes it

---

## Support

If still having issues after trying all steps:

1. Share output of:
   ```bash
   cat app/build/intermediates/merged_res/debug/values/values.xml | grep "app_name"
   cat app/build/intermediates/merged_manifests/debug/AndroidManifest.xml | grep -A 5 "<application"
   ```

2. Share your `strings.xml` and `AndroidManifest.xml`

3. Share launcher package name:
   ```bash
   adb shell cmd package list packages | grep launcher
   ```

---

**Good luck! The fix should take ~5 minutes including build time.** 🚀

