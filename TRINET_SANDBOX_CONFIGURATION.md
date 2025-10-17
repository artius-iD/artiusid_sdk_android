# TriNet Sandbox Environment Configuration

**SDK Version:** v1.2.12  
**Date:** October 17, 2025  
**Priority:** CRITICAL - Required for production deployment

---

## ✅ **FIXED: Sandbox Environment Now Supported**

SDK v1.2.12 adds **automatic Sandbox environment configuration** through `SDKConfiguration`.

---

## 🎯 **What TriNet Needs to Do**

### **Simple 1-Line Change:**

```kotlin
val config = SDKConfiguration(
    apiKey = "",
    environment = Environment.SANDBOX,  // ← Just set this!
    enableLogging = true,
    // ... rest of your config
)

ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
```

**That's it!** URLs are now configured automatically.

---

## 🌐 **Backend URLs (Automatically Configured)**

When you set `environment = Environment.SANDBOX`, the SDK automatically uses:

### **Verification URL:**
```
https://sandbox.mobile.artiusid.dev/verifi/api/verification
```

### **mTLS Certificate URL:**
```
https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

### **Authentication URL:**
```
https://sandbox.mobile.artiusid.dev/auth/api/auth
```

---

## 📋 **Complete Configuration Example**

```kotlin
package com.trinet.app

import android.app.Application
import android.util.Log
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.models.SDKColorScheme
import com.artiusid.sdk.models.SDKIconTheme
import com.artiusid.sdk.models.SDKImageOverrides
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TriNetApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // ✅ Step 1: Configure SDK with Sandbox environment
            val config = SDKConfiguration(
                apiKey = "",
                environment = Environment.SANDBOX,  // ← Sandbox URLs configured automatically!
                enableLogging = true,
                enableAnalytics = true,
                enableBiometrics = true,
                enableNFC = true,
                timeoutSeconds = 30,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true,
                
                // Branding overrides
                localizationOverrides = mapOf(
                    "app_name" to "TriNet",
                    "app_name_artius" to "TriNet",
                    "app_name_id" to "",
                    "welcome_title" to "Welcome to TriNet",
                    "verification_title" to "TriNet Identity Verification",
                    "scan_document_title" to "Scan Your Document",
                    "scan_face_title" to "Scan Your Face",
                    "scan_passport_chip_title" to "Scan Passport Chip"
                ),
                
                // Image overrides
                imageOverrides = SDKImageOverrides(
                    brandLogo = "android.resource://$packageName/drawable/trinet_logo",
                    welcomeLogo = "android.resource://$packageName/drawable/trinet_logo",
                    verificationLogo = "android.resource://$packageName/drawable/trinet_logo"
                )
            )
            
            // ✅ Step 2: Configure enhanced theme
            val enhancedTheme = EnhancedSDKThemeConfiguration(
                brandName = "TriNet",
                brandLogoUrl = "android.resource://$packageName/drawable/trinet_logo",
                
                colorScheme = SDKColorScheme(
                    // TriNet Blue (Primary)
                    primaryColorHex = "#0B0134",
                    onPrimaryColorHex = "#FFFFFF",
                    primaryContainerColorHex = "#0B0134",
                    onPrimaryContainerColorHex = "#FFFFFF",
                    
                    // TriNet Orange (Secondary/Accent)
                    secondaryColorHex = "#D64100",
                    onSecondaryColorHex = "#FFFFFF",
                    secondaryContainerColorHex = "#FFE0B2",
                    onSecondaryContainerColorHex = "#0B0134",
                    
                    // Background & Surface
                    backgroundColorHex = "#FFFFFF",
                    onBackgroundColorHex = "#0B0134",
                    surfaceColorHex = "#FFFFFF",
                    onSurfaceColorHex = "#0B0134",
                    
                    // Status Colors
                    successColorHex = "#4CAF50",
                    errorColorHex = "#F44336",
                    warningColorHex = "#D64100",
                    
                    // Overlays (Orange)
                    faceDetectionOverlayColorHex = "#D64100",
                    documentScanOverlayColorHex = "#D64100",
                    
                    // Borders/Outlines (Orange)
                    outlineColorHex = "#D64100",
                    outlineVariantColorHex = "#FFB74D",
                    
                    // Step indicators
                    pendingStepColorHex = "#9E9E9E",
                    completedStepColorHex = "#D64100"
                ),
                
                iconTheme = SDKIconTheme(
                    // All icons in TriNet Orange
                    primaryIconColorHex = "#D64100",
                    accentIconColorHex = "#D64100",
                    actionIconColorHex = "#D64100",
                    navigationIconColorHex = "#D64100",
                    documentIconColorHex = "#D64100",
                    scanIconColorHex = "#D64100",
                    biometricIconColorHex = "#D64100",
                    verificationIconColorHex = "#D64100",
                    successIconColorHex = "#4CAF50",
                    errorIconColorHex = "#F44336",
                    warningIconColorHex = "#D64100"
                ),
                
                isDarkMode = false
            )
            
            // ✅ Step 3: Initialize SDK
            ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
            
            Log.i("TriNet", "✅ SDK initialized successfully with Sandbox environment")
            
        } catch (e: Exception) {
            Log.e("TriNet", "❌ Failed to initialize SDK", e)
        }
    }
}
```

---

## 📊 **SDK Logs to Verify Configuration**

When you run your app, you should see these logs:

```
I/ArtiusIDSDK: 🌉 Initializing artius.iD SDK Bridge with Enhanced Theming...
I/ArtiusIDSDK: 🌐 Environment set to: Sandbox
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox.artiusid.dev
I/ArtiusIDSDK:    Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
I/ArtiusIDSDK:    Certificate: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

