package com.artiusid.data.model

import com.google.gson.annotations.SerializedName

data class ApprovalResponse(
    @SerializedName("approvalData")
    val approvalData: ApprovalData
)

data class ApprovalData(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("success")
    val success: Boolean
)