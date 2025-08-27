package com.artiusid.sdk.sdk.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Main configuration class for the ArtiusID SDK
 */
@Parcelize
data class ArtiusSDKConfig(
    val apiKey: String,
    val environment: Environment = Environment.PRODUCTION,
    val brandingConfig: BrandingConfig = BrandingConfig(),
    val localizationConfig: LocalizationConfig = LocalizationConfig(),
    val verificationConfig: VerificationConfig = VerificationConfig(),
    val authenticationConfig: AuthenticationConfig = AuthenticationConfig(),
    val firebaseConfig: FirebaseConfig? = null,
    val debugMode: Boolean = false
) : Parcelable {
    
    class Builder {
        private var apiKey: String = ""
        private var environment: Environment = Environment.PRODUCTION
        private var brandingConfig: BrandingConfig = BrandingConfig()
        private var localizationConfig: LocalizationConfig = LocalizationConfig()
        private var verificationConfig: VerificationConfig = VerificationConfig()
        private var authenticationConfig: AuthenticationConfig = AuthenticationConfig()
        private var firebaseConfig: FirebaseConfig? = null
        private var debugMode: Boolean = false
        
        fun setApiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun setEnvironment(environment: Environment) = apply { this.environment = environment }
        fun setBrandingConfig(config: BrandingConfig) = apply { this.brandingConfig = config }
        fun setLocalizationConfig(config: LocalizationConfig) = apply { this.localizationConfig = config }
        fun setVerificationConfig(config: VerificationConfig) = apply { this.verificationConfig = config }
        fun setAuthenticationConfig(config: AuthenticationConfig) = apply { this.authenticationConfig = config }
        fun setFirebaseConfig(config: FirebaseConfig) = apply { this.firebaseConfig = config }
        fun setDebugMode(enabled: Boolean) = apply { this.debugMode = enabled }
        
        fun build(): ArtiusSDKConfig {
            require(apiKey.isNotBlank()) { "API key is required" }
            
            return ArtiusSDKConfig(
                apiKey = apiKey,
                environment = environment,
                brandingConfig = brandingConfig,
                localizationConfig = localizationConfig,
                verificationConfig = verificationConfig,
                authenticationConfig = authenticationConfig,
                firebaseConfig = firebaseConfig,
                debugMode = debugMode
            )
        }
    }
}

/**
 * SDK Environment configuration
 */
enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

/**
 * Branding configuration for customizing SDK appearance
 */
@Parcelize
data class BrandingConfig(
    val colorScheme: ColorScheme = ColorScheme.LIGHT,
    val companyLogo: String? = null,
    val companyName: String = "Your Company",
    val primaryFont: String? = null,
    val secondaryFont: String? = null,
    val customIcons: Map<String, String> = emptyMap(),
    val customImages: Map<String, String> = emptyMap()
) : Parcelable

/**
 * Color scheme options
 */
enum class ColorScheme {
    LIGHT,
    DARK,
    CUSTOM
}

/**
 * Localization configuration
 */
@Parcelize
data class LocalizationConfig(
    val defaultLanguage: String = "en",
    val supportedLanguages: List<String> = listOf("en", "es", "fr"),
    val customStrings: Map<String, String> = emptyMap(),
    val fallbackToDefault: Boolean = true
) : Parcelable

/**
 * Verification flow configuration
 */
@Parcelize
data class VerificationConfig(
    val enableFaceVerification: Boolean = true,
    val enableDocumentScanning: Boolean = true,
    val enableNFCReading: Boolean = false,
    val livenessConfig: LivenessConfig = LivenessConfig(),
    val documentConfig: DocumentConfig = DocumentConfig(),
    val nfcConfig: NFCConfig = NFCConfig(),
    val flowTimeout: Long = 300000L,
    val allowSkipSteps: Boolean = false
) : Parcelable

/**
 * Face liveness detection configuration
 */
@Parcelize
data class LivenessConfig(
    val segmentCount: Int = 8,
    val headMovementThreshold: Float = 5.0f,
    val distanceThreshold: Float = 0.3f,
    val timeoutPerSegment: Long = 10000L,
    val requireBlink: Boolean = true,
    val qualityThreshold: Float = 0.7f,
    val enableCalibration: Boolean = true,
    val calibrationFrames: Int = 30
) : Parcelable

/**
 * Document scanning configuration
 */
@Parcelize
data class DocumentConfig(
    val supportedDocuments: List<DocumentType> = listOf(DocumentType.ID_CARD, DocumentType.PASSPORT),
    val enableAutoCapture: Boolean = true,
    val captureTimeout: Long = 30000L,
    val qualityThreshold: Float = 0.8f,
    val enableOCR: Boolean = true,
    val enableMRZParsing: Boolean = true,
    val enableBarcodeScanning: Boolean = true
) : Parcelable

/**
 * Document types supported by the SDK
 */
enum class DocumentType {
    PASSPORT,
    DRIVERS_LICENSE,
    ID_CARD,
    VISA,
    OTHER
}

/**
 * NFC reading configuration
 */
@Parcelize
data class NFCConfig(
    val readTimeout: Long = 30000L,
    val enableActiveAuthentication: Boolean = true,
    val enablePassiveAuthentication: Boolean = true,
    val requiredDataGroups: List<Int> = listOf(1, 2),
    val enableSecurityValidation: Boolean = true
) : Parcelable

/**
 * Authentication flow configuration
 */
@Parcelize
data class AuthenticationConfig(
    val enableFaceMatching: Boolean = true,
    val faceMatchingThreshold: Float = 0.8f,
    val enableLivenessCheck: Boolean = true,
    val authTimeout: Long = 60000L,
    val maxAttempts: Int = 3
) : Parcelable

/**
 * Firebase configuration for SDK
 */
@Parcelize
data class FirebaseConfig(
    val fcmToken: String? = null,
    val projectId: String? = null,
    val enableAnalytics: Boolean = false,
    val enableCrashlytics: Boolean = false,
    val customParameters: Map<String, String> = emptyMap()
) : Parcelable
