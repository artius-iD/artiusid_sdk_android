package com.artiusid.sdk.models

import com.artiusid.sdk.models.*

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
    GENERAL_ERROR,
    // Face-related errors
    FACE_TOO_BLURRY,
    FACE_NOT_DETECTED,
    FACE_TOO_DARK,
    FACE_TOO_BRIGHT,
    FACE_TOO_FAR,
    FACE_TOO_CLOSE,
    FACE_ANGLE_INCORRECT,
    // Document-related errors
    DOCUMENT_TOO_BLURRY,
    DOCUMENT_GLARE,
    DOCUMENT_CROPPED,
    DOCUMENT_NOT_DETECTED,
    DOCUMENT_ANGLE_INCORRECT
}
