/*
 * File: DocumentDataHolder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

// Data classes to hold document information
data class PhotoIdData(
    val firstName: String?,
    val lastName: String?,
    val driversLicenseNumber: String?,
    val dateOfBirth: String?,
    val address: String?
)

data class PassportData(
    val firstName: String?,
    val lastName: String?,
    val documentNumber: String?,
    val nationality: String?,
    val dateOfBirth: String?,
    val dateOfExpiry: String?
)

object DocumentDataHolder {
    private var _photoIdData: PhotoIdData? = null
    private var _passportData: PassportData? = null
    
    fun setPhotoIdData(data: PhotoIdData?) {
        _photoIdData = data
    }
    
    fun getPhotoIdData(): PhotoIdData? {
        return _photoIdData
    }
    
    fun setPassportData(data: PassportData?) {
        _passportData = data
    }
    
    fun getPassportData(): PassportData? {
        return _passportData
    }
    
    fun clearData() {
        _photoIdData = null
        _passportData = null
    }
} 