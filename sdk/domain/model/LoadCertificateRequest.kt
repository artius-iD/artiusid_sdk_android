/*
 * File: LoadCertificateRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.model

import com.google.gson.annotations.SerializedName

data class LoadCertificateRequest(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("csr")
    val csr: String
) 