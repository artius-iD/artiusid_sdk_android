/*
 * File: ArtiusRepository.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import com.artiusid.sdk.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtiusRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Add repository methods here
    // Example:
    // suspend fun getUser(userId: String) = apiService.getUser(userId)
} 