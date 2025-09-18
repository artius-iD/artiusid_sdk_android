/*
 * File: PassportNFCData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.models.passport

import android.graphics.Bitmap
import java.util.*

/**
 * Represents NFC data read from passport chip
 * Android equivalent of iOS NFCPassportModel class
 * Contains data from various datagroups (DG1, DG2, etc.)
 */
data class PassportNFCData(
    // Basic document information (from DG1)
    val documentType: String = "",
    val documentSubType: String = "",
    val documentNumber: String = "",
    val issuingAuthority: String = "",
    val documentExpiryDate: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nationality: String = "",
    
    // Name information
    val lastName: String = "",
    val firstName: String = "",
    
    // MRZ data from chip
    val passportMRZ: String = "",
    
    // Biometric data (from DG2)
    val faceImage: Bitmap? = null,
    val faceImageFormat: String = "", // JPEG, JPEG2000, etc.
    
    // Authentication status
    val bacStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
    val paceStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
    val passiveAuthenticationStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
    val activeAuthenticationStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
    
    // Additional datagroups (if available)
    val fingerprints: List<ByteArray> = emptyList(), // DG3
    val irisImages: List<ByteArray> = emptyList(), // DG4
    val displayedPortrait: Bitmap? = null, // DG5
    val additionalPersonalDetails: Map<String, String> = emptyMap(), // DG11
    val additionalDocumentDetails: Map<String, String> = emptyMap(), // DG12
    
    // Security object data
    val sodFile: ByteArray? = null,
    val documentSignerCertificates: List<ByteArray> = emptyList(),
    
    // Processing metadata
    val readTimestamp: Long = System.currentTimeMillis(),
    val processingTimeMs: Long = 0,
    val dataGroupsRead: List<String> = emptyList()
) {
    
    /**
     * Check if the passport data contains essential information
     */
    fun hasEssentialData(): Boolean {
        return documentNumber.isNotEmpty() && 
               lastName.isNotEmpty() && 
               firstName.isNotEmpty() &&
               dateOfBirth.isNotEmpty()
    }
    
    /**
     * Check if BAC authentication was successful
     */
    fun isBACAuthenticated(): Boolean {
        return bacStatus == PassportAuthenticationStatus.SUCCESS
    }
    
    /**
     * Check if PACE authentication was successful
     */
    fun isPACEAuthenticated(): Boolean {
        return paceStatus == PassportAuthenticationStatus.SUCCESS
    }
    
    /**
     * Check if any authentication method was successful
     */
    fun isAuthenticated(): Boolean {
        return isBACAuthenticated() || isPACEAuthenticated()
    }
    
    /**
     * Get full name
     */
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }
    
    /**
     * Get processing summary
     */
    fun getProcessingSummary(): String {
        return buildString {
            append("Read ${dataGroupsRead.size} datagroups in ${processingTimeMs}ms\n")
            append("Authentication: ")
            when {
                isPACEAuthenticated() -> append("PACE Success")
                isBACAuthenticated() -> append("BAC Success")
                else -> append("Authentication Failed")
            }
            if (faceImage != null) {
                append("\nFace image: ${faceImage.width}x${faceImage.height} ($faceImageFormat)")
            }
        }
    }
    
    /**
     * Convert to verification request format
     */
    fun toVerificationData(): Map<String, Any> {
        return mapOf(
            "documentNumber" to documentNumber,
            "firstName" to firstName,
            "lastName" to lastName,
            "dateOfBirth" to dateOfBirth,
            "nationality" to nationality,
            "gender" to gender,
            "documentType" to documentType,
            "issuingAuthority" to issuingAuthority,
            "expiryDate" to documentExpiryDate,
            "authenticated" to isAuthenticated(),
            "authenticationMethod" to when {
                isPACEAuthenticated() -> "PACE"
                isBACAuthenticated() -> "BAC"
                else -> "None"
            },
            "dataGroupsRead" to dataGroupsRead,
            "hasFaceImage" to (faceImage != null),
            "readTimestamp" to readTimestamp
        )
    }
}

/**
 * Authentication status for various passport security features
 */
enum class PassportAuthenticationStatus {
    NOT_DONE,
    SUCCESS,
    FAILED
}