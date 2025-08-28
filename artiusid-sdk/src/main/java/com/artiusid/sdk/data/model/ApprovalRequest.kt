package com.artiusid.data.model

import com.google.gson.annotations.SerializedName

data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("requestId")
    val requestId: Int?,
    
    @SerializedName("responseValue")
    val responseValue: String
)