package com.artiusid.sdk.models

data class LoadCertificateRequest(
    val deviceId: String,
    val csr: String
) 