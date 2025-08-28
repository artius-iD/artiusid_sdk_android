package com.artiusid.sdk.config

import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Environment configuration for the SDK
 */
enum class Environment {
    DEVELOPMENT,
    STAGING, 
    PRODUCTION
}

/**
 * SDK Theme configuration for customizing UI appearance
 */
data class SDKTheme(
    val primaryColor: Color = Color(0xFF6200EE),
    val secondaryColor: Color = Color(0xFF03DAC5),
    val backgroundColor: Color = Color.White,
    val surfaceColor: Color = Color(0xFFF5F5F5),
    val textColor: Color = Color.Black,
    val textSecondaryColor: Color = Color.Gray,
    val errorColor: Color = Color.Red,
    val successColor: Color = Color.Green,
    val warningColor: Color = Color(0xFFFF9800),
    @DrawableRes val logo: Int? = null,
    @FontRes val fontFamily: Int? = null,
    val cornerRadius: Dp = 8.dp,
    val buttonStyle: ButtonStyle = ButtonStyle.FILLED,
    val progressIndicatorStyle: ProgressStyle = ProgressStyle.CIRCULAR,
    val enableAnimations: Boolean = true,
    val darkModeSupport: Boolean = true
) {
    class Builder {
        private var primaryColor: Color = Color(0xFF6200EE)
        private var secondaryColor: Color = Color(0xFF03DAC5)
        private var backgroundColor: Color = Color.White
        private var surfaceColor: Color = Color(0xFFF5F5F5)
        private var textColor: Color = Color.Black
        private var textSecondaryColor: Color = Color.Gray
        private var errorColor: Color = Color.Red
        private var successColor: Color = Color.Green
        private var warningColor: Color = Color(0xFFFF9800)
        private var logo: Int? = null
        private var fontFamily: Int? = null
        private var cornerRadius: Dp = 8.dp
        private var buttonStyle: ButtonStyle = ButtonStyle.FILLED
        private var progressIndicatorStyle: ProgressStyle = ProgressStyle.CIRCULAR
        private var enableAnimations: Boolean = true
        private var darkModeSupport: Boolean = true
        
        fun setPrimaryColor(color: Color) = apply { this.primaryColor = color }
        fun setSecondaryColor(color: Color) = apply { this.secondaryColor = color }
        fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
        fun setSurfaceColor(color: Color) = apply { this.surfaceColor = color }
        fun setTextColor(color: Color) = apply { this.textColor = color }
        fun setTextSecondaryColor(color: Color) = apply { this.textSecondaryColor = color }
        fun setErrorColor(color: Color) = apply { this.errorColor = color }
        fun setSuccessColor(color: Color) = apply { this.successColor = color }
        fun setWarningColor(color: Color) = apply { this.warningColor = color }
        fun setLogo(@DrawableRes logo: Int?) = apply { this.logo = logo }
        fun setFontFamily(@FontRes fontFamily: Int?) = apply { this.fontFamily = fontFamily }
        fun setCornerRadius(radius: Dp) = apply { this.cornerRadius = radius }
        fun setButtonStyle(style: ButtonStyle) = apply { this.buttonStyle = style }
        fun setProgressIndicatorStyle(style: ProgressStyle) = apply { this.progressIndicatorStyle = style }
        fun setEnableAnimations(enable: Boolean) = apply { this.enableAnimations = enable }
        fun setDarkModeSupport(support: Boolean) = apply { this.darkModeSupport = support }
        
        fun build() = SDKTheme(
            primaryColor, secondaryColor, backgroundColor, surfaceColor,
            textColor, textSecondaryColor, errorColor, successColor, warningColor,
            logo, fontFamily, cornerRadius, buttonStyle, progressIndicatorStyle,
            enableAnimations, darkModeSupport
        )
    }
}

/**
 * Button style options
 */
enum class ButtonStyle {
    FILLED,
    OUTLINED,
    TEXT
}

/**
 * Progress indicator style options
 */
enum class ProgressStyle {
    CIRCULAR,
    LINEAR,
    CUSTOM
}

/**
 * Security configuration for the SDK
 */
