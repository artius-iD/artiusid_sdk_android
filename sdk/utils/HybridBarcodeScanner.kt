/*
 * File: HybridBarcodeScanner.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// HybridBarcodeScanner.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.pdf417.PDF417Reader
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class HybridBarcodeScanner {
    companion object {
        private const val TAG = "HybridBarcodeScanner"
        private const val MIN_CONFIDENCE = 0.1f
        private const val MIN_PAYLOAD_LENGTH = 50 // PDF417 on IDs typically has substantial data
    }

    private val mlKitScanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417)
            .build()
        Log.d(TAG, "MLKit scanner initialized for PDF417")
        BarcodeScanning.getClient(options)
    }

    private val zxingReader: PDF417Reader by lazy {
        Log.d(TAG, "ZXing PDF417 reader initialized")
        PDF417Reader()
    }

    suspend fun scanBarcode(bitmap: Bitmap, deviceOrientation: Int = 0): BarcodeResult? {
        Log.d(TAG, "Starting hybrid barcode scan on ${bitmap.width}x${bitmap.height} bitmap")
        Log.d(TAG, "Bitmap config: ${bitmap.config}, isMutable: ${bitmap.isMutable}")
        
        // Validate input
        if (bitmap.isRecycled) {
            Log.w(TAG, "Bitmap is recycled, cannot process")
            return null
        }

        // iOS-style optimized orientation strategy: prioritize likely orientations for ID cards
        val orientationsToTry = getOrientationPriority(deviceOrientation)
        
        for ((index, orientation) in orientationsToTry.withIndex()) {
            Log.d(TAG, "Barcode scanner attempt ${index + 1}: trying orientation $orientation")
            
            val rotatedBitmap = when (orientation) {
                0 -> bitmap // Original orientation
                90 -> rotateBitmap(bitmap, 90f)
                180 -> rotateBitmap(bitmap, 180f)
                270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
            
            try {
                // Try MLKit first with current orientation
                val mlKitResult = tryMlKit(rotatedBitmap, "Orientation_$orientation")
                if (mlKitResult != null && validateBarcodeResult(mlKitResult)) {
                    Log.d(TAG, "MLKit successfully detected barcode with orientation $orientation: ${mlKitResult.rawValue.take(50)}...")
                    return mlKitResult.copy(strategy = "MLKit_Orientation_$orientation")
                }

                // If MLKit fails, try ZXing with current orientation
                val zxingResult = tryZxing(rotatedBitmap, "Orientation_$orientation")
                if (zxingResult != null && validateBarcodeResult(zxingResult)) {
                    Log.d(TAG, "ZXing successfully detected barcode with orientation $orientation: ${zxingResult.rawValue.take(50)}...")
                    return zxingResult.copy(strategy = "ZXing_Orientation_$orientation")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Failed to perform barcode detection with orientation $orientation: ${e.message}")
            } finally {
                // Clean up rotated bitmap if it's different from original
                if (rotatedBitmap != bitmap && !rotatedBitmap.isRecycled) {
                    rotatedBitmap.recycle()
                }
            }
        }

        // If orientation-based detection fails, try enhanced processing strategies as fallback
        Log.d(TAG, "Orientation-based detection failed, trying enhanced processing strategies...")
        val strategies = listOf(
            { "Enhanced Contrast" to enhanceContrast(bitmap) },
            { "Grayscale" to convertToGrayscale(bitmap) },
            { "High Contrast" to createHighContrast(bitmap) },
            { "Inverted" to invertBitmap(bitmap) },
            { "Cropped Bottom 30%" to cropBottomArea(bitmap, 0.3f) },
            { "Cropped Bottom 50%" to cropBottomArea(bitmap, 0.5f) },
            { "Cropped Center" to cropCenterArea(bitmap, 0.6f) }
        )
        
        for (strategy in strategies) {
            val (strategyName, processedBitmap) = strategy()
            Log.d(TAG, "Trying enhanced strategy: $strategyName")
            
            // Try MLKit first with enhanced image
            val mlKitResult = tryMlKit(processedBitmap, strategyName)
            if (mlKitResult != null && validateBarcodeResult(mlKitResult)) {
                Log.d(TAG, "MLKit successfully detected barcode with strategy '$strategyName': ${mlKitResult.rawValue.take(50)}...")
                return mlKitResult
            }

            // If MLKit fails, try ZXing with enhanced image
            Log.d(TAG, "MLKit failed with strategy '$strategyName', trying ZXing...")
            val zxingResult = tryZxing(processedBitmap, strategyName)
            if (zxingResult != null && validateBarcodeResult(zxingResult)) {
                Log.d(TAG, "ZXing successfully detected barcode with strategy '$strategyName': ${zxingResult.rawValue.take(50)}...")
                return zxingResult
            }
        }

        Log.w(TAG, "Both MLKit and ZXing failed to detect barcode with all strategies and orientations")
        
        // Send failure logs when barcode cannot be read (matching iOS pattern)
        sendFailureLogs(
            reason = "Barcode reading failed", 
            errorDetails = "Could not detect any PDF417 barcode data with any orientation or enhancement strategy after multiple attempts"
        )
        
        return null
    }

    private suspend fun tryMlKit(bitmap: Bitmap, strategyName: String): BarcodeResult? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        Log.d(TAG, "MLKit processing ${bitmap.width}x${bitmap.height} image with strategy: $strategyName")
        
        mlKitScanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d(TAG, "MLKit returned ${barcodes.size} barcodes with strategy: $strategyName")
                
                val barcode = barcodes.firstOrNull { it.format == Barcode.FORMAT_PDF417 }
                if (barcode != null) {
                    Log.d(TAG, "MLKit found PDF417 barcode with strategy: $strategyName")
                    Log.d(TAG, "Barcode bounds: ${barcode.boundingBox}")
                    Log.d(TAG, "Barcode value length: ${barcode.rawValue?.length ?: 0}")
                    
                    val result = BarcodeResult(
                        rawValue = barcode.rawValue ?: "",
                        format = "PDF417",
                        boundingBox = barcode.boundingBox,
                        scanner = "MLKit",
                        strategy = strategyName
                    )
                    continuation.resume(result)
                } else {
                    Log.d(TAG, "MLKit found ${barcodes.size} barcodes but none were PDF417 with strategy: $strategyName")
                    barcodes.forEach { barcode ->
                        Log.d(TAG, "Found barcode format: ${barcode.format}, value type: ${barcode.valueType}")
                    }
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "MLKit scanning failed with strategy: $strategyName", exception)
                continuation.resume(null)
            }
    }

    private fun tryZxing(bitmap: Bitmap, strategyName: String): BarcodeResult? {
        return try {
            Log.d(TAG, "ZXing processing ${bitmap.width}x${bitmap.height} image with strategy: $strategyName")
            
            // Convert bitmap to binary data
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            // Create luminance source
            val source = RGBLuminanceSource(width, height, pixels)
            val binarizer = HybridBinarizer(source)
            val bitmap2 = BinaryBitmap(binarizer)

            // Try to decode with multiple hint combinations
            val hintSets = listOf(
                mapOf(
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.PURE_BARCODE to true
                ),
                mapOf(
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.PURE_BARCODE to false
                ),
                mapOf(
                    DecodeHintType.TRY_HARDER to false,
                    DecodeHintType.PURE_BARCODE to true
                )
            )
            
            for ((hintIndex, hints) in hintSets.withIndex()) {
                try {
                    Log.d(TAG, "ZXing trying hint set ${hintIndex + 1} with strategy: $strategyName")
                    val result = zxingReader.decode(bitmap2, hints)
                    
                    Log.d(TAG, "ZXing successfully decoded with hint set ${hintIndex + 1} and strategy: $strategyName")
                    Log.d(TAG, "ZXing result bounds: ${result.resultPoints.joinToString { "(${it.x},${it.y})" }}")
                    
                    return BarcodeResult(
                        rawValue = result.text,
                        format = "PDF417",
                        boundingBox = Rect(
                            result.resultPoints[0].x.toInt(),
                            result.resultPoints[0].y.toInt(),
                            result.resultPoints[2].x.toInt(),
                            result.resultPoints[2].y.toInt()
                        ),
                        scanner = "ZXing",
                        strategy = "$strategyName (hints:$hintIndex)"
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "ZXing hint set ${hintIndex + 1} failed with strategy: $strategyName - ${e.message}")
                }
            }
            
            Log.d(TAG, "ZXing failed with all hint sets for strategy: $strategyName")
            null
        } catch (e: Exception) {
            Log.e(TAG, "ZXing scanning failed with strategy: $strategyName", e)
            null
        }
    }

    data class BarcodeResult(
        val rawValue: String,
        val format: String,
        val boundingBox: Rect?,
        val scanner: String,
        val strategy: String = ""
    )

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
     * Enhanced validation: check confidence and payload quality (matching iOS logic)
     */
    private fun validateBarcodeResult(result: BarcodeResult): Boolean {
        val payload = result.rawValue
        
        if (payload.isEmpty()) {
            Log.d(TAG, "Barcode filtered out: empty payload")
            return false
        }
        
        if (payload.length < MIN_PAYLOAD_LENGTH) {
            Log.d(TAG, "Barcode filtered out: payload too short (${payload.length} < $MIN_PAYLOAD_LENGTH)")
            return false
        }
        
        Log.d(TAG, "Valid barcode found: length=${payload.length}")
        return true
    }

    /**
     * Send failure logs (placeholder - integrate with actual logging system)
     */
    private fun sendFailureLogs(reason: String, errorDetails: String) {
        Log.w(TAG, "Failure log: $reason - $errorDetails")
        // TODO: Integrate with actual LogManager equivalent for Android
        // This would typically send structured logs to your analytics/logging service
    }

    // Image processing methods
    private fun enhanceContrast(bitmap: Bitmap): Bitmap {
        val enhancedBitmap = bitmap.copy(bitmap.config, true)
        val pixels = IntArray(enhancedBitmap.width * enhancedBitmap.height)
        enhancedBitmap.getPixels(pixels, 0, enhancedBitmap.width, 0, 0, enhancedBitmap.width, enhancedBitmap.height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)
            
            val factor = 1.8f
            val newR = ((r - 128) * factor + 128).toInt().coerceIn(0, 255)
            val newG = ((g - 128) * factor + 128).toInt().coerceIn(0, 255)
            val newB = ((b - 128) * factor + 128).toInt().coerceIn(0, 255)
            
            pixels[i] = android.graphics.Color.rgb(newR, newG, newB)
        }
        
        enhancedBitmap.setPixels(pixels, 0, enhancedBitmap.width, 0, 0, enhancedBitmap.width, enhancedBitmap.height)
        return enhancedBitmap
    }
    
    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val grayscaleBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(grayscaleBitmap)
        val paint = android.graphics.Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix().apply {
                setSaturation(0f)
            })
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return grayscaleBitmap
    }
    
    private fun createHighContrast(bitmap: Bitmap): Bitmap {
        val contrastBitmap = bitmap.copy(bitmap.config, true)
        val pixels = IntArray(contrastBitmap.width * contrastBitmap.height)
        contrastBitmap.getPixels(pixels, 0, contrastBitmap.width, 0, 0, contrastBitmap.width, contrastBitmap.height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)
            
            val gray = (r * 0.299 + g * 0.587 + b * 0.114).toInt()
            val threshold = 128
            val newValue = if (gray > threshold) 255 else 0
            
            pixels[i] = android.graphics.Color.rgb(newValue, newValue, newValue)
        }
        
        contrastBitmap.setPixels(pixels, 0, contrastBitmap.width, 0, 0, contrastBitmap.width, contrastBitmap.height)
        return contrastBitmap
    }
    
    private fun invertBitmap(bitmap: Bitmap): Bitmap {
        val invertedBitmap = bitmap.copy(bitmap.config, true)
        val pixels = IntArray(invertedBitmap.width * invertedBitmap.height)
        invertedBitmap.getPixels(pixels, 0, invertedBitmap.width, 0, 0, invertedBitmap.width, invertedBitmap.height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = 255 - android.graphics.Color.red(pixel)
            val g = 255 - android.graphics.Color.green(pixel)
            val b = 255 - android.graphics.Color.blue(pixel)
            
            pixels[i] = android.graphics.Color.rgb(r, g, b)
        }
        
        invertedBitmap.setPixels(pixels, 0, invertedBitmap.width, 0, 0, invertedBitmap.width, invertedBitmap.height)
        return invertedBitmap
    }
    
    private fun cropBottomArea(bitmap: Bitmap, ratio: Float): Bitmap {
        val cropHeight = (bitmap.height * ratio).toInt()
        val topY = bitmap.height - cropHeight
        return android.graphics.Bitmap.createBitmap(bitmap, 0, topY, bitmap.width, cropHeight)
    }
    
    private fun cropCenterArea(bitmap: Bitmap, ratio: Float): Bitmap {
        val cropWidth = (bitmap.width * ratio).toInt()
        val cropHeight = (bitmap.height * ratio).toInt()
        val leftX = (bitmap.width - cropWidth) / 2
        val topY = (bitmap.height - cropHeight) / 2
        return android.graphics.Bitmap.createBitmap(bitmap, leftX, topY, cropWidth, cropHeight)
    }

    // Custom luminance source for ZXing
    private class RGBLuminanceSource(
        width: Int,
        height: Int,
        private val pixels: IntArray
    ) : LuminanceSource(width, height) {

        override fun getRow(y: Int, row: ByteArray?): ByteArray {
            val result = row ?: ByteArray(width)
            val offset = y * width
            for (x in 0 until width) {
                val pixel = pixels[offset + x]
                result[x] = ((pixel shr 16 and 0xFF) * 0.299 + 
                            (pixel shr 8 and 0xFF) * 0.587 + 
                            (pixel and 0xFF) * 0.114).toInt().toByte()
            }
            return result
        }

        override fun getMatrix(): ByteArray {
            val result = ByteArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    val pixel = pixels[offset + x]
                    result[offset + x] = ((pixel shr 16 and 0xFF) * 0.299 + 
                                        (pixel shr 8 and 0xFF) * 0.587 + 
                                        (pixel and 0xFF) * 0.114).toInt().toByte()
                }
            }
            return result
        }

        override fun isCropSupported(): Boolean = true

        override fun crop(left: Int, top: Int, width: Int, height: Int): LuminanceSource {
            return RGBLuminanceSource(width, height, pixels)
        }

        override fun isRotateSupported(): Boolean = false

        override fun rotateCounterClockwise(): LuminanceSource {
            throw UnsupportedOperationException("Rotate not supported")
        }

        override fun rotateCounterClockwise45(): LuminanceSource {
            throw UnsupportedOperationException("Rotate not supported")
        }

        override fun invert(): LuminanceSource {
            return InvertedLuminanceSource(this)
        }
    }
} 