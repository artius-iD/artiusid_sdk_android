package com.artiusid.sdk.models

/**
 * SDK error class - EXACT STANDALONE MATCH
 */
data class SDKError(
    val code: SDKErrorCode,
    val message: String,
    val cause: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "SDKError(code=$code, message='$message', cause=$cause)"
    }
}

/**
 * SDK error codes - EXACT STANDALONE MATCH
 */
enum class SDKErrorCode(val value: Int) {
    // General errors
    SDK_NOT_INITIALIZED(1001),
    INVALID_CONFIGURATION(1002),
    INVALID_CONFIG(1002), // Alias for INVALID_CONFIGURATION
    NETWORK_ERROR(1003),
    TIMEOUT_ERROR(1004),
    PERMISSION_DENIED(1005),
    
    // Camera errors
    CAMERA_NOT_AVAILABLE(2001),
    CAMERA_PERMISSION_DENIED(2002),
    CAMERA_INITIALIZATION_FAILED(2003),
    
    // Face detection errors
    FACE_NOT_DETECTED(3001),
    MULTIPLE_FACES_DETECTED(3002),
    FACE_TOO_SMALL(3003),
    FACE_TOO_LARGE(3004),
    POOR_IMAGE_QUALITY(3005),
    LIVENESS_CHECK_FAILED(3006),
    
    // Document scanning errors
    DOCUMENT_NOT_DETECTED(4001),
    DOCUMENT_TOO_BLURRY(4002),
    DOCUMENT_GLARE_DETECTED(4003),
    DOCUMENT_NOT_SUPPORTED(4004),
    MRZ_READ_FAILED(4005),
    BARCODE_READ_FAILED(4006),
    
    // NFC errors
    NFC_NOT_SUPPORTED(5001),
    NFC_NOT_ENABLED(5002),
    NFC_TAG_NOT_FOUND(5003),
    NFC_READ_FAILED(5004),
    NFC_AUTHENTICATION_FAILED(5005),
    
    // API errors
    API_ERROR(6001),
    INVALID_API_KEY(6002),
    API_RATE_LIMIT_EXCEEDED(6003),
    SERVER_ERROR(6004),
    
    // Verification errors
    VERIFICATION_FAILED(7001),
    VERIFICATION_TIMEOUT(7002),
    VERIFICATION_CANCELLED(7003),
    FACE_LIVENESS_FAILED(7004),
    DOCUMENT_SCAN_FAILED(7005),
    PROCESSING_FAILED(7006),
    PROCESSING_ERROR(7007),
    AUTHENTICATION_FAILED(7008),
    
    // Unknown error
    UNKNOWN_ERROR(9999);
    
    companion object {
        fun fromValue(value: Int): SDKErrorCode {
            return values().find { it.value == value } ?: UNKNOWN_ERROR
        }
    }
}
