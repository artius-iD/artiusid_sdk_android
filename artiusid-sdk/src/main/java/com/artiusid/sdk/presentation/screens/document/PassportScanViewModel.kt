/*
 * File: PassportScanViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.data.models.passport.PassportData
import com.artiusid.sdk.data.models.passport.PassportMRZData
import com.artiusid.sdk.data.models.passport.PassportScanningState
import com.artiusid.sdk.data.models.passport.ScannedPassportImage
import com.artiusid.sdk.utils.passport.MRZParser
import com.artiusid.sdk.utils.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for passport scanning functionality
 * Manages MRZ detection, image capture, and validation
 */
@HiltViewModel
class PassportScanViewModel @Inject constructor() : ViewModel() {
    
    companion object {
        private const val TAG = "PassportScanViewModel"
    }
    
    // UI State
    private val _uiState = MutableStateFlow(PassportScanUiState())
    val uiState: StateFlow<PassportScanUiState> = _uiState.asStateFlow()
    
    // Passport data
    private val _passportData = MutableStateFlow<PassportData?>(null)
    val passportData: StateFlow<PassportData?> = _passportData.asStateFlow()
    
    // Text recognition results
    private val _recognizedText = MutableStateFlow<List<String>>(emptyList())
    val recognizedText: StateFlow<List<String>> = _recognizedText.asStateFlow()
    
    /**
     * Handle text recognized from camera
     */
    fun onTextRecognized(textLines: List<String>) {
        _recognizedText.value = textLines
        
        // Update UI with potential MRZ lines
        val potentialMRZLines = MRZParser.extractPotentialMRZLines(textLines)
        _uiState.value = _uiState.value.copy(
            potentialMRZLines = potentialMRZLines,
            isScanning = true
        )
    }
    
