package com.artiusid.sdk.models

/**
 * Authentication request model - EXACT STANDALONE MATCH
 */
data class AuthenticationRequest(
    val username: String,
    val password: String? = null,
    val biometricData: String? = null,
    val deviceId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
