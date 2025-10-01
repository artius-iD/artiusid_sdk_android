/*
 * File: EnrollmentData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.model

data class EnrollmentData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val dateOfBirth: String = "",
    val ssn: String = "",
    val documentNumber: String = "",
    val documentType: String = "",
    val documentExpiryDate: String = "",
    val faceVerificationScore: Float = 0f
) 