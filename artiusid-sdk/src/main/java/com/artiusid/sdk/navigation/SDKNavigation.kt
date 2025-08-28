package com.artiusid.sdk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.artiusid.sdk.sdk.ArtiusIDSDK
import com.artiusid.sdk.sdk.models.*
import com.artiusid.sdk.sdk.utils.VerificationDataHolder
import com.artiusid.sdk.sdk.utils.ImageStorage

// Import the EXACT standalone screens (migrated to SDK)
import com.artiusid.sdk.sdk.ui.screens.verification.VerificationStepsScreen
import com.artiusid.sdk.sdk.ui.screens.face.FaceScanIntroScreen
import com.artiusid.sdk.sdk.ui.screens.face.FaceScanScreen
import com.artiusid.sdk.sdk.ui.screens.document.SelectDocumentTypeScreen
import com.artiusid.sdk.sdk.ui.screens.document.DocumentScanIntroScreen
import com.artiusid.sdk.sdk.ui.screens.document.DocumentScanScreen
import com.artiusid.sdk.sdk.ui.screens.document.DocumentScanBackIntroScreen
import com.artiusid.sdk.sdk.ui.screens.document.PassportScanIntroScreen
import com.artiusid.sdk.sdk.ui.screens.document.PassportScanScreen
import com.artiusid.sdk.sdk.ui.screens.document.PassportChipIntroScreen
import com.artiusid.sdk.sdk.ui.screens.document.PassportChipScanScreen
import com.artiusid.sdk.sdk.ui.screens.verification.VerificationProcessingScreen
import com.artiusid.sdk.sdk.ui.screens.verification.VerificationResultsScreen
import com.artiusid.sdk.sdk.ui.screens.verification.VerificationFailureScreen
import com.artiusid.sdk.sdk.ui.screens.authentication.AuthenticationScreen
import com.artiusid.sdk.sdk.ui.screens.authentication.AuthenticatedScreen

/**
 * SDK Navigation - Uses EXACT standalone app screens and flows
 * 
 * This navigation replicates the exact same flow as the standalone app:
 * Verification: VerificationSteps → FaceScanIntro → FaceScan → SelectDocumentType → 
 *               Document/Passport flows → VerificationProcessing → Results
 * Authentication: AuthenticationScreen → AuthenticatedScreen
 */

sealed class SDKScreen(val route: String) {
    // Verification Flow - EXACT same routes as standalone
    object VerificationSteps : SDKScreen("verification_steps")
    object FaceScanIntro : SDKScreen("face_scan_intro")
    object FaceScan : SDKScreen("face_scan")
    object SelectDocumentType : SDKScreen("select_document_type")
    
    // Document scanning flow - EXACT same routes as standalone
    object DocumentScanIntro : SDKScreen("document_scan_intro")
    object DocumentScan : SDKScreen("document_scan/{documentType}") {
        fun createRoute(documentType: String = "id") = "document_scan/$documentType"
    }
    object DocumentScanBackIntro : SDKScreen("document_scan_back_intro")
    object DocumentScanBack : SDKScreen("document_scan_back")
    
    // Passport scanning flow - EXACT same routes as standalone
    object PassportScanIntro : SDKScreen("passport_scan_intro")
    object PassportScan : SDKScreen("passport_scan")
    object PassportChipIntro : SDKScreen("passport_chip_intro")
    object PassportChipScan : SDKScreen("passport_chip_scan")
    
    // Processing and Results - EXACT same routes as standalone
    object VerificationProcessing : SDKScreen("verification_processing")
    object VerificationResults : SDKScreen("verification_results")
    object VerificationFailure : SDKScreen("verification_failure/{failureType}/{errorReason}") {
        fun createRoute(failureType: String, errorReason: String) = "verification_failure/$failureType/$errorReason"
    }
    
    // Authentication Flow - EXACT same routes as standalone
    object Authentication : SDKScreen("authentication")
    object Authenticated : SDKScreen("authenticated")
}

