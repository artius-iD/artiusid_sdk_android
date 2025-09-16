/**
 * Author: Todd Bryant
 * Company: artius.iD
 * 
 * Android equivalent of iOS DocumentRecaptureNotificationView
 * Shows user-friendly error messages when document recapture is needed
 */
package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.data.model.DocumentRecaptureType
import com.artiusid.sdk.ui.theme.Yellow900
import kotlinx.coroutines.delay

/**
 * Composable that displays document recapture notification UI
 * Equivalent to iOS DocumentRecaptureNotificationView
 */
@Composable
fun DocumentRecaptureNotificationView(
    recaptureType: DocumentRecaptureType,
    onRecaptureAction: () -> Unit,
    onCancel: (() -> Unit)? = null,
    autoNavigateDelay: Long = 3000L // Auto-navigate after 3 seconds
) {
    // Auto-navigate after delay
    LaunchedEffect(Unit) {
        delay(autoNavigateDelay)
        onRecaptureAction()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        // Error icon based on recapture type
        val iconResource = when (recaptureType) {
            DocumentRecaptureType.PASSPORT_MRZ_ERROR,
            DocumentRecaptureType.PASSPORT_OCR_ERROR -> R.drawable.img_failed
            
            DocumentRecaptureType.STATE_ID_FRONT_ERROR,
            DocumentRecaptureType.STATE_ID_BACK_ERROR,
            DocumentRecaptureType.STATE_ID_BARCODE_ERROR -> R.drawable.img_failed
            
            DocumentRecaptureType.IMAGE_QUALITY_ERROR -> R.drawable.img_system_error
            DocumentRecaptureType.NFC_TIMEOUT_ERROR -> R.drawable.informational_icon
            DocumentRecaptureType.GENERAL_API_ERROR -> R.drawable.img_system_error
        }
        
        Image(
            painter = painterResource(id = iconResource),
            contentDescription = "Error",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )
        
        // Title
        Text(
            text = recaptureType.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Error message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = recaptureType.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp),
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Auto-redirect message
        Text(
            text = "Redirecting automatically in 3 seconds...",
            style = MaterialTheme.typography.bodyMedium,
            color = Yellow900,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary action button
            Button(
                onClick = onRecaptureAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = recaptureType.actionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Cancel button (if provided)
            onCancel?.let { cancelAction ->
                OutlinedButton(
                    onClick = cancelAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Preview composable for testing different recapture types
 */
@Composable
private fun DocumentRecaptureNotificationPreview() {
    MaterialTheme {
        DocumentRecaptureNotificationView(
            recaptureType = DocumentRecaptureType.PASSPORT_MRZ_ERROR,
            onRecaptureAction = { },
            onCancel = { }
        )
    }
}
