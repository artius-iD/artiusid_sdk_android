package com.artiusid.sdk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.models.*
import com.artiusid.sdk.utils.VerificationDataHolder
import com.artiusid.sdk.utils.ImageStorage

// Import the working SimpleScreens
import com.artiusid.sdk.ui.screens.FaceScanIntroScreen
import com.artiusid.sdk.ui.screens.FaceScanScreen
import com.artiusid.sdk.ui.screens.SelectDocumentTypeScreen
import com.artiusid.sdk.ui.screens.DocumentScanIntroScreen
import com.artiusid.sdk.ui.screens.DocumentScanScreen
import com.artiusid.sdk.ui.screens.DocumentScanBackIntroScreen
import com.artiusid.sdk.ui.screens.PassportScanIntroScreen
import com.artiusid.sdk.ui.screens.PassportScanScreen
import com.artiusid.sdk.ui.screens.PassportChipIntroScreen
import com.artiusid.sdk.ui.screens.PassportChipScanScreen
import com.artiusid.sdk.ui.screens.VerificationProcessingScreen
import com.artiusid.sdk.ui.screens.VerificationResultsScreen
import com.artiusid.sdk.ui.screens.VerificationFailureScreen
import com.artiusid.sdk.ui.screens.AuthenticationScreen
import com.artiusid.sdk.ui.screens.AuthenticatedScreen

sealed class SDKScreen(val route: String) {
    object Splash : SDKScreen("splash")
    object Home : SDKScreen("home")
    object VerificationSteps : SDKScreen("verification_steps")
    object FaceScanIntro : SDKScreen("face_scan_intro")
    object FaceScan : SDKScreen("face_scan")
    object SelectDocumentType : SDKScreen("select_document_type")
    object DocumentScanIntro : SDKScreen("document_scan_intro")
    object DocumentScan : SDKScreen("document_scan/{documentType}") {
        fun createRoute(documentType: String) = "document_scan/$documentType"
    }
    object DocumentScanBackIntro : SDKScreen("document_scan_back_intro")
    object DocumentScanBack : SDKScreen("document_scan_back")
    object PassportScanIntro : SDKScreen("passport_scan_intro")
    object PassportScan : SDKScreen("passport_scan")
    object PassportChipIntro : SDKScreen("passport_chip_intro")
    object PassportChipScan : SDKScreen("passport_chip_scan")
    object VerificationProcessing : SDKScreen("verification_processing")
    object VerificationResults : SDKScreen("verification_results")
    object VerificationFailure : SDKScreen("verification_failure/{failureType}/{errorReason}") {
        fun createRoute(failureType: String, errorReason: String) = "verification_failure/$failureType/$errorReason"
    }
    object Authentication : SDKScreen("authentication")
    object Authenticated : SDKScreen("authenticated")
}

