package com.artiusid.sdk.data.model

/**
 * Enum representing different types of verification failures
 * This matches the standalone app's failure types
 */
enum class VerificationFailureType {
    GENERAL,
    FACE,
    STATE_ID_FRONT,
    STATE_ID_BACK,
    PASSPORT,
    NFC_FAILED,
    NETWORK_ERROR,
    PROCESSING_ERROR
}