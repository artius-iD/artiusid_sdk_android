package com.artiusid.sdk.models

/**
 * Passport scanning states - EXACT STANDALONE MATCH
 */
enum class PassportScanningState {
    IDLE,
    SCANNING,
    MRZ_DETECTED,
    PROCESSING_MRZ,
    MRZ_COMPLETE,
    READY_FOR_NFC,
    ERROR,
    NOT_STARTED,
    VALIDATING,
    COMPLETED,
    FAILED
}
