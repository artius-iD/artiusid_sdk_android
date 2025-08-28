package com.artiusid.sdk.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing captured images during verification process
 */
@Parcelize
data class CapturedImages(
    val faceLivenessImage: String? = null,  // Base64 encoded
    val faceVerificationImage: String? = null,  // Base64 encoded
    val documentFrontImage: String? = null,  // Base64 encoded
    val documentBackImage: String? = null,  // Base64 encoded
    val passportImage: String? = null,  // Base64 encoded
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * Check if any images are captured
     */
    fun hasImages(): Boolean {
        return faceLivenessImage != null || 
               faceVerificationImage != null || 
               documentFrontImage != null || 
               documentBackImage != null || 
               passportImage != null
    }
    
    /**
     * Get count of captured images
     */
    fun getImageCount(): Int {
        var count = 0
        if (faceLivenessImage != null) count++
        if (faceVerificationImage != null) count++
        if (documentFrontImage != null) count++
        if (documentBackImage != null) count++
        if (passportImage != null) count++
        return count
    }
    
    companion object {
        /**
         * Convert Bitmap to Base64 string
         */
        fun bitmapToBase64(bitmap: Bitmap): String {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        }
        
        /**
         * Convert Base64 string to Bitmap
         */
        fun base64ToBitmap(base64String: String): Bitmap? {
            return try {
                val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }
}
