# Obfuscated Distribution ProGuard Rules
# Maximum obfuscation for IP protection

# Keep only essential public API
-keep public class com.artiusid.sample.SampleApplication { *; }
-keep public class com.artiusid.sample.BridgeMainActivity { *; }

# Keep SDK classes (handled by SDK consumer rules)
-keep class com.artiusid.sdk.** { *; }

# Keep Firebase essentials
-keep class com.google.firebase.** { *; }

# Keep HILT essentials
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Compose essentials
-keep class androidx.compose.** { *; }

# Keep Android framework classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service

# Aggressive obfuscation (dictionary files not needed for basic obfuscation)

# Remove debugging info
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
