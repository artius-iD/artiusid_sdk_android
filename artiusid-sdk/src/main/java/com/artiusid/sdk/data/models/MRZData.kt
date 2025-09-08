package com.artiusid.sdk.data.models

/**
 * MRZ (Machine Readable Zone) data model - EXACT STANDALONE MATCH
 */
data class MRZData(
    val documentType: String? = null,
    val countryCode: String? = null,
    val surname: String? = null,
    val givenNames: String? = null,
    val documentNumber: String? = null,
    val nationality: String? = null,
    val dateOfBirth: String? = null,
    val sex: String? = null,
    val expirationDate: String? = null,
    val personalNumber: String? = null,
    val checkDigit1: String? = null,
    val checkDigit2: String? = null,
    val checkDigit3: String? = null,
    val compositeCheckDigit: String? = null,
    val rawMrzLine1: String? = null,
    val rawMrzLine2: String? = null,
    val isValid: Boolean = false,
    val confidence: Float = 0.0f
) {
    fun getFullName(): String {
        return "${givenNames ?: ""} ${surname ?: ""}".trim()
    }
    
    fun isComplete(): Boolean {
        return !documentNumber.isNullOrEmpty() && 
               !surname.isNullOrEmpty() && 
               !givenNames.isNullOrEmpty() &&
               !dateOfBirth.isNullOrEmpty() &&
               !expirationDate.isNullOrEmpty()
    }
}
