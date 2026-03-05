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
    DEBUG, INFO, WARNING, ERROR;

    /** Whether a message at this level should be shown given current max level (iOS parity: shouldShow). */
    fun shouldShow(currentMaxLevel: LogLevel): Boolean {
        if (currentMaxLevel == NONE) return false
        return ordinal >= currentMaxLevel.ordinal
    }
}

/**
 * SDK Configuration for the bridge to standalone application
 */
@Parcelize
data class SDKConfiguration(
    val apiKey: String,
    val baseUrl: String = "https://api.artiusid.com",
    val environment: Environment = Environment.SANDBOX,
    /** Optional URL template (iOS parity). e.g. "https://#env#.mobile.#domain#". #env# = sandbox|dev|stage|"". */
    val urlTemplate: String? = null,
    /** Optional mobile domain with urlTemplate (iOS parity). */
    val mobileDomain: String? = null,
    /** Optional registration URL template (iOS parity). */
    val registrationUrlTemplate: String? = null,
    /** Optional registration domain (iOS parity). */
    val registrationDomain: String? = null,
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
    
    // Host app context sharing for mTLS and Firebase (iOS: hostAppBundleIdentifier; same concept)
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
        if (clientId < 1) errors.add("clientId must be positive")
        if (clientGroupId < 1) errors.add("clientGroupId must be positive")
        if (requestTimeout <= 0) errors.add("requestTimeout must be positive")
        if (timeoutSeconds <= 0) errors.add("timeoutSeconds must be positive")
        if (enableCertificatePinning && certificatePins.isEmpty()) errors.add("certificatePins required when enableCertificatePinning is true")
        if (!handleFirebaseNotifications && customFcmToken.isNullOrBlank()) errors.add("customFcmToken required when not handling Firebase notifications")
        return errors
    }

    /** Debug description (iOS parity: debugDescription). */
    fun debugDescription(): String = buildString {
        append("SDK Configuration:\n")
        append("  - API Key: ${apiKey.take(10)}...\n")
        append("  - Environment: $environment\n")
        append("  - Client ID: $clientId, Client Group ID: $clientGroupId\n")
        append("  - Logging: ${if (enableLogging) "enabled" else "disabled"}, Log Level: $logLevel\n")
        append("  - Firebase Handling: ${if (handleFirebaseNotifications) "SDK" else "Client"}, FCM Token: ${if (customFcmToken != null) "provided" else "none"}\n")
        append("  - Timeout: ${requestTimeout}s\n")
        append("  - Certificate Pinning: ${if (enableCertificatePinning) "enabled" else "disabled"}\n")
        append("  - NFC: $enableNFC, Face: $enableFaceVerification, Document: $enableDocumentScanning, Analytics: $enableAnalytics\n")
        append("  - Localization Overrides: ${localizationOverrides.size} strings\n")
        append("  - Image Overrides: ${if (imageOverrides.hasOverrides) "configured" else "none"}\n")
        append("  - Validation: ${if (isValid) "valid" else "⚠ ${validate().size} issues"}\n")
    }

    /** Firebase-related debug info (iOS parity: getFirebaseDebugInfo). */
    fun getFirebaseDebugInfo(): String = buildString {
        append("Firebase Configuration:\n")
        append("  - SDK Handles Notifications: $handleFirebaseNotifications\n")
        append("  - Custom FCM Token: ${if (customFcmToken != null) "provided (${customFcmToken!!.length} chars)" else "not provided"}\n")
        append("  - Shared Context: $sharedFirebaseContext\n")
    }

    /** Copy with updated FCM token for client-provided token (iOS parity: mutating updateFCMToken). */
    fun copyWithFcmToken(customFcmToken: String?) = copy(customFcmToken = customFcmToken)
    /** Copy with updated logging (iOS parity: mutating updateLogging). */
    fun copyWithLogging(enableLogging: Boolean, logLevel: LogLevel = this.logLevel) = copy(enableLogging = enableLogging, logLevel = logLevel)

    companion object {
        /** Development config with logging enabled (iOS parity: development(apiKey:)). */
        @JvmStatic
        fun development(apiKey: String, baseUrl: String = "https://api.artiusid.com") = SDKConfiguration(
            apiKey = apiKey,
            baseUrl = baseUrl,
            environment = Environment.DEVELOPMENT,
            enableLogging = true,
            logLevel = LogLevel.DEBUG
        )

        /** Production config with secure defaults (iOS parity: production(apiKey:clientId:clientGroupId:)). */
        @JvmStatic
        fun production(apiKey: String, clientId: Int = 1, clientGroupId: Int = 1) = SDKConfiguration(
            apiKey = apiKey,
            environment = Environment.PRODUCTION,
            clientId = clientId,
            clientGroupId = clientGroupId,
            enableLogging = false,
            enableCertificatePinning = true
        )
    }
}

/** Extension to check if image overrides are present (for debugDescription). */
private val SDKImageOverrides.hasOverrides: Boolean
    get() = faceOverlay != null || passportOverlay != null || stateIdFrontOverlay != null ||
        brandLogo != null || customOverrides.isNotEmpty()

enum class Environment {
    SANDBOX,      // Sandbox environment (sandbox.mobile.artiusid.dev)
    QA,           // QA environment (iOS parity)
    DEVELOPMENT,  // Development environment (service-mobile.dev.artiusid.dev)
    STAGING,      // Staging environment (service-mobile.stage.artiusid.dev)
    PRODUCTION;  // Production (mobile.artiusid.com) - iOS parity

    companion object {
        /** Map view-layer name (e.g. "sandbox", "development") to Environment (iOS parity). */
        fun fromViewLayer(name: String): Environment = when (name.lowercase()) {
            "sandbox" -> SANDBOX
            "development", "dev" -> DEVELOPMENT
            "staging", "stage" -> STAGING
            "production", "prod" -> PRODUCTION
            "qa" -> QA
            else -> SANDBOX
        }
    }
}