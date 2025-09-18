/*
 * File: VerificationResultData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

data class VerificationResultData(
    // Personal/Risk data
    val personScore: Double = 0.0,
    val personResult: String? = null,
    val personRating: String? = null,
    val personRiskScore: Int = 0,
    
    // Document data
    val documentStatus: String? = null,
    val documentScore: Int = 0,
    val faceMatchScore: Int = 0,
    val antiSpoofingFaceScore: Int = 0,
    
    // Risk information
    val riskInformationScore: Int = 0,
    val riskInformationResult: String? = null,
    val riskInformationRating: String? = null,
    
    // Account info
    val accountNumber: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
) {
    companion object {
        // Create from parsed JSON payload exactly like iOS does
        fun fromPayload(payload: String?): VerificationResultData {
            if (payload.isNullOrEmpty()) {
                return VerificationResultData()
            }
            
            return try {
                // Parse JSON payload exactly like iOS VerificationResponse.swift
                val jsonObject = org.json.JSONObject(payload)
                
                // Extract account number
                val accountNumber = jsonObject.optString("accountNumber", null)
                
                // Extract document data like iOS
                var documentStatus: String? = null
                var documentScore = 0
                var faceMatchScore = 0
                var antiSpoofingFaceScore = 0
                
                if (jsonObject.has("documentData")) {
                    val documentObject = jsonObject.getJSONObject("documentData")
                    if (documentObject.has("payload")) {
                        val documentPayload = documentObject.getJSONObject("payload")
                        if (documentPayload.has("document_data")) {
                            val documentData = documentPayload.getJSONObject("document_data")
                            documentStatus = documentData.optString("documentStatus", "n/a")
                            documentScore = documentData.optInt("documentScore", 0)
                            faceMatchScore = documentData.optInt("faceMatchScore", 0)
                            antiSpoofingFaceScore = documentData.optInt("antiSpoofingFaceScore", 0)
                        }
                    }
                }
                
                // Extract risk data like iOS
                var personScore = 0.0
                var personResult: String? = null
                var personRating: String? = null
                var riskInformationScore = 0
                var riskInformationResult: String? = null
                var riskInformationRating: String? = null
                
                if (jsonObject.has("riskData")) {
                    val riskObject = jsonObject.getJSONObject("riskData")
                    
                    // Person search data
                    if (riskObject.has("personSearchDataResults")) {
                        val personSearchDataResults = riskObject.getJSONObject("personSearchDataResults")
                        if (personSearchDataResults.has("personsearch_data")) {
                            val personSearchData = personSearchDataResults.getJSONObject("personsearch_data")
                            personScore = personSearchData.optDouble("personSearchScore", 0.0)
                            personResult = personSearchData.optString("personSearchResult", "n/a")
                            personRating = personSearchData.optString("personSearchRating", "n/a")
                        }
                    }
                    
                    // Risk information data
                    if (riskObject.has("informationSearchDataResults")) {
                        val informationSearchDataResults = riskObject.getJSONObject("informationSearchDataResults")
                        if (informationSearchDataResults.has("informationsearch_data")) {
                            val informationSearchData = informationSearchDataResults.getJSONObject("informationsearch_data")
                            riskInformationScore = informationSearchData.optInt("riskInformationScore", 0)
                            riskInformationResult = informationSearchData.optString("riskInformationResult", "n/a")
                            riskInformationRating = informationSearchData.optString("riskInformationRating", "n/a")
                        }
                    }
                }
                
                // Get names from document data (will be set later from PhotoID/Passport data)
                val names = getDocumentNames()
                
                VerificationResultData(
                    personScore = personScore,
                    personResult = personResult,
                    personRating = personRating,
                    documentStatus = documentStatus,
                    documentScore = documentScore,
                    faceMatchScore = faceMatchScore,
                    antiSpoofingFaceScore = antiSpoofingFaceScore,
                    riskInformationScore = riskInformationScore,
                    riskInformationResult = riskInformationResult,
                    riskInformationRating = riskInformationRating,
                    accountNumber = accountNumber,
                    firstName = names.first,
                    lastName = names.second
                )
            } catch (e: Exception) {
                android.util.Log.e("VerificationResultData", "Error parsing payload JSON", e)
                VerificationResultData()
            }
        }
        
        // Get first and last name from document data like iOS
        private fun getDocumentNames(): Pair<String?, String?> {
            return try {
                // Check if we have passport data (NFC)
                val passportData = com.artiusid.sdk.utils.DocumentDataHolder.getPassportData()
                if (passportData != null) {
                    return Pair(passportData.firstName, passportData.lastName)
                }
                
                // Check if we have PhotoID data (PDF417)
                val photoIdData = com.artiusid.sdk.utils.DocumentDataHolder.getPhotoIdData()
                if (photoIdData != null) {
                    return Pair(photoIdData.firstName, photoIdData.lastName)
                }
                
                // Fallback to default
                Pair("User", null)
            } catch (e: Exception) {
                android.util.Log.e("VerificationResultData", "Error getting document names", e)
                Pair("User", null)
            }
        }
    }
} 