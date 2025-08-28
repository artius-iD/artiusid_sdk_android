package com.artiusid.sdk.callbacks

import com.artiusid.sdk.models.*

/**
 * Callback for verification flow completion
 */
interface VerificationCallback {
    fun onVerificationComplete(result: VerificationResult)
    fun onVerificationError(error: SDKError)
    fun onVerificationCancelled()
}

/**
 * Callback for liveness detection completion
 */
interface LivenessCallback {
    fun onLivenessComplete(result: LivenessResult)
    fun onLivenessError(error: SDKError)
}

/**
 * Callback for document scanning completion
 */
interface DocumentScanCallback {
    fun onDocumentScanComplete(result: DocumentScanResult)
    fun onDocumentScanError(error: SDKError)
}

/**
 * Callback for NFC reading completion
 */
interface NFCReadingCallback {
    fun onNFCReadingComplete(result: NFCPassportResult)
    fun onNFCReadingError(error: SDKError)
}

/**
 * Callback for authentication completion
 */
interface AuthenticationCallback {
    fun onAuthenticationComplete(result: AuthenticationResult)
    fun onAuthenticationError(error: SDKError)
    fun onAuthenticationCancelled()
}
