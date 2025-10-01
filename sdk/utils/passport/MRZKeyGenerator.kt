/*
 * File: MRZKeyGenerator.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// MRZKeyGenerator.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.utils.passport

import android.util.Log
import java.security.MessageDigest
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates MRZ-derived keys for passport authentication (BAC/PACE)
 * Based on ICAO Doc 9303 specifications and iOS PassportReader implementation
 */
object MRZKeyGenerator {
    
    private const val TAG = "MRZKeyGenerator"
    
    /**
     * Generate MRZ key from passport MRZ data for BAC authentication
     * Format: passportNumber + dateOfBirth + dateOfExpiry (YYMMDD format)
     * iOS-compatible format: raw values without calculated check digits
     * 
     * @param passportNumber Passport document number (e.g., "P12345678")
     * @param dateOfBirth Date of birth in YYMMDD format (e.g., "901215")
     * @param dateOfExpiry Date of expiry in YYMMDD format (e.g., "301215")
     * @return MRZ key string for authentication
     */
    fun generateMRZKey(
        passportNumber: String,
        dateOfBirth: String,
        dateOfExpiry: String
    ): String {
        Log.d(TAG, "Generating iOS-compatible MRZ key from passport: $passportNumber, DOB: $dateOfBirth, Expiry: $dateOfExpiry")
        
        // Validate input parameters
        validateMRZInputs(passportNumber, dateOfBirth, dateOfExpiry)
        
        // Clean and format inputs - iOS compatible (no calculated check digits)
        val cleanPassportNumber = cleanPassportNumber(passportNumber)
        val cleanDateOfBirth = cleanDate(dateOfBirth)
        val cleanDateOfExpiry = cleanDate(dateOfExpiry)
        
        // Generate MRZ key in iOS-compatible format (raw values only)
        val mrzKey = "$cleanPassportNumber$cleanDateOfBirth$cleanDateOfExpiry"
        
        Log.d(TAG, "Generated iOS-compatible MRZ key: ${mrzKey.take(6)}... (${mrzKey.length} characters)")
        Log.d(TAG, "MRZ key components: passport='$cleanPassportNumber', dob='$cleanDateOfBirth', expiry='$cleanDateOfExpiry'")
        return mrzKey
    }
    
    /**
     * Generate MRZ key from PassportMRZData
     */
    fun generateMRZKey(mrzData: com.artiusid.sdk.data.models.passport.PassportMRZData): String {
        return generateMRZKey(
            passportNumber = mrzData.passportNumber ?: "",
            dateOfBirth = mrzData.dateOfBirth ?: "",
            dateOfExpiry = mrzData.dateOfExpiry ?: ""
        )
    }
    
    /**
     * Generate formatted MRZ key for NFC authentication with pipe delimiters
     * This format is used by PassportNFCReader for parsing
     */
    fun generateFormattedMRZKey(
        passportNumber: String,
        dateOfBirth: String,
        dateOfExpiry: String
    ): String {
        val cleanPassportNumber = cleanPassportNumber(passportNumber)
        val cleanDateOfBirth = cleanDate(dateOfBirth)
        val cleanDateOfExpiry = cleanDate(dateOfExpiry)
        
        return "$cleanPassportNumber|$cleanDateOfBirth|$cleanDateOfExpiry"
    }
    
    /**
     * Parse formatted MRZ key back into components
     */
    fun parseMRZKey(formattedKey: String): Triple<String, String, String> {
        val parts = formattedKey.split("|")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid MRZ key format. Expected format: passportNumber|dateOfBirth|dateOfExpiry")
        }
        return Triple(parts[0], parts[1], parts[2])
    }
    
    /**
     * Generate BAC authentication seed from MRZ key
     * Used for deriving encryption and MAC keys for BAC protocol
     */
    fun generateBACKey(mrzKey: String): ByteArray {
        try {
            // Convert MRZ key to bytes and compute SHA-1 hash
            val mrzBytes = mrzKey.toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(mrzBytes)
            
            // BAC key is first 16 bytes of SHA-1 hash
            return hash.copyOf(16)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate BAC key", e)
            throw RuntimeException("BAC key generation failed", e)
        }
    }
    
    /**
     * Clean passport number by removing non-alphanumeric characters and padding
     */
    private fun cleanPassportNumber(passportNumber: String): String {
        // Remove non-alphanumeric characters
        val cleaned = passportNumber.replace(Regex("[^A-Z0-9]"), "")
        
        // Pad to 9 characters with '<' if necessary (standard MRZ format)
        return if (cleaned.length < 9) {
            cleaned.padEnd(9, '<')
        } else {
            cleaned.take(9)
        }
    }
    
    /**
     * Clean and validate date string (YYMMDD format)
     */
    private fun cleanDate(date: String): String {
        // Remove non-digit characters
        val cleaned = date.replace(Regex("[^0-9]"), "")
        
        if (cleaned.length != 6) {
            throw IllegalArgumentException("Date must be 6 digits in YYMMDD format, got: $date")
        }
        
        // Validate date format
        try {
            val format = SimpleDateFormat("yyMMdd", Locale.US)
            format.isLenient = false
            format.parse(cleaned)
        } catch (e: ParseException) {
            throw IllegalArgumentException("Invalid date format: $date. Expected YYMMDD format.")
        }
        
        return cleaned
    }
    
    /**
     * Validate MRZ input parameters
     */
    private fun validateMRZInputs(passportNumber: String, dateOfBirth: String, dateOfExpiry: String) {
        if (passportNumber.isBlank()) {
            throw IllegalArgumentException("Passport number cannot be blank")
        }
        
        if (dateOfBirth.isBlank()) {
            throw IllegalArgumentException("Date of birth cannot be blank")
        }
        
        if (dateOfExpiry.isBlank()) {
            throw IllegalArgumentException("Date of expiry cannot be blank")
        }
        
        // Additional length validation
        if (passportNumber.length > 20) {
            throw IllegalArgumentException("Passport number too long: ${passportNumber.length} characters")
        }
    }
    
    /**
     * Convert date from various formats to YYMMDD
     */
    fun convertDateToMRZFormat(date: String): String {
        val inputFormats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy", 
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "MM-dd-yyyy",
            "yyyyMMdd",
            "yyMMdd"
        )
        
        val outputFormat = SimpleDateFormat("yyMMdd", Locale.US)
        
        for (format in inputFormats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.US)
                inputFormat.isLenient = false
                val parsedDate = inputFormat.parse(date)
                return outputFormat.format(parsedDate!!)
            } catch (e: ParseException) {
                // Try next format
                continue
            }
        }
        
        throw IllegalArgumentException("Unable to parse date: $date. Supported formats: $inputFormats")
    }
    
    /**
     * Validate MRZ key format and content
     */
    fun validateMRZKey(mrzKey: String): Boolean {
        return try {
            if (mrzKey.length < 15) return false
            
            // Try to extract components and validate
            val passportNumber = mrzKey.substring(0, 9)
            val dateOfBirth = mrzKey.substring(9, 15)
            val dateOfExpiry = mrzKey.substring(15, 21)
            
            cleanDate(dateOfBirth)
            cleanDate(dateOfExpiry)
            
            true
        } catch (e: Exception) {
            Log.w(TAG, "MRZ key validation failed: ${e.message}")
            false
        }
    }
}
