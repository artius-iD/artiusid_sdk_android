/*
 * File: AAMVABarcodeParser.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object AAMVABarcodeParser {
    private const val TAG = "AAMVABarcodeParser"
    
    data class AAMVAData(
        val driversLicenseNumber: String? = null,
        val firstName: String? = null,
        val middleName: String? = null,
        val lastName: String? = null,
        val streetAddress: String? = null,
        val cityAddress: String? = null,
        val stateAddress: String? = null,
        val addressCountry: String? = null,
        val zipCode: String? = null,
        val dateOfBirth: String? = null,
        val dateOfIssue: String? = null,
        val dateOfExpiration: String? = null,
        val licenseClass: String? = null,
        val gender: String? = null,
        val eyeColor: String? = null,
        val hairColor: String? = null,
        val heightInInches: String? = null,
        val weight: String? = null,
        val drivingRestrictions: String? = null,
        val documentDiscriminator: String? = null,
        val isRealIDCompliant: Boolean? = null,
        val restrictionsCode: String? = null,
        val hazMatEndorsement: Boolean? = null,
        val cardRevisionID: String? = null
    )
    
    fun parse(barcodeData: String): AAMVAData {
        Log.d(TAG, "Parsing barcode data: ${barcodeData.take(100)}...")
        
        // Remove the leading "@\n" if present (like iOS does)
        val cleanedBarcodeData = barcodeData.replace("@\n", "")
        
        // Check if this is Canadian format (contains %BC and $ delimiters)
        if (cleanedBarcodeData.contains("%BC") && cleanedBarcodeData.contains("$")) {
            Log.d(TAG, "Detected Canadian barcode format, using specialized parser")
            return parseCanadianBarcodeData(cleanedBarcodeData)
        }
        
        Log.d(TAG, "Using US AAMVA format parser")
        
        // Split the barcode data by newline characters (US AAMVA format)
        val lines = cleanedBarcodeData.split("\n")
        val fields = mutableMapOf<String, String>()
        
        // First check if license number is in the header (first line)
        if (lines.isNotEmpty()) {
            val firstLine = lines[0]
            
            // Check for various license number patterns in the header
            // Arizona uses DLDAQL, others use DLDAQ
            for (pattern in listOf("DLDAQ", "DLDAQL")) {
                if (firstLine.contains(pattern)) {
                    val daqIndex = firstLine.indexOf(pattern)
                    if (daqIndex != -1) {
                        val startIndex = daqIndex + pattern.length
                        // Find the license number after pattern by taking characters until non-alphanumeric
                        val licenseEndIndex = firstLine.substring(startIndex).indexOfFirst { 
                            !it.isLetterOrDigit() 
                        }
                        val licenseNumber = if (licenseEndIndex != -1) {
                            firstLine.substring(startIndex, startIndex + licenseEndIndex)
                        } else {
                            firstLine.substring(startIndex)
                        }
                        if (licenseNumber.isNotEmpty()) {
                            fields["DAQ"] = licenseNumber
                            Log.d(TAG, "Found license number from header pattern '$pattern': $licenseNumber")
                            break
                        }
                    }
                }
            }
        }
        
        // Extract field data from standard 3-letter code lines
        val regex = Regex("([A-Z]{3})([^\n]+)")
        regex.findAll(cleanedBarcodeData).forEach { matchResult ->
            val fieldCode = matchResult.groupValues[1]
            val fieldValue = matchResult.groupValues[2].trim()
            fields[fieldCode] = fieldValue
            Log.d(TAG, "Extracted field $fieldCode: $fieldValue")
        }
        
        // Parse the extracted fields into structured data
        return AAMVAData(
            driversLicenseNumber = fields["DAQ"],
            firstName = fields["DAC"],
            middleName = fields["DAD"],
            lastName = fields["DCS"],
            streetAddress = fields["DAG"],
            cityAddress = fields["DAI"],
            stateAddress = fields["DAJ"],
            addressCountry = fields["DAK"],
            zipCode = fields["DAL"],
            dateOfBirth = parseDate(fields["DBB"]),
            dateOfIssue = parseDate(fields["DBD"]),
            dateOfExpiration = parseDate(fields["DBA"]),
            licenseClass = fields["DCA"],
            gender = fields["DBC"],
            eyeColor = fields["DAY"],
            hairColor = fields["DAZ"],
            heightInInches = fields["DAU"],
            weight = fields["DAW"],
            drivingRestrictions = fields["DCD"],
            documentDiscriminator = fields["DCF"],
            isRealIDCompliant = fields["DCG"]?.let { it == "1" },
            restrictionsCode = fields["DAH"],
            hazMatEndorsement = fields["DCI"]?.let { it == "1" },
            cardRevisionID = fields["DCJ"]
        ).also {
            Log.d(TAG, "Parsed AAMVA data: $it")
        }
    }
    
    private fun parseDate(dateString: String?): String? {
        if (dateString.isNullOrEmpty()) return null
        
        return try {
            // Try to parse MMddyyyy format
            if (dateString.length == 8) {
                val month = dateString.substring(0, 2)
                val day = dateString.substring(2, 4)
                val year = dateString.substring(4, 8)
                "$month/$day/$year"
            } else {
                // Return as is if not in expected format
                dateString
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing date '$dateString': ${e.message}")
            dateString
        }
    }
    
    fun logParsedData(data: AAMVAData) {
        val logMessage = """
            BARCODE SCAN RESULTS:
            Full Name: ${data.lastName?.uppercase() ?: "Unknown"}, ${data.firstName ?: ""} ${data.middleName ?: ""}
            License: ${data.driversLicenseNumber ?: "Unknown"}
            Address: ${data.streetAddress ?: ""}, ${data.cityAddress ?: ""}, ${data.stateAddress ?: ""} ${data.zipCode ?: ""}
            DOB: ${data.dateOfBirth ?: "Unknown"} | Issued: ${data.dateOfIssue ?: "Unknown"} | Expires: ${data.dateOfExpiration ?: "Unknown"}
            Physical: Gender: ${data.gender ?: "NONE"}, Height: ${data.heightInInches ?: ""} in, Weight: ${data.weight ?: ""}
            Features: Eyes: ${data.eyeColor ?: ""}, Hair: ${data.hairColor ?: ""}
            Class: ${data.licenseClass ?: "Unknown"} | Restrictions: ${data.restrictionsCode ?: "NONE"}
            REAL ID: ${if (data.isRealIDCompliant == true) "Yes" else "No"} | Document ID: ${data.documentDiscriminator ?: "Unknown"}
        """.trimIndent()
        
        Log.d(TAG, logMessage)
    }
    
    // MARK: - Canadian Barcode Parsing
    private fun parseCanadianBarcodeData(barcodeData: String): AAMVAData {
        Log.d(TAG, "Parsing Canadian barcode format")
        
        // Canadian format example: %BC WEST VANCOUVEJEWELL,$DONALD GORDON^1450 BRAMWELL RD$WEST VANCOUVER BC V7S 2N?;6360283167922=261219531211=?_%0AV7S2N9 M175 82BRNBLU9096421024 RA%,R.PMFBX?
        
        var firstName: String? = null
        var lastName: String? = null
        var streetAddress: String? = null
        var cityAddress: String? = null
        var stateAddress: String? = null
        var zipCode: String? = null
        var driversLicenseNumber: String? = null
        var dateOfBirth: String? = null
        var dateOfExpiration: String? = null
        var gender: String? = null
        var heightInInches: String? = null
        var weight: String? = null
        var eyeColor: String? = null
        var hairColor: String? = null
        
        // 1. Extract name section (between %BC and $)
        extractBetween(barcodeData, "%BC ", "$")?.let { nameSection ->
            // Parse name: "WEST VANCOUVEJEWELL,$DONALD GORDON"
            val nameComponents = nameSection.split(",")
            if (nameComponents.size >= 2) {
                // First part might be city + surname: "WEST VANCOUVEJEWELL"
                // Second part is given name: "$DONALD GORDON"
                val givenNamePart = nameComponents[1].trim().replace("$", "")
                
                // Try to extract surname by finding where city ends
                val firstPart = nameComponents[0]
                // Common Canadian cities to help parse surname
                val cities = listOf("WEST VANCOUVER", "VANCOUVER", "TORONTO", "CALGARY", "EDMONTON", "OTTAWA", "MONTREAL")
                var surname = firstPart
                
                for (cityName in cities) {
                    if (firstPart.startsWith(cityName)) {
                        surname = firstPart.substring(cityName.length)
                        break
                    }
                }
                
                lastName = if (surname.isEmpty()) firstPart else surname
                firstName = givenNamePart
                Log.d(TAG, "Canadian name parsed: $firstName $lastName")
            }
        }
        
        // 2. Extract address (between ^ and $)
        extractBetween(barcodeData, "^", "$")?.let { addressSection ->
            streetAddress = addressSection
            Log.d(TAG, "Canadian address parsed: $addressSection")
        }
        
        // 3. Extract city/province/postal from end section
        extractBetween(barcodeData, "$", "?")?.let { locationSection ->
            // Format: "WEST VANCOUVER BC V7S 2N"
            val locationParts = locationSection.split(" ")
            if (locationParts.size >= 3) {
                // Last 2-3 parts are likely postal code
                val postalParts = locationParts.takeLast(3)
                zipCode = postalParts.joinToString(" ")
                
                // Middle part is likely province
                if (locationParts.size >= 4) {
                    stateAddress = locationParts[locationParts.size - 3]
                    
                    // Remaining parts are city
                    val cityParts = locationParts.take(locationParts.size - 3)
                    cityAddress = cityParts.joinToString(" ")
                }
            }
            Log.d(TAG, "Canadian location parsed: $cityAddress, $stateAddress $zipCode")
        }
        
        // 4. Extract license number (sequence of digits between ; and =)
        extractBetween(barcodeData, ";", "=")?.let { licenseSection ->
            // Extract the first continuous sequence of digits
            val regex = Regex("[0-9]+")
            val matches = regex.findAll(licenseSection)
            for (match in matches) {
                if (match.value.length >= 6) { // Reasonable length for license
                    driversLicenseNumber = match.value
                    Log.d(TAG, "Canadian license number parsed: $driversLicenseNumber")
                    break
                }
            }
        }
        
        // 5. Extract dates (format: 261219531211 = MMDDYYMMDDYY for birth/expiry)
        extractBetween(barcodeData, "=", "=")?.let { dateSection ->
            if (dateSection.length >= 12) {
                // Parse birth date (first 6 digits: MMDDYY)
                val birthDateStr = dateSection.substring(0, 6)
                dateOfBirth = parseCanadianDate(birthDateStr)
                Log.d(TAG, "Canadian birth date parsed: $dateOfBirth")
                
                // Parse expiry date (next 6 digits: MMDDYY)
                val expiryDateStr = dateSection.substring(6, 12)
                dateOfExpiration = parseCanadianDate(expiryDateStr)
                Log.d(TAG, "Canadian expiry date parsed: $dateOfExpiration")
            }
        }
        
        // 6. Extract physical characteristics from end section
        // Format example: "V7S2N9 M175 82BRNBLU9096421024"
        val endSection = barcodeData.split("_%").lastOrNull()
        endSection?.let { section ->
            val components = section.split(" ")
            for (component in components) {
                when {
                    // Gender (M/F)
                    component == "M" || component == "F" -> {
                        gender = if (component == "M") "Male" else "Female"
                    }
                    // Height in cm (4 digits starting with 1)
                    component.length == 4 && component.startsWith("1") && component.all { it.isDigit() } -> {
                        val heightCm = component.toIntOrNull()
                        if (heightCm != null) {
                            // Convert cm to inches
                            val heightInchesValue = heightCm / 2.54
                            heightInInches = String.format("%.0f", heightInchesValue)
                        }
                    }
                    // Weight in kg (2-3 digits)
                    component.length in 2..3 && component.all { it.isDigit() } -> {
                        val weightKg = component.toIntOrNull()
                        if (weightKg != null && weightKg in 30..200) {
                            // Convert kg to lbs
                            val weightLbs = weightKg * 2.20462
                            weight = String.format("%.0f", weightLbs)
                        }
                    }
                    // Eye/Hair color (3 letter codes like BRN, BLU)
                    component.length == 3 && component.all { it.isLetter() } -> {
                        if (eyeColor == null) {
                            eyeColor = component
                        } else if (hairColor == null) {
                            hairColor = component
                        }
                    }
                }
            }
        }
        
        Log.d(TAG, "Canadian barcode parsing completed")
        
        return AAMVAData(
            driversLicenseNumber = driversLicenseNumber,
            firstName = firstName,
            lastName = lastName,
            streetAddress = streetAddress,
            cityAddress = cityAddress,
            stateAddress = stateAddress,
            zipCode = zipCode,
            dateOfBirth = dateOfBirth,
            dateOfExpiration = dateOfExpiration,
            gender = gender,
            heightInInches = heightInInches,
            weight = weight,
            eyeColor = eyeColor,
            hairColor = hairColor
        ).also {
            Log.d(TAG, "Parsed Canadian data: $it")
        }
    }
    
    private fun extractBetween(text: String, start: String, end: String): String? {
        val startIndex = text.indexOf(start)
        if (startIndex == -1) return null
        
        val searchStart = startIndex + start.length
        val endIndex = text.indexOf(end, searchStart)
        if (endIndex == -1) return null
        
        return text.substring(searchStart, endIndex)
    }
    
    private fun parseCanadianDate(dateStr: String): String? {
        if (dateStr.length != 6) return null
        
        return try {
            // Format: MMDDYY
            val mm = dateStr.substring(0, 2)
            val dd = dateStr.substring(2, 4)
            val yy = dateStr.substring(4, 6)
            
            // Convert 2-digit year to 4-digit (assume 20xx for years 00-30, 19xx for 31-99)
            val yyyy = if (yy.toInt() <= 30) "20$yy" else "19$yy"
            
            "$mm/$dd/$yyyy"
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing Canadian date '$dateStr': ${e.message}")
            null
        }
    }
} 