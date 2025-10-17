# SDK v1.2.12 - REBUILT with All Fixes

**Date:** October 17, 2025, 3:00 PM  
**Version:** v1.2.12 (REBUILT)  
**Status:** ✅ READY FOR TRINET

---

## 🎯 **What's Included in This Build**

### **1. Environment.SANDBOX Support** ✅
- Added `Environment.SANDBOX` option
- Automatic URL configuration from `SDKConfiguration`
- **Verification URL:** `https://sandbox.mobile.artiusid.dev/verifi/api/verification`
- **Certificate URL:** `https://sandbox.registration.artiusid.dev/LoadCertificateFunction`

### **2. Updated Success Sound** ✅
- Custom success sound file included
- Plays on successful document/passport capture
- File: `res/raw/clear_combo_5_394488.mp3`

### **3. Face Scan Overlay Transparency Fix** ✅
- Removed opaque square background behind face outline
- Face overlay now shows with fully transparent background
- Only the face outline is visible

---

## 📦 **SDK Files**

### **AAR File:**
```
Location: artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
Size: 25 MB
Version: 1.2.12
SHA-256: ffc0a1399428bc5014a32f640d7f1d2d89758bd638a9c7528c5b6b72fff91e14
```

### **Built:** October 17, 2025, 2:55 PM

---

## 🚀 **How TriNet Should Use This**

### **Step 1: Download AAR**
The AAR file is located at:
```
/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
```

**Copy it to your libs folder:**
```bash
cp artiusid-sdk-release.aar ~/your-project/app/libs/artiusid-sdk-1.2.12.aar
```

### **Step 2: Update Configuration**
```kotlin
val config = SDKConfiguration(
    apiKey = "",
    environment = Environment.SANDBOX,  // ← Use Sandbox URLs automatically!
    enableLogging = true,
    // ... rest of config
)

ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
```

### **Step 3: Verify Configuration**
Check logs for:
```
I/ArtiusIDSDK: 🌐 Environment set to: Sandbox
I/ArtiusIDSDK: 🌐 Backend URLs configured: Sandbox.artiusid.dev
I/ArtiusIDSDK:    Verification: https://sandbox.mobile.artiusid.dev/verifi/api/verification
I/ArtiusIDSDK:    Certificate: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
```

---

## ✅ **What's Fixed**

### **Issue 1: Wrong Backend URLs**
**Before:** SDK was using development URLs or no URL configuration  
**After:** SDK automatically uses Sandbox URLs when `environment = Environment.SANDBOX`  
**Result:** Verification requests go to correct backend

### **Issue 2: Custom Success Sound**
**Before:** Using old default sound  
**After:** Your custom success sound plays on capture  
**Result:** Better user experience with custom audio

### **Issue 3: Face Overlay Background**
**Before:** Opaque square background behind face outline  
**After:** Fully transparent background  
**Result:** Clean UI with only face outline visible

---

## 🔍 **Verification Steps**

### **Test 1: Check Sandbox URLs**
1. Launch app
2. Look for initialization logs
3. Confirm URLs show `sandbox.mobile.artiusid.dev`

### **Test 2: Test Verification Flow**
1. Complete document capture
2. Complete face scan  
3. Verify submission goes to Sandbox backend
4. Check for successful response

### **Test 3: Check Success Sound**
1. Capture document
2. Listen for custom success sound
3. Verify it's your new audio file

### **Test 4: Check Face Overlay**
1. Start face scan
2. Verify face outline shows with transparent background
3. No opaque square should be visible

---

## 📊 **SDK Configuration Example**

```kotlin
package com.trinet.app

import android.app.Application
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
        
        val config = SDKConfiguration(
            apiKey = "",
            environment = Environment.SANDBOX,  // ← Sandbox URLs configured automatically
            enableLogging = true,
            enableAnalytics = true,
            enableBiometrics = true,
            enableNFC = true,
            timeoutSeconds = 30,
            
            localizationOverrides = mapOf(
                "app_name" to "TriNet",
                "welcome_title" to "Welcome to TriNet"
            ),
            
            imageOverrides = SDKImageOverrides(
                brandLogo = "android.resource://$packageName/drawable/trinet_logo"
            )
        )
        
        val enhancedTheme = EnhancedSDKThemeConfiguration(
            brandName = "TriNet",
            
            colorScheme = SDKColorScheme(
                primaryColorHex = "#0B0134",      // TriNet Blue
                secondaryColorHex = "#D64100",    // TriNet Orange
                backgroundColorHex = "#FFFFFF",
                outlineColorHex = "#D64100"       // Orange outlines
            ),
            
            iconTheme = SDKIconTheme(
                accentIconColorHex = "#D64100",
                actionIconColorHex = "#D64100",
                documentIconColorHex = "#D64100",
                scanIconColorHex = "#D64100",
                biometricIconColorHex = "#D64100"
            )
        )
        
        ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
    }
}
```