@Composable
fun SDKNavigation(
    navController: NavHostController,
    flowType: String
) {
    val startDestination = if (flowType == "authentication") SDKScreen.Authentication.route else SDKScreen.FaceScanIntro.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(SDKScreen.FaceScanIntro.route) {
            FaceScanIntroScreen(
                onContinue = {
                    navController.navigate(SDKScreen.FaceScan.route)
                },
                onBack = {
                    ArtiusIDSDK.verificationCallback?.onVerificationBackled()
                }
            )
        }

        composable(SDKScreen.FaceScan.route) {
            FaceScanScreen(
                onFaceScanComplete = { livenessResult ->
                    VerificationDataHolder.setLivenessResult(livenessResult)
                    ImageStorage.setFaceImage(livenessResult.faceImage)
                    navController.navigate(SDKScreen.SelectDocumentType.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.FACE_LIVENESS_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.SelectDocumentType.route) {
            SelectDocumentTypeScreen(
                onDocumentTypeSelected = { documentType ->
                    when (documentType) {
                        "id" -> navController.navigate(SDKScreen.DocumentScan.createRoute("id"))
                        "passport" -> navController.navigate(SDKScreen.PassportScanIntro.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.DocumentScan.route) { backStackEntry ->
            val documentType = backStackEntry.arguments?.getString("documentType") ?: "id"
            DocumentScanScreen(
                documentType = documentType,
                onDocumentScanned = { documentScanResult ->
                    VerificationDataHolder.setDocumentScanResult(documentScanResult)
                    ImageStorage.setDocumentFrontImage(documentScanResult.frontImage)
                    ImageStorage.setDocumentBackImage(documentScanResult.backImage)
                    if (documentType == "id") {
                        navController.navigate(SDKScreen.DocumentScanBackIntro.route)
                    } else {
                        navController.navigate(SDKScreen.VerificationProcessing.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.DOCUMENT_SCAN_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.DocumentScanBackIntro.route) {
            DocumentScanBackIntroScreen(
                onContinue = {
                    navController.navigate(SDKScreen.DocumentScanBack.route)
                },
                onSkip = {
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.DocumentScanBack.route) {
            DocumentScanScreen(
                documentType = "id_back",
                onDocumentScanned = { documentScanResult ->
                    VerificationDataHolder.setDocumentBackScanResult(documentScanResult)
                    ImageStorage.setDocumentBackImage(documentScanResult.frontImage)
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.DOCUMENT_SCAN_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.PassportScanIntro.route) {
            PassportScanIntroScreen(
                onContinue = {
                    navController.navigate(SDKScreen.PassportScan.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.PassportScan.route) {
            PassportScanScreen(
                onPassportScanned = { documentScanResult ->
                    VerificationDataHolder.setPassportScanResult(documentScanResult)
                    ImageStorage.setPassportImage(documentScanResult.frontImage)
                    navController.navigate(SDKScreen.PassportChipIntro.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.DOCUMENT_SCAN_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.PassportChipIntro.route) {
            PassportChipIntroScreen(
                onContinue = {
                    navController.navigate(SDKScreen.PassportChipScan.route)
                },
                onSkip = {
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.PassportChipScan.route) {
            PassportChipScanScreen(
                onNfcComplete = { nfcResult ->
                    VerificationDataHolder.setNfcPassportResult(nfcResult)
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.NFC_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.VerificationProcessing.route) {
            VerificationProcessingScreen(
                onProcessingComplete = { result ->
                    if (result.success) {
                        navController.navigate(SDKScreen.VerificationResults.route)
                    } else {
                        val failureType = "GENERAL" // Default failure type
                        val errorReason = "Verification processing failed"
                        navController.navigate(SDKScreen.VerificationFailure.createRoute(failureType, errorReason))
                    }
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.verificationCallback?.onVerificationError(SDKError(SDKErrorCode.PROCESSING_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.VerificationResults.route) {
            VerificationResultsScreen(
                onComplete = { result ->
                    VerificationDataHolder.clearVerificationData()
                    ArtiusIDSDK.verificationCallback?.onVerificationComplete(result)
                },
                onRetry = {
                    navController.navigate(SDKScreen.FaceScanIntro.route)
                }
            )
        }

        composable(SDKScreen.VerificationFailure.route) { backStackEntry ->
            val failureType = backStackEntry.arguments?.getString("failureType") ?: "GENERAL"
            val errorReason = backStackEntry.arguments?.getString("errorReason") ?: "An error occurred during verification."
            VerificationFailureScreen(
                failureType = failureType,
                errorReason = errorReason,
                onRetry = {
                    when (failureType) {
                        DocumentRecaptureType.FACE_TOO_BLURRY.name,
                        DocumentRecaptureType.FACE_NOT_DETECTED.name,
                        DocumentRecaptureType.FACE_TOO_DARK.name,
                        DocumentRecaptureType.FACE_TOO_BRIGHT.name,
                        DocumentRecaptureType.FACE_TOO_FAR.name,
                        DocumentRecaptureType.FACE_TOO_CLOSE.name,
                        DocumentRecaptureType.FACE_ANGLE_INCORRECT.name -> navController.navigate(SDKScreen.FaceScan.route)
                        DocumentRecaptureType.DOCUMENT_TOO_BLURRY.name,
                        DocumentRecaptureType.DOCUMENT_GLARE.name,
                        DocumentRecaptureType.DOCUMENT_CROPPED.name,
                        DocumentRecaptureType.DOCUMENT_NOT_DETECTED.name,
                        DocumentRecaptureType.DOCUMENT_ANGLE_INCORRECT.name -> navController.navigate(SDKScreen.DocumentScan.createRoute("id"))
                        else -> navController.navigate(SDKScreen.FaceScanIntro.route)
                    }
                },
                onCancel = {
                    ArtiusIDSDK.verificationCallback?.onVerificationBackled()
                }
            )
        }

        // ===== AUTHENTICATION FLOW =====
        composable(SDKScreen.Authentication.route) {
            AuthenticationScreen(
                onAuthenticationComplete = { result ->
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                },
                onBack = {
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationBackled()
                },
                onError = { errorMessage ->
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationError(SDKError(SDKErrorCode.AUTHENTICATION_FAILED, errorMessage))
                }
            )
        }

        composable(SDKScreen.Authenticated.route) {
            AuthenticatedScreen(
                onComplete = { result ->
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                }
            )
        }
    }
}