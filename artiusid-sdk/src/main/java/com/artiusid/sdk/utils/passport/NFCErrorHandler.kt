/*
 * File: NFCErrorHandler.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// NFCErrorHandler.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.utils.passport

import android.content.Context
import android.nfc.NfcAdapter
import android.util.Log

/**
 * Comprehensive error handling for NFC passport reading operations
 */
class NFCErrorHandler(private val context: Context) {

    companion object {
        private const val TAG = "NFCErrorHandler"
    }

    enum class NFCErrorType {
        NFC_NOT_SUPPORTED,
        NFC_DISABLED,
        TAG_LOST,
        AUTHENTICATION_FAILED,
        COMMUNICATION_ERROR,
        TIMEOUT,
        INVALID_MRZ,
        UNSUPPORTED_PASSPORT,
        CHIP_ERROR,
        UNKNOWN_ERROR
    }

    data class NFCError(
        val type: NFCErrorType,
        val message: String,
        val userMessage: String,
        val canRetry: Boolean,
        val suggestedAction: String
    )

    /**
     * Analyze exception and return appropriate error information
     */
    fun analyzeError(exception: Throwable): NFCError {
        Log.e(TAG, "Analyzing NFC error: ${exception.message}", exception)
        
        return when {
            exception.message?.contains("NFC not supported", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.NFC_NOT_SUPPORTED,
                    message = "NFC is not supported on this device",
                    userMessage = "This device doesn't support NFC technology required for passport chip reading.",
                    canRetry = false,
                    suggestedAction = "Use a device with NFC support or skip chip scanning"
                )
            }
            
            exception.message?.contains("NFC disabled", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.NFC_DISABLED,
                    message = "NFC is disabled in device settings",
                    userMessage = "NFC is turned off. Please enable NFC in your device settings.",
                    canRetry = true,
                    suggestedAction = "Go to Settings > NFC and enable NFC"
                )
            }
            
            exception.message?.contains("tag lost", ignoreCase = true) == true ||
            exception.message?.contains("connection lost", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.TAG_LOST,
                    message = "Lost connection to passport chip",
                    userMessage = "Connection to passport chip was lost. Keep your device steady against the passport.",
                    canRetry = true,
                    suggestedAction = "Hold device firmly against passport and try again"
                )
            }
            
            exception.message?.contains("authentication", ignoreCase = true) == true ||
            exception.message?.contains("BAC", ignoreCase = true) == true ||
            exception.message?.contains("PACE", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.AUTHENTICATION_FAILED,
                    message = "Failed to authenticate with passport chip",
                    userMessage = "Could not authenticate with passport chip. Please verify passport details are correct.",
                    canRetry = true,
                    suggestedAction = "Ensure passport number, birth date, and expiry date are correct"
                )
            }
            
            exception.message?.contains("timeout", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.TIMEOUT,
                    message = "NFC operation timed out",
                    userMessage = "Reading passport chip took too long. Please try again.",
                    canRetry = true,
                    suggestedAction = "Keep device steady against passport for the entire scan"
                )
            }
            
            exception.message?.contains("MRZ", ignoreCase = true) == true ||
            exception.message?.contains("invalid key", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.INVALID_MRZ,
                    message = "Invalid MRZ data provided",
                    userMessage = "Passport information is invalid. Please rescan the passport.",
                    canRetry = true,
                    suggestedAction = "Rescan passport MRZ area with better lighting"
                )
            }
            
            exception.message?.contains("unsupported", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.UNSUPPORTED_PASSPORT,
                    message = "Unsupported passport type or version",
                    userMessage = "This passport type is not supported for chip reading.",
                    canRetry = false,
                    suggestedAction = "Continue without chip scanning"
                )
            }
            
            exception.message?.contains("chip", ignoreCase = true) == true ||
            exception.message?.contains("APDU", ignoreCase = true) == true -> {
                NFCError(
                    type = NFCErrorType.CHIP_ERROR,
                    message = "Passport chip error",
                    userMessage = "There's an issue with the passport chip. Try again or skip chip scanning.",
                    canRetry = true,
                    suggestedAction = "Try different positioning or skip chip scan"
                )
            }
            
            else -> {
                NFCError(
                    type = NFCErrorType.UNKNOWN_ERROR,
                    message = exception.message ?: "Unknown error occurred",
                    userMessage = "An unexpected error occurred while reading the passport chip.",
                    canRetry = true,
                    suggestedAction = "Try again or skip chip scanning"
                )
            }
        }
    }

    /**
     * Check NFC availability and return error if not available
     */
    fun checkNFCAvailability(): NFCError? {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        
        return when {
            nfcAdapter == null -> {
                NFCError(
                    type = NFCErrorType.NFC_NOT_SUPPORTED,
                    message = "NFC not supported on this device",
                    userMessage = "This device doesn't support NFC technology.",
                    canRetry = false,
                    suggestedAction = "Skip chip scanning or use a different device"
                )
            }
            
            !nfcAdapter.isEnabled -> {
                NFCError(
                    type = NFCErrorType.NFC_DISABLED,
                    message = "NFC is disabled",
                    userMessage = "NFC is turned off. Please enable NFC to scan passport chip.",
                    canRetry = true,
                    suggestedAction = "Enable NFC in device settings"
                )
            }
            
            else -> null // NFC is available and enabled
        }
    }

    /**
     * Get user-friendly error messages for common NFC issues
     */
    fun getRetryInstructions(errorType: NFCErrorType): List<String> {
        return when (errorType) {
            NFCErrorType.TAG_LOST -> listOf(
                "Keep your device flat against the passport",
                "Don't move the device during scanning",
                "Ensure good contact between device and passport",
                "Try a different position on the passport"
            )
            
            NFCErrorType.AUTHENTICATION_FAILED -> listOf(
                "Verify passport number is correct",
                "Check birth date format (YYMMDD)",
                "Confirm expiry date format (YYMMDD)",
                "Ensure passport is not damaged"
            )
            
            NFCErrorType.COMMUNICATION_ERROR -> listOf(
                "Clean the back of your device",
                "Remove any cases or covers",
                "Try different angles and positions",
                "Ensure passport is on a flat surface"
            )
            
            NFCErrorType.TIMEOUT -> listOf(
                "Keep device steady for the entire scan",
                "Don't interrupt the scanning process",
                "Ensure strong NFC signal",
                "Try moving to a different location"
            )
            
            NFCErrorType.NFC_DISABLED -> listOf(
                "Go to Settings > Connected devices",
                "Find NFC settings",
                "Turn on NFC",
                "Return to the app and try again"
            )
            
            else -> listOf(
                "Try repositioning your device",
                "Ensure passport is flat and clean",
                "Check NFC is enabled",
                "Try again or skip chip scanning"
            )
        }
    }

    /**
     * Log error details for debugging
     */
    fun logError(error: NFCError, context: String = "") {
        Log.e(TAG, "NFC Error [$context]: ${error.type} - ${error.message}")
        Log.d(TAG, "User message: ${error.userMessage}")
        Log.d(TAG, "Suggested action: ${error.suggestedAction}")
        Log.d(TAG, "Can retry: ${error.canRetry}")
    }
}
