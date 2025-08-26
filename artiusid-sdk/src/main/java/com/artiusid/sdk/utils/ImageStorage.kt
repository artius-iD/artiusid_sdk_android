package com.artiusid.sdk.utils

import android.graphics.Bitmap

/**
 * Utility class for managing captured images during SDK flows
 */
object ImageStorage {
    private const val TAG = "ImageStorage"
    
    private var faceImage: Bitmap? = null
    private var frontImage: Bitmap? = null
    private var backImage: Bitmap? = null
    private var passportImage: Bitmap? = null
    private var frontOcrData: Map<String, String>? = null
    private var backOcrData: Map<String, String>? = null
    
    /**
     * Store face image from liveness detection
     */
    fun setFaceImage(bitmap: Bitmap) {
        faceImage = bitmap
        android.util.Log.d(TAG, "Face image stored")
    }
    
    /**
     * Get stored face image
     */
    fun getFaceImage(): Bitmap? = faceImage
    
    /**
     * Store front document image
     */
    fun setFrontImage(bitmap: Bitmap) {
        frontImage = bitmap
        android.util.Log.d(TAG, "Front document image stored")
    }
    
    /**
     * Get stored front document image
     */
    fun getFrontImage(): Bitmap? = frontImage
    
    /**
     * Store back document image
     */
    fun setBackImage(bitmap: Bitmap) {
        backImage = bitmap
        android.util.Log.d(TAG, "Back document image stored")
    }
    
    /**
     * Get stored back document image
     */
    fun getBackImage(): Bitmap? = backImage
    
    /**
     * Store passport image from NFC
     */
    fun setPassportImage(bitmap: Bitmap) {
        passportImage = bitmap
        android.util.Log.d(TAG, "Passport image stored")
    }
    
    /**
     * Get stored passport image
     */
    fun getPassportImage(): Bitmap? = passportImage
    
    /**
     * Store OCR data from front document
     */
    fun setFrontOcrData(data: Map<String, String>) {
        frontOcrData = data
        android.util.Log.d(TAG, "Front OCR data stored: ${data.size} fields")
    }
    
    /**
     * Get OCR data from front document
     */
    fun getFrontOcrData(): Map<String, String>? = frontOcrData
    
    /**
     * Store OCR data from back document
     */
    fun setBackOcrData(data: Map<String, String>) {
        backOcrData = data
        android.util.Log.d(TAG, "Back OCR data stored: ${data.size} fields")
    }
    
    /**
     * Get OCR data from back document
     */
    fun getBackOcrData(): Map<String, String>? = backOcrData
    
    /**
     * Get all captured images as a data class
     */
    fun getCapturedImages(): CapturedImages {
        return CapturedImages(
            faceImage = faceImage,
            frontImage = frontImage,
            backImage = backImage,
            passportImage = passportImage
        )
    }
    
    /**
     * Clear all stored images and data
     */
    fun clearAll() {
        faceImage?.recycle()
        frontImage?.recycle()
        backImage?.recycle()
        passportImage?.recycle()
        
        faceImage = null
        frontImage = null
        backImage = null
        passportImage = null
        frontOcrData = null
        backOcrData = null
        
        android.util.Log.d(TAG, "All stored images and data cleared")
    }
    
    /**
     * Check if face image is available
     */
    fun hasFaceImage(): Boolean = faceImage != null
    
    /**
     * Check if front document image is available
     */
    fun hasFrontImage(): Boolean = frontImage != null
    
    /**
     * Check if back document image is available
     */
    fun hasBackImage(): Boolean = backImage != null
    
    /**
     * Check if passport image is available
     */
    fun hasPassportImage(): Boolean = passportImage != null
}

/**
 * Data class to hold all captured images
 */
data class CapturedImages(
    val faceImage: Bitmap? = null,
    val frontImage: Bitmap? = null,
    val backImage: Bitmap? = null,
    val passportImage: Bitmap? = null
)