data class SecurityConfig(
    val enableCertificatePinning: Boolean = true,
    val pinnedCertificates: List<String> = emptyList(),
    val tlsVersion: String = "1.3",
    val enableNetworkSecurityConfig: Boolean = true,
    val enableAntiTampering: Boolean = true,
    val enableRootDetection: Boolean = true,
    val enableDebugDetection: Boolean = true,
    val obfuscationLevel: ObfuscationLevel = ObfuscationLevel.HIGH
) {
    class Builder {
        private var enableCertificatePinning: Boolean = true
        private var pinnedCertificates: List<String> = emptyList()
        private var tlsVersion: String = "1.3"
        private var enableNetworkSecurityConfig: Boolean = true
        private var enableAntiTampering: Boolean = true
        private var enableRootDetection: Boolean = true
        private var enableDebugDetection: Boolean = true
        private var obfuscationLevel: ObfuscationLevel = ObfuscationLevel.HIGH
        
        fun enableCertificatePinning(enable: Boolean) = apply { this.enableCertificatePinning = enable }
        fun setPinnedCertificates(certificates: List<String>) = apply { this.pinnedCertificates = certificates }
        fun setTLSVersion(version: String) = apply { this.tlsVersion = version }
        fun enableNetworkSecurityConfig(enable: Boolean) = apply { this.enableNetworkSecurityConfig = enable }
        fun enableAntiTampering(enable: Boolean) = apply { this.enableAntiTampering = enable }
        fun enableRootDetection(enable: Boolean) = apply { this.enableRootDetection = enable }
        fun enableDebugDetection(enable: Boolean) = apply { this.enableDebugDetection = enable }
        fun setObfuscationLevel(level: ObfuscationLevel) = apply { this.obfuscationLevel = level }
        
        fun build() = SecurityConfig(
            enableCertificatePinning, pinnedCertificates, tlsVersion,
            enableNetworkSecurityConfig, enableAntiTampering, enableRootDetection,
            enableDebugDetection, obfuscationLevel
        )
    }
}

/**
 * Obfuscation level for security
 */
enum class ObfuscationLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Localization configuration
 */
data class LocalizationConfig(
    val defaultLanguage: String = "en",
    val supportedLanguages: List<String> = listOf("en", "es", "fr", "de"),
    val customStrings: Map<String, String> = emptyMap(),
    val enableRTL: Boolean = false
) {
    class Builder {
        private var defaultLanguage: String = "en"
        private var supportedLanguages: List<String> = listOf("en", "es", "fr", "de")
        private var customStrings: Map<String, String> = emptyMap()
        private var enableRTL: Boolean = false
        
        fun setDefaultLanguage(language: String) = apply { this.defaultLanguage = language }
        fun setSupportedLanguages(languages: List<String>) = apply { this.supportedLanguages = languages }
        fun setCustomStrings(strings: Map<String, String>) = apply { this.customStrings = strings }
        fun enableRTL(enable: Boolean) = apply { this.enableRTL = enable }
        
        fun build() = LocalizationConfig(defaultLanguage, supportedLanguages, customStrings, enableRTL)
    }
}

/**
 * Verification flow configuration
 */
data class VerificationFlowConfig(
    val enableFaceLiveness: Boolean = true,
    val enableDocumentScan: Boolean = true,
    val enableNFC: Boolean = true,
    val enableBarcodeScanning: Boolean = true,
    val documentTypes: List<String> = listOf("PASSPORT", "DRIVERS_LICENSE", "ID_CARD"),
    val timeoutMs: Long = 300000L, // 5 minutes
    val maxRetries: Int = 3,
    val qualityThreshold: Float = 0.8f,
    val enableQualityChecks: Boolean = true
) {
    class Builder {
        private var enableFaceLiveness: Boolean = true
        private var enableDocumentScan: Boolean = true
        private var enableNFC: Boolean = true
        private var enableBarcodeScanning: Boolean = true
        private var documentTypes: List<String> = listOf("PASSPORT", "DRIVERS_LICENSE", "ID_CARD")
        private var timeoutMs: Long = 300000L
        private var maxRetries: Int = 3
        private var qualityThreshold: Float = 0.8f
        private var enableQualityChecks: Boolean = true
        
        fun enableFaceLiveness(enable: Boolean) = apply { this.enableFaceLiveness = enable }
        fun enableDocumentScan(enable: Boolean) = apply { this.enableDocumentScan = enable }
        fun enableNFC(enable: Boolean) = apply { this.enableNFC = enable }
        fun enableBarcodeScanning(enable: Boolean) = apply { this.enableBarcodeScanning = enable }
        fun setDocumentTypes(types: List<String>) = apply { this.documentTypes = types }
        fun setTimeout(timeout: Long) = apply { this.timeoutMs = timeout }
        fun setMaxRetries(retries: Int) = apply { this.maxRetries = retries }
        fun setQualityThreshold(threshold: Float) = apply { this.qualityThreshold = threshold }
        fun enableQualityChecks(enable: Boolean) = apply { this.enableQualityChecks = enable }
        
        fun build() = VerificationFlowConfig(
            enableFaceLiveness, enableDocumentScan, enableNFC, enableBarcodeScanning,
            documentTypes, timeoutMs, maxRetries, qualityThreshold, enableQualityChecks
        )
    }
}

