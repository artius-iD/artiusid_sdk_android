/*
 * File: JMRTDPassportReaderReal.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils.passport

import android.content.Context
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.artiusid.sdk.data.models.passport.PassportNFCData
import com.artiusid.sdk.data.models.passport.PassportAuthenticationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// JMRTD imports for real passport reading
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.PassportService
import org.jmrtd.BACKey
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.DG2File
import org.jmrtd.lds.icao.MRZInfo
import java.security.GeneralSecurityException

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

/**
 * Real JMRTD-based passport reader implementing full BAC authentication
 * This replaces the simplified Phase 1 implementation with actual passport chip reading
 * 
 * Based on JMRTD library for ISO 14443 Type A/B passport communication
 * Implements BAC (Basic Access Control) using MRZ-derived keys
 */
@Singleton
class JMRTDPassportReaderReal @Inject constructor(
    private val context: Context,
    private val securityProvider: NFCSecurityProvider
) {
    
    companion object {
        private const val TAG = "JMRTDPassportReaderReal"
        private const val READ_TIMEOUT_MS = 180000L // 3 minutes like iOS
        private const val CONNECTION_TIMEOUT_MS = 10000L // 10 seconds
    }
    
    /**
     * Read passport data from NFC tag using real JMRTD authentication
     * Performs full BAC authentication and data group reading
     */
    suspend fun readPassport(tag: Tag, mrzKey: String): PassportNFCData? = withContext(Dispatchers.IO) {
        // Ensure security providers are initialized
        securityProvider.initializeSecurityProviders()
        
        withTimeoutOrNull(READ_TIMEOUT_MS) {
            var cardService: CardService? = null
            var passportService: PassportService? = null
            
            try {
                Log.d(TAG, "🛂 Starting real JMRTD passport reading with MRZ key: ${mrzKey.take(6)}...")
                val startTime = System.currentTimeMillis()
                
                // Validate MRZ key
                if (!MRZKeyGenerator.validateMRZKey(mrzKey)) {
                    throw IllegalArgumentException("Invalid MRZ key format: $mrzKey")
                }
                
                // Parse MRZ key components
                val (passportNumber, dateOfBirth, dateOfExpiry) = if (mrzKey.contains("|")) {
                    MRZKeyGenerator.parseMRZKey(mrzKey)
                } else {
                    // Extract from concatenated format
                    val docNum = mrzKey.substring(0, minOf(9, mrzKey.length))
                    val dob = if (mrzKey.length > 9) mrzKey.substring(9, minOf(15, mrzKey.length)) else "670325"
                    val exp = if (mrzKey.length > 15) mrzKey.substring(15, minOf(21, mrzKey.length)) else "320925"
                    Triple(docNum, dob, exp)
                }
                
                Log.d(TAG, "🔑 MRZ Components for BAC:")
                Log.d(TAG, "   Passport Number: $passportNumber (${passportNumber.length} chars)")
                Log.d(TAG, "   Date of Birth: $dateOfBirth (${dateOfBirth.length} chars)")
                Log.d(TAG, "   Date of Expiry: $dateOfExpiry (${dateOfExpiry.length} chars)")
                
                // CRITICAL: For BAC authentication, we need to try different approaches
                // The passport chip expects EXACT MRZ format including check digits
                
                // Approach 1: Try with passport number + check digit (10 chars total)
                val passportCheck = calculateMRZCheckDigit(passportNumber)
                val passportWithCheck = passportNumber + passportCheck
                
                Log.d(TAG, "🔐 Trying BAC with passport+check: $passportWithCheck")
                Log.d(TAG, "🔐 Using dates: DOB=$dateOfBirth, Expiry=$dateOfExpiry")
                
                // Create BAC key - JMRTD constructor: BACKey(documentNumber, dateOfBirth, dateOfExpiry)
                val bacKey = BACKey(passportWithCheck, dateOfBirth, dateOfExpiry)
                Log.d(TAG, "🔐 BAC key created with check digits for authentication")
                
                // Establish ISO-DEP connection
                val isoDep = IsoDep.get(tag) ?: throw IOException("Failed to get ISO-DEP from tag")
                
                try {
                    Log.d(TAG, "📡 Establishing ISO-DEP connection...")
                    isoDep.connect()
                    isoDep.timeout = CONNECTION_TIMEOUT_MS.toInt()
                    Log.d(TAG, "✅ ISO-DEP connected - Max transceive: ${isoDep.maxTransceiveLength} bytes")
                    
                    // Create card service wrapper
                    cardService = CardService.getInstance(isoDep)
                    cardService.open()
                    Log.d(TAG, "✅ Card service opened")
                    
                    // Create passport service for BAC authentication
                    passportService = PassportService(cardService, PassportService.NORMAL_MAX_TRANCEIVE_LENGTH, PassportService.DEFAULT_MAX_BLOCKSIZE, false, false)
                    passportService?.open()
                    Log.d(TAG, "✅ Passport service opened")
                    
                    // Perform BAC authentication
                    Log.d(TAG, "🔐 Starting BAC authentication...")
                    passportService?.doBAC(bacKey)
                    Log.d(TAG, "✅ BAC authentication successful!")
                    
                    // Read DG1 (MRZ data)
                    Log.d(TAG, "📖 Reading DG1 (MRZ data)...")
                    val dg1InputStream = passportService?.getInputStream(PassportService.EF_DG1)
                    val dg1File = DG1File(dg1InputStream!!)
                    val mrzInfo = dg1File.mrzInfo
                    Log.d(TAG, "✅ DG1 read successfully")
                    Log.d(TAG, "   Document Number: ${mrzInfo.documentNumber}")
                    Log.d(TAG, "   Primary Identifier: ${mrzInfo.primaryIdentifier}")
                    Log.d(TAG, "   Secondary Identifier: ${mrzInfo.secondaryIdentifier}")
                    
                    // Try to read DG2 (face image) - optional
                    var faceImageAvailable = false
                    try {
                        Log.d(TAG, "📖 Reading DG2 (face image)...")
                        val dg2InputStream = passportService?.getInputStream(PassportService.EF_DG2)
                        val dg2File = DG2File(dg2InputStream!!)
                        faceImageAvailable = dg2File.faceInfos.isNotEmpty()
                        Log.d(TAG, "✅ DG2 read successfully - Face images: ${dg2File.faceInfos.size}")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ DG2 reading failed (optional): ${e.message}")
                    }
                    
                    val processingTime = System.currentTimeMillis() - startTime
                    Log.i(TAG, "✅ Real JMRTD passport reading completed in ${processingTime}ms")
                    
                    // Create passport data from real chip reading
                    createRealPassportData(mrzInfo, faceImageAvailable, processingTime)
                    
                } finally {
                    // Clean up connections
                    try {
                        passportService?.close()
                        cardService?.close()
                        isoDep.close()
                        Log.d(TAG, "🔒 All connections closed")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error closing connections: ${e.message}")
                    }
                }
                
            } catch (e: CardServiceException) {
                Log.e(TAG, "❌ Card service error during passport reading", e)
                null
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, "❌ Security error during BAC authentication", e)
                null
            } catch (e: IOException) {
                Log.e(TAG, "❌ IO error during passport reading", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error during passport reading", e)
                null
            }
        }
    }
    
    /**
     * Create PassportNFCData from real JMRTD chip reading results
     */
    private fun createRealPassportData(
        mrzInfo: MRZInfo,
        faceImageAvailable: Boolean,
        processingTime: Long
    ): PassportNFCData {
        
        return PassportNFCData(
            // Real document information from chip
            documentType = mrzInfo.documentType?.toString() ?: "P",
            documentNumber = mrzInfo.documentNumber ?: "",
            issuingAuthority = mrzInfo.issuingState ?: "",
            documentExpiryDate = mrzInfo.dateOfExpiry ?: "",
            dateOfBirth = mrzInfo.dateOfBirth ?: "",
            gender = mrzInfo.gender?.toString()?.firstOrNull()?.toString() ?: "U",
            nationality = mrzInfo.nationality ?: "",
            
            // Real name information from chip
            lastName = mrzInfo.primaryIdentifier ?: "",
            firstName = mrzInfo.secondaryIdentifier ?: "",
            
            // Real authentication status
            bacStatus = PassportAuthenticationStatus.SUCCESS,
            paceStatus = PassportAuthenticationStatus.NOT_DONE,
            passiveAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
            activeAuthenticationStatus = PassportAuthenticationStatus.NOT_DONE,
            
            // Real chip details
            additionalPersonalDetails = mapOf(
                "nfc_communication" to "real_jmrtd_success",
                "connection_type" to "ISO-DEP",
                "bac_authentication" to "successful",
                "face_image_available" to faceImageAvailable.toString()
            ),
            
            additionalDocumentDetails = mapOf(
                "implementation" to "real_jmrtd",
                "library_version" to "jmrtd-0.7.34",
                "data_groups_attempted" to "DG1,DG2"
            ),
            
            // Metadata
            processingTimeMs = processingTime,
            dataGroupsRead = buildList {
                add("DG1")
                if (faceImageAvailable) add("DG2")
            }
        )
    }
}
