package com.artiusid.sdk.config

/**
 * Main SDK configuration
 */
data class SDKConfig(
    val apiKey: String,
    val environment: String = "production",
    val enableLogging: Boolean = false
)

/**
 * Configuration for verification flow
 */
data class VerificationConfig(
    val enableFaceLiveness: Boolean = true,
    val enableDocumentScan: Boolean = true,
    val enableNFC: Boolean = false,
    val timeoutMs: Long = 60000L
)

/**
 * Configuration for liveness detection
 */
data class LivenessConfig(
    val timeoutMs: Long = 30000L,
    val qualityThreshold: Float = 0.8f
)

/**
 * Configuration for document scanning
 */
data class DocumentScanConfig(
    val allowedDocumentTypes: List<String> = listOf("PASSPORT", "ID_CARD"),
    val timeoutMs: Long = 45000L
)

/**
 * Configuration for NFC reading
 */
data class NFCConfig(
    val timeoutMs: Long = 20000L,
    val enablePassiveAuth: Boolean = true
)

/**
 * Configuration for authentication
 */
data class AuthConfig(
    val endpoint: String,
    val timeoutMs: Long = 15000L
)
