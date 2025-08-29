package com.artiusid.sdk.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun isUserLoggedIn(): Boolean
} 