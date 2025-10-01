/*
 * File: PassportChipScanScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// PassportChipScanScreen.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.presentation.screens.document

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.ToneGenerator
import android.media.AudioManager
import com.artiusid.sdk.standalone.StandaloneAppActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.data.models.passport.PassportAuthenticationStatus
import com.artiusid.sdk.data.models.passport.PassportNFCData
import com.artiusid.sdk.data.models.passport.PassportMRZData
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.utils.passport.PassportNFCReader
import com.artiusid.sdk.utils.ImageStorage
import kotlinx.coroutines.launch

// JMRTD imports for proper passport NFC reading
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec
import org.jmrtd.PassportService
import org.jmrtd.lds.icao.MRZInfo
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.CardSecurityFile
import net.sf.scuba.smartcards.*
import java.io.InputStream

// NFC Scanning states
sealed class NFCScanState {
    object Initial : NFCScanState()
    object WaitingForNFC : NFCScanState()
    object Connecting : NFCScanState()
    object Authenticating : NFCScanState()
    object ReadingData : NFCScanState()
    data class Success(val data: PassportNFCData) : NFCScanState()
    data class Error(val message: String) : NFCScanState()
}

// Helper function for NFC feedback (vibration + sound)
fun provideNFCFeedback(context: Context, isSuccess: Boolean = false) {
    try {
        // Vibration feedback
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val vibrationEffect = if (isSuccess) {
                    // Success: longer vibration
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    // Attempt: short vibration
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                it.vibrate(vibrationEffect)
            } else {
                // Fallback for older devices
                @Suppress("DEPRECATION")
                it.vibrate(if (isSuccess) 200 else 100)
            }
        }
        
        // Audio feedback
        val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
        if (isSuccess) {
            // Success: higher tone
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        } else {
            // Attempt: lower tone
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        }
        toneGenerator.release()
    } catch (e: Exception) {
        Log.w("PassportChipScan", "Failed to provide NFC feedback: ${e.message}")
    }
}

// JMRTD-based NFC passport reading function with pre-connected IsoDep
suspend fun readPassportWithIsoDep(isoDep: android.nfc.tech.IsoDep, mrzKey: String): PassportNFCData? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var cardService: CardService? = null
    var passportService: PassportService? = null
    
    try {
        Log.d("PassportChipScan", "🔗 Using pre-connected IsoDep for passport reading...")
        
        // Parse MRZ key to extract components (passport number, DOB, expiry)
        Log.d("PassportChipScan", "🔍 DIAGNOSTIC: Received MRZ key: '$mrzKey' (${mrzKey.length} chars)")
        Log.d("PassportChipScan", "🔍 DIAGNOSTIC: IsoDep connection status: ${isoDep.isConnected}")
        Log.d("PassportChipScan", "🔍 DIAGNOSTIC: IsoDep timeout: ${isoDep.timeout}ms")
        
        val mrzComponents = mrzKey.split("|")
        Log.d("PassportChipScan", "🔍 Split into ${mrzComponents.size} components: ${mrzComponents.joinToString(", ")}")
        if (mrzComponents.size != 3) {
            Log.e("PassportChipScan", "❌ DIAGNOSTIC: Invalid MRZ key format - expected 3 components, got ${mrzComponents.size}")
            Log.e("PassportChipScan", "❌ DIAGNOSTIC: Raw MRZ key: '$mrzKey'")
            return@withContext null
        }
        
        val passportNumber = mrzComponents[0]
        val dateOfBirth = mrzComponents[1] 
        val dateOfExpiry = mrzComponents[2]
        
        Log.d("PassportChipScan", "🔐 Creating JMRTD BAC key: ${passportNumber}, DOB: ${dateOfBirth}, Expiry: ${dateOfExpiry}")
        
        // Use the already connected IsoDep
        if (!isoDep.isConnected) {
            Log.d("PassportChipScan", "🔗 IsoDep not connected, connecting...")
            isoDep.connect()
        }
        
        // Create JMRTD card service from IsoDep with retry logic
        Log.d("PassportChipScan", "🔗 Creating JMRTD CardService from IsoDep...")
        
        // Create CardService from the connected IsoDep
        cardService = CardService.getInstance(isoDep)
        cardService.open()
        
        Log.d("PassportChipScan", "✅ CardService opened successfully")
        
        // Create PassportService for BAC authentication (standalone app method)
        passportService = PassportService(cardService, 224, 224, false, false)
        
        passportService.open()
        Log.d("PassportChipScan", "✅ PassportService opened successfully")
        
        // Perform BAC authentication with check digit
        Log.d("PassportChipScan", "🔐 Starting BAC authentication...")
        
        // CRITICAL FIX: Use EXACT standalone app implementation
        // BAC Key should be created WITHOUT check digits (JMRTD handles them internally)
        Log.d("PassportChipScan", "🔐 Creating JMRTD BAC key (standalone app method):")
        Log.d("PassportChipScan", "   Passport: $passportNumber (WITHOUT check digit)")
        Log.d("PassportChipScan", "   DOB: $dateOfBirth, Expiry: $dateOfExpiry")
        
        val bacKey = BACKey(passportNumber, dateOfBirth, dateOfExpiry)
        
        // Perform BAC authentication (standalone app method)
        Log.d("PassportChipScan", "🔐 Performing JMRTD BAC authentication...")
        passportService.sendSelectApplet(false)
        passportService.doBAC(bacKey)
        Log.d("PassportChipScan", "✅ JMRTD BAC authentication successful!")
        
        // Read passport data files
        Log.d("PassportChipScan", "📖 Reading passport data files...")
        
        // Read DG1 (passport data) - standalone app method
        Log.d("PassportChipScan", "📚 Reading DG1 with JMRTD...")
        val dg1InputStream = passportService.getInputStream(PassportService.EF_DG1)
        val dg1File = DG1File(dg1InputStream as InputStream)
        val mrzInfo = dg1File.mrzInfo
        
        Log.d("PassportChipScan", "✅ JMRTD passport reading successful!")
        Log.d("PassportChipScan", "   Document: ${mrzInfo.documentNumber}")
        Log.d("PassportChipScan", "   Name: ${mrzInfo.primaryIdentifier} ${mrzInfo.secondaryIdentifier}")
        Log.d("PassportChipScan", "   DOB: ${mrzInfo.dateOfBirth}")
        Log.d("PassportChipScan", "   Nationality: ${mrzInfo.nationality}")
        
        // Create PassportNFCData result using the same approach as readPassportBasic
        val passportNFCData = PassportNFCData(
            documentNumber = mrzInfo.documentNumber,
            firstName = mrzInfo.secondaryIdentifier?.split("<")?.get(0) ?: "", // Take first name only
            lastName = mrzInfo.primaryIdentifier.replace("<", " ").trim(),
            dateOfBirth = mrzInfo.dateOfBirth,
            nationality = mrzInfo.nationality,
            bacStatus = com.artiusid.sdk.data.models.passport.PassportAuthenticationStatus.SUCCESS,
            readTimestamp = System.currentTimeMillis()
        )
        
        Log.d("PassportChipScan", "✅ Passport NFC reading completed successfully!")
        Log.d("PassportChipScan", "📋 Document: ${passportNFCData.documentNumber}, Name: ${passportNFCData.firstName} ${passportNFCData.lastName}")
        
        return@withContext passportNFCData
        
    } catch (e: Exception) {
        Log.e("PassportChipScan", "❌ Error reading passport via NFC", e)
        return@withContext null
    } finally {
        // Clean up resources
        try {
            passportService?.close()
            cardService?.close()
        } catch (e: Exception) {
            Log.w("PassportChipScan", "⚠️ Error closing passport services: ${e.message}")
        }
    }
}

// JMRTD-based NFC passport reading function
suspend fun readPassportBasic(tag: Tag, mrzKey: String): PassportNFCData? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var cardService: CardService? = null
    var passportService: PassportService? = null
    var isoDep: android.nfc.tech.IsoDep? = null
    
    try {
        Log.d("PassportChipScan", "🔗 Connecting to passport chip using JMRTD...")
        
        // Parse MRZ key to extract components (passport number, DOB, expiry)
        Log.d("PassportChipScan", "🔍 Received MRZ key: '$mrzKey' (${mrzKey.length} chars)")
        val mrzComponents = mrzKey.split("|")
        Log.d("PassportChipScan", "🔍 Split into ${mrzComponents.size} components: ${mrzComponents.joinToString(", ")}")
        if (mrzComponents.size != 3) {
            Log.e("PassportChipScan", "❌ Invalid MRZ key format - expected 3 components, got ${mrzComponents.size}")
            return@withContext null
        }
        
        val passportNumber = mrzComponents[0]
        val dateOfBirth = mrzComponents[1] 
        val dateOfExpiry = mrzComponents[2]
        
        Log.d("PassportChipScan", "🔐 Creating JMRTD BAC key: ${passportNumber}, DOB: ${dateOfBirth}, Expiry: ${dateOfExpiry}")
        
        // Use already-connected IsoDep from StandaloneAppActivity if available
        Log.d("PassportChipScan", "🔗 Checking for pre-connected IsoDep from StandaloneAppActivity...")
        // Get IsoDep from StandaloneAppActivity if available
        isoDep = com.artiusid.sdk.standalone.StandaloneAppActivity.currentIsoDep
        if (isoDep == null) {
            Log.d("PassportChipScan", "🔗 No pre-connected IsoDep, converting NFC Tag to IsoDep...")
            isoDep = android.nfc.tech.IsoDep.get(tag)
            if (isoDep == null) {
                Log.e("PassportChipScan", "❌ Tag is not ISO14443-4 compatible")
                return@withContext null
            }
        } else {
            Log.d("PassportChipScan", "✅ Using pre-connected IsoDep from StandaloneAppActivity")
        }
        
        // Create JMRTD card service from IsoDep with retry logic
        Log.d("PassportChipScan", "🔗 Creating JMRTD CardService from IsoDep...")
        
        // Check if IsoDep is already connected (from StandaloneAppActivity)
        var connectionSuccess = isoDep.isConnected
        if (connectionSuccess) {
            Log.d("PassportChipScan", "✅ IsoDep already connected from StandaloneAppActivity - skipping connection")
        } else {
            Log.d("PassportChipScan", "🔗 IsoDep not connected, attempting connection...")
            // Retry NFC connection up to 3 times with delays
            for (attempt in 1..3) {
                try {
                    Log.d("PassportChipScan", "📡 NFC connection attempt $attempt/3...")
                    
                    // Connect with timeout
                    isoDep.timeout = 3000 // 3 second timeout for better stability
                    isoDep.connect()
                    
                    if (isoDep.isConnected) {
                        Log.d("PassportChipScan", "✅ NFC connection successful on attempt $attempt")
                        connectionSuccess = true
                        break
                    } else {
                        Log.w("PassportChipScan", "⚠️ IsoDep.connect() returned but not connected")
                    }
                } catch (e: java.io.IOException) {
                    Log.w("PassportChipScan", "⚠️ NFC connection attempt $attempt failed: ${e.message}")
                    if (attempt < 3) {
                        Log.d("PassportChipScan", "⏳ Waiting 500ms before retry...")
                        kotlinx.coroutines.delay(500) // Wait before retry
                    }
                }
            }
        }
        
        if (!connectionSuccess) {
            Log.e("PassportChipScan", "❌ Failed to establish NFC connection after 3 attempts")
            return@withContext null
        }
        
        cardService = CardService.getInstance(isoDep)
        cardService.open()
        
        // Create BAC key for JMRTD - CRITICAL: Use REAL check digits from MRZ
        val mrzData = ImageStorage.getPassportMRZData()
        val bacKey = if (mrzData != null) {
            val realCheckDigits = extractRealCheckDigitsFromMRZ(mrzData)
            val passportWithRealCheck = passportNumber + realCheckDigits.passportCheck
            
            Log.d("PassportChipScan", "🔐 BAC Key Generation - Using REAL MRZ check digits:")
            Log.d("PassportChipScan", "   Original passport: $passportNumber (${passportNumber.length} chars)")
            Log.d("PassportChipScan", "   Real check digit from MRZ: ${realCheckDigits.passportCheck}")
            Log.d("PassportChipScan", "   Passport with REAL check: $passportWithRealCheck (${passportWithRealCheck.length} chars)")
            Log.d("PassportChipScan", "   DOB: $dateOfBirth, Expiry: $dateOfExpiry")
            
            BACKey(passportWithRealCheck, dateOfBirth, dateOfExpiry)
        } else {
            // Fallback: try without check digit
            Log.d("PassportChipScan", "🔐 BAC Key Generation - Fallback without check digit:")
            Log.d("PassportChipScan", "   Passport: $passportNumber (${passportNumber.length} chars)")
            Log.d("PassportChipScan", "   DOB: $dateOfBirth, Expiry: $dateOfExpiry")
            
            BACKey(passportNumber, dateOfBirth, dateOfExpiry)
        }
        Log.d("PassportChipScan", "🔑 Generated JMRTD BAC key with check digit")
        
        // Initialize JMRTD PassportService for BAC (with proper constructor parameters)
        passportService = PassportService(cardService, 224, 224, false, false)
        
        // Perform BAC authentication
        Log.d("PassportChipScan", "🔐 Performing JMRTD BAC authentication...")
        passportService.sendSelectApplet(false)
        passportService.doBAC(bacKey)
        Log.d("PassportChipScan", "✅ JMRTD BAC authentication successful!")
        
        // Read DG1 (passport data)
        Log.d("PassportChipScan", "📚 Reading DG1 with JMRTD...")
        val dg1InputStream = passportService.getInputStream(PassportService.EF_DG1)
        val dg1File = DG1File(dg1InputStream as InputStream)
        val mrzInfo = dg1File.mrzInfo
        
        Log.d("PassportChipScan", "✅ JMRTD passport reading successful!")
        Log.d("PassportChipScan", "   Document: ${mrzInfo.documentNumber}")
        Log.d("PassportChipScan", "   Name: ${mrzInfo.primaryIdentifier} ${mrzInfo.secondaryIdentifier}")
        Log.d("PassportChipScan", "   DOB: ${mrzInfo.dateOfBirth}")
        Log.d("PassportChipScan", "   Nationality: ${mrzInfo.nationality}")
        
        // Return passport data
        PassportNFCData(
            documentNumber = mrzInfo.documentNumber,
            firstName = mrzInfo.secondaryIdentifier.split("<")[0], // Take first name only
            lastName = mrzInfo.primaryIdentifier.replace("<", " ").trim(),
            dateOfBirth = mrzInfo.dateOfBirth,
            nationality = mrzInfo.nationality,
            bacStatus = com.artiusid.sdk.data.models.passport.PassportAuthenticationStatus.SUCCESS,
            readTimestamp = System.currentTimeMillis()
        )
        
    } catch (e: Exception) {
        when (e) {
            is java.io.IOException -> {
                Log.e("PassportChipScan", "❌ NFC connection failed: ${e.message}")
                Log.e("PassportChipScan", "💡 This usually means the passport moved away from the NFC antenna")
                Log.e("PassportChipScan", "💡 Try holding the passport very steady against the phone's NFC area")
            }
            else -> {
                Log.e("PassportChipScan", "❌ JMRTD passport reading failed: ${e.message}", e)
                Log.e("PassportChipScan", "Exception details: ${e.javaClass.simpleName}")
            }
        }
        e.printStackTrace()
        null
    } finally {
        // CRITICAL: Always cleanup NFC resources to prevent lockups
        try {
            Log.d("PassportChipScan", "🧹 Cleaning up NFC resources...")
            passportService?.close()
            cardService?.close()
            isoDep?.close()
            Log.d("PassportChipScan", "✅ NFC resources cleaned up successfully")
        } catch (cleanupException: Exception) {
            Log.w("PassportChipScan", "⚠️ Error during NFC cleanup: ${cleanupException.message}")
        }
    }
}

// Helper data class for real check digits
data class RealCheckDigits(
    val passportCheck: String,
    val dobCheck: String,
    val expiryCheck: String
)

/**
 * Calculate MRZ check digit using ICAO 9303 standard
 */
