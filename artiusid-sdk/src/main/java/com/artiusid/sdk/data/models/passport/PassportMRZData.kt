/*
 * File: PassportMRZData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.models.passport

import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents Machine Readable Zone (MRZ) data extracted from passport
 * Android equivalent of iOS PassportMRZData struct
 * Implements ICAO Doc 9303 standards for MRZ validation
 */
data class PassportMRZData(
    val documentType: String?,
    val issuingCountry: String?,
    val surname: String?,
    val givenNames: String?,
    val passportNumber: String?,
    val nationality: String?,
    val dateOfBirth: String?, // Format: YYMMDD
    val sex: String?, // M, F, or < for unspecified
    val dateOfExpiry: String?, // Format: YYMMDD
    val personalNumber: String?,
    val finalCheckDigit: String?,
    val line1: String,
    val line2: String,
    val line2CheckDigit: String
) {
    
    companion object {
        // MRZ field indices for check digit validation
        private val INDICES_PASSPORT_NUMBER = (0..8).toList()
        private val INDICES_DATE_OF_BIRTH = (13..18).toList()
        private val INDICES_DATE_OF_EXPIRY = (21..26).toList()
        private val INDICES_PERSONAL_NUMBER = (28..41).toList()
        private val INDICES_COMPOSITE = (0..9).toList() + (13..19).toList() + (21..42).toList()
        
        /**
         * Calculate MRZ check digit according to ICAO Doc 9303
         */
        fun calculateCheckDigit(input: String): Int {
            val weightValues = intArrayOf(7, 3, 1)
            var sum = 0
            var weightIndex = 0
            
            for (char in input) {
                val value = char.mrzValue()
                val weight = weightValues[weightIndex]
                sum += value * weight
                weightIndex = (weightIndex + 1) % 3
            }
            
            return sum % 10
        }
        
        /**
         * Get MRZ value for a character according to ICAO standards
         */
        private fun Char.mrzValue(): Int {
            return when {
                this.isDigit() -> this.digitToInt()
                this.isLetter() -> this.uppercaseChar() - 'A' + 10
                this == '<' -> 0
                else -> 0
            }
        }
    }
    
    /**
     * Validate MRZ data using check digits
     * @return true if all check digits are valid
     */
    val isValid: Boolean
        get() {
            if (line2.length < 44) return false
            
            val passportNumberValid = validateCheckDigit(
                line = line2,
                checkDigit = line2[9].toString(),
                indices = INDICES_PASSPORT_NUMBER
            )
            
            val dateOfBirthValid = validateCheckDigit(
                line = line2,
                checkDigit = line2[19].toString(),
                indices = INDICES_DATE_OF_BIRTH
            )
            
            val dateOfExpiryValid = validateCheckDigit(
                line = line2,
                checkDigit = line2[27].toString(),
                indices = INDICES_DATE_OF_EXPIRY
            )
            
            val personalNumberValid = validateCheckDigit(
                line = line2,
                checkDigit = line2[42].toString(),
                indices = INDICES_PERSONAL_NUMBER
            )
            
            val compositeValid = validateCheckDigit(
                line = line2,
                checkDigit = line2CheckDigit,
                indices = INDICES_COMPOSITE
            )
            
            return passportNumberValid && dateOfBirthValid && dateOfExpiryValid && 
                   personalNumberValid && compositeValid
        }
    
    /**
     * Generate MRZ key for NFC authentication (BAC/PACE)
     * Format matches iOS implementation: passport_number + date_of_birth + date_of_expiry (RAW, no calculated check digits)
     * This ensures compatibility with iOS BAC authentication and prevents authentication failures
     * @return MRZ key string or null if data is invalid
     */
    fun generateMRZKey(): String? {
        if (!isValid || passportNumber == null || dateOfBirth == null || dateOfExpiry == null) {
            return null
        }
        
        // iOS-compatible format: raw passport number + date of birth + date of expiry
        // Do NOT add calculated check digits - use the check digits from the actual MRZ
        val cleanPassportNumber = passportNumber!!.padEnd(9, '<').take(9)
        val cleanDateOfBirth = dateOfBirth!!
        val cleanDateOfExpiry = dateOfExpiry!!
        
        return "$cleanPassportNumber$cleanDateOfBirth$cleanDateOfExpiry"
    }
    
    /**
     * Generate MRZ key with check digits (legacy format) - kept for reference
     * This is the ICAO standard format but causes authentication issues with iOS compatibility
     */
    fun generateMRZKeyWithCheckDigits(): String? {
        if (!isValid || passportNumber == null || dateOfBirth == null || dateOfExpiry == null) {
            return null
        }
        
        val passportNumberWithCheckDigit = "$passportNumber${calculateCheckDigit(passportNumber!!)}"
        val dateOfBirthWithCheckDigit = "$dateOfBirth${calculateCheckDigit(dateOfBirth!!)}"
        val dateOfExpiryWithCheckDigit = "$dateOfExpiry${calculateCheckDigit(dateOfExpiry!!)}"
        
        return "$passportNumberWithCheckDigit$dateOfBirthWithCheckDigit$dateOfExpiryWithCheckDigit"
    }
    
    /**
     * Validate a check digit for a specific field
     */
    private fun validateCheckDigit(line: String, checkDigit: String, indices: List<Int>): Boolean {
        if (line.length <= indices.maxOrNull() ?: 0) return false
        
        val extractedString = indices.map { line[it] }.joinToString("")
        val calculatedDigit = calculateCheckDigit(extractedString)
        
        return checkDigit.toIntOrNull() == calculatedDigit
    }
    
    /**
     * Format date from YYMMDD to a readable format
     */
    fun formatDate(dateString: String?): String? {
        if (dateString == null || dateString.length != 6) return null
        
        return try {
            val inputFormat = SimpleDateFormat("yyMMdd", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get full name combining surname and given names
     */
    fun getFullName(): String {
        val surnameStr = surname?.replace("<", " ")?.trim() ?: ""
        val givenNamesStr = givenNames?.replace("<", " ")?.trim() ?: ""
        
        return if (givenNamesStr.isNotEmpty()) {
            "$givenNamesStr $surnameStr"
        } else {
            surnameStr
        }.trim()
    }
}