/*
 * File: BarcodeScanManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// BarcodeScanManager.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BarcodeScanManager {
    companion object {
        private const val MIN_CONFIDENCE = 0.1f
        private const val MIN_PAYLOAD_LENGTH = 50 // PDF417 on IDs typically has substantial data
    }
    private val barcodeScanner by lazy {
        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
        
        BarcodeScanning.getClient(options)
    }

    suspend fun scanBarcodes(bitmap: Bitmap): List<BarcodeScanResult> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val results = barcodes.map { barcode ->
                    BarcodeScanResult(
                        rawValue = barcode.rawValue ?: "",
                        format = barcode.format,
                        boundingBox = barcode.boundingBox ?: Rect(),
                        valueType = barcode.valueType,
                        displayValue = barcode.displayValue ?: ""
                    )
                }
                continuation.resume(results)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    suspend fun scanBarcode(bitmap: Bitmap): BarcodeScanResult = suspendCancellableCoroutine { continuation ->
        android.util.Log.d("BarcodeScanManager", "Fast scan: Processing ${bitmap.width}x${bitmap.height} bitmap")
        val image = InputImage.fromBitmap(bitmap, 0)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                android.util.Log.d("BarcodeScanManager", "Fast scan: Found ${barcodes.size} total barcodes")
                val pdf417Barcode = barcodes.find { it.format == Barcode.FORMAT_PDF417 }
                
                if (pdf417Barcode != null) {
                    android.util.Log.d("BarcodeScanManager", "Fast scan: PDF417 found with ${pdf417Barcode.rawValue?.length ?: 0} chars")
                    val result = BarcodeScanResult(
                        rawValue = pdf417Barcode.rawValue ?: "",
                        format = pdf417Barcode.format,
                        boundingBox = pdf417Barcode.boundingBox ?: Rect(),
                        valueType = pdf417Barcode.valueType,
                        displayValue = pdf417Barcode.displayValue ?: "",
                        isSuccess = true,
                        error = null
                    )
                    continuation.resume(result)
                } else {
                    android.util.Log.d("BarcodeScanManager", "Fast scan: No PDF417 found among ${barcodes.size} barcodes")
                    if (barcodes.isNotEmpty()) {
                        android.util.Log.d("BarcodeScanManager", "Fast scan: Found formats: ${barcodes.map { it.format }.joinToString()}")
                    }
                    val result = BarcodeScanResult(
                        rawValue = "",
                        format = 0,
                        boundingBox = Rect(),
                        valueType = 0,
                        displayValue = "",
                        isSuccess = false,
                        error = "No PDF417 barcode detected"
                    )
                    continuation.resume(result)
                }
            }
            .addOnFailureListener { e ->
                val result = BarcodeScanResult(
                    rawValue = "",
                    format = 0,
                    boundingBox = Rect(),
                    valueType = 0,
                    displayValue = "",
                    isSuccess = false,
                    error = e.message
                )
                continuation.resume(result)
            }
    }

    suspend fun scanPDF417Barcode(bitmap: Bitmap, deviceOrientation: Int = 0): String? = suspendCancellableCoroutine { continuation ->
        android.util.Log.d("BarcodeScanManager", "Starting enhanced PDF417 detection with iOS-style orientation strategy")
        
        // First try iOS-style orientation strategy
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val orientationResult = detectPDF417WithOrientations(bitmap, deviceOrientation)
            if (orientationResult != null) {
                android.util.Log.d("BarcodeScanManager", "PDF417 detected with orientation strategy")
                continuation.resume(orientationResult)
                return@launch
            }
            
            // Fallback to bottom area detection
            val bottomAreaResult = detectPDF417InBottomArea(bitmap)
            if (bottomAreaResult != null) {
                android.util.Log.d("BarcodeScanManager", "PDF417 detected in bottom area fallback")
                continuation.resume(bottomAreaResult)
                return@launch
            }
            
            // If both orientation and bottom area detection fail, continue with full image approaches
            continueWithFullImageApproaches(bitmap, continuation)
        }
    }

    private suspend fun continueWithFullImageApproaches(bitmap: Bitmap, continuation: kotlinx.coroutines.CancellableContinuation<String?>) {
        // Try multiple image processing approaches for better PDF417 detection
        val approaches = listOf(
            { bitmap }, // Original bitmap
            { enhanceBitmapForBarcodeDetection(bitmap) }, // Enhanced bitmap
            { convertBitmapToGrayscale(bitmap) }, // Grayscale bitmap
            { createHighContrastBitmap(bitmap) }, // High contrast bitmap
            { invertBitmap(bitmap) }, // Inverted bitmap
            { createUltraHighContrastBitmap(bitmap) } // Ultra high contrast bitmap
        )
        
        var completedApproaches = 0
        val totalApproaches = approaches.size
        
        for ((index, approach) in approaches.withIndex()) {
            try {
                val processedBitmap = approach()
                if (processedBitmap != null) {
                    android.util.Log.d("BarcodeScanManager", "Trying approach ${index + 1}/${totalApproaches}")
                    val image = InputImage.fromBitmap(processedBitmap, 0)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { result ->
                            val pdf417Barcodes = result.filter { it.format == Barcode.FORMAT_PDF417 }
                            if (pdf417Barcodes.isNotEmpty()) {
                                val barcode = pdf417Barcodes.first()
                                android.util.Log.d("BarcodeScanManager", "PDF417 detected with approach ${index + 1}: ${barcode.rawValue?.length ?: 0} characters")
                                continuation.resume(barcode.rawValue)
                            } else {
                                completedApproaches++
                                android.util.Log.d("BarcodeScanManager", "No PDF417 found with approach ${index + 1}")
                                if (completedApproaches >= totalApproaches) {
                                    android.util.Log.w("BarcodeScanManager", "No PDF417 barcode found with any approach")
                                    continuation.resume(null)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("BarcodeScanManager", "Error in PDF417 detection approach ${index + 1}: ${e.message}")
                            completedApproaches++
                            if (completedApproaches >= totalApproaches) {
                                continuation.resume(null)
                            }
                        }
                } else {
                    completedApproaches++
                    android.util.Log.w("BarcodeScanManager", "Failed to create processed bitmap for approach ${index + 1}")
                    if (completedApproaches >= totalApproaches) {
                        continuation.resume(null)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BarcodeScanManager", "Error in PDF417 detection approach ${index + 1}: ${e.message}")
                completedApproaches++
                if (completedApproaches >= totalApproaches) {
                    continuation.resume(null)
                }
            }
        }
    }

    /**
     * iOS-style orientation detection strategy
     */
    private suspend fun detectPDF417WithOrientations(bitmap: Bitmap, deviceOrientation: Int): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        android.util.Log.d("BarcodeScanManager", "Attempting PDF417 detection with orientation strategy")
        
        // iOS-style optimized orientation strategy: prioritize likely orientations for ID cards
        val orientationsToTry = getOrientationPriority(deviceOrientation)
        
        for ((index, orientation) in orientationsToTry.withIndex()) {
            android.util.Log.d("BarcodeScanManager", "Orientation attempt ${index + 1}: trying orientation $orientation")
            
            val rotatedBitmap = when (orientation) {
                0 -> bitmap // Original orientation
                90 -> rotateBitmap(bitmap, 90f)
                180 -> rotateBitmap(bitmap, 180f)
                270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
            
            try {
                val image = InputImage.fromBitmap(rotatedBitmap, 0)
                val result = suspendCancellableCoroutine<String?> { innerContinuation ->
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            android.util.Log.d("BarcodeScanManager", "Orientation attempt ${index + 1}: Found ${barcodes.size} barcodes with orientation $orientation")
                            
                            val validBarcodes = barcodes.filter { barcode ->
                                barcode.format == Barcode.FORMAT_PDF417 && validateBarcodePayload(barcode.rawValue)
                            }
                            
                            if (validBarcodes.isNotEmpty()) {
                                val barcode = validBarcodes.first()
                                android.util.Log.d("BarcodeScanManager", "PDF417 detected with orientation $orientation: ${barcode.rawValue?.length ?: 0} characters")
                                innerContinuation.resume(barcode.rawValue)
                            } else {
                                android.util.Log.d("BarcodeScanManager", "No valid PDF417 found with orientation $orientation")
                                innerContinuation.resume(null)
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("BarcodeScanManager", "Error in orientation attempt ${index + 1}: ${e.message}")
                            innerContinuation.resume(null)
                        }
                }
                
                if (result != null) {
                    return@withContext result
                }
            } catch (e: Exception) {
                android.util.Log.e("BarcodeScanManager", "Error in orientation $orientation: ${e.message}")
            } finally {
                // Clean up rotated bitmap if it's different from original
                if (rotatedBitmap != bitmap && !rotatedBitmap.isRecycled) {
                    rotatedBitmap.recycle()
                }
            }
        }
        
        android.util.Log.w("BarcodeScanManager", "PDF417 not detected with any orientation")
        return@withContext null
    }

    private suspend fun detectPDF417InBottomArea(bitmap: Bitmap): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        android.util.Log.d("BarcodeScanManager", "Attempting PDF417 detection in bottom area")
        
        try {
            // Crop the bottom 30% of the image where PDF417 barcodes are typically located
            val bottomAreaHeight = (bitmap.height * 0.3).toInt()
            val topY = bitmap.height - bottomAreaHeight
            
            android.util.Log.d("BarcodeScanManager", "Cropping bottom area: y=$topY, height=$bottomAreaHeight")
            
            val croppedBitmap = android.graphics.Bitmap.createBitmap(
                bitmap, 
                0, 
                topY, 
                bitmap.width, 
                bottomAreaHeight
            )
            
            // Try different processing methods on the cropped area
            val processingMethods = listOf(
                { croppedBitmap },
                { enhanceBitmapForBarcodeDetection(croppedBitmap) },
                { convertBitmapToGrayscale(croppedBitmap) },
                { createHighContrastBitmap(croppedBitmap) },
                { invertBitmap(croppedBitmap) },
                { createUltraHighContrastBitmap(croppedBitmap) }
            )
            
            for ((index, method) in processingMethods.withIndex()) {
                try {
                    val processedBitmap = method()
                    if (processedBitmap != null) {
                        val image = InputImage.fromBitmap(processedBitmap, 0)
                        val result = suspendCancellableCoroutine<String?> { innerContinuation ->
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodeResult ->
                                    val pdf417Barcodes = barcodeResult.filter { it.format == Barcode.FORMAT_PDF417 }
                                    if (pdf417Barcodes.isNotEmpty()) {
                                        val barcode = pdf417Barcodes.first()
                                        android.util.Log.d("BarcodeScanManager", "PDF417 detected in bottom area with method ${index + 1}")
                                        innerContinuation.resume(barcode.rawValue)
                                    } else {
                                        innerContinuation.resume(null)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("BarcodeScanManager", "Error in bottom area method ${index + 1}: ${e.message}")
                                    innerContinuation.resume(null)
                                }
                        }
                        if (result != null) {
                            return@withContext result
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BarcodeScanManager", "Error in bottom area method ${index + 1}: ${e.message}")
                }
            }
            
            android.util.Log.w("BarcodeScanManager", "PDF417 not detected in bottom area")
            return@withContext null
            
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error cropping bottom area: ${e.message}")
            return@withContext null
        }
    }

    private fun enhanceBitmapForBarcodeDetection(bitmap: Bitmap): Bitmap? {
        return try {
            // Create enhanced bitmap with higher contrast
            val enhancedBitmap = bitmap.copy(bitmap.config, true)
            val canvas = android.graphics.Canvas(enhancedBitmap)
            
            // Apply contrast enhancement
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f) // Convert to grayscale
            val paint = android.graphics.Paint()
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            enhancedBitmap
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error enhancing bitmap: ${e.message}")
            null
        }
    }

    private fun convertBitmapToGrayscale(bitmap: Bitmap): Bitmap? {
        return try {
            val grayscaleBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(grayscaleBitmap)
            
            val paint = android.graphics.Paint()
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix().apply {
                setSaturation(0f)
            })
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            grayscaleBitmap
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error converting bitmap to grayscale: ${e.message}")
            null
        }
    }

    private fun createHighContrastBitmap(bitmap: Bitmap): Bitmap? {
        return try {
            val highContrastBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(highContrastBitmap)
            
            val paint = android.graphics.Paint()
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix().apply {
                setSaturation(0f) // Convert to grayscale
                setScale(2.0f, 2.0f, 2.0f, 1f) // Increase contrast
            })
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            highContrastBitmap
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error creating high contrast bitmap: ${e.message}")
            null
        }
    }

    private fun invertBitmap(bitmap: Bitmap): Bitmap? {
        return try {
            val invertedBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(invertedBitmap)
            
            val paint = android.graphics.Paint()
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix().apply {
                setSaturation(0f) // Convert to grayscale first
                set(android.graphics.ColorMatrix().apply {
                    setScale(-1f, -1f, -1f, 1f) // Invert colors
                })
            })
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            invertedBitmap
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error inverting bitmap: ${e.message}")
            null
        }
    }

    private fun createUltraHighContrastBitmap(bitmap: Bitmap): Bitmap? {
        return try {
            val ultraHighContrastBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(ultraHighContrastBitmap)
            
            val paint = android.graphics.Paint()
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix().apply {
                setSaturation(0f) // Convert to grayscale
                setScale(3.0f, 3.0f, 3.0f, 1f) // Ultra high contrast
            })
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            ultraHighContrastBitmap
        } catch (e: Exception) {
            android.util.Log.e("BarcodeScanManager", "Error creating ultra high contrast bitmap: ${e.message}")
            null
        }
    }

    /**
     * Get orientation priority based on device orientation (iOS-style strategy)
     */
    private fun getOrientationPriority(deviceOrientation: Int): List<Int> {
        return listOf(
            deviceOrientation,     // Start with device orientation
            0,                     // Standard portrait
            270,                   // Landscape right (common for back of ID)
            180,                   // Portrait upside down
            90                     // Landscape left
        ).distinct() // Remove duplicates if device orientation matches one of the standards
    }

    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Enhanced validation: check payload quality (matching iOS logic)
     */
    private fun validateBarcodePayload(payload: String?): Boolean {
        if (payload.isNullOrEmpty()) {
            android.util.Log.d("BarcodeScanManager", "Barcode filtered out: empty payload")
            return false
        }
        
        if (payload.length < MIN_PAYLOAD_LENGTH) {
            android.util.Log.d("BarcodeScanManager", "Barcode filtered out: payload too short (${payload.length} < $MIN_PAYLOAD_LENGTH)")
            return false
        }
        
        android.util.Log.d("BarcodeScanManager", "Valid barcode found: length=${payload.length}")
        return true
    }
}

data class BarcodeScanResult(
    val rawValue: String,
    val format: Int,
    val boundingBox: Rect,
    val valueType: Int,
    val displayValue: String,
    val isSuccess: Boolean = false,
    val error: String? = null
) 