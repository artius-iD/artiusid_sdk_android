/*
 * File: ImageUtils.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {
    private const val TAG = "ImageUtils"
    
    // TESTING: Revert to original larger sizes to test if aggressive compression caused 400 error
    private const val MAX_DOCUMENT_IMAGE_SIZE_KB = 1024 // 1MB max for documents (REVERTED)
    private const val MAX_FACE_IMAGE_SIZE_KB = 512      // 512KB max for faces (REVERTED)
    private const val MIN_IMAGE_QUALITY = 30          // Minimum acceptable quality
    
    // Target image dimensions for different types
    private const val MAX_DOCUMENT_WIDTH = 1200
    private const val MAX_DOCUMENT_HEIGHT = 1600
    private const val MAX_FACE_WIDTH = 800
    private const val MAX_FACE_HEIGHT = 600

    /**
     * Convert bitmap to base64 string matching iOS implementation
     * iOS uses compressionQuality: 1.0 (100%) and standard base64 encoding
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // TESTING: Revert to 100% quality to test if compression change caused 400 error
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        // Use NO_WRAP to match iOS base64 encoding (no line breaks)
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Convert bitmap to base64 string for face images with compression
     * Aggressively compressed to avoid HTTP 413 errors while maintaining quality
     */
    fun bitmapToFaceBase64(bitmap: Bitmap): String {
        Log.d(TAG, "Compressing face image. Original size: ${bitmap.width}x${bitmap.height}")
        
        // Resize face image to reasonable dimensions
        val resizedBitmap = resizeImage(bitmap, MAX_FACE_WIDTH, MAX_FACE_HEIGHT)
        Log.d(TAG, "Resized face image to: ${resizedBitmap.width}x${resizedBitmap.height}")
        
        // Compress with adaptive quality to target file size
        val compressedBase64 = compressToTargetSize(
            resizedBitmap, 
            MAX_FACE_IMAGE_SIZE_KB,
            "face"
        )
        
        // Clean up if we created a new bitmap
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
        
        return compressedBase64
    }
    
    /**
     * Convert bitmap to base64 string for document images with compression
     * Balances quality and file size for document readability
     */
    fun bitmapToDocumentBase64(bitmap: Bitmap): String {
        Log.d(TAG, "Compressing document image. Original size: ${bitmap.width}x${bitmap.height}")
        
        // Resize document image to reasonable dimensions  
        val resizedBitmap = resizeImage(bitmap, MAX_DOCUMENT_WIDTH, MAX_DOCUMENT_HEIGHT)
        Log.d(TAG, "Resized document image to: ${resizedBitmap.width}x${resizedBitmap.height}")
        
        // Compress with adaptive quality to target file size
        val compressedBase64 = compressToTargetSize(
            resizedBitmap,
            MAX_DOCUMENT_IMAGE_SIZE_KB,
            "document"
        )
        
        // Clean up if we created a new bitmap
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
        
        return compressedBase64
    }
    
    /**
     * Resize image while maintaining aspect ratio
     */
    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calculate scale factor to fit within max dimensions
        val scale = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        
        // If image is already smaller, don't upscale
        if (scale >= 1.0f) {
            return bitmap
        }
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        Log.d(TAG, "Scaling image from ${width}x${height} to ${newWidth}x${newHeight} (scale: $scale)")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Compress bitmap to target file size using adaptive quality
     */
    private fun compressToTargetSize(bitmap: Bitmap, targetSizeKB: Int, imageType: String): String {
        var quality = 85 // Start with good quality
        var attempt = 0
        val maxAttempts = 8
        
        while (attempt < maxAttempts) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val sizeKB = byteArray.size / 1024
            
            Log.d(TAG, "Compression attempt $attempt for $imageType: quality=$quality, size=${sizeKB}KB, target=${targetSizeKB}KB")
            
            if (sizeKB <= targetSizeKB || quality <= MIN_IMAGE_QUALITY) {
                // Target reached or minimum quality hit
                val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                val base64SizeKB = base64.length * 3 / 4 / 1024 // Approximate base64 size
                Log.d(TAG, "Final $imageType compression: quality=$quality, JPEG=${sizeKB}KB, base64≈${base64SizeKB}KB")
                return base64
            }
            
            // Adjust quality for next attempt
            quality = when {
                sizeKB > targetSizeKB * 2 -> quality - 25  // Much too large, aggressive reduction
                sizeKB > targetSizeKB * 1.5 -> quality - 15  // Too large, significant reduction
                else -> quality - 10  // Slightly too large, moderate reduction
            }
            
            quality = max(quality, MIN_IMAGE_QUALITY)
            attempt++
        }
        
        // Fallback: return whatever we have
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, MIN_IMAGE_QUALITY, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Log.w(TAG, "Could not reach target size for $imageType, using minimum quality")
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Get estimated payload size for logging and monitoring
     */
    fun getEstimatedPayloadSizeKB(base64String: String): Int {
        // Base64 encoding increases size by ~33%, so reverse that for JPEG size
        return base64String.length * 3 / 4 / 1024
    }
} 