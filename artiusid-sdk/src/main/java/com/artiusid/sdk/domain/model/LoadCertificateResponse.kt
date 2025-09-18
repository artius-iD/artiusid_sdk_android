/*
 * File: LoadCertificateResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.model

import com.google.gson.annotations.SerializedName

data class LoadCertificateResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("certificate")
    val certificate: String,
    @SerializedName("certificateChain")
    val certificateChain: String?,
    @SerializedName("deviceId")
    val deviceId: String?
) 