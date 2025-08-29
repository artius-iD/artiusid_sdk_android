package com.artiusid.sdk.models

/**
 * SDK error codes
 */
enum class SDKErrorCode {
    UNKNOWN_ERROR,
    NETWORK_ERROR,
    CAMERA_ERROR,
    PERMISSION_DENIED,
    TIMEOUT,
    INVALID_CONFIG,
    PROCESSING_ERROR,
    FACE_DETECTION_FAILED,
    FACE_LIVENESS_FAILED,
    DOCUMENT_SCAN_FAILED,
    NFC_FAILED,
    PROCESSING_FAILED,
    AUTHENTICATION_FAILED
}

/**
 * SDK error class
 */
data class SDKError(
    val code: SDKErrorCode,
    val message: String,
    val cause: Throwable? = null
)

/**
 * Verification result
 */
data class VerificationResult(
    val success: Boolean,
    val confidence: Float,
    val livenessResult: LivenessResult?,
    val documentScanResult: DocumentScanResult?,
    val nfcResult: NFCPassportResult?,
    val processingTime: Long,
    val sessionId: String
)

/**
 * Liveness detection result
 */
data class LivenessResult(
    val success: Boolean,
    val confidence: Float,
    val livenessScore: Float,
    val faceImage: android.graphics.Bitmap? = null,
    val processingTime: Long,
    val sessionId: String
)

/**
 * Document scan result
 */
data class DocumentScanResult(
    val success: Boolean,
    val documentType: String,
    val frontImage: android.graphics.Bitmap? = null,
    val backImage: android.graphics.Bitmap? = null,
    val ocrData: Map<String, String> = emptyMap(),
    val mrzData: MRZData? = null,
    val aamvaData: AAMVAData? = null,
    val confidence: Float = 0f,
    val processingTime: Long = 0L,
    val sessionId: String = "",
    val message: String? = null,
    val error: SDKError? = null,
    val validationStatus: String = "",
    val documentBounds: android.graphics.Rect? = null
)

/**
 * NFC passport result
 */
data class NFCPassportResult(
    val success: Boolean,
    val passportData: Map<String, String>,
    val confidence: Float,
    val processingTime: Long,
    val sessionId: String
)

/**
 * Authentication result
 */
data class AuthenticationResult(
    val success: Boolean,
    val token: String?,
    val expiresAt: Long,
    val sessionId: String
)

/**
 * MRZ data from passport scanning
 */
data class MRZData(
    val documentType: String = "",
    val issuingCountry: String = "",
    val documentNumber: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val sex: String = "",
    val expirationDate: String = "",
    val personalNumber: String = "",
    val surname: String = "",
    val givenNames: String = "",
    val checkDigits: Map<String, Int> = emptyMap(),
    val isValid: Boolean = false
)

/**
 * Processing result for verification flow
 */
data class ProcessingResult(
    val success: Boolean,
    val verificationResultData: VerificationResultData? = null,
    val error: SDKError? = null
)

// VerificationResultData is defined in VerificationModels.kt
