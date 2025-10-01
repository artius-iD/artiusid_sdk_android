/*
 * File: EnrollmentRepositoryImpl.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import com.artiusid.sdk.domain.model.EnrollmentData
import com.artiusid.sdk.domain.repository.EnrollmentRepository

class EnrollmentRepositoryImpl : EnrollmentRepository {
    override suspend fun submitEnrollmentData(data: EnrollmentData) {
        // TODO: Implement actual API call or local storage
        // For now, just simulate a network delay
        kotlinx.coroutines.delay(1000)
    }
} 