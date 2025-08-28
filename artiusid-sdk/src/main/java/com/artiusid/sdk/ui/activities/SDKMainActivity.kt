package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.artiusid.sdk.sdk.navigation.SDKNavigation
import com.artiusid.sdk.sdk.ui.theme.SDKThemeProvider
import com.artiusid.sdk.sdk.ArtiusIDSDK

/**
 * Main SDK Activity that hosts the complete standalone app experience
 * 
 * This activity serves as the entry point for the SDK and provides the complete
 * verification and authentication flows from the standalone application.
 * 
 * The host application launches this activity through the SDK, and this activity
 * provides the complete user experience with all the sophisticated UI/UX from
 * the standalone application.
 */
class SDKMainActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_FLOW_TYPE = "flow_type"
        const val EXTRA_CONFIG = "config"
        const val FLOW_TYPE_VERIFICATION = "verification"
        const val FLOW_TYPE_AUTHENTICATION = "authentication"
        
        private const val TAG = "SDKMainActivity"
    }
    
    private var flowType: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the flow type from intent
        flowType = intent.getStringExtra(EXTRA_FLOW_TYPE)
        
        android.util.Log.d(TAG, "Starting SDK with flow type: $flowType")
        
        setContent {
            // Use the SDK's themeable system
            SDKThemeProvider.ArtiusSDKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use the complete navigation system from standalone app
                    val navController = rememberNavController()
                    
                    SDKNavigation(
                        navController = navController,
                        flowType = flowType ?: "verification"
                    )
                }
            }
        }
    }
    

    
    override fun onBackPressed() {
        // Handle back press - return to host app with cancelled result
        when (flowType) {
            FLOW_TYPE_VERIFICATION -> {
                ArtiusIDSDK.verificationCallback?.onVerificationCancelled()
            }
            FLOW_TYPE_AUTHENTICATION -> {
                ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
            }
        }
        
        super.onBackPressed()
    }
    
    /**
     * Called when verification flow completes successfully
     */
    fun onVerificationComplete(result: Any) {
        android.util.Log.d(TAG, "Verification completed successfully")
        // The result will be handled by the navigation system and callbacks
        finish()
    }
    
    /**
     * Called when authentication flow completes successfully  
     */
    fun onAuthenticationComplete(result: Any) {
        android.util.Log.d(TAG, "Authentication completed successfully")
        // The result will be handled by the navigation system and callbacks
        finish()
    }
    
    /**
     * Called when any flow encounters an error
     */
    fun onFlowError(error: Exception) {
        android.util.Log.e(TAG, "Flow error: ${error.message}", error)
        
        when (flowType) {
            FLOW_TYPE_VERIFICATION -> {
                ArtiusIDSDK.verificationCallback?.onVerificationError(
                    com.artiusid.sdk.models.SDKError(
                        code = com.artiusid.sdk.models.SDKErrorCode.UNKNOWN_ERROR,
                        message = error.message ?: "Unknown error",
                        cause = error
                    )
                )
            }
            FLOW_TYPE_AUTHENTICATION -> {
                ArtiusIDSDK.authenticationCallback?.onAuthenticationError(
                    com.artiusid.sdk.models.SDKError(
                        code = com.artiusid.sdk.models.SDKErrorCode.UNKNOWN_ERROR,
                        message = error.message ?: "Unknown error",
                        cause = error
                    )
                )
            }
        }
        
        finish()
    }
}
