/*
 * File: MRZParser.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils.passport

import com.artiusid.sdk.data.models.passport.PassportMRZData
import java.util.regex.Pattern

/**
 * Parser for Machine Readable Zone (MRZ) data from passport text recognition
 * Implements ICAO Doc 9303 standards for passport MRZ parsing
 */
object MRZParser {
    
    // MRZ line patterns for TD-3 (passport) format
    private val MRZ_LINE1_PATTERN = Pattern.compile("^P[A-Z<]{1}[A-Z<]{3}([A-Z<]+)<<([A-Z<]+)<*$")
    private val MRZ_LINE2_PATTERN = Pattern.compile("^([A-Z0-9<]{9})([0-9<]{1})([A-Z<]{3})([0-9<]{6})([0-9<]{1})([MF<]{1})([0-9<]{6})([0-9<]{1})([A-Z0-9<]{14})([0-9<]{1})([0-9<]{1})$")
    
    /**
     * Parse MRZ lines extracted from text recognition
     * @param recognizedLines List of text lines from ML Kit
     * @return PassportMRZData if valid MRZ found, null otherwise
     */
    fun parseMRZ(recognizedLines: List<String>): PassportMRZData? {
        val cleanedLines = recognizedLines.map { cleanMRZLine(it) }
        
        // Find potential MRZ lines (should be 44 characters each)
        val mrzCandidates = cleanedLines.filter { it.length >= 42 && it.length <= 46 }
        
        if (mrzCandidates.size < 2) {
            return null
        }
        
        // Try different combinations of lines to find valid MRZ
        for (i in 0 until mrzCandidates.size - 1) {
            for (j in i + 1 until mrzCandidates.size) {
                val line1 = mrzCandidates[i]
                val line2 = mrzCandidates[j]
                
                // Try both orders
                parseMRZPair(line1, line2)?.let { return it }
                parseMRZPair(line2, line1)?.let { return it }
            }
        }
        
        return null
    }
    
    /**
     * Parse a specific pair of MRZ lines
     */
    private fun parseMRZPair(line1: String, line2: String): PassportMRZData? {
        if (line1.length != 44 || line2.length != 44) {
            return null
        }
        
        // Validate line formats
        if (!isValidMRZLine1(line1) || !isValidMRZLine2(line2)) {
            return null
        }
        
        return try {
            val data = parseValidMRZLines(line1, line2)
            if (data.isValid) data else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse validated MRZ lines into PassportMRZData
     */
    private fun parseValidMRZLines(line1: String, line2: String): PassportMRZData {
        // Parse line 1: Document type, country, names
        val documentType = line1.substring(0, 1)
        val issuingCountry = line1.substring(2, 5).replace("<", "")
        
        // Extract surname and given names
        val nameSection = line1.substring(5, 44)
        val nameComponents = nameSection.split("<<")
        val surname = nameComponents[0].replace("<", " ").trim()
        val givenNames = if (nameComponents.size > 1) {
            nameComponents[1].replace("<", " ").trim()
        } else ""
        
        // Parse line 2: Passport number, nationality, dates, etc.
        val passportNumber = line2.substring(0, 9).replace("<", "")
        val passportCheckDigit = line2.substring(9, 10)
        val nationality = line2.substring(10, 13).replace("<", "")
        val dateOfBirth = line2.substring(13, 19)
        val dateOfBirthCheckDigit = line2.substring(19, 20)
        val sex = line2.substring(20, 21)
        val dateOfExpiry = line2.substring(21, 27)
        val dateOfExpiryCheckDigit = line2.substring(27, 28)
        val personalNumber = line2.substring(28, 42).replace("<", "")
        val personalNumberCheckDigit = line2.substring(42, 43)
        val finalCheckDigit = line2.substring(43, 44)
        
        return PassportMRZData(
            documentType = documentType,
            issuingCountry = issuingCountry,
            surname = surname,
            givenNames = givenNames,
            passportNumber = passportNumber,
            nationality = nationality,
            dateOfBirth = dateOfBirth,
            sex = sex,
            dateOfExpiry = dateOfExpiry,
            personalNumber = personalNumber,
            finalCheckDigit = finalCheckDigit,
            line1 = line1,
            line2 = line2,
            line2CheckDigit = finalCheckDigit
        )
    }
    
    /**
     * Clean and normalize MRZ line from text recognition
     */
    private fun cleanMRZLine(line: String): String {
        return line
            .uppercase()
            .replace("[^A-Z0-9<]".toRegex(), "") // Remove invalid characters
            .replace("0", "O") // Common OCR mistake
            .replace("1", "I") // Common OCR mistake
            .replace("8", "B") // Common OCR mistake
            .replace("K", "<") // Common OCR mistake: K is often misread < character
            .padEnd(44, '<') // Ensure 44 characters
            .take(44) // Limit to 44 characters
    }
    
    /**
     * Validate MRZ line 1 format (document type and names)
     */
    private fun isValidMRZLine1(line: String): Boolean {
        return line.startsWith("P") && line.length == 44
    }
    
    /**
     * Validate MRZ line 2 format (passport number, dates, etc.)
     */
    private fun isValidMRZLine2(line: String): Boolean {
        if (line.length != 44) return false
        
        // Check that date fields contain only digits
        val dateOfBirth = line.substring(13, 19)
        val dateOfExpiry = line.substring(21, 27)
        
        return dateOfBirth.all { it.isDigit() || it == '<' } &&
               dateOfExpiry.all { it.isDigit() || it == '<' } &&
               line.substring(20, 21) in listOf("M", "F", "<") // Valid sex indicators
    }
    
    /**
     * Extract potential MRZ lines from text blocks
     * Looks for lines that could be MRZ based on characteristics
     */
    fun extractPotentialMRZLines(textBlocks: List<String>): List<String> {
        return textBlocks
            .asSequence()
            .map { it.trim() }
            .filter { it.length >= 35 } // Minimum length for potential MRZ
            .filter { line ->
                // Should contain mostly uppercase letters, numbers, and < characters
                val validChars = line.count { it.isUpperCase() || it.isDigit() || it == '<' }
                val ratio = validChars.toFloat() / line.length
                ratio > 0.7f
            }
            .map { cleanMRZLine(it) }
            .filter { it.length >= 40 }
            .toList()
    }
    
    /**
     * Validate that extracted MRZ data makes sense
     */
    fun validateMRZData(mrzData: PassportMRZData): List<String> {
        val issues = mutableListOf<String>()
        
        if (!mrzData.isValid) {
            issues.add("Check digits validation failed")
        }
        
        if (mrzData.passportNumber.isNullOrBlank()) {
            issues.add("Passport number is missing")
        }
        
        if (mrzData.surname.isNullOrBlank()) {
            issues.add("Surname is missing")
        }
        
        if (mrzData.dateOfBirth.isNullOrBlank() || mrzData.dateOfBirth.length != 6) {
            issues.add("Invalid date of birth format")
        }
        
        if (mrzData.dateOfExpiry.isNullOrBlank() || mrzData.dateOfExpiry.length != 6) {
            issues.add("Invalid date of expiry format")
        }
        
        if (mrzData.nationality.isNullOrBlank() || mrzData.nationality.length != 3) {
            issues.add("Invalid nationality code")
        }
        
        return issues
    }
}