@Composable
fun SDKNavigation(
    navController: NavHostController,
    startDestination: String = SDKScreen.VerificationSteps.route,
    flowType: String = "verification" // "verification" or "authentication"
) {
    NavHost(
        navController = navController,
        startDestination = if (flowType == "authentication") SDKScreen.Authentication.route else startDestination
    ) {
        
        // ===== VERIFICATION FLOW - EXACT SAME AS STANDALONE =====
        
        composable(SDKScreen.VerificationSteps.route) {
            VerificationStepsScreen(
                onNavigateToFaceScan = {
                    navController.navigate(SDKScreen.FaceScanIntro.route)
                },
                onNavigateBack = {
                    // Return to sample app with cancellation
                    ArtiusIDSDK.verificationCallback?.onVerificationCancelled()
                }
            )
        }

        composable(SDKScreen.FaceScanIntro.route) {
            FaceScanIntroScreen(
                onNavigateToFaceScan = {
                    navController.navigate(SDKScreen.FaceScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.FaceScan.route) {
            FaceScanScreen(
                onNavigateToDocumentScan = {
                    navController.navigate(SDKScreen.SelectDocumentType.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.SelectDocumentType.route) {
            SelectDocumentTypeScreen(
                onNavigateToDocumentScan = { documentType ->
                    when (documentType) {
                        "id" -> navController.navigate(SDKScreen.DocumentScanIntro.route)
                        "passport" -> navController.navigate(SDKScreen.PassportScanIntro.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== DOCUMENT SCANNING FLOW - EXACT SAME AS STANDALONE =====
        
        composable(SDKScreen.DocumentScanIntro.route) {
            DocumentScanIntroScreen(
                onNavigateToDocumentScan = {
                    navController.navigate(SDKScreen.DocumentScan.createRoute("id"))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = SDKScreen.DocumentScan.route,
            arguments = listOf(
                navArgument("documentType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val documentType = backStackEntry.arguments?.getString("documentType") ?: "id"
            DocumentScanScreen(
                documentSide = com.artiusid.sdk.utils.DocumentSide.FRONT,
                onDocumentScanComplete = {
                    navController.navigate(SDKScreen.DocumentScanBackIntro.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.DocumentScanBackIntro.route) {
            DocumentScanBackIntroScreen(
                onNavigateToDocumentScanBack = {
                    navController.navigate(SDKScreen.DocumentScanBack.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.DocumentScanBack.route) {
            DocumentScanScreen(
                documentSide = com.artiusid.sdk.utils.DocumentSide.BACK,
                onDocumentScanComplete = {
                    android.util.Log.d("SDKNavigation", "=== Navigating to VerificationProcessing from DocumentScanBack ===")
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFrontScan = {
                    android.util.Log.d("SDKNavigation", "=== Comparison failed, navigating back to front scan ===")
                    // Navigate back to front scan screen to retake the front image
                    navController.popBackStack(SDKScreen.DocumentScanIntro.route, inclusive = false)
                }
            )
        }

        // ===== PASSPORT SCANNING FLOW - EXACT SAME AS STANDALONE =====
        
        composable(SDKScreen.PassportScanIntro.route) {
            PassportScanIntroScreen(
                onNavigateToPassportScan = {
                    navController.navigate(SDKScreen.PassportScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.PassportScan.route) {
            PassportScanScreen(
                onPassportScanComplete = {
                    navController.navigate(SDKScreen.PassportChipIntro.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.PassportChipIntro.route) {
            PassportChipIntroScreen(
                onNavigateToPassportChip = {
                    navController.navigate(SDKScreen.PassportChipScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SDKScreen.PassportChipScan.route) {
            PassportChipScanScreen(
                onChipScanComplete = {
                    android.util.Log.d("SDKNavigation", "=== Navigating to VerificationProcessing from PassportChipScan ===")
                    navController.navigate(SDKScreen.VerificationProcessing.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== PROCESSING AND RESULTS - EXACT SAME AS STANDALONE =====
        
        composable(SDKScreen.VerificationProcessing.route) {
            VerificationProcessingScreen(
                onNavigateToResults = {
                    navController.navigate(SDKScreen.VerificationResults.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPassportCapture = {
                    // Clear the passport image so user can recapture
                    ImageStorage.clearPassportImage()
                    // Navigate back to passport scan screen
                    navController.navigate(SDKScreen.PassportScan.route) {
                        // Clear the current verification screen from stack
                        popUpTo(SDKScreen.VerificationProcessing.route) { inclusive = true }
                    }
                },
                onNavigateToFailure = { failureType, errorReason ->
                    navController.navigate(SDKScreen.VerificationFailure.createRoute(failureType.name, errorReason))
                }
            )
        }

        composable(SDKScreen.VerificationResults.route) {
            val verificationData = VerificationDataHolder.getVerificationData()
            if (verificationData != null) {
                VerificationResultsScreen(
                    onNavigateHome = {
                        // Instead of navigating home, return results to sample app
                        val result = VerificationResult(
                            success = true,
                            confidence = verificationData.confidence ?: 0.0f,
                            livenessResult = verificationData.livenessResult,
                            documentScanResult = verificationData.documentScanResult,
                            nfcPassportResult = verificationData.nfcPassportResult,
                            sessionId = verificationData.sessionId ?: "sdk_${System.currentTimeMillis()}"
                        )
                        
                        VerificationDataHolder.clearVerificationData()
                        ArtiusIDSDK.verificationCallback?.onVerificationComplete(result)
                    },
                    verificationData = verificationData
                )
            } else {
                // No verification data, return error to sample app
                LaunchedEffect(Unit) {
                    ArtiusIDSDK.verificationCallback?.onVerificationError(
                        SDKError(
                            code = SDKErrorCode.PROCESSING_ERROR,
                            message = "No verification data available",
                            details = "Verification results screen reached without data"
                        )
                    )
                }
            }
        }

        composable(
            route = SDKScreen.VerificationFailure.route,
            arguments = listOf(
                navArgument("failureType") { type = NavType.StringType },
                navArgument("errorReason") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val failureTypeName = backStackEntry.arguments?.getString("failureType") ?: "GENERAL"
            val errorReason = backStackEntry.arguments?.getString("errorReason") ?: "Verification failed"
            
            val failureType = try {
                com.artiusid.sdk.data.model.VerificationFailureType.valueOf(failureTypeName)
            } catch (e: IllegalArgumentException) {
                com.artiusid.sdk.data.model.VerificationFailureType.GENERAL
            }
            
            VerificationFailureScreen(
                failureType = failureType,
                onRetryClick = {
                    // Navigate to appropriate capture screen based on failure type
                    when (failureType) {
                        com.artiusid.sdk.data.model.VerificationFailureType.PASSPORT -> {
                            ImageStorage.clearPassportImage()
                            navController.navigate(SDKScreen.PassportScan.route) {
                                popUpTo(SDKScreen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.STATE_ID_FRONT -> {
                            ImageStorage.clearAll()
                            navController.navigate(SDKScreen.DocumentScan.createRoute("id")) {
                                popUpTo(SDKScreen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.STATE_ID_BACK -> {
                            ImageStorage.clearAll()
                            navController.navigate(SDKScreen.DocumentScanBack.route) {
                                popUpTo(SDKScreen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.FACE -> {
                            ImageStorage.clearAll()
                            navController.navigate(SDKScreen.FaceScan.route) {
                                popUpTo(SDKScreen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.GENERAL -> {
                            // Clear all images and start over
                            ImageStorage.clearAll()
                            navController.navigate(SDKScreen.SelectDocumentType.route) {
                                popUpTo(SDKScreen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                    }
                },
                onBackToHomeClick = {
                    // Instead of going home, return error to sample app
                    ImageStorage.clearAll()
                    VerificationDataHolder.clearVerificationData()
                    ArtiusIDSDK.verificationCallback?.onVerificationError(
                        SDKError(
                            code = SDKErrorCode.PROCESSING_FAILED,
                            message = "Verification failed: $errorReason",
                            details = "Failure type: $failureTypeName"
                        )
                    )
                }
            )
        }

        // ===== AUTHENTICATION FLOW - EXACT SAME AS STANDALONE =====
        
        composable(SDKScreen.Authentication.route) {
            AuthenticationScreen(
                onNavigateToApproval = {
                    // Navigate to authenticated screen after successful authentication
                    navController.navigate(SDKScreen.Authenticated.route) {
                        popUpTo(SDKScreen.Authentication.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Return to sample app with cancellation
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
                }
            )
        }
        
        composable(SDKScreen.Authenticated.route) {
            AuthenticatedScreen(
                onNavigateToHome = {
                    // Instead of navigating home, return results to sample app
                    val result = AuthenticationResult(
                        success = true,
                        token = "auth_token_${System.currentTimeMillis()}",
                        expiresAt = System.currentTimeMillis() + 3600000L, // 1 hour
                        sessionId = "auth_${System.currentTimeMillis()}"
                    )
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                }
            )
        }
    }
}