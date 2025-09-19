/*
 * File: BridgeMainActivity.kt
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
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sample.theme.SampleAppThemes
import com.artiusid.sample.theme.EnhancedThemeOption
import com.artiusid.sample.localization.SampleAppLocalization
import com.artiusid.sample.config.ImageOverrideOption
import com.artiusid.sdk.models.SDKImageOverrides
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.callbacks.AuthenticationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.artiusid.sdk.data.model.AppNotificationState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

/**
 * Sample App demonstrating the artius.iD SDK Integration
 * 
 * This sample app shows how to integrate with the artius.iD SDK that
 * launches the complete standalone application with full verification capabilities.
 */
class BridgeMainActivity : ComponentActivity(), VerificationCallback, AuthenticationCallback {
    
    private var isLoading by mutableStateOf(false)
    private var lastResult by mutableStateOf("Application started - checking keychain status...")
    private var selectedTheme by mutableStateOf(EnhancedThemeOption.ARTIUSID_DEFAULT)
    private var selectedImageOverride by mutableStateOf(ImageOverrideOption.DEFAULT)
    private var verificationResultData by mutableStateOf<VerificationResultData?>(null)
    private var showResultsScreen by mutableStateOf(false)
    private var fcmTokenStatus by mutableStateOf("❌ Not available")
    private var fcmTokenPreview by mutableStateOf("")
    
    // Approval flow state
    private var showApprovalRequestScreen by mutableStateOf(false)
    private var showApprovalResponseScreen by mutableStateOf(false)
    private var approvalRequestId by mutableStateOf<Int?>(null)
    private var approvalTitle by mutableStateOf("")
    private var approvalDescription by mutableStateOf("")
    private var approvalResponse by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SDK on startup so it's available for all operations
        initializeSDK()

        // Check FCM token and certificate status on startup
        checkFCMTokenStatus()
        checkCertificateStatus()
        
