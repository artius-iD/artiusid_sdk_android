/*
 * File: VerificationResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.model

import com.google.gson.annotations.SerializedName

data class VerificationResponse(
    @SerializedName("verification_data")
    val verificationData: VerificationData
)

data class VerificationData(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("payload")
    val payload: String?
)

data class VerificationPayload(
    @SerializedName("accountNumber")
    val accountNumber: String?,
    @SerializedName("isAccountActive")
    val isAccountActive: Int?,
    @SerializedName("documentData")
    val documentData: DocumentData?,
    @SerializedName("riskData")
    val riskData: RiskData?
)

data class DocumentData(
    @SerializedName("payload")
    val payload: DocumentPayload?
)

data class DocumentPayload(
    @SerializedName("document_data")
    val documentData: DocumentDataInner?
)

data class DocumentDataInner(
    @SerializedName("documentStatus")
    val documentStatus: String?,
    @SerializedName("documentScore")
    val documentScore: Int?,
    @SerializedName("faceMatchScore")
    val faceMatchScore: Int?,
    @SerializedName("antiSpoofingFaceScore")
    val antiSpoofingFaceScore: Int?
)

data class RiskData(
    @SerializedName("personSearchDataResults")
    val personSearchDataResults: PersonSearchDataResults?,
    @SerializedName("informationSearchDataResults")
    val informationSearchDataResults: InformationSearchDataResults?
)

data class PersonSearchDataResults(
    @SerializedName("personsearch_data")
    val personSearchData: PersonSearchData?
)

data class PersonSearchData(
    @SerializedName("personSearchScore")
    val personSearchScore: Double?,
    @SerializedName("personSearchResult")
    val personSearchResult: String?,
    @SerializedName("personSearchRating")
    val personSearchRating: String?
)

data class InformationSearchDataResults(
    @SerializedName("informationsearch_data")
    val informationSearchData: InformationSearchData?
)

data class InformationSearchData(
    @SerializedName("riskInformationScore")
    val riskInformationScore: Int?,
    @SerializedName("riskInformationResult")
    val riskInformationResult: String?,
    @SerializedName("riskInformationRating")
    val riskInformationRating: String?
) 