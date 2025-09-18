/*
 * File: DocumentComparisonManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.util.Log
import java.util.*

class DocumentComparisonManager {
    companion object {
        private const val TAG = "DocumentComparisonManager"
        private const val MIN_MATCH_PERCENTAGE = 0.7 // 70% match required
    }
    
    data class ComparisonResult(
        val isMatch: Boolean,
        val matchPercentage: Double,
        val matchedFields: List<String>,
        val unmatchedFields: List<String>,
        val details: String
    )
    
    fun compareOCRWithBarcode(
        ocrData: Map<String, String>,
        barcodeData: AAMVABarcodeParser.AAMVAData
    ): ComparisonResult {
        Log.d(TAG, "[COMPARE] Starting OCR vs Barcode comparison")
        Log.d(TAG, "[COMPARE] OCR data: $ocrData")
        Log.d(TAG, "[COMPARE] Barcode data: $barcodeData")
        val matchedFields = mutableListOf<String>()
        val unmatchedFields = mutableListOf<String>()
        var totalComparableFields = 0
        var matchedCount = 0
        // Helper for string normalization
        fun norm(s: String?): String = s?.replace("\\s+".toRegex(), "")?.lowercase() ?: ""
        // Compare key fields
        val fieldMappings = listOf(
            Triple("FIRST_NAME", barcodeData.firstName, "First Name"),
            Triple("LAST_NAME", barcodeData.lastName, "Last Name"),
            Triple("ID_NUMBER", barcodeData.driversLicenseNumber, "ID Number"),
            Triple("DATE_OF_BIRTH", barcodeData.dateOfBirth, "Date of Birth"),
            Triple("EXPIRY_DATE", barcodeData.dateOfExpiration, "Expiry Date"),
            Triple("STREET_ADDRESS", barcodeData.streetAddress, "Street Address"),
            Triple("CITY", barcodeData.cityAddress, "City"),
            Triple("STATE", barcodeData.stateAddress, "State"),
            Triple("ZIP_CODE", barcodeData.zipCode?.take(5), "Zip Code")
        )
        for ((ocrKey, barcodeValue, label) in fieldMappings) {
            val ocrValue = ocrData[ocrKey]
            if (barcodeValue != null && ocrValue != null) {
                totalComparableFields++
                val ocrNorm = norm(ocrValue)
                val barcodeNorm = norm(barcodeValue)
                if (ocrKey == "ZIP_CODE") {
                    // Compare only first 5 digits
                    if (ocrNorm.take(5) == barcodeNorm.take(5)) {
                        matchedFields.add(label)
                        matchedCount++
                        Log.d(TAG, "✅ $label matches: '$ocrValue' vs '$barcodeValue'")
                    } else {
                        unmatchedFields.add(label)
                        Log.d(TAG, "❌ $label does not match: '$ocrValue' vs '$barcodeValue'")
                    }
                } else if (ocrNorm == barcodeNorm) {
                    matchedFields.add(label)
                    matchedCount++
                    Log.d(TAG, "✅ $label matches: '$ocrValue' vs '$barcodeValue'")
                } else {
                    unmatchedFields.add(label)
                    Log.d(TAG, "❌ $label does not match: '$ocrValue' vs '$barcodeValue'")
                }
            } else if (barcodeValue != null) {
                // Try fallback: search ALL_TEXT for barcode value
                val allText = ocrData["ALL_TEXT"] ?: ""
                if (barcodeValue.isNotBlank() && allText.contains(barcodeValue, ignoreCase = true)) {
                    matchedFields.add(label)
                    matchedCount++
                    Log.d(TAG, "⚠️ $label fallback match in ALL_TEXT: '$barcodeValue'")
                } else {
                    unmatchedFields.add(label)
                    Log.d(TAG, "⚠️ $label not comparable: OCR='$ocrValue', Barcode='$barcodeValue'")
                }
            }
        }
        val matchPercentage = if (totalComparableFields > 0) matchedCount.toDouble() / totalComparableFields else 0.0
        val details = buildString {
            appendLine("Document Comparison Results:")
            appendLine("Match Percentage: ${"%.0f".format(matchPercentage * 100)}% ($matchedCount/$totalComparableFields)")
            appendLine("Required: 70%")
            appendLine("Result: ${if (matchPercentage >= 0.7) "MATCH" else "NO MATCH"}")
            appendLine()
            appendLine("Field Details:")
            for ((ocrKey, barcodeValue, label) in fieldMappings) {
                if (matchedFields.contains(label)) {
                    appendLine("  $label: ✅ MATCH")
                } else if (unmatchedFields.contains(label)) {
                    appendLine("  $label: ❌ NO MATCH")
                    appendLine("    OCR: '${ocrData[ocrKey] ?: ""}'")
                    appendLine("    Barcode: '${barcodeValue ?: ""}'")
                } else {
                    appendLine("  $label: NOT COMPARABLE")
                }
            }
        }
        Log.d(TAG, "[COMPARE] Matched fields: $matchedFields")
        Log.d(TAG, "[COMPARE] Unmatched fields: $unmatchedFields")
        Log.d(TAG, "[COMPARE] Match percentage: $matchPercentage")
        Log.d(TAG, "[COMPARE] Comparison result: $details")
        return ComparisonResult(
            isMatch = matchPercentage >= 0.7,
            matchPercentage = matchPercentage,
            matchedFields = matchedFields,
            unmatchedFields = unmatchedFields,
            details = details
        )
    }
    
    private data class FieldMapping(
        val ocrKey: String,
        val barcodeValue: String?,
        val fieldName: String
    )
    
    private fun buildComparisonDetails(
        matchedFields: List<String>,
        unmatchedFields: List<String>,
        matchPercentage: Double,
        fieldMappings: List<FieldMapping>,
        ocrData: Map<String, String>,
        barcodeData: AAMVABarcodeParser.AAMVAData
    ): String {
        val details = StringBuilder()
        details.appendLine("Document Comparison Results:")
        details.appendLine("Match Percentage: ${(matchPercentage * 100).toInt()}% (${matchedFields.size}/${fieldMappings.size})")
        details.appendLine("Required: ${(MIN_MATCH_PERCENTAGE * 100).toInt()}%")
        details.appendLine("Result: ${if (matchPercentage >= MIN_MATCH_PERCENTAGE) "MATCH" else "NO MATCH"}")
        details.appendLine()
        details.appendLine("Field Details:")
        
        fieldMappings.forEach { mapping ->
            val ocrValue = ocrData[mapping.ocrKey]
            val barcodeValue = mapping.barcodeValue
            
            val status = when {
                ocrValue == null && barcodeValue == null -> "NOT COMPARABLE (Both null)"
                ocrValue == null -> "❌ NO MATCH (Barcode only)"
                barcodeValue == null -> "❌ NO MATCH (OCR only)"
                ocrValue == barcodeValue -> "✅ MATCH"
                else -> "❌ NO MATCH"
            }
            
            details.appendLine("  ${mapping.fieldName}: $status")
            if (status == "❌ NO MATCH") {
                details.appendLine("    OCR: '${ocrValue ?: "N/A"}'")
                details.appendLine("    Barcode: '${barcodeValue ?: "N/A"}'")
            }
        }
        
        return details.toString()
    }
} 