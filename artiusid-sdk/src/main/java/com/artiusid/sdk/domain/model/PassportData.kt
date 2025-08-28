package com.artiusid.sdk.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing passport information extracted from NFC chip
 */
@Parcelize
data class PassportData(
    val documentNumber: String = "",
    val documentType: String = "",
    val issuingCountry: String = "",
    val surname: String = "",
    val givenNames: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val dateOfExpiry: String = "",
    val personalNumber: String = "",
    val mrzInfo: String = "",
    val photoBase64: String? = null,
    val signatureBase64: String? = null,
    val fingerprints: List<String> = emptyList(),
    val chipAuthenticationSuccessful: Boolean = false,
    val passiveAuthenticationSuccessful: Boolean = false,
    val activeAuthenticationSuccessful: Boolean = false,
    val dataGroupsRead: List<Int> = emptyList(),
    val securityFeatures: Map<String, Boolean> = emptyMap()
) : Parcelable {
    
    /**
     * Check if passport data is valid
     */
    fun isValid(): Boolean {
        return documentNumber.isNotEmpty() && 
               surname.isNotEmpty() && 
               givenNames.isNotEmpty() &&
               dateOfBirth.isNotEmpty() &&
               dateOfExpiry.isNotEmpty()
    }
    
    /**
     * Get full name
     */
    fun getFullName(): String {
        return "$givenNames $surname".trim()
    }
    
    /**
     * Check if passport is expired
     */
    fun isExpired(): Boolean {
        return try {
            val expiryDate = java.text.SimpleDateFormat("yyMMdd", java.util.Locale.getDefault()).parse(dateOfExpiry)
            expiryDate?.before(java.util.Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }
}
