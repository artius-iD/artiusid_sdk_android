# ArtiusID Android SDK - Theming Guide

**Target:** Client Application Developers & cross-platform parity with iOS SDK  
**Status:** Production Ready

---

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [ThemeManager (Unified Entry)](#thememanager-unified-entry)
4. [Theme Components](#theme-components)
5. [Color Customization](#color-customization)
6. [Localization & Image Overrides](#localization--image-overrides)
7. [SDKResourceBundle](#sdkresourcebundle)
8. [Runtime Overrides & Debug](#runtime-overrides--debug)
9. [Branded Header](#branded-header)
10. [Predefined Themes](#predefined-themes)
11. [Full Example](#full-example)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Overview

The ArtiusID Android SDK theming system mirrors the iOS SDK for cross-platform consistency. Theme concepts and keys align with iOS: color scheme, typography, component styling, layout, animation, text content, icon theme. You can customize:

- **Colors** – Primary, secondary, backgrounds, buttons, status colors
- **Typography** – Font family, sizes, weights, paragraphSpacing (iOS parity)
- **Icons** – Colors and sizes; use `IconCategory` for key names (iOS parity)
- **Components** – Corner radius, heights, shadows
- **Layout** – Spacing, padding
- **Animations** – Durations, transitions
- **Text** – Via `SDKConfiguration.localizationOverrides` and `LocalizationKeys`
- **Images** – Via `SDKConfiguration.imageOverrides` and `SDKImageOverrides`

### Architecture

- **`ThemeManager`** – Unified facade: setTheme, setLocale, getDebugInfo (single entry for theme + locale + images; iOS parity).
- **`EnhancedSDKThemeConfiguration`** – Main theme container (brand, typography, colorScheme, iconTheme, textContent, componentStyling, layoutConfig, animationConfig)
- **`EnhancedThemeManager`** – Holds current theme, notifies listeners, builds Compose ColorScheme/Typography
- **`ColorManager`** – Singleton for current color scheme and gradient; applies enhanced theme
- **`ImageOverrideManager`** – Resolves image overrides (asset, URL, file); clearOverrides, cancelPreloading, clearExpiredCache, getDebugInfo
- **`LocalizationManager`** (utils) – String overrides and locale; addOverride, removeOverride, clearOverrides, getOverrideCount, getDebugInfo
- **`SDKResourceBundle`** – Helper for localizedString(key, fallback) and image(named) with a Context

---

## Quick Start

### Apply a predefined theme

```kotlin
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.ui.theme.ThemeManager

// artius.iD default (dark blue + orange)
val theme = EnhancedSDKThemeConfiguration.artiusIDDefault()

// Light blue theme
val theme = EnhancedSDKThemeConfiguration.lightBlue()

// Dark purple theme
val theme = EnhancedSDKThemeConfiguration.darkPurple()

ThemeManager.setTheme(theme)
```

Or use `ColorManager.setEnhancedTheme(theme)` / `EnhancedThemeManager.setThemeConfiguration(theme)` directly.

### Initialize SDK with theme and config

```kotlin
ArtiusIDSDK.initialize(
    context = context,
    configuration = SDKConfiguration(
        apiKey = "your-api-key",
        environment = Environment.STAGING,
        localizationOverrides = mapOf(
            LocalizationKeys.WELCOME_TITLE to "My App Verification"
        ),
        imageOverrides = SDKImageOverrides(brandLogo = "my_logo")
    ),
    theme = EnhancedSDKThemeConfiguration.lightBlue()
)
```

---

## Theme Components

### EnhancedSDKThemeConfiguration

```kotlin
data class EnhancedSDKThemeConfiguration(
    val brandName: String = "ArtiusID",
    val brandLogoUrl: String? = null,
    val brandLogoResourceName: String? = null,
    val typography: SDKTypography = SDKTypography(),
    val colorScheme: SDKColorScheme = SDKColorScheme(),
    val iconTheme: SDKIconTheme = SDKIconTheme(),
    val textContent: SDKTextContent = SDKTextContent(),
    val componentStyling: SDKComponentStyling = SDKComponentStyling(),
    val layoutConfig: SDKLayoutConfig = SDKLayoutConfig(),
    val animationConfig: SDKAnimationConfig = SDKAnimationConfig()
)
```

### SDKColorScheme (highlights)

- **Primary / secondary:** `primaryColorHex`, `secondaryColorHex`, `onPrimaryColorHex`, `onSecondaryColorHex`
- **Backgrounds:** `backgroundColorHex`, `surfaceColorHex`, `surfaceVariantColorHex`, `onBackgroundColorHex`, `onSurfaceColorHex`
- **Status:** `successColorHex`, `errorColorHex`, `warningColorHex`, `infoColorHex`
- **Buttons:** `primaryButtonColorHex`, `primaryButtonTextColorHex`, `secondaryButtonColorHex`, `secondaryButtonTextColorHex`, `disabledButtonColorHex`, `disabledButtonTextColorHex`
- **Overlays:** `faceDetectionOverlayColorHex`, `documentScanOverlayColorHex`, `progressIndicatorColorHex`
- **Borders:** Use `outlineColorHex` and `outlineVariantColorHex` (no `borderColorHex`; matches iOS).

---

## Color Customization

Example custom color scheme:

```kotlin
val theme = EnhancedSDKThemeConfiguration(
    brandName = "MyBrand",
    colorScheme = SDKColorScheme(
        primaryColorHex = "#003DA5",
        secondaryColorHex = "#00B4D8",
        backgroundColorHex = "#FFFFFF",
        surfaceColorHex = "#F5F5F5",
        onBackgroundColorHex = "#000000",
        onSurfaceColorHex = "#000000",
        primaryButtonColorHex = "#003DA5",
        primaryButtonTextColorHex = "#FFFFFF",
        secondaryButtonColorHex = "#E8F0FE",
        secondaryButtonTextColorHex = "#003DA5",
        outlineColorHex = "#BDBDBD",
        outlineVariantColorHex = "#E0E0E0"
    )
)
ColorManager.setEnhancedTheme(theme)
```

---

## Localization & Image Overrides

### Localization keys (iOS parity)

Use `LocalizationKeys` for override keys so they match iOS:

```kotlin
import com.artiusid.sdk.localization.LocalizationKeys

val overrides = mapOf(
    LocalizationKeys.WELCOME_TITLE to "My Welcome",
    LocalizationKeys.FACE_SCAN_TITLE to "Face Verification",
    LocalizationKeys.CONTINUE_BUTTON to "Next"
)

SDKConfiguration(
    apiKey = "...",
    localizationOverrides = overrides
)
```

Strings are in `res/values/sdk_strings.xml` (and locale variants). Runtime lookup: `LocalizationManager.getString(context, key, fallback)` or use **SDKResourceBundle** (see below).

### Image overrides

`SDKImageOverrides` supports face overlay, document overlays, UI icons, status icons, brand logo, and `customOverrides`. Strategies: `ASSET`, `URL`, `FILE`, `AUTO_DETECT`. See `SDKConfiguration.imageOverrides` and `ImageOverrideManager`.

---

## SDKResourceBundle

Helper for string and image lookup with override and locale support (iOS parity: `localizedString(key, fallback)`, `image(named)`).

```kotlin
import com.artiusid.sdk.utils.SDKResourceBundle

val bundle = SDKResourceBundle(context)

// Localized string by key (uses LocalizationManager overrides + SDK resources)
val title = bundle.localizedString("welcome_title", "Welcome")
val forLocale = bundle.localizedString("face_scan_title", Locale("es"), "Face Verification")

// Drawable resource ID by name (SDK package)
val logoResId = bundle.image("brand_logo")
val iconResId = bundle.image("success_icon", R.drawable.done_icon)
```

For override-aware image loading (URL/asset/file), use **ImageOverrideManager** and **ThemedImage** in the UI.

---

## Runtime Overrides & Debug

### LocalizationManager (utils)

Runtime string overrides and debug (iOS parity):

```kotlin
import com.artiusid.sdk.utils.LocalizationManager

LocalizationManager.addOverride("welcome_title", "My Welcome")
LocalizationManager.removeOverride("welcome_title")
LocalizationManager.clearOverrides()

val count = LocalizationManager.getOverrideCount()
val keys = LocalizationManager.getOverrideKeys()
val debug = LocalizationManager.getDebugInfo()
```

### ImageOverrideManager

Clear overrides, cancel preloading, clear caches, debug:

```kotlin
import com.artiusid.sdk.utils.ImageOverrideManager

ImageOverrideManager.clearOverrides()       // Re-initialize with empty overrides
ImageOverrideManager.cancelPreloading()    // Cancel in-progress preload
ImageOverrideManager.getInstance().clearExpiredCache()
val stats = ImageOverrideManager.getInstance().getCacheStatistics()
val debug = ImageOverrideManager.getInstance().getDebugInfo()
```

---

## Branded Header

To show your brand name and/or logo in the SDK header:

1. Set **theme**: `EnhancedSDKThemeConfiguration` with `brandName` and optionally `brandLogoResourceName` or `brandLogoUrl`.
2. Set **image override**: `SDKImageOverrides(brandLogo = "your_logo_asset_or_url")` in `SDKConfiguration.imageOverrides`.
3. In Compose, use the theme’s `brandName` and resolve the logo via **ImageOverrideManager** or **ThemedImage** with key `"brand_logo"`.

Example:

```kotlin
val theme = EnhancedSDKThemeConfiguration(
    brandName = "MyBank",
    brandLogoResourceName = "img_mybank_logo"
)
ThemeManager.setTheme(theme)
```

Ensure the SDK flow uses `LocalSDKTheme.current` (or `ThemeManager.getCurrentTheme()`) to read `brandName` and that the header composable loads the logo with the override key `brand_logo`.

---

## Predefined Themes

| Preset | Description |
|--------|-------------|
| `EnhancedSDKThemeConfiguration.artiusIDDefault()` | Dark blue (#22354D), orange (#F58220), white text |
| `EnhancedSDKThemeConfiguration.lightBlue()` | Light background, blue primary (#003DA5), cyan secondary (#00B4D8) |
| `EnhancedSDKThemeConfiguration.darkPurple()` | Dark gray background (#1F2937), purple (#6366F1), pink (#EC4899) |

---

## Full Example

```kotlin
// 1. Initialize SDK with theme and overrides
ArtiusIDSDK.initialize(
    context = context,
    configuration = SDKConfiguration.production(
        apiKey = "your-api-key",
        clientId = 1L,
        clientGroupId = 1L
    ).copy(
        localizationOverrides = mapOf(
            LocalizationKeys.WELCOME_TITLE to "My App Verification"
        ),
        imageOverrides = SDKImageOverrides(brandLogo = "my_logo")
    ),
    theme = EnhancedSDKThemeConfiguration.lightBlue()
)

// 2. Optional: use ThemeManager for runtime changes
ThemeManager.setLocale("es")
ThemeManager.setTheme(EnhancedSDKThemeConfiguration.darkPurple())

// 3. Strings and images via SDKResourceBundle
val bundle = SDKResourceBundle(context)
val welcome = bundle.localizedString("welcome_title", "Welcome")
val logoId = bundle.image("brand_logo")

// 4. Debug
Log.d("SDK", ThemeManager.getDebugInfo())
```

---

## Best Practices

1. Use **LocalizationKeys** for any string override so keys align with iOS.
2. Use **outlineColorHex** / **outlineVariantColorHex** for borders (no `borderColorHex`).
3. Set the theme before showing SDK UI (e.g. in `ArtiusIDSDK.initialize` or before launching SDK flows).
4. For custom fonts, set `typography.fontFamily` and ensure the font is available to the app/SDK.
5. Prefer **ThemeManager** when you want one place to set theme and locale and to get combined debug info.

---

## Troubleshooting

- **Colors not updating:** Ensure `ColorManager.setEnhancedTheme(theme)` is called before the SDK UI is displayed.
- **Strings not found:** Ensure the key exists in `sdk_strings.xml` or in your `localizationOverrides` map; use `LocalizationKeys` constants.
- **Images not loading:** Check `ImageOverrideManager` is initialized (via `ImageOverrideInitializer` during SDK init) and that `imageOverrides` use the correct strategy (ASSET/URL/FILE) and resource names.