private fun calculateMRZCheckDigit(input: String): Int {
    val weights = intArrayOf(7, 3, 1)
    var sum = 0
    
    for (i in input.indices) {
        val char = input[i]
        val value = when {
            char.isDigit() -> char.digitToInt()
            char.isLetter() -> char.uppercaseChar().code - 'A'.code + 10
            else -> 0 // For < characters
        }
        sum += value * weights[i % 3]
    }
    
    return sum % 10
}

// Extract real check digits from the actual MRZ lines
fun extractRealCheckDigitsFromMRZ(mrzData: PassportMRZData): RealCheckDigits {
    // MRZ Line 2 format: PPPPPPPPPCCCDDDDDDDEEEEEEEXXXXXXXXXXXXXXX
    // Where: P=passport number, C=passport check, D=DOB, E=expiry, X=other data
    // Position 9 = passport check digit
    // Position 19 = DOB check digit  
    // Position 27 = expiry check digit
    
    val line2 = mrzData.line2
    Log.d("PassportChipScan", "🔍 Extracting check digits from MRZ Line 2: '$line2'")
    
    return try {
        val passportCheck = if (line2.length > 9) line2[9].toString() else "0"
        val dobCheck = if (line2.length > 19) line2[19].toString() else "0" 
        val expiryCheck = if (line2.length > 27) line2[27].toString() else "0"
        
        Log.d("PassportChipScan", "🎯 Real check digits: passport=$passportCheck, dob=$dobCheck, expiry=$expiryCheck")
        
        RealCheckDigits(passportCheck, dobCheck, expiryCheck)
    } catch (e: Exception) {
        Log.e("PassportChipScan", "❌ Error extracting check digits: ${e.message}")
        // Fallback to calculated check digits
        val passportCheck = PassportMRZData.calculateCheckDigit(mrzData.passportNumber ?: "")
        RealCheckDigits(passportCheck.toString(), "0", "0")
    }
}

