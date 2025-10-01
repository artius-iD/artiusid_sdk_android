/*
 * File: LoadCertificateResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class LoadCertificateResponse(
    @SerializedName("certificate")
    val certificate: String?,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?
) {
    // Helper method to get the actual certificate from the nested body
    fun getActualCertificate(): String? {
        return if (certificate != null && certificate.isNotEmpty()) {
            certificate
        } else {
            null
        }
    }
    
    // Helper method to get the actual message from the nested body
    fun getActualMessage(): String? {
        return if (message != null && message.isNotEmpty()) {
            message
        } else {
            null
        }
    }
} 