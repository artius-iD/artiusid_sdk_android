# SDK URL Configuration Fix - Make SDKConfiguration Control URLs

**Issue:** SDKConfiguration.environment and baseUrl are ignored  
**Current:** URLs must be set separately via UrlBuilder.setConfiguration()  
**Needed:** SDKConfiguration should automatically configure backend URLs  
**Priority:** HIGH - Customer needs Sandbox environment

---

## 🚨 **Current Problem**

### **What TriNet Does:**
```kotlin
val config = SDKConfiguration(
    environment = Environment.DEVELOPMENT,  // ← Ignored for URLs!
    baseUrl = "",  // ← Completely ignored!
    // ...
)
ArtiusIDSDK.initialize(context, config)
```

### **What Actually Happens:**
```kotlin
// In ArtiusIDSDK.kt lines 89-96:
val environmentName = when (configuration.environment) {
    Environment.DEVELOPMENT -> "Development"  // Saved to SharedPreferences
    Environment.STAGING -> "Staging"
    Environment.PRODUCTION -> "Production"
}
prefs.edit().putString("environment", environmentName).apply()

// But in UrlBuilder.kt line 50:
fun getCurrentConfiguration(): UrlConfiguration {
    return currentConfiguration ?: UrlConfiguration.SANDBOX_DEV  // ← Ignores SharedPreferences!
}
```

**Result:** SharedPreferences is written but never read! SDK always uses SANDBOX_DEV default!

---

## ✅ **The Fix**

### **Option 1: Modify UrlBuilder.getCurrentConfiguration() (RECOMMENDED)**

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/utils/UrlBuilder.kt`

**Change lines 49-51:**

```kotlin
// BEFORE:
fun getCurrentConfiguration(): UrlConfiguration {
    return currentConfiguration ?: UrlConfiguration.SANDBOX_DEV
}

// AFTER:
fun getCurrentConfiguration(): UrlConfiguration {
    // Return explicitly set configuration if available
    if (currentConfiguration != null) {
        return currentConfiguration!!
    }
    
    // Otherwise, create from SharedPreferences (set by SDKConfiguration)
    return try {
        val context = getApplicationContext()
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val environment = prefs.getString("environment", "Sandbox") ?: "Sandbox"
        val domain = prefs.getString("domain", "artiusid.dev") ?: "artiusid.dev"
        
        UrlConfiguration(environment, domain).also {
            android.util.Log.d("UrlBuilder", "📋 Using configuration from SDKConfiguration: ${it.getDescription()}")
        }
    } catch (e: Exception) {
        android.util.Log.w("UrlBuilder", "⚠️  Could not read configuration from settings, using default")
        UrlConfiguration.SANDBOX_DEV
    }
}

// Add helper to get application context
private fun getApplicationContext(): Context {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
        currentApplicationMethod.invoke(null) as Context
    } catch (e: Exception) {
        throw IllegalStateException("Could not get application context", e)
    }
}
```

---

### **Option 2: Set UrlConfiguration in SDK Initialization (ALTERNATIVE)**

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/ArtiusIDSDK.kt`

**Add after line 96 (after saving to SharedPreferences):**

```kotlin
// Line 96 currently:
prefs.edit().putString("environment", environmentName).apply()
android.util.Log.i(TAG, "🌐 Environment set to: $environmentName")

// ADD THESE LINES:
// Automatically configure UrlBuilder based on SDKConfiguration
val urlConfig = when (configuration.environment) {
    com.artiusid.sdk.config.Environment.DEVELOPMENT -> UrlConfiguration.DEVELOPMENT_DEV
    com.artiusid.sdk.config.Environment.STAGING -> UrlConfiguration.STAGING_DEV
    com.artiusid.sdk.config.Environment.PRODUCTION -> UrlConfiguration.PRODUCTION_COM
}
UrlBuilder.setConfiguration(urlConfig)
android.util.Log.i(TAG, "🌐 URL Configuration set to: ${urlConfig.getDescription()}")
```

**Also add after line 180 in `initializeWithEnhancedTheme()`:**

```kotlin
// Line 180 currently:
android.util.Log.i(TAG, "🌐 Environment set to: $environmentName")

// ADD THESE LINES:
// Automatically configure UrlBuilder based on SDKConfiguration
val urlConfig = when (configuration.environment) {
    com.artiusid.sdk.config.Environment.DEVELOPMENT -> UrlConfiguration.DEVELOPMENT_DEV
    com.artiusid.sdk.config.Environment.STAGING -> UrlConfiguration.STAGING_DEV
    com.artiusid.sdk.config.Environment.PRODUCTION -> UrlConfiguration.PRODUCTION_COM
}
UrlBuilder.setConfiguration(urlConfig)
android.util.Log.i(TAG, "🌐 URL Configuration set to: ${urlConfig.getDescription()}")
```

