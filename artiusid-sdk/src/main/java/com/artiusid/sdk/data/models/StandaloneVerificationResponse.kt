package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

data class StandaloneVerificationResponse(
    @SerializedName("verification_data") // Match iOS snake_case
    val verificationData: StandaloneVerificationData?
)

data class StandaloneVerificationData(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("payload")
    val payload: String? // The real data is in this JSON string, like iOS
) 