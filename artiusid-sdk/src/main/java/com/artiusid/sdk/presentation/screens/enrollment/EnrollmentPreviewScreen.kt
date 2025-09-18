/*
 * File: EnrollmentPreviewScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.domain.model.EnrollmentData
import com.artiusid.sdk.presentation.components.CustomBackButton
import com.artiusid.sdk.presentation.components.CustomButton
import com.artiusid.sdk.ui.theme.*

@Composable
fun EnrollmentPreviewScreen(
    enrollmentData: EnrollmentData,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CustomBackButton(onClick = onNavigateBack)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Text(
                    text = "Review Your Information",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Information Section
            PreviewSection(
                title = "Personal Information",
                showContent = showContent,
                content = {
                    PreviewField(label = "First Name", value = enrollmentData.firstName)
                    PreviewField(label = "Last Name", value = enrollmentData.lastName)
                    PreviewField(label = "Email", value = enrollmentData.email)
                    PreviewField(label = "Phone Number", value = enrollmentData.phoneNumber)
                    PreviewField(label = "Date of Birth", value = enrollmentData.dateOfBirth)
                    PreviewField(label = "SSN", value = enrollmentData.ssn)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Address Section
            PreviewSection(
                title = "Address",
                showContent = showContent,
                content = {
                    PreviewField(label = "Street Address", value = enrollmentData.address)
                    PreviewField(label = "City", value = enrollmentData.city)
                    PreviewField(label = "State", value = enrollmentData.state)
                    PreviewField(label = "ZIP Code", value = enrollmentData.zipCode)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Document Information Section
            PreviewSection(
                title = "Document Information",
                showContent = showContent,
                content = {
                    PreviewField(
                        label = "Document Type",
                        value = enrollmentData.documentType
                    )
                    PreviewField(label = "Document Number", value = enrollmentData.documentNumber)
                    PreviewField(
                        label = "Expiry Date",
                        value = enrollmentData.documentExpiryDate
                    )
                    PreviewField(
                        label = "Face Verification Score",
                        value = enrollmentData.faceVerificationScore
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomButton(
                    text = "Edit Information",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    isSecondary = true,
                    icon = Icons.Default.Edit
                )

                CustomButton(
                    text = "Confirm & Submit",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    showContent: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PreviewField(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PreviewField(
    label: String,
    value: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${value}%",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 