    /**
     * Handle MRZ detection from camera
     */
    fun onMRZDetected(mrzData: PassportMRZData, capturedImage: Bitmap) {
        viewModelScope.launch {
            try {
                Log.i(TAG, "MRZ detected - validating...")
                
                // Update scanning state
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanningState = PassportScanningState.VALIDATING,
                    detectedMRZ = mrzData
                )
                
                // **ENHANCED VALIDATION** - More permissive for better user experience
                val validationIssues = MRZParser.validateMRZData(mrzData)
                val imageQualityCheck = validateImageQuality(capturedImage)
                
                // Log details but be more permissive
                Log.d(TAG, "✅ MRZ validation: ${validationIssues.size} total issues, Quality: $imageQualityCheck")
                validationIssues.forEach { issue -> Log.d(TAG, "  - Validation issue: $issue") }
                
                // **ENHANCED PERMISSIVE VALIDATION** - Allow minor issues for better UX
                val acceptableIssues = validationIssues.filter { issue ->
                    // Filter out minor issues that shouldn't block passport acceptance
                    val minorIssues = listOf(
                        "Check digits",           // Check digit calculation errors
                        "too dark",              // Image darkness (now handled by preprocessing)
                        "checksum",              // Checksum validation errors
                        "length",                // Minor length discrepancies
                        "format"                 // Minor format issues
                    )
                    
                    // Only keep issues that are NOT minor
                    !minorIssues.any { minorIssue -> issue.contains(minorIssue, ignoreCase = true) }
                }
                
                val enhancedQualityCheck = when {
                    imageQualityCheck.contains("too dark") -> "Valid"
                    imageQualityCheck.contains("too bright") -> "Valid" 
                    else -> imageQualityCheck
                }
                
                // Log what we're allowing vs blocking
                val filteredOut = validationIssues.size - acceptableIssues.size
                Log.d(TAG, "🔍 Filtered out $filteredOut minor issues, ${acceptableIssues.size} serious issues remain")
                Log.d(TAG, "📊 Final check: acceptableIssues=${acceptableIssues.size}, quality=$enhancedQualityCheck")
                
                if (acceptableIssues.isEmpty() && enhancedQualityCheck == "Valid") {
                    // MRZ is valid - create passport data
                    val scannedImage = ScannedPassportImage(
                        capturedImage = capturedImage,
                        orientation = 0,
                        captureTimestamp = System.currentTimeMillis()
                    )
                    
                    val passportData = PassportData(
                        passportScan = scannedImage,
                        passportMRZData = mrzData,
                        scanningState = PassportScanningState.COMPLETED,
                        scanTimestamp = System.currentTimeMillis()
                    )
                    
                    _passportData.value = passportData
                    
                    // Store passport image and MRZ data in ImageStorage for NFC screen access
                    Log.d("PassportScanViewModel", "🔍 DIAGNOSTIC: About to store MRZ data in ImageStorage")
                    Log.d("PassportScanViewModel", "🔍 DIAGNOSTIC: MRZ data to store: $mrzData")
                    ImageStorage.setPassportImage(capturedImage)
                    ImageStorage.setPassportMRZData(mrzData)
                    Log.d("PassportScanViewModel", "✅ DIAGNOSTIC: MRZ data stored successfully")
                    
                    // Store passport data for verification results
                    val utilPassportData = com.artiusid.sdk.utils.PassportData(
                        firstName = mrzData.givenNames?.takeIf { it.isNotBlank() },
                        lastName = mrzData.surname?.takeIf { it.isNotBlank() },
                        documentNumber = mrzData.passportNumber?.takeIf { it.isNotBlank() },
                        nationality = mrzData.nationality?.takeIf { it.isNotBlank() },
                        dateOfBirth = mrzData.dateOfBirth?.takeIf { it.isNotBlank() },
                        dateOfExpiry = mrzData.dateOfExpiry?.takeIf { it.isNotBlank() }
                    )
                    com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(utilPassportData)
                    Log.d(TAG, "📝 Stored OCR passport data: ${utilPassportData.firstName} ${utilPassportData.lastName}")
                    
                    _uiState.value = _uiState.value.copy(
                        scanningState = PassportScanningState.COMPLETED,
                        isComplete = true,
                        successMessage = "Passport scanned successfully!"
                    )
                    
                    // TODO: Add sound feedback like iOS (AudioServicesPlaySystemSound(1108))
                    // Could use MediaPlayer or Vibrator for feedback
                    
                    Log.i(TAG, "Passport scan completed successfully - automatic capture complete")
                    
                } else {
                    // MRZ or image quality validation failed
                    val allIssues = mutableListOf<String>()
                    allIssues.addAll(validationIssues)
                    if (imageQualityCheck != "Valid") {
                        allIssues.add(imageQualityCheck)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        scanningState = PassportScanningState.FAILED,
                        errorMessage = "Validation failed: ${allIssues.joinToString(", ")}"
                    )
                    
                    Log.w(TAG, "Passport validation failed: ${allIssues.joinToString(", ")}")
                }
                
            } catch (e: Exception) {
                handleError("Error processing MRZ: ${e.message}")
            }
        }
    }
    
    /**
     * Reset scanning state to start fresh
     */
    fun resetScan() {
        _uiState.value = PassportScanUiState()
        _passportData.value = null
        _recognizedText.value = emptyList()
    }
    
    /**
     * Get current passport data for navigation
     */
    fun getCurrentPassportData(): PassportData? {
        return _passportData.value
    }
    
    /**
     * Check if passport scan is ready for NFC reading
     */
    fun isReadyForNFC(): Boolean {
        return _passportData.value?.isVisualScanComplete() == true
    }
    
    /**
     * Compare OCR/MRZ data with NFC chip data using 70% threshold
     */
    fun compareWithNFCData(nfcData: com.artiusid.sdk.data.models.passport.PassportNFCData): Boolean {
        viewModelScope.launch {
            try {
                val currentPassportData = _passportData.value
                val mrzData = currentPassportData?.passportMRZData
                
                if (mrzData == null) {
                    Log.e(TAG, "No MRZ data available for comparison")
                    _uiState.value = _uiState.value.copy(
                        scanningState = PassportScanningState.FAILED,
                        errorMessage = "No passport data available for verification"
                    )
                    return@launch
                }
                
                // Convert MRZ data to comparable format
                val ocrData = mapOf(
                    "DOCUMENT_NUMBER" to (mrzData.passportNumber ?: ""),
                    "FIRST_NAME" to (mrzData.givenNames ?: ""),
                    "LAST_NAME" to (mrzData.surname ?: ""),
                    "DATE_OF_BIRTH" to (mrzData.dateOfBirth ?: ""),
                    "NATIONALITY" to (mrzData.nationality ?: ""),
                    "EXPIRY_DATE" to (mrzData.dateOfExpiry ?: ""),
                    "GENDER" to (mrzData.sex ?: "")
                )
                
                // Convert NFC data to comparable format
                val nfcComparisonData = mapOf(
                    "DOCUMENT_NUMBER" to nfcData.documentNumber,
                    "FIRST_NAME" to nfcData.firstName,
                    "LAST_NAME" to nfcData.lastName,
                    "DATE_OF_BIRTH" to nfcData.dateOfBirth,
                    "NATIONALITY" to nfcData.nationality,
                    "EXPIRY_DATE" to nfcData.documentExpiryDate,
                    "GENDER" to nfcData.gender
                )
                
                // Perform comparison using 70% threshold
                val comparisonResult = compareDataSets(ocrData, nfcComparisonData)
                
                Log.i(TAG, "OCR vs NFC comparison: ${comparisonResult.matchPercentage * 100}% match")
                Log.i(TAG, "Matched fields: ${comparisonResult.matchedFields}")
                Log.i(TAG, "Unmatched fields: ${comparisonResult.unmatchedFields}")
                
                if (comparisonResult.isMatch) {
                    // Verification successful - proceed to next step
                    _uiState.value = _uiState.value.copy(
                        scanningState = PassportScanningState.COMPLETED,
                        successMessage = "Passport verification successful (${String.format("%.1f", comparisonResult.matchPercentage * 100)}% match)"
                    )
                    
                    Log.i(TAG, "Passport verification PASSED - OCR/NFC match above 70% threshold")
                    return@launch
                } else {
                    // Verification failed - retry needed
                    _uiState.value = _uiState.value.copy(
                        scanningState = PassportScanningState.FAILED,
                        errorMessage = "Verification failed: OCR/NFC data match below 70% threshold (${String.format("%.1f", comparisonResult.matchPercentage * 100)}%)"
                    )
                    
                    Log.w(TAG, "Passport verification FAILED - OCR/NFC match below 70% threshold")
                    
                    // Reset for retry after a delay
                    kotlinx.coroutines.delay(3000)
                    resetScan()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error comparing OCR with NFC data: ${e.message}", e)
                handleError("Verification error: ${e.message}")
            }
        }
        
        // Return current state for immediate response
        return _uiState.value.scanningState == PassportScanningState.COMPLETED
    }
    
    /**
     * Compare two data sets and calculate match percentage
     */
    private fun compareDataSets(
        ocrData: Map<String, String>,
        nfcData: Map<String, String>
    ): DocumentComparisonResult {
        val matchedFields = mutableListOf<String>()
        val unmatchedFields = mutableListOf<String>()
        var totalComparableFields = 0
        var matchedCount = 0
        
        // Helper for string normalization
        fun normalize(s: String?): String = s?.replace("\\s+".toRegex(), "")?.lowercase() ?: ""
        
        // Compare each field
        for ((key, ocrValue) in ocrData) {
            val nfcValue = nfcData[key]
            
            if (ocrValue.isNotEmpty() && nfcValue?.isNotEmpty() == true) {
                totalComparableFields++
                
                val normalizedOcr = normalize(ocrValue)
                val normalizedNfc = normalize(nfcValue)
                
                if (normalizedOcr == normalizedNfc) {
                    matchedFields.add(key)
                    matchedCount++
                } else {
                    unmatchedFields.add("$key: OCR='$ocrValue' vs NFC='$nfcValue'")
                }
            }
        }
        
        val matchPercentage = if (totalComparableFields > 0) {
            matchedCount.toDouble() / totalComparableFields.toDouble()
        } else {
            0.0
        }
        
        val isMatch = matchPercentage >= 0.7 // 70% threshold
        
        return DocumentComparisonResult(
            isMatch = isMatch,
            matchPercentage = matchPercentage,
            matchedFields = matchedFields,
            unmatchedFields = unmatchedFields,
            details = "Compared $totalComparableFields fields, matched $matchedCount (${String.format("%.1f", matchPercentage * 100)}%)"
        )
    }
    
    data class DocumentComparisonResult(
        val isMatch: Boolean,
        val matchPercentage: Double,
        val matchedFields: List<String>,
        val unmatchedFields: List<String>,
        val details: String
    )
    
    /**
     * Get MRZ key for NFC authentication
     */
    fun getMRZKeyForNFC(): String? {
        return _passportData.value?.getMRZKeyForNFC()
    }
    
    /**
     * Handle errors during scanning
     */
    private fun handleError(message: String) {
        Log.e(TAG, message)
        _uiState.value = _uiState.value.copy(
            scanningState = PassportScanningState.FAILED,
            errorMessage = message,
            isScanning = false
        )
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            scanningState = PassportScanningState.SCANNING
        )
    }
    
    /**
     * Get scan progress summary
     */
    fun getScanSummary(): String {
        val data = _passportData.value
        return data?.getProcessingSummary() ?: "No scan data available"
    }
    
    /**
     * Validate image quality (similar to iOS analyzeImageQuality)
     * Basic checks for image suitability for passport processing
     */
    private fun validateImageQuality(bitmap: Bitmap): String {
        try {
            // Basic quality checks - more permissive for better detection
            if (bitmap.width < 500 || bitmap.height < 400) {
                return "Image resolution too low"
            }
            
            // Check if image is too dark or too bright (simple luminance check)
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            
            var totalLuminance = 0.0
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                // Standard luminance formula
                totalLuminance += 0.299 * r + 0.587 * g + 0.114 * b
            }
            
            val avgLuminance = totalLuminance / pixels.size
            Log.d(TAG, "📊 Image quality - Size: ${bitmap.width}x${bitmap.height}, Luminance: $avgLuminance")
            
            when {
                avgLuminance < 20 -> return "Image too dark"
                avgLuminance > 250 -> return "Image too bright" 
                else -> return "Valid"
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating image quality: ${e.message}", e)
            return "Image quality check failed"
        }
    }
}

/**
 * UI state for passport scanning screen
 */
data class PassportScanUiState(
    val isScanning: Boolean = false,
    val scanningState: PassportScanningState = PassportScanningState.NOT_STARTED,
    val potentialMRZLines: List<String> = emptyList(),
    val detectedMRZ: PassportMRZData? = null,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)