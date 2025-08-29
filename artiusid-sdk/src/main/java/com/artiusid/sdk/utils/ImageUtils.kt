package com.artiusid.sdk.utils

import com.artiusid.sdk.utils.*

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Utility class for image processing operations
 */
object ImageUtils {
    
    /**
     * Convert bitmap to base64 string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 90): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    /**
     * Convert bitmap to base64 string optimized for documents
     */
    fun bitmapToDocumentBase64(bitmap: Bitmap): String {
        return bitmapToBase64(bitmap, 80) // Lower quality for documents to reduce size
    }
    
    /**
     * Convert bitmap to base64 string optimized for face images
     */
    fun bitmapToFaceBase64(bitmap: Bitmap): String {
        return bitmapToBase64(bitmap, 90) // Higher quality for face images
    }
    
    /**
     * Get estimated payload size in KB for a base64 string
     */
    fun getEstimatedPayloadSizeKB(base64String: String): Int {
        // Each base64 character represents 6 bits of data.
        // So, 4 base64 characters represent 3 bytes of original data.
        // Size in bytes = (base64String.length() / 4) * 3
        // Size in KB = (size in bytes) / 1024
        return (base64String.length / 4 * 3) / 1024
    }
    
    /**
     * Convert base64 string to bitmap
     */
    fun base64ToBitmap(base64String: String?): Bitmap? {
        return try {
            if (base64String.isNullOrEmpty()) return null
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error converting base64 to bitmap", e)
            null
        }
    }
    
    /**
     * Resize bitmap to specified dimensions
     */
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Crop bitmap to specified rectangle
     */
    fun cropBitmap(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        val safeX = x.coerceAtLeast(0)
        val safeY = y.coerceAtLeast(0)
        val safeWidth = width.coerceAtMost(bitmap.width - safeX)
        val safeHeight = height.coerceAtMost(bitmap.height - safeY)
        
        return Bitmap.createBitmap(bitmap, safeX, safeY, safeWidth, safeHeight)
    }
    
    /**
     * Calculate image quality score
     */
    fun calculateImageQuality(bitmap: Bitmap): Float {
        // Simple quality calculation based on image properties
        val pixels = bitmap.width * bitmap.height
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        
        var score = 0.5f
        
        // Size score
        when {
            pixels >= 1000000 -> score += 0.3f // 1MP+
            pixels >= 500000 -> score += 0.2f  // 0.5MP+
            else -> score += 0.1f
        }
        
        // Aspect ratio score (prefer reasonable ratios)
        when {
            aspectRatio in 0.7f..1.5f -> score += 0.2f
            aspectRatio in 0.5f..2.0f -> score += 0.1f
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Enhance image contrast and brightness
     */
    fun enhanceImage(bitmap: Bitmap, contrast: Float = 1.2f, brightness: Float = 10f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val enhanced = Bitmap.createBitmap(width, height, bitmap.config)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xff)
            val g = ((pixel shr 8) and 0xff)
            val b = (pixel and 0xff)
            
            val newR = ((r - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)
            val newG = ((g - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)
            val newB = ((b - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)
            
            pixels[i] = (0xff shl 24) or (newR shl 16) or (newG shl 8) or newB
        }
        
        enhanced.setPixels(pixels, 0, width, 0, 0, width, height)
        return enhanced
    }
    
    /**
     * Convert image to grayscale
     */
    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff
            
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = (0xff shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
        
        grayscale.setPixels(pixels, 0, width, 0, 0, width, height)
        return grayscale
    }
}