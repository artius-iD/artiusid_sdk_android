package com.artiusid.sdk.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.artiusid.sdk.models.*
import com.artiusid.sdk.models.DocumentScanResult as ModelsDocumentScanResult
import com.artiusid.sdk.data.models.MRZData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.artiusid.sdk.utils.AAMVABarcodeParser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for document scanning, OCR, and MRZ parsing
 */
class DocumentScanService(private val context: Context) {
    
    companion object {
        private const val TAG = "DocumentScanService"
        
        // MRZ patterns for different document types
        private val PASSPORT_MRZ_PATTERN = Pattern.compile(
            "P<([A-Z]{3})([A-Z<]+)<<([A-Z<]+)<*\\n" +
            "([A-Z0-9<]{9})([0-9])([A-Z]{3})([0-9]{6})([0-9])([MF<])([0-9]{6})([0-9])([A-Z0-9<]{14})([0-9])"
        )
        
        private val ID_CARD_MRZ_PATTERN = Pattern.compile(
            "I<([A-Z]{3})([A-Z0-9<]{9})([0-9])([A-Z0-9<]{15})\\n" +
            "([0-9]{6})([0-9])([MF<])([0-9]{6})([0-9])([A-Z]{3})([A-Z0-9<]{11})([0-9])"
        )
    }
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val enhancedBarcodeService = EnhancedBarcodeService(context)
    
    /**
     * Scan document and extract data
     */
    suspend fun scanDocument(
        bitmap: Bitmap,
        documentType: DocumentType
    ): ModelsDocumentScanResult {
        val startTime = System.currentTimeMillis()
        return try {
            Log.d(TAG, "Starting document scan for type: $documentType")
            
            // Try barcode scanning first for driver's licenses
            if (documentType == DocumentType.DRIVERS_LICENSE) {
                Log.d(TAG, "Attempting barcode scan for driver's license")
                val barcodeResult = enhancedBarcodeService.scanBarcode(bitmap, documentType.name)
                
                if (barcodeResult.success && barcodeResult.aamvaData != null) {
                    Log.d(TAG, "Barcode scan successful, using AAMVA data")
                    val aamvaMap = mapOf(
                        "firstName" to (barcodeResult.aamvaData?.firstName ?: ""),
                        "lastName" to (barcodeResult.aamvaData?.lastName ?: ""),
                        "middleName" to (barcodeResult.aamvaData?.middleName ?: ""),
                        "dateOfBirth" to (barcodeResult.aamvaData?.dateOfBirth ?: ""),
                        "gender" to (barcodeResult.aamvaData?.sex ?: ""),
                        "licenseNumber" to (barcodeResult.aamvaData?.licenseNumber ?: ""),
                        "address" to (barcodeResult.aamvaData?.address ?: ""),
                        "city" to (barcodeResult.aamvaData?.city ?: ""),
                        "state" to (barcodeResult.aamvaData?.state ?: ""),
                        "zipCode" to (barcodeResult.aamvaData?.zipCode ?: ""),
                        "issueDate" to (barcodeResult.aamvaData?.issueDate ?: ""),
                        "expirationDate" to (barcodeResult.aamvaData?.expirationDate ?: "")
                    )
                    
                    return ModelsDocumentScanResult(
                        success = true,
                        documentType = documentType.name,
                        frontImage = null,
                        backImage = null,
                        extractedData = aamvaMap,
                        confidence = barcodeResult.confidence,
                        processingTime = barcodeResult.processingTime,
                        sessionId = "barcode_scan_${System.currentTimeMillis()}",
                        errorMessage = null,
                        barcodeData = barcodeResult.barcodeData ?: ""
                    )
                } else {
                    Log.d(TAG, "Barcode scan failed, falling back to OCR")
                }
            }
            
            // Fall back to OCR processing
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = textRecognizer.process(inputImage).await()
            
            Log.d(TAG, "OCR completed, processing text...")
            
            val extractedData = when (documentType) {
                DocumentType.PASSPORT -> extractPassportData(visionText)
                DocumentType.ID_CARD, DocumentType.DRIVERS_LICENSE -> extractIDCardData(visionText)
                else -> extractGenericDocumentData(visionText)
            }
            
            val confidence = calculateConfidence(extractedData, visionText)
            
            ModelsDocumentScanResult(
                success = extractedData.isNotEmpty(),
                frontImage = null,
                backImage = null,
                documentType = documentType.name,
                extractedData = extractedData,
                confidence = confidence,
                processingTime = System.currentTimeMillis() - startTime,
                sessionId = "doc_scan_${System.currentTimeMillis()}",
                errorMessage = if (extractedData.isEmpty()) "No data extracted" else null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Document scan failed", e)
            ModelsDocumentScanResult(
                success = false,
                frontImage = null,
                backImage = null,
                documentType = documentType.name,
                extractedData = emptyMap(),
                confidence = 0.0f,
                processingTime = System.currentTimeMillis() - startTime,
                sessionId = "doc_scan_${System.currentTimeMillis()}",
                errorMessage = "Document scan failed: ${e.message}"
            )
        }
    }
    
    /**
     * Extract passport data including MRZ parsing
     */
    private fun extractPassportData(visionText: Text): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        val fullText = visionText.text
        
        Log.d(TAG, "Extracting passport data from text: $fullText")
        
        // Try to find MRZ in the text
        val mrzData = parseMRZ(fullText, DocumentType.PASSPORT)
        if (mrzData.isValid) {
            extractedData.putAll(mrzData.toMap())
            Log.d(TAG, "MRZ data extracted successfully")
        } else {
            // Fallback to OCR-based extraction
            Log.d(TAG, "MRZ parsing failed, using OCR fallback")
            extractedData.putAll(extractPassportDataFromOCR(visionText))
        }
        
        return extractedData
    }
    