// Note: BAC key generation now handled by JMRTD library

// Note: BAC authentication now handled by JMRTD library

// Note: DG1 reading now handled by JMRTD library

@Composable
fun PassportChipScanScreen(
    onChipScanComplete: (PassportNFCData?) -> Unit,
    onNavigateBack: () -> Unit,
    mrzKey: String = "", // Will be passed from passport scan
    viewModel: DocumentScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // NFC components - will be injected properly in final implementation
    // For now, use a placeholder that matches the interface
    val nfcReader = remember { 
        // This will be replaced with proper dependency injection
        null // Placeholder - NFC functionality will be implemented via ViewModel
    }
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    
    // Get MRZ data from ImageStorage to create authentication key
    val mrzData = remember { ImageStorage.getPassportMRZData() }
    val authKey = remember(mrzData) {
        Log.d("PassportChipScan", "🔍 DIAGNOSTIC: Checking MRZ data availability...")
        Log.d("PassportChipScan", "🔍 DIAGNOSTIC: mrzData is null: ${mrzData == null}")
        
        mrzData?.let { mrz ->
            Log.d("PassportChipScan", "✅ Using REAL passport MRZ data: ${mrz.passportNumber}")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: Full MRZ object: $mrz")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: MRZ Line 1: '${mrz.line1}'")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: MRZ Line 2: '${mrz.line2}'")
            // Use ICAO standard format WITH check digits for JMRTD BAC authentication
            Log.d("PassportChipScan", "🔍 MRZ validation status: ${mrz.isValid}")
            Log.d("PassportChipScan", "🔍 MRZ data: passport=${mrz.passportNumber}, dob=${mrz.dateOfBirth}, expiry=${mrz.dateOfExpiry}")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: Passport number length: ${mrz.passportNumber?.length}")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: DOB length: ${mrz.dateOfBirth?.length}")
            Log.d("PassportChipScan", "🔍 DIAGNOSTIC: Expiry length: ${mrz.dateOfExpiry?.length}")
            
            // Extract REAL check digits from the actual MRZ lines instead of calculating
            // The passport chip expects the exact same check digits that are printed on the passport
            val realCheckDigits = extractRealCheckDigitsFromMRZ(mrz)
            
            Log.d("PassportChipScan", "🔢 Extracted real check digits from MRZ: passport=${realCheckDigits.passportCheck}")
            
            // For BAC authentication, JMRTD needs passport number WITHOUT check digit
            val passportWithoutCheck = if (mrz.passportNumber?.length == 10) {
                // Remove the check digit (last character)
                Log.d("PassportChipScan", "✅ Removing check digit from passport number: ${mrz.passportNumber}")
                mrz.passportNumber.dropLast(1)
            } else {
                // Already 9 digits, use as-is
                Log.d("PassportChipScan", "✅ Passport number is 9 digits: ${mrz.passportNumber}")
                mrz.passportNumber
            }
            val dobWithoutCheck = mrz.dateOfBirth  // JMRTD handles date check digits internally
            val expiryWithoutCheck = mrz.dateOfExpiry  // JMRTD handles date check digits internally
            
            Log.d("PassportChipScan", "🔑 Generated JMRTD BAC key format (WITHOUT check digits)")
            Log.d("PassportChipScan", "   Passport: $passportWithoutCheck (9 digits), DOB: $dobWithoutCheck, Expiry: $expiryWithoutCheck")
            val finalKey = "$passportWithoutCheck|$dobWithoutCheck|$expiryWithoutCheck"
            Log.d("PassportChipScan", "🔑 DIAGNOSTIC: Final authentication key: '$finalKey' (${finalKey.length} chars)")
            finalKey
        } ?: mrzKey.ifEmpty { 
            Log.e("PassportChipScan", "❌ NO REAL MRZ DATA FOUND! Please scan your passport's MRZ first.")
            Log.e("PassportChipScan", "❌ DIAGNOSTIC: mrzKey parameter: '$mrzKey'")
            Log.e("PassportChipScan", "❌ DIAGNOSTIC: ImageStorage.getPassportMRZData() returned null")
            "NO_MRZ_DATA_AVAILABLE"
        }
    }
    
    // State management
    var nfcScanState by remember { mutableStateOf<NFCScanState>(NFCScanState.Initial) }
    var lastNfcTag by remember { mutableStateOf<Tag?>(null) }
    var retryCount by remember { mutableStateOf(0) }
    val maxRetries = 3
    
    // Check NFC availability
    val isNfcAvailable = nfcAdapter?.isEnabled == true
    val isNfcEnabled = nfcAdapter?.isEnabled == true
    
    // Handle NFC tag detection
    LaunchedEffect(Unit) {
        if (authKey == "NO_MRZ_DATA_AVAILABLE") {
            nfcScanState = NFCScanState.Error("No real passport MRZ data found. Please go back and scan your actual passport's MRZ first. Test data has been removed.")
        } else if (isNfcAvailable && isNfcEnabled) {
            nfcScanState = NFCScanState.WaitingForNFC
        } else {
            nfcScanState = NFCScanState.Error("NFC is not available or disabled")
        }
    }
    
    // Monitor for NFC connection from StandaloneAppActivity
    LaunchedEffect(nfcScanState) {
        if (nfcScanState is NFCScanState.WaitingForNFC) {
            // Poll for NFC connection every 500ms
            while (nfcScanState is NFCScanState.WaitingForNFC) {
                val currentIsoDep = com.artiusid.sdk.standalone.StandaloneAppActivity.currentIsoDep
                if (currentIsoDep != null && currentIsoDep.isConnected) {
                    Log.d("PassportChipScan", "🎉 NFC IsoDep connection detected from StandaloneAppActivity!")
                    // Start the NFC reading process
                    nfcScanState = NFCScanState.Connecting
                    // Start the NFC reading process in a coroutine with retry logic
                    scope.launch {
                        var attempt = 0
                        
                        while (attempt < maxRetries) {
                            attempt++
                            
                            try {
                                Log.d("PassportChipScan", "🚀 NFC reading attempt $attempt/$maxRetries...")
                                nfcScanState = NFCScanState.Authenticating
                                
                                val passportData = readPassportWithIsoDep(currentIsoDep, authKey)
                                
                                if (passportData != null) {
                                    Log.d("PassportChipScan", "✅ Passport reading successful on attempt $attempt!")
                                    nfcScanState = NFCScanState.Success(passportData)
                                    kotlinx.coroutines.delay(1000) // Show success state
                                    onChipScanComplete(passportData)
                                    return@launch // Success - exit retry loop
                                } else {
                                    Log.w("PassportChipScan", "⚠️ Passport reading failed on attempt $attempt")
                                    retryCount = attempt
                                    if (attempt < maxRetries) {
                                        Log.d("PassportChipScan", "🔄 Retrying NFC reading... ($attempt/$maxRetries)")
                                        nfcScanState = NFCScanState.WaitingForNFC
                                        kotlinx.coroutines.delay(1500) // Wait before retry
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("PassportChipScan", "❌ Error during NFC reading attempt $attempt: ${e.message}", e)
                                retryCount = attempt
                                if (attempt < maxRetries) {
                                    Log.d("PassportChipScan", "🔄 Retrying after error... ($attempt/$maxRetries)")
                                    nfcScanState = NFCScanState.WaitingForNFC
                                    kotlinx.coroutines.delay(1500) // Wait before retry
                                }
                            }
                        }
                        
                        // All attempts failed - proceed with verification without NFC data
                        Log.w("PassportChipScan", "⚠️ All $maxRetries NFC reading attempts failed. Proceeding with verification without NFC data.")
                        retryCount = maxRetries
                        nfcScanState = NFCScanState.Error("NFC reading failed after $maxRetries attempts. Proceeding with verification...")
                        
                        // Wait a moment to show the error message, then proceed
                        kotlinx.coroutines.delay(2000)
                        
                        // Proceed with verification without NFC data (pass null)
                        Log.d("PassportChipScan", "📋 Proceeding with verification without NFC passport data")
                        onChipScanComplete(null)
                    }
                    break
                }
                kotlinx.coroutines.delay(500) // Check every 500ms
            }
        }
    }
    
    
    // Function to retry NFC scanning
    fun retryNfcScan() {
        Log.d("PassportChipScan", "🔄 Retrying NFC scan (attempt ${retryCount + 1})")
        retryCount += 1
        nfcScanState = NFCScanState.WaitingForNFC
        
        // CRITICAL: Clear ALL stale NFC resources to prevent lockups
        com.artiusid.StandaloneAppActivity.currentNfcTag = null
        com.artiusid.StandaloneAppActivity.currentIsoDep?.let { isoDep ->
            try {
                Log.d("PassportChipScan", "🧹 Closing stale IsoDep connection from StandaloneAppActivity...")
                isoDep.close()
            } catch (e: Exception) {
                Log.w("PassportChipScan", "⚠️ Error closing stale IsoDep: ${e.message}")
            }
        }
        com.artiusid.StandaloneAppActivity.currentIsoDep = null
        lastNfcTag = null
        
        Log.d("PassportChipScan", "✅ NFC state cleared for retry")
    }
    
    
    // Function to process NFC tag - now with real reading capability
    fun processNfcTag(tag: Tag) {
        scope.launch {
            try {
                Log.d("PassportChipScan", "📡 Processing real NFC tag for passport reading... (attempt ${retryCount + 1})")
                Log.d("PassportChipScan", "📋 Tag technologies: ${tag.techList.joinToString()}")
                
                // Check if this is a valid passport chip (ISO14443-4)
                val isoDep = android.nfc.tech.IsoDep.get(tag)
                if (isoDep == null) {
                    nfcScanState = NFCScanState.Error("This NFC tag is not a passport. Please use a valid e-passport.")
                    return@launch
                }
                
                Log.d("PassportChipScan", "✅ Valid passport NFC chip detected")
                
                // Pass the tag to trigger the corrected JMRTD implementation
                lastNfcTag = tag
                
            } catch (e: Exception) {
                Log.e("PassportChipScan", "❌ NFC chip reading error: ${e.message}", e)
                nfcScanState = NFCScanState.Error("NFC reading failed: ${e.localizedMessage ?: "Unknown error"}. Tap 'Try Again' to retry.")
            }
        }
    }
    
    // Real NFC chip reading with fixed MRZ key authentication
    LaunchedEffect(nfcScanState) {
        if (nfcScanState is NFCScanState.WaitingForNFC) {
            Log.d("PassportChipScan", "🛂 Starting real NFC chip reading - Place passport on NFC reader")
            Log.d("PassportChipScan", "📱 Using MRZ key: ${authKey.take(6)}... for authentication")
        }
    }
    
    // Check for NFC tags from StandaloneAppActivity - restart monitoring when state changes to WaitingForNFC
    LaunchedEffect(nfcScanState) {
        if (nfcScanState is NFCScanState.WaitingForNFC) {
            kotlinx.coroutines.delay(1000) // Give UI time to settle
            Log.d("PassportChipScan", "🔍 Starting NFC tag monitoring loop...")
            
            while (nfcScanState is NFCScanState.WaitingForNFC) {
            // Check if StandaloneAppActivity has captured an NFC tag
            val mainActivityTag = com.artiusid.StandaloneAppActivity.currentNfcTag
            if (mainActivityTag != null) {
                Log.d("PassportChipScan", "📡 Found NFC tag from StandaloneAppActivity - processing...")
                Log.d("PassportChipScan", "📋 Tag ID: ${mainActivityTag.id.joinToString("") { "%02x".format(it) }}")
                
                // Clear the tag from StandaloneAppActivity
                com.artiusid.StandaloneAppActivity.currentNfcTag = null
                
                // Process the tag
                processNfcTag(mainActivityTag)
                break
            }
            kotlinx.coroutines.delay(500) // Check every 500ms
            Log.d("PassportChipScan", "⏳ Still waiting for NFC tag...")
            }
            
            Log.d("PassportChipScan", "🔚 NFC tag monitoring loop ended. State: $nfcScanState")
        }
    }

    // Handle actual NFC tag when detected with timeout - use retryCount to allow retries
    LaunchedEffect(lastNfcTag, retryCount) {
        lastNfcTag?.let { tag ->
            scope.launch {
                try {
                    Log.d("PassportChipScan", "📡 NFC tag detected! Starting authentication...")
                    
                    // Provide feedback for NFC attempt
                    provideNFCFeedback(context, isSuccess = false)
                    
                    nfcScanState = NFCScanState.Connecting
                    
                    kotlinx.coroutines.delay(500)
                    nfcScanState = NFCScanState.Authenticating
                    
                    Log.d("PassportChipScan", "🔐 Attempting BAC authentication with corrected MRZ key...")
                    Log.d("PassportChipScan", "   Using auth key: $authKey")
                    
                    // ✅ REMOVED iOS MRZ key generation - Use raw authKey directly for JMRTD
                    Log.d("PassportChipScan", "🔑 Using RAW MRZ components for JMRTD: '$authKey'")
                    
                    kotlinx.coroutines.delay(1000)
                    nfcScanState = NFCScanState.ReadingData
                    
                    Log.d("PassportChipScan", "📖 Reading passport data from NFC chip...")
                    
                    // Use JMRTD NFC reading with timeout
                    val passportData = try {
                        // Add timeout to prevent lockups
                        kotlinx.coroutines.withTimeout(30000) { // 30 second timeout
                            Log.d("PassportChipScan", "🔍 Using RAW authKey for JMRTD: '$authKey'")
                            readPassportBasic(tag, authKey)
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        Log.e("PassportChipScan", "❌ NFC reading timed out after 30 seconds")
                        throw Exception("NFC reading timed out. Please try again.")
                    } catch (e: Exception) {
                        Log.e("PassportChipScan", "NFC reading failed: ${e.message}", e)
                        throw e
                    }
                    
                    if (passportData != null) {
                        Log.d("PassportChipScan", "✅ NFC chip reading successful!")
                        Log.d("PassportChipScan", "   Document: ${passportData.documentNumber}")
                        Log.d("PassportChipScan", "   Name: ${passportData.firstName} ${passportData.lastName}")
                        Log.d("PassportChipScan", "   BAC Status: ${passportData.bacStatus}")
                        
                        // Provide success feedback
                        provideNFCFeedback(context, isSuccess = true)
                        
                        // Store NFC passport data for verification results (overrides OCR data with chip data)
                        val utilPassportData = com.artiusid.sdk.utils.PassportData(
                            firstName = passportData.firstName?.takeIf { it.isNotBlank() },
                            lastName = passportData.lastName?.takeIf { it.isNotBlank() },
                            documentNumber = passportData.documentNumber?.takeIf { it.isNotBlank() },
                            nationality = passportData.nationality?.takeIf { it.isNotBlank() },
                            dateOfBirth = passportData.dateOfBirth?.takeIf { it.isNotBlank() },
                            dateOfExpiry = passportData.documentExpiryDate?.takeIf { it.isNotBlank() }
                        )
                        com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(utilPassportData)
                        Log.d("PassportChipScan", "📝 Stored NFC passport data: ${utilPassportData.firstName} ${utilPassportData.lastName}")
                        
                        nfcScanState = NFCScanState.Success(passportData)
                        kotlinx.coroutines.delay(1000)
                        onChipScanComplete(passportData)
                    } else {
                        Log.w("PassportChipScan", "⚠️ NFC reading returned null - authentication may have failed")
                        
                        // Check if we've reached max retry attempts (3 failures)
                        if (retryCount >= 2) { // 0, 1, 2 = 3 attempts total
                            Log.d("PassportChipScan", "🔄 Max retry attempts reached (3). Auto-skipping chip scan...")
                            
                            // Ensure OCR passport data is preserved when NFC fails
                            val mrzData = ImageStorage.getPassportMRZData()
                            if (mrzData != null) {
                                Log.d("PassportChipScan", "📝 Preserving OCR passport data for verification results...")
                                val utilPassportData = com.artiusid.sdk.utils.PassportData(
                                    firstName = mrzData.givenNames?.takeIf { it.isNotBlank() },
                                    lastName = mrzData.surname?.takeIf { it.isNotBlank() },
                                    documentNumber = mrzData.passportNumber?.takeIf { it.isNotBlank() },
                                    nationality = mrzData.nationality?.takeIf { it.isNotBlank() },
                                    dateOfBirth = mrzData.dateOfBirth?.takeIf { it.isNotBlank() },
                                    dateOfExpiry = mrzData.dateOfExpiry?.takeIf { it.isNotBlank() }
                                )
                                com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(utilPassportData)
                                Log.d("PassportChipScan", "📝 Stored OCR passport data for verification: ${utilPassportData.firstName} ${utilPassportData.lastName}")
                            }
                            
                            onChipScanComplete(null) // Skip chip scan and continue
                        } else {
                            nfcScanState = NFCScanState.Error("Failed to read passport chip. Keep passport steady on NFC area during entire scan process. Try again.")
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("PassportChipScan", "❌ NFC chip reading error: ${e.message}", e)
                    
                    // CRITICAL: Clear stale IsoDep connection on error to prevent lockups
                    com.artiusid.StandaloneAppActivity.currentIsoDep?.let { isoDep ->
                        try {
                            Log.d("PassportChipScan", "🧹 Closing stale IsoDep connection after error...")
                            isoDep.close()
                        } catch (cleanupException: Exception) {
                            Log.w("PassportChipScan", "⚠️ Error closing IsoDep after failure: ${cleanupException.message}")
                        }
                    }
                    com.artiusid.StandaloneAppActivity.currentIsoDep = null
                    
                    // Check if we've reached max retry attempts (3 failures)
                    if (retryCount >= 2) { // 0, 1, 2 = 3 attempts total
                        Log.d("PassportChipScan", "🔄 Max retry attempts reached (3). Auto-skipping chip scan...")
                        
                        // Ensure OCR passport data is preserved when NFC fails
                        val mrzData = ImageStorage.getPassportMRZData()
                        if (mrzData != null) {
                            Log.d("PassportChipScan", "📝 Preserving OCR passport data for verification results...")
                            val utilPassportData = com.artiusid.sdk.utils.PassportData(
                                firstName = mrzData.givenNames?.takeIf { it.isNotBlank() },
                                lastName = mrzData.surname?.takeIf { it.isNotBlank() },
                                documentNumber = mrzData.passportNumber?.takeIf { it.isNotBlank() },
                                nationality = mrzData.nationality?.takeIf { it.isNotBlank() },
                                dateOfBirth = mrzData.dateOfBirth?.takeIf { it.isNotBlank() },
                                dateOfExpiry = mrzData.dateOfExpiry?.takeIf { it.isNotBlank() }
                            )
                            com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(utilPassportData)
                            Log.d("PassportChipScan", "📝 Stored OCR passport data for verification: ${utilPassportData.firstName} ${utilPassportData.lastName}")
                        }
                        
                        onChipScanComplete(null) // Skip chip scan and continue
                    } else {
                        val errorMessage = when {
                            e.message?.contains("Tag was lost") == true -> 
                                "Passport moved during scan! Keep passport steady on NFC area for entire process. Try again."
                            e.message?.contains("BAC failed") == true -> 
                                "Authentication failed. MRZ data may not match passport chip. Try scanning MRZ again."
                            else -> 
                                "NFC reading failed: ${e.localizedMessage ?: "Unknown error"}. Try again."
                        }
                        nfcScanState = NFCScanState.Error(errorMessage)
                    }
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // NFC Status Icon
            NFCStatusIcon(nfcScanState)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title and Status
            NFCStatusContent(nfcScanState, retryCount, maxRetries)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress indicator (only for active states)
            if (nfcScanState !is NFCScanState.Error && nfcScanState !is NFCScanState.Success) {
                NFCProgressIndicator(nfcScanState)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            NFCActionButtons(
                nfcScanState = nfcScanState,
                retryCount = retryCount,
                maxRetries = maxRetries,
                onRetry = { 
                    retryNfcScan()
                },
                onComplete = { onChipScanComplete(null) }
            )
        }
        
        // Instructions overlay
        if (nfcScanState is NFCScanState.WaitingForNFC) {
            NFCInstructionsOverlay()
        }
    }
}

@Composable
private fun NFCStatusIcon(state: NFCScanState) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_scan")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nfc_scale"
    )
    
    val (icon, color) = when (state) {
        is NFCScanState.Initial, 
        is NFCScanState.WaitingForNFC,
        is NFCScanState.Connecting,
        is NFCScanState.Authenticating,
        is NFCScanState.ReadingData -> Icons.Default.Nfc to com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
        is NFCScanState.Success -> Icons.Default.CheckCircle to com.artiusid.sdk.ui.theme.ThemedStatusColors.getSuccessColor()
        is NFCScanState.Error -> Icons.Default.Error to com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor()
    }
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(
                color = com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().surface,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (state is NFCScanState.WaitingForNFC || 
                    state is NFCScanState.Connecting ||
                    state is NFCScanState.Authenticating ||
                    state is NFCScanState.ReadingData) {
                    Modifier.scale(scale)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "NFC Status",
            tint = color,
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
private fun NFCStatusContent(state: NFCScanState) {
    val (title, description, titleColor) = when (state) {
        is NFCScanState.Initial -> Triple(
            "Preparing NFC", 
            "Initializing passport chip reader...", 
            com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
        )
        is NFCScanState.WaitingForNFC -> Triple(
            "Ready to Scan", 
            "Hold your device near the passport chip", 
            com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
        )
        is NFCScanState.Connecting -> Triple(
            "Connecting", 
            "Establishing connection with passport chip...", 
            com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
        )
        is NFCScanState.Authenticating -> Triple(
            "Authenticating", 
            "Keep passport steady on NFC area! Do not move until complete.", 
            com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
        )
        is NFCScanState.ReadingData -> Triple(
            "Reading Data", 
            "Keep passport steady! Reading passport data from chip...", 
            com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
        )
        is NFCScanState.Success -> Triple(
            "Scan Complete!", 
            "Passport chip data successfully read", 
            LightGreen900
        )
        is NFCScanState.Error -> Triple(
            "Scan Failed", 
            state.message, 
            com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor()
        )
    }
    
    Text(
        text = title,
        color = titleColor,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = description,
        color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        lineHeight = 24.sp
    )
}

@Composable
private fun NFCStatusContent(state: NFCScanState, retryCount: Int, maxRetries: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (title, description) = when (state) {
            is NFCScanState.Initial -> "NFC Passport Reading" to "Initializing NFC scanner..."
            is NFCScanState.WaitingForNFC -> "Ready to Scan" to "Hold your passport against the back of your phone"
            is NFCScanState.Connecting -> "Connecting" to "Establishing connection to passport chip..."
            is NFCScanState.Authenticating -> "Authenticating" to if (retryCount > 0) "Authenticating... (Attempt ${retryCount + 1}/$maxRetries)" else "Authenticating with passport chip..."
            is NFCScanState.ReadingData -> "Reading Data" to "Reading passport information from chip..."
            is NFCScanState.Success -> "Success!" to "Passport data read successfully"
            is NFCScanState.Error -> {
                val errorTitle = if (retryCount >= maxRetries) "NFC Reading Failed" else "Scan Failed"
                val errorDesc = if (retryCount >= maxRetries) {
                    "Unable to read passport after $maxRetries attempts. Please try again or contact support."
                } else {
                    state.message
                }
                errorTitle to errorDesc
            }
        }
        
        Text(
            text = title,
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = description,
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun NFCProgressIndicator(state: NFCScanState) {
    val progress = when (state) {
        is NFCScanState.Initial -> 0.1f
        is NFCScanState.WaitingForNFC -> 0.2f
        is NFCScanState.Connecting -> 0.4f
        is NFCScanState.Authenticating -> 0.6f
        is NFCScanState.ReadingData -> 0.8f
        else -> 1.0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
        trackColor = com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().surface
    )
}

@Composable
private fun NFCActionButtons(
    nfcScanState: NFCScanState,
    retryCount: Int,
    maxRetries: Int,
    onRetry: () -> Unit,
    onComplete: () -> Unit
) {
    when (nfcScanState) {
        is NFCScanState.Error -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show retry count if we've attempted
                if (retryCount > 0) {
                    Text(
                        text = "Attempt $retryCount of $maxRetries failed",
                        color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Try Again button (if we haven't exceeded max retries)
                if (retryCount < maxRetries) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                            contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonTextColor()
                        )
                    ) {
                        Text(
                            text = "Try Again (${retryCount + 1}/$maxRetries)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        is NFCScanState.Success -> {
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGreen900,
                    contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getSecondaryButtonTextColor()
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        is NFCScanState.WaitingForNFC -> {
            // No skip button - user must complete NFC scan
        }
        else -> {
            // No buttons for active scanning states (Connecting, Authenticating, ReadingData)
        }
    }
}

@Composable
private fun NFCInstructionsOverlay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().surface.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📱 NFC Instructions",
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Make sure NFC is enabled on your device\n• Hold your device flat against the passport\n• Keep steady until scan completes",
                    color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Mock tag creation for testing - simplified approach
private fun createMockTag(): Tag? {
    // For testing purposes, we'll return null and handle it in the calling code
    // In a real implementation, this would be a real NFC tag from an intent
    return null
}