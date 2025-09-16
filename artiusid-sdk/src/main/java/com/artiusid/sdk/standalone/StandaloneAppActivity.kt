package com.artiusid.sdk.standalone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.app.PendingIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.bridge.BridgeCallbackRegistry
import com.artiusid.sdk.bridge.StandaloneAppBridge
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.models.SDKThemeConfiguration
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.models.SDKErrorCode
import com.artiusid.sdk.navigation.AppNavigation
import com.artiusid.sdk.presentation.theme.ArtiusIDTheme
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

/**
 * Standalone Application Activity Wrapper
 * 
 * This activity serves as a bridge wrapper around the complete standalone ArtiusID application.
 * It receives configuration and theming from the SDK bridge, applies the theme to the standalone
 * app, and handles result communication back to the host app.
 * 
 * The complete standalone app with all ML Kit, OCR, MRZ, PDF-417, NFC functionality runs here.
 */
@AndroidEntryPoint
class StandaloneAppActivity : ComponentActivity() {
    
    private val TAG = "StandaloneAppActivity"
    
    private var flowType: String? = null
    private var sdkConfiguration: SDKConfiguration? = null
    private var themeConfiguration: SDKThemeConfiguration? = null
    
    // NFC handling
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    
    companion object {
        @JvmStatic
        var currentIsoDep: android.nfc.tech.IsoDep? = null
            private set
            
        fun setIsoDep(isoDep: android.nfc.tech.IsoDep?) {
            currentIsoDep = isoDep
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d(TAG, "🚀 Starting standalone application activity...")
        
        // Extract configuration from bridge
        extractConfigurationFromIntent()
        
        // Initialize NFC
        initializeNFC()
        
        // Launch the complete standalone application with theming
        setContent {
            StandaloneAppTheme(themeConfiguration) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Launch the COMPLETE standalone application with all functionality
                    val navController = rememberNavController()
                    
                    AppNavigation(
                        navController = navController,
                        startDestination = if (flowType == "verification") "verification_steps" else "authentication",
                        onVerificationComplete = { result -> handleVerificationSuccess(result) },
                        onAuthenticationComplete = { result -> handleAuthenticationSuccess(result) },
                        onError = { errorMessage -> handleError(errorMessage) },
                        onCancel = { handleCancel() }
                    )
                }
            }
        }
    }
    
    private fun extractConfigurationFromIntent() {
        flowType = intent.getStringExtra(StandaloneAppBridge.EXTRA_FLOW_TYPE)
        sdkConfiguration = intent.getParcelableExtra(StandaloneAppBridge.EXTRA_SDK_CONFIG)
        themeConfiguration = intent.getParcelableExtra(StandaloneAppBridge.EXTRA_THEME_CONFIG)
        
        android.util.Log.d(TAG, "📋 Configuration extracted:")
        android.util.Log.d(TAG, "   Flow Type: $flowType")
        android.util.Log.d(TAG, "   Theme: ${themeConfiguration?.brandName}")
        android.util.Log.d(TAG, "   Environment: ${sdkConfiguration?.environment}")
    }
    
    private fun handleVerificationSuccess(standaloneResult: Any) {
        android.util.Log.d(TAG, "✅ Standalone verification completed successfully")
        android.util.Log.d(TAG, "📄 Standalone result type: ${standaloneResult::class.simpleName}")
        android.util.Log.d(TAG, "📄 Standalone result data: $standaloneResult")
        
        // Extract raw JSON response for detailed results parsing
        val rawResponse = when (standaloneResult) {
            is String -> standaloneResult // JSON string
            is com.artiusid.sdk.data.model.VerificationResultData -> {
                // Convert to JSON if it's the data class
                try {
                    org.json.JSONObject().apply {
                        put("accountNumber", standaloneResult.accountNumber)
                        put("documentData", org.json.JSONObject().apply {
                            put("payload", org.json.JSONObject().apply {
                                put("document_data", org.json.JSONObject().apply {
                                    put("documentStatus", standaloneResult.documentStatus)
                                    put("documentScore", standaloneResult.documentScore)
                                    put("faceMatchScore", standaloneResult.faceMatchScore)
                                    put("antiSpoofingFaceScore", standaloneResult.antiSpoofingFaceScore)
                                })
                            })
                        })
                        put("riskData", org.json.JSONObject().apply {
                            put("personSearchDataResults", org.json.JSONObject().apply {
                                put("personsearch_data", org.json.JSONObject().apply {
                                    put("personSearchScore", standaloneResult.personScore)
                                    put("personSearchResult", standaloneResult.personResult)
                                    put("personSearchRating", standaloneResult.personRating)
                                })
                            })
                            put("informationSearchDataResults", org.json.JSONObject().apply {
                                put("informationsearch_data", org.json.JSONObject().apply {
                                    put("riskInformationScore", standaloneResult.riskInformationScore)
                                    put("riskInformationResult", standaloneResult.riskInformationResult)
                                    put("riskInformationRating", standaloneResult.riskInformationRating)
                                })
                            })
                        })
                    }.toString()
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error converting result to JSON", e)
                    "{\"success\": true}"
                }
            }
            else -> {
                android.util.Log.w(TAG, "Unknown result type, using default JSON")
                "{\"success\": true}"
            }
        }
        
        // Convert standalone verification result to SDK format
        val result = VerificationResult(
            success = true,
            verificationId = "verify_${System.currentTimeMillis()}",
            confidence = 0.95f,
            documentType = "ID_CARD", // Extract from standalone result
            extractedData = mapOf(
                "name" to "Extracted Name",
                "document_number" to "Extracted Number"
                // Extract actual data from standalone result
            ),
            processingTime = System.currentTimeMillis() - (intent.getLongExtra(StandaloneAppBridge.EXTRA_START_TIME, 0)),
            sessionId = "session_${System.currentTimeMillis()}",
            rawResponse = rawResponse // Include raw JSON for detailed parsing
        )
        
        android.util.Log.d(TAG, "🔄 Converted to SDK result with raw response length: ${rawResponse.length}")
        
        BridgeCallbackRegistry.getVerificationCallback()?.onVerificationSuccess(result)
        
        // Clear callbacks and finish activity
        BridgeCallbackRegistry.clearCallbacks()
        finish()
    }
    
    private fun handleAuthenticationSuccess(standaloneResult: Any) {
        android.util.Log.d(TAG, "✅ Standalone authentication completed successfully")
        
        // Convert standalone authentication result to SDK format
        val result = AuthenticationResult(
            success = true,
            authenticationId = "auth_${System.currentTimeMillis()}",
            confidence = 0.98f,
            processingTime = System.currentTimeMillis() - (intent.getLongExtra(StandaloneAppBridge.EXTRA_START_TIME, 0)),
            sessionId = "session_${System.currentTimeMillis()}"
        )
        
        BridgeCallbackRegistry.getAuthenticationCallback()?.onAuthenticationSuccess(result)
        
        // Clear callbacks and finish activity
        BridgeCallbackRegistry.clearCallbacks()
        finish()
    }
    
    private fun handleError(errorMessage: String) {
        android.util.Log.e(TAG, "❌ Standalone app error: $errorMessage")
        
        val error = SDKError(
            code = SDKErrorCode.UNKNOWN_ERROR,
            message = errorMessage
        )
        
        when (flowType) {
            StandaloneAppBridge.FLOW_TYPE_VERIFICATION -> {
                BridgeCallbackRegistry.getVerificationCallback()?.onVerificationError(error)
            }
            
            StandaloneAppBridge.FLOW_TYPE_AUTHENTICATION -> {
                BridgeCallbackRegistry.getAuthenticationCallback()?.onAuthenticationError(error)
            }
        }
        
        // Clear callbacks and finish activity
        BridgeCallbackRegistry.clearCallbacks()
        finish()
    }
    
    private fun handleCancel() {
        android.util.Log.d(TAG, "⏹️ Standalone app cancelled by user")
        
        when (flowType) {
            StandaloneAppBridge.FLOW_TYPE_VERIFICATION -> {
                BridgeCallbackRegistry.getVerificationCallback()?.onVerificationCancelled()
            }
            
            StandaloneAppBridge.FLOW_TYPE_AUTHENTICATION -> {
                BridgeCallbackRegistry.getAuthenticationCallback()?.onAuthenticationCancelled()
            }
        }
        
        // Clear callbacks and finish activity
        BridgeCallbackRegistry.clearCallbacks()
        finish()
    }
    
    // NFC handling methods
    private fun initializeNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            android.util.Log.w(TAG, "⚠️ NFC not available on this device")
            return
        }
        
        if (nfcAdapter?.isEnabled != true) {
            android.util.Log.w(TAG, "⚠️ NFC is disabled")
            return
        }
        
        // Create pending intent for NFC
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        android.util.Log.d(TAG, "✅ NFC initialized successfully")
    }
    
    override fun onResume() {
        super.onResume()
        enableNFCReading()
    }
    
    override fun onPause() {
        super.onPause()
        disableNFCReading()
    }
    
    private fun enableNFCReading() {
        nfcAdapter?.let { adapter ->
            pendingIntent?.let { intent ->
                try {
                    adapter.enableForegroundDispatch(this, intent, null, null)
                    android.util.Log.d(TAG, "✅ NFC foreground dispatch enabled")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Failed to enable NFC foreground dispatch", e)
                }
            }
        }
    }
    
    private fun disableNFCReading() {
        nfcAdapter?.let { adapter ->
            try {
                adapter.disableForegroundDispatch(this)
                android.util.Log.d(TAG, "✅ NFC foreground dispatch disabled")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "❌ Failed to disable NFC foreground dispatch", e)
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        intent?.let { newIntent ->
            android.util.Log.d(TAG, "📱 New intent received: ${newIntent.action}")
            
            // Handle NFC tag discovery
            if (NfcAdapter.ACTION_TAG_DISCOVERED == newIntent.action ||
                NfcAdapter.ACTION_TECH_DISCOVERED == newIntent.action ||
                NfcAdapter.ACTION_NDEF_DISCOVERED == newIntent.action) {
                
                val tag = newIntent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if (tag != null) {
                    handleNFCTag(tag)
                } else {
                    android.util.Log.w(TAG, "⚠️ NFC intent received but no tag found")
                }
            }
        }
    }
    
    private fun handleNFCTag(tag: Tag) {
        android.util.Log.d(TAG, "🏷️ NFC Tag detected: ${tag.id.joinToString("") { "%02x".format(it) }}")
        
        try {
            val isoDep = android.nfc.tech.IsoDep.get(tag)
            if (isoDep != null) {
                android.util.Log.d(TAG, "✅ IsoDep tag detected, connecting...")
                isoDep.connect()
                setIsoDep(isoDep)
                android.util.Log.d(TAG, "✅ IsoDep connected and stored for passport reading")
            } else {
                android.util.Log.w(TAG, "⚠️ Tag is not ISO14443-4 compatible")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to handle NFC tag", e)
        }
    }
}

/**
 * Theme wrapper that applies SDK theme configuration to the standalone app
 */
@Composable
fun StandaloneAppTheme(
    themeConfig: SDKThemeConfiguration?,
    content: @Composable () -> Unit
) {
    // Convert hex colors to Compose Colors
    val primaryColor = themeConfig?.primaryColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF263238)
    val backgroundColor = themeConfig?.backgroundColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF263238)
    
    // Apply theme to MaterialTheme
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = primaryColor,
            background = backgroundColor
        ),
        content = content
    )
}

