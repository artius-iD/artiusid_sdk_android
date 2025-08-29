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
    
    /**
     * Set face image (alias for storeFaceLivenessImage)
     */
    fun setFaceImage(bitmap: Bitmap?) {
        bitmap?.let { storeFaceLivenessImage(it) }
    }
    
    /**
     * Set document front image (alias for storeDocumentFrontImage)
     */
    fun setDocumentFrontImage(bitmap: Bitmap?) {
        bitmap?.let { storeDocumentFrontImage(it) }
    }
    
    /**
     * Set document back image (alias for storeDocumentBackImage)
     */
    fun setDocumentBackImage(bitmap: Bitmap?) {
        bitmap?.let { storeDocumentBackImage(it) }
    }
    
    /**
     * Set passport image (alias for storePassportImage)
     */
    fun setPassportImage(bitmap: Bitmap?) {
        bitmap?.let { storePassportImage(it) }
    }
    
    /**
     * Set document image (alias for setDocumentFrontImage)
     */
    fun setDocumentImage(bitmap: Bitmap?) {
        setDocumentFrontImage(bitmap)
    }
    
    /**
     * Set front OCR data
     */
    fun setFrontOcrData(ocrData: Map<String, String>) {
        // Store OCR data - for now we'll just log it
        // In a full implementation, this would be stored in a data manager
        android.util.Log.d("ImageStorage", "Storing front OCR data: $ocrData")
    }
    
    /**
     * Get front OCR data
     */
    fun getFrontOcrData(): Map<String, String> {
        // Return empty map for now - in full implementation would retrieve stored data
        return emptyMap()
    }
    
    /**
     * Set front image (alias for setDocumentFrontImage)
     */
    fun setFrontImage(bitmap: Bitmap?) {
        setDocumentFrontImage(bitmap)
    }
    
    /**
     * Set back image (alias for setDocumentBackImage)
     */
    fun setBackImage(bitmap: Bitmap?) {
        setDocumentBackImage(bitmap)
    }
    
    /**
     * Get captured images for verification processing
     */
    fun getCapturedImages(): CapturedImages {
        return CapturedImages(
            faceImage = ImageUtils.base64ToBitmap(faceLivenessImage),
            documentFrontImage = ImageUtils.base64ToBitmap(documentFrontImage),
            documentBackImage = ImageUtils.base64ToBitmap(documentBackImage),
            passportImage = ImageUtils.base64ToBitmap(passportImage)
        )
    }
    
    /**
     * Set passport MRZ data
     */
    fun setPassportMRZData(mrzData: Any?) {
        // Store MRZ data - in full implementation would store structured data
        android.util.Log.d("ImageStorage", "Storing passport MRZ data: $mrzData")
    }
    
    /**
     * Data class to hold captured images
     */
    data class CapturedImages(
        val faceImage: Bitmap? = null,
        val documentFrontImage: Bitmap? = null,
        val documentBackImage: Bitmap? = null,
        val passportImage: Bitmap? = null
    )
}