/**
 * Authentication flow configuration
 */
data class AuthenticationFlowConfig(
    val enableBiometric: Boolean = true,
    val enableFaceRecognition: Boolean = true,
    val enableFingerprint: Boolean = true,
    val enableDeviceBinding: Boolean = true,
    val sessionTimeout: Long = 3600000L, // 1 hour
    val maxAuthAttempts: Int = 3
) {
    class Builder {
        private var enableBiometric: Boolean = true
        private var enableFaceRecognition: Boolean = true
        private var enableFingerprint: Boolean = true
        private var enableDeviceBinding: Boolean = true
        private var sessionTimeout: Long = 3600000L
        private var maxAuthAttempts: Int = 3
        
        fun enableBiometric(enable: Boolean) = apply { this.enableBiometric = enable }
        fun enableFaceRecognition(enable: Boolean) = apply { this.enableFaceRecognition = enable }
        fun enableFingerprint(enable: Boolean) = apply { this.enableFingerprint = enable }
        fun enableDeviceBinding(enable: Boolean) = apply { this.enableDeviceBinding = enable }
        fun setSessionTimeout(timeout: Long) = apply { this.sessionTimeout = timeout }
        fun setMaxAuthAttempts(attempts: Int) = apply { this.maxAuthAttempts = attempts }
        
        fun build() = AuthenticationFlowConfig(
            enableBiometric, enableFaceRecognition, enableFingerprint,
            enableDeviceBinding, sessionTimeout, maxAuthAttempts
        )
    }
}

/**
 * Main SDK configuration class
 */
data class ArtiusSDKConfig(
    val apiEndpoint: String,
    val apiKey: String,
    val environment: Environment = Environment.PRODUCTION,
    val theme: SDKTheme = SDKTheme(),
    val securityConfig: SecurityConfig = SecurityConfig(),
    val localizationConfig: LocalizationConfig = LocalizationConfig(),
    val verificationFlowConfig: VerificationFlowConfig = VerificationFlowConfig(),
    val authenticationFlowConfig: AuthenticationFlowConfig = AuthenticationFlowConfig(),
    val firebaseTokenProvider: (suspend () -> String)? = null,
    val debugMode: Boolean = false,
    val enableAnalytics: Boolean = true,
    val enableCrashReporting: Boolean = true
) {
    class Builder {
        private var apiEndpoint: String = ""
        private var apiKey: String = ""
        private var environment: Environment = Environment.PRODUCTION
        private var theme: SDKTheme = SDKTheme()
        private var securityConfig: SecurityConfig = SecurityConfig()
        private var localizationConfig: LocalizationConfig = LocalizationConfig()
        private var verificationFlowConfig: VerificationFlowConfig = VerificationFlowConfig()
        private var authenticationFlowConfig: AuthenticationFlowConfig = AuthenticationFlowConfig()
        private var firebaseTokenProvider: (suspend () -> String)? = null
        private var debugMode: Boolean = false
        private var enableAnalytics: Boolean = true
        private var enableCrashReporting: Boolean = true
        
        fun setApiEndpoint(endpoint: String) = apply { this.apiEndpoint = endpoint }
        fun setApiKey(key: String) = apply { this.apiKey = key }
        fun setEnvironment(env: Environment) = apply { this.environment = env }
        fun setTheme(theme: SDKTheme) = apply { this.theme = theme }
        fun setSecurityConfig(config: SecurityConfig) = apply { this.securityConfig = config }
        fun setLocalizationConfig(config: LocalizationConfig) = apply { this.localizationConfig = config }
        fun setVerificationFlowConfig(config: VerificationFlowConfig) = apply { this.verificationFlowConfig = config }
        fun setAuthenticationFlowConfig(config: AuthenticationFlowConfig) = apply { this.authenticationFlowConfig = config }
        fun setFirebaseTokenProvider(provider: suspend () -> String) = apply { this.firebaseTokenProvider = provider }
        fun setDebugMode(debug: Boolean) = apply { this.debugMode = debug }
        fun enableAnalytics(enable: Boolean) = apply { this.enableAnalytics = enable }
        fun enableCrashReporting(enable: Boolean) = apply { this.enableCrashReporting = enable }
        
        fun build(): ArtiusSDKConfig {
            require(apiEndpoint.isNotEmpty()) { "API endpoint is required" }
            require(apiKey.isNotEmpty()) { "API key is required" }
            
            return ArtiusSDKConfig(
                apiEndpoint, apiKey, environment, theme, securityConfig,
                localizationConfig, verificationFlowConfig, authenticationFlowConfig,
                firebaseTokenProvider, debugMode, enableAnalytics, enableCrashReporting
            )
        }
    }
}
