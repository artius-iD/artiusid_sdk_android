/*
 * Author: Todd Bryant
 * Company: artius.iD
 */
package com.artiusid.sdk.services

import android.content.Context
import android.util.Log
import com.artiusid.sdk.utils.CertificateManager
import com.artiusid.sdk.utils.TLSSessionManager
import com.artiusid.sdk.utils.EnvironmentManager
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.data.model.VerificationRequest
import com.artiusid.sdk.data.model.VerificationResponse
import com.artiusid.sdk.data.model.VerificationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class VerificationServiceImpl(
    private val context: Context,
    private val certificateManager: CertificateManager,
    private val tlsSessionManager: TLSSessionManager
) : VerificationService {

    companion object {
        private const val TAG = "VerificationServiceImpl"
        private const val CONTENT_TYPE = "application/json"
    }

    private fun getClient(): OkHttpClient {
        Log.d(TAG, "Creating OkHttpClient for verification with shared mTLS context")
        
        // Use shared context manager if available, otherwise fall back to local TLS manager
        val sharedContextManager = ArtiusIDSDK.getSharedContextManager()
        return if (sharedContextManager != null) {
            Log.d(TAG, "✅ Using shared mTLS context from host application")
            sharedContextManager.getSharedOkHttpClient()
        } else {
            Log.w(TAG, "⚠️ Shared context not available, using local TLS manager")
            tlsSessionManager.getOkHttpClient()
        }
    }

    override suspend fun submitVerification(verificationData: String): String = withContext(Dispatchers.IO) {
        val client = getClient()
        val url = UrlBuilder.getVerificationUrl(context) // Use dynamic URL builder
        val body = verificationData.toRequestBody(CONTENT_TYPE.toMediaType())
        // Use proper Android User-Agent string
        val androidUserAgent = "ArtiusID-Android"
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", CONTENT_TYPE)
            .addHeader("User-Agent", androidUserAgent)
            .build()
        Log.d(TAG, "Verification request URL: $url")
        Log.d(TAG, "Verification request headers: ${request.headers}")
        Log.d(TAG, "Verification request body: $verificationData")
        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Verification response code: ${response.code}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "Verification failed: ${response.code}")
                    throw IOException("Unexpected code $response")
                }
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Verification response body: $responseBody")
                return@withContext responseBody
            }
        } catch (e: Exception) {
            Log.e(TAG, "Verification request error", e)
            throw e
        }
    }

    override suspend fun checkVerificationStatus(verificationId: String): String = withContext(Dispatchers.IO) {
        val client = getClient()
        val url = UrlBuilder.getVerificationStatusUrl(context, verificationId) // Use dynamic URL builder
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        Log.d(TAG, "Verification status check URL: $url")
        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Status check response code: ${response.code}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "Status check failed: ${response.code}")
                    throw IOException("Unexpected code $response")
                }
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Status check response body: $responseBody")
                return@withContext responseBody
            }
        } catch (e: Exception) {
            Log.e(TAG, "Status check request error", e)
            throw e
        }
    }

    override suspend fun getVerificationHistory(): String = withContext(Dispatchers.IO) {
        val client = getClient()
        val url = UrlBuilder.getVerificationHistoryUrl(context) // Use dynamic URL builder
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "History fetch failed: ${response.code}")
                    throw IOException("Unexpected code $response")
                }
                return@withContext response.body?.string() ?: ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "History fetch request error", e)
            throw e
        }
    }
} 