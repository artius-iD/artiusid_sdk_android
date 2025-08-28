package com.artiusid.sdk.models

/**
 * Enum representing different processing stages in face scanning
 */
enum class ProcessingStage {
    INITIALIZING,
    DETECTING_FACE,
    ANALYZING_QUALITY,
    CAPTURING_IMAGE,
    PROCESSING_LIVENESS,
    COMPLETED,
    ERROR
}

/**
 * Enum representing passport scanning states
 */
enum class PassportScanningState {
    IDLE,
    SCANNING,
    PROCESSING,
    SUCCESS,
    ERROR,
    TIMEOUT
}

/**
 * Enum representing document recapture types
 */
enum class DocumentRecaptureType {
    NONE,
    FRONT_DOCUMENT,
    BACK_DOCUMENT,
    PASSPORT,
    FACE_IMAGE,
    GENERAL_ERROR
}
