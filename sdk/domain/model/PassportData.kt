/*
 * File: PassportData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.model

data class PassportData(
    val passportNumber: String = "",
    val issuingCountry: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val placeOfBirth: String = "",
    val sex: String = "",
    val dateOfIssue: String = "",
    val dateOfExpiry: String = "",
    val issuingAuthority: String = "",
    val personalNumber: String = "",
    val surname: String = "",
    val givenNames: String = ""
) 