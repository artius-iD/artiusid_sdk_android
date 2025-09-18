/*
 * File: PassportData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.models.passport

/**
 * Main passport data container combining all passport information
 * Android equivalent of iOS Passport class
 * Manages the complete passport scanning and NFC reading workflow
 */
data class PassportData(
    // Visual scan data
    val passportScan: ScannedPassportImage? = null,
    
    // MRZ data extracted from visual scan
    val passportMRZData: PassportMRZData? = null,
    
    // NFC data read from chip
    val passportRFIDData: PassportNFCData? = null,
    
    // Processing states
    val scanningState: PassportScanningState = PassportScanningState.NOT_STARTED,
    val nfcReadingState: PassportNFCReadingState = PassportNFCReadingState.NOT_STARTED,
    
    // Metadata
    val scanTimestamp: Long? = null,
    val nfcTimestamp: Long? = null,
    val processingErrors: List<String> = emptyList()
) {
    
    /**
     * Check if visual scanning is complete and valid
     */
    fun isVisualScanComplete(): Boolean {
        return passportScan != null && 
               passportMRZData != null && 
               passportMRZData.isValid &&
               scanningState == PassportScanningState.COMPLETED
    }
    
    /**
     * Check if NFC reading is complete
     */
    fun isNFCReadingComplete(): Boolean {
        return passportRFIDData != null && 
               passportRFIDData.isAuthenticated() &&
               nfcReadingState == PassportNFCReadingState.COMPLETED
    }
    
    /**
     * Check if entire passport processing is complete
     */
    fun isComplete(): Boolean {
        return isVisualScanComplete() && isNFCReadingComplete()
    }
    
    /**
     * Get MRZ key for NFC authentication
     */
    fun getMRZKeyForNFC(): String? {
        return passportMRZData?.generateMRZKey()
    }
    
    /**
     * Get the passport image as Base64 for verification submission
     */
    fun getPassportImageBase64(): String? {
        return passportScan?.toBase64JPEG(85)
    }
    
    /**
     * Validate that passport data is ready for verification submission
     */
    fun isReadyForVerification(): Boolean {
        return isComplete() && 
               passportScan?.meetsQualityRequirements() == true &&
               passportRFIDData?.hasEssentialData() == true
    }
    
    /**
     * Get processing summary for debugging
     */
    fun getProcessingSummary(): String {
        return buildString {
            append("Passport Processing Summary:\n")
            append("Visual Scan: $scanningState\n")
            append("NFC Reading: $nfcReadingState\n")
            
            passportMRZData?.let { mrz ->
                append("MRZ Valid: ${mrz.isValid}\n")
                append("Passport #: ${mrz.passportNumber}\n")
                append("Name: ${mrz.getFullName()}\n")
            }
            
            passportRFIDData?.let { nfc ->
                append("NFC Auth: ${nfc.isAuthenticated()}\n")
                append("Face Image: ${nfc.faceImage != null}\n")
                append(nfc.getProcessingSummary())
            }
            
            if (processingErrors.isNotEmpty()) {
                append("\nErrors: ${processingErrors.joinToString(", ")}")
            }
        }
    }
    
    /**
     * Create verification request data
     */
    fun toVerificationRequest(): Map<String, Any>? {
        if (!isReadyForVerification()) return null
        
        val baseData = mutableMapOf<String, Any>()
        
        // Add passport image
        getPassportImageBase64()?.let { imageBase64 ->
            baseData["frontImageBase64"] = imageBase64
            baseData["documentType"] = 2 // Passport type
        }
        
        // Add NFC data
        passportRFIDData?.let { nfcData ->
            baseData.putAll(nfcData.toVerificationData())
        }
        
        // Add MRZ data
        passportMRZData?.let { mrzData ->
            baseData["mrzLine1"] = mrzData.line1
            baseData["mrzLine2"] = mrzData.line2
            baseData["mrzValid"] = mrzData.isValid
        }
        
        return baseData
    }
}

/**
 * State of passport visual scanning process
 */
enum class PassportScanningState {
    NOT_STARTED,
    SCANNING,
    MRZ_DETECTED,
    VALIDATING,
    COMPLETED,
    FAILED
}

/**
 * State of passport NFC reading process
 */
enum class PassportNFCReadingState {
    NOT_STARTED,
    INITIALIZING,
    AUTHENTICATING_BAC,
    AUTHENTICATING_PACE,
    READING_DATAGROUPS,
    COMPLETED,
    FAILED,
    TIMEOUT
}