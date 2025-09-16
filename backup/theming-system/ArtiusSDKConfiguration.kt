package com.artiusid.sdk.config

import com.artiusid.sdk.theme.SDKThemeConfiguration
import com.artiusid.sdk.theme.SDKAnimationConfiguration

/**
 * Complete configuration for the ArtiusID SDK
 * 
 * This provides comprehensive customization options for integrating
 * the SDK into any application with complete brand alignment.
 */
data class ArtiusSDKConfiguration(
    // API Configuration
    val apiKey: String,
    val baseUrl: String = "https://api.artiusid.com",
    val environment: Environment = Environment.PRODUCTION,
    
    // Theme Configuration
    val theme: SDKThemeConfiguration = SDKThemeConfiguration.Default,
    val animations: SDKAnimationConfiguration = SDKAnimationConfiguration(),
    
    // Feature Configuration
    val enabledFeatures: Set<SDKFeature> = setOf(
        SDKFeature.FACE_VERIFICATION,
        SDKFeature.DOCUMENT_SCANNING,
        SDKFeature.NFC_PASSPORT_READING,
        SDKFeature.LIVENESS_DETECTION
    ),
    
    // Behavior Configuration
    val autoStartVerification: Boolean = false,
    val showIntroductionScreen: Boolean = true,
    val enableLogging: Boolean = false,
    val logLevel: LogLevel = LogLevel.ERROR,
    
    // Security Configuration
    val enableSSLPinning: Boolean = true,
    val allowScreenshots: Boolean = false,
    val sessionTimeoutMinutes: Int = 30,
    
    // Localization
    val language: String = "en",
    val customStrings: Map<String, String> = emptyMap(),
    
    // Callbacks Configuration
    val enableAnalytics: Boolean = true,
    val enableCrashReporting: Boolean = true
) {
    
    /**
     * Validation for the configuration
     */
    fun validate(): ConfigurationValidationResult {
        val errors = mutableListOf<String>()
        
        if (apiKey.isBlank()) {
            errors.add("API key cannot be blank")
        }
        
        if (baseUrl.isBlank()) {
            errors.add("Base URL cannot be blank")
        }
        
        if (enabledFeatures.isEmpty()) {
            errors.add("At least one feature must be enabled")
        }
        
        if (sessionTimeoutMinutes <= 0) {
            errors.add("Session timeout must be positive")
        }
        
        return if (errors.isEmpty()) {
            ConfigurationValidationResult.Valid
        } else {
            ConfigurationValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        /**
         * Create a basic configuration with just API key
         */
        fun basic(apiKey: String) = ArtiusSDKConfiguration(apiKey = apiKey)
        
        /**
         * Create a configuration with custom theme
         */
        fun withTheme(apiKey: String, theme: SDKThemeConfiguration) = 
            ArtiusSDKConfiguration(apiKey = apiKey, theme = theme)
        
        /**
         * Create a development configuration
         */
        fun development(apiKey: String) = ArtiusSDKConfiguration(
            apiKey = apiKey,
            environment = Environment.DEVELOPMENT,
            enableLogging = true,
            logLevel = LogLevel.DEBUG,
            allowScreenshots = true,
            enableCrashReporting = false
        )
    }
}

/**
 * Available SDK features
 */
enum class SDKFeature {
    FACE_VERIFICATION,
    DOCUMENT_SCANNING,
    NFC_PASSPORT_READING,
    LIVENESS_DETECTION,
    BIOMETRIC_MATCHING,
    DOCUMENT_AUTHENTICITY_CHECK,
    AGE_VERIFICATION,
    IDENTITY_VERIFICATION
}

/**
 * SDK environments
 */
enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

/**
 * Logging levels
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}

/**
 * Configuration validation result
 */
sealed class ConfigurationValidationResult {
    object Valid : ConfigurationValidationResult()
    data class Invalid(val errors: List<String>) : ConfigurationValidationResult()
}
