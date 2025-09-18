/*
 * File: AuthenticationRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class AuthenticationRequest(
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("deviceModel")
    val deviceModel: String
    // Note: clientId, clientGroupId, accountNumber are sent as query parameters (like iOS)
    // fcmToken is not included in authentication requests (unlike verification)
)