# ========================================
# HEAVY OBFUSCATION PROGUARD RULES
# For Customer Distribution Sample App
# ========================================

# ✅ BALANCED OBFUSCATION SETTINGS (Memory Optimized)
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 3
-allowaccessmodification
-repackageclasses 'a'

# ✅ RENAME EVERYTHING POSSIBLE
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# ✅ REMOVE DEBUG INFO
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ========================================
# PRESERVE ESSENTIAL ANDROID COMPONENTS
# ========================================

# Keep Activities (Android requires these)
-keep public class * extends android.app.Activity
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends androidx.fragment.app.FragmentActivity

# Keep Application class
-keep public class * extends android.app.Application

# Keep essential Android lifecycle methods
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ========================================
# PRESERVE SDK INTEGRATION POINTS
# ========================================

# Keep ArtiusID SDK public API (customers need these)
-keep class com.artiusid.sdk.ArtiusIDSDK { *; }
-keep class com.artiusid.sdk.config.** { *; }
-keep class com.artiusid.sdk.models.** { *; }
-keep class com.artiusid.sdk.callbacks.** { *; }

# Keep SDK theme and configuration classes
-keep class com.artiusid.sdk.models.SDKThemeConfiguration { *; }
-keep class com.artiusid.sdk.models.EnhancedSDKThemeConfiguration { *; }
-keep class com.artiusid.sdk.models.SDKImageOverrides { *; }

# ========================================
# PRESERVE FRAMEWORK REQUIREMENTS
# ========================================

# Hilt/Dagger
-dontwarn com.google.dagger.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil (Image loading)
-keep class coil.** { *; }

# ========================================
# OBFUSCATE SAMPLE APP INTERNALS
# ========================================

# HEAVILY OBFUSCATE: All sample app business logic
-keep,allowobfuscation class com.artiusid.sample.** {
    # Keep only essential public methods for Android
    public <init>(...);
}

# OBFUSCATE: All theme implementations (keep structure, hide logic)
-keep,allowobfuscation class com.artiusid.sample.theme.** {
    public <methods>;
}

# OBFUSCATE: All configuration classes (hide implementation details)
-keep,allowobfuscation class com.artiusid.sample.config.** {
    public <methods>;
}

# OBFUSCATE: All localization logic
-keep,allowobfuscation class com.artiusid.sample.localization.** {
    public <methods>;
}

# ========================================
# REMOVE DEBUGGING & LOGGING
# ========================================

# Remove all Log calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove println calls
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}

# ========================================
# AGGRESSIVE OPTIMIZATION
# ========================================

# Remove unused code aggressively
-dontwarn **
-ignorewarnings

# Optimize method calls
-optimizations !code/simplification/variable

# Remove unused parameters
-optimizations !method/removal/parameter

# ========================================
# PRESERVE REFLECTION USAGE
# ========================================

# Keep classes that might be accessed via reflection
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========================================
# FINAL OBFUSCATION PASS
# ========================================

# Ensure maximum obfuscation of internal methods
-keepclassmembers,allowobfuscation class com.artiusid.sample.** {
    !public <methods>;
    !public <fields>;
}
