# ArtiusID Android SDK v1.2.53 - Release Notes

**Release Date:** March 2026  
**Author:** artius.iD, Inc.

---

## 🎨 ThemeManager, LocalizationManager, SDKResourceBundle (iOS parity)

This release adds unified theme/locale APIs and resource helpers for host app integration.

### **1. ThemeManager (unified facade)**

- **`ThemeManager.setTheme(config)`** – Set current SDK theme (delegates to EnhancedThemeManager + ColorManager).
- **`ThemeManager.setLocale(languageCode)`** – Set SDK display language (e.g. `"en"`, `"es"`).
- **`ThemeManager.getDebugInfo()`** – Combined debug string for localization, theme, and image overrides.

### **2. LocalizationManager (utils) – runtime overrides**

- **`addOverride(key, value)`** / **`removeOverride(key)`** / **`clearOverrides()`**
- **`setOverrides(map)`** – Replace all overrides at once.
- **`getOverrideCount()`** / **`getOverrideKeys()`** / **`getDebugInfo()`**

### **3. ImageOverrideManager**

- **`ImageOverrideManager.clearOverrides()`** – Re-initialize with empty overrides.
- **`cancelPreloading()`** – Cancel in-progress image preload.
- **`clearExpiredCache()`** – Clear in-memory image/drawable caches.
- **`getCacheStatistics()`** – Alias for cache stats; **`getDebugInfo()`** for diagnostics.

### **4. SDKResourceBundle**

- **`SDKResourceBundle(context)`** – Helper for SDK string/image lookup.
- **`localizedString(key, fallback)`** / **`localizedString(key, locale, fallback)`**
- **`image(named)`** / **`image(named, fallbackResId)`** – Drawable resource ID by name.

### **5. Sample app**

- Uses **ThemeManager.setTheme()** and **ThemeManager.setLocale()** when theme/language changes.
- **SDK Debug Info** card on main screen shows **ThemeManager.getDebugInfo()** and **SDKResourceBundle** example.

---

## 📱 MINIMUM REQUIREMENTS

- **Android:** API 24 (Android 7.0) or higher
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.0 or higher
- **Gradle:** 8.0 or higher

---

## 🎨 Compose build compatibility (host app alignment)

The SDK AAR is built with the following Compose versions. **Host apps must use the same BOM and compiler** to avoid runtime `NoSuchMethodError`.

| Component | Version |
|-----------|---------|
| **Compose BOM** | `androidx.compose:compose-bom:2023.10.01` |
| **Compose compiler** | `1.5.3` (Kotlin 1.9.x) |

In your app's `build.gradle`:

```gradle
android {
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
}
dependencies {
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.foundation:foundation'
    implementation 'androidx.compose.material3:material3'
    // ... other Compose deps as needed
}
```

---

## 📦 WHAT'S INCLUDED

- `artiusid-sdk-1.2.53.aar` – Production SDK library
- `RELEASE_NOTES_v1.2.53.md` – This file

---

## 🚀 DEPLOYMENT

Run the automated publish script (or equivalent) to build and publish the AAR for v1.2.53.
