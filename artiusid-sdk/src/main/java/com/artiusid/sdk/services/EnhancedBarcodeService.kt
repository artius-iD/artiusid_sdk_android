package com.artiusid.sdk.services

import android.content.Context
import android.graphics.*
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.artiusid.sdk.utils.AAMVABarcodeParser
import com.artiusid.sdk.models.AAMVAData
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced barcode scanning service with iOS-style image preprocessing
 * Optimized for PDF417 barcodes on driver's licenses and ID cards
 */
class EnhancedBarcodeService(private val context: Context) {
    
    companion object {
        private const val TAG = "EnhancedBarcodeService"
    }
    
    // PDF417 barcode scanner optimized for driver's licenses
    private val pdf417Scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417)
            .build()
        Log.d(TAG, "PDF417 scanner configured for driver's license detection")
        BarcodeScanning.getClient(options)
    }
    
    // General barcode scanner for other formats
    private val generalScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
        BarcodeScanning.getClient(options)
    }
    
    /**
     * Scan barcode from bitmap with enhanced preprocessing
     */
    suspend fun scanBarcode(
        bitmap: Bitmap,
        documentType: String = "DRIVERS_LICENSE"
    ): BarcodeResult {
        return try {
            Log.d(TAG, "Starting enhanced barcode scan for document type: $documentType")
            
            // Apply iOS-style preprocessing for better barcode detection
            val preprocessedBitmap = preprocessImageForBarcodeDetection(bitmap)
            
            // Try PDF417 scanner first (most common for driver's licenses)
            val pdf417Result = scanWithPDF417(preprocessedBitmap)
            if (pdf417Result.success) {
                Log.d(TAG, "PDF417 barcode detected successfully")
                return pdf417Result
            }
            
            // Fall back to general scanner
            Log.d(TAG, "PDF417 failed, trying general barcode scanner")
            val generalResult = scanWithGeneralScanner(preprocessedBitmap)
            if (generalResult.success) {
                Log.d(TAG, "General barcode scanner detected barcode")
                return generalResult
            }
            
            // Try with original image if preprocessing failed
            Log.d(TAG, "Preprocessed scan failed, trying original image")
            val originalResult = scanWithPDF417(bitmap)
            if (originalResult.success) {
                return originalResult
            }
            
            Log.d(TAG, "No barcode detected in image")
            BarcodeResult(
                success = false,
                barcodeData = null,
                barcodeFormat = null,
                confidence = 0.0f,
                processingTime = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during barcode scanning", e)
            BarcodeResult(
                success = false,
                barcodeData = null,
                barcodeFormat = null,
                confidence = 0.0f,
                processingTime = System.currentTimeMillis(),
                error = e.message
            )
        }
    }
    
    /**
     * Scan with PDF417 scanner specifically
     */
    private suspend fun scanWithPDF417(bitmap: Bitmap): BarcodeResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val barcodes = pdf417Scanner.process(inputImage).await()
        
        return if (barcodes.isNotEmpty()) {
            val barcode = barcodes[0]
            val rawValue = barcode.rawValue ?: ""
            
            Log.d(TAG, "PDF417 barcode found: ${rawValue.take(100)}...")
            
            BarcodeResult(
                success = true,
                barcodeData = rawValue,
                barcodeFormat = "PDF417",
                confidence = 0.95f, // PDF417 is highly reliable when detected
                processingTime = System.currentTimeMillis(),
                aamvaData = if (rawValue.isNotEmpty()) {
                    val parsedData = AAMVABarcodeParser.parseBarcode(rawValue)
                    // Convert AAMVABarcodeParser.AAMVAData to models.AAMVAData
                    AAMVAData(
                        firstName = parsedData?.firstName ?: "",
                        lastName = parsedData?.lastName ?: "",
                        middleName = parsedData?.firstName ?: "", // Using firstName as middleName fallback
                        dateOfBirth = parsedData?.dateOfBirth ?: "",
                        sex = parsedData?.firstName ?: "", // Simplified mapping
                        licenseNumber = parsedData?.licenseNumber ?: "",
                        address = parsedData?.address ?: "",
                        city = parsedData?.city ?: "",
                        state = parsedData?.state ?: "",
                        zipCode = parsedData?.zipCode ?: "",
                        issueDate = parsedData?.issueDate ?: "",
                        expirationDate = parsedData?.expirationDate ?: "",
                        rawData = mapOf("rawValue" to rawValue)
                    )
                } else null
            )
        } else {
            BarcodeResult(
                success = false,
                barcodeData = null,
                barcodeFormat = null,
                confidence = 0.0f,
                processingTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Scan with general barcode scanner
     */
    private suspend fun scanWithGeneralScanner(bitmap: Bitmap): BarcodeResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val barcodes = generalScanner.process(inputImage).await()
        
        return if (barcodes.isNotEmpty()) {
            val barcode = barcodes[0]
            val rawValue = barcode.rawValue ?: ""
            val format = when (barcode.format) {
                Barcode.FORMAT_PDF417 -> "PDF417"
                Barcode.FORMAT_CODE_128 -> "CODE_128"
                Barcode.FORMAT_CODE_39 -> "CODE_39"
                Barcode.FORMAT_QR_CODE -> "QR_CODE"
                Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
                else -> "UNKNOWN"
            }
            
            Log.d(TAG, "General barcode found ($format): ${rawValue.take(100)}...")
            
            BarcodeResult(
                success = true,
                barcodeData = rawValue,
                barcodeFormat = format,
                confidence = 0.85f,
                processingTime = System.currentTimeMillis(),
                aamvaData = if (format == "PDF417" && rawValue.isNotEmpty()) {
                    val parsedData = AAMVABarcodeParser.parseBarcode(rawValue)
                    // Convert AAMVABarcodeParser.AAMVAData to models.AAMVAData
                    AAMVAData(
                        firstName = parsedData?.firstName ?: "",
                        lastName = parsedData?.lastName ?: "",
                        middleName = parsedData?.firstName ?: "", // Using firstName as middleName fallback
                        dateOfBirth = parsedData?.dateOfBirth ?: "",
                        sex = parsedData?.firstName ?: "", // Simplified mapping
                        licenseNumber = parsedData?.licenseNumber ?: "",
                        address = parsedData?.address ?: "",
                        city = parsedData?.city ?: "",
                        state = parsedData?.state ?: "",
                        zipCode = parsedData?.zipCode ?: "",
                        issueDate = parsedData?.issueDate ?: "",
                        expirationDate = parsedData?.expirationDate ?: "",
                        rawData = mapOf("rawValue" to rawValue)
                    )
                } else null
            )
        } else {
            BarcodeResult(
                success = false,
                barcodeData = null,
                barcodeFormat = null,
                confidence = 0.0f,
                processingTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * iOS-style image preprocessing for barcode detection
     */
    private fun preprocessImageForBarcodeDetection(bitmap: Bitmap): Bitmap {
        Log.d(TAG, "Starting iOS-style image preprocessing")
        
        // Step 1: Convert to grayscale (like iOS CIPhotoEffectMono)
        val grayscaleBitmap = convertToGrayscale(bitmap)
        Log.d(TAG, "Grayscale conversion completed")
        
        // Step 2: Apply contrast enhancement (like iOS CIColorControls with contrast=1.2)
        val contrastBitmap = enhanceContrast(grayscaleBitmap, 1.2f)
        Log.d(TAG, "Contrast enhancement completed")
        
        // Step 3: Apply brightness adjustment (like iOS CIColorControls with brightness=0.0)
        val brightnessBitmap = adjustBrightness(contrastBitmap, 0.0f)
        Log.d(TAG, "Brightness adjustment completed")
        
        // Step 4: Apply sharpening filter (like iOS CIUnsharpMask)
        val sharpenedBitmap = applySharpeningFilter(brightnessBitmap)
        Log.d(TAG, "Sharpening filter applied")
        
        return sharpenedBitmap
    }
    
    /**
     * Convert bitmap to grayscale
     */
    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Remove all color saturation
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return grayscaleBitmap
    }
    
    /**
     * Enhance contrast of the bitmap
     */
    private fun enhanceContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(contrastBitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        val scale = contrast + 1f
        val translate = (-.5f * scale + .5f) * 255f
        
        colorMatrix.set(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return contrastBitmap
    }
    
    /**
     * Adjust brightness of the bitmap
     */
    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val brightnessBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(brightnessBitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        val brightnessValue = brightness * 255f
        
        colorMatrix.set(floatArrayOf(
            1f, 0f, 0f, 0f, brightnessValue,
            0f, 1f, 0f, 0f, brightnessValue,
            0f, 0f, 1f, 0f, brightnessValue,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return brightnessBitmap
    }
    
    /**
     * Apply sharpening filter to enhance barcode edges
     */
    private fun applySharpeningFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sharpenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Simple sharpening kernel
        val kernel = arrayOf(
            intArrayOf(0, -1, 0),
            intArrayOf(-1, 5, -1),
            intArrayOf(0, -1, 0)
        )
        
        val newPixels = IntArray(width * height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0
                var g = 0
                var b = 0
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]
                        val weight = kernel[ky + 1][kx + 1]
                        
                        r += ((pixel shr 16) and 0xFF) * weight
                        g += ((pixel shr 8) and 0xFF) * weight
                        b += (pixel and 0xFF) * weight
                    }
                }
                
                r = max(0, min(255, r))
                g = max(0, min(255, g))
                b = max(0, min(255, b))
                
                newPixels[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        sharpenedBitmap.setPixels(newPixels, 0, width, 0, 0, width, height)
        
        return sharpenedBitmap
    }
    
    /**
     * Release resources
     */
    fun release() {
        pdf417Scanner.close()
        generalScanner.close()
    }
}

/**
 * Result from barcode scanning operation
 */
data class BarcodeResult(
    val success: Boolean,
    val barcodeData: String?,
    val barcodeFormat: String?,
    val confidence: Float,
    val processingTime: Long,
    val aamvaData: AAMVAData? = null,
    val error: String? = null
)
