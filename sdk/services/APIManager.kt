/*
 * Author: Todd Bryant
 * Company: artius.iD
 */
package com.artiusid.sdk.services

import android.content.Context
import android.util.Log
import com.artiusid.sdk.models.LoadCertificateRequest
import com.artiusid.sdk.models.LoadCertificateResponse
import com.artiusid.sdk.utils.CertificateManager
import com.artiusid.sdk.utils.TLSSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class APIManager(private val context: Context) {
    companion object {
        private const val TAG = "APIManager"
        private const val CONTENT_TYPE = "application/json"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val tlsSessionManager = TLSSessionManager(context)
    private val certManager = CertificateManager(context)

    // Call this on app start to ensure certificate exists
    suspend fun ensureCertificate(deviceId: String, serviceUrl: String) {
        val certManager = CertificateManager(context)
        if (certManager.loadCertificatePem() == null) {
            Log.d(TAG, "No certificate PEM found, generating Keystore keypair and CSR...")
            val csr = certManager.generateCSR(deviceId)
            val response = loadCertificate(serviceUrl, LoadCertificateRequest(deviceId, csr))
            certManager.storeCertificatePem(response.certificate)
            Log.d(TAG, "Certificate registration and PEM storage complete")
        } else {
            Log.d(TAG, "Existing certificate PEM found")
        }
    }

    // Call this on environment change to clear and reload identity
    fun clearAndReloadIdentity() {
        Log.d(TAG, "Clearing and reloading identity (certificate and key)...")
        certManager.removeCertificatePem()
        certManager.removeKeyPair()
    }

    // Load certificate endpoint - does NOT use mTLS (exactly like iOS)
    suspend fun loadCertificate(serviceUrl: String, request: LoadCertificateRequest): LoadCertificateResponse {
        // Append /LoadCertificateFunction to match iOS endpoint exactly
        val fullUrl = "$serviceUrl/LoadCertificateFunction"
        Log.d(TAG, "Loading certificate from: $fullUrl")
        return withContext(Dispatchers.IO) {
            try {
                // Certificate registration endpoint should NOT use mTLS or strict certificate pinning
                // This endpoint provides the client certificate, so it must be accessible without one
                Log.d(TAG, "🔓 Creating plain OkHttpClient for certificate registration (NO mTLS, NO pinning)")
                val client = OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    // Use system default SSL context (no custom pinning or mTLS)
                    .build()
                
                Log.d(TAG, "🔓 Plain client created successfully - will use system trust store only")

                val jsonBody = JSONObject().apply {
                    put("deviceId", request.deviceId)
                    put("csr", request.csr)
                }.toString()
                val body = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

                // Use proper Android User-Agent string
                val androidUserAgent = "artius.iD/5"
                Log.d(TAG, "Using User-Agent: $androidUserAgent")

                val req = Request.Builder()
                    .url(fullUrl)
                    .post(body)  // Certificate registration should use POST method
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Content-Length", jsonBody.toByteArray().size.toString())
                    .addHeader("User-Agent", androidUserAgent)
                    .build()

                Log.d(TAG, "Certificate registration REQUEST:")
                Log.d(TAG, "  Headers: ${req.headers}")
                Log.d(TAG, "  Body: $jsonBody")
                Log.d(TAG, "Sending certificate registration request...")

                val response = client.newCall(req).execute()
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Certificate registration failed: ${response.code} - $responseBody")
                    throw IOException("Certificate registration failed: ${response.code} - $responseBody")
                }
                Log.d(TAG, "Certificate registration successful: $responseBody")
                // Parse the response as JSON - handle nested structure from API Gateway
                val jsonResponse = JSONObject(responseBody)
                val certificate = if (jsonResponse.has("body")) {
                    // API Gateway wraps the response in a "body" field
                    val bodyJson = JSONObject(jsonResponse.getString("body"))
                    bodyJson.optString("certificate", "")
                } else {
                    // Direct response (fallback)
                    jsonResponse.optString("certificate", "")
                }
                if (certificate.isEmpty()) {
                    throw IOException("Invalid certificate response: missing certificate")
                }
                return@withContext LoadCertificateResponse(certificate = certificate)
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadCertificate", e)
                throw e
            }
        }
    }
} 