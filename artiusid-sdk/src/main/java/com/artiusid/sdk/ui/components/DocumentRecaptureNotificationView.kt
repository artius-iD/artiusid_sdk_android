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
        DocumentRecaptureType.FACE_TOO_BLURRY -> "Face Too Blurry"
        DocumentRecaptureType.FACE_NOT_DETECTED -> "Face Not Detected"
        DocumentRecaptureType.FACE_TOO_DARK -> "Face Too Dark"
        DocumentRecaptureType.FACE_TOO_BRIGHT -> "Face Too Bright"
        DocumentRecaptureType.FACE_TOO_FAR -> "Face Too Far"
        DocumentRecaptureType.FACE_TOO_CLOSE -> "Face Too Close"
        DocumentRecaptureType.FACE_ANGLE_INCORRECT -> "Face Angle Incorrect"
        DocumentRecaptureType.DOCUMENT_TOO_BLURRY -> "Document Too Blurry"
        DocumentRecaptureType.DOCUMENT_NOT_DETECTED -> "Document Not Detected"
        DocumentRecaptureType.DOCUMENT_GLARE -> "Document Glare Detected"
        DocumentRecaptureType.DOCUMENT_CROPPED -> "Document Partially Visible"
        DocumentRecaptureType.DOCUMENT_ANGLE_INCORRECT -> "Document Angle Incorrect"
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
        DocumentRecaptureType.FACE_TOO_BLURRY -> 
            "Your face appears too blurry. Please ensure good lighting and hold the device steady."
        DocumentRecaptureType.FACE_NOT_DETECTED -> 
            "We couldn't detect your face. Please position your face clearly in the camera view."
        DocumentRecaptureType.FACE_TOO_DARK -> 
            "Your face appears too dark. Please move to better lighting."
        DocumentRecaptureType.FACE_TOO_BRIGHT -> 
            "Your face appears too bright. Please avoid direct lighting."
        DocumentRecaptureType.FACE_TOO_FAR -> 
            "Your face is too far from the camera. Please move closer."
        DocumentRecaptureType.FACE_TOO_CLOSE -> 
            "Your face is too close to the camera. Please move back slightly."
        DocumentRecaptureType.FACE_ANGLE_INCORRECT -> 
            "Please look directly at the camera with your face straight."
        DocumentRecaptureType.DOCUMENT_TOO_BLURRY -> 
            "Your document appears too blurry. Please hold the device steady and ensure good focus."
        DocumentRecaptureType.DOCUMENT_NOT_DETECTED -> 
            "We couldn't detect your document. Please position it clearly in the camera view."
        DocumentRecaptureType.DOCUMENT_GLARE -> 
            "Glare detected on your document. Please adjust the angle to avoid reflections."
        DocumentRecaptureType.DOCUMENT_CROPPED -> 
            "Your document is only partially visible. Please ensure the entire document is in the frame."
        DocumentRecaptureType.DOCUMENT_ANGLE_INCORRECT -> 
            "Please hold your document straight and parallel to the camera."
    }
}