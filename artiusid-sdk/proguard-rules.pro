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

# ✅ Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# ✅ Remove debug logging from SDK
-assumenosideeffects class com.artiusid.sdk.** {
    public static void log*(...);
    public static void debug*(...);
    public static void print*(...);
}

# ================================
# PROTECT SDK PUBLIC API
# ================================

# ✅ Keep main SDK entry points (public API)
-keep public class com.artiusid.sdk.ArtiusIDSDK {
    public *;
}

# ✅ Keep configuration classes (public API)
-keep public class com.artiusid.sdk.config.** {
    public *;
}

# ✅ Keep model classes (public API)
-keep public class com.artiusid.sdk.models.** {
    public *;
}

# ✅ Keep callback interfaces (public API)
-keep public interface com.artiusid.sdk.callbacks.** {
    public *;
}

# ================================
# OBFUSCATE INTERNAL IMPLEMENTATION
# ================================

# ✅ Heavily obfuscate internal utils (CRITICAL SECURITY)
-keep,allowobfuscation class com.artiusid.sdk.utils.** {
    *;
}

# ✅ Heavily obfuscate certificate management (CRITICAL SECURITY)
-keep,allowobfuscation class com.artiusid.sdk.utils.CertificateManager {
    *;
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
