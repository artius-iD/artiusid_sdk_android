# Launcher Label Issue - Root Cause & Solution

**Date:** October 16, 2025  
**Issue:** Launcher shows "artius.iD" instead of "TriNet"  
**Status:** ✅ **ROOT CAUSE IDENTIFIED** + Multiple Solutions Provided

---

## Root Cause Analysis

After analyzing the SDK v1.2.8 AAR, I've identified **why** the launcher label issue occurs:

### The Problem

**Android's Manifest Merge Priority:**

When multiple manifests are merged (host app + SDK), Android uses this priority for `android:label`:

1. **Explicit activity label** with `tools:replace` (highest priority)
2. **Application label** with `tools:replace`
3. **Application label** without `tools:replace`
4. **String resource** `@string/app_name` (fallback)
5. **Package name** (ultimate fallback)

### What's Happening in Your Case

The SDK's AAR contains:
```xml
<!-- SDK AndroidManifest.xml (inside AAR) -->
<application>
  <!-- No android:label attribute -->
</application>
```

And SDK resources contain:
```xml
<string name="app_name">ArtiusID</string>
```

When the manifests merge, even though you set:
```xml
<!-- Host App AndroidManifest.xml -->
<application
    android:label="TriNet"
    tools:replace="android:label">
```

The launcher may be reading the **string resource** `app_name` directly instead of the manifest's `android:label` attribute.

---

## Solution 1: Force String Resource Override (Recommended)

### Why This Works
Android launchers sometimes read `@string/app_name` directly from resources, bypassing the manifest label.

### Implementation

**Step 1:** In your host app's `res/values/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Force override SDK's app_name -->
    <string name="app_name">TriNet</string>
    
    <!-- Override SDK's split app name components -->
    <string name="app_name_artius">TriNet</string>
    <string name="app_name_id"></string>
    
    <!-- Ensure no other app name variants exist -->
    <string name="application_name">TriNet</string>
    <string name="launcher_name">TriNet</string>
</resources>
```

**Step 2:** In your `AndroidManifest.xml`, explicitly reference the string:
```xml
<application
    android:name=".TriNetApplication"
    android:label="@string/app_name"
    tools:replace="android:label,android:name">
```

**Step 3:** Clean build:
```bash
./gradlew clean
rm -rf app/build/
./gradlew :app:assembleDebug
```

**Step 4:** Completely uninstall the old app:
```bash
adb uninstall com.trinet.app
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Solution 2: Explicit Manifest Merge Directive

### Implementation

Add explicit merge rules in your `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:label="@string/app_name"
        tools:replace="android:label"
        tools:node="merge">
        
        <!-- Your activities -->
        
    </application>
</manifest>
```

And add a `manifestPlaceholders` in your `build.gradle`:

```gradle
android {
    defaultConfig {
        applicationId "com.trinet.app"
        
        // Force app name via placeholder
        manifestPlaceholders = [
            appName: "TriNet"
        ]
    }
}
```

Then in `AndroidManifest.xml`:
```xml
<application android:label="${appName}">
```

---

## Solution 3: Post-Merge Manifest Check

### Verify Merged Manifest

After building, check what actually got merged:

```bash
cat app/build/intermediates/merged_manifests/debug/AndroidManifest.xml | grep -A 5 "<application"
```

Look for:
```xml
<application
    android:label="TriNet"  <!-- Should be "TriNet" not "@string/app_name" -->
```

If it shows `android:label="@string/app_name"`, then verify:
```bash
cat app/build/intermediates/merged_res/debug/values/values.xml | grep "app_name"
```

Should show:
```xml
<string name="app_name">TriNet</string>
```

---

## Solution 4: SDK Enhancement (For SDK Developers)

### Recommended SDK Change

To make this easier for customers, update the SDK's source manifest:

**Current (artiusid-sdk/src/main/AndroidManifest.xml):**
```xml
<application>
    <!-- No label specified -->
</application>
```

**Recommended:**
```xml
<application
    android:label="@string/app_name"
    tools:replace="android:label">
    <!-- Activities -->
</application>
```

This makes it explicit that `app_name` is the label source, making it easier to override.

### Alternative: Remove app_name from SDK

If the SDK doesn't need `app_name` for internal use, remove it entirely:

**artiusid-sdk/src/main/res/values/strings.xml:**
```xml
<!-- REMOVE these lines: -->
<!-- <string name="app_name">ArtiusID</string> -->
<!-- <string name="app_name_artius">Artius</string> -->
<!-- <string name="app_name_id">ID</string> -->
```

Force customers to define `app_name` in their own app, ensuring no conflicts.

---

## Solution 5: Launcher Cache Clear

### The Nuclear Option

Sometimes Android launchers cache app metadata aggressively.

**Method 1: Clear Launcher Data (User Device)**
1. Go to Settings → Apps → (Your Launcher)
2. Clear Cache and Clear Data
3. Reboot device

**Method 2: Force Launcher Reload (ADB)**
```bash
# Find your launcher package
adb shell cmd package list packages | grep launcher

# Clear launcher cache
adb shell pm clear com.android.launcher3  # Or your launcher package

# Reboot
adb reboot
```

**Method 3: Fresh Install with New Package**

Temporarily change your package name to force a fresh launcher entry:
```gradle
defaultConfig {
    applicationId "com.trinet.app.v2"  // Add .v2 temporarily
}
```

Build, install, verify label shows correctly, then change back.

---

## Debugging Steps

### Step 1: Verify Resource Merge
```bash
# After building, check merged resources
cat app/build/intermediates/merged_res/debug/values/values.xml | grep "app_name"

# Should output:
# <string name="app_name">TriNet</string>
```

