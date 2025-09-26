/*
 * File: BridgeMainActivity.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ColorScheme
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Sample App demonstrating the artius.iD SDK Integration
 * 
 * This sample app shows how to integrate with the artius.iD SDK that
 * launches the complete standalone application with full verification capabilities.
 */
class BridgeMainActivity : FragmentActivity(), VerificationCallback, AuthenticationCallback {
    
    /**
     * Create Material3 ColorScheme from selected theme
     */
    private fun createColorSchemeFromTheme(theme: EnhancedThemeOption): ColorScheme {
        val themeConfig = theme.themeConfig
        val colors = themeConfig.colorScheme
        
        // Determine if this is a dark theme by checking background color luminance
        val backgroundColor = android.graphics.Color.parseColor(colors.backgroundColorHex)
        val luminance = (0.299 * android.graphics.Color.red(backgroundColor) + 
                        0.587 * android.graphics.Color.green(backgroundColor) + 
                        0.114 * android.graphics.Color.blue(backgroundColor)) / 255.0
        val isDarkTheme = luminance < 0.5
        
        // Special handling for artius.iD theme - use light scheme even though it has dark accent colors
        val isArtiusIDTheme = theme == EnhancedThemeOption.ARTIUSID_DEFAULT
        
        return if (isDarkTheme && !isArtiusIDTheme) {
            // Use dark color scheme for dark themes - HARDCODED to preserve current sample app appearance
            darkColorScheme(
                primary = Color(0xFFF58220), // Orange - hardcoded from current theme
                onPrimary = Color(0xFF22354D), // Dark blue - hardcoded
                secondary = Color(0xFF22354D), // Dark blue - hardcoded
                onSecondary = Color(0xFFFFFFFF), // White - hardcoded
                background = Color(0xFFF8F9FA), // Light background - hardcoded
                onBackground = Color(0xFF22354D), // Dark text - hardcoded
                surface = Color(0xFFFFFFFF), // White surface - hardcoded
                onSurface = Color(0xFF22354D), // Dark text - hardcoded
                error = Color(0xFFD32F2F), // Red - hardcoded
                onError = Color(0xFFFFFFFF) // White - hardcoded
            )
        } else {
            // Use light color scheme for light themes - HARDCODED to preserve current sample app appearance
            lightColorScheme(
                primary = Color(0xFF22354D), // Dark blue - hardcoded from current theme
                onPrimary = Color(0xFFFFFFFF), // White - hardcoded
                secondary = Color(0xFFF58220), // Orange - hardcoded
                onSecondary = Color(0xFF22354D), // Dark blue - hardcoded
                background = Color(0xFFF8F9FA), // Light background - hardcoded
                onBackground = Color(0xFF22354D), // Dark text - hardcoded
                surface = Color(0xFFFFFFFF), // White surface - hardcoded
                onSurface = Color(0xFF22354D), // Dark text - hardcoded
                error = Color(0xFFD32F2F), // Red - hardcoded
                onError = Color(0xFFFFFFFF) // White - hardcoded
            )
        }
    }
    
    private var isVerificationLoading by mutableStateOf(false)
    private var isApprovalLoading by mutableStateOf(false)
    private var lastResult by mutableStateOf("Application started - checking keychain status...")
    private var selectedTheme by mutableStateOf(EnhancedThemeOption.ARTIUSID_DEFAULT)
    private var selectedImageOverride by mutableStateOf(ImageOverrideOption.DEFAULT)
    private var verificationResultData by mutableStateOf<VerificationResultData?>(null)
    private var showResultsScreen by mutableStateOf(false)
    private var fcmTokenStatus by mutableStateOf("❌ Not available")
    private var fcmTokenPreview by mutableStateOf("")
    private var memberIdStatus by mutableStateOf("❌ Not available")
    private var memberIdPreview by mutableStateOf("")
    
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

        // Request notification permissions for Android 13+
        requestNotificationPermissions()

        // Check FCM token, certificate, and member ID status on startup
        checkFCMTokenStatus()
        checkCertificateStatus()
        checkMemberIdStatus()

