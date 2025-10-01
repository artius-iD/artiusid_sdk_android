/*
 * File: LoadCertificateRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.models

data class LoadCertificateRequest(
    val deviceId: String,
    val csr: String
) 