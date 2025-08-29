package com.artiusid.sdk.models

import com.artiusid.sdk.models.*

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing NFC data read from passport chip
 */
@Parcelize
data class PassportNFCData(
    val documentNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val documentExpiryDate: String = "",
    val gender: String = "",
    val issuingCountry: String = "",
    val personalNumber: String = "",
    val placeOfBirth: String = "",
    val dateOfIssue: String = "",
    val issuingAuthority: String = "",
    val bacStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_ATTEMPTED,
    val chipAuthenticationStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_ATTEMPTED,
    val passiveAuthenticationStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_ATTEMPTED,
    val photoBase64: String? = null,
    val signatureBase64: String? = null,
    val fingerprints: List<String> = emptyList(),
    val rawMRZ: String = "",
    val chipData: Map<String, String> = emptyMap(),
    val readingTime: Long = 0L,
    val errors: List<String> = emptyList()
) : Parcelable {
    
    /**
     * Check if NFC reading was successful
     */
    fun isValid(): Boolean {
        return documentNumber.isNotEmpty() && 
               firstName.isNotEmpty() && 
               lastName.isNotEmpty() && 
               bacStatus == PassportAuthenticationStatus.SUCCESS
    }
    
    /**
     * Get full name
     */
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }
    
    /**
     * Check if authentication was successful
     */
    fun isAuthenticationSuccessful(): Boolean {
        return bacStatus == PassportAuthenticationStatus.SUCCESS
    }
    
    /**
     * Get authentication summary
     */
    fun getAuthenticationSummary(): String {
        return buildString {
            append("BAC: ${bacStatus.name}")
            if (chipAuthenticationStatus != PassportAuthenticationStatus.NOT_ATTEMPTED) {
                append(", Chip Auth: ${chipAuthenticationStatus.name}")
            }
            if (passiveAuthenticationStatus != PassportAuthenticationStatus.NOT_ATTEMPTED) {
                append(", Passive Auth: ${passiveAuthenticationStatus.name}")
            }
        }
    }
}

/**
 * Enum representing passport authentication status
 */
enum class PassportAuthenticationStatus {
    NOT_ATTEMPTED,
    SUCCESS,
    FAILED,
    ERROR
}
