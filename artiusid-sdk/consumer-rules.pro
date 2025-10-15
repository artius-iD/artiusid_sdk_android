# artius.iD SDK - Consumer ProGuard Rules
# Author: Todd Bryant  
# Company: artius.iD, Inc.
# These rules are automatically applied to host applications using the SDK

# ================================
# SDK PUBLIC API PROTECTION
# ================================

# ✅ Keep SDK public API for host applications
-keep public class com.artiusid.sdk.ArtiusIDSDK {
    public *;
}

-keep public class com.artiusid.sdk.config.** {
    public *;
}

-keep public class com.artiusid.sdk.models.** {
    public *;
}

-keep public interface com.artiusid.sdk.callbacks.** {
    public *;
}

# ✅ Keep SDK error classes
-keep public class com.artiusid.sdk.models.SDKError {
    public *;
}

-keep public enum com.artiusid.sdk.models.SDKErrorCode {
    public *;
}

# ================================
# REQUIRED DEPENDENCIES
# ================================

# ✅ Ensure host app preserves required components
-keep class androidx.compose.** { *; }
-keep class com.google.firebase.** { *; }
-keep class androidx.camera.** { *; }
-keep class com.google.mlkit.** { *; }

# ✅ Keep Hilt components for SDK integration - CRITICAL
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }

# Keep all Hilt generated components (MUST NOT BE OBFUSCATED)
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class **_HiltComponents { *; }
-keep class **_HiltComponents$* { *; }
-keep class **_MembersInjector { *; }
-keep class **_Factory { *; }
-keep class **_Impl { *; }
-keep class **Hilt_** { *; }

# Keep Hilt entry points
-keep interface * extends dagger.hilt.internal.ComponentEntryPoint { *; }

# Keep classes with Hilt annotations
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Dagger modules
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Prevent obfuscation of Hilt generated code
-keepnames class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewWithFragmentComponentManager
-keepnames class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$ViewModelFactoriesEntryPoint

# ✅ Preserve SDK theme and configuration classes
-keep class com.artiusid.sdk.models.SDKThemeConfiguration { *; }
-keep class com.artiusid.sdk.models.EnhancedSDKThemeConfiguration { *; }
-keep class com.artiusid.sdk.config.SDKConfiguration { *; }

# ================================
# SECURITY NOTICE
# ================================
# Host applications using this SDK should implement additional
# security measures including:
# 1. Certificate pinning
# 2. Root detection  
# 3. Debugger detection
# 4. Emulator detection
# 5. App signing verification