---

## 🔧 **Build Information**

### **SDK Changes:**
```
Commit 1: 8976c29 - Bump version to 1.2.12
Commit 2: dae9f13 - Add Environment.SANDBOX and automatic URL configuration
Commit 3: 2c2c812 - Fix face scan overlay - set background to transparent
Commit 4: 814319e - Update success sound file
Commit 5: 45d2851 - Add gradle.properties backup file
```

### **Files Modified:**
- `artiusid-sdk/src/main/java/com/artiusid/sdk/config/SDKConfiguration.kt`
  * Added `SANDBOX` to Environment enum
  
- `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt`
  * Added automatic UrlBuilder configuration based on Environment
  * Added detailed URL logging
  
- `artiusid-sdk/src/main/java/com/artiusid/sdk/presentation/screens/face/FaceScanScreen.kt`
  * Added `.background(Color.Transparent)` to face overlay Box
  
- `artiusid-sdk/src/main/res/raw/clear_combo_5_394488.mp3`
  * Updated with custom success sound

### **Build Details:**
- **Gradle:** 8.11.1
- **Kotlin:** 1.9.10
- **Build Time:** 32 seconds (SDK)
- **Obfuscation:** ✅ Enabled (ProGuard/R8)
- **Size:** 25 MB

---

## 📝 **Changelog**

### **v1.2.12 (Rebuilt - October 17, 2025)**

**🌐 Environment Configuration:**
- ✅ Added `Environment.SANDBOX` option
- ✅ Automatic URL configuration from `SDKConfiguration.environment`
- ✅ Sandbox URLs: `sandbox.mobile.artiusid.dev`
- ✅ Clear logging showing configured URLs

**🔊 Audio:**
- ✅ Updated success sound file
- ✅ Custom audio plays on successful capture

**🎨 UI:**
- ✅ Fixed face scan overlay transparency
- ✅ Removed opaque background behind face outline

**🔧 Technical:**
- ✅ Backward compatible (no breaking changes)
- ✅ All ProGuard rules preserved
- ✅ Hilt support maintained

---

## 🐛 **Known Issues**

None - All reported issues have been fixed in this build.

---

## 📞 **Support**

If you encounter any issues:

1. **Check logs** for URL configuration messages
2. **Verify environment** is set to `Environment.SANDBOX`
3. **Test network calls** go to `sandbox.mobile.artiusid.dev`
4. **Send logs** if verification still fails

---

## ✅ **Deployment Checklist**

Before deploying to production:

- [ ] AAR copied to `app/libs/artiusid-sdk-1.2.12.aar`
- [ ] `build.gradle` updated to reference v1.2.12
- [ ] `environment = Environment.SANDBOX` in SDKConfiguration
- [ ] Clean build performed (`./gradlew clean`)
- [ ] Old app uninstalled from test device
- [ ] Fresh APK installed
- [ ] Logs verified showing Sandbox URLs
- [ ] Verification flow tested end-to-end
- [ ] Success sound tested
- [ ] Face scan overlay verified (transparent background)

---

## 🎯 **Summary**

**What TriNet Gets:**
- ✅ Working Sandbox environment URLs
- ✅ Automatic URL configuration (no manual UrlBuilder setup)
- ✅ Custom success sound
- ✅ Clean face scan UI (transparent overlay)
- ✅ Production-ready SDK

**What TriNet Needs to Do:**
1. Copy AAR to project
2. Update `environment = Environment.SANDBOX`
3. Test and verify
4. Deploy

**Result:**
- ✅ Verification works with Sandbox backend
- ✅ All UI/UX issues resolved
- ✅ Ready for production deployment

---

**Status:** ✅ READY FOR TRINET  
**Build Date:** October 17, 2025, 2:55 PM  
**Version:** v1.2.12 (Rebuilt with all fixes)  
**Priority:** HIGH - Deploy immediately

