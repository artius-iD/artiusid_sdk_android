package com.artiusid.sdk.managers

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.artiusid.sdk.config.ArtiusSDKConfig

/**
 * SDK configuration manager - EXACT STANDALONE MATCH
 */
object SDKConfigManager {
    
    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized
    
    // Theme colors
    var primaryColor: Color = Color(0xFF2D3748)
    var secondaryColor: Color = Color(0xFFFF6B35)
    var backgroundColor: Color = Color.White
    var textColor: Color = Color.Black
    
    // Button styles
    enum class ButtonStyle {
        FILLED, OUTLINED, TEXT
    }
    
    // Progress styles
    enum class ProgressStyle {
        CIRCULAR, LINEAR, CUSTOM
    }
    
    var buttonStyle: ButtonStyle = ButtonStyle.FILLED
    var progressStyle: ProgressStyle = ProgressStyle.CIRCULAR
    
    // API configuration
    var apiBaseUrl: String = "https://api.artiusid.com"
    var apiTimeout: Long = 30000L
    
    // Camera settings
    var cameraQuality: Float = 0.8f
    var enableFlash: Boolean = false
    
    // Verification settings
    var livenessThreshold: Float = 0.7f
    var documentConfidenceThreshold: Float = 0.8f
    
    fun initialize(context: Context, config: ArtiusSDKConfig) {
        // Apply configuration settings
        primaryColor = config.primaryColor
        secondaryColor = config.secondaryColor
        backgroundColor = config.backgroundColor
        textColor = config.textColor
        apiBaseUrl = config.apiBaseUrl
        apiTimeout = config.apiTimeout
        livenessThreshold = config.livenessThreshold
        documentConfidenceThreshold = config.documentConfidenceThreshold
        
        _isInitialized = true
    }
    
    fun trackVerificationStarted() {
        // Track verification started event
    }
    
    fun trackAuthenticationStarted() {
        // Track authentication started event
    }
    
    fun getConfig(): ArtiusSDKConfig {
        return ArtiusSDKConfig(
            apiKey = "default_api_key", // Default value
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            backgroundColor = backgroundColor,
            textColor = textColor,
            apiBaseUrl = apiBaseUrl,
            apiTimeout = apiTimeout,
            livenessThreshold = livenessThreshold,
            documentConfidenceThreshold = documentConfidenceThreshold
        )
    }
    
    fun getTheme(): com.artiusid.sdk.ui.theme.SDKTheme {
        return com.artiusid.sdk.ui.theme.SDKTheme(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            backgroundColor = backgroundColor,
            textColor = textColor,
            textSecondaryColor = Color(0xFF6B7280),
            buttonStyle = buttonStyle.name,
            progressStyle = progressStyle.name,
            darkModeSupport = true,
            errorColor = Color(0xFFD32F2F),
            surfaceColor = Color(0xFFF5F5F5),
            fontFamily = null, // No custom font resource
            cornerRadius = 8.0f
        )
    }
    
    fun reset() {
        primaryColor = Color(0xFF2D3748)
        secondaryColor = Color(0xFFFF6B35)
        backgroundColor = Color.White
        textColor = Color.Black
        buttonStyle = ButtonStyle.FILLED
        progressStyle = ProgressStyle.CIRCULAR
        _isInitialized = false
    }
}