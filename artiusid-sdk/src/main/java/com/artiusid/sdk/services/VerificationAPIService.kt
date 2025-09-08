package com.artiusid.sdk.services

import com.artiusid.sdk.models.*
import kotlinx.coroutines.delay

/**
 * Service for making verification API calls to cloud services
 */
class VerificationAPIService {
    
    /**
     * Submit verification data to cloud services for final verification
     */
    suspend fun submitVerification(
        livenessResult: LivenessResult?,
        documentResult: DocumentScanResult?,
        nfcResult: NFCPassportResult? = null
    ): VerificationAPIResult {
        
        // Simulate API call delay
        delay(2000)
        
        // Simulate cloud verification logic
        val overallConfidence = calculateOverallConfidence(livenessResult, documentResult, nfcResult)
        val verificationScore = calculateVerificationScore(livenessResult, documentResult, nfcResult)
        
        return VerificationAPIResult(
            success = overallConfidence > 0.8f,
            verificationId = "VER-${System.currentTimeMillis()}",
            overallConfidence = overallConfidence,
            verificationScore = verificationScore,
            livenessVerified = livenessResult?.success == true,
            documentVerified = documentResult?.success == true,
            nfcVerified = nfcResult?.success == true,
            riskScore = calculateRiskScore(overallConfidence),
            processingTime = 2000L,
            timestamp = System.currentTimeMillis(),
            details = mapOf(
                "faceMatch" to "verified",
                "documentAuthenticity" to "verified", 
                "nfcAuthenticity" to if (nfcResult != null) "verified" else "not_applicable",
                "biometricQuality" to "high",
                "documentQuality" to "high"
            )
        )
    }
    
    private fun calculateOverallConfidence(
        livenessResult: LivenessResult?,
        documentResult: DocumentScanResult?,
        nfcResult: NFCPassportResult?
    ): Float {
        val scores = mutableListOf<Float>()
        
        livenessResult?.confidence?.let { scores.add(it) }
        documentResult?.confidence?.let { scores.add(it) }
        // NFCPassportResult doesn't have confidence, use isAuthenticated as indicator
        if (nfcResult?.isAuthenticated == true) scores.add(0.95f)
        
        return if (scores.isNotEmpty()) {
            scores.average().toFloat()
        } else {
            0.0f
        }
    }
    
    private fun calculateVerificationScore(
        livenessResult: LivenessResult?,
        documentResult: DocumentScanResult?,
        nfcResult: NFCPassportResult?
    ): Float {
        var score = 0f
        var maxScore = 0f
        
        // Face liveness contributes 40% to overall score
        if (livenessResult != null) {
            maxScore += 40f
            if (livenessResult.success) {
                score += 40f * livenessResult.confidence
            }
        }
        
        // Document verification contributes 40% to overall score  
        if (documentResult != null) {
            maxScore += 40f
            if (documentResult.success) {
                score += 40f * documentResult.confidence
            }
        }
        
        // NFC verification contributes 20% to overall score (if available)
        if (nfcResult != null) {
            maxScore += 20f
            if (nfcResult.success) {
                score += 20f * (if (nfcResult.isAuthenticated) 0.95f else 0.5f)
            }
        } else {
            // If no NFC, redistribute the 20% to document verification
            maxScore += 20f
            if (documentResult?.success == true) {
                score += 20f * documentResult.confidence
            }
        }
        
        return if (maxScore > 0) score / maxScore else 0f
    }
    
    private fun calculateRiskScore(confidence: Float): String {
        return when {
            confidence >= 0.9f -> "LOW"
            confidence >= 0.7f -> "MEDIUM"
            else -> "HIGH"
        }
    }
}

/**
 * Result from verification API call
 */
data class VerificationAPIResult(
    val success: Boolean,
    val verificationId: String,
    val overallConfidence: Float,
    val verificationScore: Float,
    val livenessVerified: Boolean,
    val documentVerified: Boolean,
    val nfcVerified: Boolean,
    val riskScore: String,
    val processingTime: Long,
    val timestamp: Long,
    val details: Map<String, String>
)
