/*
 * File: EnrollmentDataScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.enrollment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.presentation.components.CustomBackButton
import com.artiusid.sdk.presentation.components.CustomButton
import com.artiusid.sdk.presentation.components.ErrorMessage
import com.artiusid.sdk.presentation.components.LoadingScreen
import com.artiusid.sdk.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EnrollmentDataScreen(
    onNavigateToDocumentScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter Your Information",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("First Name", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow900,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Yellow900,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Yellow900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Last Name", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow900,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Yellow900,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Yellow900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Email", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow900,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Yellow900,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Yellow900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToDocumentScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow900,
                    contentColor = Color.White
                )
            ) {
                Text("Continue")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoSection(
    viewModel: EnrollmentDataViewModel,
    shimmerAlpha: Float
) {
    val data by viewModel.enrollmentData.collectAsState()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            OutlinedTextField(
                value = data.firstName,
                onValueChange = { viewModel.updateFirstName(it) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        
        // Similar animations for other personal info fields
        // ... (rest of the personal info fields)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressSection(
    viewModel: EnrollmentDataViewModel,
    shimmerAlpha: Float
) {
    val data by viewModel.enrollmentData.collectAsState()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            OutlinedTextField(
                value = data.address,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        
        // Similar animations for other address fields
        // ... (rest of the address fields)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionalInfoSection(
    viewModel: EnrollmentDataViewModel,
    shimmerAlpha: Float
) {
    val data by viewModel.enrollmentData.collectAsState()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            OutlinedTextField(
                value = data.dateOfBirth,
                onValueChange = { viewModel.updateDateOfBirth(it) },
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        
        // Similar animations for other additional info fields
        // ... (rest of the additional info fields)
    }
} 