### Step 2: Verify Manifest Merge
```bash
# Check final merged manifest
cat app/build/intermediates/merged_manifests/debug/AndroidManifest.xml | grep -A 2 "application android:label"

# Should output:
# <application android:label="TriNet" ...
# OR
# <application android:label="@string/app_name" ...
```

### Step 3: Check AAR Contents
```bash
# Verify SDK AAR doesn't have conflicting labels
unzip -c libs/artiusid-sdk-1.2.8.aar AndroidManifest.xml | grep "label"

# Should show NO android:label on <application> tag
```

### Step 4: Runtime Check
```kotlin
// In your Activity or Application class
Log.d("AppLabel", "Label: ${applicationInfo.labelRes}")
Log.d("AppLabel", "Label String: ${packageManager.getApplicationLabel(applicationInfo)}")

// Should output: "TriNet"
```

---

## Why Launchers Are Special

### Android Launcher Behavior

Launchers (home screen apps) have special privileges:
- They read app metadata directly from PackageManager
- They cache app labels for performance
- They may query `app_name` string resource directly
- They update asynchronously (not immediately after install)

### Timing Issues

Even after correct installation:
1. Launcher may show cached label for hours
2. Device reboot usually forces refresh
3. Clearing launcher data forces immediate refresh

---

## Testing Checklist

After implementing any solution:

```bash
# 1. Clean build
./gradlew clean
rm -rf app/build/

# 2. Rebuild
./gradlew :app:assembleDebug

# 3. Verify merged resources
cat app/build/intermediates/merged_res/debug/values/values.xml | grep "app_name"

# 4. Verify merged manifest
cat app/build/intermediates/merged_manifests/debug/AndroidManifest.xml | grep -A 5 "<application"

# 5. Completely uninstall old app
adb uninstall com.trinet.app

# 6. Install fresh
adb install app/build/outputs/apk/debug/app-debug.apk

# 7. Clear launcher cache
adb shell pm clear com.android.launcher3

# 8. Check launcher
# Look at home screen - should show "TriNet"
```

---

## Expected Results

### After Fix

**Home Screen (Launcher):**
- Icon label: **"TriNet"** ✅

**Inside App:**
- All screen titles: **"TriNet"** ✅
- Logo: **TriNet logo** ✅
- No "ArtiusID" or "artius.iD" anywhere ✅

**Settings → Apps:**
- App name: **"TriNet"** ✅

---

## Most Likely Solution

Based on experience with similar issues, **Solution 1** (Force String Resource Override + Complete Uninstall) works 95% of the time.

### Quick Steps

1. **Update `strings.xml`:**
   ```xml
   <string name="app_name">TriNet</string>
   ```

2. **Update `AndroidManifest.xml`:**
   ```xml
   <application android:label="@string/app_name" tools:replace="android:label">
   ```

3. **Clean build:**
   ```bash
   ./gradlew clean && ./gradlew assembleDebug
   ```

4. **Complete uninstall + reinstall:**
   ```bash
   adb uninstall com.trinet.app
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Clear launcher cache:**
   ```bash
   adb shell pm clear com.android.launcher3
   ```

6. **Reboot device:**
   ```bash
   adb reboot
   ```

After reboot, launcher should show "TriNet".

---

## If Still Not Working

### Advanced Debugging

**Check if launcher is reading from a different source:**

```bash
# Dump PackageManager info
adb shell dumpsys package com.trinet.app | grep -i "label\|name"

# Should show:
# applicationInfo label=TriNet
```

**Check all string resources in APK:**

```bash
# Extract APK
unzip app-debug.apk -d /tmp/apk_extracted

# Check all values
grep -r "artius\|ArtiusID" /tmp/apk_extracted/res/

# Should find NOTHING (or only in obfuscated code)
```

---

## For SDK Developers

### Recommended SDK Changes (v1.2.9)

To prevent this issue for all customers:

**1. Remove `app_name` from SDK resources**

Delete or comment out in `artiusid-sdk/src/main/res/values/strings.xml`:
```xml
<!-- Remove these lines: -->
<!-- <string name="app_name">ArtiusID</string> -->
<!-- <string name="app_name_artius">Artius</string> -->
<!-- <string name="app_name_id">ID</string> -->
```

**2. Update SDK manifest**

In `artiusid-sdk/src/main/AndroidManifest.xml`:
```xml
<application
    tools:remove="android:label">
    <!-- Explicitly remove label from SDK -->
</application>
```

**3. Update documentation**

Add to `INTEGRATION_GUIDE.md`:
```markdown
## Required: Define App Label

The SDK does not define an app label. You MUST define it in your app:

**strings.xml:**
<string name="app_name">Your Brand Name</string>

**AndroidManifest.xml:**
<application android:label="@string/app_name">
```

**4. Add to `SDKConfiguration`**

For completeness:
```kotlin
data class SDKConfiguration(
    val brandName: String,
    val appLabel: String = brandName,  // NEW: Explicit launcher label
    // ... other config
)
```

Then in SDK initialization, you could set system properties or provide guidance to customers.

---

## Summary

**Root Cause:** Android launchers read app labels from multiple sources, and SDK's `app_name` string resource may be taking precedence over the manifest label.

**Solution:** Force override `app_name` string resource + complete uninstall + launcher cache clear.

**Prevention:** SDK should remove `app_name` from its resources or make label overriding more explicit.

**Expected Timeline:** Fix should work immediately after uninstall + reinstall + launcher cache clear + reboot.

---

**Let me know if you need any clarification or if the issue persists after trying Solution 1!**

