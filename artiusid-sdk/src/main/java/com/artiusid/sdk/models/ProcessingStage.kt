package com.artiusid.sdk.models

/**
 * Processing stages for face liveness - EXACT STANDALONE MATCH
 */
enum class ProcessingStage {
    INITIALIZING,
    POSITIONING,
    DETECTING_FACE,
    ANALYZING_LIVENESS,
    CAPTURING,
    PROCESSING,
    COMPLETED,
    ERROR,
    INITIAL_INSTRUCTIONS,
    CAPTURE_PHOTO,
    CALIBRATING,
    SELFIE_CAPTURE,
    GUIDED_MESH_CAPTURE,
    BLINK_DETECTION
}