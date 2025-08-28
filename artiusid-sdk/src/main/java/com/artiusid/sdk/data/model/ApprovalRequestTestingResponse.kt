package com.artiusid.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingResponse.swift
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("approval_data")
    val approvalData: ApprovalTestingData?
)

data class ApprovalTestingData(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("requestId")
    val requestId: Int
) 