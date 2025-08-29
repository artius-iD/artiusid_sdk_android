package com.artiusid.sdk.data.api

import com.artiusid.sdk.models.ApprovalRequest
import com.artiusid.sdk.models.ApprovalRequestTestingRequest
import com.artiusid.sdk.models.ApprovalRequestTestingResponse
import com.artiusid.sdk.models.ApprovalResponse
import com.artiusid.sdk.models.AuthenticationRequest
import com.artiusid.sdk.models.AuthenticationResponse
import com.artiusid.sdk.models.LoadCertificateRequest
import com.artiusid.sdk.models.LoadCertificateResponse
import com.artiusid.sdk.models.VerificationRequest
import com.artiusid.sdk.models.VerificationResponse
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
    suspend fun sendApprovalRequest(
        @Body request: ApprovalRequestTestingRequest
    ): ApprovalRequestTestingResponse
}

// Separate API service for certificate loading (uses different base URL)
interface CertificateApiService {
    @POST("LoadCertificateFunction")
    suspend fun loadCertificate(
        @Body request: Map<String, String>
    ): LoadCertificateResponse
} 