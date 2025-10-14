# Customer Distribution ProGuard Rules
# Basic obfuscation for customer distribution

# Keep main application class
-keep public class com.artiusid.sample.** { *; }

# Keep SDK classes (handled by SDK consumer rules)
-keep class com.artiusid.sdk.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep HILT
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep standard Android classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
