# 🎨 artius.iD SDK - Image Override System Documentation

## 📋 **Overview**

The artius.iD SDK Image Override System allows host applications to completely customize all images and animated GIFs within the SDK. This provides comprehensive visual branding control while maintaining the SDK's full functionality.

---

## 🏗️ **Architecture**

### **Core Components**

1. **SDKImageOverrides** - Configuration data structure with 40+ override fields
2. **ImageOverrideManager** - Centralized image resolution and caching
3. **ThemedImage/ThemedGifAnimation** - Override-aware UI components
4. **ImageOverrideInitializer** - Enhanced Coil setup and initialization
5. **Sample App Integration** - Complete configuration UI and examples

### **Data Flow**

```
Host App → SDKConfiguration → ImageOverrideManager → ThemedComponents → UI
```

---

## 🎯 **Key Features**

### **✅ Loading Strategies**
- **URL-based**: `https://example.com/custom_face_overlay.png`
- **Asset-based**: `"custom_face_overlay"` (from host app assets)
- **File-based**: `"file:///android_asset/custom_images/face_overlay.png"`
- **Resource ID**: Direct Android resource IDs
- **Auto-Detection**: Automatically detects strategy from string format

### **✅ Advanced Capabilities**
- **Caching System**: Memory + disk caching with configurable duration
- **Fallback Support**: Graceful fallback to default SDK assets
- **Preloading**: Optional image preloading on SDK initialization
- **Performance Optimized**: Enhanced Coil configuration with hardware acceleration
- **Multi-Density Support**: Automatic handling of different screen densities

---

## 📱 **Implementation Guide**

### **Step 1: Configure Image Overrides**

```kotlin
import com.artiusid.sdk.models.SDKImageOverrides
import com.artiusid.sdk.models.ImageLoadingStrategy

val imageOverrides = SDKImageOverrides(
    // Face scan assets
    faceOverlay = "custom_face_overlay",
    faceUpGif = "custom_face_up_animation",
    faceDownGif = "custom_face_down_animation",
    phoneUpGif = "custom_phone_up_animation",
    phoneDownGif = "custom_phone_down_animation",
    
    // Document assets
    passportOverlay = "custom_passport_overlay",
    stateIdFrontOverlay = "custom_stateid_front_overlay",
    stateIdBackOverlay = "custom_stateid_back_overlay",
    passportAnimationGif = "custom_passport_animation",
    stateIdAnimationGif = "custom_stateid_animation",
    
    // UI icons
    backButtonIcon = "custom_back_button",
    cameraButtonIcon = "custom_camera_button",
    scanFaceIcon = "custom_scan_face",
    docScanIcon = "custom_doc_scan",
    
    // Status icons
    successIcon = "custom_success",
    failedIcon = "custom_failed",
    errorIcon = "custom_error",
    
    // Brand assets
    brandLogo = "custom_logo",
    brandImage = "custom_brand_image",
    
    // Document selection icons
    passportIcon = "custom_passport_icon",
    stateIdIcon = "custom_stateid_icon",
    
    // Configuration
    defaultLoadingStrategy = ImageLoadingStrategy.ASSET,
    enableCaching = true,
    enableFallback = true,
    preloadImages = true
)
```

### **Step 2: Initialize SDK with Overrides**

```kotlin
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.ArtiusIDSDK

val sdkConfig = SDKConfiguration(
    apiKey = "your_api_key",
    environment = Environment.PRODUCTION,
    enableLogging = true,
    imageOverrides = imageOverrides
)

ArtiusIDSDK.initializeWithEnhancedTheme(
    context = this,
    configuration = sdkConfig,
    enhancedTheme = yourThemeConfig
)
```

### **Step 3: Add Override Assets**

#### **Asset-based (Recommended)**
```
your-app/src/main/assets/
├── custom_face_overlay.png
├── custom_face_up_animation.gif
├── custom_passport_overlay.png
├── custom_logo.png
└── ...
```

#### **URL-based**
```kotlin
val urlOverrides = SDKImageOverrides(
    faceOverlay = "https://yourcdn.com/custom/face_overlay.png",
    brandLogo = "https://yourcdn.com/custom/logo.png",
    successIcon = "https://yourcdn.com/custom/success.png"
)
```

#### **File-based**
```kotlin
val fileOverrides = SDKImageOverrides(
    faceOverlay = "file:///android_asset/custom_images/face_overlay.png",
    brandLogo = "file:///android_asset/custom_images/logo.png"
)
```

---

## 🎨 **Override Categories**

