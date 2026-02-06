/*
 * File: CollectOktaIDScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Android equivalent of iOS CollectOktaIDView
 * Optional Okta ID collection during verification flow (NEW in v2.0.12)
 */

package com.artiusid.sdk.presentation.screens.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.*

/**
 * Screen for collecting Okta ID during verification flow
 * Matches iOS CollectOktaIDView functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectOktaIDScreen(
    onOktaIDCollected: (String) -> Unit,
    onSkip: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var oktaID by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus the text field when screen appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorManager.getCurrentScheme().background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom back button
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = "Okta ID"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Icon/Image
            Image(
                painter = painterResource(id = R.drawable.informational_icon),
                contentDescription = "Okta ID",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )
            
            // Title
            Text(
                text = "Enter Your Okta ID",
                color = ThemedButtonColors.getPrimaryButtonColor(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Please enter your Okta ID to continue with verification. This helps us verify your identity more securely.",
                color = ThemedTextColors.getPrimaryTextColor(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Okta ID Input Field
            OutlinedTextField(
                value = oktaID,
                onValueChange = { 
                    oktaID = it
                    isError = false
                },
                label = { Text("Okta ID") },
                placeholder = { Text("Enter your Okta ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (oktaID.isNotBlank()) {
                            onOktaIDCollected(oktaID.trim())
                        } else {
                            isError = true
                        }
                    }
                ),
                isError = isError,
                supportingText = if (isError) {
                    { Text("Okta ID is required") }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemedButtonColors.getPrimaryButtonColor(),
                    focusedLabelColor = ThemedButtonColors.getPrimaryButtonColor(),
                    cursorColor = ThemedButtonColors.getPrimaryButtonColor(),
                    unfocusedBorderColor = ThemedTextColors.getSecondaryTextColor(),
                    unfocusedLabelColor = ThemedTextColors.getSecondaryTextColor()
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Helper text
            Text(
                text = "Your Okta ID is typically your email username or employee ID",
                color = ThemedTextColors.getSecondaryTextColor(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue button
            Button(
                onClick = {
                    if (oktaID.isNotBlank()) {
                        android.util.Log.d("CollectOktaIDScreen", "Okta ID collected: $oktaID")
                        onOktaIDCollected(oktaID.trim())
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemedButtonColors.getPrimaryButtonColor(),
                    contentColor = ThemedButtonColors.getPrimaryButtonTextColor()
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Skip button (optional)
            TextButton(
                onClick = {
                    android.util.Log.d("CollectOktaIDScreen", "Okta ID collection skipped")
                    onSkip()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Skip for now",
                    color = ThemedTextColors.getSecondaryTextColor(),
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CustomBackButton(
    onBackClick: () -> Unit,
    navTitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ThemedTextColors.getPrimaryTextColor()
            )
        }
        
        if (navTitle.isNotEmpty()) {
            Text(
                text = navTitle,
                color = ThemedTextColors.getPrimaryTextColor(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