    /**
     * Extract ID card data
     */
    private fun extractIDCardData(visionText: Text): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        val fullText = visionText.text
        
        Log.d(TAG, "Extracting ID card data from text: $fullText")
        
        // Try MRZ first
        val mrzData = parseMRZ(fullText, DocumentType.ID_CARD)
        if (mrzData.isValid) {
            extractedData.putAll(mrzData.toMap())
        } else {
            // Fallback to OCR-based extraction
            extractedData.putAll(extractIDCardDataFromOCR(visionText))
        }
        
        return extractedData
    }
    
    /**
     * Extract generic document data
     */
    private fun extractGenericDocumentData(visionText: Text): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        
        // Extract common patterns
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val text = line.text
                
                // Look for common document patterns
                when {
                    text.contains("NAME", ignoreCase = true) -> {
                        extractedData["name"] = extractValueAfterLabel(text, "NAME")
                    }
                    text.contains("DOB", ignoreCase = true) || text.contains("BIRTH", ignoreCase = true) -> {
                        extractedData["dateOfBirth"] = extractDateFromText(text)
                    }
                    text.contains("EXP", ignoreCase = true) || text.contains("EXPIRES", ignoreCase = true) -> {
                        extractedData["expirationDate"] = extractDateFromText(text)
                    }
                    text.matches(Regex("\\d{8,9}")) -> {
                        extractedData["documentNumber"] = text
                    }
                }
            }
        }
        
        return extractedData
    }
    
    /**
     * Parse MRZ (Machine Readable Zone) data
     */
    private fun parseMRZ(text: String, documentType: DocumentType): MRZData {
        Log.d(TAG, "Parsing MRZ for document type: $documentType")
        
        // Clean and normalize text for MRZ parsing
        val cleanText = text.replace(" ", "").replace("\r", "\n")
        val lines = cleanText.split("\n").filter { it.isNotEmpty() }
        
        return when (documentType) {
            DocumentType.PASSPORT -> parsePassportMRZ(lines)
            DocumentType.ID_CARD -> parseIDCardMRZ(lines)
            else -> MRZData()
        }
    }
    
    /**
     * Parse passport MRZ (2 lines)
     */
    private fun parsePassportMRZ(lines: List<String>): MRZData {
        // Find potential MRZ lines (should be 44 characters each)
        val mrzLines = lines.filter { it.length >= 40 && it.all { char -> char.isLetterOrDigit() || char == '<' } }
        
        if (mrzLines.size < 2) {
            Log.d(TAG, "Not enough MRZ lines found for passport")
            return MRZData()
        }
        
        return try {
            val line1 = mrzLines[0].padEnd(44, '<')
            val line2 = mrzLines[1].padEnd(44, '<')
            
            Log.d(TAG, "Parsing passport MRZ lines:")
            Log.d(TAG, "Line 1: $line1")
            Log.d(TAG, "Line 2: $line2")
            
            // Parse line 1: P<COUNTRY_CODE<SURNAME<<GIVEN_NAMES<<<<<<<<<<<<<<<
            val documentType = line1.substring(0, 1)
            val issuingCountry = line1.substring(2, 5)
            val nameSection = line1.substring(5)
            val names = nameSection.split("<<")
            val surname = names.getOrNull(0)?.replace("<", " ")?.trim() ?: ""
            val givenNames = names.getOrNull(1)?.replace("<", " ")?.trim() ?: ""
            
            // Parse line 2: DOCUMENT_NUMBER<CHECK<NATIONALITY<DOB<CHECK<SEX<EXPIRY<CHECK<PERSONAL_NUMBER<CHECK
            val documentNumber = line2.substring(0, 9).replace("<", "")
            val nationality = line2.substring(10, 13)
            val dateOfBirth = formatMRZDate(line2.substring(13, 19))
            val sex = line2.substring(20, 21)
            val expirationDate = formatMRZDate(line2.substring(21, 27))
            val personalNumber = line2.substring(28, 42).replace("<", "")
            
            MRZData(
                documentType = documentType,
                countryCode = issuingCountry,
                documentNumber = documentNumber,
                nationality = nationality,
                dateOfBirth = dateOfBirth,
                sex = sex,
                expirationDate = expirationDate,
                personalNumber = personalNumber,
                surname = surname,
                givenNames = givenNames,
                isValid = true
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse passport MRZ", e)
            MRZData()
        }
    }
    
    /**
     * Parse ID card MRZ (2 lines)
     */
    private fun parseIDCardMRZ(lines: List<String>): MRZData {
        // Similar to passport but different format
        val mrzLines = lines.filter { it.length >= 30 && it.all { char -> char.isLetterOrDigit() || char == '<' } }
        
        if (mrzLines.size < 2) {
            Log.d(TAG, "Not enough MRZ lines found for ID card")
            return MRZData()
        }
        
        return try {
            val line1 = mrzLines[0].padEnd(30, '<')
            val line2 = mrzLines[1].padEnd(30, '<')
            
            Log.d(TAG, "Parsing ID card MRZ lines:")
            Log.d(TAG, "Line 1: $line1")
            Log.d(TAG, "Line 2: $line2")
            
            // Parse based on ID card MRZ format
            val documentNumber = line1.substring(5, 14).replace("<", "")
            val issuingCountry = line1.substring(2, 5)
            val dateOfBirth = formatMRZDate(line2.substring(0, 6))
            val sex = line2.substring(7, 8)
            val expirationDate = formatMRZDate(line2.substring(8, 14))
            val nationality = line2.substring(15, 18)
            
            MRZData(
                documentType = "I",
                countryCode = issuingCountry,
                documentNumber = documentNumber,
                nationality = nationality,
                dateOfBirth = dateOfBirth,
                sex = sex,
                expirationDate = expirationDate,
                isValid = true
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ID card MRZ", e)
            MRZData()
        }
    }
    
    /**
     * Extract passport data from OCR (fallback when MRZ fails)
     */
    private fun extractPassportDataFromOCR(visionText: Text): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val text = line.text.uppercase()
                
                when {
                    text.contains("PASSPORT") -> extractedData["documentType"] = "PASSPORT"
                    text.contains("SURNAME") -> extractedData["surname"] = extractValueAfterLabel(text, "SURNAME")
                    text.contains("GIVEN NAME") -> extractedData["givenNames"] = extractValueAfterLabel(text, "GIVEN NAME")
                    text.contains("NATIONALITY") -> extractedData["nationality"] = extractValueAfterLabel(text, "NATIONALITY")
                    text.contains("DATE OF BIRTH") -> extractedData["dateOfBirth"] = extractDateFromText(text)
                    text.contains("DATE OF EXPIRY") -> extractedData["expirationDate"] = extractDateFromText(text)
                    text.matches(Regex("P\\d{8}")) -> extractedData["documentNumber"] = text
                }
            }
        }
        
        return extractedData
    }
    
    /**
     * Extract ID card data from OCR
     */
    private fun extractIDCardDataFromOCR(visionText: Text): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val text = line.text.uppercase()
                
                when {
                    text.contains("DRIVER") || text.contains("LICENSE") -> extractedData["documentType"] = "DRIVERS_LICENSE"
                    text.contains("ID") -> extractedData["documentType"] = "ID_CARD"
                    text.contains("NAME") -> extractedData["name"] = extractValueAfterLabel(text, "NAME")
                    text.contains("DOB") -> extractedData["dateOfBirth"] = extractDateFromText(text)
                    text.contains("EXP") -> extractedData["expirationDate"] = extractDateFromText(text)
                    text.matches(Regex("\\d{8,12}")) -> extractedData["documentNumber"] = text
                }
            }
        }
        
        return extractedData
    }
    
    /**
     * Helper functions
     */
    private fun extractValueAfterLabel(text: String, label: String): String {
        val index = text.indexOf(label, ignoreCase = true)
        if (index != -1) {
            val afterLabel = text.substring(index + label.length).trim()
            return afterLabel.split(" ").firstOrNull()?.trim() ?: ""
        }
        return ""
    }
    
    private fun extractDateFromText(text: String): String {
        // Look for date patterns: DD/MM/YYYY, DD-MM-YYYY, DDMMYYYY
        val datePatterns = listOf(
            Regex("\\d{2}/\\d{2}/\\d{4}"),
            Regex("\\d{2}-\\d{2}-\\d{4}"),
            Regex("\\d{8}")
        )
        
        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return formatDate(match.value)
            }
        }
        return ""
    }
    
    private fun formatDate(date: String): String {
        return when {
            date.length == 8 && date.all { it.isDigit() } -> {
                // DDMMYYYY -> DD/MM/YYYY
                "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4, 8)}"
            }
            else -> date
        }
    }
    
    private fun formatMRZDate(mrzDate: String): String {
        return if (mrzDate.length == 6 && mrzDate.all { it.isDigit() }) {
            // YYMMDD -> DD/MM/YYYY (assuming 20XX for years)
            val year = "20${mrzDate.substring(0, 2)}"
            val month = mrzDate.substring(2, 4)
            val day = mrzDate.substring(4, 6)
            "$day/$month/$year"
        } else {
            mrzDate
        }
    }
    
    private fun calculateConfidence(extractedData: Map<String, String>, visionText: Text): Float {
        var confidence = 0.0f
        
        // Base confidence from OCR
        confidence += 0.3f
        
        // Bonus for each extracted field
        confidence += extractedData.size * 0.1f
        
        // Bonus for MRZ detection
        if (extractedData.containsKey("documentNumber") && extractedData.containsKey("dateOfBirth")) {
            confidence += 0.3f
        }
        
        // Bonus for text quality
        val totalBlocks = visionText.textBlocks.size
        if (totalBlocks > 5) {
            confidence += 0.2f
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Release resources
     */
    fun release() {
        textRecognizer.close()
        enhancedBarcodeService.release()
    }
}

/**
 * Extension function to convert MRZData to Map
 */
private fun MRZData.toMap(): Map<String, String> {
    return mapOf(
        "documentType" to (documentType ?: ""),
        "issuingCountry" to (countryCode ?: ""),
        "documentNumber" to (documentNumber ?: ""),
        "nationality" to (nationality ?: ""),
        "dateOfBirth" to (dateOfBirth ?: ""),
        "sex" to (sex ?: ""),
        "expirationDate" to (expirationDate ?: ""),
        "personalNumber" to (personalNumber ?: ""),
        "surname" to (surname ?: ""),
        "givenNames" to (givenNames ?: "")
    ).filterValues { it.isNotEmpty() }
}
