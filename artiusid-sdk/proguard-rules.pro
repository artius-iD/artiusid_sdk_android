# artius.iD SDK - Advanced Security ProGuard Rules
# Author: Todd Bryant
# Company: artius.iD, Inc.

# ================================
# MAXIMUM SECURITY CONFIGURATION
# ================================

# ✅ CRITICAL: Aggressive obfuscation settings
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-forceprocessing
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'a'
-flattenpackagehierarchy 'a'

# ✅ Remove ONLY verbose and debug logging in release builds
# KEEP ERROR, WARNING, and INFO logs for debugging customer issues
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# ✅ Remove debug logging from SDK (keep error/warning/info)
-assumenosideeffects class com.artiusid.sdk.** {
    public static void log*(...);
    public static void debug*(...);
    public static void print*(...);
}

# ✅ NEVER strip error logs from ArtiusIDSDK (critical for customer support)
-keepclassmembers class com.artiusid.sdk.ArtiusIDSDK {
    private static final java.lang.String TAG;
}

# ================================
# PROTECT SDK PUBLIC API
# ================================

# ✅ Keep main SDK entry points (public API)
-keep public class com.artiusid.sdk.ArtiusIDSDK {
    public *;
}

# ✅ Keep certificate registration API methods and their dependencies (v1.2.13+)
-keepclassmembers class com.artiusid.sdk.ArtiusIDSDK {
    public *** ensureCertificateRegistered(...);
    public *** isCertificateRegistered(...);
    private static *** sdkConfiguration;
}

# ✅ Keep APIManager for certificate registration
-keep class com.artiusid.sdk.services.APIManager {
    public <init>(...);
    public *** loadCertificateFromFullUrl(...);
    public *** loadCertificate(...);
}
-keepclassmembers class com.artiusid.sdk.services.APIManager {
    private static final java.lang.String TAG;
}

# ✅ Keep configuration classes (public API)
-keep public class com.artiusid.sdk.config.** {
    public *;
}

# ✅ Keep model classes (public API)
-keep public class com.artiusid.sdk.models.** {
    public *;
}

# ✅ Keep ColorManager and EnhancedThemeManager (CRITICAL for enhanced theming)
-keep class com.artiusid.sdk.ui.theme.ColorManager { *; }
-keepclassmembers class com.artiusid.sdk.ui.theme.ColorManager { *; }
-keep class com.artiusid.sdk.ui.theme.EnhancedThemeManager { *; }
-keepclassmembers class com.artiusid.sdk.ui.theme.EnhancedThemeManager { *; }

# ✅ Keep callback interfaces (public API)
-keep public interface com.artiusid.sdk.callbacks.** {
    public *;
}

# ================================
# OBFUSCATE INTERNAL IMPLEMENTATION
# ================================

# ✅ Keep UrlBuilder and DeviceUtils for certificate registration (v1.2.13+)
-keep class com.artiusid.sdk.utils.UrlBuilder {
    public static *** getLoadCertificateUrl(...);
    public static *** setConfiguration(...);
}
-keep class com.artiusid.sdk.util.DeviceUtils {
    public static *** getDeviceId(...);
}
-keep class com.artiusid.sdk.utils.DeviceUtils {
    public static *** getDeviceId(...);
}

# ✅ Heavily obfuscate internal utils (CRITICAL SECURITY)
# But allow certificate-related classes to function
-keep,allowobfuscation class com.artiusid.sdk.utils.** {
    *;
}

# ✅ Keep CertificateManager methods needed for registration
-keep class com.artiusid.sdk.utils.CertificateManager {
    public <init>(...);
    public *** loadCertificatePem(...);
    public *** storeCertificatePem(...);
    public *** generateCSR(...);
}

# ✅ Heavily obfuscate verification state (CRITICAL SECURITY)
-keep,allowobfuscation class com.artiusid.sdk.utils.VerificationStateManager {
    *;
}

# ✅ Heavily obfuscate Firebase token management (CRITICAL SECURITY)
-keep,allowobfuscation class com.artiusid.sdk.utils.FirebaseTokenManager {
    *;
}

# ✅ Obfuscate network layer (API endpoints, requests)
-keep,allowobfuscation class com.artiusid.sdk.data.** {
    *;
}

# ✅ Obfuscate presentation layer (UI implementation)
-keep,allowobfuscation class com.artiusid.sdk.presentation.** {
    *;
}

# ================================
# ANDROID & THIRD-PARTY LIBRARIES
# ================================

# ✅ Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ✅ Keep Jetpack Compose (required for UI)
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# ✅ Keep Hilt/Dagger (dependency injection) - CRITICAL FOR HILT TO WORK
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

# Keep ALL Hilt modules and their generated code (CRITICAL for ViewModel factories)
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_HiltModules_** { *; }
-keep class **_HiltModules_*$** { *; }
-keep class **_ProvideFactory { *; }
-keep class **_KeyModule { *; }
-keep class **_KeyModule_** { *; }
-keep class **_KeyModule$** { *; }

# Keep ViewModel factory methods
-keepclassmembers class **_HiltModules_KeyModule_ProvideFactory {
    public *;
}
-keepclassmembers class **_HiltModules_** {
    public * provide*(...);
}

# Keep ALL Dagger @Provides methods - CRITICAL for AppModule
-keepclassmembers class * {
    @dagger.Provides public *;
    @dagger.Provides static *;
}

# Keep all provide*() methods in ALL Factory classes (AppModule, ViewModels, etc.)
-keepclassmembers class **_Factory {
    public * provide*(...);
    public static * provide*(...);
}

# Keep all provide*() methods in AppModule factories specifically
-keepclassmembers class **AppModule_Provide** {
    public * provide*(...);
    public static * provide*(...);
}

# Keep Dagger Provider interface implementations
-keep,allowobfuscation class * implements javax.inject.Provider {
    public * get();
}
-keepclassmembers class * implements javax.inject.Provider {
    public * provide*(...);
    public static * provide*(...);
}

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

# ✅ Keep Firebase (FCM)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ✅ Keep Retrofit/OkHttp (networking)
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp.** { *; }

# ✅ Keep Gson (JSON serialization)
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ✅ Keep ML Kit (face detection, text recognition)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# ✅ Keep CameraX
-keep class androidx.camera.** { *; }

# ✅ Keep NFC/JMRTD (passport reading)
-keep class org.jmrtd.** { *; }
-keep class net.sf.scuba.** { *; }

# ================================
# ANTI-TAMPERING MEASURES
# ================================

# ✅ Obfuscate reflection usage
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ✅ Remove source file names and line numbers (anti-debugging)
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ✅ Keep native methods (if any)
-keepclasseswithmembernames class * {
    native <methods>;
}

# ✅ Keep serializable classes structure
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================
# ADDITIONAL SECURITY MEASURES
# ================================

# ✅ Warn about potential issues
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**

# ✅ Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ✅ Preserve stack traces for crash reporting (but obfuscated)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
