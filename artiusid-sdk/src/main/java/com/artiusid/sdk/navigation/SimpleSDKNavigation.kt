package com.artiusid.sdk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.models.*
import com.artiusid.sdk.ui.screens.*

/**
 * Simplified SDK Navigation using SimpleScreens
 * This provides a working navigation flow while we fix the detailed screens
 */

sealed class SimpleSDKScreen(val route: String) {
    object Splash : SimpleSDKScreen("splash")
    object Home : SimpleSDKScreen("home")
    object VerificationSteps : SimpleSDKScreen("verification_steps")
    object FaceScanIntro : SimpleSDKScreen("face_scan_intro")
    object FaceScan : SimpleSDKScreen("face_scan")
    object SelectDocumentType : SimpleSDKScreen("select_document_type")
    object DocumentScanIntro : SimpleSDKScreen("document_scan_intro")
    object DocumentScan : SimpleSDKScreen("document_scan")
    object DocumentScanBackIntro : SimpleSDKScreen("document_scan_back_intro")
    object DocumentScanBack : SimpleSDKScreen("document_scan_back")
    object PassportScanIntro : SimpleSDKScreen("passport_scan_intro")
    object PassportScan : SimpleSDKScreen("passport_scan")
    object PassportChipIntro : SimpleSDKScreen("passport_chip_intro")
    object PassportChipScan : SimpleSDKScreen("passport_chip_scan")
    object VerificationProcessing : SimpleSDKScreen("verification_processing")
    object VerificationResults : SimpleSDKScreen("verification_results")
    object VerificationFailure : SimpleSDKScreen("verification_failure")
    object Authentication : SimpleSDKScreen("authentication")
    object Authenticated : SimpleSDKScreen("authenticated")
}

@Composable
fun SimpleSDKNavigation(
    navController: NavHostController,
    flowType: String = "verification"
) {
    val startDestination = if (flowType == "authentication") {
        SimpleSDKScreen.Authentication.route
    } else {
        SimpleSDKScreen.FaceScanIntro.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        
        // VERIFICATION FLOW
        composable(SimpleSDKScreen.FaceScanIntro.route) {
            FaceScanIntroScreen(
                onContinue = {
                    navController.navigate(SimpleSDKScreen.FaceScan.route)
                },
                onBack = {
                    ArtiusIDSDK.verificationCallback?.onVerificationCancelled()
                }
            )
        }

        composable(SimpleSDKScreen.FaceScan.route) {
            FaceScanScreen(
                onFaceScanComplete = { livenessResult ->
                    navController.navigate(SimpleSDKScreen.SelectDocumentType.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { error ->
                    navController.navigate(SimpleSDKScreen.VerificationFailure.route)
                }
            )
        }

        composable(SimpleSDKScreen.SelectDocumentType.route) {
            SelectDocumentTypeScreen(
                onDocumentTypeSelected = { documentType ->
                    navController.navigate(SimpleSDKScreen.DocumentScanIntro.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SimpleSDKScreen.DocumentScanIntro.route) {
            DocumentScanIntroScreen(
                onContinue = {
                    navController.navigate(SimpleSDKScreen.DocumentScan.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SimpleSDKScreen.DocumentScan.route) {
            DocumentScanScreen(
                documentType = "government_id", // Default for now
                onDocumentScanned = { documentResult ->
                    navController.navigate(SimpleSDKScreen.VerificationProcessing.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { error ->
                    navController.navigate(SimpleSDKScreen.VerificationFailure.route)
                }
            )
        }

        composable(SimpleSDKScreen.VerificationProcessing.route) {
            VerificationProcessingScreen(
                onProcessingComplete = { verificationResult ->
                    navController.navigate(SimpleSDKScreen.VerificationResults.route)
                },
                onError = { error ->
                    navController.navigate(SimpleSDKScreen.VerificationFailure.route)
                }
            )
        }

        composable(SimpleSDKScreen.VerificationResults.route) {
            VerificationResultsScreen(
                onComplete = { result ->
                    ArtiusIDSDK.verificationCallback?.onVerificationComplete(result)
                },
                onRetry = {
                    navController.navigate(SimpleSDKScreen.FaceScanIntro.route)
                }
            )
        }

        composable(SimpleSDKScreen.VerificationFailure.route) {
            VerificationFailureScreen(
                failureType = "GENERAL_ERROR",
                errorReason = "Verification failed",
                onRetry = {
                    navController.navigate(SimpleSDKScreen.FaceScanIntro.route)
                },
                onCancel = {
                    ArtiusIDSDK.verificationCallback?.onVerificationCancelled()
                }
            )
        }

        // AUTHENTICATION FLOW
        composable(SimpleSDKScreen.Authentication.route) {
            AuthenticationScreen(
                onAuthenticationComplete = { authResult ->
                    navController.navigate(SimpleSDKScreen.Authenticated.route)
                },
                onBack = {
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
                },
                onError = { error ->
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationError(
                        SDKError(SDKErrorCode.AUTHENTICATION_FAILED, error)
                    )
                }
            )
        }

        composable(SimpleSDKScreen.Authenticated.route) {
            AuthenticatedScreen(
                onComplete = { result ->
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                }
            )
        }
    }
}
