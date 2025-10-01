/*
 * File: VerificationResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class VerificationResponse(
    @SerializedName("verification_data") // Match iOS snake_case
    val verificationData: VerificationData?
)

data class VerificationData(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("payload")
    val payload: String? // The real data is in this JSON string, like iOS
) 