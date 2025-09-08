package com.artiusid.sdk.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Self-contained NFC handler for the SDK
 * Prevents external app launches and handles all NFC operations internally
 * EXACT STANDALONE APPLICATION IMPLEMENTATION
 */
class NfcHandler(private val context: Context) {
    
    companion object {
        private const val TAG = "SDKNfcHandler"
        
        // Global state for NFC tag sharing within SDK
        @Volatile
        var currentNfcTag: Tag? = null
        
        @Volatile
        var currentIsoDep: IsoDep? = null
        
        @Volatile
        var isNfcScanActive: Boolean = false
    }
    
    private var nfcAdapter: NfcAdapter? = null
    private var isReaderModeEnabled = false
    
    fun initialize() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        Log.d(TAG, "📡 SDK NFC Handler initialized: ${nfcAdapter?.isEnabled}")
    }

    fun isNfcAvailable(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Enable NFC reader mode for passport chip detection
     * This prevents external app launches and keeps NFC handling within SDK
     */
    fun enableNfcReading(activity: Activity) {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled && !isReaderModeEnabled) {
                Log.d(TAG, "📡 Enabling SDK NFC Reader Mode")
                
                // Use enableReaderMode to prevent external app launches
                adapter.enableReaderMode(
                    activity,
                    { tag ->
                        // Handle NFC tag detection within SDK
                        Log.d(TAG, "🎯 NFC CALLBACK TRIGGERED! Tag detected in SDK reader mode")
                        activity.runOnUiThread {
                            handleNfcTagDetected(tag)
                        }
                    },
                    NfcAdapter.FLAG_READER_NFC_A or 
                    NfcAdapter.FLAG_READER_NFC_B or 
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
                
                isReaderModeEnabled = true
                Log.d(TAG, "✅ SDK NFC Reader Mode enabled successfully")
                Log.d(TAG, "📡 Reader Mode Flags: NFC_A | NFC_B | SKIP_NDEF_CHECK")
                Log.d(TAG, "📡 Activity: ${activity.javaClass.simpleName}")
                Log.d(TAG, "📡 Waiting for NFC tags...")
            } else if (!adapter.isEnabled) {
                Log.w(TAG, "📡 NFC is disabled on this device")
            } else {
                Log.d(TAG, "📡 NFC Reader Mode already enabled")
            }
        } ?: run {
            Log.w(TAG, "📡 No NFC adapter available")
        }
    }

    /**
     * Disable NFC reader mode
     */
    fun disableNfcReading(activity: Activity) {
        nfcAdapter?.let { adapter ->
            if (isReaderModeEnabled) {
                Log.d(TAG, "📡 Disabling SDK NFC Reader Mode")
                adapter.disableReaderMode(activity)
                
                // Clean up stored NFC data
                cleanupNfcData()
                
                isReaderModeEnabled = false
                Log.d(TAG, "✅ SDK NFC Reader Mode disabled successfully")
            }
        }
    }
    
    /**
     * Handle NFC tag detection within SDK (prevents external app launches)
     */
    private fun handleNfcTagDetected(tag: Tag) {
        Log.d(TAG, "📡 NFC tag detected within SDK!")
        Log.d(TAG, "📋 Tag ID: ${tag.id.joinToString("") { "%02x".format(it) }}")
        Log.d(TAG, "📋 Tag technologies: ${tag.techList.joinToString()}")
        
        // Store the tag for SDK internal use
        currentNfcTag = tag
        
        // Check if this is an IsoDep tag (passport) and connect immediately
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                Log.d(TAG, "🔗 Connecting to IsoDep within SDK...")
                isoDep.timeout = 3000 // 3 second timeout for better stability
                isoDep.connect()
                currentIsoDep = isoDep
                Log.d(TAG, "✅ IsoDep connected successfully within SDK! MaxTransceive: ${isoDep.maxTransceiveLength}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to connect to IsoDep within SDK: ${e.message}")
                currentIsoDep = null
            }
        } else {
            Log.w(TAG, "⚠️ Tag is not ISO14443-4 compatible")
            currentIsoDep = null
        }
    }

    /**
     * Handle NFC intent (fallback for older Android versions)
     */
    fun handleNfcIntent(intent: Intent): Tag? {
        return if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                handleNfcTagDetected(tag)
            }
            tag
        } else {
            null
        }
    }

    /**
     * Get IsoDep connection for passport reading
     */
    fun getIsoDep(tag: Tag): IsoDep? {
        return IsoDep.get(tag)
    }
    
    /**
     * Clean up NFC resources
     */
    private fun cleanupNfcData() {
        currentNfcTag = null
        currentIsoDep?.let { isoDep ->
            try {
                isoDep.close()
                Log.d(TAG, "🧹 IsoDep connection closed")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error closing IsoDep: ${e.message}")
            }
        }
        currentIsoDep = null
    }
    
    /**
     * Check if NFC scan is currently active
     */
    fun isNfcScanActive(): Boolean = isNfcScanActive
    
    /**
     * Set NFC scan active state
     */
    fun setNfcScanActive(active: Boolean) {
        isNfcScanActive = active
        Log.d(TAG, "📡 NFC scan active state: $active")
    }
}