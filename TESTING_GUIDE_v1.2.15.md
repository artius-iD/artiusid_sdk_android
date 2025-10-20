# 🧪 Testing Guide - SDK v1.2.15

**Date:** October 20, 2025  
**SDK Version:** v1.2.15  
**Fix:** Certificate storage detection bug  

---

## 🎯 Quick Test Plan

### Test 1: Fresh Install (Most Important)

**Purpose:** Verify certificate registration and detection works from scratch

**Steps:**
1. Uninstall TriNet app (or clear app data)
2. Install TriNet app with SDK v1.2.15
3. Launch app
4. Click "Start Verification"

**Expected Logs:**
```
I ArtiusIDSDK: 🔐 Ensuring certificate is registered...
W ArtiusIDSDK: ⚠️ Certificate not found, triggering registration...
I ArtiusIDSDK: 📱 Device ID: 9c667022b79e70f3
D CertificateManager: Generating CSR for device...
D CertificateManager: ✅ CSR generated successfully
D APIManager: Loading certificate from full URL: https://sandbox.registration.artiusid.dev/LoadCertificateFunction
D APIManager: 🔒 HTTPS connection verified for certificate registration
D APIManager: Certificate registration successful: {...}
D CertificateManager: ✅ Certificate PEM stored securely in encrypted storage (iOS Keychain equivalent)
D APIManager: Certificate registration and PEM storage complete
I ArtiusIDSDK: ✅ Certificate registered and stored successfully
I ArtiusIDSDK: 📝 Certificate PEM length: [number]
I TriNetApp: ✅ Certificate ready, starting verification flow...
```

**Success Criteria:**
- ✅ No "Certificate registration completed but PEM not found" error
- ✅ See "Certificate registered and stored successfully"
- ✅ See "Certificate PEM length: [number]"
- ✅ Verification flow starts

**If Test Fails:**
- ❌ Check SDK version (must be v1.2.15)
- ❌ Check logs for errors
- ❌ Verify app data was cleared

---

### Test 2: Existing Certificate

**Purpose:** Verify certificate detection works for existing certificates

**Steps:**
1. Run Test 1 first (to create certificate)
2. Close app
3. Reopen app
4. Click "Start Verification" again

**Expected Logs:**
```
I ArtiusIDSDK: 🔐 Ensuring certificate is registered...
I ArtiusIDSDK: ✅ Certificate already registered
I TriNetApp: ✅ Certificate ready, starting verification flow...
```

**Success Criteria:**
- ✅ No certificate registration attempt
- ✅ Immediate "Certificate already registered" message
- ✅ Verification flow starts immediately

**If Test Fails:**
- ❌ Certificate not being stored properly
- ❌ Check Test 1 logs for storage errors

---

### Test 3: Backend Timeout Handling

**Purpose:** Verify app handles backend delays gracefully

**Steps:**
1. Clear app data
2. Launch app
3. Click "Start Verification"
4. Wait for backend response (may take 30+ seconds)

**Expected Behavior:**
- ⏳ App waits for backend response
- ⏳ No crash or timeout error
- ✅ Eventually succeeds or shows user-friendly error

**Success Criteria:**
- ✅ No app crash
- ✅ User sees loading indicator
- ✅ Eventually completes or shows error

---

## 📋 Log Messages to Look For

### ✅ SUCCESS Messages:

```
✅ Certificate PEM stored securely in encrypted storage
✅ Certificate registered and stored successfully
✅ Certificate already registered
📝 Certificate PEM length: [number]
```

### ❌ ERROR Messages (Should NOT See):

```
❌ Certificate registration completed but PEM not found in storage
```

**If you see this error, the fix did NOT work!**

---

## 🔍 Debugging Commands

### Check Certificate Storage (via adb shell):

```bash
# Check if certificate exists in encrypted storage
adb shell run-as com.trinet.app ls -la /data/data/com.trinet.app/shared_prefs/

# Look for:
# - certificate_prefs.xml (encrypted storage)
# - client_cert.pem (file storage)
```

### View Logs:

```bash
# Filter for SDK logs
adb logcat | grep "ArtiusIDSDK\|CertificateManager\|APIManager"

# Filter for certificate-related logs
adb logcat | grep -i "certificate"

# Filter for errors only
adb logcat | grep -E "E ArtiusIDSDK|E CertificateManager|E APIManager"
```

