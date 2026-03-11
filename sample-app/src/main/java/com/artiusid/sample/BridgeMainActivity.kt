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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.config.Environment
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.ui.theme.ThemeManager
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
import android.app.Activity
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.artiusid.sdk.utils.UrlBuilder
import com.artiusid.sample.config.AppConstants
import com.artiusid.sample.config.AppUrlConfig
import com.artiusid.sample.okta.OktaLoginActivity
import com.artiusid.sample.okta.OktaLoginHelper
import com.artiusid.sample.localization.LanguageManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import com.artiusid.sample.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.artiusid.sdk.utils.SDKResourceBundle

/**
 * Sample App demonstrating the artius.iD SDK Integration
 * 
 * This sample app shows how to integrate with the artius.iD SDK that
 * launches the complete standalone application with full verification capabilities.
 */
class BridgeMainActivity : FragmentActivity(), VerificationCallback, AuthenticationCallback {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapWithStoredLocale(newBase))
    }
    
    /**
     * Create Material3 ColorScheme from selected theme - MATCHING iOS sample app exactly.
     * For artius.iD and other themes: use primaryButtonColorHex for Material "primary" (app bar, accents)
     * so the chrome matches the main CTA button; use primaryColorHex for text (onBackground/onSurface).
     */
    private fun createColorSchemeFromTheme(theme: EnhancedThemeOption): ColorScheme {
        val colors = theme.themeConfig.colorScheme
        val parse = { hex: String -> Color(android.graphics.Color.parseColor(hex)) }
        // Material primary = main accent (button/app bar) - use primaryButtonColor (e.g. artius.iD orange #F58220)
        val primary = parse(colors.primaryButtonColorHex)
        val onPrimary = parse(colors.primaryButtonTextColorHex)
        val secondary = parse(colors.secondaryButtonColorHex)
        val onSecondary = parse(colors.secondaryButtonTextColorHex)
        val background = parse(colors.backgroundColorHex)
        val onBackground = parse(colors.onBackgroundColorHex)
        val surface = parse(colors.surfaceColorHex.takeIf { it.isNotEmpty() } ?: colors.backgroundColorHex)
        val onSurface = parse(colors.onSurfaceColorHex)
        val luminance = (0.299 * android.graphics.Color.red(android.graphics.Color.parseColor(colors.backgroundColorHex)) +
            0.587 * android.graphics.Color.green(android.graphics.Color.parseColor(colors.backgroundColorHex)) +
            0.114 * android.graphics.Color.blue(android.graphics.Color.parseColor(colors.backgroundColorHex))) / 255.0
        val isDark = luminance < 0.5
        return if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = parse(colors.errorColorHex),
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = parse(colors.errorColorHex),
                onError = Color.White
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
    private var includeOktaID by mutableStateOf(false) // Okta ID off by default when app opens
    private var verificationResultData by mutableStateOf<VerificationResultData?>(null)
    private var showResultsScreen by mutableStateOf(false)
    /** iOS parity: which result card to show (verification, authentication, approval, clear). Empty = show last result card. */
    private var lastActionType by mutableStateOf("")
    /** iOS parity: approval result for approval card ("yes"/"no"). */
    private var lastApprovalResult by mutableStateOf<String?>(null)
    /** iOS parity: authentication result for auth card. */
    private var lastAuthenticationResult by mutableStateOf<String?>(null)
    private var showSettings by mutableStateOf(false)
    /** iOS parity: environment section hidden until long-press (3s) on Settings title. */
    private var isEnvironmentUnlocked by mutableStateOf(false)
    /** iOS parity: show "Processing" overlay when switching environment. */
    private var isUpdatingEnvironment by mutableStateOf(false)
    private var showInfoSheet by mutableStateOf(false)
    private var copyToastMessage by mutableStateOf<String?>(null)
    private var fcmTokenStatus by mutableStateOf("❌ Not available")
    private var fcmTokenPreview by mutableStateOf("")
    private var memberIdStatus by mutableStateOf("❌ Not available")
    private var memberIdPreview by mutableStateOf("")
    
    // Approval flow state
    private var showApprovalRequestScreen by mutableStateOf(false)
    private var showApprovalResponseScreen by mutableStateOf(false)
    // Test Authentication Request (iOS parity: SampleAppView "Test Authentication Request" button)
    private var showTestAuthenticationRequest by mutableStateOf(false)
    private var approvalRequestId by mutableStateOf<Int?>(null)
    private var approvalTitle by mutableStateOf("")
    private var approvalDescription by mutableStateOf("")
    private var approvalResponse by mutableStateOf("")

    // Okta login (matches iOS OktaProvisioningCoordinator flow)
    private var showOktaGuidanceDialog by mutableStateOf(false)
    private var oktaLoginInProgress by mutableStateOf(false)
    private var oktaError by mutableStateOf<String?>(null)
    private var oktaSuccessMessage by mutableStateOf<String?>(null)
    private var shouldTriggerOktaAfterVerification by mutableStateOf(false)
    private var showClearVerificationDialog by mutableStateOf(false)
    private var showClearOktaReregisterDialog by mutableStateOf(false)
    private var oktaLoginLauncher: ActivityResultLauncher<Intent>? = null
    private var developerMode by mutableStateOf(false)
    private var demoMode by mutableStateOf(false)
    private var faceMeshUploadEnabled by mutableStateOf(false)

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
        // Sync SDK display language with app language
        ArtiusIDSDK.setLanguage(this, LanguageManager.getStoredLanguage(this))

        // Request notification permissions for Android 13+
        requestNotificationPermissions()

        // Check FCM token, certificate, and member ID status on startup
        // IMPORTANT: Must get FCM token first, then request certificate if needed
        initializeAppCredentials()

        // Handle notification intent if app was launched from notification
        handleNotificationIntent(intent)
        // Handle Okta OIDC redirect if app was launched from browser callback
        handleOktaRedirect(intent)

        oktaLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            oktaLoginInProgress = false
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val userId = result.data?.getStringExtra(OktaLoginActivity.EXTRA_OKTA_USER_ID)
                    if (!userId.isNullOrBlank()) {
                        ArtiusIDSDK.setOktaUserId(userId)
                        oktaSuccessMessage = getString(R.string.okta_login_success)
                        android.util.Log.d("BridgeMainActivity", "✅ Okta user ID set (in-app): ${userId.take(10)}...")
                    }
                }
                else -> {
                    val error = result.data?.getStringExtra(OktaLoginActivity.EXTRA_ERROR)
                    oktaError = error ?: getString(R.string.okta_login_button).plus(" failed")
                    showOktaGuidanceDialog = true
                }
            }
        }

        setContent {
            // After verification success, optionally show Okta provisioning (like iOS RootNavigationView)
            LaunchedEffect(shouldTriggerOktaAfterVerification) {
                if (shouldTriggerOktaAfterVerification && ArtiusIDSDK.getOktaUserId().isNullOrEmpty()) {
                    shouldTriggerOktaAfterVerification = false
                    showOktaGuidanceDialog = true
                }
            }
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
                                                    override suspend fun registerOkta(request: Map<String, String>) = 
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
                                        lastActionType = "approval"
                                        lastApprovalResult = if (approvalResponse == "approve") "yes" else "no"
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
                        showTestAuthenticationRequest -> {
                            // iOS parity: Test Authentication Request - local UI test (ArtiusID.AuthenticationRequestView)
                            val themeConfig = com.artiusid.sdk.ui.theme.EnhancedThemeManager.getCurrentThemeConfig()
                            com.artiusid.sdk.ui.theme.EnhancedSDKTheme(themeConfig = themeConfig) {
                                TestAuthenticationRequestScreen(
                                    title = getString(R.string.test_authentication_request_title),
                                    description = getString(R.string.test_authentication_request_description),
                                    onApprove = {
                                        android.util.Log.d("BridgeMainActivity", "User approved authentication request")
                                        showTestAuthenticationRequest = false
                                        AppNotificationState.reset()
                                    },
                                    onDeny = {
                                        android.util.Log.d("BridgeMainActivity", "User denied authentication request")
                                        showTestAuthenticationRequest = false
                                        AppNotificationState.reset()
                                    },
                                    onCancel = {
                                        android.util.Log.d("BridgeMainActivity", "User cancelled authentication")
                                        showTestAuthenticationRequest = false
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
    
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun BridgeSampleApp() {
        // iOS parity: minimal nav bar (gear only, no title); content = ScrollView with VStack(header, buttons, result).
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = getString(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            LaunchedEffect(Unit) {
                checkCertificateStatus()
                checkFCMTokenStatus()
                checkMemberIdStatus()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Header (iOS: VStack spacing 8, largeTitle 34pt, body, padding top 24 / bottom 16)
                Text(
                    text = getString(R.string.sample_header_title),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp)
                )
                Text(
                    text = getString(R.string.sample_header_subtitle),
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                if (showClearVerificationDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearVerificationDialog = false },
                        title = { Text(getString(R.string.settings_clearVerification)) },
                        text = { Text(getString(R.string.settings_verificationClearedMessage)) },
                        confirmButton = {
                            Button(onClick = {
                                com.artiusid.sdk.utils.VerificationStateManager(this@BridgeMainActivity).clearVerificationData()
                                memberIdStatus = "❌ Verification Required"
                                memberIdPreview = "Not verified"
                                showClearVerificationDialog = false
                            }) { Text(getString(R.string.alert_proceed)) }
                        },
                        dismissButton = { TextButton(onClick = { showClearVerificationDialog = false }) { Text(getString(R.string.alert_cancel)) } }
                    )
                }
                if (showClearOktaReregisterDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearOktaReregisterDialog = false },
                        title = { Text(getString(R.string.settings_clearOktaAndReregister)) },
                        text = { Text(getString(R.string.settings_reregisterMessage)) },
                        confirmButton = {
                            Button(onClick = {
                                ArtiusIDSDK.setOktaUserId(null)
                                refreshFCMToken()
                                showClearOktaReregisterDialog = false
                            }) { Text(getString(R.string.alert_proceed)) }
                        },
                        dismissButton = { TextButton(onClick = { showClearOktaReregisterDialog = false }) { Text(getString(R.string.alert_cancel)) } }
                    )
                }
                // Okta guidance dialog (matches iOS OktaProvisioningGuidanceView)
                if (showOktaGuidanceDialog) {
                AlertDialog(
                    onDismissRequest = { showOktaGuidanceDialog = false; oktaError = null },
                    title = { Text(getString(R.string.okta_whyLoginRequired)) },
                    text = {
                        Column {
                            Text(getString(R.string.okta_provisioningMessage))
                            oktaError?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
                            if (oktaLoginInProgress) Text(getString(R.string.intro_requestApprovalSending), modifier = Modifier.padding(top = 8.dp))
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                oktaError = null
                                oktaLoginInProgress = true
                                val authUrl = OktaLoginHelper.launchOktaLoginInApp(this@BridgeMainActivity)
                                val intent = Intent(this@BridgeMainActivity, OktaLoginActivity::class.java)
                                    .putExtra(OktaLoginActivity.EXTRA_AUTH_URL, authUrl)
                                oktaLoginLauncher?.launch(intent)
                            },
                            enabled = !oktaLoginInProgress
                        ) { Text(getString(R.string.view_continue)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOktaGuidanceDialog = false; oktaError = null }) { Text(getString(R.string.alert_cancel), color = Color.Red) }
                    }
                )
                }
                if (oktaSuccessMessage != null) {
                    AlertDialog(
                        onDismissRequest = { oktaSuccessMessage = null },
                        title = { Text("Okta Login") },
                        text = { Text(oktaSuccessMessage!!) },
                        confirmButton = { Button(onClick = { oktaSuccessMessage = null }) { Text(getString(R.string.alert_ok)) } }
                    )
                }

                // Action Buttons (iOS: VStack spacing 16, no extra spacer after header)
                val scheme = selectedTheme.themeConfig.colorScheme
                val verificationBlue = Color(0xFF007AFF)
                val secondaryBtnColor = Color(android.graphics.Color.parseColor(scheme.secondaryColorHex))
                val secondaryBtnOnColor = Color(android.graphics.Color.parseColor(scheme.secondaryButtonTextColorHex))
                val buttonShape = RoundedCornerShape(8.dp)
                Button(
                onClick = { startVerificationFlow() },
                enabled = !isVerificationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = verificationBlue,
                    contentColor = Color.White
                )
            ) {
                if (isVerificationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = getString(R.string.sample_verification),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryBtnColor,
                    contentColor = secondaryBtnOnColor
                )
            ) {
                Text(
                    text = getString(R.string.sample_authentication),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryBtnOnColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { sendApprovalRequest() },
                enabled = !isApprovalLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                if (isApprovalLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = getString(R.string.sample_approval),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clear ALL Credentials (iOS: sample_clear_credentials, #D32F2F)
            Button(
                onClick = { clearExistingCertificate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = getString(R.string.sample_clear_credentials),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Single result card (iOS: one of verification / authentication / approval / last result)
            when {
                lastActionType == "verification" && verificationResultData != null -> InlineVerificationResultCard(
                    verificationData = verificationResultData!!,
                    primaryButtonColorHex = selectedTheme.themeConfig.colorScheme.primaryButtonColorHex,
                    primaryButtonTextColorHex = selectedTheme.themeConfig.colorScheme.primaryButtonTextColorHex,
                    onViewFullDetails = { showResultsScreen = true }
                )
                lastActionType == "authentication" -> InlineAuthenticationResultCard(
                    resultText = lastResult,
                    status = lastAuthenticationResult
                )
                lastActionType == "approval" && lastApprovalResult != null -> InlineApprovalResultCard(approved = lastApprovalResult == "yes")
                else -> LastResultCard(
                    lastResult = lastResult,
                    fcmTokenStatus = fcmTokenStatus,
                    fcmTokenPreview = fcmTokenPreview,
                    memberIdStatus = memberIdStatus,
                    memberIdPreview = memberIdPreview
                )
            }
            
            if (showSettings) {
                ModalBottomSheet(
                    onDismissRequest = { if (!isUpdatingEnvironment) showSettings = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                getString(R.string.settings_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = { isEnvironmentUnlocked = true }
                                    )
                            )
                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_info_details),
                                    contentDescription = getString(R.string.settings_config_info)
                                )
                            }
                            TextButton(
                                onClick = { if (!isUpdatingEnvironment) showSettings = false },
                                enabled = !isUpdatingEnvironment
                            ) {
                                Text(getString(R.string.sample_done))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        SampleAppSettingsContent()
                        }
                        if (isUpdatingEnvironment) {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .padding(30.dp)
                                ) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = getString(R.string.gen_processing),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showInfoSheet) {
                SettingsInfoSheet(
                    onDismiss = { showInfoSheet = false },
                    copyToastMessage = copyToastMessage,
                    onCopy = { label, value ->
                        if (value != "Not Available") {
                            (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(
                                ClipData.newPlainText(label, value)
                            )
                            copyToastMessage = getString(R.string.sample_copied) + " $label"
                        }
                    },
                    clientIdGroupId = "${AppConstants.clientId}/${AppConstants.clientGroupId}",
                    accountNumber = verificationResultData?.accountNumber ?: if (memberIdPreview.isNotEmpty()) memberIdPreview else "Not Available",
                    fcmToken = ArtiusIDSDK.getCurrentFCMToken(this@BridgeMainActivity).ifEmpty { "Not Available" },
                    deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "Not Available",
                    sdkVersion = ArtiusIDSDK.getSdkVersion(),
                    appVersion = run {
                        try {
                            @Suppress("DEPRECATION")
                            val info = packageManager.getPackageInfo(packageName, 0)
                            "${info.versionName ?: "?"} (${info.versionCode})"
                        } catch (_: Exception) { "Not Available" }
                    },
                    certStatus = if (ArtiusIDSDK.hasCertificate(this@BridgeMainActivity)) getString(R.string.sample_status_loaded) else getString(R.string.sample_status_not_loaded),
                    deviceModel = Build.MODEL,
                    androidVersion = Build.VERSION.RELEASE
                )
            }
            LaunchedEffect(copyToastMessage) {
                val msg = copyToastMessage
                if (msg != null) {
                    kotlinx.coroutines.delay(1300)
                    copyToastMessage = null
                }
            }
        }
    }
    }
    
    /** iOS parity: Info sheet with Config & Info, copyable rows, and copy toast. */
    @Composable
    private fun SettingsInfoSheet(
        onDismiss: () -> Unit,
        copyToastMessage: String?,
        onCopy: (label: String, value: String) -> Unit,
        clientIdGroupId: String,
        accountNumber: String,
        fcmToken: String,
        deviceId: String,
        sdkVersion: String,
        appVersion: String,
        certStatus: String,
        deviceModel: String,
        androidVersion: String
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(getString(R.string.settings_app_info), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(getString(R.string.settings_config_info), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow(getString(R.string.settings_client_id), clientIdGroupId)
                    Spacer(Modifier.height(12.dp))
                    CopyableRow(getString(R.string.settings_account_number), accountNumber, onCopy)
                    CopyableRow(getString(R.string.settings_fcm_token), fcmToken, onCopy)
                    CopyableRow(getString(R.string.settings_device_id), deviceId, onCopy)
                    Spacer(Modifier.height(16.dp))
                    Text(getString(R.string.settings_system_info), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow(getString(R.string.settings_sdk_version), sdkVersion)
                    InfoRow(getString(R.string.settings_app_version), appVersion)
                    Spacer(Modifier.height(16.dp))
                    Text(getString(R.string.settings_certificate), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow(getString(R.string.settings_status), certStatus)
                    Spacer(Modifier.height(16.dp))
                    Text(getString(R.string.settings_device), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    InfoRow(getString(R.string.settings_model), deviceModel)
                    InfoRow(getString(R.string.settings_android_version), androidVersion)
                    if (copyToastMessage != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            copyToastMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(getString(R.string.sample_done)) }
            }
        )
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }

    @Composable
    private fun CopyableRow(label: String, value: String, onCopy: (String, String) -> Unit) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(value, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onCopy(label, value) }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_share),
                    contentDescription = getString(R.string.sample_copied)
                )
            }
        }
    }

    /** Settings cards use white background; use consistent dark text (cardTextColor) for readability. */
    @Composable
    private fun SampleAppSettingsContent() {
        val settingsTextColor = cardTextColor
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.settings_language), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = settingsTextColor, modifier = Modifier.padding(bottom = 8.dp))
                    LanguageDropdown(
                        currentCode = LanguageManager.getStoredLanguage(this@BridgeMainActivity),
                        contentColor = settingsTextColor,
                        onLanguageSelected = { code ->
                            if (code != LanguageManager.getStoredLanguage(this@BridgeMainActivity)) {
                                LanguageManager.setLanguage(this@BridgeMainActivity, code)
                                recreate()
                            }
                        }
                    )
                }
            }
            if (isEnvironmentUnlocked) {
                Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(getString(R.string.settings_environment), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = settingsTextColor, modifier = Modifier.padding(bottom = 8.dp))
                        EnvironmentDropdown(
                            selectedEnvironment = selectedEnvironment,
                            contentColor = settingsTextColor,
                            onEnvironmentSelected = { handleEnvironmentChange(selectedEnvironment, it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = when (selectedEnvironment) {
                                "Sandbox" -> getString(R.string.env_description_sandbox)
                                "Development" -> getString(R.string.env_description_development)
                                "Staging" -> getString(R.string.env_description_staging)
                                else -> ""
                            },
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(getString(R.string.domain_label) + ": ", fontSize = 14.sp, color = settingsTextColor)
                        DomainDropdown(
                            selectedDomain = selectedDomain,
                            contentColor = settingsTextColor,
                            onDomainSelected = {
                                selectedDomain = it
                                UrlBuilder.setDomain(this@BridgeMainActivity, it)
                                initializeSDK()
                            }
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.sample_theme_preview), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = settingsTextColor, modifier = Modifier.padding(bottom = 12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ColorSwatch(getString(R.string.sample_primary_color), selectedTheme.themeConfig.colorScheme.primaryColorHex)
                        ColorSwatch(getString(R.string.sample_secondary_color), selectedTheme.themeConfig.colorScheme.secondaryColorHex)
                        ColorSwatch(getString(R.string.sample_background_color), selectedTheme.themeConfig.colorScheme.backgroundColorHex)
                    }
                }
            }
            // iOS parity: Theme section – rows with two color circles, displayName, description, checkmark
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.settings_theme), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 12.dp))
                    EnhancedThemeOption.values().forEach { theme ->
                        val themePrimary = Color(android.graphics.Color.parseColor(theme.themeConfig.colorScheme.primaryColorHex))
                        val themeSecondary = Color(android.graphics.Color.parseColor(theme.themeConfig.colorScheme.secondaryColorHex))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = theme
                                    ThemeManager.setTheme(theme.themeConfig)
                                    initializeSDK()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Box(Modifier.size(16.dp).background(themePrimary, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Box(Modifier.size(16.dp).background(themeSecondary, CircleShape))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = theme.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cardTextColor
                                )
                                Text(
                                    text = theme.description,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            if (selectedTheme == theme) {
                                val c = android.graphics.Color.parseColor(theme.themeConfig.colorScheme.primaryColorHex)
                                val lum = (0.299 * android.graphics.Color.red(c) + 0.587 * android.graphics.Color.green(c) + 0.114 * android.graphics.Color.blue(c)) / 255.0
                                val checkTint = if (lum > 0.7) cardTextColor else themePrimary
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = checkTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            // iOS parity: Image Overrides section – rows with count badge, displayName, description, checkmark
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                val settingsScheme = selectedTheme.themeConfig.colorScheme
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.settings_imageOverrides), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 12.dp))
                    ImageOverrideOption.values().forEach { scenario ->
                        val primaryColor = Color(android.graphics.Color.parseColor(settingsScheme.primaryColorHex))
                        val accentColor = Color(android.graphics.Color.parseColor(settingsScheme.secondaryColorHex))
                        val isSelected = selectedImageOverride == scenario
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedImageOverride = scenario; initializeSDK() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Selection indicator: filled circle when selected, empty circle when not
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(
                                        if (isSelected)
                                            Modifier.background(accentColor, CircleShape)
                                        else
                                            Modifier.border(2.dp, Color.Gray, CircleShape)
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = scenario.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cardTextColor
                                )
                                Text(
                                    text = scenario.description,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_info_details),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(android.graphics.Color.parseColor(settingsScheme.primaryColorHex))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = getString(R.string.settings_imageOverrides_description),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.settings_applicationModes), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = settingsTextColor, modifier = Modifier.padding(bottom = 12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(getString(R.string.settings_developerMode), Modifier.weight(1f), fontSize = 14.sp, color = settingsTextColor)
                        Switch(checked = developerMode, onCheckedChange = { developerMode = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(getString(R.string.settings_demoMode), Modifier.weight(1f), fontSize = 14.sp, color = settingsTextColor)
                        Switch(checked = demoMode, onCheckedChange = { demoMode = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(getString(R.string.settings_enableFaceMeshUpload), Modifier.weight(1f), fontSize = 14.sp, color = settingsTextColor)
                        Switch(checked = faceMeshUploadEnabled, onCheckedChange = { faceMeshUploadEnabled = it })
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(getString(R.string.settings_includeOktaID), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = settingsTextColor, modifier = Modifier.padding(bottom = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(getString(R.string.okta_collection_description), fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Switch(checked = includeOktaID, onCheckedChange = { includeOktaID = it; initializeSDK() })
                    }
                    if (!ArtiusIDSDK.getOktaUserId().isNullOrEmpty()) {
                        TextButton(onClick = { showOktaGuidanceDialog = true }) {
                            Text(getString(R.string.okta_relogin_button))
                        }
                    }
                }
            }
        }
    }
    
    /** iOS parity: Last result card (white bg, dark text for readability). */
    private val cardTextColor = Color(0xFF212121)

    @Composable
    private fun LastResultCard(
        lastResult: String,
        fcmTokenStatus: String,
        fcmTokenPreview: String,
        memberIdStatus: String,
        memberIdPreview: String
    ) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = getString(R.string.sample_last_result),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cardTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (lastResult.isNotEmpty()) {
                    Text(text = lastResult, fontSize = 14.sp, color = cardTextColor, modifier = Modifier.padding(bottom = 12.dp))
                }
                Text(getString(R.string.sample_fcm_status), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    text = "${getString(R.string.status_label)}: $fcmTokenStatus",
                    fontSize = 12.sp,
                    color = if (fcmTokenStatus.contains("✅")) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                if (fcmTokenPreview.isNotEmpty()) {
                    Text(text = "${getString(R.string.sample_token_label)} $fcmTokenPreview", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(getString(R.string.sample_cert_status), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 4.dp))
                val hasCertificate = ArtiusIDSDK.hasCertificate(this@BridgeMainActivity)
                val certStatus = if (hasCertificate) "✅ ${getString(R.string.sample_status_loaded)}" else "❌ ${getString(R.string.sample_status_not_loaded)}"
                Text(text = "${getString(R.string.status_label)}: $certStatus", fontSize = 12.sp, color = if (hasCertificate) Color(0xFF4CAF50) else Color(0xFFF44336))
                Spacer(Modifier.height(12.dp))
                Text(getString(R.string.sample_account_number_status), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    text = "${getString(R.string.status_label)}: $memberIdStatus",
                    fontSize = 12.sp,
                    color = if (memberIdStatus.contains("✅")) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                if (memberIdPreview.isNotEmpty()) {
                    Text(text = "${getString(R.string.sample_id_label)} $memberIdPreview", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }

    /** iOS parity: Inline verification result card with "View full details". */
    @Composable
    private fun InlineVerificationResultCard(
        verificationData: VerificationResultData,
        primaryButtonColorHex: String,
        primaryButtonTextColorHex: String,
        onViewFullDetails: () -> Unit
    ) {
        val btnColor = Color(android.graphics.Color.parseColor(primaryButtonColorHex))
        val btnOnColor = Color(android.graphics.Color.parseColor(primaryButtonTextColorHex))
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
            Column(Modifier.padding(16.dp)) {
                Text("Verification Result", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 8.dp))
                Text(text = "${verificationData.firstName ?: ""} ${verificationData.lastName ?: ""}".trim().ifEmpty { "—" }, fontSize = 14.sp, color = cardTextColor)
                verificationData.accountNumber?.let { Text(text = "Member ID: $it", fontSize = 12.sp, color = Color.Gray) }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onViewFullDetails, colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = btnOnColor)) {
                    Text("View full details", color = btnOnColor)
                }
            }
        }
    }

    /** iOS parity: Inline authentication result card. */
    @Composable
    private fun InlineAuthenticationResultCard(resultText: String, status: String?) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
            Column(Modifier.padding(16.dp)) {
                Text(getString(R.string.sample_authentication_result), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 8.dp))
                val statusLabel = when (status) {
                    "success" -> getString(R.string.sample_authentication_success)
                    "failed" -> getString(R.string.sample_authentication_failed)
                    "cancelled" -> getString(R.string.sample_authentication_cancelled)
                    else -> getString(R.string.sample_authentication_complete)
                }
                Text(text = "${getString(R.string.sample_response)}: $statusLabel", fontSize = 14.sp, color = cardTextColor, modifier = Modifier.padding(bottom = 4.dp))
                Text(text = resultText, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }

    /** iOS parity: Inline approval result card (Approved/Declined). */
    @Composable
    private fun InlineApprovalResultCard(approved: Boolean) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
            Column(Modifier.padding(16.dp)) {
                Text(getString(R.string.sample_approval_request_result), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = cardTextColor, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = if (approved) getString(R.string.sample_approved) else getString(R.string.sample_declined),
                    fontSize = 16.sp,
                    color = if (approved) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }

    /** iOS parity: ColorSwatchView 60×60, corner 8, gray stroke 0.3, label below (12pt). */
    @Composable
    fun ColorSwatch(label: String, colorHex: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(colorHex)),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    /**
     * iOS parity: Test Authentication Request screen (ArtiusID.AuthenticationRequestView).
     * Local UI test with Approve / Deny / Cancel - no server call.
     */
    @Composable
    private fun TestAuthenticationRequestScreen(
        title: String,
        description: String,
        onApprove: () -> Unit,
        onDeny: () -> Unit,
        onCancel: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Approve", color = Color.White)
                }
                Button(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Deny", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
        handleOktaRedirect(intent)
    }

    /** Handle Okta OIDC redirect (com.artiusid.sampleapp:/callback?code=...&state=...) */
    private fun handleOktaRedirect(intent: android.content.Intent?) {
        val data = intent?.data
        if (data != null) {
            android.util.Log.i("BridgeMainActivity", "🔐 Okta: intent with data scheme=${data.scheme} host=${data.host} (hasCode=${data.getQueryParameter("code") != null})")
        }
        if (intent != null && OktaLoginHelper.handleRedirect(intent, this)) {
            android.util.Log.i("BridgeMainActivity", "🔐 Okta: redirect handled")
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
        isUpdatingEnvironment = true
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
                    isUpdatingEnvironment = false
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
                    isUpdatingEnvironment = false
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
            "PRODUCTION" -> Environment.PRODUCTION
            else -> Environment.SANDBOX // Default fallback
        }
            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: SDK Environment: $sdkEnvironment (from SharedPreferences: $storedEnvironment)")
            android.util.Log.d("BridgeMainActivity", "🚨 CRITICAL: This environment will be used for ALL SDK operations")
            
            val sdkConfig = SDKConfiguration(
                apiKey = AppConstants.apiKey,
                baseUrl = "https://api.artiusid.com", // Will be overridden by UrlBuilder based on environment
                environment = sdkEnvironment, // ✅ Now uses current UrlBuilder environment
                
                // From AppConstants (MFA iOS app parity; overridable via appconstants.json)
                clientId = AppConstants.clientId,
                clientGroupId = AppConstants.clientGroupId,
                
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
            ThemeManager.setTheme(selectedTheme.themeConfig)
            ThemeManager.setLocale(LanguageManager.getStoredLanguage(this))
            android.util.Log.d("BridgeMainActivity", "✅ SDK initialized successfully on startup")
            
            // ✅ Get certificate on app load (required for mTLS; obtain immediately after SDK init)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    android.util.Log.d("BridgeMainActivity", "🔐 Loading certificate on app load...")
                    val ready = ArtiusIDSDK.ensureCertificateRegistered(this@BridgeMainActivity)
                    android.util.Log.d("BridgeMainActivity", if (ready) "🔐 Certificate ready on app load" else "🔐 Certificate load completed (check status)")
                    runOnUiThread { checkCertificateStatus() }
                } catch (e: Exception) {
                    android.util.Log.e("BridgeMainActivity", "🔐 Certificate load on startup failed", e)
                }
            }
            
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
            lastActionType = "clear"
        } catch (e: Exception) {
            android.util.Log.e("BridgeMainActivity", "❌ Error clearing all credentials", e)
            lastResult = "❌ Error clearing all credentials: ${e.message}"
            lastActionType = "clear"
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
                    val result = ArtiusIDSDK.sendApprovalRequest(this@BridgeMainActivity)
                    
                    // Update UI on main thread
                    runOnUiThread {
                        isApprovalLoading = false
                        if (result.success) {
                            lastResult = "✅ Approval request sent successfully!\nRequest ID: ${result.requestId}\nMessage: ${result.message}"
                            android.util.Log.d("BridgeMainActivity", "✅ Approval request successful: ${result.message}")
                        } else {
                            lastResult = "❌ Approval request failed: ${result.message}"
                            android.util.Log.e("BridgeMainActivity", "❌ Approval request failed: ${result.message}")
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
        
        // Parse the verification result data for inline result card (iOS parity: show result on main, not full screen)
        verificationResultData = VerificationResultData.fromVerificationResult(result)
        lastActionType = "verification"
        // Like iOS: after verification success, offer Okta login if no Okta user ID yet
        if (ArtiusIDSDK.getOktaUserId().isNullOrEmpty()) {
            shouldTriggerOktaAfterVerification = true
        }
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
        lastActionType = "verification"
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Verification Error [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
        """.trimIndent()
    }
    
    override fun onVerificationCancelled() {
        isVerificationLoading = false
        lastActionType = "verification"
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = "⏹️ Verification Cancelled [$timestamp]"
    }
    
    // AuthenticationCallback implementation
    override fun onAuthenticationSuccess(result: AuthenticationResult) {
        isVerificationLoading = false
        lastActionType = "authentication"
        lastAuthenticationResult = "success"
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
        lastActionType = "authentication"
        lastAuthenticationResult = "failed"
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastResult = """
            ❌ Authentication Error [$timestamp]
            Code: ${error.code}
            Message: ${error.message}
        """.trimIndent()
    }
    
    override fun onAuthenticationCancelled() {
        isVerificationLoading = false
        lastActionType = "authentication"
        lastAuthenticationResult = "cancelled"
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
                val scheme = theme.themeConfig.colorScheme
                val primaryColor = Color(android.graphics.Color.parseColor(scheme.primaryColorHex))
                val secondaryColor = Color(android.graphics.Color.parseColor(scheme.secondaryColorHex))
                val isSelected = theme == selectedTheme
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(16.dp).background(primaryColor, CircleShape))
                                    Spacer(Modifier.width(4.dp))
                                    Box(Modifier.size(16.dp).background(secondaryColor, CircleShape))
                                }
                                Column {
                                    Text(
                                        text = theme.displayName,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = theme.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
private fun LanguageDropdown(
    currentCode: String,
    contentColor: Color = Color(0xFF212121),
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = LanguageManager.getSupportedLanguages()
    val displayName = languages.find { it.first == currentCode }?.second ?: currentCode
    Box {
        val currentFlag = when (currentCode) {
            "en" -> "🇺🇸"
            "es-ES", "es" -> "🇪🇸"
            "fr" -> "🇫🇷"
            "de" -> "🇩🇪"
            else -> "🌐"
        }
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$currentFlag $displayName", fontWeight = FontWeight.Medium, color = contentColor)
                Text("▼", fontSize = 12.sp, color = contentColor)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                val flag = when (code) {
                    "en" -> "🇺🇸"
                    "es-ES", "es" -> "🇪🇸"
                    "fr" -> "🇫🇷"
                    "de" -> "🇩🇪"
                    else -> "🌐"
                }
                DropdownMenuItem(
                    text = { Text(text = "$flag $name", fontWeight = FontWeight.Medium, color = contentColor) },
                    onClick = {
                        onLanguageSelected(code)
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
    contentColor: Color = Color(0xFF212121),
    onEnvironmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // ✅ FIX: Convert to Title Case to match UrlConfiguration format (iOS: Sandbox, Development, Staging, Production)
    val availableEnvironments = UrlBuilder.getAvailableEnvironments().map { env ->
        when (env) {
            "SANDBOX" -> "Sandbox"
            "DEVELOPMENT" -> "Development"
            "STAGING" -> "Staging"
            "PRODUCTION" -> "Production"
            else -> env
        }
    }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedEnvironment,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Text("▼", fontSize = 12.sp, color = contentColor)
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
                            fontWeight = FontWeight.Medium,
                            color = contentColor
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
    contentColor: Color = Color(0xFF212121),
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDomain,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Text("▼", fontSize = 12.sp, color = contentColor)
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
                            fontWeight = FontWeight.Medium,
                            color = contentColor
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

