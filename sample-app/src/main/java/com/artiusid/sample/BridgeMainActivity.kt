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
 * Sample App demonstrating the ArtiusID SDK Integration
 * 
 * This sample app shows how to integrate with the ArtiusID SDK that
 * launches the complete standalone application with full verification capabilities.
 */
class BridgeMainActivity : ComponentActivity(), VerificationCallback, AuthenticationCallback {
    
    private var isLoading by mutableStateOf(false)
    private var lastResult by mutableStateOf("Application started - checking keychain status...")
    private var selectedTheme by mutableStateOf(ThemeOption.ARTIUSID_DEFAULT)
    private var verificationResultData by mutableStateOf<VerificationResultData?>(null)
    private var showResultsScreen by mutableStateOf(false)
    private var fcmTokenStatus by mutableStateOf("❌ Not available")
    private var fcmTokenPreview by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check FCM token and certificate status on startup
        checkFCMTokenStatus()
        checkCertificateStatus()
        
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
                        
                        // Add FCM Token and Certificate Info
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // FCM Token Section
                        Text(
                            text = "🔥 FCM Token Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = "Status: $fcmTokenStatus",
                            fontSize = 12.sp,
                            color = if (fcmTokenStatus.contains("✅")) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        
                        if (fcmTokenPreview.isNotEmpty()) {
                            Text(
                                text = "Token: $fcmTokenPreview",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Certificate Section
                        Text(
                            text = "🔐 Client Certificate Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        val certManager = com.artiusid.sdk.utils.CertificateManager(this@BridgeMainActivity)
                        val hasCertificate = try {
                            certManager.loadCertificatePem() != null
                        } catch (e: Exception) {
                            false
                        }
                        
                        val certStatus = if (hasCertificate) "✅ Loaded" else "❌ Not loaded"
                        val certColor = if (hasCertificate) Color(0xFF4CAF50) else Color(0xFFF44336)
                        
                        Text(
                            text = "Status: $certStatus",
                            fontSize = 12.sp,
                            color = certColor
                        )
                        
                        if (hasCertificate) {
                            val keyMatch = try {
                                certManager.verifyCertificateKeyMatch()
                            } catch (e: Exception) {
                                false
                            }
                            
                            Text(
                                text = "Key Match: ${if (keyMatch) "✅ Valid" else "❌ Invalid"}",
                                fontSize = 10.sp,
                                color = if (keyMatch) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
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
    
    private fun checkFCMTokenStatus() {
        android.util.Log.d("BridgeMainActivity", "🔍 Checking FCM token status...")
        
        try {
            // Check Firebase initialization first
            try {
                val firebaseApp = com.google.firebase.FirebaseApp.getInstance()
                android.util.Log.d("BridgeMainActivity", "🔥 Firebase app instance: ${firebaseApp.name}")
            } catch (e: Exception) {
                android.util.Log.e("BridgeMainActivity", "❌ Firebase not initialized properly", e)
                fcmTokenStatus = "❌ Firebase not initialized"
                lastResult = "❌ Firebase initialization error: ${e.message}"
                return
            }
            
            val fcmTokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this)
            android.util.Log.d("BridgeMainActivity", "📱 FCM TokenManager instance: ${fcmTokenManager != null}")
            
            if (fcmTokenManager == null) {
                fcmTokenStatus = "❌ Manager null"
                lastResult = "❌ FCM TokenManager could not be created"
                return
            }
            
            val cachedToken = fcmTokenManager.getFCMToken() ?: ""
            android.util.Log.d("BridgeMainActivity", "💾 Cached token length: ${cachedToken.length}")
            
            if (cachedToken.isNotEmpty()) {
                fcmTokenStatus = "✅ Available"
                fcmTokenPreview = cachedToken.take(20) + "..."
                android.util.Log.d("BridgeMainActivity", "✅ FCM token found in cache: ${cachedToken.take(20)}...")
                
                // Update last result to show we found the token
                lastResult = "✅ FCM Token found in keychain: ${cachedToken.take(20)}..."
            } else {
                android.util.Log.d("BridgeMainActivity", "⚠️ No cached FCM token, attempting to retrieve...")
                fcmTokenStatus = "🔄 Retrieving..."
                lastResult = "🔄 Retrieving FCM Token..."
                
                // Try to get token asynchronously using Firebase Messaging directly
                Thread {
                    try {
                        android.util.Log.d("BridgeMainActivity", "🔄 Calling Firebase Messaging directly...")
                        
                        // Try direct Firebase Messaging call
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                            .addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    android.util.Log.w("BridgeMainActivity", "⚠️ Fetching FCM registration token failed", task.exception)
                                    runOnUiThread {
                                        fcmTokenStatus = "❌ Not available"
                                        lastResult = "❌ FCM Token fetch failed: ${task.exception?.message}"
                                    }
                                    return@addOnCompleteListener
                                }

                                // Get new FCM registration token
                                val token = task.result
                                android.util.Log.d("BridgeMainActivity", "📥 Direct Firebase token result: ${token?.take(20) ?: "null"}")
                                
                                if (!token.isNullOrEmpty()) {
                                    // Save token using FirebaseTokenManager
                                    fcmTokenManager.saveToken(token)
                                    
                                    runOnUiThread {
                                        fcmTokenStatus = "✅ Available"
                                        fcmTokenPreview = token.take(20) + "..."
                                        lastResult = "✅ FCM Token retrieved and cached: ${token.take(20)}..."
                                    }
                                    android.util.Log.d("BridgeMainActivity", "✅ FCM token retrieved directly: ${token.take(20)}...")
                                } else {
                                    runOnUiThread {
                                        fcmTokenStatus = "❌ Not available"
                                        lastResult = "❌ FCM Token is null/empty"
                                    }
                                    android.util.Log.w("BridgeMainActivity", "⚠️ FCM token is null or empty")
                                }
                            }
                    } catch (e: Exception) {
                        runOnUiThread {
                            fcmTokenStatus = "❌ Error"
                            lastResult = "❌ FCM Token error: ${e.message}"
                        }
                        android.util.Log.e("BridgeMainActivity", "❌ Error retrieving FCM token", e)
                    }
                }.start()
            }
        } catch (e: Exception) {
            fcmTokenStatus = "❌ Error"
            lastResult = "❌ FCM Token check error: ${e.message}"
            android.util.Log.e("BridgeMainActivity", "❌ Error checking FCM token status", e)
        }
    }
    
    private fun checkCertificateStatus() {
        android.util.Log.d("BridgeMainActivity", "🔐 Checking certificate status...")
        
        try {
            val certManager = com.artiusid.sdk.utils.CertificateManager(this)
            android.util.Log.d("BridgeMainActivity", "📱 Certificate manager created for context: ${this.packageName}")
            
            val hasCertificate = try {
                val cert = certManager.loadCertificatePem()
                android.util.Log.d("BridgeMainActivity", "💾 Certificate PEM loaded: ${cert != null}, length: ${cert?.length ?: 0}")
                cert != null && cert.isNotEmpty()
            } catch (e: Exception) {
                android.util.Log.w("BridgeMainActivity", "Certificate load failed: ${e.message}")
                false
            }
            
            if (hasCertificate) {
                val keyMatch = try {
                    val result = certManager.verifyCertificateKeyMatch()
                    android.util.Log.d("BridgeMainActivity", "🔑 Key match verification result: $result")
                    result
                } catch (e: Exception) {
                    android.util.Log.w("BridgeMainActivity", "Key match verification failed: ${e.message}")
                    false
                }
                
                val certStatus = if (keyMatch) "✅ Valid certificate with matching key" else "⚠️ Certificate found but key mismatch"
                android.util.Log.d("BridgeMainActivity", "🔐 Certificate status: $certStatus")
                
                // Update last result to include certificate info
                if (lastResult.contains("FCM Token")) {
                    lastResult += "\n$certStatus"
                } else {
                    lastResult = certStatus
                }
            } else {
                android.util.Log.d("BridgeMainActivity", "⚠️ No certificate found, this will be generated during verification")
                val certStatus = "⚠️ No certificate - will generate during verification"
                
                // Update last result to include certificate info
                if (lastResult.contains("FCM Token")) {
                    lastResult += "\n$certStatus"
                } else {
                    lastResult = certStatus
                }
            }
        } catch (e: Exception) {
            val errorStatus = "❌ Certificate check error: ${e.message}"
            android.util.Log.e("BridgeMainActivity", "❌ Error checking certificate status", e)
            
            // Update last result to include certificate error
            if (lastResult.contains("FCM Token")) {
                lastResult += "\n$errorStatus"
            } else {
                lastResult = errorStatus
            }
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
        
        // Refresh FCM token status after verification
        checkFCMTokenStatus()
        
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
