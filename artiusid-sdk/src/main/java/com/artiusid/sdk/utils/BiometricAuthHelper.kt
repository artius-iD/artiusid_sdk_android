package com.artiusid.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager.Authenticators
import android.hardware.biometrics.BiometricPrompt as SystemBiometricPrompt
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay

/**
 * BiometricAuthHelper - Centralized Face ID authentication utility
 * Provides consistent Face ID authentication across the app
 */
object BiometricAuthHelper {
    
    private const val TAG = "BiometricAuthHelper"

    /**
     * Check if Face ID authentication is available on the device
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val status = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        Log.d(TAG, "Biometric authentication status: $status")
        return status == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Check if device has Face ID hardware specifically
     */
    fun hasFaceIdHardware(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
        } else {
            // For older Android versions, assume Face ID if biometric is available
            canAuthenticate(context)
        }
    }
    
    /**
     * Check if device has fingerprint hardware
     */
    fun hasFingerprintHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }
    
    /**
     * Get preferred biometric type for the device
     */
    fun getPreferredBiometricType(context: Context): BiometricType {
        return when {
            hasFaceIdHardware(context) && !hasFingerprintHardware(context) -> BiometricType.FACE_ONLY
            !hasFaceIdHardware(context) && hasFingerprintHardware(context) -> BiometricType.FINGERPRINT_ONLY
            hasFaceIdHardware(context) && hasFingerprintHardware(context) -> BiometricType.BOTH_AVAILABLE
            else -> BiometricType.NONE_AVAILABLE
        }
    }
    
    /**
     * Check detailed biometric status for better error handling
     */
    fun getBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.Unsupported
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.Unknown
            else -> BiometricStatus.NotAvailable
        }
    }

    /**
     * Enhanced authenticate method with Face ID preference strategies
     * @param activity FragmentActivity instance
     * @param title Dialog title
     * @param subtitle Dialog subtitle
     * @param description Dialog description
     * @param forceFaceIdOnly If true, attempts to force Face ID only (experimental)
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails with error message
     * @param onUserCancel Called when user cancels authentication
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Face ID Authentication",
        subtitle: String = "Look at the front camera",
        description: String = "Face authentication required",
        forceFaceIdOnly: Boolean = true,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "Starting enhanced Face ID authentication...")
        
        // Log available biometric types
        val biometricType = getPreferredBiometricType(activity)
        Log.d(TAG, "Device biometric type: $biometricType")
        Log.d(TAG, "Face ID hardware: ${hasFaceIdHardware(activity)}")
        Log.d(TAG, "Fingerprint hardware: ${hasFingerprintHardware(activity)}")
        
        // Strategy 1: Use ONLY biometric authentication (no PIN/password fallback)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        Log.d(TAG, "🎯 Strategy 1: Using BIOMETRIC_STRONG only (no PIN/password fallback)")
        
        val executor = ContextCompat.getMainExecutor(activity)

        // Strategy 2: Very explicit messaging to discourage fingerprint use
        val enhancedTitle = if (forceFaceIdOnly && biometricType == BiometricType.BOTH_AVAILABLE) {
            "🚫 FACE ID ONLY - NO FINGERPRINT"
        } else {
            title
        }
        
        val enhancedSubtitle = if (forceFaceIdOnly && biometricType == BiometricType.BOTH_AVAILABLE) {
            "❌ Do NOT use fingerprint sensor ❌"
        } else {
            subtitle
        }
        
        val enhancedDescription = if (forceFaceIdOnly && biometricType == BiometricType.BOTH_AVAILABLE) {
            "FACE CAMERA ONLY: Look at front camera. Ignore fingerprint icon below."
        } else {
            description
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(enhancedTitle)
            .setSubtitle(enhancedSubtitle)
            .setDescription(enhancedDescription)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false) // Auto-confirm on success
        
        // Strategy 3: Always show cancel button since we're not using device credentials
        promptInfoBuilder.setNegativeButtonText("Cancel")
        
        val promptInfo = promptInfoBuilder.build()

        var biometricPromptInstance: BiometricPrompt? = null
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "✅ Face ID authentication succeeded")
                    Log.d(TAG, "✅ Authentication type: ${result.authenticationType}")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "❌ Face ID authentication error: Code=$errorCode, Message=$errString")
                    
                    // Strategy 4: Auto-retry if fingerprint was attempted but we want Face ID
                    if (forceFaceIdOnly && (errorCode == BiometricPrompt.ERROR_TIMEOUT || errorCode == BiometricPrompt.ERROR_CANCELED) && !hasFingerprintRetryAttempted(activity)) {
                        Log.d(TAG, "🔄 Auto-retrying with Face ID focus (fingerprint may have been attempted)...")
                        markFingerprintRetryAttempted(activity)
                        
                        // Retry immediately with more aggressive Face ID messaging
                        authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                        return
                    }
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            Log.d(TAG, "User canceled Face ID authentication")
                            onUserCancel?.invoke() ?: onError("Authentication canceled")
                        }
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            Log.d(TAG, "User clicked cancel button")
                            onUserCancel?.invoke() ?: onError("Authentication canceled")
                        }
                        BiometricPrompt.ERROR_TIMEOUT -> {
                            if (biometricType == BiometricType.BOTH_AVAILABLE && forceFaceIdOnly) {
                                onError("Face ID timeout. System may have prioritized fingerprint. Please try again and use the front camera only.")
                            } else {
                                onError("Face ID authentication timeout. Please try again.")
                            }
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            onError("No Face ID enrolled. Please set up Face ID in device settings.")
                        }
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                            onError("Face ID hardware not available on this device.")
                        }
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                            onError("Face ID hardware is currently unavailable.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            onError("Too many failed attempts. Face ID is temporarily locked.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Face ID is permanently locked. Please use device settings to unlock.")
                        }
                        else -> {
                            onError("Face ID authentication failed: $errString")
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ Face ID authentication failed - user can retry")
                    // If this is a fingerprint attempt, provide guidance
                    if (biometricType == BiometricType.BOTH_AVAILABLE && forceFaceIdOnly) {
                        Log.d(TAG, "🎯 Authentication failed - likely fingerprint attempt. Guiding user to Face ID...")
                        // Don't error out, let them try again but provide guidance
                    }
                }
            })
        
        biometricPromptInstance = biometricPrompt

        try {
            Log.d(TAG, "🚀 Launching enhanced Face ID prompt...")
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Face ID prompt", e)
            onError("Failed to start Face ID authentication: ${e.message}")
        }
    }
    
    /**
     * Fallback authentication strategy - tries different approaches
     */
    private fun authenticateFallback(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🔄 Attempting fallback Face ID authentication...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        // Fallback: Use only BIOMETRIC_STRONG without device credential
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Face Authentication Only")
            .setSubtitle("Please use the front camera for Face ID")
            .setDescription("Fingerprint authentication disabled - Face ID required")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "✅ Fallback Face ID authentication succeeded")
                    clearFingerprintRetryAttempted(activity)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "❌ Fallback Face ID authentication error: Code=$errorCode")
                    clearFingerprintRetryAttempted(activity)
                    onError("Face ID authentication failed: $errString")
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ Fallback Face ID authentication failed - user can retry")
                }
            })
        
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch fallback Face ID prompt", e)
            clearFingerprintRetryAttempted(activity)
            onError("Failed to start fallback authentication: ${e.message}")
        }
    }
    
    /**
     * Forced Face ID authentication with automatic cancellation of fingerprint
     * This method tries to force Face ID by cancelling if fingerprint is detected
     */
    private fun authenticateFaceIdForced(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🔥 FORCED: Starting Face ID with fingerprint auto-cancel...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("🎯 RETRY: FACE ID ONLY")
            .setSubtitle("Look at FRONT CAMERA immediately")
            .setDescription("FINAL ATTEMPT: Auto-cancels in 6 seconds. Use Face ID ONLY or authentication will fail.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
        
        var isCompleted = false
        var hasTimedOut = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (isCompleted) return
                    isCompleted = true
                    
                    Log.d(TAG, "✅ FORCED Face ID authentication succeeded")
                    timeoutHandler.removeCallbacksAndMessages(null)
                    clearFingerprintRetryAttempted(activity)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (isCompleted) return
                    isCompleted = true
                    
                    Log.e(TAG, "❌ FORCED Face ID authentication error: Code=$errorCode, Message=$errString")
                    timeoutHandler.removeCallbacksAndMessages(null)
                    clearFingerprintRetryAttempted(activity)
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_CANCELED -> {
                            if (hasTimedOut) {
                                Log.d(TAG, "🚫 Forced timeout cancellation - showing final error")
                                onError("Face ID authentication required. System detected fingerprint priority. Please enable Face ID preference in device biometric settings.")
                            } else {
                                onUserCancel?.invoke() ?: onError("Authentication canceled")
                            }
                        }
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            onUserCancel?.invoke() ?: onError("Authentication canceled")
                        }
                        BiometricPrompt.ERROR_TIMEOUT -> {
                            onError("Face ID timeout. Please try again and use the front camera immediately.")
                        }
                        else -> {
                            if (hasTimedOut) {
                                Log.d(TAG, "🚫 Forced timeout with error code $errorCode - showing final error")
                                onError("Face ID authentication required. System detected fingerprint priority. Please enable Face ID preference in device biometric settings.")
                            } else {
                                onError("Face ID authentication failed: $errString")
                            }
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ FORCED Face ID authentication failed - retry available")
                }
            })
        
        // Manual timeout for forced retry (slightly longer)
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                hasTimedOut = true
                Log.d(TAG, "⏰ FORCED TIMEOUT: 6 seconds elapsed - Face ID not used")
                Log.d(TAG, "🔧 Aggressively canceling forced biometric prompt...")
                try {
                    // Multiple cancel attempts to ensure it works
                    biometricPrompt.cancelAuthentication()
                    Log.d(TAG, "✅ First cancel attempt completed")
                    
                    // Add a small delay then try again if still not completed
                    timeoutHandler.postDelayed({
                        if (!isCompleted) {
                            Log.d(TAG, "🔧 Second cancel attempt - dialog still open")
                            try {
                                biometricPrompt.cancelAuthentication()
                            } catch (e2: Exception) {
                                Log.e(TAG, "Second cancel failed", e2)
                            }
                            
                            // Force completion after additional delay
                            timeoutHandler.postDelayed({
                                if (!isCompleted) {
                                    Log.d(TAG, "🚫 Force completing stuck dialog")
                                    isCompleted = true
                                    clearFingerprintRetryAttempted(activity)
                                    onError("Face ID authentication required. Dialog was stuck - please restart the app if issue persists.")
                                }
                            }, 300)
                        }
                    }, 200)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to cancel forced biometric prompt", e)
                    if (!isCompleted) {
                        isCompleted = true
                        clearFingerprintRetryAttempted(activity)
                        onError("Face ID required but system stuck. Please check Face ID settings.")
                    }
                }
            } else {
                Log.d(TAG, "⚠️ Forced timeout fired but authentication already completed")
            }
        }
        
        try {
            Log.d(TAG, "🎯 Launching FORCED Face ID prompt with 6-second timeout...")
            biometricPrompt.authenticate(promptInfo)
            
            // Start forced timeout countdown
            timeoutHandler.postDelayed(timeoutRunnable, 6000) // 6 second timeout
            
            // Progress warnings
            timeoutHandler.postDelayed({
                if (!isCompleted && !hasTimedOut) {
                    Log.d(TAG, "⚠️ 3 seconds remaining - Face ID required!")
                }
            }, 3000)
            
        } catch (e: Exception) {
            timeoutHandler.removeCallbacksAndMessages(null)
            Log.e(TAG, "Failed to launch FORCED Face ID prompt", e)
            clearFingerprintRetryAttempted(activity)
            onError("Failed to start forced Face ID authentication: ${e.message}")
        }
    }
    
    // Simple retry tracking to prevent infinite loops
    private fun hasFingerprintRetryAttempted(activity: FragmentActivity): Boolean {
        return activity.intent?.getBooleanExtra("fingerprint_retry_attempted", false) ?: false
    }
    
    private fun markFingerprintRetryAttempted(activity: FragmentActivity) {
        activity.intent?.putExtra("fingerprint_retry_attempted", true)
    }
    
    private fun clearFingerprintRetryAttempted(activity: FragmentActivity) {
        activity.intent?.putExtra("fingerprint_retry_attempted", false)
    }
    
    /**
     * Attempt to activate Face ID immediately with quick timeout strategy
     * This method tries to force Face ID to activate by timing out fingerprint quickly
     */
    private fun authenticateWithFaceIdActivation(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🎯 ACTIVATION: Starting Face ID with quick fingerprint timeout...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        // First attempt: Aggressive messaging with manual timeout
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("🔥 FACE ID ACTIVATING...")
            .setSubtitle("LOOK AT CAMERA NOW!")
            .setDescription("Face ID auto-cancels in 4 seconds if not used. Do NOT touch fingerprint sensor.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
        
        var hasTimedOut = false
        var isCompleted = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (isCompleted) return
                    isCompleted = true
                    
                    Log.d(TAG, "✅ ACTIVATION: Face ID authentication succeeded!")
                    Log.d(TAG, "Authentication method: ${result.authenticationType}")
                    timeoutHandler.removeCallbacksAndMessages(null) // Cancel timeout
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (isCompleted) return
                    isCompleted = true
                    
                    Log.e(TAG, "❌ ACTIVATION: Face ID error: Code=$errorCode, Message=$errString")
                    timeoutHandler.removeCallbacksAndMessages(null) // Cancel timeout
                    
                    // Check if this was our manual timeout cancellation (Code=5 = ERROR_CANCELED)
                    if ((errorCode == BiometricPrompt.ERROR_CANCELED || errorCode == BiometricPrompt.ERROR_USER_CANCELED) && hasTimedOut) {
                        Log.d(TAG, "🔄 Manual timeout triggered (Code=$errorCode) - retrying with forced Face ID...")
                        // Reset completion flag for retry
                        isCompleted = false
                        authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                        return
                    }
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            if (!hasTimedOut) {
                                onUserCancel?.invoke() ?: onError("Authentication canceled")
                            } else {
                                // This is likely our timeout - retry
                                Log.d(TAG, "🔄 Timeout cancel detected - forcing Face ID retry...")
                                isCompleted = false
                                authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                            }
                        }
                        BiometricPrompt.ERROR_CANCELED -> {
                            // This is our manual cancellation - retry
                            Log.d(TAG, "🔄 Manual cancel detected - forcing Face ID retry...")
                            isCompleted = false
                            authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                        }
                        BiometricPrompt.ERROR_TIMEOUT -> {
                            Log.d(TAG, "⏰ System timeout - retrying with forced Face ID...")
                            isCompleted = false
                            authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                        }
                        else -> {
                            onError("Face ID authentication failed: $errString")
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ ACTIVATION: Authentication failed - user can retry")
                    // Don't complete - let them try again
                }
            })
        
        // Manual timeout mechanism - cancel the prompt if Face ID isn't used
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                hasTimedOut = true
                Log.d(TAG, "⏰ MANUAL TIMEOUT: 4 seconds elapsed - canceling to force Face ID retry...")
                Log.d(TAG, "🔧 Canceling biometric prompt to trigger retry...")
                try {
                    biometricPrompt.cancelAuthentication()
                    Log.d(TAG, "✅ Biometric prompt canceled successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to cancel biometric prompt", e)
                    if (!isCompleted) {
                        isCompleted = true
                        Log.d(TAG, "🔄 Direct retry due to cancel failure...")
                        authenticateFaceIdForced(activity, onSuccess, onError, onUserCancel)
                    }
                }
            } else {
                Log.d(TAG, "⚠️ Timeout fired but authentication already completed")
            }
        }
        
        try {
            Log.d(TAG, "🚀 Launching Face ID activation prompt with 4-second manual timeout...")
            Log.d(TAG, "📱 Expected behavior: Dialog shows for 4 seconds, then auto-cancels if Face ID not used")
            biometricPrompt.authenticate(promptInfo)
            
            // Start manual timeout countdown
            timeoutHandler.postDelayed(timeoutRunnable, 4000) // 4 second timeout
            
            // Progress indicator for user
            timeoutHandler.postDelayed({
                if (!isCompleted && !hasTimedOut) {
                    Log.d(TAG, "💡 2 seconds remaining - guiding user to Face ID...")
                }
            }, 2000) // 2 second progress update
            
        } catch (e: Exception) {
            timeoutHandler.removeCallbacksAndMessages(null)
            Log.e(TAG, "Failed to launch Face ID activation prompt", e)
            onError("Failed to start Face ID activation: ${e.message}")
        }
    }
    
    /**
     * Convenience method for simple authentication with just success/failure
     */
    fun authenticateSimple(
        activity: FragmentActivity,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        authenticate(
            activity = activity,
            onSuccess = { onResult(true, null) },
            onError = { error -> onResult(false, error) },
            onUserCancel = { onResult(false, "User canceled authentication") }
        )
    }
    
    /**
     * Face ID authentication with clear user guidance about Android limitations
     * Acknowledges that Android prioritizes fingerprint but guides user to Face ID
     */
    fun authenticateFaceIdOnly(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🔥 ADVANCED: Starting Face ID with user guidance approach...")
        
        val biometricType = getPreferredBiometricType(activity)
        Log.d(TAG, "🔍 Device analysis - Biometric type: $biometricType")
        
        when (biometricType) {
            BiometricType.FACE_ONLY -> {
                Log.d(TAG, "✅ Device has Face ID only - proceeding normally")
                authenticateSimpleFaceId(activity, onSuccess, onError, onUserCancel)
            }
            BiometricType.FINGERPRINT_ONLY -> {
                Log.w(TAG, "⚠️ Device has fingerprint only - no Face ID available")
                onError("This device does not support Face ID. Face authentication required.")
            }
            BiometricType.BOTH_AVAILABLE -> {
                Log.d(TAG, "🎯 Device has both - using Class 3 biometric security...")
                authenticateWithClass3Biometrics(activity, onSuccess, onError, onUserCancel)
            }
            BiometricType.NONE_AVAILABLE -> {
                Log.e(TAG, "❌ No biometric hardware available")
                onError("No biometric authentication available on this device.")
            }
        }
    }
    
    /**
     * Simple Face ID authentication for devices with only Face ID
     */
    private fun authenticateSimpleFaceId(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "😊 Simple Face ID authentication (Face ID only device)...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Use Biometrics to Unlock")
            .setSubtitle("Biometric authentication required")
            .setDescription("Authenticate using your biometric credentials to proceed.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "✅ Simple Face ID succeeded!")
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "❌ Simple Face ID error: Code=$errorCode")
                    onError("Face ID authentication failed: $errString")
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ Simple Face ID failed - retry available")
                }
            })
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Authenticate using Class 3 biometric security (BIOMETRIC_STRONG)
     * Provides clean, professional prompt for highest security level
     */
    private fun authenticateWithClass3Biometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "📱 Starting Class 3 biometric authentication (BIOMETRIC_STRONG)...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        // Clean, professional biometric prompt with Class 3 security
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Use Biometrics to Unlock")
            .setSubtitle("Biometric authentication required")
            .setDescription("Authenticate using your biometric credentials to proceed.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "✅ Class 3 biometric authentication succeeded!")
                    Log.d(TAG, "✅ Authentication type: ${result.authenticationType}")
                    
                    // Check if this was actually biometric (Class 3 security)
                    if (result.authenticationType == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                        Log.d(TAG, "✅ Confirmed: Strong biometric authentication used (Class 3)")
                        onSuccess()
                    } else {
                        Log.w(TAG, "⚠️ Non-biometric authentication used")
                        onSuccess() // Still proceed but log the difference
                    }
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "❌ Class 3 biometric authentication error: Code=$errorCode, Message=$errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            onUserCancel?.invoke() ?: onError("Authentication canceled")
                        }
                        else -> {
                            onError("Authentication failed: $errString")
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ Class 3 biometric authentication failed - retry available")
                    // Authentication attempt failed - allow system to retry
                }
            })
        
        try {
            Log.d(TAG, "🚀 Launching Class 3 biometric prompt...")
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Class 3 biometric authentication", e)
            onError("Failed to start authentication: ${e.message}")
        }
    }
    
    /**
     * Use Android's passive face recognition without explicit user confirmation
     * Based on Android documentation for face/iris recognition
     */
    private fun authenticatePassiveFaceId(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "👁️ Starting passive Face ID authentication...")
        
        // Try system-level BiometricPrompt for better control
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                authenticateWithSystemBiometricPrompt(activity, onSuccess, onError, onUserCancel)
                return
            } catch (e: Exception) {
                Log.w(TAG, "System BiometricPrompt failed, falling back to androidx: ${e.message}")
            }
        }
        
        // Fallback to androidx BiometricPrompt
        authenticateWithAndroidXBiometricPrompt(activity, onSuccess, onError, onUserCancel)
    }
    
    /**
     * Try using system-level BiometricPrompt for more direct control
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun authenticateWithSystemBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🔧 Attempting system-level BiometricPrompt for Face ID priority...")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ has more granular control
            val authenticators = Authenticators.BIOMETRIC_STRONG
            
            val promptInfo = SystemBiometricPrompt.Builder(activity)
                .setTitle("Face ID Authentication")
                .setSubtitle("Look at the front camera")
                .setDescription("Face recognition will happen automatically")
                .setAllowedAuthenticators(authenticators)
                .setConfirmationRequired(false)
                .build()
            
            Log.d(TAG, "🚀 Launching system BiometricPrompt with Face ID preference...")
            
            promptInfo.authenticate(
                android.os.CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                object : SystemBiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: SystemBiometricPrompt.AuthenticationResult?) {
                        Log.d(TAG, "✅ System Face ID authentication succeeded!")
                        onSuccess()
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        Log.e(TAG, "❌ System Face ID error: Code=$errorCode, Message=$errString")
                        when (errorCode) {
                            SystemBiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> {
                                onUserCancel?.invoke() ?: onError("Authentication canceled")
                            }
                            else -> {
                                onError("Face ID authentication failed: $errString")
                            }
                        }
                    }
                    
                    override fun onAuthenticationFailed() {
                        Log.w(TAG, "⚠️ System Face ID failed - retry available")
                    }
                }
            )
        } else {
            throw Exception("System BiometricPrompt not fully supported on this Android version")
        }
    }
    
    /**
     * Fallback using androidx BiometricPrompt with enhanced settings
     */
    private fun authenticateWithAndroidXBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserCancel: (() -> Unit)? = null
    ) {
        Log.d(TAG, "🔄 Using androidx BiometricPrompt with enhanced Face ID settings...")
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        // More explicit Face ID messaging to guide user behavior
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("🔒 Face ID Required")
            .setSubtitle("Look directly at the front camera")
            .setDescription("IMPORTANT: Do NOT use fingerprint. Face recognition will activate automatically when you look at the camera.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false) // Passive face recognition
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "✅ AndroidX Face ID authentication succeeded!")
                    Log.d(TAG, "✅ Authentication type: ${result.authenticationType}")
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "❌ AndroidX Face ID error: Code=$errorCode, Message=$errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            Log.d(TAG, "User canceled Face ID authentication")
                            onUserCancel?.invoke() ?: onError("Authentication canceled")
                        }
                        BiometricPrompt.ERROR_TIMEOUT -> {
                            onError("Face ID timeout. Please ensure your face is visible to the camera and try again.")
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            onError("No Face ID enrolled. Please set up Face ID in device settings.")
                        }
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                            onError("Face ID hardware not available on this device.")
                        }
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                            onError("Face ID hardware is currently unavailable.")
                        }
                        else -> {
                            onError("Face ID authentication failed: $errString")
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    Log.w(TAG, "⚠️ AndroidX Face ID failed - user can retry")
                    // Don't error immediately - allow retry for face recognition
                }
            })
        
        try {
            Log.d(TAG, "🚀 Launching AndroidX Face ID authentication...")
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch AndroidX Face ID", e)
            onError("Failed to start Face ID authentication: ${e.message}")
        }
    }
}

/**
 * Enum representing different biometric availability states
 */
enum class BiometricStatus {
    Available,
    NoHardware,
    HardwareUnavailable,
    NoneEnrolled,
    SecurityUpdateRequired,
    Unsupported,
    Unknown,
    NotAvailable
}

/**
 * Enum representing different biometric hardware types
 */
enum class BiometricType {
    FACE_ONLY,
    FINGERPRINT_ONLY,
    BOTH_AVAILABLE,
    NONE_AVAILABLE
}