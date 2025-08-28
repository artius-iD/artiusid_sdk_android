package com.artiusid.data.model

import com.google.gson.annotations.SerializedName

data class LoadCertificateRequest(
    @SerializedName("csr")
    val csr: String,
    
    @SerializedName("deviceId")
    val deviceId: String
) 