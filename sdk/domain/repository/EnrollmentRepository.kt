/*
 * File: EnrollmentRepository.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.repository

import com.artiusid.sdk.domain.model.EnrollmentData

interface EnrollmentRepository {
    suspend fun submitEnrollmentData(data: EnrollmentData)
} 