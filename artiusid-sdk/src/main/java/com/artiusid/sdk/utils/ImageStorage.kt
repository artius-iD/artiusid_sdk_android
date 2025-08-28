package com.artiusid.sdk.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.util.Base64

/**
 * Utility class for storing captured images during verification flow
 * This replicates the functionality from the standalone app
 */
object ImageStorage {
    
    private var faceLivenessImage: String? = null
    private var documentFrontImage: String? = null
    private var documentBackImage: String? = null
    private var passportImage: String? = null
    
    /**
     * Store face liveness image
     */
    fun storeFaceLivenessImage(bitmap: Bitmap) {
        faceLivenessImage = bitmapToBase64(bitmap)
    }
    
    /**
     * Store document front image
     */
    fun storeDocumentFrontImage(bitmap: Bitmap) {
        documentFrontImage = bitmapToBase64(bitmap)
    }
    
    /**
     * Store document back image
     */
    fun storeDocumentBackImage(bitmap: Bitmap) {
        documentBackImage = bitmapToBase64(bitmap)
    }
    
    /**
     * Store passport image
     */
    fun storePassportImage(bitmap: Bitmap) {
        passportImage = bitmapToBase64(bitmap)
    }
    
    /**
     * Get face liveness image
     */
    fun getFaceLivenessImage(): String? = faceLivenessImage
    
    /**
     * Get document front image
     */
    fun getDocumentFrontImage(): String? = documentFrontImage
    
    /**
     * Get document back image
     */
    fun getDocumentBackImage(): String? = documentBackImage
    
    /**
     * Get passport image
     */
    fun getPassportImage(): String? = passportImage
    
    /**
     * Clear all images
     */
    fun clearAll() {
        faceLivenessImage = null
        documentFrontImage = null
        documentBackImage = null
        passportImage = null
    }
    
    /**
     * Clear passport image only
     */
    fun clearPassportImage() {
        passportImage = null
    }
    
    /**
     * Clear document images only
     */
    fun clearDocumentImages() {
        documentFrontImage = null
        documentBackImage = null
    }
    
    /**
     * Convert bitmap to base64 string
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    /**
     * Check if all required images are captured
     */
    fun hasAllRequiredImages(): Boolean {
        return faceLivenessImage != null && 
               (documentFrontImage != null || passportImage != null)
    }
}