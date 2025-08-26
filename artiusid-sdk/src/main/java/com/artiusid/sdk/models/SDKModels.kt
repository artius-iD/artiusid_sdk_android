package com.artiusid.sdk.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Complete verification result containing all verification data
 */
@Parcelize
data class VerificationResult(
    val sessionId: String,
    val timestamp: Date,
    val isSuccessful: Boolean,
    val livenessResult: LivenessResult? = null,
    val documentResult: DocumentScanResult? = null,
    val nfcResult: NFCPassportResult? = null,
    val overallScore: Float,
    val verificationId: String,
    val processingTime: Long = 0L,
    val errorDetails: String? = null
) : Parcelable

/**
 * Face liveness detection result
 */
@Parcelize
data class LivenessResult(
    val isLive: Boolean,
    val confidence: Float,
    val faceImage: Bitmap? = null,
    val segmentsCompleted: Int,
    val totalSegments: Int,
    val processingTime: Long,
    val livenessScore: Float,
    val qualityScore: Float,
    val headMovementData: List<HeadMovement> = emptyList(),
    val blinkDetected: Boolean = false,
    val errorDetails: String? = null
) : Parcelable

/**
 * Head movement data point
 */
@Parcelize
data class HeadMovement(
    val timestamp: Long,
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val segment: Int
) : Parcelable

/**
 * Document scanning result
 */
@Parcelize
data class DocumentScanResult(
    val documentType: com.artiusid.sdk.config.DocumentType,
    val frontImage: Bitmap,
    val backImage: Bitmap? = null,
    val extractedData: Map<String, String>,
    val mrzData: MRZData? = null,
    val barcodeData: BarcodeData? = null,
    val qualityScore: Float,
    val processingTime: Long,
    val ocrConfidence: Float = 0f,
    val errorDetails: String? = null
) : Parcelable

/**
 * Machine Readable Zone (MRZ) data
 */
@Parcelize
data class MRZData(
    val documentType: String,
    val issuingCountry: String,
    val documentNumber: String,
    val dateOfBirth: String,
    val dateOfExpiry: String,
    val nationality: String,
    val sex: String,
    val surname: String,
    val givenNames: String,
    val personalNumber: String? = null,
    val checkDigitsValid: Boolean = true,
    val rawMRZ: String
) : Parcelable

/**
 * Barcode data from document
 */
@Parcelize
data class BarcodeData(
    val format: String,
    val rawData: String,
    val parsedData: Map<String, String> = emptyMap()
) : Parcelable

/**
 * NFC passport reading result
 */
@Parcelize
data class NFCPassportResult(
    val isSuccessful: Boolean,
    val passportData: PassportData,
    val faceImage: Bitmap? = null,
    val securityFeatures: SecurityFeatures,
    val processingTime: Long,
    val dataGroups: Map<Int, ByteArray> = emptyMap(),
    val errorDetails: String? = null
) : Parcelable

/**
 * Passport data from NFC
 */
@Parcelize
data class PassportData(
    val documentNumber: String,
    val issuingCountry: String,
    val nationality: String,
    val surname: String,
    val givenNames: String,
    val dateOfBirth: String,
    val dateOfExpiry: String,
    val sex: String,
    val personalNumber: String? = null,
    val placeOfBirth: String? = null,
    val issuingAuthority: String? = null,
    val dateOfIssue: String? = null
) : Parcelable

/**
 * Security features validation result
 */
@Parcelize
data class SecurityFeatures(
    val activeAuthentication: Boolean = false,
    val passiveAuthentication: Boolean = false,
    val chipAuthentication: Boolean = false,
    val terminalAuthentication: Boolean = false,
    val securityScore: Float,
    val validationErrors: List<String> = emptyList()
) : Parcelable

/**
 * Authentication result
 */
@Parcelize
data class AuthenticationResult(
    val isAuthenticated: Boolean,
    val confidence: Float,
    val faceMatchScore: Float,
    val livenessScore: Float,
    val capturedImage: Bitmap? = null,
    val referenceImage: Bitmap? = null,
    val processingTime: Long,
    val authenticationId: String,
    val errorDetails: String? = null
) : Parcelable

/**
 * SDK Error information
 */
@Parcelize
data class SDKError(
    val code: Int,
    val message: String,
    val details: String? = null,
    val timestamp: Date = Date(),
    val recoverable: Boolean = false,
    val suggestedAction: String? = null
) : Parcelable {
    
    companion object {
        // Error codes
        const val ERROR_INITIALIZATION = 1000
        const val ERROR_PERMISSION_DENIED = 1001
        const val ERROR_CAMERA_UNAVAILABLE = 1002
        const val ERROR_NFC_UNAVAILABLE = 1003
        const val ERROR_NETWORK_ERROR = 1004
        const val ERROR_PROCESSING_FAILED = 1005
        const val ERROR_TIMEOUT = 1006
        const val ERROR_USER_CANCELLED = 1007
        const val ERROR_INVALID_CONFIGURATION = 1008
        const val ERROR_DEVICE_NOT_SUPPORTED = 1009
        const val ERROR_FACE_NOT_DETECTED = 1100
        const val ERROR_FACE_TOO_CLOSE = 1101
        const val ERROR_FACE_TOO_FAR = 1102
        const val ERROR_FACE_NOT_CENTERED = 1103
        const val ERROR_LIVENESS_FAILED = 1104
        const val ERROR_DOCUMENT_NOT_DETECTED = 1200
        const val ERROR_DOCUMENT_BLURRY = 1201
        const val ERROR_DOCUMENT_GLARE = 1202
        const val ERROR_DOCUMENT_PARTIAL = 1203
        const val ERROR_OCR_FAILED = 1204
        const val ERROR_MRZ_INVALID = 1205
        const val ERROR_NFC_CONNECTION_FAILED = 1300
        const val ERROR_NFC_READ_FAILED = 1301
        const val ERROR_NFC_AUTHENTICATION_FAILED = 1302
        const val ERROR_NFC_DATA_CORRUPTED = 1303
    }
}

/**
 * SDK Version information
 */
@Parcelize
data class SDKVersionInfo(
    val version: String,
    val buildNumber: String,
    val buildDate: String,
    val features: List<String> = listOf("FaceLiveness", "DocumentScan", "NFCReading", "Authentication")
) : Parcelable
