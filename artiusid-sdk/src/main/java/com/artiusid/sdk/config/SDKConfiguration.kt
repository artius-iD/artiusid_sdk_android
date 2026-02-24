/*
 * File: SDKConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.artiusid.sdk.models.SDKImageOverrides

/**
 * Log level for SDK output (iOS parity: LogLevel).
 */
enum class LogLevel {
    NONE,   // No logging (iOS parity)
    VERBOSE,
    DEBUG, INFO, WARNING, ERROR
}

/**
 * SDK Configuration for the bridge to standalone application
 */
@Parcelize
data class SDKConfiguration(
    val apiKey: String,
    val baseUrl: String = "https://api.artiusid.com",
    val environment: Environment = Environment.SANDBOX,
    
    // Client identification (matches iOS AppConstants)
    val clientId: Int = 1,
    val clientGroupId: Int = 1,
    
    val enableLogging: Boolean = false,
    /** Structured log level (iOS parity). Default INFO. */
    val logLevel: LogLevel = LogLevel.INFO,
    val enableAnalytics: Boolean = true,
    val enableBiometrics: Boolean = true,
    val enableNFC: Boolean = true,
    val timeoutSeconds: Int = 30,
    /** Request timeout in seconds (iOS parity: requestTimeout). Alias for timeoutSeconds when both needed. */
    val requestTimeout: Int = 30,
    /** Enable certificate pinning for API calls (iOS parity: enableCertificatePinning). */
    val enableCertificatePinning: Boolean = false,
    /** Certificate pins (SHA-256 hashes) when enableCertificatePinning is true (iOS parity: certificatePins). */
    val certificatePins: List<String> = emptyList(),
    /** Enable face verification flow (iOS parity: enableFaceVerification). */
    val enableFaceVerification: Boolean = true,
    /** Enable document scanning flow (iOS parity: enableDocumentScanning). */
    val enableDocumentScanning: Boolean = true,
    
    // Firebase Configuration (NEW in v1.2.43)
    val handleFirebaseNotifications: Boolean = true, // Set to false to disable SDK's Firebase handling
    val customFcmToken: String? = null, // Client can provide their own FCM token
    
    // Host app context sharing for mTLS and Firebase
    val hostAppPackageName: String? = null,
    val sharedCertificateContext: Boolean = true,
    val sharedFirebaseContext: Boolean = true,
    
    // Okta ID Integration (NEW - matches iOS v2.0.12)
    val includeOktaIDInVerificationPayload: Boolean = true, // Default to true like iOS
    /** Pre-set Okta user ID (iOS parity). When set, CollectOktaID screen is skipped and this value is used in verification payload. Stored per environment. */
    val oktaUserId: String? = null,

    // Localization support
    val localizationOverrides: Map<String, String> = emptyMap(),
    // Image and GIF override support
    val imageOverrides: SDKImageOverrides = SDKImageOverrides()
) : Parcelable {

    /** Whether configuration is valid (iOS parity: isValid). */
    val isValid: Boolean
        get() = validate().isEmpty()

    /** Validate configuration; returns list of error messages (iOS parity: validate()). */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (apiKey.isBlank()) errors.add("apiKey is required")
        if (requestTimeout <= 0) errors.add("requestTimeout must be positive")
        if (timeoutSeconds <= 0) errors.add("timeoutSeconds must be positive")
        if (enableCertificatePinning && certificatePins.isEmpty()) errors.add("certificatePins required when enableCertificatePinning is true")
        return errors
    }
}

enum class Environment {
    SANDBOX,      // Sandbox environment (sandbox.mobile.artiusid.dev)
    QA,           // QA environment (iOS parity)
    DEVELOPMENT,  // Development environment (service-mobile.dev.artiusid.dev)
    STAGING,      // Staging environment (service-mobile.stage.artiusid.dev)
    PRODUCTION    // Production (mobile.artiusid.com) - iOS parity
}