/*
 * File: ApiService.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.api

import com.artiusid.sdk.data.model.ApprovalRequest
import com.artiusid.sdk.data.model.ApprovalRequestTestingRequest
import com.artiusid.sdk.data.model.ApprovalRequestTestingResponse
import com.artiusid.sdk.data.model.ApprovalResponse
import com.artiusid.sdk.data.model.ApprovalResultData
import com.artiusid.sdk.data.model.AuthenticationRequest
import com.artiusid.sdk.data.model.AuthenticationResponse
import com.artiusid.sdk.data.model.LoadCertificateRequest
import com.artiusid.sdk.data.model.LoadCertificateResponse
import com.artiusid.sdk.data.model.VerificationRequest
import com.artiusid.sdk.data.model.VerificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("verifi/api/verification")
    suspend fun verify(
        @Query("clientId") clientId: Int,
        @Query("clientGroupId") clientGroupId: Int,
        @Body request: LinkedHashMap<String, Any>
    ): VerificationResponse

    @POST("auth/api/auth")
    suspend fun authenticate(
        @Query("clientId") clientId: Int,
        @Query("clientGroupId") clientGroupId: Int,
        @Query("accountNumber") accountNumber: String,
        @Body request: AuthenticationRequest
    ): AuthenticationResponse

    @POST("ApprovalResponseFunction")
    suspend fun sendApprovalResponse(
        @Body request: ApprovalRequest
    ): ApprovalResponse

    @POST("LoadCertificateFunction")
    suspend fun loadCertificate(
        @Query("clientId") clientId: Int,
        @Query("clientGroupId") clientGroupId: Int,
        @Body request: LoadCertificateRequest
    ): LoadCertificateResponse

    @POST("load-certificate")
    suspend fun loadCertificate(@Body request: LoadCertificateRequest): LoadCertificateResponse

    @POST("ApprovalRequestTestingFunction")
    suspend fun sendApprovalRequestIOS(
        @Body request: ApprovalRequestTestingRequest
    ): ApprovalRequestTestingResponse

    @POST("ApprovalResponseFunction")
    suspend fun approval(
        @Body request: ApprovalRequest
    ): ApprovalResultData
}

// Separate API service for certificate loading (uses different base URL)
interface CertificateApiService {
    @POST("LoadCertificateFunction")
    suspend fun loadCertificate(
        @Body request: Map<String, String>
    ): LoadCertificateResponse
} 