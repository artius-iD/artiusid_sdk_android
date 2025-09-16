package com.artiusid.sdk.config

import androidx.compose.ui.graphics.Color

/**
 * SDK configuration class - EXACT STANDALONE MATCH
 */
data class ArtiusSDKConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.artiusid.com",
    val clientId: Int = 0,
    val clientGroupId: Int = 0,
    val enableLogging: Boolean = false,
    val enableAnalytics: Boolean = true,
    
    // Theme configuration
    val primaryColor: Color = Color(0xFF2D3748),
    val secondaryColor: Color = Color(0xFFFF6B35),
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val buttonColor: Color = Color(0xFF4CAF50),
    
    // Camera settings
    val cameraQuality: Float = 0.8f,
    val enableFaceDetection: Boolean = true,
    val enableLivenessDetection: Boolean = true,
    
    // NFC settings
    val enableNFC: Boolean = true,
    val nfcTimeout: Long = 30000L,
    
    // Network settings
    val requestTimeout: Long = 30000L,
    val retryAttempts: Int = 3,
    
    // Additional API settings
    val apiBaseUrl: String = baseUrl,
    val apiTimeout: Long = requestTimeout,
    val livenessThreshold: Float = 0.7f,
    val documentConfidenceThreshold: Float = 0.8f
)