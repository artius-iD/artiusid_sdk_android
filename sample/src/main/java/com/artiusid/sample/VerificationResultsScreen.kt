/*
 * File: VerificationResultsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationResultsScreen(
    verificationData: VerificationResultData,
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Blue-900
                        Color(0xFF111827)  // Gray-900
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Verification Results",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackHome) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon (using a built-in Android icon)
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Verification Complete",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 20.dp),
                    tint = Color.White
                )

                // User name header
                Text(
                    text = "${verificationData.firstName ?: "User"} ${verificationData.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Member ID (Account Number)
                if (!verificationData.accountNumber.isNullOrEmpty()) {
                    Text(
                        text = "Member ID: ${verificationData.accountNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Results card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF374151), // Gray-700
                                        Color(0xFF1F2937)  // Gray-800
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Document Result Section
                            Text(
                                text = "Document Result",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                StatusBadge(status = verificationData.documentStatus ?: "Unknown")
                            }

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Face Match Score
                            ScoreRow(
                                label = "Face Match Score",
                                value = verificationData.faceMatchScore.toString(),
                                isScore = true
                            )

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Document Score
                            ScoreRow(
                                label = "Document Score",
                                value = verificationData.documentScore.toString(),
                                isScore = true
                            )

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Anti-Spoofing Face Score
                            ScoreRow(
                                label = "Anti Spoofing Face Score",
                                value = verificationData.antiSpoofingFaceScore.toString(),
                                isScore = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Background Check Section
                            Text(
                                text = "Background Check",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Result",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                StatusBadge(status = verificationData.personResult ?: "Unknown")
                            }

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Person Search Score
                            ScoreRow(
                                label = "Person Search Score",
                                value = verificationData.personScore.toInt().toString(),
                                isScore = true
                            )

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Person Search Rating
                            ScoreRow(
                                label = "Person Search Rating",
                                value = verificationData.personRating ?: "N/A",
                                isScore = false,
                                valueColor = Color(0xFFFBBF24) // Yellow-400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Back Home Button
                Button(
                    onClick = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFBBF24) // Yellow-400
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Back Home",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val isPass = status.lowercase() == "pass"
    
    Surface(
        color = if (isPass) Color(0xFF10B981) else Color(0xFFEF4444), // Green-500 or Red-500
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = status,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ScoreRow(
    label: String,
    value: String,
    isScore: Boolean = false,
    valueColor: Color = Color(0xFF10B981) // Green-500
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

// Data class for verification results (matching the standalone app structure)
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
        // Create from JSON payload like the standalone app
        fun fromPayload(payload: String?): VerificationResultData {
            if (payload.isNullOrEmpty()) {
                return VerificationResultData()
            }
            
            return try {
                val jsonObject = org.json.JSONObject(payload)
                
                // Extract account number
                val accountNumber = jsonObject.optString("accountNumber", null)
                
                // Extract document data
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
                
                // Extract risk data
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
                
                // Get first and last name from document data like the standalone app
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
        
        // Get first and last name from document data like the standalone app
        private fun getDocumentNames(): Pair<String?, String?> {
            return try {
                // Check if we have passport data (NFC)
                val passportData = com.artiusid.sdk.utils.DocumentDataHolder.getPassportData()
                if (passportData != null) {
                    android.util.Log.d("VerificationResultData", "Using passport data: ${passportData.firstName} ${passportData.lastName}")
                    return Pair(passportData.firstName, passportData.lastName)
                }
                
                // Check if we have PhotoID data (PDF417)
                val photoIdData = com.artiusid.sdk.utils.DocumentDataHolder.getPhotoIdData()
                if (photoIdData != null) {
                    android.util.Log.d("VerificationResultData", "Using photo ID data: ${photoIdData.firstName} ${photoIdData.lastName}")
                    return Pair(photoIdData.firstName, photoIdData.lastName)
                }
                
                // Fallback to default
                android.util.Log.w("VerificationResultData", "No document data found, using default names")
                Pair("User", null)
            } catch (e: Exception) {
                android.util.Log.e("VerificationResultData", "Error getting document names", e)
                Pair("User", null)
            }
        }
    }
}
