/*
 * File: AuthenticationResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(
    @SerializedName("authentication_data")
    val authenticationData: AuthenticationData
)

data class AuthenticationData(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("payload")
    val payload: String // Match iOS structure exactly
)