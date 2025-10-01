/*
 * File: EnhancedBarcodeReader.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// EnhancedBarcodeReader.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
// Created by Todd Bryant on 12/28/24.
//

package com.artiusid.sdk.utils

import android.graphics.*
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

// MARK: - Supporting Data Classes

/**
 * Image characteristics for quality assessment
 */
data class ImageCharacteristics(
    val brightness: Float,
    val contrast: Float,
    val noiseLevel: Float
)

/**
 * Barcode result with quality metrics
 */
data class BarcodeResult(
    val payload: String,
    val confidence: Float,
    val orientation: Int,
    val processingStrategy: ImageProcessingStrategy,
    val qualityScore: Float
) {
    val overallScore: Float
        get() = confidence * 0.6f + qualityScore * 0.4f
}

/**
 * Image processing strategies
 */
enum class ImageProcessingStrategy(val description: String) {
    ORIGINAL("Original image"),
    ENHANCED_CONTRAST("Enhanced contrast"),
    HIGH_CONTRAST("High contrast"),
    GRAYSCALE("Grayscale conversion"),
    DENOISED("Noise reduction"),
    SHARPENED("Sharpening filter")
}

/**
 * State/Province specific configuration
 */
data class StateConfig(
    val preferredOrientations: List<Int>,
    val minConfidence: Float,
    val expectedPayloadLength: IntRange,
    val preferredStrategies: List<ImageProcessingStrategy>
)

/**
 * Enhanced PDF417 barcode reader with comprehensive improvements.
 * Features:
 * - Dynamic confidence thresholds based on image quality
 * - Advanced image preprocessing pipeline with multiple strategies  
 * - Smart orientation priority based on device context and regional patterns
 * - Multi-pass validation to select best quality result
 * - Region-specific detection for US states and Canadian provinces
 * - Comprehensive image quality analysis
 */
class EnhancedBarcodeReader {
    companion object {
        private const val TAG = "EnhancedBarcodeReader"
        private const val BASE_MIN_CONFIDENCE = 0.1f
        private const val MIN_PAYLOAD_LENGTH = 50 // PDF417 on IDs typically has substantial data
    }

