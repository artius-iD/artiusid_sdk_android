package com.artiusid.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.callbacks.*
import com.artiusid.sdk.config.*
import com.artiusid.sdk.models.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date

/**
 * MINIMAL Sample Application for ArtiusID SDK
 * 
 * This demonstrates the CORRECT SDK integration pattern:
 * 
 * 1. Host app provides ONLY configuration (theme, branding, API keys)
 * 2. Host app calls SDK.startVerificationFlow() - SDK handles EVERYTHING
 * 3. Host app calls SDK.startAuthenticationFlow() - SDK handles EVERYTHING  
 * 4. SDK provides COMPLETE standalone app experience internally
 * 5. SDK returns results to host app via callbacks
 * 
 * The SDK contains the ENTIRE standalone application UI/UX and functionality.
 * The host app just launches it and receives results.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. CONFIGURE SDK with host app's branding and settings
        initializeSDK()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleAppContent()
                }
            }
        }
    }
    
    /**
     * Initialize SDK with host app configuration
     * This is the ONLY setup the host app needs to do
     */
    private fun initializeSDK() {
        val sdkConfig = ArtiusSDKConfig.Builder()
            // API Configuration
            .setApiEndpoint("https://api.artiusid.com")
            .setApiKey("demo-api-key-12345")
            .setEnvironment(Environment.DEVELOPMENT)
            
            // Host App Branding (SDK will use this in its UI)
            .setTheme(createHostAppTheme())
            .setSecurityConfig(createSecurityConfig())
            
            // Firebase Token Provider (for mTLS)
            .setFirebaseTokenProvider { getFirebaseToken() }
            
            // Optional: Localization
            .setLocalizationConfig(createLocalizationConfig())
            
            .setDebugMode(true)
            .build()
        
        // Initialize SDK - this prepares it to provide complete standalone experience
        ArtiusIDSDK.initialize(this, sdkConfig)
        
        android.util.Log.i("SampleApp", "✅ ArtiusID SDK initialized with complete standalone functionality")
        android.util.Log.i("SampleApp", "🎨 SDK will use host app branding in its sophisticated UI")
        android.util.Log.i("SampleApp", "🚀 Ready to launch complete verification/authentication flows")
    }
    
    /**
     * Create host app theme that SDK will use in its UI
     */
    private fun createHostAppTheme(): SDKTheme {
        return SDKTheme.Builder()
            .setPrimaryColor(Color(0xFF1976D2))      // Host app blue
            .setSecondaryColor(Color(0xFF424242))    // Host app gray
            .setAccentColor(Color(0xFF4CAF50))       // Host app green
            .setBackgroundColor(Color(0xFFFAFAFA))   // Host app background
            .setSurfaceColor(Color.White)            // Host app surface
            .setErrorColor(Color(0xFFD32F2F))        // Host app error red
            .setTextPrimaryColor(Color(0xFF212121))  // Host app text
            .setTextSecondaryColor(Color(0xFF757575)) // Host app secondary text
            .setButtonStyle(ButtonStyle.ROUNDED)
            .setProgressStyle(ProgressStyle.CIRCULAR)
            .setLogoUrl("https://example.com/logo.png") // Host app logo in SDK
            .build()
    }
    
    private fun createSecurityConfig(): SecurityConfig {
        return SecurityConfig.Builder()
            .setEnableCertificatePinning(false) // Disable for demo - no certificates provided
            .setEnableRootDetection(false) // Disable for demo to avoid issues on development devices
            .setEnableAntiTampering(false) // Disable for demo to avoid issues on development devices
            .build()
    }
    
    private fun createLocalizationConfig(): LocalizationConfig {
        return LocalizationConfig.Builder()
            .setLanguage("en")
            .setCountry("US")
            .build()
    }
    
    /**
     * Simulated Firebase token provider
     * In real app, this would get actual Firebase token for mTLS
     */
    private suspend fun getFirebaseToken(): String {
        delay(100) // Simulate network call
        return "firebase-token-${System.currentTimeMillis()}"
    }
}

/**
 * MINIMAL host app UI - just two buttons to launch SDK flows
 * All the sophisticated UI/UX happens inside the SDK
 */
@Composable
fun SampleAppContent() {
    var lastResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as ComponentActivity
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Host App Header
        Text(
            text = "ArtiusID SDK Demo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Complete Standalone App Experience",
            fontSize = 16.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Launch Complete Verification Flow
        Button(
            onClick = {
                isLoading = true
                // Launch verification flow from the activity context
                ArtiusIDSDK.startVerificationFlow(
                    activity = activity,
                    callback = object : VerificationCallback {
                        override fun onVerificationComplete(result: VerificationResult) {
                            lastResult = "✅ Verification Success!\n" +
                                    "Confidence: ${result.confidence}\n" +
                                    "Face Match: ${result.faceMatch}\n" +
                                    "Document Valid: ${result.documentValid}\n" +
                                    "Completed: ${formatTimestamp(result.timestamp)}"
                            isLoading = false
                        }
                        
                        override fun onVerificationError(error: SDKError) {
                            lastResult = "❌ Verification Failed\n" +
                                    "Error: ${error.message}\n" +
                                    "Code: ${error.code}\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                        
                        override fun onVerificationCancelled() {
                            lastResult = "🚫 Verification Cancelled\n" +
                                    "User cancelled the verification process\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                        
                        override fun onVerificationBackled() {
                            lastResult = "🚫 Verification Back Pressed\n" +
                                    "User pressed back during verification\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "🔍 Start Complete Verification Flow",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Launch Complete Authentication Flow
        Button(
            onClick = {
                isLoading = true
                // Launch authentication flow from the activity context
                ArtiusIDSDK.startAuthenticationFlow(
                    activity = activity,
                    callback = object : AuthenticationCallback {
                        override fun onAuthenticationComplete(result: AuthenticationResult) {
                            lastResult = "✅ Authentication Success!\n" +
                                    "Token: ${result.token?.take(20)}...\n" +
                                    "User ID: ${result.userId}\n" +
                                    "Completed: ${formatTimestamp(result.timestamp)}"
                            isLoading = false
                        }
                        
                        override fun onAuthenticationError(error: SDKError) {
                            lastResult = "❌ Authentication Failed\n" +
                                    "Error: ${error.message}\n" +
                                    "Code: ${error.code}\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                        
                        override fun onAuthenticationCancelled() {
                            lastResult = "🚫 Authentication Cancelled\n" +
                                    "User cancelled the authentication process\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                        
                        override fun onAuthenticationBackled() {
                            lastResult = "🚫 Authentication Back Pressed\n" +
                                    "User pressed back during authentication\n" +
                                    "Time: ${formatTimestamp(System.currentTimeMillis())}"
                            isLoading = false
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "🔐 Start Complete Authentication Flow",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // SDK Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 How This Works:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• SDK contains COMPLETE standalone app UI/UX\n" +
                          "• Host app provides only configuration & branding\n" +
                          "• SDK handles entire verification/auth process\n" +
                          "• Results returned via callbacks",
                    fontSize = 12.sp,
                    color = Color(0xFF424242),
                    lineHeight = 16.sp
                )
            }
        }
        
        // Show last result
        lastResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.contains("Success")) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Last Result:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (result.contains("Success")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}



/**
 * Helper function to format timestamps
 */
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(Date(timestamp))
}