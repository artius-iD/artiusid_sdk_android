package com.artiusid.sdk.models

import com.artiusid.sdk.data.models.StandaloneVerificationResultData

/**
 * Complete verification result - EXACT STANDALONE MATCH
 */
data class VerificationResult(
    val success: Boolean,
    val verificationData: StandaloneVerificationResultData? = null,
    val livenessResult: LivenessResult? = null,
    val documentResult: DocumentScanResult? = null,
    val nfcResult: NFCPassportResult? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
