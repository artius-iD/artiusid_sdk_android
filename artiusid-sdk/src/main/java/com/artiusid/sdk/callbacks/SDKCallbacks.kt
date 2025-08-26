package com.artiusid.sdk.callbacks

import com.artiusid.sdk.models.*

/**
 * Callback interface for complete verification flow
 */
interface VerificationCallback {
    fun onSuccess(result: VerificationResult)
    fun onError(error: SDKError)
    fun onCancelled()
    fun onProgress(step: VerificationStep, progress: Int) {}
}

/**
 * Callback interface for face liveness detection
 */
interface LivenessCallback {
    fun onSuccess(result: LivenessResult)
    fun onError(error: SDKError)
    fun onCancelled()
    fun onProgress(segmentsCompleted: Int, totalSegments: Int, currentInstruction: String) {}
}

/**
 * Callback interface for document scanning
 */
interface DocumentScanCallback {
    fun onSuccess(result: DocumentScanResult)
    fun onError(error: SDKError)
    fun onCancelled()
    fun onProgress(isDocumentDetected: Boolean, qualityScore: Float) {}
}

/**
 * Callback interface for NFC passport reading
 */
interface NFCCallback {
    fun onSuccess(result: NFCPassportResult)
    fun onError(error: SDKError)
    fun onCancelled()
    fun onProgress(status: NFCReadingStatus, progress: Int) {}
}

/**
 * Callback interface for authentication
 */
interface AuthenticationCallback {
    fun onSuccess(result: AuthenticationResult)
    fun onError(error: SDKError)
    fun onCancelled()
    fun onProgress(step: AuthenticationStep, progress: Int) {}
}

/**
 * Verification flow steps
 */
enum class VerificationStep {
    FACE_LIVENESS,
    DOCUMENT_SCAN,
    NFC_READING,
    PROCESSING,
    COMPLETED
}

/**
 * Authentication steps
 */
enum class AuthenticationStep {
    FACE_CAPTURE,
    FACE_MATCHING,
    LIVENESS_CHECK,
    COMPLETED
}

/**
 * NFC reading status
 */
enum class NFCReadingStatus {
    INITIALIZING,
    WAITING_FOR_CARD,
    READING_DATA,
    VALIDATING,
    COMPLETED
}
