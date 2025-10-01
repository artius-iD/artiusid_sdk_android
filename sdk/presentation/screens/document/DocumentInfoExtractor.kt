/*
 * File: DocumentInfoExtractor.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DocumentInfoExtractor {
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val TAG = "DocumentInfoExtractor"

    suspend fun extractInfo(bitmap: Bitmap, documentType: DocumentType): Map<String, String> = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Starting OCR extraction for document type: $documentType")
            Log.d(TAG, "Input bitmap size: ${bitmap.width}x${bitmap.height}")
            
            val image = InputImage.fromBitmap(bitmap, 0)
            Log.d(TAG, "Created InputImage successfully")
            
            val text = recognizeText(image)
            Log.d(TAG, "Text recognition completed. Text length: ${text.length}")
            Log.d(TAG, "Extracted OCR text: ${text.take(200)}...")
            
            val result = parseDocumentInfo(text, documentType)
            Log.d(TAG, "Document parsing completed. Extracted ${result.size} fields")
            Log.d(TAG, "Final OCR result: $result")
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract document information: ${e.message}", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw DocumentExtractionException("Failed to extract document information: ${e.message}")
        }
    }

    private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    private fun parseDocumentInfo(text: String, documentType: DocumentType): Map<String, String> {
        Log.d(TAG, "Parsing document info for type: $documentType")
        Log.d(TAG, "Raw OCR text: $text")
        val result = mutableMapOf<String, String>()
        result["ALL_TEXT"] = text
        when (documentType) {
            DocumentType.ID_CARD -> {
                val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d(TAG, "Processing ${lines.size} lines of OCR text")
                // Skip header/title lines
                val contentLines = lines.filterNot {
                    it.contains("texas", true) ||
                    it.contains("driver", true) ||
                    it.contains("license", true) ||
                    it.contains("director", true) ||
                    it.matches(Regex("^[0-9]{1,2}\\..*")) // skip numbered instructions
                }
                // Name extraction
                var foundName = false
                for (i in contentLines.indices) {
                    val line = contentLines[i]
                    // Look for LASTNAME (all caps, not a city)
                    if (!foundName && line.matches(Regex("^[A-Z]{2,}( [A-Z]{2,})?$")) && !line.contains("SAN ANTONIO")) {
                        result["LAST_NAME"] = line
                        if (i + 1 < contentLines.size) {
                            val next = contentLines[i + 1]
                            // Look for first/middle name (capitalized words)
                            if (next.matches(Regex("^[A-Z][a-z]+( [A-Z][a-z]+)*$"))) {
                                val names = next.split(" ")
                                result["FIRST_NAME"] = names[0]
                                if (names.size > 1) result["MIDDLE_NAME"] = names.subList(1, names.size).joinToString(" ")
                                foundName = true
                                Log.d(TAG, "Extracted name: ${result["FIRST_NAME"]} ${result["MIDDLE_NAME"] ?: ""} ${result["LAST_NAME"]}")
                            }
                        }
                    }
                }
                // Fallback: try to extract names from ALL_TEXT if not found
                if (!result.containsKey("FIRST_NAME") || !result.containsKey("LAST_NAME")) {
                    val nameRegex = Regex("([A-Z]{2,}), ([A-Z][a-z]+)( [A-Z][a-z]+)?")
                    val match = nameRegex.find(text)
                    if (match != null) {
                        result["LAST_NAME"] = match.groupValues[1]
                        result["FIRST_NAME"] = match.groupValues[2]
                        if (match.groupValues.size > 3) result["MIDDLE_NAME"] = match.groupValues[3].trim()
                        Log.d(TAG, "Fallback name extraction: ${result["FIRST_NAME"]} ${result["MIDDLE_NAME"] ?: ""} ${result["LAST_NAME"]}")
                    }
                }
                // ID Number extraction
                val idRegex = Regex("DL:? ?([0-9]{7,9})")
                val idMatch = idRegex.find(text)
                if (idMatch != null) {
                    result["ID_NUMBER"] = idMatch.groupValues[1]
                    Log.d(TAG, "Extracted ID_NUMBER: ${result["ID_NUMBER"]}")
                } else {
                    // Fallback: look for 7-9 digit number
                    val fallbackId = Regex("\b[0-9]{7,9}\b").find(text)
                    if (fallbackId != null) {
                        result["ID_NUMBER"] = fallbackId.value
                        Log.d(TAG, "Fallback ID_NUMBER: ${result["ID_NUMBER"]}")
                    }
                }
                // Date extraction
                val dobRegex = Regex("DOB:? ?([0-9]{2}/[0-9]{2}/[0-9]{4})")
                val dobMatch = dobRegex.find(text)
                if (dobMatch != null) {
                    result["DATE_OF_BIRTH"] = dobMatch.groupValues[1]
                    Log.d(TAG, "Extracted DATE_OF_BIRTH: ${result["DATE_OF_BIRTH"]}")
                } else {
                    // Fallback: first date in text
                    val dateMatch = Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").find(text)
                    if (dateMatch != null) {
                        result["DATE_OF_BIRTH"] = dateMatch.value
                        Log.d(TAG, "Fallback DATE_OF_BIRTH: ${result["DATE_OF_BIRTH"]}")
                    }
                }
                // Expiry date
                val expRegex = Regex("Exp:? ?([0-9]{2}/[0-9]{2}/[0-9]{4})")
                val expMatch = expRegex.find(text)
                if (expMatch != null) {
                    result["EXPIRY_DATE"] = expMatch.groupValues[1]
                    Log.d(TAG, "Extracted EXPIRY_DATE: ${result["EXPIRY_DATE"]}")
                }
                // Address extraction
                val addressRegex = Regex("([0-9]+ [A-Z0-9 .]+)")
                val addressMatch = addressRegex.find(text)
                if (addressMatch != null) {
                    result["STREET_ADDRESS"] = addressMatch.groupValues[1]
                    Log.d(TAG, "Extracted STREET_ADDRESS: ${result["STREET_ADDRESS"]}")
                }
                // City, State, Zip
                val cityStateZipRegex = Regex("([A-Z ]+), ?([A-Z]{2}) ?([0-9]{5})")
                val cityStateZipMatch = cityStateZipRegex.find(text)
                if (cityStateZipMatch != null) {
                    result["CITY"] = cityStateZipMatch.groupValues[1].trim()
                    result["STATE"] = cityStateZipMatch.groupValues[2]
                    result["ZIP_CODE"] = cityStateZipMatch.groupValues[3].take(5)
                    Log.d(TAG, "Extracted CITY: ${result["CITY"]}, STATE: ${result["STATE"]}, ZIP: ${result["ZIP_CODE"]}")
                }
                // Fallback: extract zip code from any 5-digit number
                if (!result.containsKey("ZIP_CODE")) {
                    val zipMatch = Regex("\b[0-9]{5}\b").find(text)
                    if (zipMatch != null) {
                        result["ZIP_CODE"] = zipMatch.value
                        Log.d(TAG, "Fallback ZIP_CODE: ${result["ZIP_CODE"]}")
                    }
                }
            }
            else -> {}
        }
        return result
    }

    private fun parsePassport(text: String): Map<String, String> {
        val info = mutableMapOf<String, String>()
        
        // Passport number pattern
        val passportPattern = Pattern.compile("([A-Z0-9]{6,9})", Pattern.CASE_INSENSITIVE)
        val passportMatcher = passportPattern.matcher(text)
        if (passportMatcher.find()) {
            val passportNumber = passportMatcher.group(1)?.trim()
            if (!passportNumber.isNullOrEmpty()) {
                info["Passport Number"] = passportNumber
            }
        }
        
        // Name pattern for passport
        val namePattern = Pattern.compile("Name:\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE)
        val nameMatcher = namePattern.matcher(text)
        if (nameMatcher.find()) {
            val name = nameMatcher.group(1)?.trim()
            if (!name.isNullOrEmpty()) {
                info["Name"] = name
            }
        }
        
        // Date of Birth pattern
        val dobPattern = Pattern.compile("(?:DOB|Date of Birth):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE)
        val dobMatcher = dobPattern.matcher(text)
        if (dobMatcher.find()) {
            val dob = dobMatcher.group(1)?.trim()
            if (!dob.isNullOrEmpty()) {
                info["Date of Birth"] = dob
            }
        }
        
        // Expiry Date pattern
        val expPattern = Pattern.compile("(?:EXP|Expiry Date):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE)
        val expMatcher = expPattern.matcher(text)
        if (expMatcher.find()) {
            val expDate = expMatcher.group(1)?.trim()
            if (!expDate.isNullOrEmpty()) {
                info["Expiry Date"] = expDate
            }
        }
        
        return info
    }
}

class DocumentExtractionException(message: String) : Exception(message) 