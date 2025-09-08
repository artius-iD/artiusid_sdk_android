package com.artiusid.sdk.data.api

import com.artiusid.sdk.data.models.StandaloneVerificationResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Complete API service from the standalone application
 * This contains the EXACT API endpoints and structure used by the standalone app
 */
interface StandaloneApiService {
    @POST("verifi/api/verification")
    suspend fun verify(
        @Query("clientId") clientId: Int,
        @Query("clientGroupId") clientGroupId: Int,
        @Body request: LinkedHashMap<String, Any>
    ): StandaloneVerificationResponse
}
