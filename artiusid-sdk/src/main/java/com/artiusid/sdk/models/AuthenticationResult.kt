package com.artiusid.sdk.models

/**
 * Result from authentication - EXACT STANDALONE MATCH
 */
data class AuthenticationResult(
    val isAuthenticated: Boolean,
    val userId: String? = null,
    val token: String? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
