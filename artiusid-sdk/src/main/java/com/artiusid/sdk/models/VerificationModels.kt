package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data classes for verification process
 */

@Parcelize
data class VerificationRequest(
    val faceImage: String? = null,
    val documentFrontImage: String? = null,
    val documentBackImage: String? = null,
    val passportImage: String? = null,
    val nfcData: PassportNFCData? = null,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class VerificationResponse(
    val success: Boolean,
    val confidence: Float,
    val results: VerificationResults,
    val sessionId: String,
    val processingTime: Long
) : Parcelable

/**
 * Verification results enum
 */
enum class VerificationResults {
    SUCCESS,
    FAILED_FACE_MATCH,
    FAILED_DOCUMENT_QUALITY,
    FAILED_NFC_VERIFICATION,
    FAILED_GENERAL,
    PROCESSING_ERROR
}

@Parcelize
data class VerificationResultData(
    val success: Boolean,
    val confidence: Float,
    val faceMatchScore: Float = 0f,
    val documentQualityScore: Float = 0f,
    val nfcVerificationScore: Float = 0f,
    val results: VerificationResults,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val processingTime: Long = 0L,
    val errors: List<String> = emptyList()
) : Parcelable

/**
 * Data class for scanned passport image
 */
@Parcelize
data class ScannedPassportImage(
    val imageBase64: String,
    val mrzData: PassportMRZData? = null,
    val quality: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Data class for passport data
 */
@Parcelize
data class PassportData(
    val mrzData: PassportMRZData? = null,
    val nfcData: PassportNFCData? = null,
    val imageBase64: String? = null,
    val isVisualScanComplete: Boolean = false,
    val isNfcScanComplete: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    fun isComplete(): Boolean {
        return isVisualScanComplete && (isNfcScanComplete || nfcData != null)
    }
}

/**
 * Data class for AAMVA barcode data
 */
@Parcelize
data class AAMVAData(
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val licenseNumber: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val issueDate: String = "",
    val expirationDate: String = "",
    val rawData: String = ""
) : Parcelable
