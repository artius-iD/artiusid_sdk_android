package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingRequest.swift
 */
data class ApprovalRequestTestingRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("approvalTitle")
    val approvalTitle: String,
    
    @SerializedName("approvalDescription")
    val approvalDescription: String
) 