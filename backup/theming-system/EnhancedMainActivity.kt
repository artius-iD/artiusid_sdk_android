/*
 * File: EnhancedMainActivity.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.ArtiusSDKConfiguration
import com.artiusid.sdk.config.SDKFeature
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.theme.SDKThemeConfiguration
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced sample app demonstrating the complete ArtiusID SDK
 * with comprehensive theming and configuration options
 */
class EnhancedMainActivity : ComponentActivity(), VerificationCallback, AuthenticationCallback {
    
    private var isLoading by mutableStateOf(false)
    private var lastResult by mutableStateOf("")
    private var selectedTheme by mutableStateOf(ThemeOption.ARTIUSID_DEFAULT)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnhancedSampleApp()
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EnhancedSampleApp() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "ArtiusID SDK Demo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Complete Identity Verification with Custom Theming",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Theme Selection
            ThemeSelectionCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // SDK Actions
            SDKActionsCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Results
            if (lastResult.isNotEmpty()) {
                ResultsCard()
            }
        }
    }
    
    @Composable
    private fun ThemeSelectionCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎨 Theme Selection",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ThemeOption.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { 
                                selectedTheme = theme
                                initializeSDKWithTheme(theme)
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = theme.displayName,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = theme.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun SDKActionsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🚀 SDK Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { startCompleteVerification() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Start Complete Verification")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { startAuthentication() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    Text("Start Authentication")
                }
            }
        }
    }
    
    @Composable
    private fun ResultsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = lastResult,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
    
    private fun initializeSDKWithTheme(theme: ThemeOption) {
        try {
            val configuration = ArtiusSDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com",
                environment = Environment.DEVELOPMENT,
                theme = theme.themeConfiguration,
                enabledFeatures = setOf(
                    SDKFeature.FACE_VERIFICATION,
                    SDKFeature.DOCUMENT_SCANNING,
                    SDKFeature.NFC_PASSPORT_READING,
                    SDKFeature.LIVENESS_DETECTION
                ),
                enableLogging = true,
                showIntroductionScreen = true
            )
            
            ArtiusIDSDK.initialize(this, configuration)
            lastResult = "✅ SDK initialized with ${theme.displayName} theme"
            
        } catch (e: Exception) {
            lastResult = "❌ SDK initialization failed: ${e.message}"
        }
    }
    
    private fun startCompleteVerification() {
        if (!ArtiusIDSDK.isInitialized()) {
            initializeSDKWithTheme(selectedTheme)
        }
        
        isLoading = true
        lastResult = "🚀 Starting complete verification with ${selectedTheme.displayName} theme..."
        
        ArtiusIDSDK.startCompleteEmbeddedVerification(this, this)
    }
    
    private fun startAuthentication() {
        if (!ArtiusIDSDK.isInitialized()) {
            initializeSDKWithTheme(selectedTheme)
        }
        
        isLoading = true
        lastResult = "🔐 Starting authentication with ${selectedTheme.displayName} theme..."
        
        ArtiusIDSDK.startAuthenticationFlow(this, this)
    }
    
    // Callback implementations
    override fun onVerificationComplete(result: VerificationResult) {
        lastResult = """
            ✅ Verification Complete!
            Theme: ${selectedTheme.displayName}
            Success: ${result.success}
            Face: ${result.livenessResult?.success ?: "N/A"}
            Document: ${result.documentResult?.success ?: "N/A"}
            NFC: ${result.nfcResult?.success ?: "N/A"}
            Time: ${formatTimestamp(result.timestamp)}
        """.trimIndent()
        isLoading = false
    }
    
    override fun onVerificationError(error: SDKError) {
        lastResult = "❌ Verification Error: ${error.message}\nCode: ${error.code}"
        isLoading = false
    }
    
    override fun onAuthenticationComplete(result: AuthenticationResult) {
        lastResult = """
            ✅ Authentication Complete!
            Theme: ${selectedTheme.displayName}
            Success: ${result.isAuthenticated}
            User ID: ${result.userId ?: "N/A"}
            Time: ${formatTimestamp(result.timestamp)}
        """.trimIndent()
        isLoading = false
    }
    
    override fun onAuthenticationError(error: SDKError) {
        lastResult = "❌ Authentication Error: ${error.message}\nCode: ${error.code}"
        isLoading = false
    }
    
    override fun onVerificationBackled() {
        lastResult = "🔙 Verification cancelled by user"
        isLoading = false
    }
    
    override fun onVerificationCancelled() {
        lastResult = "❌ Verification cancelled"
        isLoading = false
    }
    
    override fun onAuthenticationBackled() {
        lastResult = "🔙 Authentication cancelled by user"
        isLoading = false
    }
    
    override fun onAuthenticationCancelled() {
        lastResult = "❌ Authentication cancelled"
        isLoading = false
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * Available theme options for demonstration
 */
enum class ThemeOption(
    val displayName: String,
    val description: String,
    val themeConfiguration: SDKThemeConfiguration
) {
    ARTIUSID_DEFAULT(
        "ArtiusID Default",
        "Standard ArtiusID branding",
        SDKThemeConfiguration(
            primaryColor = Color(0xFF263238), // Bluegray900 - original standalone background
            secondaryColor = Color(0xFFF57C00), // Yellow900 - original standalone orange
            backgroundColor = Color(0xFF263238), // Bluegray900
            surfaceColor = Color(0xFF37474F), // Bluegray901
            onBackground = Color(0xFFFFFFFF), // WhiteA700
            onSurface = Color(0xFFFFFFFF), // WhiteA700
            brandName = "ArtiusID",
            successColor = Color(0xFF4CAF50),
            errorColor = Color(0xFFD32F2F),
            warningColor = Color(0xFFFF9800),
            faceDetectionOverlayColor = Color(0xFF4CAF50),
            documentScanOverlayColor = Color(0xFFF57C00)
        )
    ),
    
    CORPORATE_BLUE(
        "Corporate Blue",
        "Professional blue theme",
        SDKThemeConfiguration(
            primaryColor = Color(0xFF1565C0),
            secondaryColor = Color(0xFF42A5F5),
            backgroundColor = Color(0xFFF8F9FA),
            surfaceColor = Color.White,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121),
            brandName = "Corporate Identity",
            successColor = Color(0xFF2E7D32),
            faceDetectionOverlayColor = Color(0xFF42A5F5),
            documentScanOverlayColor = Color(0xFF1565C0)
        )
    ),
    
    FINTECH_GREEN(
        "FinTech Green",
        "Financial services theme",
        SDKThemeConfiguration(
            primaryColor = Color(0xFF1B5E20),
            secondaryColor = Color(0xFF4CAF50),
            backgroundColor = Color(0xFFF1F8E9),
            surfaceColor = Color.White,
            onBackground = Color(0xFF1B5E20),
            onSurface = Color(0xFF1B5E20),
            brandName = "FinTech Verify",
            successColor = Color(0xFF4CAF50),
            faceDetectionOverlayColor = Color(0xFF4CAF50),
            documentScanOverlayColor = Color(0xFF1B5E20)
        )
    ),
    
    HEALTHCARE_TEAL(
        "Healthcare Teal",
        "Medical/healthcare theme",
        SDKThemeConfiguration(
            primaryColor = Color(0xFF00695C),
            secondaryColor = Color(0xFF26A69A),
            backgroundColor = Color(0xFFE0F2F1),
            surfaceColor = Color.White,
            onBackground = Color(0xFF00695C),
            onSurface = Color(0xFF00695C),
            brandName = "MedVerify",
            successColor = Color(0xFF388E3C),
            faceDetectionOverlayColor = Color(0xFF26A69A),
            documentScanOverlayColor = Color(0xFF00695C)
        )
    ),
    
    DARK_MODE(
        "Dark Professional",
        "Dark theme for modern apps",
        SDKThemeConfiguration(
            primaryColor = Color(0xFF1A1A1A),
            secondaryColor = Color(0xFF90CAF9),
            backgroundColor = Color(0xFF000000),
            surfaceColor = Color(0xFF1E1E1E),
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFE0E0E0),
            brandName = "Dark Verify",
            successColor = Color(0xFF4CAF50),
            errorColor = Color(0xFFF44336),
            warningColor = Color(0xFFFF9800),
            faceDetectionOverlayColor = Color(0xFF90CAF9),
            documentScanOverlayColor = Color(0xFF90CAF9),
            pendingStepColor = Color(0xFF424242)
        )
    )
}