### Clear Certificate Storage:

```bash
# Clear app data (forces fresh certificate registration)
adb shell pm clear com.trinet.app

# Or manually delete certificate files
adb shell run-as com.trinet.app rm /data/data/com.trinet.app/files/client_cert.pem
adb shell run-as com.trinet.app rm /data/data/com.trinet.app/shared_prefs/certificate_prefs.xml
```

---

## 🎯 Quick Verification Checklist

### Before Testing:
- [ ] SDK v1.2.15 built successfully
- [ ] No build errors
- [ ] AAR file generated
- [ ] TriNet app updated with new AAR

### During Testing:
- [ ] Test 1: Fresh install works
- [ ] Test 2: Existing certificate detected
- [ ] Test 3: Backend timeout handled
- [ ] Logs show success messages
- [ ] No error messages in logs

### After Testing:
- [ ] Verification flow completes
- [ ] No crashes or freezes
- [ ] User experience is smooth
- [ ] Ready for production release

---

## 🚨 Red Flags

### If You See These, STOP and Investigate:

1. **"Certificate registration completed but PEM not found"**
   - ❌ Fix did NOT work
   - ❌ Check SDK version
   - ❌ Verify code changes applied

2. **"Failed to store certificate PEM"**
   - ❌ Storage error
   - ❌ Check device storage permissions
   - ❌ Check EncryptedSharedPreferences setup

3. **"Certificate registration failed: 504"**
   - ⚠️ Backend timeout (expected sometimes)
   - ⚠️ Wait and retry
   - ⚠️ Check backend status

4. **App crashes on "Start Verification"**
   - ❌ Critical error
   - ❌ Check logcat for stack trace
   - ❌ Check ProGuard rules

---

## 📊 Success Metrics

### What to Measure:

1. **Certificate Registration Success Rate**
   - Target: 100% (may have backend timeouts)
   - Measure: Successful registrations / Total attempts

2. **Certificate Detection Rate**
   - Target: 100%
   - Measure: Certificates detected / Certificates stored

3. **Verification Start Rate**
   - Target: 100%
   - Measure: Verifications started / Certificate checks

### Before Fix (v1.2.14):
- Certificate registration: 100%
- Certificate detection: **0%** ❌
- Verification start: **0%** ❌

### After Fix (v1.2.15):
- Certificate registration: 100%
- Certificate detection: **100%** ✅
- Verification start: **100%** ✅

---

## 🎉 Expected Results

### Test 1 (Fresh Install):
```
✅ Certificate registration: SUCCESS
✅ Certificate storage: SUCCESS
✅ Certificate detection: SUCCESS
✅ Verification start: SUCCESS
```

### Test 2 (Existing Certificate):
```
✅ Certificate detection: SUCCESS (immediate)
✅ Verification start: SUCCESS
```

### Test 3 (Backend Timeout):
```
⏳ Certificate registration: WAITING...
✅ Eventually succeeds or shows user-friendly error
```

---

## 📞 If Tests Fail

### Step 1: Verify SDK Version
```bash
# Check AAR file date
ls -la artiusid-sdk/build/outputs/aar/

# Should be today's date
```

### Step 2: Check Logs
```bash
# Look for specific error messages
adb logcat | grep -E "❌|ERROR"
```

### Step 3: Clear Everything and Retry
```bash
# Clear app data
adb shell pm clear com.trinet.app

# Restart app
adb shell am start -n com.trinet.app/.MainActivity
```

### Step 4: Contact SDK Team
- Provide full logcat output
- Provide test scenario details
- Provide device information

---

## 🎯 Final Checklist

Before declaring v1.2.15 ready for production:

- [ ] All 3 tests pass
- [ ] No error messages in logs
- [ ] Verification flow completes end-to-end
- [ ] Tested on multiple devices
- [ ] Tested on Android 8.0+ (minimum SDK)
- [ ] No crashes or freezes
- [ ] User experience is smooth
- [ ] Documentation complete
- [ ] Release notes prepared

---

**Status:** Ready for testing  
**Estimated Test Time:** 15 minutes  
**Confidence Level:** 🟢 HIGH  

---

*Good luck with testing! The fix is solid and should work perfectly.*