### **Face Scan Assets (5 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `face_overlay` | `R.drawable.face_overlay` | Face outline overlay for positioning |
| `face_up_gif` | `R.raw.face_up` | Face positioning up animation |
| `face_down_gif` | `R.raw.face_down` | Face positioning down animation |
| `phone_up_gif` | `R.raw.phone_up` | Phone positioning up animation |
| `phone_down_gif` | `R.raw.phone_down` | Phone positioning down animation |

### **Document Assets (5 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `passport_overlay` | `R.drawable.passport_overlay` | Passport scan overlay |
| `state_id_front_overlay` | `R.drawable.state_id_front_overlay` | State ID front overlay |
| `state_id_back_overlay` | `R.drawable.state_id_back_overlay` | State ID back overlay |
| `passport_animation_gif` | `R.drawable.passport_animation` | Passport scan animation |
| `state_id_animation_gif` | `R.drawable.stateid_animation` | State ID scan animation |

### **UI Icons (6 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `back_button_icon` | `R.drawable.back_button_icon` | Back navigation button |
| `camera_button_icon` | `R.drawable.camera_button_icon` | Camera capture button |
| `scan_face_icon` | `R.drawable.scan_face_icon` | Face scan step icon |
| `doc_scan_icon` | `R.drawable.doc_scan_icon` | Document scan step icon |
| `passport_icon` | `R.drawable.passport_icon` | Passport selection icon |
| `state_id_icon` | `R.drawable.stateid_icon` | State ID selection icon |

### **Status Icons (3 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `success_icon` | `R.drawable.img_success` | Success/approval icon |
| `failed_icon` | `R.drawable.img_failed` | Failure/error icon |
| `error_icon` | `R.drawable.img_system_error` | System error icon |

### **Brand Assets (2 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `brand_logo` | `R.drawable.logo` | Primary brand logo |
| `brand_image` | `R.drawable.img_artiusid_ios` | Brand illustration |

### **Instruction Icons (6 items)**
| Override Key | Default Resource | Description |
|--------------|------------------|-------------|
| `no_glasses_icon` | `R.drawable.no_glasses_icon` | Remove glasses instruction |
| `no_hat_icon` | `R.drawable.no_hat_icon` | Remove hat instruction |
| `no_mask_icon` | `R.drawable.no_mask_icon` | Remove mask instruction |
| `good_light_icon` | `R.drawable.good_light_icon` | Good lighting instruction |
| `lay_flat_icon` | `R.drawable.lay_flat_icon` | Lay document flat instruction |
| `no_glare_icon` | `R.drawable.no_glare_icon` | Avoid glare instruction |

---

## ⚙️ **Configuration Options**

### **Loading Strategy**
```kotlin
enum class ImageLoadingStrategy {
    AUTO_DETECT,  // Automatically detect from string format
    URL,          // Force load from web URLs
    ASSET,        // Force load from host app assets
    FILE,         // Force load from file system paths
    RESOURCE      // Use Android resource IDs
}
```

### **Caching Configuration**
```kotlin
val overrides = SDKImageOverrides(
    // ... your overrides ...
    
    enableCaching = true,                    // Enable image caching
    cacheDurationMs = 24 * 60 * 60 * 1000L, // 24 hours cache duration
    enableFallback = true,                   // Fallback to defaults on error
    preloadImages = false                    // Preload on SDK initialization
)
```

### **Custom Overrides**
```kotlin
val overrides = SDKImageOverrides(
    // ... standard overrides ...
    
    customOverrides = mapOf(
        "special_animation" to "holiday_animation",
        "branded_background" to "company_background",
        "loading_spinner" to "custom_loading_animation"
    )
)
```

---

## 🧪 **Sample App Integration**

The sample app demonstrates complete image override functionality:

### **Available Override Sets**
1. **SDK Default** - Use default SDK images
2. **Corporate Theme** - Professional blue/grey styling
3. **Modern Theme** - Sleek, modern design with gradients
4. **URL-Based (Demo)** - Load images from web URLs
5. **File-Based (Demo)** - Load from local file system paths
6. **Custom Extended** - Demonstrates extensibility features

### **UI Features**
- **Dropdown Selection** - Easy override set switching
- **Statistics Display** - Shows active override count and percentage
- **Real-time Preview** - Immediate visual feedback
- **Configuration Export** - Copy configuration for production use

---

## 🔧 **Advanced Usage**

### **Custom UI Components**