---

## 🎯 **Add Sandbox to SDKConfiguration.Environment**

### **Problem:**
TriNet needs **Sandbox** but SDKConfiguration.Environment only has:
```kotlin
enum class Environment {
    DEVELOPMENT,
    STAGING, 
    PRODUCTION
}
```

### **Fix:**

**File:** `artiusid-sdk/src/main/java/com/artiusid/sdk/config/SDKConfiguration.kt`

**Change lines 36-40:**

```kotlin
// BEFORE:
enum class Environment {
    DEVELOPMENT,
    STAGING, 
    PRODUCTION
}

// AFTER:
enum class Environment {
    SANDBOX,      // ← ADD THIS
    DEVELOPMENT,
    STAGING, 
    PRODUCTION
}
```

**Then update ArtiusIDSDK.kt line 91:**

```kotlin
// BEFORE:
val environmentName = when (configuration.environment) {
    com.artiusid.sdk.config.Environment.DEVELOPMENT -> "Development"
    com.artiusid.sdk.config.Environment.STAGING -> "Staging"
    com.artiusid.sdk.config.Environment.PRODUCTION -> "Production"
}

// AFTER:
val environmentName = when (configuration.environment) {
    com.artiusid.sdk.config.Environment.SANDBOX -> "Sandbox"       // ← ADD THIS
    com.artiusid.sdk.config.Environment.DEVELOPMENT -> "Development"
    com.artiusid.sdk.config.Environment.STAGING -> "Staging"
    com.artiusid.sdk.config.Environment.PRODUCTION -> "Production"
}
```

**And update the URL configuration mapping:**

```kotlin
// In the new code we're adding:
val urlConfig = when (configuration.environment) {
    com.artiusid.sdk.config.Environment.SANDBOX -> UrlConfiguration.SANDBOX_DEV      // ← ADD THIS
    com.artiusid.sdk.config.Environment.DEVELOPMENT -> UrlConfiguration.DEVELOPMENT_DEV
    com.artiusid.sdk.config.Environment.STAGING -> UrlConfiguration.STAGING_DEV
    com.artiusid.sdk.config.Environment.PRODUCTION -> UrlConfiguration.PRODUCTION_COM
}
```

---

## 📊 **After Fix - TriNet Usage**

### **What TriNet Will Do:**

```kotlin
val config = SDKConfiguration(
    apiKey = "",
    baseUrl = "",
    environment = Environment.SANDBOX,  // ← Now supported!
    enableLogging = true,
    // ...
)

ArtiusIDSDK.initializeWithEnhancedTheme(this, config, enhancedTheme)
```

### **What SDK Will Do:**

```kotlin
// 1. Detect Environment.SANDBOX
// 2. Map to UrlConfiguration.SANDBOX_DEV
// 3. Configure UrlBuilder automatically
// 4. Log: "🌐 URL Configuration set to: Sandbox.artiusid.dev"
// 5. Backend URL: https://sandbox.mobile.artiusid.dev
```

**Result:** URLs configured automatically from SDKConfiguration! ✅

---

## 🔄 **Migration Path**

### **For Existing Apps (Backward Compatible):**

**Option A: Keep using UrlBuilder.setConfiguration() (works as before)**
```kotlin
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)
ArtiusIDSDK.initialize(context, config)
```

**Option B: Use new automatic configuration (simpler)**
```kotlin
val config = SDKConfiguration(
    environment = Environment.SANDBOX  // Automatically configures URLs
)
ArtiusIDSDK.initialize(context, config)
```

**Both options work! No breaking changes!**

---

## 📋 **Complete Implementation**

### **Files to Modify:**

**1. SDKConfiguration.kt**
- Add `SANDBOX` to Environment enum
- No other changes needed

**2. ArtiusIDSDK.kt** (two places)
- In `initialize()` method (after line 96)
- In `initializeWithEnhancedTheme()` method (after line 180)
- Add automatic UrlBuilder configuration based on Environment

**3. UrlBuilder.kt** (Optional but recommended)
- Modify `getCurrentConfiguration()` to read from SharedPreferences if no explicit config set
- Makes the system more robust

---

## ✅ **Testing Checklist**

### **Test 1: Sandbox Environment**
```kotlin
SDKConfiguration(environment = Environment.SANDBOX)
```
**Expected URL:** `https://sandbox.mobile.artiusid.dev/verifi/api/verification`

### **Test 2: Development Environment**
```kotlin
SDKConfiguration(environment = Environment.DEVELOPMENT)
```
**Expected URL:** `https://dev.mobile.artiusid.dev/verifi/api/verification`

