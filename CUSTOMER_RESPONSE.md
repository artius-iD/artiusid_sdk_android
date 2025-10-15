# Response to Customer: Hilt Build Error

## TL;DR
**Your Hilt compilation error is FIXED in SDK v1.2.3 (released today, Oct 15, 2025).**

Please upgrade to: https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3

---

## The Issue You're Experiencing

```
java.lang.IllegalArgumentException: component method cannot be void: a()
```

This error was caused by a duplicate `StandaloneAppActivity` class in the wrong package within the SDK, creating conflicting Hilt components.

## The Solution

**✅ FIXED IN SDK v1.2.3**

We identified and removed the duplicate class that was causing the Hilt component conflict. The SDK now builds cleanly with Hilt 2.48.

### How to Resolve

1. **Download SDK v1.2.3:**
   - Go to: https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3
   - Download: `artiusid-sdk-1.2.3.aar`

2. **Replace your current AAR:**
   - Replace the old SDK AAR in your project with v1.2.3
   - Update your `build.gradle` reference to point to the new AAR

3. **Clean and rebuild:**
   ```bash
   ./gradlew clean build
   ```

4. **Verify:**
   - The Hilt compilation error should be completely resolved
   - Your app should build successfully

## What Else Was Fixed in v1.2.3

In addition to the Hilt error, we also fixed:

1. **NFC Crash** - App no longer crashes when scanning passport chips
2. **NFC Retry Loop** - Properly stops after 3 failed attempts
3. **Verification Parameters** - Sends all required parameters correctly

## Your Configuration is Correct

Based on your notes, your setup is already configured correctly:

✅ Hilt 2.48  
✅ Kotlin 1.9.10  
✅ KSP 1.9.10-1.0.13  
✅ Compose Compiler 1.5.3  
✅ @HiltAndroidApp on Application class  
✅ @AndroidEntryPoint on Activities  
✅ All required dependencies (Coil, Firebase, etc.)  

**You don't need to change anything in your configuration - just upgrade the SDK AAR to v1.2.3.**

## Sample App Available

A fully functional sample app is included in the release to demonstrate proper integration:
- **Download:** https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.2.3
- **File:** `sample-app-customerDistribution-1.2.3.apk` (173MB)

## Verification

We've already tested v1.2.3:
- ✅ Compiles without Hilt errors
- ✅ Sample app builds successfully  
- ✅ Installs and runs on Android devices
- ✅ All SDK features functional

## Support Resources

All documentation is included in the release:
- `INTEGRATION_GUIDE.md` - Complete integration instructions
- `HILT_INTEGRATION_GUIDE.md` - Detailed Hilt setup guide
- `README_HILT_SETUP.md` - Quick reference
- `hilt_diagnostic_script.gradle` - Troubleshooting tool
- `setup_hilt.sh` - Automated setup script

## Timeline

- **Issue Reported:** Customer experiencing Hilt error with v1.2.1/v1.2.2
- **Issue Identified:** Duplicate StandaloneAppActivity causing component conflict
- **Fix Developed:** Removed duplicate class, updated references
- **Testing Complete:** Full validation on sample app
- **Release Published:** v1.2.3 - October 15, 2025

## Next Steps

1. **Immediate:** Upgrade to SDK v1.2.3 (resolves your Hilt error)
2. **Test:** Verify your app builds and runs correctly
3. **Report:** Let us know if you encounter any other issues

## Contact

If you continue to experience issues after upgrading to v1.2.3, please provide:
1. Confirmation you're using SDK v1.2.3
2. Your complete `build.gradle` file
3. Full error stacktrace
4. Gradle version and Android Studio version

---

**Bottom Line:** The issue you're experiencing was a known bug in previous SDK versions and is **completely fixed in v1.2.3**. Simply upgrade your SDK AAR and rebuild.