```kotlin
@Composable
fun MyCustomScreen() {
    // Use themed image with override support
    ThemedImage(
        defaultResourceId = R.drawable.my_default_image,
        overrideKey = "my_custom_image",
        contentDescription = "My Custom Image",
        modifier = Modifier.size(100.dp)
    )
    
    // Use themed GIF animation with override support
    ThemedGifAnimation(
        defaultResourceId = R.raw.my_default_animation,
        overrideKey = "my_custom_animation",
        contentDescription = "My Custom Animation",
        modifier = Modifier.size(200.dp)
    )
}
```

### **Runtime Override Updates**

```kotlin
// Update overrides at runtime (requires SDK re-initialization)
fun updateImageOverrides(newOverrides: SDKImageOverrides) {
    val newConfig = currentSDKConfig.copy(imageOverrides = newOverrides)
    
    ArtiusIDSDK.initializeWithEnhancedTheme(
        context = this,
        configuration = newConfig,
        enhancedTheme = currentTheme
    )
}
```

### **Override Validation**

```kotlin
import com.artiusid.sample.config.ImageOverrideHelper

val overrides = SDKImageOverrides(/* your config */)
val issues = ImageOverrideHelper.validateOverrides(overrides)

if (issues.isNotEmpty()) {
    Log.w("ImageOverrides", "Configuration issues: ${issues.joinToString()}")
}
```

---

## 📊 **Performance Considerations**

### **Best Practices**
- ✅ Use appropriate image compression (PNG with alpha, optimized GIFs)
- ✅ Consider multi-density assets for different screen sizes
- ✅ Enable caching for frequently accessed images
- ✅ Use HTTPS URLs for web-hosted assets
- ✅ Test loading performance with large assets
- ✅ Implement proper error handling for missing assets

### **Memory Management**
- The system automatically manages memory usage through Coil's caching
- Disk cache uses 2% of available storage by default
- Memory cache uses 25% of available memory by default
- Hardware acceleration is enabled for better performance

### **Network Considerations**
- URL-based overrides require network connectivity
- Implement offline fallbacks for critical assets
- Consider CDN usage for better global performance
- Monitor network usage for large GIF animations

---

## 🐛 **Troubleshooting**

### **Common Issues**

#### **Override Not Loading**
```kotlin
// Check logs for loading errors
Log.d("ImageOverride", "Override loading failed: ${error.message}")

// Verify asset exists in correct location
val assetExists = assets.list("").contains("my_custom_image.png")

// Ensure fallback is enabled
val overrides = SDKImageOverrides(
    myCustomImage = "my_custom_image",
    enableFallback = true  // This ensures fallback to default
)
```

#### **Performance Issues**
```kotlin
// Disable preloading for faster startup
val overrides = SDKImageOverrides(
    // ... your overrides ...
    preloadImages = false
)

// Reduce cache duration for memory-constrained devices
val overrides = SDKImageOverrides(
    // ... your overrides ...
    cacheDurationMs = 60 * 60 * 1000L  // 1 hour instead of 24
)
```

#### **URL Loading Failures**
```kotlin
// Ensure network security config allows HTTP (if needed)
// Add to AndroidManifest.xml:
// android:networkSecurityConfig="@xml/network_security_config"

// Check URL accessibility
val overrides = SDKImageOverrides(
    brandLogo = "https://your-cdn.com/logo.png",  // Ensure this URL is accessible
    enableFallback = true  // Fallback to default on failure
)
```

---

## 📈 **Migration Guide**

### **From Previous Versions**

If you're upgrading from a version without image override support:

1. **Update SDK Configuration**:
```kotlin
// Old configuration
val oldConfig = SDKConfiguration(apiKey = "key")

// New configuration with image overrides
val newConfig = SDKConfiguration(
    apiKey = "key",
    imageOverrides = SDKImageOverrides()  // Start with defaults
)
```

2. **Gradual Override Implementation**:
```kotlin
// Start with just brand assets
val initialOverrides = SDKImageOverrides(
    brandLogo = "your_logo",
    brandImage = "your_brand_image"
)

// Gradually add more overrides
val expandedOverrides = initialOverrides.copy(
    faceOverlay = "custom_face_overlay",
    successIcon = "custom_success"
)
```

---

## 🎉 **Summary**

The artius.iD SDK Image Override System provides:

- **Complete Visual Control**: Override any SDK image or GIF
- **Flexible Loading**: Support for URLs, assets, files, and resources
- **Performance Optimized**: Advanced caching and hardware acceleration
- **Easy Integration**: Simple configuration and comprehensive documentation
- **Production Ready**: Robust error handling and fallback mechanisms

This system enables host applications to maintain complete brand consistency while leveraging the full power of the artius.iD SDK's verification and authentication capabilities.

---

**🔗 For additional support, please refer to the sample app implementation and asset examples provided in the project.**