        // Handle notification intent if app was launched from notification
        handleNotificationIntent(intent)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showApprovalRequestScreen -> {
                            ApprovalRequestScreen(
                                requestId = approvalRequestId,
                                title = approvalTitle,
                                description = approvalDescription,
                                onApprove = {
                                    android.util.Log.d("BridgeMainActivity", "✅ User approved the request")
                                    approvalResponse = "approve"
                                    showApprovalRequestScreen = false
                                    showApprovalResponseScreen = true
                                },
                                onDeny = {
                                    android.util.Log.d("BridgeMainActivity", "❌ User denied the request")
                                    approvalResponse = "deny"
                                    showApprovalRequestScreen = false
                                    showApprovalResponseScreen = true
                                },
                                onNavigateBack = {
                                    android.util.Log.d("BridgeMainActivity", "🔙 User navigated back from approval request")
                                    showApprovalRequestScreen = false
                                    AppNotificationState.reset()
                                }
                            )
                        }
                        showApprovalResponseScreen -> {
                            ApprovalResponseScreen(
                                response = approvalResponse,
                                requestId = approvalRequestId,
                                onNavigateHome = {
                                    android.util.Log.d("BridgeMainActivity", "🏠 Returning to home from approval response")
                                    showApprovalResponseScreen = false
                                    approvalRequestId = null
                                    approvalTitle = ""
                                    approvalDescription = ""
                                    approvalResponse = ""
                                    AppNotificationState.reset()
                                }
                            )
                        }
                        showResultsScreen && verificationResultData != null -> {
                            VerificationResultsScreen(
                                verificationData = verificationResultData!!,
                                onBackHome = {
                                    showResultsScreen = false
                                    verificationResultData = null
                                }
                            )
                        }
                        else -> {
                            BridgeSampleApp()
                        }
                    }
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BridgeSampleApp() {
        // Observe notification state for approval responses
        val notificationType by AppNotificationState.notificationType.collectAsState()
        val requestId by AppNotificationState.requestId.collectAsState()
        val notificationTitle by AppNotificationState.notificationTitle.collectAsState()
        val notificationDescription by AppNotificationState.notificationDescription.collectAsState()
        
        // Handle approval notifications
        LaunchedEffect(notificationType) {
            if (notificationType == AppNotificationState.NotificationType.APPROVAL) {
                android.util.Log.d("BridgeMainActivity", "🔔 Approval notification received!")
                android.util.Log.d("BridgeMainActivity", "📋 Request ID: $requestId")
                android.util.Log.d("BridgeMainActivity", "📋 Title: $notificationTitle")
                android.util.Log.d("BridgeMainActivity", "📋 Description: $notificationDescription")
                
                // Show approval request screen with biometric authentication
                approvalRequestId = requestId
                approvalTitle = notificationTitle
                approvalDescription = notificationDescription
                showApprovalRequestScreen = true
                
                android.util.Log.d("BridgeMainActivity", "🔐 Showing approval request screen with biometric authentication")
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "artius.iD SDK Demo",
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
                    
                    ThemeDropdown(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it }
                    )
                }
            }
            
            // Image Override Selection
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
                        text = "🖼️ Image Override Selection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ImageOverrideDropdown(
                        selectedOverride = selectedImageOverride,
                        onOverrideSelected = { selectedImageOverride = it }
                    )
                    
                    // Show override statistics
                    if (selectedImageOverride != ImageOverrideOption.DEFAULT) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val stats = com.artiusid.sample.config.ImageOverrideHelper.getOverrideStats(selectedImageOverride.overrides)
                        Text(
                            text = "📊 ${stats["activeOverrides"]} overrides active (${stats["overridePercentage"]}%)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.primaryColorHex))
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.onPrimaryColorHex))
                    )
                } else {
                    Text(
                        text = "🔍 Start Verification (Bridge)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.onPrimaryColorHex))
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
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.secondaryColorHex))
                )
            ) {
                Text(
                    text = "🔐 Start Authentication (Bridge)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.onSecondaryColorHex))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { sendApprovalRequest() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.successColorHex))
                )
            ) {
                Text(
                    text = "📋 Test Approval Process",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.onPrimaryColorHex))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { simulateApprovalNotification() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.warningColorHex))
                )
            ) {
                Text(
                    text = "🔔 Simulate Notification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(selectedTheme.themeConfig.colorScheme.onPrimaryColorHex))
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
                        ColorSwatch("Primary", selectedTheme.themeConfig.colorScheme.primaryColorHex)
                        ColorSwatch("Secondary", selectedTheme.themeConfig.colorScheme.secondaryColorHex)
                        ColorSwatch("Background", selectedTheme.themeConfig.colorScheme.backgroundColorHex)
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
    
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNotificationIntent(it) }
    }
    
    private fun handleNotificationIntent(intent: android.content.Intent?) {
        intent?.extras?.let { extras ->
            android.util.Log.d("BridgeMainActivity", "🔔 Handling notification intent with extras: ${extras.keySet()}")
            
            // Check for approval notification data
            val approvalTitle = extras.getString("approvalTitle")
            val approvalDescription = extras.getString("approvalDescription")
            val requestIdString = extras.getString("requestId")
            
            if (!approvalTitle.isNullOrEmpty() && !approvalDescription.isNullOrEmpty()) {
                val requestId = requestIdString?.toIntOrNull()
                android.util.Log.d("BridgeMainActivity", "🔔 Processing approval notification from intent")
                android.util.Log.d("BridgeMainActivity", "📋 Request ID: $requestId")
                android.util.Log.d("BridgeMainActivity", "📋 Title: $approvalTitle")
                android.util.Log.d("BridgeMainActivity", "📋 Description: $approvalDescription")
                
                // Show approval request screen with biometric authentication
                this.approvalRequestId = requestId
                this.approvalTitle = approvalTitle
                this.approvalDescription = approvalDescription
                showApprovalRequestScreen = true
                
                android.util.Log.d("BridgeMainActivity", "🔐 Showing approval request screen from notification intent")
            }
        }
    }

    private fun initializeSDK() {
        try {
            android.util.Log.d("BridgeMainActivity", "🚀 Initializing SDK on app startup...")
            
            // Get localization overrides
            val localizationOverrides = SampleAppLocalization.getStringOverrides(this)
            android.util.Log.d("BridgeMainActivity", "🌐 Localization overrides: ${localizationOverrides.size} strings")
            
            // Create SDK configuration
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = Environment.STAGING,
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true,
                localizationOverrides = localizationOverrides,
                imageOverrides = selectedImageOverride.overrides
            )
            
            // Initialize SDK with enhanced theme
            ArtiusIDSDK.initializeWithEnhancedTheme(
                context = this,
                configuration = sdkConfig,
                enhancedTheme = selectedTheme.themeConfig
            )
            
            android.util.Log.d("BridgeMainActivity", "✅ SDK initialized successfully on startup")
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Failed to initialize SDK on startup", e)
            lastResult = "❌ SDK initialization error: ${e.message}"
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
            
            android.util.Log.d("BridgeMainActivity", "🔍 Starting verification flow...")
            android.util.Log.d("BridgeMainActivity", "🎨 Selected Theme: ${selectedTheme.displayName}")
            android.util.Log.d("BridgeMainActivity", "🖼️ Selected Image Override: ${selectedImageOverride.displayName}")
            
            // Update SDK configuration with current theme and image overrides
            // (SDK is already initialized, but we may need to update theme/images)
            val localizationOverrides = SampleAppLocalization.getStringOverrides(this)
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = Environment.STAGING,
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true,
                localizationOverrides = localizationOverrides,
                imageOverrides = selectedImageOverride.overrides
            )
            
            // Re-initialize with updated theme and image overrides
            ArtiusIDSDK.initializeWithEnhancedTheme(
                context = this,
                configuration = sdkConfig,
                enhancedTheme = selectedTheme.themeConfig
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
            
            // Initialize SDK Bridge with selected theme, shared context, and localization overrides
            val localizationOverrides = SampleAppLocalization.getStringOverrides(this)
            android.util.Log.d("BridgeMainActivity", "🌐 Localization overrides: ${localizationOverrides.size} strings")
            
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = Environment.STAGING,
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true,
                localizationOverrides = localizationOverrides,
                imageOverrides = selectedImageOverride.overrides
            )
            
            ArtiusIDSDK.initializeWithEnhancedTheme(
                context = this,
                configuration = sdkConfig,
                enhancedTheme = selectedTheme.themeConfig
            )
            
            // Start authentication via bridge to standalone app
            ArtiusIDSDK.startAuthentication(this, this)
            
        } catch (e: Exception) {
            isLoading = false
            lastResult = "❌ Error starting authentication bridge: ${e.message}"
        }
    }
    
    private fun sendApprovalRequest() {
        try {
            isLoading = true
            android.util.Log.d("BridgeMainActivity", "📋 Starting approval request test...")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use the new public SDK method for approval requests
                    android.util.Log.d("BridgeMainActivity", "📋 Sending approval request via SDK...")
                    val (success, message, requestId) = ArtiusIDSDK.sendApprovalRequest(this@BridgeMainActivity)
                    
                    // Update UI on main thread
                    runOnUiThread {
                        isLoading = false
                        if (success) {
                            lastResult = "✅ Approval request sent successfully!\nRequest ID: $requestId\nMessage: $message"
                            android.util.Log.d("BridgeMainActivity", "✅ Approval request successful: $message")
                        } else {
                            lastResult = "❌ Approval request failed: $message"
                            android.util.Log.e("BridgeMainActivity", "❌ Approval request failed: $message")
                        }
                    }
                    
                } catch (e: Exception) {
                    runOnUiThread {
                        isLoading = false
                        lastResult = "❌ Approval request error: ${e.message}"
                        android.util.Log.e("BridgeMainActivity", "❌ Approval request exception", e)
                    }
                }
            }
            
        } catch (e: Exception) {
            isLoading = false
            lastResult = "❌ Error starting approval request: ${e.message}"
            android.util.Log.e("BridgeMainActivity", "❌ Error starting approval request", e)
        }
    }
    
    private fun simulateApprovalNotification() {
        try {
            android.util.Log.d("BridgeMainActivity", "🔔 Simulating approval notification...")
            
            // Simulate an approval notification response
            AppNotificationState.handleApprovalNotification(
                requestId = 12345,
                title = "Approval Request Approved",
                description = "Your test approval request for \$25.00 has been approved."
            )
            
            android.util.Log.d("BridgeMainActivity", "✅ Simulated notification sent to AppNotificationState")
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Error simulating notification: ${e.message}", e)
            lastResult = "❌ Error simulating notification: ${e.message}"
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
 * Theme selection dropdown composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    selectedTheme: EnhancedThemeOption,
    onThemeSelected: (EnhancedThemeOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTheme.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Select Theme") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            EnhancedThemeOption.values().forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = theme.displayName,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Image Override selection dropdown composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageOverrideDropdown(
    selectedOverride: ImageOverrideOption,
    onOverrideSelected: (ImageOverrideOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOverride.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Select Image Override") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ImageOverrideOption.values().forEach { override ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = override.displayName,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = override.description,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    },
                    onClick = {
                        onOverrideSelected(override)
                        expanded = false
                    }
                )
            }
        }
    }
}