### **Test 3: Production Environment**
```kotlin
SDKConfiguration(environment = Environment.PRODUCTION)
```
**Expected URL:** `https://prod.mobile.artiusid.com/verifi/api/verification`

### **Test 4: Explicit UrlConfiguration (backward compatibility)**
```kotlin
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)
SDKConfiguration(environment = Environment.PRODUCTION)  // Should be ignored
```
**Expected URL:** `https://sandbox.mobile.artiusid.dev/verifi/api/verification`  
**Reason:** Explicit UrlBuilder.setConfiguration() takes precedence

### **Test 5: No Configuration (fallback)**
```kotlin
// Don't call UrlBuilder.setConfiguration()
SDKConfiguration(environment = null or missing)
```
**Expected URL:** `https://sandbox.mobile.artiusid.dev/verifi/api/verification`  
**Reason:** Falls back to SANDBOX_DEV default

---

## 🎯 **Benefits**

**For Customers:**
- ✅ Simpler integration (one configuration object)
- ✅ Environment automatically sets correct URLs
- ✅ No need to understand UrlBuilder internals
- ✅ Less error-prone

**For SDK:**
- ✅ Consistent configuration approach
- ✅ SDKConfiguration becomes "source of truth"
- ✅ Backward compatible (no breaking changes)
- ✅ Better logging and debugging

**For Support:**
- ✅ Fewer integration issues
- ✅ Easier to diagnose problems
- ✅ Clear mapping: Environment → URL
- ✅ One less thing for customers to configure

---

## 📝 **Documentation Update**

### **Before (Confusing):**
```kotlin
// Step 1: Configure URLs
UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)

// Step 2: Initialize SDK
val config = SDKConfiguration(
    environment = Environment.DEVELOPMENT  // ← Doesn't affect URLs!
)
ArtiusIDSDK.initialize(context, config)
```

### **After (Clear):**
```kotlin
// Just initialize SDK with desired environment
val config = SDKConfiguration(
    environment = Environment.SANDBOX  // ← Automatically configures URLs!
)
ArtiusIDSDK.initialize(context, config)
```

---

## 🚀 **Rollout Plan**

### **v1.2.12 - SDK Update:**

**Changes:**
1. Add `SANDBOX` to `Environment` enum
2. Add automatic URL configuration in `initialize()` methods
3. Update `getCurrentConfiguration()` to read from SharedPreferences
4. Add logging for URL configuration

**Testing:**
1. Test all 4 environments (Sandbox, Development, Staging, Production)
2. Test backward compatibility (explicit UrlBuilder.setConfiguration())
3. Test fallback behavior (no configuration)
4. Verify logging shows correct URLs

**Documentation:**
1. Update integration guide with new simplified approach
2. Mark UrlBuilder.setConfiguration() as "advanced/optional"
3. Add environment-to-URL mapping table
4. Add troubleshooting section for URL issues

### **Customer Communication:**

**Email Subject:** SDK v1.2.12 - Simplified Environment Configuration

**Body:**
> Hi all,
> 
> We're releasing SDK v1.2.12 with a **much simpler** environment configuration!
> 
> **What's New:**
> - SDKConfiguration.environment now **automatically** configures backend URLs
> - Added `Environment.SANDBOX` option (requested by multiple customers)
> - No need to call `UrlBuilder.setConfiguration()` separately anymore!
> 
> **Before (v1.2.11 and earlier):**
> ```kotlin
> UrlBuilder.setConfiguration(UrlConfiguration.SANDBOX_DEV)
> val config = SDKConfiguration(environment = Environment.DEVELOPMENT)
> ArtiusIDSDK.initialize(context, config)
> ```
> 
> **After (v1.2.12):**
> ```kotlin
> val config = SDKConfiguration(environment = Environment.SANDBOX)
> ArtiusIDSDK.initialize(context, config)
> ```
> 
> **That's it!** URLs are configured automatically.
> 
> **Backward Compatible:** If you're using `UrlBuilder.setConfiguration()`, it still works and takes precedence.
> 
> **Upgrade:** Just replace your AAR with v1.2.12 and optionally simplify your initialization code.

---

## ✅ **Summary**

| Item | Status |
|------|--------|
| **Issue** | SDKConfiguration.environment doesn't control URLs |
| **Root Cause** | UrlBuilder ignores SDKConfiguration |
| **Fix** | Auto-configure UrlBuilder from SDKConfiguration |
| **SANDBOX Support** | Add to Environment enum |
| **Breaking Changes** | None (backward compatible) |
| **Implementation Time** | 2 hours |
| **Testing Time** | 1 hour |
| **Release** | v1.2.12 |

---

**Status:** Ready to implement  
**Priority:** HIGH - TriNet needs Sandbox  
**ETA:** Can be done immediately  
**Impact:** Significantly simplifies SDK integration

