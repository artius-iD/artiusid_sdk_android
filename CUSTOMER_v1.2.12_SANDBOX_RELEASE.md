# 🎉 SDK v1.2.12 Release - Sandbox Environment Support

**Release Date:** October 17, 2025  
**Priority:** CRITICAL - Fixes Verification URL Issues  
**Status:** ✅ DEPLOYED TO GITHUB

---

## 🚀 **What's New in v1.2.12**

### **Environment.SANDBOX Added!**

SDK now supports **Sandbox environment** for testing and production use.

**Before v1.2.12 (Didn't Work):**
```kotlin
environment = Environment.DEVELOPMENT  // Wrong URLs for your backend!
```

**After v1.2.12 (Works Correctly):**
```kotlin
environment = Environment.SANDBOX  // Correct Sandbox URLs! ✅
```

---

## 🌐 **Automatic URL Configuration**

v1.2.12 automatically configures backend URLs based on `SDKConfiguration.environment`.

**No more manual UrlBuilder configuration!**

### **Sandbox URLs (Automatically Set):**

```
Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
Certificate:  https://sandbox.registration.artiusid.dev/LoadCertificateFunction
Auth:         https://sandbox.mobile.artiusid.dev/auth/api/auth
```

---

## 📋 **How to Upgrade**

### **Step 1: Download SDK v1.2.12**

```
https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.12
```

**Files:**
- `artiusid-sdk-1.2.12.aar` (25 MB)
- `sample-app-obfuscated.apk` (173 MB) - Optional testing app

### **Step 2: Update Your Project**

Replace the old AAR:
```
app/libs/artiusid-sdk-1.2.11.aar  →  app/libs/artiusid-sdk-1.2.12.aar
```

Update `build.gradle`:
```gradle
dependencies {
    implementation(files("libs/artiusid-sdk-1.2.12.aar"))  // ← Update version
    // ... rest of dependencies
}
```

### **Step 3: Update Configuration**

In `TriNetApplication.kt`:

```kotlin
val config = SDKConfiguration(
    apiKey = "",
    environment = Environment.SANDBOX,  // ← Change to SANDBOX
    enableLogging = true,
    // ... rest of config
)

ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
```

**That's it!** URLs are configured automatically.

### **Step 4: Remove Old Workarounds (If Present)**

If you have this code, **delete it**:
```kotlin
// DELETE THIS - No longer needed!
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sdk.config.UrlConfiguration
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)
```

### **Step 5: Clean Build**

```bash
./gradlew clean
./gradlew assembleCustomerDistribution
```

### **Step 6: Install and Test**

```bash
adb uninstall com.trinet.app
adb install app/build/outputs/apk/customerDistribution/app-customerDistribution.apk
```

---

## ✅ **Verify Configuration**

### **Check Logs:**

After launching your app, look for these logs:

```
I/ArtiusIDSDK: 🌉 Initializing artius.iD SDK Bridge with Enhanced Theming...
I/ArtiusIDSDK: 🌐 Environment set to: Sandbox
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox.artiusid.dev
I/ArtiusIDSDK:    Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
I/ArtiusIDSDK:    Certificate: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

**If you see these URLs → Configuration is correct!** ✅

---

## 🎯 **What This Fixes**

### **Before v1.2.12:**
- ❌ Verification failed with 400/404 errors
- ❌ Wrong backend URLs (dev.mobile.artiusid.dev)
- ❌ Had to manually configure UrlBuilder
- ❌ Confusing two-step configuration

### **After v1.2.12:**
- ✅ Verification works correctly
- ✅ Correct Sandbox URLs (sandbox.mobile.artiusid.dev)
- ✅ Automatic URL configuration
- ✅ Simple one-line configuration

---

## 📊 **Supported Environments**

| Environment | When to Use | URLs |
|-------------|-------------|------|
| **SANDBOX** | **Your production app** | `sandbox.mobile.artiusid.dev` |
| DEVELOPMENT | Internal SDK development | `dev.mobile.artiusid.dev` |
| STAGING | Pre-production testing | `stage.mobile.artiusid.dev` |
| PRODUCTION | Future production (not yet) | `prod.mobile.artiusid.com` |

**For TriNet: Use `Environment.SANDBOX`** ✅

---

## 🔧 **Complete Configuration Example**

```kotlin
package com.trinet.app

import android.app.Application
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment  // ← Import this
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.models.SDKColorScheme
import com.artiusid.sdk.models.SDKIconTheme
import com.artiusid.sdk.models.SDKImageOverrides
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TriNetApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // ✅ Configure SDK with Sandbox environment
        val config = SDKConfiguration(
            apiKey = "",
            environment = Environment.SANDBOX,  // ← Sandbox URLs configured automatically!
            enableLogging = true,
            enableAnalytics = true,
            enableBiometrics = true,
            enableNFC = true,
            timeoutSeconds = 30,
            
            // Branding overrides
            localizationOverrides = mapOf(
                "app_name" to "TriNet",
                "welcome_title" to "Welcome to TriNet"
            ),
            
            // Image overrides
            imageOverrides = SDKImageOverrides(
                brandLogo = "android.resource://$packageName/drawable/trinet_logo"
            )
        )
        
        // ✅ Configure enhanced theme
        val enhancedTheme = EnhancedSDKThemeConfiguration(
            brandName = "TriNet",
            
            colorScheme = SDKColorScheme(
                primaryColorHex = "#0B0134",      // TriNet Blue
                secondaryColorHex = "#D64100",    // TriNet Orange
                backgroundColorHex = "#FFFFFF"
            ),
            
            iconTheme = SDKIconTheme(
                accentIconColorHex = "#D64100"    // Orange icons
            )
        )
        
        // ✅ Initialize SDK
        ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
    }
}
```

---

## 🐛 **Troubleshooting**

### **Problem: Compilation error on `Environment.SANDBOX`**

**Error:**
```
Unresolved reference: SANDBOX
```

**Cause:** Using old SDK version

**Solution:** Make sure you're using v1.2.12:
```gradle
implementation(files("libs/artiusid-sdk-1.2.12.aar"))
```

### **Problem: Verification still fails**

**Check logs for:**
```
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox.artiusid.dev
```

**If missing:**
1. Verify SDK version is 1.2.12
2. Clean and rebuild
3. Uninstall and reinstall app

**If present but still failing:**
- Contact backend team to verify Sandbox environment is running
- Check network connectivity
- Review verification logs for specific error

### **Problem: Still seeing dev.mobile.artiusid.dev URLs**

**Solution:**
1. Verify `environment = Environment.SANDBOX` (not DEVELOPMENT)
2. Clean build: `./gradlew clean`
3. Delete build folder: `rm -rf app/build`
4. Rebuild: `./gradlew assembleCustomerDistribution`
5. Uninstall old app completely
6. Install fresh APK

---

## 📊 **Testing Checklist**

Before deploying to users:

- [ ] SDK v1.2.12 installed
- [ ] `environment = Environment.SANDBOX` configured
- [ ] Logs show `Sandbox.artiusid.dev` URLs
- [ ] App launches successfully
- [ ] SDK screens show TriNet branding (orange icons)
- [ ] Verification flow completes without errors
- [ ] Network calls go to `sandbox.mobile.artiusid.dev`
- [ ] mTLS certificate loads correctly
- [ ] NFC passport scanning works
- [ ] Face detection works
- [ ] Document scanning works
- [ ] Verification succeeds and returns result

---

## 📚 **Additional Documentation**

**Detailed Setup Guide:**
- `/Users/toddbryant/Documents/mobile-sdk-android/TRINET_SANDBOX_CONFIGURATION.md`

**Technical Design:**
- `/Users/toddbryant/Documents/mobile-sdk-android/SDK_URL_CONFIGURATION_FIX.md`

**All Dependencies Guide:**
- `/Users/toddbryant/Documents/mobile-sdk-android/SDK_DEPENDENCY_REQUIREMENTS.md`

**Missing Dependencies Fix:**
- `/Users/toddbryant/Documents/mobile-sdk-android/TRINET_MISSING_DEPENDENCIES_FIX.md`

---

## 🚀 **What's Next**

### **For TriNet:**
1. ✅ Update to SDK v1.2.12
2. ✅ Change `environment = Environment.SANDBOX`
3. ✅ Test verification flow
4. ✅ Deploy to production

### **For SDK Team:**
- ✅ v1.2.12 deployed to GitHub
- ✅ Documentation updated
- ✅ Customer notification sent

---

## 📞 **Support**

**If you encounter issues:**

1. **Check logs** for URL configuration messages
2. **Verify environment** is set to `Environment.SANDBOX`
3. **Send logs** showing initialization and verification
4. **Contact support** with:
   - SDK version
   - Environment configuration
   - Error logs
   - Network traces (if available)

---

## ✅ **Summary**

**What Changed:**
- ✅ Added `Environment.SANDBOX` option
- ✅ Automatic URL configuration from `SDKConfiguration`
- ✅ Simplified integration (no UrlBuilder needed)
- ✅ Clear logging of configured URLs

**What You Need to Do:**
- ✅ Update to SDK v1.2.12
- ✅ Set `environment = Environment.SANDBOX`
- ✅ Test and deploy

**Result:**
- ✅ Verification URLs: `https://sandbox.mobile.artiusid.dev`
- ✅ Certificate URLs: `https://sandbox.registration.artiusid.dev`
- ✅ Verification works correctly with backend
- ✅ Production-ready SDK

---

**Release Status:** ✅ DEPLOYED  
**GitHub Release:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.12  
**Priority:** CRITICAL - Required for correct backend communication  
**Impact:** Fixes all verification URL issues

