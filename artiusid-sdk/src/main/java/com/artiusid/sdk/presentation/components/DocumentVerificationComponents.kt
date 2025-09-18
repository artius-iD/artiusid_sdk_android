/*
 * File: DocumentVerificationComponents.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.domain.service.PassportData
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.R

@Composable
fun DocumentVerificationResult(
    passportData: PassportData,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Document Verification Complete",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        VerificationResultCard(
            title = "Document Number",
            value = passportData.documentNumber
        )

        Spacer(modifier = Modifier.height(16.dp))

        VerificationResultCard(
            title = "Full Name",
            value = "${passportData.givenNames} ${passportData.surname}"
        )

        Spacer(modifier = Modifier.height(16.dp))

        VerificationResultCard(
            title = "Date of Birth",
            value = passportData.dateOfBirth
        )

        Spacer(modifier = Modifier.height(16.dp))

        VerificationResultCard(
            title = "Nationality",
            value = passportData.nationality
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            )

            CustomButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun VerificationResultCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

@Composable
fun DocumentVerificationCard(
    title: String,
    description: String,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryButton(
                text = "Verify Document",
                onClick = onVerifyClick
            )
        }
    }
}

@Composable
fun DocumentScanOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Document frame
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1.586f) // Standard ID card aspect ratio
                .align(Alignment.Center)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent)
        ) {
            // Corner markers
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomStart)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.White)
            )
        }
        
        // Instructions
        Text(
            text = "Position your document within the frame",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun DocumentVerificationStatus(
    status: DocumentVerificationStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, textColor) = when (status) {
        DocumentVerificationStatus.VERIFIED -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        DocumentVerificationStatus.PENDING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        DocumentVerificationStatus.FAILED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor)
    ) {
        Text(
            text = when (status) {
                DocumentVerificationStatus.VERIFIED -> "Verified"
                DocumentVerificationStatus.PENDING -> "Pending Verification"
                DocumentVerificationStatus.FAILED -> "Verification Failed"
            },
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

enum class DocumentVerificationStatus {
    VERIFIED,
    PENDING,
    FAILED
} 