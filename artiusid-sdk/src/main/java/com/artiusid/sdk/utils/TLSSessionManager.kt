/*
 * File: TLSSessionManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import java.security.cert.CertificateException

class TLSSessionManager(private val context: Context) {
    companion object {
        private const val TAG = "TLSSessionManager"
        private const val API_CERT_CHAIN_FILE = "api-cert-chain.pem"
    }

    fun getOkHttpClient(): OkHttpClient {
        try {
            Log.d(TAG, "Initializing SSLContext for mTLS...")
            val sslContext = SSLContext.getInstance("TLS")
            val keyManager = createKeyManager()
            val trustManager = createPinnedTrustManager()
            Log.d(TAG, "SSLContext.init() with keyManager: $keyManager, trustManager: $trustManager")
            sslContext.init(keyManager, trustManager, null)
            Log.d(TAG, "SSLContext initialized with custom KeyManager and TrustManager")

            // --- VERBOSE TLS LOGGING PATCH ---
            // Log all client certificates in the chain (at OkHttpClient creation, not just handshake)
            if (keyManager != null && keyManager.isNotEmpty() && keyManager[0] is X509KeyManager) {
                val km = keyManager[0] as X509KeyManager
                val aliases = km.getClientAliases("RSA", null)
                if (aliases != null && aliases.isNotEmpty()) {
                    Log.d(TAG, "[TLS-LOG] All client aliases: ${aliases.joinToString()}")
                    for (alias in aliases) {
                        val certChain = km.getCertificateChain(alias)
                        if (certChain != null && certChain.isNotEmpty()) {
                            Log.d(TAG, "[TLS-LOG] Client cert chain for alias '$alias': ${certChain.size} certs")
                            for ((i, cert) in certChain.withIndex()) {
                                Log.d(TAG, "[TLS-LOG] Client cert #$i: ${cert.subjectDN}")
                                try {
                                    val pem = encodePEM(cert.encoded, "CERTIFICATE")
                                    Log.d(TAG, "[TLS-LOG] Client cert #$i PEM:\n$pem")
                                } catch (e: Exception) {
                                    Log.w(TAG, "[TLS-LOG] Failed to encode client cert #$i to PEM: ${e.message}")
                                }
                            }
                        } else {
                            Log.w(TAG, "[TLS-LOG] Alias '$alias' has no certificate chain or chain is empty!")
                        }
                    }
                } else {
                    Log.w(TAG, "[TLS-LOG] No client aliases found in X509KeyManager!")
                }
            } else {
                Log.w(TAG, "[TLS-LOG] No X509KeyManager found or keyManager is empty!")
            }
            // --- END VERBOSE TLS LOGGING PATCH ---

            // Custom SSLSocketFactory to log handshake events
            val loggingSSLSocketFactory = object : SSLSocketFactory() {
                override fun getDefaultCipherSuites(): Array<String> = sslContext.socketFactory.defaultCipherSuites
                override fun getSupportedCipherSuites(): Array<String> = sslContext.socketFactory.supportedCipherSuites
                override fun createSocket(s: java.net.Socket?, host: String?, port: Int, autoClose: Boolean): java.net.Socket {
                    val socket = sslContext.socketFactory.createSocket(s, host, port, autoClose)
                    if (socket is javax.net.ssl.SSLSocket) {
                        socket.addHandshakeCompletedListener { event ->
                            Log.d(TAG, "[TLS-LOG] Handshake completed: cipher=${event.cipherSuite}, peer=${event.session.peerHost}")
                            try {
                                val serverCerts = event.peerCertificates
                                Log.d(TAG, "[TLS-LOG] Server presented ${serverCerts.size} certs:")
                                for ((i, cert) in serverCerts.withIndex()) {
                                    if (cert is X509Certificate) {
                                        Log.d(TAG, "[TLS-LOG] Server cert #$i: ${cert.subjectDN}")
                                        val pem = encodePEM(cert.encoded, "CERTIFICATE")
                                        Log.d(TAG, "[TLS-LOG] Server cert #$i PEM:\n$pem")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "[TLS-LOG] Failed to log server certificates: ${e.message}")
                            }
                        }
                    }
                    return socket
                }
                override fun createSocket(host: String?, port: Int): java.net.Socket =
                    sslContext.socketFactory.createSocket(host, port)
                override fun createSocket(host: String?, port: Int, localHost: java.net.InetAddress?, localPort: Int): java.net.Socket =
                    sslContext.socketFactory.createSocket(host, port, localHost, localPort)
                override fun createSocket(host: java.net.InetAddress?, port: Int): java.net.Socket =
                    sslContext.socketFactory.createSocket(host, port)
                override fun createSocket(address: java.net.InetAddress?, port: Int, localAddress: java.net.InetAddress?, localPort: Int): java.net.Socket =
                    sslContext.socketFactory.createSocket(address, port, localAddress, localPort)
            }

            return OkHttpClient.Builder()
                .sslSocketFactory(loggingSSLSocketFactory, trustManager[0] as X509TrustManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val newRequest = originalRequest.newBuilder()
                        // Use proper Android User-Agent format
                        .header("User-Agent", "ArtiusID-Android")
                        // Match iOS headers exactly
                        .header("Content-Type", "application/json")
                        .build()
                    
                    // Log the headers being sent for debugging
                    Log.d(TAG, "📤 Sending headers:")
                    Log.d(TAG, "📤   User-Agent: ArtiusID-Android")
                    Log.d(TAG, "📤   Content-Type: application/json")
                    
                    chain.proceed(newRequest)
                }
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating OkHttpClient for mTLS", e)
            throw RuntimeException(e)
        }
    }

    private fun createKeyManager(): Array<KeyManager>? {
        try {
            val certManager = CertificateManager(context)
            val certPem = certManager.loadCertificatePem()
            Log.d(TAG, "Attempting to load client certificate PEM from app storage...")
            if (certPem == null) {
                Log.e(TAG, "No certificate PEM found for mTLS!")
                return null
            }
            
            // Use hybrid approach for TLS compatibility
            val hybridManager = HybridCertificateManager(context)
            val tlsKeyStore = hybridManager.createTLSKeyStore(certPem)
            
            Log.d(TAG, "Created TLS-compatible KeyStore using hybrid approach")
            
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(tlsKeyStore, "".toCharArray()) // Empty password
            
            Log.d(TAG, "KeyManager initialized with hybrid TLS-compatible KeyStore")
            return kmf.keyManagers
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating KeyManager for mTLS (hybrid approach)", e)
            return null
        }
    }

    private fun createPinnedTrustManager(): Array<TrustManager> {
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = context.assets.open(API_CERT_CHAIN_FILE)
            val pinnedCerts = cf.generateCertificates(caInput)
            val pinnedCertsList = pinnedCerts.toList()
            Log.d(TAG, "Loaded ${pinnedCertsList.size} pinned certificates for strict pinning")
            pinnedCertsList.forEachIndexed { i, cert ->
                Log.d(TAG, "Pinned CA cert #$i: ${(cert as X509Certificate).subjectDN}")
            }
            // Get the default system trust manager
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(null as KeyStore?)
            val systemTrustManager = tmf.trustManagers.first { it is X509TrustManager } as X509TrustManager
            // Custom TrustManager that only accepts exact matches to pinned certs, or falls back to system trust
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = pinnedCertsList.map { it as java.security.cert.X509Certificate }.toTypedArray()
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    Log.d(TAG, "Server presented ${chain.size} certs. Checking for exact match against pinned certs...")
                    for ((serverIdx, serverCert) in chain.withIndex()) {
                        for ((pinnedIdx, pinned) in pinnedCertsList.withIndex()) {
                            if (serverCert.encoded.contentEquals((pinned as X509Certificate).encoded)) {
                                Log.d(TAG, "Server certificate #$serverIdx matches pinned certificate #$pinnedIdx. Pinning passed.")
                                return
                            }
                        }
                    }
                    Log.w(TAG, "No server certificate matched any pinned certificate. Trying system trust fallback...")
                    try {
                        systemTrustManager.checkServerTrusted(chain, authType)
                        Log.d(TAG, "System trust manager accepted the server certificate chain. Trust fallback passed.")
                    } catch (e: CertificateException) {
                        Log.e(TAG, "System trust manager rejected the server certificate chain. Pinning and trust failed.", e)
                        throw CertificateException("Server certificate does not match any pinned certificate and is not trusted by the system.", e)
                    }
                }
            }
            return arrayOf(trustManager)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TrustManager for mTLS (PEM pinning/system trust fallback)", e)
            throw RuntimeException(e)
        }
    }

    // Helper to encode PEM for logging
    private fun encodePEM(der: ByteArray, type: String): String {
        val base64 = android.util.Base64.encodeToString(der, android.util.Base64.NO_WRAP)
        val chunks = base64.chunked(64)
        return buildString {
            append("-----BEGIN $type-----\n")
            for (chunk in chunks) append("$chunk\n")
            append("-----END $type-----\n")
        }
    }
} 