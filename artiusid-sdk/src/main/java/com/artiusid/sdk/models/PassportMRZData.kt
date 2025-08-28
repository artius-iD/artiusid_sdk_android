package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing MRZ (Machine Readable Zone) data from passport
 */
@Parcelize
data class PassportMRZData(
    val documentType: String = "",
    val issuingCountry: String = "",
    val surname: String = "",
    val givenNames: String = "",
    val passportNumber: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val dateOfExpiry: String = "",
    val personalNumber: String = "",
    val checkDigit1: String = "",
    val checkDigit2: String = "",
    val checkDigit3: String = "",
    val compositeCheckDigit: String = "",
    val rawMRZ: String = ""
) : Parcelable {
    
    /**
     * Validate MRZ data integrity
     */
    fun isValid(): Boolean {
        return documentType.isNotEmpty() && 
               passportNumber.isNotEmpty() && 
               surname.isNotEmpty() && 
               dateOfBirth.isNotEmpty() && 
               dateOfExpiry.isNotEmpty()
    }
    
    /**
     * Get full name
     */
    fun getFullName(): String {
        return if (givenNames.isNotEmpty()) {
            "$givenNames $surname"
        } else {
            surname
        }
    }
}
