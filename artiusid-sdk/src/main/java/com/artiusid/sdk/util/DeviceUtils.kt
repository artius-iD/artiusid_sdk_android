package com.artiusid.sdk.util

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Utility class for device-related operations
 */
object DeviceUtils {
    
    /**
     * Get device model
     */
    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    /**
     * Get Android version
     */
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
    
    /**
     * Get SDK version
     */
    fun getSDKVersion(): Int {
        return Build.VERSION.SDK_INT
    }
    
    /**
     * Get device ID (Android ID)
     */
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }
    
    /**
     * Check if device is rooted (basic check)
     */
    fun isDeviceRooted(): Boolean {
        val rootIndicators = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return rootIndicators.any { java.io.File(it).exists() }
    }
    
    /**
     * Check if device is in debug mode
     */
    fun isDebugMode(context: Context): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    /**
     * Get device information as map
     */
    fun getDeviceInfo(context: Context): Map<String, String> {
        return mapOf(
            "model" to getDeviceModel(),
            "androidVersion" to getAndroidVersion(),
            "sdkVersion" to getSDKVersion().toString(),
            "deviceId" to getDeviceId(context),
            "isRooted" to isDeviceRooted().toString(),
            "isDebug" to isDebugMode(context).toString()
        )
    }
}