**If you see these URLs, your configuration is correct!** ✅

---

## 🔄 **Available Environments**

SDK v1.2.12 supports 4 environments:

| Environment | Verification URL | Certificate URL |
|-------------|------------------|-----------------|
| `SANDBOX` | `https://sandbox.mobile.artiusid.dev` | `https://sandbox.registration.artiusid.dev` |
| `DEVELOPMENT` | `https://dev.mobile.artiusid.dev` | `https://dev.registration.artiusid.dev` |
| `STAGING` | `https://stage.mobile.artiusid.dev` | `https://stage.registration.artiusid.dev` |
| `PRODUCTION` | `https://prod.mobile.artiusid.com` | `https://prod.registration.artiusid.com` |

**For TriNet production app:** Use `Environment.SANDBOX` ✅

---

## ⚠️ **Before v1.2.12 (Old Method)**

If you're still on v1.2.11 or earlier, you need the workaround:

```kotlin
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sdk.config.UrlConfiguration

// BEFORE SDK initialization:
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)

// Then initialize SDK:
val config = SDKConfiguration(
    environment = Environment.DEVELOPMENT  // This is ignored for URLs
)
ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
```

**But with v1.2.12, you don't need this anymore!** Just set the environment.

---

## 🧪 **Testing Verification**

After updating to v1.2.12 and setting `Environment.SANDBOX`:

### **Test 1: Check Logs**
Look for:
```
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox.artiusid.dev
I/ArtiusIDSDK:    Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
```

### **Test 2: Run Verification Flow**
1. Launch app
2. Tap "Start Verification"
3. Complete verification steps
4. Check if verification succeeds with backend

### **Test 3: Check Network Calls**
Use logcat to verify network calls go to:
- `https://sandbox.mobile.artiusid.dev/verifi/api/verification`
- `https://sandbox.registration.artiusid.dev/LoadCertificateFunction`

---

## 🚀 **Migration Steps**

### **Step 1: Update SDK to v1.2.12**

Download the latest AAR:
```
https://github.com/artiusid1/mobile-sdk-android/releases/tag/v1.2.12
```

Replace in your project:
```
app/libs/artiusid-sdk-1.2.12.aar
```

Update `build.gradle`:
```gradle
dependencies {
    implementation(files("libs/artiusid-sdk-1.2.12.aar"))
    // ... rest of dependencies
}
```

### **Step 2: Update Configuration**

In `TriNetApplication.kt`, change:

```kotlin
// BEFORE:
environment = Environment.DEVELOPMENT

// AFTER:
environment = Environment.SANDBOX
```

### **Step 3: Remove Old Workaround (if present)**

If you have this code, **delete it**:
```kotlin
// DELETE THIS:
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sdk.config.UrlConfiguration
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)
```

### **Step 4: Clean Build**

```bash
./gradlew clean
./gradlew assembleCustomerDistribution
```

### **Step 5: Test**

Install and verify logs show correct Sandbox URLs.

---

## ✅ **Verification Checklist**

Before deploying to production, verify:

- [ ] SDK v1.2.12 installed
- [ ] `environment = Environment.SANDBOX` in SDKConfiguration
- [ ] Logs show `Sandbox.artiusid.dev` URLs
- [ ] Verification flow completes successfully
- [ ] Network calls go to `sandbox.mobile.artiusid.dev`
- [ ] mTLS certificate loads from `sandbox.registration.artiusid.dev`
- [ ] No 400/404 errors from wrong URLs

---

## 🐛 **Troubleshooting**

### **Problem: Still seeing wrong URLs**

**Check:**
1. Are you using SDK v1.2.12?
2. Did you set `environment = Environment.SANDBOX`?
3. Did you clean and rebuild?

**Solution:**
```bash
./gradlew clean
rm -rf app/build
./gradlew assembleCustomerDistribution
adb uninstall com.trinet.app
adb install app/build/outputs/apk/customerDistribution/app-customerDistribution.apk
```

### **Problem: Compilation error on Environment.SANDBOX**

**Cause:** Using old SDK version (v1.2.11 or earlier)

**Solution:** Update to SDK v1.2.12 which adds `Environment.SANDBOX`

### **Problem: Verification fails with 400/404**

**Check logs for:**
```
E/Retrofit: HTTP 404 Not Found
```

**Solution:** Verify logs show correct Sandbox URLs. If not, contact SDK support.

---

## 📞 **Support**

If you encounter issues:

1. **Check logs** for URL configuration messages
2. **Verify SDK version** is v1.2.12
3. **Send logs** showing the URL configuration output
4. **Contact support** with error details

---

## 📝 **Summary**

**What Changed in v1.2.12:**
- ✅ Added `Environment.SANDBOX` option
- ✅ Automatic URL configuration from `SDKConfiguration.environment`
- ✅ Clear logging showing configured URLs
- ✅ No need for `UrlBuilder.setConfiguration()` workaround

**What TriNet Needs to Do:**
- ✅ Update to SDK v1.2.12
- ✅ Set `environment = Environment.SANDBOX`
- ✅ Remove old `UrlBuilder` workaround (if present)
- ✅ Test and verify logs

**Result:**
- ✅ Sandbox URLs: `https://sandbox.mobile.artiusid.dev`
- ✅ Sandbox Certificate: `https://sandbox.registration.artiusid.dev`
- ✅ Verification works correctly with backend

---

**Status:** Ready for deployment  
**Impact:** Fixes verification failures caused by wrong backend URLs  
**Priority:** CRITICAL - Required for production  
**ETA:** Available immediately in v1.2.12