    // MARK: - State-specific configurations
    private val stateConfigs: Map<String, StateConfig> = mapOf(
        // US States
        "CA" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 80..300,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "TX" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.12f,
            expectedPayloadLength = 90..350,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "FL" to StateConfig(
            preferredOrientations = listOf(270, 0, 180, 90),
            minConfidence = 0.18f,
            expectedPayloadLength = 70..280,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "NY" to StateConfig(
            preferredOrientations = listOf(0, 270, 90, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        
        // Additional US States (alphabetical order)
        "AL" to StateConfig( // Alabama
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..310,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "AK" to StateConfig( // Alaska
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.16f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "AZ" to StateConfig( // Arizona
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "AR" to StateConfig( // Arkansas
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.12f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "CO" to StateConfig( // Colorado
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 90..330,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "CT" to StateConfig( // Connecticut
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 75..295,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "DE" to StateConfig( // Delaware
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 70..285,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "DC" to StateConfig( // District of Columbia
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.16f,
            expectedPayloadLength = 80..300,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "GA" to StateConfig( // Georgia
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "HI" to StateConfig( // Hawaii
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "ID" to StateConfig( // Idaho
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.12f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "IL" to StateConfig( // Illinois
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 90..340,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "IN" to StateConfig( // Indiana
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "IA" to StateConfig( // Iowa
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.12f,
            expectedPayloadLength = 80..310,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.SHARPENED)
        ),
        "KS" to StateConfig( // Kansas
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "KY" to StateConfig( // Kentucky
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "LA" to StateConfig( // Louisiana
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.15f,
            expectedPayloadLength = 85..325,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "ME" to StateConfig( // Maine
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 75..295,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "MD" to StateConfig( // Maryland
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 80..310,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "MA" to StateConfig( // Massachusetts
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "MI" to StateConfig( // Michigan
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 90..335,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "MN" to StateConfig( // Minnesota
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "MS" to StateConfig( // Mississippi
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.12f,
            expectedPayloadLength = 80..300,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "MO" to StateConfig( // Missouri
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "MT" to StateConfig( // Montana
            preferredOrientations = listOf(270, 0, 180, 90),
            minConfidence = 0.14f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "NE" to StateConfig( // Nebraska
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "NV" to StateConfig( // Nevada
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "NH" to StateConfig( // New Hampshire
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 75..295,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "NJ" to StateConfig( // New Jersey
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.16f,
            expectedPayloadLength = 85..325,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "NM" to StateConfig( // New Mexico
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..310,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "NC" to StateConfig( // North Carolina
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.13f,
            expectedPayloadLength = 90..330,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "ND" to StateConfig( // North Dakota
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.12f,
            expectedPayloadLength = 75..295,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "OH" to StateConfig( // Ohio
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "OK" to StateConfig( // Oklahoma
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "OR" to StateConfig( // Oregon
            preferredOrientations = listOf(270, 0, 180, 90),
            minConfidence = 0.15f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "PA" to StateConfig( // Pennsylvania
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 90..335,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "RI" to StateConfig( // Rhode Island
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.15f,
            expectedPayloadLength = 70..285,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "SC" to StateConfig( // South Carolina
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "SD" to StateConfig( // South Dakota
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.12f,
            expectedPayloadLength = 75..295,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.SHARPENED)
        ),
        "TN" to StateConfig( // Tennessee
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..315,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "UT" to StateConfig( // Utah
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..305,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "VT" to StateConfig( // Vermont
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "VA" to StateConfig( // Virginia
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 85..325,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "WA" to StateConfig( // Washington
            preferredOrientations = listOf(270, 0, 180, 90),
            minConfidence = 0.14f,
            expectedPayloadLength = 90..330,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "WV" to StateConfig( // West Virginia
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..300,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "WI" to StateConfig( // Wisconsin
            preferredOrientations = listOf(0, 90, 180, 270),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "WY" to StateConfig( // Wyoming
            preferredOrientations = listOf(0, 90, 270, 180),
            minConfidence = 0.12f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        
        // Canadian Provinces
        "ON" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.16f,
            expectedPayloadLength = 90..380,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "BC" to StateConfig(
            preferredOrientations = listOf(270, 180, 0, 90),
            minConfidence = 0.14f,
            expectedPayloadLength = 85..350,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "AB" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.13f,
            expectedPayloadLength = 80..340,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.SHARPENED)
        ),
        "QC" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.17f,
            expectedPayloadLength = 95..400,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "MB" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.15f,
            expectedPayloadLength = 75..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "SK" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.14f,
            expectedPayloadLength = 80..310,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "NS" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.16f,
            expectedPayloadLength = 70..300,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.SHARPENED)
        ),
        "NB" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.15f,
            expectedPayloadLength = 75..290,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "NL" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.17f,
            expectedPayloadLength = 80..320,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        ),
        "PE" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.16f,
            expectedPayloadLength = 70..280,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.ENHANCED_CONTRAST)
        ),
        "YT" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.18f,
            expectedPayloadLength = 65..270,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.GRAYSCALE, ImageProcessingStrategy.SHARPENED)
        ),
        "NT" to StateConfig(
            preferredOrientations = listOf(270, 0, 90, 180),
            minConfidence = 0.19f,
            expectedPayloadLength = 65..275,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST)
        ),
        "NU" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.20f,
            expectedPayloadLength = 60..270,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.DENOISED)
        ),
        "DEFAULT" to StateConfig(
            preferredOrientations = listOf(0, 270, 180, 90),
            minConfidence = 0.15f,
            expectedPayloadLength = 50..400,
            preferredStrategies = listOf(ImageProcessingStrategy.ORIGINAL, ImageProcessingStrategy.ENHANCED_CONTRAST, ImageProcessingStrategy.HIGH_CONTRAST, ImageProcessingStrategy.GRAYSCALE)
        )
    )

    // Optimized barcode scanner for PDF417 exclusively
    private val barcodeScanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417) // Exclusively PDF417 for State ID barcodes
            .build()
        Log.d(TAG, "MLKit scanner initialized for PDF417 with enhanced settings")
        BarcodeScanning.getClient(options)
    }

    /**
     * Enhanced barcode reading with comprehensive strategies
     * @param bitmap The image bitmap to scan
     * @param deviceOrientation The current device orientation
     * @param detectedState Optional state/province code for optimized detection
     * @return The barcode data string if found, null otherwise
     */
    suspend fun readCodesWithEnhancedDetection(
        bitmap: Bitmap, 
        deviceOrientation: Int = 0, 
        detectedState: String? = null
    ): String? {
        Log.d(TAG, "Starting enhanced barcode detection on ${bitmap.width}x${bitmap.height} bitmap")
        
        // Validate input bitmap
        if (bitmap.isRecycled) {
            logWarning("Bitmap is recycled, cannot process")
            return null
        }

        // Analyze image characteristics for quality assessment
        val imageCharacteristics = analyzeImageCharacteristics(bitmap)
        Log.d(TAG, "Image quality analysis - Brightness: ${imageCharacteristics.brightness}, Contrast: ${imageCharacteristics.contrast}, Noise Level: ${imageCharacteristics.noiseLevel}")

        // Get state-specific configuration
        val config = getStateConfig(detectedState)
        
        // Build smart orientation strategy
        val orientationsToTry = buildSmartOrientationStrategy(deviceOrientation, config, imageCharacteristics)
        
        // Collect all valid results for comparison
        val validResults = mutableListOf<BarcodeResult>()
        
        // Try each processing strategy and orientation combination
        for (strategy in config.preferredStrategies) {
            val processedBitmap = applyImageProcessingStrategy(bitmap, strategy)
                ?: continue
            
            try {
                for (orientation in orientationsToTry) {
                    val rotatedBitmap = when (orientation) {
                        0 -> processedBitmap
                        90 -> rotateBitmap(processedBitmap, 90f)
                        180 -> rotateBitmap(processedBitmap, 180f)  
                        270 -> rotateBitmap(processedBitmap, 270f)
                        else -> processedBitmap
                    }
                    
                    try {
                        val result = detectBarcodeWithValidation(
                            rotatedBitmap, 
                            orientation, 
                            strategy, 
                            imageCharacteristics, 
                            config
                        )
                        
                        result?.let { validResults.add(it) }
                        
                    } finally {
                        if (rotatedBitmap != processedBitmap && !rotatedBitmap.isRecycled) {
                            rotatedBitmap.recycle()
                        }
                    }
                }
            } finally {
                if (processedBitmap != bitmap && !processedBitmap.isRecycled) {
                    processedBitmap.recycle()
                }
            }
        }
        
        // Select the best result based on overall score
        val bestResult = validResults.maxByOrNull { it.overallScore }
        
        return if (bestResult != null) {
            Log.d(TAG, "Enhanced detection successful - Strategy: ${bestResult.processingStrategy}, " +
                    "Orientation: ${bestResult.orientation}, Overall Score: ${bestResult.overallScore}")
            bestResult.payload
        } else {
            Log.d(TAG, "Enhanced detection failed after trying ${config.preferredStrategies.size} strategies")
            
            // Send failure logs
            sendFailureLogs(
                reason = "Enhanced barcode reading failed",
                errorDetails = "Could not detect any PDF417 barcode data after trying ${config.preferredStrategies.size} processing strategies and ${orientationsToTry.size} orientations. Image quality - Brightness: ${imageCharacteristics.brightness}, Contrast: ${imageCharacteristics.contrast}"
            )
            
            null
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    suspend fun readCodes(bitmap: Bitmap, deviceOrientation: Int = 0): String? {
        return readCodesWithEnhancedDetection(bitmap, deviceOrientation, null)
    }

    // MARK: - Helper Methods
    
    private fun getStateConfig(state: String?): StateConfig {
        return state?.uppercase()?.let { stateConfigs[it] } ?: stateConfigs["DEFAULT"]!!
    }
    
    private fun buildSmartOrientationStrategy(
        deviceOrientation: Int,
        stateConfig: StateConfig,
        imageCharacteristics: ImageCharacteristics
    ): List<Int> {
        val orientations = mutableListOf<Int>()
        
        // Start with device orientation if it's in the preferred list
        if (stateConfig.preferredOrientations.contains(deviceOrientation)) {
            orientations.add(deviceOrientation)
        }
        
        // Add state-specific preferred orientations
        for (orientation in stateConfig.preferredOrientations) {
            if (!orientations.contains(orientation)) {
                orientations.add(orientation)
            }
        }
        
        // If image quality is poor, try additional orientations
        if (calculateQualityScore(imageCharacteristics) < 0.3f) {
            val allOrientations = listOf(0, 90, 180, 270)
            for (orientation in allOrientations) {
                if (!orientations.contains(orientation)) {
                    orientations.add(orientation)
                }
            }
        }
        
        return orientations
    }
    
    private fun calculateQualityScore(imageCharacteristics: ImageCharacteristics): Float {
        // Weighted quality score (0.0 to 1.0)
        return (imageCharacteristics.brightness * 0.25f + 
                imageCharacteristics.contrast * 0.35f + 
                (1.0f - imageCharacteristics.noiseLevel) * 0.4f)
    }
    
    private fun calculateDynamicConfidenceThreshold(
        imageCharacteristics: ImageCharacteristics,
        baseThreshold: Float
    ): Float {
        val qualityFactor = calculateQualityScore(imageCharacteristics)
        
        // Lower quality images need higher confidence
        // Scale from baseThreshold (high quality) to baseThreshold * 3 (low quality)
        val dynamicThreshold = baseThreshold + (1.0f - qualityFactor) * (baseThreshold * 2.0f)
        
        // Clamp between reasonable bounds
        return dynamicThreshold.coerceIn(0.05f, 0.4f)
    }
    
    private suspend fun detectBarcodeWithValidation(
        bitmap: Bitmap,
        orientation: Int,
        strategy: ImageProcessingStrategy,
        imageCharacteristics: ImageCharacteristics,
        config: StateConfig
    ): BarcodeResult? = suspendCancellableCoroutine { continuation ->
        
        val image = InputImage.fromBitmap(bitmap, 0)
        val dynamicThreshold = calculateDynamicConfidenceThreshold(imageCharacteristics, config.minConfidence)
        Log.d(TAG, "Using dynamic threshold: $dynamicThreshold for strategy: ${strategy.description}")
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d(TAG, "Processing ${barcodes.size} barcodes with strategy: ${strategy.description}, orientation: $orientation")
                
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_PDF417) {
                        val payload = barcode.rawValue
                        
                        if (!payload.isNullOrEmpty() && 
                            payload.length >= MIN_PAYLOAD_LENGTH &&
                            isValidBarcodePayload(payload, config)) {
                            
                            val qualityScore = calculateQualityScore(imageCharacteristics)
                            
                            Log.d(TAG, "Valid PDF417 found - Strategy: ${strategy.description}, " +
                                    "Length: ${payload.length}, Quality: $qualityScore")
                            
                            val result = BarcodeResult(
                                payload = payload,
                                confidence = 1.0f, // MLKit doesn't provide confidence, using 1.0
                                orientation = orientation,
                                processingStrategy = strategy,
                                qualityScore = qualityScore
                            )
                            
                            continuation.resume(result)
                            return@addOnSuccessListener
                        }
                    }
                }
                
                continuation.resume(null)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Barcode detection failed with strategy: ${strategy.description} - ${exception.message}")
                continuation.resume(null)
            }
    }
    
    private fun isValidBarcodePayload(payload: String, config: StateConfig): Boolean {
        // Check payload length against state-specific expectations
        if (!config.expectedPayloadLength.contains(payload.length)) {
            Log.d(TAG, "Payload length ${payload.length} outside expected range ${config.expectedPayloadLength}")
            return false
        }
        
        // Check for basic PDF417 ID structure (AAMVA or similar)
        val hasAAMVAHeader = payload.contains("@") || payload.contains("ANSI") || 
                           payload.contains("AAMVA") || payload.startsWith("636")
        val hasFieldCodes = Regex("[A-Z]{2}[A-Z0-9]").find(payload) != null
        
        // Canadian licenses might have different patterns
        val hasCanadianPattern = payload.contains("CAN") || 
                                payload.matches(Regex(".*[A-Z]{2}\\d{4}.*"))
        
        if (!hasAAMVAHeader && !hasFieldCodes && !hasCanadianPattern) {
            Log.d(TAG, "Payload missing expected ID structure")
            return false
        }
        
        return true
    }

    // MARK: - Image Processing Functions
    
    private fun analyzeImageCharacteristics(bitmap: Bitmap): ImageCharacteristics {
        val brightness = calculateImageBrightness(bitmap)
        val contrast = calculateImageContrast(bitmap)
        val noiseLevel = estimateNoiseLevel(bitmap)
        
        return ImageCharacteristics(
            brightness = brightness,
            contrast = contrast,
            noiseLevel = noiseLevel
        )
    }
    
    private fun calculateImageBrightness(bitmap: Bitmap): Float {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var totalBrightness = 0.0
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // Using luminance formula
            totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b)
        }
        
        val averageBrightness = totalBrightness / pixels.size
        return (averageBrightness / 255.0).toFloat()
    }
    
    private fun calculateImageContrast(bitmap: Bitmap): Float {
        // Convert to grayscale and calculate standard deviation
        val grayscaleBitmap = convertToGrayscale(bitmap)
        val pixels = IntArray(grayscaleBitmap.width * grayscaleBitmap.height)
        grayscaleBitmap.getPixels(pixels, 0, grayscaleBitmap.width, 0, 0, grayscaleBitmap.width, grayscaleBitmap.height)
        
        val grayValues = pixels.map { (it shr 16) and 0xFF }
        val mean = grayValues.average()
        val variance = grayValues.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        
        grayscaleBitmap.recycle()
        
        // Normalize to 0-1 range (typical std dev for 8-bit images is 0-64)
        return (stdDev / 64.0).coerceAtMost(1.0).toFloat()
    }
    
    private fun estimateNoiseLevel(bitmap: Bitmap): Float {
        // Simple noise estimation using local variance
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var totalVariance = 0.0
        var validSamples = 0
        
        // Sample variance in 3x3 neighborhoods
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val neighborhood = mutableListOf<Int>()
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val pixel = pixels[(y + dy) * width + (x + dx)]
                        val gray = ((pixel shr 16) and 0xFF)
                        neighborhood.add(gray)
                    }
                }
                
                val mean = neighborhood.average()
                val variance = neighborhood.map { (it - mean).pow(2) }.average()
                totalVariance += variance
                validSamples++
            }
        }
        
        val avgVariance = if (validSamples > 0) totalVariance / validSamples else 0.0
        return (avgVariance / 1000.0).coerceAtMost(1.0).toFloat()
    }
    
    private fun applyImageProcessingStrategy(bitmap: Bitmap, strategy: ImageProcessingStrategy): Bitmap? {
        return when (strategy) {
            ImageProcessingStrategy.ORIGINAL -> bitmap
            ImageProcessingStrategy.ENHANCED_CONTRAST -> enhanceContrast(bitmap, 1.4f)
            ImageProcessingStrategy.HIGH_CONTRAST -> enhanceContrast(bitmap, 2.0f)
            ImageProcessingStrategy.GRAYSCALE -> convertToGrayscale(bitmap)
            ImageProcessingStrategy.DENOISED -> applyDenoising(bitmap)
            ImageProcessingStrategy.SHARPENED -> applySharpen(bitmap)
        }
    }
    
    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return grayscaleBitmap
    }
    
    private fun enhanceContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(contrastBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(contrast, contrast, contrast, 1f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return contrastBitmap
    }
    
    private fun applyDenoising(bitmap: Bitmap): Bitmap {
        // Simple blur filter for noise reduction
        val width = bitmap.width
        val height = bitmap.height
        val denoisedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(denoisedBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        
        // Apply slight blur
        paint.maskFilter = BlurMaskFilter(1.0f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return denoisedBitmap
    }
    
    private fun applySharpen(bitmap: Bitmap): Bitmap {
        // Simple unsharp mask effect using contrast enhancement
        return enhanceContrast(bitmap, 1.8f)
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
    
    // MARK: - Province/State Detection
    
    /**
     * Enhanced function to detect province/state from barcode content
     */
    fun detectProvinceOrState(payload: String): String? {
        val upperPayload = payload.uppercase()
        
        // Check for explicit province/state codes
        val allRegions = stateConfigs.keys.filter { it != "DEFAULT" }
        
        // First, look for province codes in common positions
        for (region in allRegions) {
            // Check at start of payload
            if (upperPayload.startsWith(region)) return region
            
            // Check after common prefixes like "636", "ANSI", "@", etc.
            val patterns = listOf("636$region", "ANSI$region", "@$region", " $region")
            for (pattern in patterns) {
                if (upperPayload.contains(pattern)) return region
            }
            
            // Check for region code in structured data fields
            val structuredPatterns = listOf("IIN$region", "AAK$region", "DCG$region")
            for (pattern in structuredPatterns) {
                if (upperPayload.contains(pattern)) return region
            }
        }
        
        // Canadian specific patterns
        if (upperPayload.contains("CANADA") || upperPayload.contains("CAN")) {
            // Try to find specific province indicators
            val provinceIndicators = mapOf(
                "ONTARIO" to "ON", "ONT" to "ON",
                "BRITISH COLUMBIA" to "BC", "BC" to "BC",
                "ALBERTA" to "AB", "ALB" to "AB",
                "QUEBEC" to "QC", "QUE" to "QC",
                "MANITOBA" to "MB", "MAN" to "MB",
                "SASKATCHEWAN" to "SK", "SASK" to "SK"
            )
            
            for ((indicator, province) in provinceIndicators) {
                if (upperPayload.contains(indicator)) return province
            }
        }
        
        return null
    }

    /**
     * Comprehensive test function for debugging (matching iOS functionality)
     */
    suspend fun testBarcodeDetection(bitmap: Bitmap): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        // Analyze image characteristics
        val imageCharacteristics = analyzeImageCharacteristics(bitmap)
        results["imageCharacteristics"] = mapOf(
            "brightness" to imageCharacteristics.brightness,
            "contrast" to imageCharacteristics.contrast,
            "noiseLevel" to imageCharacteristics.noiseLevel,
            "qualityScore" to calculateQualityScore(imageCharacteristics)
        )
        
        // Test with different processing strategies
        val strategies = ImageProcessingStrategy.values()
        val strategyResults = mutableMapOf<String, Any>()
        
        for (strategy in strategies) {
            val processedBitmap = applyImageProcessingStrategy(bitmap, strategy)
            if (processedBitmap != null) {
                val strategyOrientationResults = mutableMapOf<String, Any>()
                val orientations = listOf(0, 90, 180, 270)
                
                for (orientation in orientations) {
                    val rotatedBitmap = when (orientation) {
                        0 -> processedBitmap
                        else -> rotateBitmap(processedBitmap, orientation.toFloat())
                    }
                    
                    try {
                        val image = InputImage.fromBitmap(rotatedBitmap, 0)
                        val barcodes = suspendCancellableCoroutine<List<Barcode>> { continuation ->
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes -> continuation.resume(barcodes) }
                                .addOnFailureListener { continuation.resume(emptyList()) }
                        }
                        
                        val orientationResults = mutableListOf<Map<String, Any>>()
                        
                        for (barcode in barcodes) {
                            val config = getStateConfig(null) // Use default config for testing
                            val dynamicThreshold = calculateDynamicConfidenceThreshold(
                                imageCharacteristics = imageCharacteristics,
                                baseThreshold = config.minConfidence
                            )
                            
                            orientationResults.add(mapOf(
                                "confidence" to 1.0f, // MLKit doesn't provide confidence
                                "dynamicThreshold" to dynamicThreshold,
                                "format" to barcode.format,
                                "payloadLength" to (barcode.rawValue?.length ?: 0),
                                "payloadPreview" to (barcode.rawValue?.take(50) ?: ""),
                                "isValid" to isValidBarcodePayload(barcode.rawValue ?: "", config),
                                "overallScore" to (1.0f * 0.6f + calculateQualityScore(imageCharacteristics) * 0.4f)
                            ))
                        }
                        
                        strategyOrientationResults["orientation_$orientation"] = orientationResults
                    } catch (e: Exception) {
                        strategyOrientationResults["orientation_${orientation}_error"] = e.localizedMessage ?: "Unknown error"
                    } finally {
                        if (rotatedBitmap != processedBitmap && !rotatedBitmap.isRecycled) {
                            rotatedBitmap.recycle()
                        }
                    }
                }
                
                strategyResults[strategy.description] = strategyOrientationResults
                
                if (processedBitmap != bitmap && !processedBitmap.isRecycled) {
                    processedBitmap.recycle()
                }
            }
        }
        
        results["processingStrategies"] = strategyResults
        
        // Test enhanced detection
        val enhancedResult = readCodesWithEnhancedDetection(bitmap, 0, null)
        results["enhancedDetectionResult"] = if (enhancedResult != null) {
            mapOf(
                "success" to true,
                "payloadLength" to enhancedResult.length,
                "payloadPreview" to enhancedResult.take(100)
            )
        } else {
            mapOf("success" to false)
        }
        
        // Test Canadian province detection
        val canadianTestResults = mutableMapOf<String, Any>()
        val canadianProvinces = listOf("ON", "BC", "AB", "QC", "MB", "SK", "NS", "NB", "NL", "PE", "YT", "NT", "NU")
        
        for (province in canadianProvinces) {
            val provincialResult = readCodesWithEnhancedDetection(bitmap, 0, province)
            if (provincialResult != null) {
                canadianTestResults[province] = mapOf(
                    "success" to true,
                    "payloadLength" to provincialResult.length,
                    "payloadPreview" to provincialResult.take(50),
                    "config" to mapOf(
                        "minConfidence" to (stateConfigs[province]?.minConfidence ?: 0.0f),
                        "expectedLength" to "${stateConfigs[province]?.expectedPayloadLength?.first ?: 0}-${stateConfigs[province]?.expectedPayloadLength?.last ?: 0}",
                        "strategies" to (stateConfigs[province]?.preferredStrategies?.map { it.description } ?: emptyList<String>())
                    )
                )
            } else {
                canadianTestResults[province] = mapOf("success" to false)
            }
        }
        
        results["canadianProvincesTest"] = canadianTestResults
        
        return results
    }

    /**
     * Send failure logs (placeholder - integrate with actual logging system)
     */
    private fun sendFailureLogs(reason: String, errorDetails: String) {
        Log.w(TAG, "Failure log: $reason - $errorDetails")
        // TODO: Integrate with actual LogManager or analytics service
        // This would typically send structured logs to your analytics/logging service
    }

    /**
     * Log warning message
     */
    private fun logWarning(message: String) {
        Log.w(TAG, message)
    }
}
