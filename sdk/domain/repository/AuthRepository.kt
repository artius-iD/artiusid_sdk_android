/*
 * File: AuthRepository.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.repository

interface AuthRepository {
    suspend fun authenticate(): Boolean
    suspend fun login(email: String, password: String): Boolean
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
} 