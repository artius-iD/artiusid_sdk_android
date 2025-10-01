/*
 * File: DocumentScanViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.utils.BarcodeScanManager
import com.artiusid.sdk.utils.BarcodeScanResult
import com.artiusid.sdk.utils.DocumentScanManager
import com.artiusid.sdk.utils.DocumentScanResult
import com.artiusid.sdk.utils.DocumentSide
import com.artiusid.sdk.utils.ImageStorage
import com.artiusid.sdk.utils.AAMVABarcodeParser
import com.artiusid.sdk.utils.DocumentComparisonManager
import com.artiusid.sdk.presentation.screens.document.DocumentInfoExtractor
import com.artiusid.sdk.presentation.screens.document.DocumentType
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class DocumentScanViewModel @Inject constructor(
    private val documentScanManager: DocumentScanManager,
    private val barcodeScanManager: BarcodeScanManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocumentScanUiState>(DocumentScanUiState.Initial)
    val uiState: StateFlow<DocumentScanUiState> = _uiState.asStateFlow()

    private val _previewState = MutableStateFlow<Preview?>(null)
    val previewState: StateFlow<Preview?> = _previewState.asStateFlow()

    private val _validationMessage = MutableStateFlow<String>("Position your ID card in the frame")
    val validationMessage: StateFlow<String> = _validationMessage.asStateFlow()

    private val _isProcessingComplete = MutableStateFlow(false)
    val isProcessingComplete: StateFlow<Boolean> = _isProcessingComplete.asStateFlow()

    private var isCurrentlyProcessing = false // Flag to prevent multiple processing

    private val _documentSide = MutableStateFlow<DocumentSide>(DocumentSide.FRONT)
    val documentSide: StateFlow<DocumentSide> = _documentSide.asStateFlow()

    // New fields for OCR and comparison
    private val documentInfoExtractor = DocumentInfoExtractor()
    private val comparisonManager = DocumentComparisonManager()
    private var frontOcrData: Map<String, String>? = null
    private var barcodeData: AAMVABarcodeParser.AAMVAData? = null
    private var hasRetriedScan = false // Track if we've already retried
    private var lastBackScanProcessTime = 0L // Track timing for back scan throttling

    fun setDocumentSide(side: DocumentSide) {
        _documentSide.value = side
        resetScan()
    }

    fun processDocumentImage(bitmap: Bitmap) {
        if (isCurrentlyProcessing) {
            android.util.Log.d("DocumentScanViewModel", "Already processing, skipping duplicate call")
            return
        }
        
        isCurrentlyProcessing = true
        android.util.Log.d("DocumentScanViewModel", "Processing document image for side: ${_documentSide.value}")
        
        viewModelScope.launch {
            try {
                val documentResult = documentScanManager.scanDocument(bitmap, _documentSide.value)
                android.util.Log.d("DocumentScanViewModel", "Document validation result: ${documentResult.validationStatus}")
                
                _validationMessage.value = documentResult.validationStatus
                when (documentResult.validationStatus) {
                    "Valid", "Document detected" -> {
                        android.util.Log.d("DocumentScanViewModel", "Front scan validation successful (${documentResult.validationStatus}), saving image and extracting OCR")
                        
                        // Store the front image for verification
                        ImageStorage.setFrontImage(bitmap)
                        android.util.Log.d("DocumentScanViewModel", "Front image saved to ImageStorage")
                        
                        // Extract OCR data from front image
                        android.util.Log.d("DocumentScanViewModel", "About to start OCR extraction...")
                        try {
                            android.util.Log.d("DocumentScanViewModel", "Extracting OCR data from front image")
                            android.util.Log.d("DocumentScanViewModel", "Bitmap size: ${bitmap.width}x${bitmap.height}")
                            android.util.Log.d("DocumentScanViewModel", "DocumentType: ${DocumentType.ID_CARD}")
                            
                            android.util.Log.d("DocumentScanViewModel", "Calling documentInfoExtractor.extractInfo...")
                            frontOcrData = documentInfoExtractor.extractInfo(bitmap, DocumentType.ID_CARD)
                            android.util.Log.d("DocumentScanViewModel", "OCR extraction completed: ${frontOcrData?.keys}")
                            android.util.Log.d("DocumentScanViewModel", "OCR data: $frontOcrData")
                            
                            // Store OCR data in shared storage for back scan comparison
                            if (frontOcrData != null) {
                                ImageStorage.setFrontOcrData(frontOcrData!!)
                                android.util.Log.d("DocumentScanViewModel", "OCR data stored in ImageStorage for back scan comparison")
                            }
                            
                            // Add 3-second delay before showing success to prevent premature green overlay
                            android.util.Log.d("DocumentScanViewModel", "Adding 3-second delay before showing success")
                            kotlinx.coroutines.delay(3000)
                            
                            _uiState.value = DocumentScanUiState.Success(documentResult)
                            android.util.Log.d("DocumentScanViewModel", "Setting isProcessingComplete to true")
                            _isProcessingComplete.value = true
                            android.util.Log.d("DocumentScanViewModel", "Front scan processing complete - should navigate to back scan")
                        } catch (e: Exception) {
                            android.util.Log.e("DocumentScanViewModel", "OCR extraction failed: ${e.message}", e)
                            android.util.Log.e("DocumentScanViewModel", "OCR extraction stack trace: ${e.stackTraceToString()}")
                            _uiState.value = DocumentScanUiState.Error("OCR extraction failed: ${e.message}")
                        }
                    }
                    "Invalid" -> {
                        android.util.Log.d("DocumentScanViewModel", "Front scan validation failed")
                        _uiState.value = DocumentScanUiState.Error("Invalid document. Please try again.")
                    }
                    "No face detected on ID" -> {
                        android.util.Log.d("DocumentScanViewModel", "No face detected on ID")
                        _uiState.value = DocumentScanUiState.Error("No face detected on ID. Please ensure your face is clearly visible.")
                    }
                    "Insufficient text content" -> {
                        android.util.Log.d("DocumentScanViewModel", "Insufficient text content")
                        _uiState.value = DocumentScanUiState.InsufficientContent
                    }
                    "Document content unclear" -> {
                        android.util.Log.d("DocumentScanViewModel", "Document content unclear, confidence: ${documentResult.confidence}")
                        _uiState.value = DocumentScanUiState.LowQuality(documentResult.confidence)
                    }
                    else -> {
                        android.util.Log.d("DocumentScanViewModel", "Unknown validation status: ${documentResult.validationStatus}")
                        _uiState.value = DocumentScanUiState.Error("Unknown validation status: ${documentResult.validationStatus}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DocumentScanViewModel", "Error processing document", e)
                _uiState.value = DocumentScanUiState.Error("Error processing document: ${e.message}")
            } finally {
                isCurrentlyProcessing = false
            }
        }
    }

    fun processBackScanImage(bitmap: Bitmap) {
        // Process each new frame individually for barcode detection
        if (isCurrentlyProcessing) {
            android.util.Log.d("DocumentScanViewModel", "Already processing, skipping duplicate call")
            return
        }
        isCurrentlyProcessing = true
        android.util.Log.d("DocumentScanViewModel", "[BACK] Processing back scan frame: ${bitmap.width}x${bitmap.height}")
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentScanViewModel", "[BACK] Starting barcode detection...")
                val barcodeDataString = documentScanManager.scanDocumentForBarcode(bitmap, DocumentSide.BACK)
                if (barcodeDataString != null) {
                    android.util.Log.d("DocumentScanViewModel", "[BACK] SUCCESS: PDF417 detected: ${barcodeDataString.length} chars")
                    android.util.Log.d("DocumentScanViewModel", "[BACK] Barcode data preview: ${barcodeDataString.take(50)}")
                    // Store the back image for verification
                    ImageStorage.setBackImage(bitmap)
                    android.util.Log.d("DocumentScanViewModel", "[BACK] Back image set: ${bitmap.width}x${bitmap.height}")
                } else {
                    android.util.Log.e("DocumentScanViewModel", "[BACK] No barcode detected in current frame")
                }
                
                if (barcodeDataString != null) {
                    android.util.Log.d("DocumentScanViewModel", "SUCCESS: PDF417 detected: ${barcodeDataString.length} chars")
                    android.util.Log.d("DocumentScanViewModel", "Barcode data preview: ${barcodeDataString.take(50)}")
                    
                    // Parse AAMVA barcode data
                    barcodeData = AAMVABarcodeParser.parse(barcodeDataString)
                    AAMVABarcodeParser.logParsedData(barcodeData!!)
                    
                    // Store PhotoID data for verification results
                    com.artiusid.sdk.utils.DocumentDataHolder.setPhotoIdData(
                        com.artiusid.sdk.utils.PhotoIdData(
                            firstName = barcodeData!!.firstName,
                            lastName = barcodeData!!.lastName,
                            driversLicenseNumber = barcodeData!!.driversLicenseNumber,
                            dateOfBirth = barcodeData!!.dateOfBirth,
                            address = "${barcodeData!!.streetAddress ?: ""}, ${barcodeData!!.cityAddress ?: ""}, ${barcodeData!!.stateAddress ?: ""} ${barcodeData!!.zipCode ?: ""}".trim()
                        )
                    )
                    android.util.Log.d("DocumentScanViewModel", "Stored PhotoID data: firstName=${barcodeData!!.firstName}, lastName=${barcodeData!!.lastName}")
                    
                    // Store the back image for verification
                    ImageStorage.setBackImage(bitmap)
                    
                    // Retrieve OCR data from shared storage for comparison
                    val storedOcrData = ImageStorage.getFrontOcrData()
                    android.util.Log.d("DocumentScanViewModel", "[COMPARE] Retrieved OCR data from ImageStorage: ${storedOcrData?.keys}")
                    android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode data: $barcodeData")
                    
                    // Run comparison if we have both OCR and barcode data
                    if (storedOcrData != null && barcodeData != null) {
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Running OCR vs Barcode comparison")
                        val currentBarcodeData = barcodeData!!
                        val comparisonResult = comparisonManager.compareOCRWithBarcode(storedOcrData, currentBarcodeData)
                        
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Comparison result: ${comparisonResult.details}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Matched fields: ${comparisonResult.matchedFields}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Unmatched fields: ${comparisonResult.unmatchedFields}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Match percentage: ${comparisonResult.matchPercentage}")
                        
                        if (comparisonResult.isMatch) {
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] ✅ DOCUMENT VERIFICATION SUCCESSFUL - ${(comparisonResult.matchPercentage * 100).toInt()}% match")
                            _validationMessage.value = "Document verification successful! ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match."
                            
                            _uiState.value = DocumentScanUiState.BarcodeDetected(
                                listOf(BarcodeScanResult(
                                    rawValue = barcodeDataString,
                                    format = Barcode.FORMAT_PDF417,
                                    boundingBox = Rect(),
                                    valueType = Barcode.TYPE_TEXT,
                                    displayValue = barcodeDataString
                                ))
                            )
                            _isProcessingComplete.value = true
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] Submitting for verification...")
                            hasRetriedScan = false // Reset retry flag on success
                        } else {
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] ❌ DOCUMENT VERIFICATION FAILED - ${(comparisonResult.matchPercentage * 100).toInt()}% match (need 70%)")
                            _validationMessage.value = "Document verification failed. Only ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match. Retaking front image..."
                            
                            // Clear the front image and OCR data so it can be retaken
                            ImageStorage.clearAll() // Clear all images to force complete retake
                            frontOcrData = null
                            
                            _uiState.value = DocumentScanUiState.ComparisonFailed(
                                matchPercentage = comparisonResult.matchPercentage,
                                message = "Document verification failed. Only ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match. Please retake the front image with better lighting and positioning."
                            )
                            hasRetriedScan = false // Reset for next session
                        }
                    } else {
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode detected but waiting for OCR data or comparison failed")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Stored OCR data: ${storedOcrData != null}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode data: ${barcodeData != null}")
                        _validationMessage.value = "Barcode detected! Processing verification..."
                        
                        _uiState.value = DocumentScanUiState.BarcodeDetected(
                            listOf(BarcodeScanResult(
                                rawValue = barcodeDataString,
                                format = Barcode.FORMAT_PDF417,
                                boundingBox = Rect(),
                                valueType = Barcode.TYPE_TEXT,
                                displayValue = barcodeDataString
                            ))
                        )
                        _isProcessingComplete.value = true
                        hasRetriedScan = false // Reset for next session
                    }
                } else {
                    android.util.Log.d("DocumentScanViewModel", "No barcode detected in current frame")
                    _validationMessage.value = "Scanning for PDF417 barcode... Position the barcode clearly in the frame"
                    hasRetriedScan = false // Reset for next session
                }
            } catch (e: Exception) {
                android.util.Log.e("DocumentScanViewModel", "[BACK] Error in back scan: ${e.message}", e)
            } finally {
                isCurrentlyProcessing = false
            }
        }
    }

    fun resetScan() {
        _isProcessingComplete.value = false
        // Only clear OCR data if we're switching back to front scan
        // Keep frontOcrData when switching to back scan for comparison
        if (_documentSide.value == DocumentSide.FRONT) {
            frontOcrData = null
        }
        barcodeData = null
        val currentSide = _documentSide.value
        _validationMessage.value = when (currentSide) {
            DocumentSide.FRONT -> "Position your ID card in the frame"
            DocumentSide.BACK -> "Position the back of your ID card in the frame. Ensure the PDF417 barcode is clearly visible with good lighting."
        }
        _uiState.value = DocumentScanUiState.Initial
        hasRetriedScan = false // Reset retry flag on new scan
    }

    // For testing purposes - manually trigger completion
    fun manuallyTriggerCompletion() {
        android.util.Log.d("DocumentScanViewModel", "Manually triggering completion")
        _isProcessingComplete.value = true
    }

    /**
     * Enhanced autofocus support - processes document image and returns document bounds
     * for autofocus adjustment
     */
    fun processDocumentImageWithBounds(bitmap: Bitmap): Rect? {
        if (isCurrentlyProcessing) {
            android.util.Log.d("DocumentScanViewModel", "Already processing, skipping duplicate call")
            return null
        }
        
        isCurrentlyProcessing = true
        android.util.Log.d("DocumentScanViewModel", "Processing document image with bounds for side: ${_documentSide.value}")
        
        var documentBounds: Rect? = null
        
        viewModelScope.launch {
            try {
                val documentResult = documentScanManager.scanDocument(bitmap, _documentSide.value)
                android.util.Log.d("DocumentScanViewModel", "Document validation result: ${documentResult.validationStatus}")
                
                // Extract document bounds from the result if available
                documentBounds = documentResult.documentBounds
                
                _validationMessage.value = documentResult.validationStatus
                when (documentResult.validationStatus) {
                    "Valid", "Document detected" -> {
                        android.util.Log.d("DocumentScanViewModel", "Valid document detected, processing OCR")
                        
                        // Extract text using OCR (MLKit or Tesseract)
                        val ocrResult = documentInfoExtractor.extractInfo(bitmap, DocumentType.ID_CARD)
                        frontOcrData = ocrResult
                        
                        android.util.Log.d("DocumentScanViewModel", "OCR extraction completed")
                        
                        if (_documentSide.value == DocumentSide.FRONT) {
                            android.util.Log.d("DocumentScanViewModel", "Front scan completed successfully")
                            
                            // CRITICAL FIX: Store the front image for verification
                            ImageStorage.setFrontImage(bitmap)
                            android.util.Log.d("DocumentScanViewModel", "Front image saved to ImageStorage")
                            
                            // Store OCR data in shared storage for back scan comparison
                            if (frontOcrData != null) {
                                ImageStorage.setFrontOcrData(frontOcrData!!)
                                android.util.Log.d("DocumentScanViewModel", "OCR data stored in ImageStorage for back scan comparison")
                            }
                            
                            // Store front scan completion - navigation will handle the transition
                            _isProcessingComplete.value = true
                            _validationMessage.value = "Front scan completed successfully"
                            _uiState.value = DocumentScanUiState.Success(
                                DocumentScanResult(
                                    isSuccess = true,
                                    validationStatus = "Front scan completed successfully",
                                    confidence = 1.0f,
                                    bitmap = bitmap,
                                    documentBounds = documentBounds
                                )
                            )
                        }
                    }
                    "Move closer", "Document too far" -> {
                        _uiState.value = DocumentScanUiState.InsufficientContent
                    }
                    "Document blurry" -> {
                        _uiState.value = DocumentScanUiState.LowQuality(documentResult.confidence)
                    }
                    "Rotate device", "Wrong orientation" -> {
                        _uiState.value = DocumentScanUiState.IncorrectOrientation
                    }
                    else -> {
                        _uiState.value = DocumentScanUiState.Initial
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DocumentScanViewModel", "Error processing document", e)
                _uiState.value = DocumentScanUiState.Error("Error processing document: ${e.message}")
            } finally {
                isCurrentlyProcessing = false
            }
        }
        
        return documentBounds
    }

    /**
     * Enhanced autofocus support - processes back scan image and returns document bounds
     * for autofocus adjustment
     */
    fun processBackScanImageWithBounds(bitmap: Bitmap): Rect? {
        // For back scan, don't throttle as aggressively - allow more frequent attempts
        if (isCurrentlyProcessing) {
            // For back scan, only skip if we just started processing (< 200ms ago)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackScanProcessTime < 200) {
                android.util.Log.d("DocumentScanViewModel", "Recently processed, skipping")
                return null
            }
        }
        
        isCurrentlyProcessing = true
        lastBackScanProcessTime = System.currentTimeMillis()
        android.util.Log.d("DocumentScanViewModel", "Processing back scan image with bounds")
        
        var documentBounds: Rect? = null
        
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] Starting fast barcode detection...")
                
                // Try fast detection first (for real-time feedback)
                val fastBarcodeResult = barcodeScanManager.scanBarcode(bitmap)
                documentBounds = fastBarcodeResult.boundingBox
                
                var barcodeDataString: String? = null
                if (fastBarcodeResult.isSuccess) {
                    barcodeDataString = fastBarcodeResult.rawValue
                    android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] Fast detection SUCCESS!")
                } else {
                    // Fallback to robust detection only if fast detection fails
                    android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] Fast detection failed, trying robust method...")
                    barcodeDataString = documentScanManager.scanDocumentForBarcode(bitmap, DocumentSide.BACK)
                }
                
                if (barcodeDataString != null) {
                    android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] SUCCESS: PDF417 detected: ${barcodeDataString.length} chars")
                    android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] Barcode data preview: ${barcodeDataString.take(50)}")
                    
                    // Parse AAMVA data
                    barcodeData = AAMVABarcodeParser.parse(barcodeDataString)
                    AAMVABarcodeParser.logParsedData(barcodeData!!)
                    
                    // Store PhotoID data for verification results
                    com.artiusid.sdk.utils.DocumentDataHolder.setPhotoIdData(
                        com.artiusid.sdk.utils.PhotoIdData(
                            firstName = barcodeData!!.firstName,
                            lastName = barcodeData!!.lastName,
                            driversLicenseNumber = barcodeData!!.driversLicenseNumber,
                            dateOfBirth = barcodeData!!.dateOfBirth,
                            address = "${barcodeData!!.streetAddress ?: ""}, ${barcodeData!!.cityAddress ?: ""}, ${barcodeData!!.stateAddress ?: ""} ${barcodeData!!.zipCode ?: ""}".trim()
                        )
                    )
                    android.util.Log.d("DocumentScanViewModel", "Stored PhotoID data: firstName=${barcodeData!!.firstName}, lastName=${barcodeData!!.lastName}")
                    
                    // Store the back image for verification
                    ImageStorage.setBackImage(bitmap)
                    
                    // Retrieve OCR data from shared storage for comparison
                    val storedOcrData = ImageStorage.getFrontOcrData()
                    android.util.Log.d("DocumentScanViewModel", "[COMPARE] Retrieved OCR data from ImageStorage: ${storedOcrData?.keys}")
                    android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode data: $barcodeData")
                    
                    // Run comparison if we have both OCR and barcode data
                    if (storedOcrData != null && barcodeData != null) {
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Running OCR vs Barcode comparison")
                        val currentBarcodeData = barcodeData!!
                        val comparisonResult = comparisonManager.compareOCRWithBarcode(storedOcrData, currentBarcodeData)
                        
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Comparison result: ${comparisonResult.details}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Matched fields: ${comparisonResult.matchedFields}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Unmatched fields: ${comparisonResult.unmatchedFields}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Match percentage: ${comparisonResult.matchPercentage}")
                        
                        if (comparisonResult.isMatch) {
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] ✅ DOCUMENT VERIFICATION SUCCESSFUL - ${(comparisonResult.matchPercentage * 100).toInt()}% match")
                            _validationMessage.value = "Document verification successful! ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match."
                            
                            _uiState.value = DocumentScanUiState.BarcodeDetected(
                                listOf(BarcodeScanResult(
                                    rawValue = barcodeDataString,
                                    format = Barcode.FORMAT_PDF417,
                                    boundingBox = documentBounds ?: Rect(),
                                    valueType = Barcode.TYPE_TEXT,
                                    displayValue = barcodeDataString
                                ))
                            )
                            _isProcessingComplete.value = true
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] Submitting for verification...")
                            hasRetriedScan = false // Reset retry flag on success
                        } else {
                            android.util.Log.d("DocumentScanViewModel", "[VERIFY] ❌ DOCUMENT VERIFICATION FAILED - ${(comparisonResult.matchPercentage * 100).toInt()}% match (need 70%)")
                            _validationMessage.value = "Document verification failed. Only ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match. Retaking front image..."
                            
                            // Clear the front image and OCR data so it can be retaken
                            ImageStorage.clearAll() // Clear all images to force complete retake
                            frontOcrData = null
                            
                            _uiState.value = DocumentScanUiState.ComparisonFailed(
                                matchPercentage = comparisonResult.matchPercentage,
                                message = "Document verification failed. Only ${(comparisonResult.matchPercentage * 100).toInt()}% of fields match. Please retake the front image with better lighting and positioning."
                            )
                            hasRetriedScan = false // Reset for next session
                        }
                    } else {
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode detected but waiting for OCR data or comparison failed")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Stored OCR data: ${storedOcrData != null}")
                        android.util.Log.d("DocumentScanViewModel", "[COMPARE] Barcode data: ${barcodeData != null}")
                        _validationMessage.value = "Barcode detected! Processing verification..."
                        
                        _uiState.value = DocumentScanUiState.BarcodeDetected(
                            listOf(BarcodeScanResult(
                                rawValue = barcodeDataString,
                                format = Barcode.FORMAT_PDF417,
                                boundingBox = documentBounds ?: Rect(),
                                valueType = Barcode.TYPE_TEXT,
                                displayValue = barcodeDataString
                            ))
                        )
                        _isProcessingComplete.value = true
                        hasRetriedScan = false // Reset for next session
                    }
                } else {
                    android.util.Log.d("DocumentScanViewModel", "[BACK_BOUNDS] No barcode detected in current frame")
                    _validationMessage.value = "Position the back of your ID card in the frame. Ensure the PDF417 barcode is clearly visible with good lighting."
                    _uiState.value = DocumentScanUiState.NoBarcodeDetected
                }
            } catch (e: Exception) {
                android.util.Log.e("DocumentScanViewModel", "Error processing back scan", e)
                _uiState.value = DocumentScanUiState.Error("Error scanning barcode: ${e.message}")
            } finally {
                isCurrentlyProcessing = false
            }
        }
        
        return documentBounds
    }
}

sealed class DocumentScanUiState {
    object Initial : DocumentScanUiState()
    object NoFaceDetected : DocumentScanUiState()
    object NoBarcodeDetected : DocumentScanUiState()
    object InsufficientContent : DocumentScanUiState()
    object IncorrectOrientation : DocumentScanUiState()
    data class LowQuality(val confidence: Float) : DocumentScanUiState()
    data class BarcodeDetected(val barcodes: List<BarcodeScanResult>) : DocumentScanUiState()
    data class Success(val result: DocumentScanResult) : DocumentScanUiState()
    data class Error(val message: String) : DocumentScanUiState()
    data class ComparisonFailed(val matchPercentage: Double, val message: String) : DocumentScanUiState()
} 