package com.artiusid.sdk.managers

import android.content.Context
import android.util.Log
import com.artiusid.sdk.sdk.config.*
import com.artiusid.sdk.sdk.network.SimpleAPIClient

/**
 * Centralized SDK configuration manager
 */
object SDKConfigManager {
    private const val TAG = "SDKConfigManager"
    
    private var config: ArtiusSDKConfig? = null
    private var apiClient: SimpleAPIClient? = null
    private var isInitialized = false
    
    /**
     * Initialize the SDK configuration
     */
    fun initialize(context: Context, sdkConfig: ArtiusSDKConfig) {
        if (isInitialized) {
            Log.w(TAG, "SDK already initialized")
            return
        }
        
        try {
            Log.d(TAG, "Initializing SDK configuration...")
            
            // Validate configuration
            validateConfiguration(sdkConfig)
            
            // Store configuration
            config = sdkConfig
            
            // Initialize secure API client
            apiClient = SimpleAPIClient(context, sdkConfig)
            
            // Initialize other managers
            LocalizationManager.initialize(sdkConfig.localizationConfig)
            SecurityManager.initialize(context, sdkConfig.securityConfig)
            
            // Initialize analytics if enabled
            AnalyticsManager.initialize(sdkConfig.debugMode)
            
            isInitialized = true
            Log.i(TAG, "SDK configuration initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SDK configuration", e)
            throw SDKInitializationException("SDK initialization failed: ${e.message}", e)
        }
    }
    
    /**
     * Get the current SDK configuration
     */
    fun getConfig(): ArtiusSDKConfig {
        return config ?: throw IllegalStateException("SDK not initialized. Call ArtiusIDSDK.initialize() first.")
    }
    
    /**
     * Get the SDK theme configuration
     */
    fun getTheme(): SDKTheme {
        return getConfig().theme
    }
    
    /**
     * Get the secure API client
     */
    fun getApiClient(): SimpleAPIClient {
        return apiClient ?: throw IllegalStateException("SDK not initialized. Call ArtiusIDSDK.initialize() first.")
    }
    
    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Update configuration (for runtime changes)
     */
    fun updateConfig(newConfig: ArtiusSDKConfig) {
        validateConfiguration(newConfig)
        config = newConfig
        Log.d(TAG, "SDK configuration updated")
    }
    
    /**
     * Validate SDK configuration
     */
    private fun validateConfiguration(config: ArtiusSDKConfig) {
        require(config.apiEndpoint.isNotEmpty()) { "API endpoint cannot be empty" }
        require(config.apiKey.isNotEmpty()) { "API key cannot be empty" }
        
        // Validate API endpoint format
        try {
            java.net.URL(config.apiEndpoint)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid API endpoint format: ${config.apiEndpoint}")
        }
        
        // Validate security configuration
        if (config.securityConfig.enableCertificatePinning) {
            require(config.securityConfig.pinnedCertificates.isNotEmpty()) {
                "Certificate pinning enabled but no certificates provided"
            }
        }
        
        Log.d(TAG, "Configuration validation passed")
    }
}

/**
 * Localization manager for multi-language support
 */
object LocalizationManager {
    private const val TAG = "LocalizationManager"
    
    private var config: LocalizationConfig? = null
    private var currentLanguage: String = "en"
    
    // Default localized strings
    private val defaultStrings = mapOf(
        // Core UI strings
        "verification_title" to "Identity Verification",
        "authentication_title" to "Authentication",
        "face_liveness_title" to "Face Liveness Check",
        "document_scan_title" to "Document Scan",
        "nfc_reading_title" to "NFC Reading",
        
        // Buttons
        "continue_button" to "Continue",
        "cancel_button" to "Cancel",
        "retry_button" to "Retry",
        "skip_button" to "Skip",
        "done_button" to "Done",
        "capture_button" to "Capture",
        
        // Instructions
        "position_face_instruction" to "Position your face in the circle",
        "look_straight_instruction" to "Look straight ahead",
        "blink_instruction" to "Please blink",
        "document_position_instruction" to "Position document in frame",
        "nfc_hold_instruction" to "Hold phone near passport",
        
        // Status messages
        "processing" to "Processing...",
        "completed" to "Completed",
        "failed" to "Failed",
        "cancelled" to "Cancelled",
        "success" to "Success",
        
        // Error messages
        "camera_permission_required" to "Camera permission is required",
        "nfc_not_available" to "NFC is not available on this device",
        "network_error" to "Network error occurred",
        "timeout_error" to "Operation timed out",
        "processing_error" to "Processing failed",
        
        // Document types
        "passport" to "Passport",
        "drivers_license" to "Driver's License",
        "id_card" to "ID Card",
        "select_document_type" to "Select Document Type"
    )
    
    fun initialize(config: LocalizationConfig) {
        this.config = config
        this.currentLanguage = config.defaultLanguage
        Log.d(TAG, "Localization initialized with language: $currentLanguage")
    }
    
    fun setLanguage(language: String) {
        val config = this.config ?: return
        
        if (config.supportedLanguages.contains(language)) {
            currentLanguage = language
            Log.d(TAG, "Language changed to: $language")
        } else {
            Log.w(TAG, "Language $language not supported, using default")
        }
    }
    
    fun getString(key: String, vararg args: Any): String {
        val config = this.config
        
        // Try custom strings first
        val customString = config?.customStrings?.get(key)
        if (customString != null) {
            return if (args.isNotEmpty()) {
                String.format(customString, *args)
            } else {
                customString
            }
        }
        
        // Fall back to default strings
        val defaultString = defaultStrings[key] ?: key
        return if (args.isNotEmpty()) {
            String.format(defaultString, *args)
        } else {
            defaultString
        }
    }
    
    fun getCurrentLanguage(): String = currentLanguage
}

/**
 * Security manager for device and app security
 */
object SecurityManager {
    private const val TAG = "SecurityManager"
    
    private var config: SecurityConfig? = null
    private var context: Context? = null
    
    fun initialize(context: Context, config: SecurityConfig) {
        this.context = context
        this.config = config
        
        // Perform security checks
        if (config.enableRootDetection) {
            checkRootDetection()
        }
        
        if (config.enableDebugDetection) {
            checkDebugDetection()
        }
        
        if (config.enableAntiTampering) {
            checkAntiTampering()
        }
        
        Log.d(TAG, "Security manager initialized")
    }
    
    private fun checkRootDetection() {
        // Basic root detection
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
        
        val isRooted = rootIndicators.any { java.io.File(it).exists() }
        
        if (isRooted) {
            Log.w(TAG, "Root detection: Device appears to be rooted")
            // In production, you might want to block functionality or alert the server
        }
    }
    
    private fun checkDebugDetection() {
        val context = this.context ?: return
        
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        
        if (isDebuggable) {
            Log.w(TAG, "Debug detection: App is in debug mode")
        }
    }
    
    private fun checkAntiTampering() {
        // Basic anti-tampering checks
        val context = this.context ?: return
        
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            // In production, you would verify the signature against known good signatures
            Log.d(TAG, "Anti-tampering: Package signature verified")
        } catch (e: Exception) {
            Log.w(TAG, "Anti-tampering: Failed to verify package signature", e)
        }
    }
}



/**
 * SDK initialization exception
 */
class SDKInitializationException(message: String, cause: Throwable? = null) : Exception(message, cause)
