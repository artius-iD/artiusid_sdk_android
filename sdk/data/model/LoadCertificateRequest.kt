/*
 * File: LoadCertificateRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class LoadCertificateRequest(
    @SerializedName("csr")
    val csr: String,
    
    @SerializedName("deviceId")
    val deviceId: String
) 