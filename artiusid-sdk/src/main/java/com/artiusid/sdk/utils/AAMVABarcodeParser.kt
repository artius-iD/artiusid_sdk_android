package com.artiusid.sdk.utils

import com.artiusid.sdk.utils.*

/**
 * Parser for AAMVA (American Association of Motor Vehicle Administrators) barcodes
 * Used for parsing driver's license barcodes (PDF417)
 */
object AAMVABarcodeParser {
    
    /**
     * Parse AAMVA barcode data
     */
    fun parseBarcode(barcodeData: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        try {
            // AAMVA barcode format parsing
            val lines = barcodeData.split("\n")
            
            for (line in lines) {
                if (line.length >= 3) {
                    val elementId = line.substring(0, 3)
                    val elementData = if (line.length > 3) line.substring(3) else ""
                    
                    when (elementId) {
                        "DCS" -> result["lastName"] = elementData
                        "DCT" -> result["firstName"] = elementData
                        "DCU" -> result["middleName"] = elementData
                        "DBD" -> result["issueDate"] = elementData
                        "DBA" -> result["expirationDate"] = elementData
                        "DBC" -> result["gender"] = elementData
                        "DBB" -> result["dateOfBirth"] = elementData
                        "DAY" -> result["eyeColor"] = elementData
                        "DAU" -> result["height"] = elementData
                        "DAG" -> result["address1"] = elementData
                        "DAI" -> result["city"] = elementData
                        "DAJ" -> result["state"] = elementData
                        "DAK" -> result["zipCode"] = elementData
                        "DAQ" -> result["licenseNumber"] = elementData
                        "DCF" -> result["documentDiscriminator"] = elementData
                        "DCG" -> result["issuingCountry"] = elementData
                        "DCH" -> result["federalCommercialVehicleCodes"] = elementData
                        "DCI" -> result["placeOfBirth"] = elementData
                        "DCJ" -> result["auditInformation"] = elementData
                        "DCK" -> result["inventoryControlNumber"] = elementData
                        "DCL" -> result["raceEthnicity"] = elementData
                        "DCM" -> result["standardVehicleClassification"] = elementData
                        "DCN" -> result["standardEndorsementCode"] = elementData
                        "DCO" -> result["standardRestrictionCode"] = elementData
                        "DCP" -> result["jurisdictionVehicleClassificationDescription"] = elementData
                        "DCQ" -> result["jurisdictionEndorsementCodeDescription"] = elementData
                        "DCR" -> result["jurisdictionRestrictionCodeDescription"] = elementData
                        "DCS" -> result["familyName"] = elementData
                        "DCT" -> result["givenName"] = elementData
                        "DCU" -> result["suffixName"] = elementData
                        "DDA" -> result["complianceType"] = elementData
                        "DDB" -> result["cardRevisionDate"] = elementData
                        "DDC" -> result["hazmatEndorsementExpirationDate"] = elementData
                        "DDD" -> result["limitedDurationDocumentIndicator"] = elementData
                        "DDE" -> result["familyNameTruncation"] = elementData
                        "DDF" -> result["givenNameTruncation"] = elementData
                        "DDG" -> result["middleNameTruncation"] = elementData
                        "DDH" -> result["under18Until"] = elementData
                        "DDI" -> result["under19Until"] = elementData
                        "DDJ" -> result["under21Until"] = elementData
                        "DDK" -> result["organDonorIndicator"] = elementData
                        "DDL" -> result["veteranIndicator"] = elementData
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AAMVABarcodeParser", "Error parsing barcode: ${e.message}")
        }
        
        return result
    }
    
    /**
     * Format parsed data for display
     */
    fun formatParsedData(parsedData: Map<String, String>): String {
        val sb = StringBuilder()
        
        parsedData["firstName"]?.let { sb.append("First Name: $it\n") }
        parsedData["lastName"]?.let { sb.append("Last Name: $it\n") }
        parsedData["middleName"]?.let { sb.append("Middle Name: $it\n") }
        parsedData["dateOfBirth"]?.let { sb.append("Date of Birth: $it\n") }
        parsedData["gender"]?.let { sb.append("Gender: $it\n") }
        parsedData["licenseNumber"]?.let { sb.append("License Number: $it\n") }
        parsedData["address1"]?.let { sb.append("Address: $it\n") }
        parsedData["city"]?.let { sb.append("City: $it\n") }
        parsedData["state"]?.let { sb.append("State: $it\n") }
        parsedData["zipCode"]?.let { sb.append("ZIP Code: $it\n") }
        parsedData["issueDate"]?.let { sb.append("Issue Date: $it\n") }
        parsedData["expirationDate"]?.let { sb.append("Expiration Date: $it\n") }
        
        return sb.toString()
    }
}