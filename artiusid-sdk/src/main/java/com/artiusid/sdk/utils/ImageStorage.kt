/*
 * File: ImageStorage.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import com.artiusid.sdk.domain.model.CapturedImages
import com.artiusid.sdk.data.models.passport.PassportMRZData

object ImageStorage {
    private var frontImage: Bitmap? = null
    private var backImage: Bitmap? = null
    private var faceImage: Bitmap? = null
    private var passportImage: Bitmap? = null // Add passport image
    
    // Add OCR data storage
    private var frontOcrData: Map<String, String>? = null
    
    // Add passport MRZ data storage
    private var passportMRZData: PassportMRZData? = null

    fun setFrontImage(bitmap: Bitmap) {
        frontImage = bitmap
        android.util.Log.d("ImageStorage", "Front image set: ${bitmap.width}x${bitmap.height}")
    }

    fun setBackImage(bitmap: Bitmap) {
        backImage = bitmap
        android.util.Log.d("ImageStorage", "Back image set: ${bitmap.width}x${bitmap.height}")
    }

    fun setFaceImage(bitmap: Bitmap) {
        faceImage = bitmap
        android.util.Log.d("ImageStorage", "Face image set: ${bitmap.width}x${bitmap.height}")
    }
    
    // Add OCR data storage methods
    fun setFrontOcrData(ocrData: Map<String, String>) {
        frontOcrData = ocrData
        android.util.Log.d("ImageStorage", "Front OCR data stored: ${ocrData.keys}")
    }
    
    fun getFrontOcrData(): Map<String, String>? {
        return frontOcrData
    }

    fun setPassportImage(bitmap: Bitmap) {
        passportImage = bitmap
        android.util.Log.d("ImageStorage", "Passport image set: ${bitmap.width}x${bitmap.height}")
    }
    
    fun getPassportImage(): Bitmap? {
        return passportImage
    }
    
    fun setPassportMRZData(mrzData: PassportMRZData) {
        passportMRZData = mrzData
        android.util.Log.d("ImageStorage", "Passport MRZ data set: ${mrzData.passportNumber}")
    }
    
    fun getPassportMRZData(): PassportMRZData? = passportMRZData
    
    fun clearPassportImage() {
        passportImage = null
        passportMRZData = null
        android.util.Log.d("ImageStorage", "Passport image and MRZ data cleared for recapture")
    }

    fun getCapturedImages(): CapturedImages {
        return CapturedImages(
            frontImage = frontImage,
            backImage = backImage,
            faceImage = faceImage,
            passportImage = passportImage
        )
    }

    fun clearAll() {
        frontImage = null
        backImage = null
        faceImage = null
        passportImage = null
        frontOcrData = null
        passportMRZData = null
    }
} 