        // Handle notification intent if app was launched from notification
        handleNotificationIntent(intent)
        
        setContent {
            // Create custom ColorScheme from selected theme
            val customColorScheme = createColorSchemeFromTheme(selectedTheme)
            
            MaterialTheme(colorScheme = customColorScheme) {
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
                                    
                                    // Clear notification state to prevent loop
                                    AppNotificationState.reset()
                                    
                                    // Send approval response to server using SDK (like iOS)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val result = ArtiusIDSDK.sendApprovalResponse(this@BridgeMainActivity, "yes")
                                            if (result != null) {
                                                android.util.Log.d("BridgeMainActivity", "✅ Approval response sent successfully: $result")
                                            } else {
                                                android.util.Log.e("BridgeMainActivity", "❌ Failed to send approval response")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("BridgeMainActivity", "❌ Error sending approval response", e)
                                        }
                                        
                                        // Navigate to response screen on main thread
                                        CoroutineScope(Dispatchers.Main).launch {
                                            showApprovalResponseScreen = true
                                        }
                                    }
                                },
                                onDeny = {
                                    android.util.Log.d("BridgeMainActivity", "❌ User denied the request")
                                    approvalResponse = "deny"
                                    showApprovalRequestScreen = false
                                    
                                    // Clear notification state to prevent loop
                                    AppNotificationState.reset()
                                    
                                    // Send denial response to server using SDK (like iOS)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val result = ArtiusIDSDK.sendApprovalResponse(this@BridgeMainActivity, "no")
                                            if (result != null) {
                                                android.util.Log.d("BridgeMainActivity", "✅ Denial response sent successfully: $result")
                                            } else {
                                                android.util.Log.e("BridgeMainActivity", "❌ Failed to send denial response")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("BridgeMainActivity", "❌ Error sending denial response", e)
                                        }
                                        
                                        // Navigate to response screen on main thread
                                        CoroutineScope(Dispatchers.Main).launch {
                                            showApprovalResponseScreen = true
                                        }
                                    }
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
                enabled = !isVerificationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22354D) // Hardcoded dark blue to preserve sample app appearance
                )
            ) {
                if (isVerificationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFFFFFFFF) // Hardcoded white to preserve sample app appearance
                    )
                } else {
                    Text(
                        text = "🔍 Start Verification (Bridge)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF) // Hardcoded white to preserve sample app appearance
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { startAuthenticationFlow() },
                enabled = !isVerificationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF58220) // Hardcoded orange to preserve sample app appearance
                )
            ) {
                Text(
                    text = "🔐 Start Authentication (Bridge)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF) // Hardcoded white to preserve sample app appearance
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { sendApprovalRequest() },
                enabled = !isApprovalLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Hardcoded green to preserve sample app appearance
                )
            ) {
                if (isApprovalLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFFFFFFFF) // Hardcoded white to preserve sample app appearance
                    )
                } else {
                    Text(
                        text = "📋 Test Approval Process",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF) // Hardcoded white to preserve sample app appearance
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // FCM Token Refresh Button (for debugging)
            OutlinedButton(
                onClick = { refreshFCMToken() },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22354D)), // Hardcoded dark blue
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF22354D), // Hardcoded dark blue to preserve sample app appearance
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "🔥 Refresh FCM Token",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22354D) // Hardcoded dark blue to preserve sample app appearance
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Member ID Section
                        Text(
                            text = "👤 Member ID Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = "Status: $memberIdStatus",
                            fontSize = 12.sp,
                            color = if (memberIdStatus.contains("✅")) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        
                        if (memberIdPreview.isNotEmpty()) {
                            Text(
                                text = "ID: $memberIdPreview",
                                fontSize = 10.sp,
                                color = Color.Gray,
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
                    containerColor = Color(android.graphics.Color.parseColor(colorHex)) // Keep dynamic for color swatches
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

    private fun checkMemberIdStatus() {
        android.util.Log.d("BridgeMainActivity", "👤 Checking member ID status...")
        
        try {
            // First check if we have verification result data (most recent/accurate)
            val resultMemberId = verificationResultData?.accountNumber
            
            // Also check VerificationStateManager (secure storage)
            val verificationStateManager = com.artiusid.sdk.utils.VerificationStateManager(this)
            val storedMemberId = verificationStateManager.getAccountNumber()
            
            android.util.Log.d("BridgeMainActivity", "📱 VerificationStateManager created for context: ${this.packageName}")
            android.util.Log.d("BridgeMainActivity", "🔍 Result Member ID: ${resultMemberId?.take(8)}...${resultMemberId?.takeLast(4)}")
            android.util.Log.d("BridgeMainActivity", "🔍 Stored Member ID: ${storedMemberId?.take(8)}...${storedMemberId?.takeLast(4)}")
            
            // Use the most recent verification result if available, otherwise use stored
            val memberId = if (!resultMemberId.isNullOrEmpty()) {
                android.util.Log.d("BridgeMainActivity", "✅ Using Member ID from verification result (most recent)")
                resultMemberId
            } else if (!storedMemberId.isNullOrEmpty()) {
                android.util.Log.d("BridgeMainActivity", "✅ Using Member ID from secure storage")
                storedMemberId
            } else {
                null
            }

            if (!memberId.isNullOrEmpty()) {
                memberIdStatus = "✅ Available"
                memberIdPreview = memberId.take(8) + "..." + memberId.takeLast(4)
                android.util.Log.d("BridgeMainActivity", "💾 Final Member ID: ${memberId.take(8)}...${memberId.takeLast(4)}")
                android.util.Log.d("BridgeMainActivity", "✅ Full Member ID for approval: $memberId")
                
                // CRITICAL: Sync the Member ID to VerificationStateManager if it's from result data
                if (!resultMemberId.isNullOrEmpty() && resultMemberId != storedMemberId) {
                    android.util.Log.d("BridgeMainActivity", "🔄 Syncing Member ID to secure storage for approval requests")
                    verificationStateManager.storeVerificationSuccess(
                        accountNumber = resultMemberId,
                        accountFullName = "${verificationResultData?.firstName ?: ""} ${verificationResultData?.lastName ?: ""}".trim().takeIf { it.isNotEmpty() },
                        isAccountActive = true
                    )
                }
                
                val memberStatus = "✅ Member ID: ${memberId.take(8)}...${memberId.takeLast(4)}"
                
                // Update last result to include member ID info
                if (lastResult.contains("Certificate") || lastResult.contains("FCM Token")) {
                    lastResult += "\n$memberStatus"
                } else {
                    lastResult = memberStatus
                }
            } else {
                memberIdStatus = "❌ Not available"
                memberIdPreview = "N/A"
                android.util.Log.w("BridgeMainActivity", "⚠️ No member ID found in verification result or secure storage.")
                android.util.Log.w("BridgeMainActivity", "⚠️ User must complete verification first to get member ID for approval requests")
                
                val memberStatus = "⚠️ No Member ID - complete verification first"
                
                // Update last result to include member ID warning
                if (lastResult.contains("Certificate") || lastResult.contains("FCM Token")) {
                    lastResult += "\n$memberStatus"
                } else {
                    lastResult = memberStatus
                }
            }
        } catch (e: Exception) {
            val errorStatus = "❌ Member ID check error: ${e.message}"
            android.util.Log.e("BridgeMainActivity", "❌ Error checking member ID status", e)
            
            memberIdStatus = "❌ Error"
            memberIdPreview = "Error"
            
            // Update last result to include member ID error
            if (lastResult.contains("Certificate") || lastResult.contains("FCM Token")) {
                lastResult += "\n$errorStatus"
            } else {
                lastResult = errorStatus
            }
        }
    }
    
    private fun startVerificationFlow() {
        try {
            isVerificationLoading = true
            
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
            isVerificationLoading = false
            lastResult = "❌ Error starting verification bridge: ${e.message}"
        }
    }
    
    private fun startAuthenticationFlow() {
        try {
            isVerificationLoading = true
            
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
            isVerificationLoading = false
            lastResult = "❌ Error starting authentication bridge: ${e.message}"
        }
    }
    
    private fun sendApprovalRequest() {
        try {
            isApprovalLoading = true
            android.util.Log.d("BridgeMainActivity", "📋 Starting approval request test...")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use the new public SDK method for approval requests
                    android.util.Log.d("BridgeMainActivity", "📋 Sending approval request via SDK...")
                    val (success, message, requestId) = ArtiusIDSDK.sendApprovalRequest(this@BridgeMainActivity)
                    
                    // Update UI on main thread
                    runOnUiThread {
                        isApprovalLoading = false
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
                        isApprovalLoading = false
                        lastResult = "❌ Approval request error: ${e.message}"
                        android.util.Log.e("BridgeMainActivity", "❌ Approval request exception", e)
                    }
                }
            }
            
        } catch (e: Exception) {
            isApprovalLoading = false
            lastResult = "❌ Error starting approval request: ${e.message}"
            android.util.Log.e("BridgeMainActivity", "❌ Error starting approval request", e)
        }
    }
    
    private fun requestNotificationPermissions() {
        try {
            android.util.Log.d("BridgeMainActivity", "🔔 Checking notification permissions...")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    android.util.Log.d("BridgeMainActivity", "📱 Requesting POST_NOTIFICATIONS permission...")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                } else {
                    android.util.Log.d("BridgeMainActivity", "✅ POST_NOTIFICATIONS permission already granted")
                }
            } else {
                android.util.Log.d("BridgeMainActivity", "✅ Notification permissions not required for this Android version")
            }
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Error requesting notification permissions: ${e.message}", e)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.d("BridgeMainActivity", "✅ POST_NOTIFICATIONS permission granted")
                    lastResult = "✅ Notification permissions granted - ready to receive approval notifications"
                } else {
                    android.util.Log.w("BridgeMainActivity", "⚠️ POST_NOTIFICATIONS permission denied")
                    lastResult = "⚠️ Notification permissions denied - may not receive approval notifications"
                }
            }
        }
    }
    
    private fun refreshFCMToken() {
        try {
            android.util.Log.d("BridgeMainActivity", "🔥 Refreshing FCM token...")
            
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.e("BridgeMainActivity", "❌ Failed to get FCM token", task.exception)
                    lastResult = "❌ Failed to refresh FCM token: ${task.exception?.message}"
                    return@addOnCompleteListener
                }

                // Get new token
                val token = task.result
                android.util.Log.d("BridgeMainActivity", "🔥 Fresh FCM token: $token")
                android.util.Log.d("BridgeMainActivity", "🔥 Token length: ${token.length} characters")
                
                // Save the new token
                val tokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this)
                tokenManager?.saveToken(token)
                
                // Update UI
                lastResult = "🔥 FCM Token refreshed successfully!\nToken: ${token.take(20)}...\nLength: ${token.length} chars"
                
                // Update FCM status
                checkFCMTokenStatus()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Error refreshing FCM token: ${e.message}", e)
            lastResult = "❌ Error refreshing FCM token: ${e.message}"
        }
    }
    
    
    // VerificationCallback implementation
    override fun onVerificationSuccess(result: VerificationResult) {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Parse the verification result data for the results screen
        verificationResultData = VerificationResultData.fromPayload(result.rawResponse)
        
        // Show the results screen
        showResultsScreen = true
        
        // Refresh FCM token and member ID status after verification
        checkFCMTokenStatus()
        checkMemberIdStatus()
        
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
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Verification Error (Bridge) [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onVerificationCancelled() {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Verification Cancelled (Bridge) [$timestamp]\n🌉 Via Standalone App Bridge"
    }
    
    // AuthenticationCallback implementation
    override fun onAuthenticationSuccess(result: AuthenticationResult) {
        isVerificationLoading = false
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
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Authentication Error (Bridge) [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
            🌉 Via Standalone App Bridge
        """.trimIndent()
    }
    
    override fun onAuthenticationCancelled() {
        isVerificationLoading = false
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

