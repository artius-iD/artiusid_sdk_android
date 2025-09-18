/*
 * File: DocumentScanManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.tasks.await
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlin.math.sqrt
import kotlin.math.min
import kotlin.math.max

enum class DocumentSide {
    FRONT, BACK
}

data class DocumentScanResult(
    val validationStatus: String,
    val confidence: Float,
    val barcodeData: String? = null,
    val isSuccess: Boolean = false,
    val bitmap: Bitmap? = null,
    val documentBounds: Rect? = null
)

class DocumentScanManager {
    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    
    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
        ObjectDetection.getClient(options)
    }
    
    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.1f)
            .build()
        FaceDetection.getClient(options)
    }
    
    // PDF417 barcode scanner with iOS-style optimization
    private val pdf417Scanner by lazy {
        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417)
            .build()
        Log.d("DocumentScanManager", "PDF417 scanner configured for iOS-style detection")
        BarcodeScanning.getClient(options)
    }

    // iOS-style image preprocessing for barcode detection
    private fun preprocessImageForBarcodeDetection(bitmap: Bitmap): Bitmap {
        Log.d("DocumentScanManager", "Starting iOS-style image preprocessing")
        
        // Step 1: Convert to grayscale (like iOS CIPhotoEffectMono)
        val grayscaleBitmap = convertToGrayscale(bitmap)
        Log.d("DocumentScanManager", "Grayscale conversion completed")
        
        // Step 2: Apply contrast enhancement (like iOS CIColorControls with contrast=1.2)
        val contrastBitmap = enhanceContrast(grayscaleBitmap, 1.2f)
        Log.d("DocumentScanManager", "Contrast enhancement completed")
        
        // Step 3: Apply brightness adjustment (like iOS CIColorControls with brightness=0.0)
        val brightnessBitmap = adjustBrightness(contrastBitmap, 0.0f)
        Log.d("DocumentScanManager", "Brightness adjustment completed")
        
        // Step 4: Apply sharpening (like iOS CISharpenLuminance with sharpness=0.5)
        val sharpenedBitmap = sharpenImage(brightnessBitmap, 0.5f)
        Log.d("DocumentScanManager", "Sharpening completed")
        
        Log.d("DocumentScanManager", "iOS-style preprocessing completed")
        return sharpenedBitmap
    }
    
    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        
        // Use ColorMatrix for fast grayscale conversion
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // This converts to grayscale
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return grayscaleBitmap
    }
    
    private fun enhanceContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(contrastBitmap)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setScale(contrast, contrast, contrast, 1f) // Apply contrast
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return contrastBitmap
    }
    
    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        Log.d("DocumentScanManager", "Starting brightness adjustment with factor: $brightness")
        
        val width = bitmap.width
        val height = bitmap.height
        val brightnessBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(brightnessBitmap)
        val paint = Paint()
        
        // Use ColorMatrix for much faster brightness adjustment
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(1f + brightness, 1f + brightness, 1f + brightness, 1f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        Log.d("DocumentScanManager", "Brightness adjustment completed")
        return brightnessBitmap
    }
    
    private fun sharpenImage(bitmap: Bitmap, sharpness: Float): Bitmap {
        Log.d("DocumentScanManager", "Starting sharpening with factor: $sharpness")
        
        // Use a much simpler and faster sharpening approach
        val width = bitmap.width
        val height = bitmap.height
        val sharpenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(sharpenedBitmap)
        val paint = Paint()
        
        // Apply a simple sharpening filter using ColorMatrix
        val colorMatrix = ColorMatrix()
        // Create a simple sharpening kernel effect
        colorMatrix.setScale(1f + sharpness, 1f + sharpness, 1f + sharpness, 1f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        Log.d("DocumentScanManager", "Sharpening completed")
        return sharpenedBitmap
    }
    


    // Direct MLKit PDF417 detection
    private suspend fun detectPDF417BarcodeDirectly(image: InputImage): String? {
        return try {
            Log.d("DocumentScanManager", "MLKit: Scanning for PDF417 barcodes...")
            
            val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_PDF417)
                .build())
            
            val result = withContext(Dispatchers.IO) {
                scanner.process(image).await()
            }
            
            Log.d("DocumentScanManager", "MLKit: Found ${result.size} barcodes total")
            
            for (barcode in result) {
                if (barcode.format == Barcode.FORMAT_PDF417) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        Log.d("DocumentScanManager", "MLKit: PDF417 barcode detected with ${rawValue.length} characters")
                        return rawValue
                    }
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error in MLKit PDF417 detection: ${e.message}", e)
            null
        }
    }

    // Main scanning function that continuously processes frames until barcode is found
    suspend fun scanDocumentForBarcode(bitmap: Bitmap, side: DocumentSide): String? = withContext(Dispatchers.Default) {
        Log.d("DocumentScanManager", "=== STARTING ${side.name} DOCUMENT SCAN ===")
        Log.d("DocumentScanManager", "Original bitmap size: ${bitmap.width}x${bitmap.height}")
        Log.d("DocumentScanManager", "Bitmap config: ${bitmap.config}")
        
        return@withContext try {
            // Step 1: Apply iOS-style preprocessing (no cropping)
            val preprocessedBitmap = applyIOSStylePreprocessing(bitmap)
            Log.d("DocumentScanManager", "Preprocessed bitmap size: ${preprocessedBitmap.width}x${preprocessedBitmap.height}")
            
            // Step 2: Save preprocessed image for debugging (DISABLED)
            // val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            // val filename = "preprocessed_${side.name.lowercase()}_scan_$timestamp.png"
            // saveBitmapToPictures(preprocessedBitmap, filename)
            // Log.d("DocumentScanManager", "Saved preprocessed image to: /storage/emulated/0/Pictures/$filename")
            
            // Step 3: Try MLKit barcode detection in all orientations
            val barcodeData = tryMultipleOrientations(preprocessedBitmap)
            
            if (barcodeData != null) {
                Log.d("DocumentScanManager", "SUCCESS: PDF417 barcode detected: ${barcodeData.take(50)}...")
                Log.d("DocumentScanManager", "=== ${side.name} DOCUMENT SCAN COMPLETED - BARCODE FOUND ===")
                barcodeData
            } else {
                Log.d("DocumentScanManager", "FAILED: No PDF417 barcode detected")
                Log.d("DocumentScanManager", "=== ${side.name} DOCUMENT SCAN COMPLETED - NO BARCODE FOUND ===")
                null
            }
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error during ${side.name} document scan", e)
            null
        }
    }

    // Apply iOS-style preprocessing (tuned for Android)
    private fun applyIOSStylePreprocessing(bitmap: Bitmap): Bitmap {
        Log.d("DocumentScanManager", "Applying iOS-style preprocessing (tuned)")
        try {
            // Step 1: Convert to grayscale
            val grayscaleBitmap = convertToGrayscale(bitmap)
            Log.d("DocumentScanManager", "Grayscale conversion completed")
            // Step 2: Apply contrast enhancement (lowered)
            val contrastBitmap = enhanceContrast(grayscaleBitmap, 1.1f)
            Log.d("DocumentScanManager", "Contrast enhancement completed")
            // Step 3: Apply brightness adjustment (neutral)
            val brightnessBitmap = adjustBrightness(contrastBitmap, 0.0f)
            Log.d("DocumentScanManager", "Brightness adjustment completed")
            // Step 4: Apply sharpening (mild)
            val sharpenedBitmap = sharpenImage(brightnessBitmap, 0.3f)
            Log.d("DocumentScanManager", "Sharpening completed")
            Log.d("DocumentScanManager", "iOS-style preprocessing completed successfully")
            return sharpenedBitmap
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error in iOS-style preprocessing: ${e.message}", e)
            return bitmap // Return original if preprocessing fails
        }
    }

    // Try multiple orientations for PDF417 detection
    private suspend fun tryMultipleOrientations(bitmap: Bitmap): String? {
        val orientations = listOf(0, 90, 180, 270)
        
        for (rotation in orientations) {
            try {
                Log.d("DocumentScanManager", "Trying orientation: ${rotation}°")
                
                val rotatedBitmap = if (rotation == 0) {
                    bitmap
                } else {
                    rotateBitmap(bitmap, rotation)
                }
                
                val image = InputImage.fromBitmap(rotatedBitmap, 0)
                val result = detectPDF417BarcodeDirectly(image)
                
                if (result != null) {
                    Log.d("DocumentScanManager", "PDF417 barcode found at ${rotation}° orientation")
                    return result
                }
                
            } catch (e: Exception) {
                Log.e("DocumentScanManager", "Error trying orientation ${rotation}°: ${e.message}")
            }
        }
        
        Log.d("DocumentScanManager", "No PDF417 barcodes found in any orientation")
        return null
    }

    // Rotate bitmap
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Legacy scanDocument method for backward compatibility
    suspend fun scanDocument(bitmap: Bitmap, side: DocumentSide): DocumentScanResult {
        Log.d("DocumentScanManager", "Legacy scanDocument called for ${side.name}")
        
        return try {
            when (side) {
                DocumentSide.FRONT -> {
                    // For front scan, just validate document presence
                    val faces = detectFaces(bitmap)
                    if (faces.isNotEmpty()) {
                        DocumentScanResult("Valid", 0.9f, null)
                    } else {
                        DocumentScanResult("No face detected on ID", 0.0f, null)
                    }
                }
                DocumentSide.BACK -> {
                    // For back scan, try barcode detection
                    val barcodeData = scanDocumentForBarcode(bitmap, side)
                    if (barcodeData != null) {
                        DocumentScanResult("PDF417 barcode detected", 0.9f, barcodeData)
                    } else {
                        DocumentScanResult("No barcode detected", 0.0f, null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error in legacy scanDocument: ${e.message}")
            DocumentScanResult("Error: ${e.message}", 0.0f, null)
        }
    }

    // Legacy methods for backward compatibility
    suspend fun detectPDF417BarcodeInBottomArea(bitmap: Bitmap): String? = withContext(kotlinx.coroutines.Dispatchers.IO) {
        Log.d("DocumentScanManager", "Attempting PDF417 detection in bottom area of ID")
        
        try {
            val bottomAreaHeight = (bitmap.height * 0.3).toInt()
            val topY = bitmap.height - bottomAreaHeight
            
            Log.d("DocumentScanManager", "Cropping bottom area: y=$topY, height=$bottomAreaHeight")
            
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, topY, bitmap.width, bottomAreaHeight)
            
            val processingMethods = listOf(
                { croppedBitmap },
                { preprocessImageForBarcodeDetection(croppedBitmap) },
                { convertToGrayscale(croppedBitmap) }
            )
            
            for ((index, method) in processingMethods.withIndex()) {
                try {
                    val processedBitmap = method()
                    val image = InputImage.fromBitmap(processedBitmap, 0)
                    val result = detectPDF417BarcodeDirectly(image)
                    if (result != null) {
                        Log.d("DocumentScanManager", "PDF417 detected in bottom area with method ${index + 1}")
                        return@withContext result
                    }
                } catch (e: Exception) {
                    Log.e("DocumentScanManager", "Error in bottom area method ${index + 1}: ${e.message}")
                }
            }
            
            Log.w("DocumentScanManager", "PDF417 not detected in bottom area")
            return@withContext null
            
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error cropping bottom area: ${e.message}")
            return@withContext null
        }
    }

    suspend fun detectPDF417BarcodeRobustly(bitmap: Bitmap): String? = withContext(kotlinx.coroutines.Dispatchers.IO) {
        Log.d("DocumentScanManager", "Starting robust PDF417 detection on ${bitmap.width}x${bitmap.height} bitmap")
        
        // Try iOS-style detection first
        val iosResult = detectPDF417BarcodeDirectly(InputImage.fromBitmap(bitmap, 0))
        if (iosResult != null) {
            Log.d("DocumentScanManager", "iOS-style detection succeeded")
            return@withContext iosResult
        }
        
        // Fallback to other methods
        val bottomAreaResult = detectPDF417BarcodeInBottomArea(bitmap)
        if (bottomAreaResult != null) {
            Log.d("DocumentScanManager", "Bottom area detection succeeded")
            return@withContext bottomAreaResult
        }
        
        Log.w("DocumentScanManager", "All detection methods failed")
        return@withContext null
    }

    private fun enhanceBitmapForBarcodeDetection(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(enhancedBitmap)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f) // Grayscale
        colorMatrix.setScale(1.5f, 1.5f, 1.5f, 1f) // High contrast
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return enhancedBitmap
    }

    private fun createHighContrastBitmap(bitmap: Bitmap): Bitmap {
        // Fast ColorMatrix-based contrast enhancement
        val width = bitmap.width
        val height = bitmap.height
        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(contrastBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(2.0f, 2.0f, 2.0f, 1f) // High contrast
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return contrastBitmap
    }

    private fun invertBitmap(bitmap: Bitmap): Bitmap {
        // Fast ColorMatrix-based inversion
        val width = bitmap.width
        val height = bitmap.height
        val invertedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(invertedBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        // Simple inversion matrix
        colorMatrix.set(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return invertedBitmap
    }

    private fun createUltraHighContrastBitmap(bitmap: Bitmap): Bitmap {
        // Fast ColorMatrix-based ultra contrast (threshold)
        val width = bitmap.width
        val height = bitmap.height
        val ultraContrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(ultraContrastBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(10.0f, 10.0f, 10.0f, 1f) // Ultra high contrast
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return ultraContrastBitmap
    }

    // Text recognition methods
    suspend fun extractText(bitmap: Bitmap): List<String> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            
            return@withContext result.textBlocks.map { it.text }
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error extracting text: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Object detection methods
    suspend fun detectObjects(bitmap: Bitmap): List<Rect> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = objectDetector.process(image).await()
            
            return@withContext result.mapNotNull { it.boundingBox }
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error detecting objects: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Face detection methods
    suspend fun detectFaces(bitmap: Bitmap): List<Rect> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = faceDetector.process(image).await()
            
            return@withContext result.mapNotNull { it.boundingBox }
        } catch (e: Exception) {
            Log.e("DocumentScanManager", "Error detecting faces: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Remove or comment out the saveBitmapToPictures function implementation
    // private fun saveBitmapToPictures(bitmap: Bitmap, prefix: String) {
    //     try {
    //         Log.d("DocumentScanManager", "Attempting to save bitmap: ${bitmap.width}x${bitmap.height}")
            
    //         val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
    //         val filename = "${prefix}_${timestamp}.png"
            
    //         val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    //         Log.d("DocumentScanManager", "Pictures directory: ${picturesDir.absolutePath}")
    //         Log.d("DocumentScanManager", "Directory exists: ${picturesDir.exists()}")
    //         Log.d("DocumentScanManager", "Directory writable: ${picturesDir.canWrite()}")
            
    //         val file = File(picturesDir, filename)
    //         Log.d("DocumentScanManager", "Target file: ${file.absolutePath}")
            
    //         val outputStream = FileOutputStream(file)
    //         val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    //         outputStream.close()
            
    //         if (success) {
    //             Log.d("DocumentScanManager", "SUCCESS: Saved preprocessed image to: ${file.absolutePath}")
    //             Log.d("DocumentScanManager", "File size: ${file.length()} bytes")
    //             Log.d("DocumentScanManager", "Preprocessed bitmap size: ${bitmap.width}x${bitmap.height}")
    //         } else {
    //             Log.e("DocumentScanManager", "FAILED: Bitmap compression failed")
    //         }
    //     } catch (e: Exception) {
    //         Log.e("DocumentScanManager", "Error saving bitmap: ${e.message}")
    //         Log.e("DocumentScanManager", "Exception type: ${e.javaClass.simpleName}")
    //         e.printStackTrace()
    //     }
    // }
} 