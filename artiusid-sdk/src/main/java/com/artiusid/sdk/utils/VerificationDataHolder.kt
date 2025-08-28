package com.artiusid.sdk.utils

import com.artiusid.sdk.sdk.models.*

/**
 * Utility class to hold verification data during the SDK flow
 * This replicates the functionality from the standalone app
 */
object VerificationDataHolder {
    
    private var verificationData: VerificationData? = null
    
    /**
     * Store verification data
     */
    fun setVerificationData(data: VerificationData) {
        verificationData = data
    }
    
    /**
     * Get stored verification data
     */
    fun getVerificationData(): VerificationData? {
        return verificationData
    }
    
    /**
     * Clear verification data
     */
    fun clearVerificationData() {
        verificationData = null
    }
    
    /**
     * Update liveness result
     */
    fun updateLivenessResult(result: LivenessResult) {
        verificationData = verificationData?.copy(livenessResult = result) ?: VerificationData(
            livenessResult = result,
            sessionId = "sdk_${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Update document scan result
     */
    fun updateDocumentScanResult(result: DocumentScanResult) {
        verificationData = verificationData?.copy(documentScanResult = result) ?: VerificationData(
            documentScanResult = result,
            sessionId = "sdk_${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Update NFC passport result
     */
    fun updateNFCPassportResult(result: NFCPassportResult) {
        verificationData = verificationData?.copy(nfcPassportResult = result) ?: VerificationData(
            nfcPassportResult = result,
            sessionId = "sdk_${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Update confidence score
     */
    fun updateConfidence(confidence: Float) {
        verificationData = verificationData?.copy(confidence = confidence) ?: VerificationData(
            confidence = confidence,
            sessionId = "sdk_${System.currentTimeMillis()}"
        )
    }
}

/**
 * Data class to hold all verification information
 */
data class VerificationData(
    val livenessResult: LivenessResult? = null,
    val documentScanResult: DocumentScanResult? = null,
    val nfcPassportResult: NFCPassportResult? = null,
    val confidence: Float? = null,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis()
)