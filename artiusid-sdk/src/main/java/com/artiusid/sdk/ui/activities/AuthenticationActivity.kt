package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.sdk.ArtiusIDSDK
import com.artiusid.sdk.sdk.models.*
import com.artiusid.sdk.sdk.ui.theme.ArtiusIDSDKTheme

/**
 * Activity for user authentication
 */
class AuthenticationActivity : BaseSDKActivity() {
    
    @Composable
    override fun Content() {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        
        ArtiusIDSDKTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Authentication",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                performAuthentication(username, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Authenticate")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { finishAsCancelled() },
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        }
    }
    
    private fun performAuthentication(username: String, password: String) {
        // Simulate authentication process
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val success = username == "demo" && password == "demo123"
            
            if (success) {
                val result = AuthenticationResult(
                    success = true,
                    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFydGl1c0lEIFVzZXIiLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
                    sessionId = "auth-${System.currentTimeMillis()}"
                )
                
                ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                finishWithSuccess(result)
            } else {
                val error = SDKError(
                    code = SDKErrorCode.PROCESSING_ERROR,
                    message = "Invalid credentials"
                )
                
                ArtiusIDSDK.authenticationCallback?.onAuthenticationError(error)
                finishWithError(error)
            }
        }, 2000) // Simulate 2 second auth process
    }
}
