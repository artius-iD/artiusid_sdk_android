/*
 * File: SDKSecurityManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * Advanced Security Manager for artius.iD SDK
 * Implements multiple layers of runtime security checks
 */
object SDKSecurityManager {
    private const val TAG = "SDKSecurity"
    
    // ✅ Expected signature hash of legitimate host applications
    private val ALLOWED_SIGNATURE_HASHES = setOf(
        // Add your production app signature hashes here
        "YOUR_PRODUCTION_APP_SIGNATURE_HASH",
        "DEVELOPMENT_SIGNATURE_HASH_FOR_TESTING"
    )
    
    /**
     * Comprehensive security validation
     * @return true if all security checks pass
     */
    fun validateSecurityEnvironment(context: Context): Boolean {
        if (!isValidHostApplication(context)) {
            Log.e(TAG, "❌ Invalid host application detected")
            return false
        }
        
        if (isDebuggingDetected()) {
            Log.e(TAG, "❌ Debugging detected - SDK disabled")
            return false
        }
        
        if (isEmulatorDetected()) {
            Log.e(TAG, "❌ Emulator detected - SDK disabled")
            return false
        }
        
        if (isRootDetected(context)) {
            Log.e(TAG, "❌ Root detected - SDK disabled")
            return false
        }
        
        if (isTamperingDetected(context)) {
            Log.e(TAG, "❌ Tampering detected - SDK disabled")
            return false
        }
        
        Log.d(TAG, "✅ All security checks passed")
        return true
    }
    
    /**
     * Verify host application signature
     */
    private fun isValidHostApplication(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            if (signatures.isEmpty()) return false
            
            val signature = signatures[0]
            val signatureHash = MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            val isValid = ALLOWED_SIGNATURE_HASHES.contains(signatureHash)
            
            if (!isValid) {
                Log.w(TAG, "⚠️ Unknown signature hash: $signatureHash")
            }
            
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "❌ Signature validation failed", e)
            false
        }
    }
    
    /**
     * Detect debugging attempts
     */
    private fun isDebuggingDetected(): Boolean {
        // Check if debugger is connected
        if (Debug.isDebuggerConnected()) {
            return true
        }
        
        // Check if waiting for debugger
        if (Debug.waitingForDebugger()) {
            return true
        }
        
        // Check debug flags
        try {
            val debugFlag = android.os.Debug::class.java
                .getDeclaredField("DEBUG_ENABLE_DEBUGGER")
            debugFlag.isAccessible = true
            if (debugFlag.getBoolean(null)) {
                return true
            }
        } catch (e: Exception) {
            // Field might not exist in all Android versions
        }
        
        return false
    }
    
    /**
     * Detect emulator environment
     */
    private fun isEmulatorDetected(): Boolean {
        // Check build properties
        if (Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.lowercase().contains("vbox") ||
            Build.FINGERPRINT.lowercase().contains("test-keys") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
            "google_sdk" == Build.PRODUCT) {
            return true
        }
        
        // Check for emulator files
        val emulatorFiles = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
        )
        
        for (file in emulatorFiles) {
            if (File(file).exists()) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Detect rooted device
     */
    private fun isRootDetected(context: Context): Boolean {
        // Check for common root binaries
        val rootBinaries = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        for (binary in rootBinaries) {
            if (File(binary).exists()) {
                return true
            }
        }
        
        // Check for root management apps
        val rootApps = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot"
        )
        
        return rootApps.any { isPackageInstalled(context, it) }
    }
    
    /**
     * Detect tampering attempts
     */
    private fun isTamperingDetected(context: Context): Boolean {
        // Check if app is installed from unknown sources
        try {
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            val validInstallers = setOf(
                "com.android.vending", // Google Play Store
                "com.amazon.venezia",  // Amazon App Store
                "com.sec.android.app.samsungapps", // Samsung Galaxy Store
                null // Direct installation (development)
            )
            
            if (installer !in validInstallers) {
                Log.w(TAG, "⚠️ App installed from unknown source: $installer")
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not verify installer", e)
        }
        
        // Check for Xposed framework
        try {
            Class.forName("de.robv.android.xposed.XposedHelpers")
            return true
        } catch (e: ClassNotFoundException) {
            // Xposed not detected
        }
        
        // Check for Frida
        try {
            val fridaFiles = arrayOf(
                "/data/local/tmp/frida-server",
                "/data/local/tmp/re.frida.server"
            )
            
            for (file in fridaFiles) {
                if (File(file).exists()) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return false
    }
    
    /**
     * Check if a package is installed
     */
    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate runtime integrity hash
     */
    fun generateIntegrityHash(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signature = packageInfo.signatures[0]
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(signature.toByteArray())
            digest.update(Build.FINGERPRINT.toByteArray())
            digest.update(context.packageName.toByteArray())
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to generate integrity hash", e)
            "INVALID"
        }
    }
}
