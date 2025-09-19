/*
 * File: VerificationResultsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.data.model.VerificationResultData
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.ui.components.ThemedImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationResultsScreen(
    onNavigateHome: () -> Unit,
    verificationData: VerificationResultData,
    viewModel: VerificationProcessingViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().background, com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().surface),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                )
            )
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Verification Results",
                    onBackClick = onNavigateHome
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
                // Header image
                ThemedImage(
                    defaultResourceId = R.drawable.img_crossdevicema,
                    overrideKey = "cross_device_image",
                    contentDescription = "Verification Complete",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(vertical = 20.dp)
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
                
                // Member ID (Account Number) - matching iOS display
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

                // Results card matching iOS design
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
                                        Bluegray901,
                                        Bluegray902
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
                                valueColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Back Home Button (matching iOS)
                Button(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
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
        color = if (isPass) com.artiusid.sdk.ui.theme.ThemedStatusColors.getSuccessColor() else com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor(),
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
    valueColor: Color = LightGreen900
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