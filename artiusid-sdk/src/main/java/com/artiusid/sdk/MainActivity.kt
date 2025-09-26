/*
 * File: MainActivity.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.navigation.AppNavigation
import com.artiusid.sdk.presentation.screens.root.RootScreen
import com.artiusid.sdk.ui.theme.EnhancedSDKTheme
import com.artiusid.sdk.ui.theme.EnhancedThemeManager
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.utils.FirebaseTokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StandaloneAppActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "StandaloneAppActivity"
        var currentNfcTag: Tag? = null // Store NFC tag for current session
        var currentIsoDep: android.nfc.tech.IsoDep? = null // Store connected IsoDep
    }
    
    private var permissionStatus = mutableMapOf<String, Boolean>()
    private var nfcAdapter: NfcAdapter? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results similar to iOS AppDelegate
        permissions.forEach { (permission, granted) ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    permissionStatus["notifications"] = granted
                    if (granted) {
                        Log.i(TAG, "Notification permission granted")
                        // Initialize FCM token similar to iOS
                        initializeFCMToken()
                    } else {
                        Log.w(TAG, "Notification permission denied")
                        permissionStatus["notifications"] = false
                    }
                }
            }
        }
        
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d(TAG, "All permissions granted")
        } else {
            Log.w(TAG, "Some permissions denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== StandaloneAppActivity onCreate ===")
        
        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        // Request permissions (including notifications)
        requestAllPermissions()
        
        // Initialize FCM token on app start (similar to iOS Firebase setup)
        initializeFCMToken()
        
        // NFC handling is done via ReaderMode, no need for intent handling
        
        setContent {
            // Use local state for theme that can be updated via callback
            val initialTheme = EnhancedThemeManager.getCurrentThemeConfig()
            android.util.Log.d("MainActivity", "🎨 MainActivity starting with theme: ${initialTheme.brandName}")
            
            val (localTheme, setLocalTheme) = remember { 
                mutableStateOf(initialTheme) 
            }
            
            // Register theme change listener with proper cleanup
            DisposableEffect(Unit) {
                val listener = { newTheme: EnhancedSDKThemeConfiguration? ->
                    android.util.Log.d("MainActivity", "🎨 Theme change callback received: ${newTheme?.brandName ?: "null"}")
                    setLocalTheme(newTheme ?: EnhancedThemeManager.getCurrentThemeConfig())
                }
                EnhancedThemeManager.addThemeChangeListener(listener)
                
                onDispose {
                    EnhancedThemeManager.removeThemeChangeListener(listener)
                }
            }
            
            // Debug logging to see theme changes
            android.util.Log.d("MainActivity", "🎨 Current theme in MainActivity: ${localTheme.brandName}")
            
            EnhancedSDKTheme(
                themeConfig = localTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootScreen()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "=== StandaloneAppActivity onNewIntent ===")
        
        // Set the new intent to prevent app restart
        setIntent(intent)
        
        // Handle NFC intents as backup if ReaderMode doesn't work
        val action = intent.action
        if (NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            
            Log.d(TAG, "📡 NFC intent received as backup - processing")
            handleNfcIntent(intent)
        } else {
            Log.d(TAG, "Non-NFC intent received: $action")
        }
    }
    
    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }
    
    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }
    
    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Handling NFC intent with action: $action")
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                Log.d(TAG, "📡 NFC tag detected via Intent (backup method)!")
                Log.d(TAG, "📋 Tag ID: ${tag.id.joinToString("") { "%02x".format(it) }}")
                Log.d(TAG, "📋 Tag technologies: ${tag.techList.joinToString()}")
                
                // Store the tag globally so the PassportChipScanScreen can access it
                currentNfcTag = tag
                
                // Check if this is an IsoDep tag (passport) and connect immediately
                val isoDep = android.nfc.tech.IsoDep.get(tag)
                if (isoDep != null) {
                    try {
                        Log.d(TAG, "🔗 Connecting to IsoDep immediately via Intent...")
                        isoDep.timeout = 3000 // 3 second timeout for better stability
                        isoDep.connect()
                        currentIsoDep = isoDep
                        Log.d(TAG, "✅ IsoDep connected successfully via Intent! MaxTransceive: ${isoDep.maxTransceiveLength}")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to connect to IsoDep via Intent: ${e.message}")
                        currentIsoDep = null
                    }
                } else {
                    Log.w(TAG, "⚠️ Tag is not ISO14443-4 compatible")
                    currentIsoDep = null
                }
            } else {
                Log.w(TAG, "❌ No NFC tag found in intent")
            }
        } else {
            Log.d(TAG, "Intent action is not NFC-related: $action")
        }
    }

    
    private fun enableNfcForegroundDispatch() {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                // Use enableReaderMode for better NFC stability (recommended by Android docs)
                adapter.enableReaderMode(
                    this,
                    { tag ->
                        // Run on UI thread to ensure proper handling
                        runOnUiThread {
                            Log.d(TAG, "📡 NFC tag detected via ReaderMode!")
                            Log.d(TAG, "📋 Tag ID: ${tag.id.joinToString("") { "%02x".format(it) }}")
                            Log.d(TAG, "📋 Tag technologies: ${tag.techList.joinToString()}")
                            
                            // Store the tag globally so the PassportChipScanScreen can access it
                            currentNfcTag = tag
                            
                            // Check if this is an IsoDep tag (passport) and connect immediately
                            val isoDep = android.nfc.tech.IsoDep.get(tag)
                            if (isoDep != null) {
                                try {
                                    Log.d(TAG, "🔗 Connecting to IsoDep immediately...")
                                    isoDep.timeout = 3000 // 3 second timeout for better stability
                                    isoDep.connect()
                                    currentIsoDep = isoDep
                                    Log.d(TAG, "✅ IsoDep connected successfully! MaxTransceive: ${isoDep.maxTransceiveLength}")
                                } catch (e: Exception) {
                                    Log.e(TAG, "❌ Failed to connect to IsoDep immediately: ${e.message}")
                                    currentIsoDep = null
                                }
                            } else {
                                Log.w(TAG, "⚠️ Tag is not ISO14443-4 compatible")
                                currentIsoDep = null
                            }
                        }
                    },
                    // Include all NFC reader flags for maximum compatibility
                    NfcAdapter.FLAG_READER_NFC_A or 
                    NfcAdapter.FLAG_READER_NFC_B or 
                    NfcAdapter.FLAG_READER_NFC_F or 
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    null
                )
                Log.d(TAG, "🛂 NFC ReaderMode enabled for passport scanning")
            } else {
                Log.w(TAG, "❌ NFC adapter is not enabled")
            }
        }
    }
    
    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.let { adapter ->
            // Disable ReaderMode instead of ForegroundDispatch
            adapter.disableReaderMode(this)
            Log.d(TAG, "🛂 NFC ReaderMode disabled")
        }
    }
    
    /**
     * Initialize FCM token similar to iOS Firebase configuration
     */
    private fun initializeFCMToken() {
        lifecycleScope.launch {
            try {
                val tokenManager = FirebaseTokenManager.getInstance()
                tokenManager?.getFCMTokenAsync()
                val token = tokenManager?.getFCMToken()
                Log.d(TAG, "FCM token initialized: ${token?.take(10) ?: "null"}...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FCM token: ${e.message}", e)
            }
        }
    }
    
    /**
     * Request all required permissions including notifications
     * Similar to iOS permission management
     */
    private fun requestAllPermissions() {
        val permissions = mutableListOf<String>()
        
        // Storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - request READ_MEDIA_IMAGES
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - request READ_EXTERNAL_STORAGE
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Older versions - request both read and write
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        // Notification permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Check if permissions are already granted
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            Log.d(TAG, "All permissions already granted")
            // Initialize FCM token if notifications are already permitted
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                permissionStatus["notifications"] = true
                initializeFCMToken()
            }
        }
    }
    
    /**
     * Public method for other parts of the app to check permission status
     * Similar to iOS AppDelegate.hasPermission
     */
    fun hasPermission(type: String): Boolean {
        return permissionStatus[type] ?: false
    }
} 