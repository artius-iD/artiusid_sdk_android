package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.SDKColors
import com.artiusid.sdk.models.DocumentRecaptureType

/**
 * Notification view for document recapture scenarios
 */
@Composable
fun DocumentRecaptureNotificationView(
    recaptureType: DocumentRecaptureType,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SDKColors.Warning.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = SDKColors.Warning,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getRecaptureTitle(recaptureType),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getRecaptureMessage(recaptureType),
                fontSize = 14.sp,
                color = SDKColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SDKColors.TextSecondary
                    )
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SDKColors.Yellow900,
                        contentColor = SDKColors.WhiteA700
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Get title for recapture type
 */
private fun getRecaptureTitle(type: DocumentRecaptureType): String {
    return when (type) {
        DocumentRecaptureType.FRONT_DOCUMENT -> "Retake Front Document"
        DocumentRecaptureType.BACK_DOCUMENT -> "Retake Back Document"
        DocumentRecaptureType.PASSPORT -> "Retake Passport"
        DocumentRecaptureType.FACE_IMAGE -> "Retake Face Photo"
        DocumentRecaptureType.GENERAL_ERROR -> "Capture Error"
        DocumentRecaptureType.NONE -> "Unknown Error"
    }
}

/**
 * Get message for recapture type
 */
private fun getRecaptureMessage(type: DocumentRecaptureType): String {
    return when (type) {
        DocumentRecaptureType.FRONT_DOCUMENT -> 
            "The front of your document couldn't be processed clearly. Please ensure good lighting and hold the document steady."
        DocumentRecaptureType.BACK_DOCUMENT -> 
            "The back of your document couldn't be processed clearly. Please ensure good lighting and hold the document steady."
        DocumentRecaptureType.PASSPORT -> 
            "Your passport couldn't be processed clearly. Please ensure good lighting and hold the passport steady."
        DocumentRecaptureType.FACE_IMAGE -> 
            "Your face photo couldn't be processed clearly. Please ensure good lighting and look directly at the camera."
        DocumentRecaptureType.GENERAL_ERROR -> 
            "There was an error processing your capture. Please try again."
        DocumentRecaptureType.NONE -> 
            "An unknown error occurred. Please try again."
    }
}