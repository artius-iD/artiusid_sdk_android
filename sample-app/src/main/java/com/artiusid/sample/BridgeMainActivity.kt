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
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bridge Sample App demonstrating the ArtiusID SDK Bridge Architecture
 * 
 * This sample app shows how to integrate with the ArtiusID SDK bridge that
 * launches the complete standalone application in an isolated activity context.
 */
class BridgeMainActivity : ComponentActivity(), VerificationCallback, AuthenticationCallback {
    
    private var isLoading by mutableStateOf(false)
    private var lastResult by mutableStateOf("")
    private var selectedTheme by mutableStateOf(ThemeOption.ARTIUSID_DEFAULT)
    private var verificationResultData by mutableStateOf<VerificationResultData?>(null)
    private var showResultsScreen by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showResultsScreen && verificationResultData != null) {
                        VerificationResultsScreen(
                            verificationData = verificationResultData!!,
                            onBackHome = {
                                showResultsScreen = false
                                verificationResultData = null
                            }
                        )
                    } else {
                        BridgeSampleApp()
                    }
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BridgeSampleApp() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "ArtiusID SDK Bridge Demo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            Text(
                text = "🌉 Bridge to Complete Standalone Application",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Architecture Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🏗️ Bridge Architecture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Host App → SDK Bridge → Standalone App Activity → Results → SDK Bridge → Host App",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Complete isolation of standalone app\n• Seamless theming and configuration\n• Full functionality preservation\n• Clean result communication",
                        fontSize = 14.sp
                    )
                }
            }
            
            // Theme Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                                onClick = { selectedTheme = theme }
                            )
                            Column(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = theme.displayName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = theme.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Button(
                onClick = { startVerificationFlow() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.primaryColorHex))
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.onPrimaryColorHex))
                    )
                } else {
                    Text(
                        text = "🔍 Start Verification (Bridge)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.onPrimaryColorHex))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { startAuthenticationFlow() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.secondaryColorHex))
                )
            ) {
                Text(
                    text = "🔐 Start Authentication (Bridge)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.onSecondaryColorHex))
                )
            }
            
            // Results Display
            if (lastResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📋 Last Result",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = lastResult,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Theme Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🎨 Current Theme Preview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorSwatch("Primary", selectedTheme.themeConfig.primaryColorHex)
                        ColorSwatch("Secondary", selectedTheme.themeConfig.secondaryColorHex)
                        ColorSwatch("Background", selectedTheme.themeConfig.backgroundColorHex)
                    }
                }
            }
        }
    }
    
    @Composable
    fun ColorSwatch(label: String, colorHex: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(android.graphics.Color.parseColor(colorHex))
                )
            ) {}
            Text(
                text = label,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    
    private fun startVerificationFlow() {
        try {
            isLoading = true
            
            // Initialize SDK Bridge with selected theme and shared context
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = Environment.STAGING,
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true
            )
            
            ArtiusIDSDK.initialize(
                context = this,
                configuration = sdkConfig,
                theme = selectedTheme.themeConfig
            )
            
            // Start verification via bridge to standalone app
            ArtiusIDSDK.startVerification(this, this)
            
        } catch (e: Exception) {
            isLoading = false
            lastResult = "❌ Error starting verification bridge: ${e.message}"
        }
    }
    
    private fun startAuthenticationFlow() {
        try {
            isLoading = true
            
            // Initialize SDK Bridge with selected theme and shared context
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = Environment.STAGING,
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true
            )
            
            ArtiusIDSDK.initialize(
                context = this,
                configuration = sdkConfig,
                theme = selectedTheme.themeConfig
            )
            
            // Start authentication via bridge to standalone app
            ArtiusIDSDK.startAuthentication(this, this)
            
        } catch (e: Exception) {
            isLoading = false
            lastResult = "❌ Error starting authentication bridge: ${e.message}"
        }
    }
    
    // VerificationCallback implementation
    override fun onVerificationSuccess(result: VerificationResult) {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Parse the verification result data for the results screen
        verificationResultData = VerificationResultData.fromPayload(result.rawResponse)
        
        // Show the results screen
        showResultsScreen = true
        
        // Also update the text result for debugging
        lastResult = """
            ✅ Verification Success (Bridge) [$timestamp]
            ID: ${result.verificationId}
            Confidence: ${(result.confidence * 100).toInt()}%
            Document: ${result.documentType ?: "Unknown"}
            Processing Time: ${result.processingTime}ms
            Session: ${result.sessionId}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onVerificationError(error: SDKError) {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Verification Error (Bridge) [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onVerificationCancelled() {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Verification Cancelled (Bridge) [$timestamp]\n🌉 Via Standalone App Bridge"
    }
    
    // AuthenticationCallback implementation
    override fun onAuthenticationSuccess(result: AuthenticationResult) {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ✅ Authentication Success (Bridge) [$timestamp]
            ID: ${result.authenticationId}
            Confidence: ${(result.confidence * 100).toInt()}%
            Processing Time: ${result.processingTime}ms
            Session: ${result.sessionId}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onAuthenticationError(error: SDKError) {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Authentication Error (Bridge) [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onAuthenticationCancelled() {
        isLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Authentication Cancelled (Bridge) [$timestamp]\n🌉 Via Standalone App Bridge"
    }
}

/**
 * Theme options for the bridge to standalone application
 */
enum class ThemeOption(
    val displayName: String,
    val description: String,
    val themeConfig: SDKThemeConfiguration
) {
    ARTIUSID_DEFAULT(
        "ArtiusID Default",
        "Standard ArtiusID branding",
        SDKThemeConfiguration(
            brandName = "ArtiusID",
            primaryColorHex = "#263238", // Bluegray900
            secondaryColorHex = "#F57C00", // Yellow900
            backgroundColorHex = "#263238",
            surfaceColorHex = "#37474F",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#263238",
            onBackgroundColorHex = "#FFFFFF",
            onSurfaceColorHex = "#FFFFFF",
            successColorHex = "#4CAF50",
            errorColorHex = "#D32F2F",
            warningColorHex = "#FF9800",
            faceDetectionOverlayColorHex = "#4CAF50",
            documentScanOverlayColorHex = "#F57C00"
        )
    ),
    
    DARK_MODE(
        "Dark Professional",
        "Modern dark theme for professional applications",
        SDKThemeConfiguration(
            brandName = "Dark Professional",
            primaryColorHex = "#121212", // True dark
            secondaryColorHex = "#03DAC6", // Teal accent
            backgroundColorHex = "#121212",
            surfaceColorHex = "#1E1E1E",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#000000",
            onBackgroundColorHex = "#FFFFFF",
            onSurfaceColorHex = "#FFFFFF",
            successColorHex = "#4CAF50",
            errorColorHex = "#CF6679",
            warningColorHex = "#FFB74D",
            faceDetectionOverlayColorHex = "#03DAC6",
            documentScanOverlayColorHex = "#03DAC6",
            pendingStepColorHex = "#666666",
            isDarkMode = true
        )
    ),
    
    CORPORATE_BLUE(
        "Corporate Blue",
        "Professional blue theme for enterprise applications",
        SDKThemeConfiguration(
            brandName = "Corporate",
            primaryColorHex = "#1976D2", // Blue 700
            secondaryColorHex = "#42A5F5", // Blue 400
            backgroundColorHex = "#F5F5F5",
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#000000",
            onBackgroundColorHex = "#212121",
            onSurfaceColorHex = "#212121",
            successColorHex = "#388E3C",
            errorColorHex = "#D32F2F",
            warningColorHex = "#F57C00",
            faceDetectionOverlayColorHex = "#42A5F5",
            documentScanOverlayColorHex = "#42A5F5"
        )
    )
}
