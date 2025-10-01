/*
 * File: AppNavigation.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.utils.VerificationDataHolder
import com.artiusid.sdk.utils.ImageStorage
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.navArgument
import com.artiusid.sdk.presentation.screens.approval.ApprovalRequestScreen
import com.artiusid.sdk.presentation.screens.approval.ApprovalResponseScreen
import com.artiusid.sdk.presentation.screens.authentication.AuthenticationScreen
import com.artiusid.sdk.presentation.screens.authentication.AuthenticatedScreen
import com.artiusid.sdk.presentation.screens.document.DocumentScanScreen
import com.artiusid.sdk.presentation.screens.document.DocumentType
import com.artiusid.sdk.presentation.screens.document.DocumentVerificationScreen
import com.artiusid.sdk.presentation.screens.document.NfcReadingScreen
import com.artiusid.sdk.presentation.screens.enrollment.EnrollmentDataScreen
import com.artiusid.sdk.presentation.screens.face.FaceVerificationScreen
import com.artiusid.sdk.presentation.screens.home.HomeScreen
import com.artiusid.sdk.presentation.screens.settings.SecuritySettingsScreen
import com.artiusid.sdk.presentation.screens.settings.SettingsScreen
import com.artiusid.sdk.presentation.screens.verification.VerificationResultsScreen
import com.artiusid.sdk.presentation.screens.verification.VerificationFailureScreen
import com.artiusid.sdk.presentation.screens.splash.SplashScreen
import com.artiusid.sdk.presentation.permissions.PermissionsHandler
import com.artiusid.sdk.presentation.screens.verification.VerificationStepsScreen
import com.artiusid.sdk.presentation.screens.document.DocumentPreviewScreen
import com.artiusid.sdk.presentation.screens.face.FaceScanIntroScreen
import com.artiusid.sdk.ui.screens.face.FaceScanScreen
import com.artiusid.sdk.presentation.screens.document.SelectDocumentTypeScreen
import com.artiusid.sdk.presentation.screens.verification.VerificationProcessingScreen
import com.artiusid.sdk.presentation.screens.document.DocumentScanIntroScreen
import com.artiusid.sdk.presentation.screens.document.DocumentScanBackIntroScreen
import com.artiusid.sdk.presentation.screens.document.DocumentScanBackScreen
import com.artiusid.sdk.presentation.screens.document.PassportScanIntroScreen
import com.artiusid.sdk.presentation.screens.document.PassportScanScreen
import com.artiusid.sdk.presentation.screens.document.PassportChipIntroScreen
import com.artiusid.sdk.presentation.screens.document.PassportChipScanScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object VerificationSteps : Screen("verification_steps")
    object FaceScanIntro : Screen("face_scan_intro")
    object FaceScan : Screen("face_scan")
    object SelectDocumentType : Screen("select_document_type")
    
    // Document scanning flow
    object DocumentScanIntro : Screen("document_scan_intro")
    object DocumentScan : Screen("document_scan/{documentType}") {
        fun createRoute(documentType: String = "id") = "document_scan/$documentType"
    }
    object DocumentScanBackIntro : Screen("document_scan_back_intro")
    object DocumentScanBack : Screen("document_scan_back")
    
    // Passport scanning flow
    object PassportScanIntro : Screen("passport_scan_intro")
    object PassportScan : Screen("passport_scan")
    object PassportChipIntro : Screen("passport_chip_intro")
    object PassportChipScan : Screen("passport_chip_scan")
    
    object DocumentPreview : Screen("document_preview/{documentPath}") {
        fun createRoute(documentPath: String) = "document_preview/$documentPath"
    }
    object VerificationProcessing : Screen("verification_processing")
    object VerificationResults : Screen("verification_results")
    object VerificationFailure : Screen("verification_failure/{failureType}/{errorReason}") {
        fun createRoute(failureType: String, errorReason: String) = "verification_failure/$failureType/$errorReason"
    }
    object Authentication : Screen("authentication")
    object Authenticated : Screen("authenticated")
    object ApprovalRequest : Screen("approval_request")
    object ApprovalResponse : Screen("approval_response/{response}") {
        fun createRoute(response: String) = "approval_response/$response"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    onVerificationComplete: ((Any) -> Unit)? = null,
    onAuthenticationComplete: ((Any) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVerificationSteps = {
                    navController.navigate(Screen.VerificationSteps.route)
                },
                onNavigateToAuthentication = {
                    navController.navigate(Screen.Authentication.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToApprovalRequest = {
                    navController.navigate(Screen.ApprovalRequest.route)
                }
            )
        }

        composable(Screen.VerificationSteps.route) {
            VerificationStepsScreen(
                onNavigateToFaceScan = {
                    navController.navigate(Screen.FaceScanIntro.route)
                },
                onNavigateBack = {
                    // If this is the start destination, call onCancel to exit the SDK
                    if (startDestination == Screen.VerificationSteps.route) {
                        android.util.Log.d("AppNavigation", "🔙 Back pressed on start screen, calling onCancel")
                        onCancel?.invoke()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Screen.FaceScanIntro.route) {
            FaceScanIntroScreen(
                onNavigateToFaceScan = {
                    navController.navigate(Screen.FaceScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FaceScan.route) {
            FaceScanScreen(
                onNavigateToDocumentScan = {
                    navController.navigate(Screen.SelectDocumentType.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SelectDocumentType.route) {
            SelectDocumentTypeScreen(
                onNavigateToDocumentScan = { documentType ->
                    when (documentType) {
                        "id" -> navController.navigate(Screen.DocumentScanIntro.route)
                        "passport" -> navController.navigate(Screen.PassportScanIntro.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Document scanning flow
        composable(Screen.DocumentScanIntro.route) {
            DocumentScanIntroScreen(
                onNavigateToDocumentScan = {
                    navController.navigate(Screen.DocumentScan.createRoute("id"))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DocumentScan.route,
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
                    navController.navigate(Screen.DocumentScanBackIntro.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DocumentScanBackIntro.route) {
            DocumentScanBackIntroScreen(
                onNavigateToDocumentScanBack = {
                    navController.navigate(Screen.DocumentScanBack.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DocumentScanBack.route) {
            DocumentScanScreen(
                documentSide = com.artiusid.sdk.utils.DocumentSide.BACK,
                onDocumentScanComplete = {
                    android.util.Log.d("AppNavigation", "=== Navigating to VerificationProcessing from DocumentScanBack ===")
                    navController.navigate(Screen.VerificationProcessing.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFrontScan = {
                    android.util.Log.d("AppNavigation", "=== Comparison failed, navigating back to front scan ===")
                    // Navigate back to front scan screen to retake the front image
                    navController.popBackStack(Screen.DocumentScanIntro.route, inclusive = false)
                }
            )
        }

        // Passport scanning flow
        composable(Screen.PassportScanIntro.route) {
            PassportScanIntroScreen(
                onNavigateToPassportScan = {
                    navController.navigate(Screen.PassportScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PassportScan.route) {
            PassportScanScreen(
                onPassportScanComplete = {
                    android.util.Log.d("AppNavigation", "🔍 DIAGNOSTIC: PassportScan completed, navigating to PassportChipIntro")
                    navController.navigate(Screen.PassportChipIntro.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PassportChipIntro.route) {
            PassportChipIntroScreen(
                onNavigateToPassportChip = {
                    android.util.Log.d("AppNavigation", "🔍 DIAGNOSTIC: PassportChipIntro completed, navigating to PassportChipScan")
                    navController.navigate(Screen.PassportChipScan.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PassportChipScan.route) {
            android.util.Log.d("AppNavigation", "🔍 DIAGNOSTIC: Entering PassportChipScan screen")
            PassportChipScanScreen(
                onChipScanComplete = { passportData ->
                    android.util.Log.d("AppNavigation", "=== Passport NFC scan completed ===")
                    
                    // Store passport data in DocumentDataHolder for name extraction
                    if (passportData != null) {
                        val passportInfo = com.artiusid.sdk.utils.PassportData(
                            firstName = passportData.firstName,
                            lastName = passportData.lastName,
                            documentNumber = passportData.documentNumber,
                            nationality = passportData.nationality,
                            dateOfBirth = passportData.dateOfBirth,
                            dateOfExpiry = passportData.documentExpiryDate
                        )
                        com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(passportInfo)
                        android.util.Log.d("AppNavigation", "✅ Stored passport data: ${passportData.firstName} ${passportData.lastName}")
                    } else {
                        android.util.Log.w("AppNavigation", "⚠️ No passport data received from NFC scan")
                    }
                    
                    android.util.Log.d("AppNavigation", "=== Navigating to VerificationProcessing from PassportChipScan ===")
                    navController.navigate(Screen.VerificationProcessing.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Authentication.route) {
            AuthenticationScreen(
                onNavigateToApproval = {
                    // Navigate to authenticated screen after successful authentication (like iOS)
                    navController.navigate(Screen.Authenticated.route) {
                        popUpTo(Screen.Authentication.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // If this is the start destination, call onCancel to exit the SDK
                    if (startDestination == Screen.Authentication.route) {
                        android.util.Log.d("AppNavigation", "🔙 Back pressed on start screen, calling onCancel")
                        onCancel?.invoke()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        
        composable(Screen.Authenticated.route) {
            AuthenticatedScreen(
                onNavigateToHome = {
                    // Navigate back to home (like iOS IntroHomeView)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.VerificationProcessing.route) {
            VerificationProcessingScreen(
                onNavigateToResults = {
                    // When verification succeeds, call the SDK callback instead of navigating to internal results
                    val verificationData = VerificationDataHolder.getVerificationData()
                    android.util.Log.d("AppNavigation", "✅ Verification completed successfully, calling SDK callback")
                    android.util.Log.d("AppNavigation", "📄 Verification data: $verificationData")
                    onVerificationComplete?.invoke(verificationData ?: "Verification completed successfully")
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onError = onError,
                onNavigateToPassportCapture = {
                    // Clear the passport image so user can recapture
                    ImageStorage.clearPassportImage()
                    // Navigate back to passport scan screen
                    navController.navigate(Screen.PassportScan.route) {
                        // Clear the current verification screen from stack
                        popUpTo(Screen.VerificationProcessing.route) { inclusive = true }
                    }
                },
                onNavigateToFailure = { failureType, errorReason ->
                    navController.navigate(Screen.VerificationFailure.createRoute(failureType.name, errorReason))
                }
            )
        }

        composable(
            route = Screen.DocumentPreview.route,
            arguments = listOf(
                navArgument("documentPath") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val documentPath = backStackEntry.arguments?.getString("documentPath") ?: ""
            DocumentPreviewScreen(
                documentPath = documentPath,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.VerificationResults.route) {
            val verificationData = VerificationDataHolder.getVerificationData()
            if (verificationData != null) {
                VerificationResultsScreen(
                    onNavigateHome = {
                        // Clear verification data and exit standalone app to return to sample app
                        VerificationDataHolder.clearVerificationData()
                        android.util.Log.d("AppNavigation", "🏠 Back Home pressed on verification results, calling onCancel to exit standalone app")
                        onCancel?.invoke()
                    },
                    verificationData = verificationData
                )
            } else {
                // No verification data, navigate back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(
            route = Screen.VerificationFailure.route,
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
                            navController.navigate(Screen.PassportScan.route) {
                                popUpTo(Screen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.STATE_ID_FRONT -> {
                            ImageStorage.clearAll()
                            navController.navigate(Screen.DocumentScan.createRoute("id")) {
                                popUpTo(Screen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.STATE_ID_BACK -> {
                            ImageStorage.clearAll()
                            navController.navigate(Screen.DocumentScanBack.route) {
                                popUpTo(Screen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.FACE -> {
                            ImageStorage.clearAll()
                            navController.navigate(Screen.FaceScan.route) {
                                popUpTo(Screen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                        com.artiusid.sdk.data.model.VerificationFailureType.GENERAL -> {
                            // Clear all images and start over
                            ImageStorage.clearAll()
                            navController.navigate(Screen.SelectDocumentType.route) {
                                popUpTo(Screen.VerificationFailure.route) { inclusive = true }
                            }
                        }
                    }
                },
                onBackToHomeClick = {
                    // Clear all data and exit standalone app to return to sample app
                    ImageStorage.clearAll()
                    VerificationDataHolder.clearVerificationData()
                    android.util.Log.d("AppNavigation", "🔙 Back Home pressed on verification failure, calling onCancel to exit standalone app")
                    onCancel?.invoke()
                }
            )
        }

        composable(Screen.ApprovalRequest.route) {
            ApprovalRequestScreen(
                onNavigateToApprovalResponse = { response ->
                    navController.navigate(Screen.ApprovalResponse.createRoute(response))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ApprovalResponse.route,
            arguments = listOf(navArgument("response") { type = NavType.StringType })
        ) { backStackEntry ->
            val response = backStackEntry.arguments?.getString("response") ?: "no"
            ApprovalResponseScreen(
                response = response,
                onNavigateToHome = {
                    // Navigate back to home (like iOS IntroHomeView reset)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
    }
} 