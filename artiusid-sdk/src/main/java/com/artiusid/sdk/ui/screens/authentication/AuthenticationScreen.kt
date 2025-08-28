package com.artiusid.presentation.screens.authentication

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.ui.theme.Yellow900
import kotlinx.coroutines.delay

@Composable
fun AuthenticationScreen(
    onNavigateToApproval: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthenticationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    
    // Start authentication when screen loads
    LaunchedEffect(Unit) {
        viewModel.startAuthentication(context)
    }
    
    // Handle biometric authentication (like iOS LAContext)
    LaunchedEffect(uiState) {
        if (uiState is AuthenticationUiState.BiometricRequired) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                val biometricManager = BiometricManager.from(context)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        // Create BiometricPrompt like iOS LAContext
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    viewModel.onBiometricSuccess()
                                }
                                
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    viewModel.onBiometricFailure()
                                }
                                
                                override fun onAuthenticationFailed() {
                                    viewModel.onBiometricFailure()
                                }
                            }
                        )
                        
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Unlock Device Permission")
                            .setSubtitle("Use your biometric to authenticate")
                            .setNegativeButtonText("Cancel")
                            .build()
                        
                        biometricPrompt.authenticate(promptInfo)
                    }
                    else -> {
                        // No biometric available - proceed anyway (like iOS fallback)
                        viewModel.onBiometricSuccess()
                    }
                }
            } else {
                // Fallback if not FragmentActivity
                viewModel.onBiometricSuccess()
            }
        }
    }
    
    GradientBackground {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Authentication",
                    onBackClick = onNavigateBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (uiState) {
                    is AuthenticationUiState.Processing -> {
                        // Processing animation - matches iOS
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(100.dp),
                            color = Yellow900,
                            strokeWidth = 8.dp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = currentStep,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Authenticating your account...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    is AuthenticationUiState.BiometricRequired -> {
                        // Biometric prompt state - show waiting for biometric
                        Image(
                            painter = painterResource(id = R.drawable.img_artiusid_ios),
                            contentDescription = "ArtiusID",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(vertical = 20.dp)
                        )
                        
                        Text(
                            text = "Biometric Authentication Required",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Yellow900,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Please use your fingerprint or face to complete authentication",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    is AuthenticationUiState.Success -> {
                        // Success state - show success image and navigate
                        Image(
                            painter = painterResource(id = R.drawable.img_success),
                            contentDescription = "Authentication Success",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Authentication Successful",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Yellow900,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = "You can now receive approval requests",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        // Navigate to authenticated screen after delay (like iOS)
                        LaunchedEffect(Unit) {
                            delay(2000)
                            onNavigateToApproval() // This will navigate to AuthenticatedScreen
                        }
                    }
                    
                    is AuthenticationUiState.Error -> {
                        // Error state
                        Image(
                            painter = painterResource(id = R.drawable.img_failed),
                            contentDescription = "Authentication Failed",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Authentication Failed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = (uiState as AuthenticationUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                        
                        // Retry button
                        Button(
                            onClick = { viewModel.startAuthentication(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow900
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(
                                text = "Try Again",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
} 