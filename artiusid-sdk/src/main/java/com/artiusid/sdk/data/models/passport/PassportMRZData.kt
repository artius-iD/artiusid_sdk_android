package com.artiusid.sdk.data.models.passport

/**
 * Passport MRZ (Machine Readable Zone) data - EXACT STANDALONE MATCH
 */
data class PassportMRZData(
    val documentType: String? = null,
    val countryCode: String? = null,
    val surname: String? = null,
    val givenNames: String? = null,
    val passportNumber: String? = null,
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
    val confidence: Float = 0.0f,
    val mrzLine1: String? = null,
    val mrzLine2: String? = null,
    val fullName: String? = null,
    val issuingCountry: String? = null,
    val dateOfExpiry: String? = null,
    val finalCheckDigit: String? = null,
    val line1: String? = null,
    val line2: String? = null,
    val line2CheckDigit: String? = null
)