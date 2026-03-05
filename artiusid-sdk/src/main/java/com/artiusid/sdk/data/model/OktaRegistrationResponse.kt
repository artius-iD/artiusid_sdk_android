/*
 * File: OktaRegistrationResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 *
 * Response structure from Okta registration Lambda (iOS parity: OktaRegistrationResponse).
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class OktaRegistrationResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("hint") val hint: String? = null,
    @SerializedName("missing") val missing: List<String>? = null
) {
    val isSuccessful: Boolean
        get() = status?.uppercase() == "SUCCESS"

    val resultMessage: String
        get() = when {
            isSuccessful -> message ?: "Okta registration successful"
            else -> error ?: message ?: "Okta registration failed"
        }
}
