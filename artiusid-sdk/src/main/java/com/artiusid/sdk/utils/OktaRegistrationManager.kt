/*
 * File: OktaRegistrationManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 *
 * Okta user registration (iOS parity: OktaRegistrationManager).
 * Calls backend okta-registration endpoint with mTLS.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.data.model.OktaRegistrationRequest
import com.artiusid.sdk.data.model.OktaRegistrationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException

/**
 * Manages Okta user registration against the backend Lambda (iOS parity).
 * Use [registerOktaUser] to register/link an Okta user with an Artius member.
 */
object OktaRegistrationManager {

    private const val TAG = "OktaRegistrationManager"

    /**
     * Register Okta user with the backend (iOS parity: registerOktaUser).
     * Uses approval-response base URL and mTLS when available.
     *
     * @param context Application context (used for URL config and optional TLS client)
     * @param userId Okta user ID (preferred identifier)
     * @param userEmail User email (alternative identifier)
     * @param phoneNumber Phone in E.164 (optional)
     * @param memberId Artius member ID (required)
     * @return OktaRegistrationResponse or null on network/parse error
     */
    suspend fun registerOktaUser(
        context: Context,
        userId: String?,
        userEmail: String?,
        phoneNumber: String?,
        memberId: String
    ): OktaRegistrationResponse? = withContext(Dispatchers.IO) {
        val request = OktaRegistrationRequest(
            userId = userId,
            userEmail = userEmail,
            phoneNumber = phoneNumber,
            memberId = memberId
        )
        val body = request.toEncodableBody()
        val envUrl = UrlBuilder.getApprovalResponseBaseUrl(context)
        Log.i(TAG, "========== OKTA REGISTRATION START ==========")
        Log.i(TAG, "Endpoint base: $envUrl")
        Log.i(TAG, "Request: userId=$userId, userEmail=$userEmail, phoneNumber=$phoneNumber, memberId=$memberId")

        try {
            val client = com.artiusid.sdk.ArtiusIDSDK.getSharedContextManager()?.getSharedOkHttpClient()
                ?: okhttp3.OkHttpClient.Builder().build()
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl(envUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(com.artiusid.sdk.data.api.ApiService::class.java)
            val response = api.registerOkta(body)
            Log.i(TAG, "========== OKTA REGISTRATION RESPONSE ==========")
            Log.i(TAG, "Status: ${response.status}, isSuccessful: ${response.isSuccessful}, resultMessage: ${response.resultMessage}")
            response
        } catch (e: HttpException) {
            Log.e(TAG, "Okta registration HTTP error: ${e.code()} ${e.message()}", e)
            OktaRegistrationResponse(
                status = "FAILURE",
                error = e.message(),
                message = e.response()?.errorBody()?.string() ?: "HTTP ${e.code()}"
            )
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Okta registration network error: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Okta registration error: ${e.message}", e)
            OktaRegistrationResponse(
                status = "FAILURE",
                error = e.message ?: "Unknown error"
            )
        }
    }
}
