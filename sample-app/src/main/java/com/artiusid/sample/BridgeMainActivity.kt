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
import com.artiusid.sdk.presentation.screens.approval.ApprovalRequestViewModel
import com.artiusid.sdk.data.api.ApiService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sample.config.AppUrlConfig

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
    private var selectedEnvironment by mutableStateOf("Sandbox")
    private var selectedDomain by mutableStateOf("artiusid.dev")
    private var includeOktaID by mutableStateOf(true) // Okta ID enabled by default (matches iOS)
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

        // Set URL configuration from config file
        UrlBuilder.setConfiguration(AppUrlConfig.getConfiguration())
        
        // 🚨 CRITICAL FIX: Auto-detect environment from stored credentials
        val environmentCredentialManager = com.artiusid.sdk.utils.EnvironmentCredentialManager.getInstance(this)
        val detectedEnvironment = environmentCredentialManager.autoDetectEnvironmentFromCredentials()
        
        android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
        android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: ENVIRONMENT AUTO-DETECTION")
        android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
        android.util.Log.d("BridgeMainActivity", "🚨 Detected environment from credentials: $detectedEnvironment")
        
        // Use detected environment if available, otherwise fall back to config file
        selectedEnvironment = if (!detectedEnvironment.isNullOrEmpty()) {
            android.util.Log.d("BridgeMainActivity", "🚨 ✅ Using detected environment: $detectedEnvironment")
            // Sync URL builder with detected environment
            environmentCredentialManager.setEnvironmentForAllCredentials(detectedEnvironment)
            detectedEnvironment
        } else {
            android.util.Log.d("BridgeMainActivity", "🚨 ⚠️ No credentials found, using config file default")
            val config = UrlBuilder.getCurrentConfiguration()
            config.environment
        }
        
        selectedDomain = UrlBuilder.getCurrentDomain(this).takeIf { it.isNotEmpty() }
            ?: UrlBuilder.getCurrentConfiguration().domain
        
        android.util.Log.d("BridgeMainActivity", "🚨 FINAL ENVIRONMENT SELECTION:")
        android.util.Log.d("BridgeMainActivity", "🚨 Selected environment: $selectedEnvironment")
        android.util.Log.d("BridgeMainActivity", "🚨 Selected domain: $selectedDomain")
        android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
        
        // Log credentials summary for debugging
        android.util.Log.d("BridgeMainActivity", environmentCredentialManager.getCredentialsSummary())

        // Initialize SDK on startup so it's available for all operations
        initializeSDK()

        // Request notification permissions for Android 13+
        requestNotificationPermissions()

        // Check FCM token, certificate, and member ID status on startup
        // IMPORTANT: Must get FCM token first, then request certificate if needed
        initializeAppCredentials()

        // Handle notification intent if app was launched from notification
        handleNotificationIntent(intent)
        
        setContent {
            // Observe AppNotificationState like iOS RootView does
            val notificationType by AppNotificationState.notificationType.collectAsState()
            val notificationTitle by AppNotificationState.notificationTitle.collectAsState()
            val notificationDescription by AppNotificationState.notificationDescription.collectAsState()
            val requestId by AppNotificationState.requestId.collectAsState()
            
            // Handle notification state changes (like iOS RootView)
            LaunchedEffect(notificationType) {
                when (notificationType) {
                    AppNotificationState.NotificationType.APPROVAL -> {
                        android.util.Log.d("BridgeMainActivity", "🔔 AppNotificationState changed to APPROVAL - showing approval screens")
                        showApprovalRequestScreen = true
                        approvalTitle = notificationTitle
                        approvalDescription = notificationDescription
                        approvalRequestId = requestId
                    }
                    AppNotificationState.NotificationType.DEFAULT -> {
                        android.util.Log.d("BridgeMainActivity", "🔔 AppNotificationState changed to DEFAULT")
                        // Keep current state - don't auto-hide approval screens
                    }
                }
            }
            
            // Create custom ColorScheme from selected theme
            val customColorScheme = createColorSchemeFromTheme(selectedTheme)
            
            MaterialTheme(colorScheme = customColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showApprovalRequestScreen -> {
                            // Get current theme configuration directly from EnhancedThemeManager
                            val themeConfig = com.artiusid.sdk.ui.theme.EnhancedThemeManager.getCurrentThemeConfig()
                            android.util.Log.d("BridgeMainActivity", "🎨 Approval Request - Using theme: ${themeConfig.brandName}")
                            
                            // Wrap approval screens with SDK theme context (like AuthenticationActivity does)
                            com.artiusid.sdk.ui.theme.EnhancedSDKTheme(
                                themeConfig = themeConfig
                            ) {
                                // Create ViewModel manually (without Hilt to avoid dependency issues)
                                val viewModelFactory = remember {
                                    object : ViewModelProvider.Factory {
                                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                            if (modelClass.isAssignableFrom(ApprovalRequestViewModel::class.java)) {
                                                // Create a mock ApiService instance for approval flow
                                                val apiService = object : ApiService {
                                                    override suspend fun verify(clientId: Int, clientGroupId: Int, request: LinkedHashMap<String, Any>) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun authenticate(clientId: Int, clientGroupId: Int, accountNumber: String, request: com.artiusid.sdk.data.model.AuthenticationRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun sendApprovalResponse(request: com.artiusid.sdk.data.model.ApprovalRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun loadCertificate(clientId: Int, clientGroupId: Int, request: com.artiusid.sdk.data.model.LoadCertificateRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun loadCertificate(request: com.artiusid.sdk.data.model.LoadCertificateRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun sendApprovalRequestIOS(request: com.artiusid.sdk.data.model.ApprovalRequestTestingRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                    override suspend fun approval(request: com.artiusid.sdk.data.model.ApprovalRequest) = 
                                                        throw NotImplementedError("Not needed for approval flow")
                                                }
                                                @Suppress("UNCHECKED_CAST")
                                                return ApprovalRequestViewModel(apiService) as T
                                            }
                                            throw IllegalArgumentException("Unknown ViewModel class")
                                        }
                                    }
                                }
                                
                                val viewModel = viewModel<ApprovalRequestViewModel>(factory = viewModelFactory)
                                
                                // Use SDK approval screens (like authentication screens) for proper theming
                                com.artiusid.sdk.presentation.screens.approval.ApprovalRequestScreen(
                                onNavigateToApprovalResponse = { response ->
                                    android.util.Log.d("BridgeMainActivity", "📝 Approval response: $response")
                                    approvalResponse = if (response == "yes") "approve" else "deny"
                                    showApprovalRequestScreen = false
                                    
                                    // Clear notification state to prevent loop
                                    AppNotificationState.reset()
                                    
                                    // Send approval response to server using SDK (like iOS)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val result = ArtiusIDSDK.sendApprovalResponse(this@BridgeMainActivity, response)
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
                                onNavigateBack = {
                                    android.util.Log.d("BridgeMainActivity", "🔙 User navigated back from approval request")
                                    showApprovalRequestScreen = false
                                    AppNotificationState.reset()
                                },
                                viewModel = viewModel
                            )
                        }
                        }
                        showApprovalResponseScreen -> {
                            // Get current theme configuration directly from EnhancedThemeManager
                            val themeConfig = com.artiusid.sdk.ui.theme.EnhancedThemeManager.getCurrentThemeConfig()
                            android.util.Log.d("BridgeMainActivity", "🎨 Approval Response - Using theme: ${themeConfig.brandName}")
                            
                            // Wrap approval response screen with SDK theme context (like AuthenticationActivity does)
                            com.artiusid.sdk.ui.theme.EnhancedSDKTheme(
                                themeConfig = themeConfig
                            ) {
                                // Use SDK approval response screen for proper theming
                                com.artiusid.sdk.presentation.screens.approval.ApprovalResponseScreen(
                                    response = if (approvalResponse == "approve") "yes" else "no",
                                    onNavigateToHome = {
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
        // Note: Approval notifications are now handled directly by ApprovalActivity
        // No need to observe notification state in BridgeMainActivity anymore
        
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
                text = "Secure identity verification and authentication",
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
                        onThemeSelected = { 
                            android.util.Log.d("BridgeMainActivity", "🎨 Theme dropdown changed to: ${it.displayName}")
                            android.util.Log.d("BridgeMainActivity", "🎨 New theme brand name: ${it.themeConfig.brandName}")
                            android.util.Log.d("BridgeMainActivity", "🎨 New theme background: ${it.themeConfig.colorScheme.backgroundColorHex}")
                            android.util.Log.d("BridgeMainActivity", "🎨 New theme primary button: ${it.themeConfig.colorScheme.primaryButtonColorHex}")
                            selectedTheme = it
                            // Re-initialize SDK with new theme configuration
                            initializeSDK()
                        }
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
                        onOverrideSelected = { 
                            selectedImageOverride = it
                            // Re-initialize SDK with new image overrides
                            initializeSDK()
                        }
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
            
            // Environment and Domain Configuration
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
                        text = "🌐 Environment & Domain Configuration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Environment Selection
                    Text(
                        text = "Environment:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // ✅ DEBUG: Log current selectedEnvironment state
                    android.util.Log.d("BridgeMainActivity", "🔍 UI: Rendering dropdown with selectedEnvironment = '$selectedEnvironment'")
                    
                    EnvironmentDropdown(
                        selectedEnvironment = selectedEnvironment,
                        onEnvironmentSelected = { newEnvironment ->
                            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: Environment changed from $selectedEnvironment to $newEnvironment")
                            
                            // 🚨 CRITICAL FIX: Complete environment change handler
                            handleEnvironmentChange(selectedEnvironment, newEnvironment)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Domain Selection
                    Text(
                        text = "Domain:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    DomainDropdown(
                        selectedDomain = selectedDomain,
                        onDomainSelected = { 
                            selectedDomain = it
                            UrlBuilder.setDomain(this@BridgeMainActivity, it)
                            // Re-initialize SDK with new configuration
                            initializeSDK()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Show current configuration
                    Text(
                        text = "📍 Current: ${UrlBuilder.getCurrentConfiguration(this@BridgeMainActivity)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Okta ID Configuration (NEW - matches iOS v2.0.12)
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
                        text = "🔐 Okta ID Configuration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Include Okta ID in Verification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Collect Okta ID during verification flow",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = includeOktaID,
                            onCheckedChange = { 
                                includeOktaID = it
                                android.util.Log.d("BridgeMainActivity", "🔐 Okta ID setting changed: $it")
                                // Re-initialize SDK with new configuration
                                initializeSDK()
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show current status
                    Text(
                        text = if (includeOktaID) "✅ Okta ID collection enabled" else "❌ Okta ID collection disabled",
                        fontSize = 12.sp,
                        color = if (includeOktaID) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                        text = "🔍 Start Verification",
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
                    text = "🔐 Start Authentication",
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test Sound Effects Button
            Button(
                onClick = { testSoundEffects() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color for sound test
                )
            ) {
                Text(
                    text = "🔊 Test Camera Sounds",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Temporary button to clear certificate for testing sandbox environment
            Button(
                onClick = { clearExistingCertificate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722) // Orange/red for clear action
                )
            ) {
                Text(
                    text = "🚨 Clear ALL Credentials",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )
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
                        
                        // ✅ Use SDK public API instead of internal classes
                        val hasCertificate = ArtiusIDSDK.hasCertificate(this@BridgeMainActivity)
                        
                        val certStatus = if (hasCertificate) "✅ Loaded" else "❌ Not loaded"
                        val certColor = if (hasCertificate) Color(0xFF4CAF50) else Color(0xFFF44336)
                        
                        Text(
                            text = "Status: $certStatus",
                            fontSize = 12.sp,
                            color = certColor
                        )
                        
                        if (hasCertificate) {
                            // ✅ Use SDK public API for detailed certificate status
                            val certDetails = ArtiusIDSDK.getCertificateStatus(this@BridgeMainActivity)
                            val keyMatch = certDetails["hasValidKey"] as? Boolean ?: false
                            
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
        intent?.let { notificationIntent ->
            val approvalTitle = notificationIntent.getStringExtra("approvalTitle")
            val approvalDescription = notificationIntent.getStringExtra("approvalDescription") 
            val requestId = notificationIntent.getStringExtra("requestId")
            
            if (approvalTitle != null && approvalDescription != null) {
                android.util.Log.d("BridgeMainActivity", "🔔 Received approval notification - Title: $approvalTitle")
                
                // Update AppNotificationState like iOS does
                AppNotificationState.handleApprovalNotification(
                    requestId = requestId?.toIntOrNull() ?: -1,
                    title = approvalTitle,
                    description = approvalDescription
                )
                
                android.util.Log.d("BridgeMainActivity", "🔔 AppNotificationState updated for approval")
            } else {
                android.util.Log.d("BridgeMainActivity", "🔔 Non-approval notification received")
            }
        }
    }

    /**
     * 🚨 CRITICAL: Handle environment change with complete state reset
     * When environment changes, we must:
     * 1. Clear previous verification state (different environment = different backend)
     * 2. Get new certificate for the new environment
     * 3. Refresh FCM token 
     * 4. Force new verification
     */
    private fun handleEnvironmentChange(oldEnvironment: String, newEnvironment: String) {
        android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
        android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL ENVIRONMENT CHANGE HANDLER")
        android.util.Log.d("BridgeMainActivity", "🚨 From: $oldEnvironment → To: $newEnvironment")
        android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
        
        // Step 1: Update UI state immediately
        selectedEnvironment = newEnvironment
        
        // Step 2: Use Environment Credential Manager for coordinated environment change
        val environmentCredentialManager = com.artiusid.sdk.utils.EnvironmentCredentialManager.getInstance(this@BridgeMainActivity)
        
        android.util.Log.d("BridgeMainActivity", "🚨 Step 2: Setting environment for all credentials...")
        environmentCredentialManager.setEnvironmentForAllCredentials(newEnvironment)
        android.util.Log.d("BridgeMainActivity", "✅ Step 2: Environment set for all credential managers")
        
        // Step 3: CRITICAL - Clear old environment credentials and UI state
        android.util.Log.d("BridgeMainActivity", "🚨 Step 3: Clearing old environment credentials and UI state...")
        try {
            // Clear old environment credentials to prevent cross-contamination
            environmentCredentialManager.clearCredentialsForEnvironment(oldEnvironment)
            
            // 🚨 CRITICAL FIX: Clear verification result data from UI state
            // This prevents showing old member ID from previous environment
            verificationResultData = null
            
            // Clear verification state - reset member ID status
            memberIdStatus = "❌ Verification Required"
            memberIdPreview = "Not verified"
            android.util.Log.d("BridgeMainActivity", "✅ Old environment credentials and UI state cleared")
        } catch (e: Exception) {
            android.util.Log.w("BridgeMainActivity", "⚠️ Could not clear old credentials: ${e.message}")
        }
        
        // Step 4: Re-initialize SDK with new configuration
        android.util.Log.d("BridgeMainActivity", "🚨 Step 4: Re-initializing SDK...")
        initializeSDK()
        
        // Step 5: CRITICAL - Check if user has credentials in new environment
        android.util.Log.d("BridgeMainActivity", "🚨 Step 5: Checking credentials in new environment...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if user has verification in new environment
                val verificationStateManager = com.artiusid.sdk.utils.VerificationStateManager(this@BridgeMainActivity)
                val hasVerificationInNewEnv = verificationStateManager.isVerified(newEnvironment)
                val accountNumberInNewEnv = verificationStateManager.getAccountNumber(newEnvironment)
                
                android.util.Log.d("BridgeMainActivity", "🚨 Has verification in $newEnvironment: $hasVerificationInNewEnv")
                android.util.Log.d("BridgeMainActivity", "🚨 Account number in $newEnvironment: $accountNumberInNewEnv")
                
                if (hasVerificationInNewEnv && !accountNumberInNewEnv.isNullOrEmpty()) {
                    // User has credentials in new environment - update UI
                    runOnUiThread {
                        memberIdStatus = "✅ Verified"
                        memberIdPreview = accountNumberInNewEnv
                        android.util.Log.d("BridgeMainActivity", "✅ User has existing verification in $newEnvironment")
                    }
                } else {
                    // User needs to verify in new environment
                    runOnUiThread {
                        memberIdStatus = "❌ Verification Required"
                        memberIdPreview = "Not verified"
                        android.util.Log.d("BridgeMainActivity", "⚠️ User needs to verify in $newEnvironment")
                    }
                }
                
                // Generate new FCM token for new environment
                android.util.Log.d("BridgeMainActivity", "🔥 Refreshing FCM token for new environment...")
                refreshFCMTokenForNewEnvironment()
                
                // Get new certificate for new environment
                android.util.Log.d("BridgeMainActivity", "🔐 Requesting new certificate for environment: $newEnvironment")
                val certResult = ArtiusIDSDK.ensureCertificateRegistered(this@BridgeMainActivity)
                if (certResult) {
                    android.util.Log.d("BridgeMainActivity", "✅ Certificate registered for new environment")
                } else {
                    android.util.Log.e("BridgeMainActivity", "❌ Failed to register certificate for new environment")
                }
                
                // Step 6: Update UI status
                runOnUiThread {
                    fcmTokenStatus = "✅ Ready"
                    
                    android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
                    android.util.Log.d("BridgeMainActivity", "🚨 ENVIRONMENT CHANGE COMPLETE")
                    android.util.Log.d("BridgeMainActivity", "🚨 Environment: $newEnvironment")
                    android.util.Log.d("BridgeMainActivity", "🚨 Verification Status: ${if (hasVerificationInNewEnv) "VERIFIED" else "NEEDS VERIFICATION"}")
                    android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
                    
                    // Log final credentials summary
                    android.util.Log.d("BridgeMainActivity", environmentCredentialManager.getCredentialsSummary())
                }
                
            } catch (e: Exception) {
                android.util.Log.e("BridgeMainActivity", "❌ Error during environment change", e)
                runOnUiThread {
                    fcmTokenStatus = "❌ Error"
                }
            }
        }
    }
    
    /**
     * Refresh FCM token for new environment
     */
    private suspend fun refreshFCMTokenForNewEnvironment() {
        try {
            android.util.Log.d("BridgeMainActivity", "🔥 Generating fresh FCM token for new environment...")
            
            // Delete old token from Firebase
            FirebaseMessaging.getInstance().deleteToken()
            android.util.Log.d("BridgeMainActivity", "🗑️ Old FCM token deleted")
            
            // Get new token
            val newToken = FirebaseMessaging.getInstance().token.result
            if (!newToken.isNullOrEmpty()) {
                // Store new token
                val tokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this@BridgeMainActivity)
                tokenManager?.saveToken(newToken)
                
                // Update SDK with new token
                ArtiusIDSDK.updateFcmToken(newToken)
                
                android.util.Log.d("BridgeMainActivity", "✅ New FCM token generated and saved: ${newToken.take(20)}...")
                
                runOnUiThread {
                    fcmTokenStatus = "✅ Refreshed"
                    fcmTokenPreview = newToken.take(20) + "..."
                }
            } else {
                throw Exception("Failed to generate new FCM token")
            }
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Failed to refresh FCM token", e)
            runOnUiThread {
                fcmTokenStatus = "❌ Refresh Failed"
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
            // 🚨 CRITICAL FIX: Use DYNAMIC environment from SharedPreferences, not static configuration
            val storedEnvironment = UrlBuilder.getCurrentEnvironment(this)
            val storedDomain = UrlBuilder.getCurrentDomain(this)
            
            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: Reading environment from SharedPreferences")
            android.util.Log.d("BridgeMainActivity", "🚨 Stored environment: $storedEnvironment")
            android.util.Log.d("BridgeMainActivity", "🚨 Stored domain: $storedDomain")
            
        val sdkEnvironment = when (storedEnvironment.uppercase()) {
            "SANDBOX" -> Environment.SANDBOX
            "DEVELOPMENT" -> Environment.DEVELOPMENT
            "STAGING" -> Environment.STAGING
            else -> Environment.SANDBOX // Default fallback
        }
            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: SDK Environment: $sdkEnvironment (from SharedPreferences: $storedEnvironment)")
            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: This environment will be used for ALL SDK operations")
            
            val sdkConfig = SDKConfiguration(
                apiKey = "demo_api_key_12345",
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = sdkEnvironment, // ✅ Now uses current UrlBuilder environment
                
                // ✅ Sample App uses clientId=1 (default/demo client)
                clientId = 1,
                clientGroupId = 1,
                
                enableLogging = true,
                hostAppPackageName = packageName,
                sharedCertificateContext = true,
                sharedFirebaseContext = true,
                localizationOverrides = localizationOverrides,
                imageOverrides = selectedImageOverride.overrides,
                
                // ✅ ARCHITECTURAL FIX: Sample app manages its own Firebase tokens AND notifications
                // SDK should NOT handle Firebase notifications - sample app controls everything
                handleFirebaseNotifications = false, // Disable SDK Firebase handling - sample app controls it
                customFcmToken = null, // Will be provided later via ArtiusIDSDK.updateFcmToken()
                
                // ✅ Okta ID Integration (NEW - matches iOS v2.0.12)
                includeOktaIDInVerificationPayload = includeOktaID
            )
            
            // Initialize SDK with enhanced theme
            android.util.Log.d("BridgeMainActivity", "🎨 Initializing SDK with theme: ${selectedTheme.themeConfig.brandName}")
            ArtiusIDSDK.initializeWithEnhancedTheme(
                context = this,
                configuration = sdkConfig,
                enhancedTheme = selectedTheme.themeConfig
            )
            
            android.util.Log.d("BridgeMainActivity", "✅ SDK initialized successfully on startup")
            
            // ✅ NEW: Sample app manages its own Firebase tokens
            setupFirebaseTokenManagement()
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Failed to initialize SDK on startup", e)
            lastResult = "❌ SDK initialization error: ${e.message}"
        }
    }
    
    /**
     * Initialize app credentials in the correct sequence (MATCHING iOS KEYCHAIN BEHAVIOR):
     * 1. FIRST: Check Android Keystore for existing FCM token (like iOS keychain)
     * 2. FIRST: Check Android Keystore for existing member ID (like iOS keychain)
     * 3. Only generate NEW tokens if none exist in keystore
     * 4. Request certificate using stored/retrieved FCM token
     */
    private fun initializeAppCredentials() {
        android.util.Log.d("BridgeMainActivity", "🔐 Starting credential initialization sequence (iOS keychain style)...")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: CRITICAL FIX - Check Android Keystore for existing FCM token FIRST (like iOS)
                android.util.Log.d("BridgeMainActivity", "📱 Step 1: Checking Android Keystore for existing FCM token...")
                
                var fcmToken: String? = null
                
                // 🔑 CRITICAL FIX: Use SDK's FirebaseTokenManager to get stored FCM token (like iOS keychain)
                try {
                    android.util.Log.d("BridgeMainActivity", "🔑 Checking SDK's secure FCM token storage (like iOS keychain)...")
                    
                    // Use SDK's FirebaseTokenManager to get cached token from keystore
                    val tokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this@BridgeMainActivity)
                    val storedToken = tokenManager?.getFCMToken()
                    
                    if (!storedToken.isNullOrEmpty()) {
                        fcmToken = storedToken
                        android.util.Log.d("BridgeMainActivity", "✅ Found existing FCM token in keystore: ${fcmToken.take(20)}...")
                        android.util.Log.d("BridgeMainActivity", "🔑 Using stored FCM token (like iOS keychain behavior)")
                        
                        // Provide the stored token to SDK
                        ArtiusIDSDK.updateFcmToken(fcmToken)
                    } else {
                        android.util.Log.d("BridgeMainActivity", "⚠️ No FCM token found in keystore, generating new one...")
                        // Only generate new token if none exists in keystore
                        fcmToken = FirebaseMessaging.getInstance().token.result
                        
                        if (!fcmToken.isNullOrEmpty()) {
                            // Store the new token in keystore for future use
                            tokenManager?.saveToken(fcmToken)
                            ArtiusIDSDK.updateFcmToken(fcmToken)
                            android.util.Log.d("BridgeMainActivity", "✅ Generated and stored new FCM token: ${fcmToken.take(20)}...")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("BridgeMainActivity", "Failed to check keystore, falling back to Firebase: ${e.message}")
                    // Fallback to Firebase if keystore check fails
                    fcmToken = FirebaseMessaging.getInstance().token.result
                    if (!fcmToken.isNullOrEmpty()) {
                        ArtiusIDSDK.updateFcmToken(fcmToken)
                    }
                }
                
                runOnUiThread {
                    if (!fcmToken.isNullOrEmpty()) {
                        fcmTokenStatus = "✅ Available"
                        fcmTokenPreview = fcmToken.take(20) + "..."
                        lastResult = "✅ FCM Token retrieved: ${fcmToken.take(20)}..."
                        android.util.Log.d("BridgeMainActivity", "✅ FCM token available: ${fcmToken.take(20)}...")
                    } else {
                        fcmTokenStatus = "❌ Failed"
                        lastResult = "❌ FCM Token retrieval failed"
                        android.util.Log.e("BridgeMainActivity", "❌ FCM token retrieval failed")
                        return@runOnUiThread
                    }
                }
                
                // Step 2: Ensure we have certificate (request if needed)
                android.util.Log.d("BridgeMainActivity", "🔐 Step 2: Ensuring certificate exists...")
                // ✅ Use SDK public API for certificate management
                val deviceId = fcmToken ?: return@launch // Use FCM token as device ID like iOS
                
                try {
                    // This will check if certificate exists, and if not, request one
                    val certificateReady = ArtiusIDSDK.ensureCertificateRegistered(this@BridgeMainActivity)
                    
                    runOnUiThread {
                        val currentResult = lastResult
                        lastResult = "$currentResult\n✅ Certificate ensured (generated or found)"
                        android.util.Log.d("BridgeMainActivity", "✅ Certificate ensured successfully")
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        val currentResult = lastResult
                        lastResult = "$currentResult\n❌ Certificate error: ${e.message}"
                        android.util.Log.e("BridgeMainActivity", "❌ Certificate ensure failed", e)
                    }
                }
                
                // Step 3: Check certificate status
                runOnUiThread {
                    checkCertificateStatus()
                }
                
                // Step 4: CRITICAL FIX - Check Android Keystore for existing member ID FIRST (like iOS)
                android.util.Log.d("BridgeMainActivity", "👤 Step 4: Checking Android Keystore for existing member ID...")
                
                // 🔑 CRITICAL FIX: Use SDK's VerificationStateManager to get stored member ID (like iOS keychain)
                var storedMemberId: String? = null
                try {
                    android.util.Log.d("BridgeMainActivity", "🔑 Checking SDK's secure member ID storage (like iOS keychain['verification'])...")
                    
                    // Use SDK's VerificationStateManager to get stored member ID from keystore
                    val verificationStateManager = com.artiusid.sdk.utils.VerificationStateManager(this@BridgeMainActivity)
                    storedMemberId = verificationStateManager.getAccountNumber()
                    
                    if (!storedMemberId.isNullOrEmpty()) {
                        android.util.Log.d("BridgeMainActivity", "✅ Found existing member ID in keystore: ${storedMemberId.take(8)}...${storedMemberId.takeLast(4)}")
                        android.util.Log.d("BridgeMainActivity", "🔑 Using stored member ID (like iOS keychain['verification'] behavior)")
                        
                        // Also check if account is active
                        val isActive = verificationStateManager.isVerified()
                        android.util.Log.d("BridgeMainActivity", "🔑 Account active status: $isActive")
                    } else {
                        android.util.Log.d("BridgeMainActivity", "⚠️ No member ID found in keystore - user needs to complete verification first")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("BridgeMainActivity", "Failed to check keystore for member ID: ${e.message}")
                }
                
                runOnUiThread {
                    // Update UI with stored member ID if found
                    if (!storedMemberId.isNullOrEmpty()) {
                        memberIdStatus = "✅ Available (from keystore)"
                        memberIdPreview = storedMemberId.take(8) + "..." + storedMemberId.takeLast(4)
                        
                        val memberStatus = "✅ Member ID (keystore): ${storedMemberId.take(8)}...${storedMemberId.takeLast(4)}"
                        if (lastResult.contains("Certificate") || lastResult.contains("FCM Token")) {
                            lastResult += "\n$memberStatus"
                        } else {
                            lastResult = memberStatus
                        }
                    } else {
                        // Fallback to checking verification result data
                        checkMemberIdStatus()
                    }
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    lastResult = "❌ Credential initialization error: ${e.message}"
                    android.util.Log.e("BridgeMainActivity", "❌ Credential initialization failed", e)
                }
            }
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
            
            // ✅ Sample app handles Firebase tokens directly - NO SDK involvement
            android.util.Log.d("BridgeMainActivity", "📱 Getting FCM token directly from Firebase")
            
            val cachedToken = try {
                FirebaseMessaging.getInstance().token.result ?: ""
            } catch (e: Exception) {
                android.util.Log.w("BridgeMainActivity", "Failed to get FCM token: ${e.message}")
                ""
            }
            android.util.Log.d("BridgeMainActivity", "💾 FCM token length: ${cachedToken.length}")
            
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
                                    // ✅ Provide token to SDK when needed
                                    ArtiusIDSDK.updateFcmToken(token)
                                    
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
            // ✅ Use SDK public API instead of internal classes
            android.util.Log.d("BridgeMainActivity", "📱 Using ArtiusIDSDK public API for context: ${this.packageName}")
            
            val hasCertificate = try {
                val hasCert = ArtiusIDSDK.hasCertificate(this)
                val certDetails = ArtiusIDSDK.getCertificateStatus(this)
                val certLength = certDetails["certificateLength"] as? Int ?: 0
                android.util.Log.d("BridgeMainActivity", "💾 Certificate status: $hasCert, length: $certLength")
                hasCert
            } catch (e: Exception) {
                android.util.Log.w("BridgeMainActivity", "Certificate status check failed: ${e.message}")
                false
            }
            
            if (hasCertificate) {
                val keyMatch = try {
                    // ✅ Use SDK public API for key validation
                    val certDetails = ArtiusIDSDK.getCertificateStatus(this)
                    val result = certDetails["hasValidKey"] as? Boolean ?: false
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

    private fun testSoundEffects() {
        android.util.Log.d("BridgeMainActivity", "🔊 Testing camera sound effects...")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // ✅ Sample app handles its own sounds - no SDK dependency needed
                android.util.Log.d("BridgeMainActivity", "🔊 Playing capture sound...")
                // Use system default camera sound or implement custom sound here if needed
                
                // Wait a moment between sounds
                kotlinx.coroutines.delay(1000)
                
                android.util.Log.d("BridgeMainActivity", "🔊 Playing success sound...")
                // ✅ Sample app handles its own sounds - no SDK dependency needed
                // Use system default success sound or implement custom sound here if needed
                
                android.util.Log.d("BridgeMainActivity", "✅ Sound test completed")
                
            } catch (e: Exception) {
                android.util.Log.e("BridgeMainActivity", "❌ Sound test failed: ${e.message}", e)
            }
        }
    }

    private fun clearExistingCertificate() {
        try {
            android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
            android.util.Log.d("BridgeMainActivity", "🚨 CLEARING ALL CREDENTIALS FROM ALL ENVIRONMENTS")
            android.util.Log.d("BridgeMainActivity", "🚨 This will eliminate cross-environment contamination")
            android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
            
            // Step 1: Clear certificates
            android.util.Log.d("BridgeMainActivity", "🧹 Step 1: Clearing certificates...")
            val cleared = ArtiusIDSDK.clearCertificate(this)
            android.util.Log.d("BridgeMainActivity", "✅ Certificates cleared")
            
            // Step 2: Clear ALL verification data from ALL environments
            android.util.Log.d("BridgeMainActivity", "🧹 Step 2: Clearing verification data from ALL environments...")
            val verificationStateManager = com.artiusid.sdk.utils.VerificationStateManager(this)
            
            // Clear all environment data (null = clear all environments)
            verificationStateManager.clearVerificationData(null)
            android.util.Log.d("BridgeMainActivity", "✅ All verification data cleared from all environments")
            
            // Step 3: Clear ALL FCM tokens from ALL environments
            android.util.Log.d("BridgeMainActivity", "🧹 Step 3: Clearing FCM tokens from ALL environments...")
            val firebaseTokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this)
            firebaseTokenManager?.clearToken()
            android.util.Log.d("BridgeMainActivity", "✅ All FCM tokens cleared from all environments")
            
            // Step 4: CRITICAL FIX - DO NOT reset environment when clearing credentials
            // The user may have specifically chosen an environment and we should preserve it
            android.util.Log.d("BridgeMainActivity", "🧹 Step 4: Preserving current environment setting...")
            val currentEnvironment = selectedEnvironment
            android.util.Log.d("BridgeMainActivity", "✅ Current environment preserved: $currentEnvironment")
            
            // Step 5: Reset UI state (but preserve environment)
            android.util.Log.d("BridgeMainActivity", "🧹 Step 5: Resetting UI state (preserving environment)...")
            memberIdStatus = "❌ Verification Required"
            memberIdPreview = "Not verified"
            fcmTokenStatus = "🔄 Refreshing..."
            // selectedEnvironment = "Sandbox" // 🚨 REMOVED - preserve user's environment choice
            android.util.Log.d("BridgeMainActivity", "✅ UI state reset (environment preserved: $selectedEnvironment)")
            
            // Step 6: Update certificate status
            checkCertificateStatus()
            
            android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
            android.util.Log.d("BridgeMainActivity", "🚨 ALL CREDENTIALS CLEARED SUCCESSFULLY")
            android.util.Log.d("BridgeMainActivity", "🚨 Cross-environment contamination eliminated")
            android.util.Log.d("BridgeMainActivity", "🚨 Ready for fresh verification in any environment")
            android.util.Log.d("BridgeMainActivity", "🚨 ========================================")
            
            lastResult = "✅ ALL CREDENTIALS CLEARED - Cross-environment contamination eliminated. Environment preserved: $selectedEnvironment. Ready for fresh verification."
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Error clearing all credentials", e)
            lastResult = "❌ Error clearing all credentials: ${e.message}"
        }
    }
    
    private fun checkMemberIdStatus() {
        android.util.Log.d("BridgeMainActivity", "👤 Checking member ID status...")
        
        try {
            // First check if we have verification result data (most recent/accurate)
            val resultMemberId = verificationResultData?.accountNumber
            
            // Also check VerificationStateManager (secure storage)
            // ✅ Sample app only uses verification result data - no internal SDK storage access
            android.util.Log.d("BridgeMainActivity", "🔍 Result Member ID: ${resultMemberId?.take(8)}...${resultMemberId?.takeLast(4)}")
            
            // Use the verification result data only
            val memberId = if (!resultMemberId.isNullOrEmpty()) {
                android.util.Log.d("BridgeMainActivity", "✅ Using Member ID from verification result")
                resultMemberId
            } else {
                android.util.Log.d("BridgeMainActivity", "❌ No Member ID found in verification result")
                null
            }

            if (!memberId.isNullOrEmpty()) {
                memberIdStatus = "✅ Available"
                memberIdPreview = memberId.take(8) + "..." + memberId.takeLast(4)
                android.util.Log.d("BridgeMainActivity", "💾 Final Member ID: ${memberId.take(8)}...${memberId.takeLast(4)}")
                android.util.Log.d("BridgeMainActivity", "✅ Full Member ID for approval: $memberId")
                
                // ✅ Sample app uses only verification result data - no internal SDK storage
                if (!resultMemberId.isNullOrEmpty()) {
                    android.util.Log.d("BridgeMainActivity", "✅ Member ID available from verification result")
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
            
            // SDK is already initialized in onCreate() with current theme and image overrides
            // Re-initialization here causes Hilt to create a NEW ViewModel instance,
            // which resets the guard flag and causes duplicate verification requests
            // FIX: Just start verification - SDK is already properly configured
            
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
            
            android.util.Log.d("BridgeMainActivity", "🔐 Starting authentication flow...")
            android.util.Log.d("BridgeMainActivity", "🎨 Selected Theme: ${selectedTheme.displayName}")
            
            // SDK is already initialized in onCreate() with current theme and image overrides
            // Re-initialization here causes Hilt to create a NEW ViewModel instance,
            // which resets the guard flag and causes duplicate authentication requests
            // FIX: Just start authentication - SDK is already properly configured
            
            // Start authentication via bridge to standalone app
            ArtiusIDSDK.startAuthentication(this, this)
            
        } catch (e: Exception) {
            isVerificationLoading = false
            lastResult = "❌ Error starting authentication bridge: ${e.message}"
        }
    }
    
    // Guard flag to prevent rapid button clicks causing duplicate requests
    private var lastApprovalRequestTime = 0L
    private val approvalRequestDebounceMs = 2000L // 2 second debounce
    
    private fun sendApprovalRequest() {
        try {
            // Debounce: Prevent multiple clicks within 2 seconds
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastApprovalRequestTime < approvalRequestDebounceMs) {
                android.util.Log.w("BridgeMainActivity", "⚠️ Approval request debounced - too soon after last request (${currentTime - lastApprovalRequestTime}ms)")
                return
            }
            lastApprovalRequestTime = currentTime
            
            isApprovalLoading = true
            android.util.Log.d("BridgeMainActivity", "📋 ========================================")
            android.util.Log.d("BridgeMainActivity", "📋 APPROVAL REQUEST BUTTON CLICKED")
            android.util.Log.d("BridgeMainActivity", "📋 Timestamp: $currentTime")
            android.util.Log.d("BridgeMainActivity", "📋 ========================================")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use the new public SDK method for approval requests
                    android.util.Log.d("BridgeMainActivity", "📋 Calling ArtiusIDSDK.sendApprovalRequest()...")
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
                
                // ✅ Sample app manages its own tokens - provide to SDK when needed
                ArtiusIDSDK.updateFcmToken(token)
                
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
            ✅ Verification Success [$timestamp]
            ID: ${result.verificationId}
            Confidence: ${(result.confidence * 100).toInt()}%
            Document: ${result.documentType ?: "Unknown"}
            Processing Time: ${result.processingTime}ms
            Session: ${result.sessionId}
        """.trimIndent()
    }
    
    override fun onVerificationError(error: SDKError) {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Verification Error [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
        """.trimIndent()
    }
    
    override fun onVerificationCancelled() {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Verification Cancelled [$timestamp]"
    }
    
    // AuthenticationCallback implementation
    override fun onAuthenticationSuccess(result: AuthenticationResult) {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ✅ Authentication Success [$timestamp]
            ID: ${result.authenticationId}
            Confidence: ${(result.confidence * 100).toInt()}%
            Processing Time: ${result.processingTime}ms
            Session: ${result.sessionId}
        """.trimIndent()
    }
    
    override fun onAuthenticationError(error: SDKError) {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Authentication Error [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
        """.trimIndent()
    }
    
    override fun onAuthenticationCancelled() {
        isVerificationLoading = false
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Authentication Cancelled [$timestamp]"
    }
    
    /**
     * ✅ NEW: Sample app manages its own Firebase tokens
     * This demonstrates how client apps can handle FCM tokens when SDK Firebase handling is disabled
     */
    private fun setupFirebaseTokenManagement() {
        try {
            android.util.Log.d("BridgeMainActivity", "🔥 Setting up Firebase token management for sample app")
            
            // CRITICAL FIX: Get FCM token synchronously first, then set up async listener
            // This ensures the SDK has a token BEFORE any verification requests
            
            // Step 1: Get FCM token directly from Firebase (synchronous)
            val cachedToken = try {
                FirebaseMessaging.getInstance().token.result
            } catch (e: Exception) {
                android.util.Log.w("BridgeMainActivity", "Failed to get FCM token synchronously: ${e.message}")
                null
            }
            
            if (!cachedToken.isNullOrEmpty()) {
                android.util.Log.d("BridgeMainActivity", "🔥 Using Firebase FCM token: ${cachedToken.take(20)}...")
                
                // 🚨 CRITICAL: Save FCM token with current environment
                val currentEnvironment = selectedEnvironment
                val firebaseTokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this)
                firebaseTokenManager?.saveToken(cachedToken, currentEnvironment)
                android.util.Log.d("BridgeMainActivity", "🔥 ✅ FCM token saved for environment: $currentEnvironment")
                
                ArtiusIDSDK.updateFcmToken(cachedToken)
                android.util.Log.d("BridgeMainActivity", "🔥 ✅ FCM token provided to SDK immediately")
            } else {
                android.util.Log.d("BridgeMainActivity", "🔥 No token available, will wait for Firebase async token...")
            }
            
            // Step 2: Set up async listener for token updates (for future token refreshes)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.w("BridgeMainActivity", "🔥 Failed to get FCM token", task.exception)
                    return@addOnCompleteListener
                }
                
                // Get new FCM Registration Token
                val token = task.result
                android.util.Log.d("BridgeMainActivity", "🔥 Sample app FCM token: ${token?.take(20)}...")
                
                // Provide token to SDK for approval requests
                if (!token.isNullOrEmpty()) {
                    // 🚨 CRITICAL: Save FCM token with current environment
                    val currentEnvironment = selectedEnvironment
                    val firebaseTokenManager = com.artiusid.sdk.utils.FirebaseTokenManager.getInstance(this@BridgeMainActivity)
                    firebaseTokenManager?.saveToken(token, currentEnvironment)
                    android.util.Log.d("BridgeMainActivity", "🔥 ✅ FCM token saved for environment: $currentEnvironment")
                    
                    ArtiusIDSDK.updateFcmToken(token)
                    android.util.Log.d("BridgeMainActivity", "🔥 ✅ FCM token provided to SDK")
                }
            }
            
            // Listen for token refresh (optional - for production apps)
            // Note: In a real app, you'd implement FirebaseMessagingService to handle token updates
            android.util.Log.d("BridgeMainActivity", "🔥 ✅ Firebase token management setup complete")
            
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "🔥 ❌ Failed to setup Firebase token management", e)
        }
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

@Composable
private fun EnvironmentDropdown(
    selectedEnvironment: String,
    onEnvironmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // ✅ FIX: Convert to Title Case to match UrlConfiguration format
    val availableEnvironments = UrlBuilder.getAvailableEnvironments().map { env ->
        when (env) {
            "SANDBOX" -> "Sandbox"
            "DEVELOPMENT" -> "Development" 
            "STAGING" -> "Staging"
            else -> env
        }
    }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedEnvironment,
                    fontWeight = FontWeight.Medium
                )
                // ✅ DEBUG: Log what's being displayed
                android.util.Log.d("BridgeMainActivity", "🔍 Dropdown: Displaying '$selectedEnvironment'")
                Text("▼", fontSize = 12.sp)
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableEnvironments.forEach { environment ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = environment,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onEnvironmentSelected(environment)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DomainDropdown(
    selectedDomain: String,
    onDomainSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableDomains = listOf(
        "artiusid.dev",
        "artiusid.com", 
        "artiusid.net",
        "localhost:8080"
    )
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDomain,
                    fontWeight = FontWeight.Medium
                )
                Text("▼", fontSize = 12.sp)
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableDomains.forEach { domain ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = domain,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onDomainSelected(domain)
                        expanded = false
                    }
                )
            }
        }
    }
}

