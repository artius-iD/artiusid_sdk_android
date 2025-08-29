package com.artiusid.sdk.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Comprehensive verification data holder
 */
@Parcelize
data class VerificationData(
    val sessionId: String,
    val faceImage: @RawValue Bitmap? = null,
    val documentFrontImage: @RawValue Bitmap? = null,
    val documentBackImage: @RawValue Bitmap? = null,
    val passportImage: @RawValue Bitmap? = null,
    val livenessResult: @RawValue LivenessResult? = null,
    val documentScanResult: @RawValue DocumentScanResult? = null,
    val nfcResult: @RawValue NFCPassportResult? = null,
    val mrzData: PassportMRZData? = null,
    val aamvaData: AAMVAData? = null,
    val verificationResult: @RawValue VerificationResult? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String? = null,
    val appVersion: String? = null
) : Parcelable {
    
    /**
     * Check if verification data is complete
     */
    fun isComplete(): Boolean {
        return faceImage != null && 
               (documentFrontImage != null || passportImage != null) &&
               livenessResult?.success == true
    }
    
    /**
     * Get verification completion percentage
     */
    fun getCompletionPercentage(): Float {
        var completed = 0f
        var total = 0f
        
        // Face capture (required)
        total += 1f
        if (faceImage != null) completed += 1f
        
        // Document capture (required)
        total += 1f
        if (documentFrontImage != null || passportImage != null) completed += 1f
        
        // Liveness check (required)
        total += 1f
        if (livenessResult?.success == true) completed += 1f
        
        // Optional components
        if (documentBackImage != null) completed += 0.5f
        if (nfcResult?.success == true) completed += 0.5f
        if (mrzData != null) completed += 0.5f
        if (aamvaData != null) completed += 0.5f
        
        return if (total > 0) (completed / total).coerceAtMost(1f) else 0f
    }
}
