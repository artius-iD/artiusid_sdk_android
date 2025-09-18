/*
 * File: ScannedPassportImage.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.models.passport

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Represents a scanned passport image captured during the scanning process
 * Android equivalent of iOS ScannedImage class
 */
data class ScannedPassportImage(
    val capturedImage: Bitmap,
    val orientation: Int,
    val captureTimestamp: Long = System.currentTimeMillis()
) {
    
    /**
     * Convert the captured image to Base64 encoded JPEG string
     * @param compressionQuality JPEG compression quality (0-100)
     * @return Base64 encoded JPEG string
     */
    fun toBase64JPEG(compressionQuality: Int = 85): String {
        val outputStream = ByteArrayOutputStream()
        capturedImage.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
    
    /**
     * Convert the captured image to Base64 encoded PNG string
     * @return Base64 encoded PNG string
     */
    fun toBase64PNG(): String {
        val outputStream = ByteArrayOutputStream()
        capturedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
    
    /**
     * Get the image dimensions
     * @return Pair of width and height
     */
    fun getDimensions(): Pair<Int, Int> = Pair(capturedImage.width, capturedImage.height)
    
    /**
     * Check if the image meets minimum quality requirements
     * @return true if image meets quality standards
     */
    fun meetsQualityRequirements(): Boolean {
        return capturedImage.width >= 1000 && capturedImage.height >= 700
    }
}