package com.example.growCare.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.growCare.presentation.screens.auth.login.LoginScreen
import com.example.growCare.presentation.screens.auth.signup.SignUpScreen
import com.example.growCare.presentation.screens.chat.ChatScreen
import com.example.growCare.presentation.screens.detection.DiseaseScanScreen
import com.example.growCare.presentation.screens.fertilizer.FertilizerScreen
import com.example.growCare.presentation.screens.home.HomeScreen
import com.example.growCare.presentation.screens.profile.ProfileScreen
import com.example.growCare.presentation.screens.seed.SeedScanScreen

/**
 * Navigation graph for the GrowCare application
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication screens
        composable(Screen.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SIGNUP)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SIGNUP) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.SIGNUP) { inclusive = true }
                    }
                }
            )
        }

        // Home screen
        composable(Screen.HOME) {
            HomeScreen(
                onNavigateToFertilizer = {
                    navController.navigate(Screen.FERTILIZER)
                },
                onNavigateToSeedScan = {
                    navController.navigate(Screen.SEED_SCAN)
                },
                onNavigateToDiseaseScan = {
                    navController.navigate(Screen.DISEASE_SCAN)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.CHAT)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.PROFILE)
                }
            )
        }

        // Fertilizer calculator
        composable(Screen.FERTILIZER) {
            FertilizerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Seed quality scanner
        composable(Screen.SEED_SCAN) {
            SeedScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = {
                    navController.navigate(Screen.SEED_RESULT)
                }
            )
        }

        // Disease detection
        composable(Screen.DISEASE_SCAN) {
            DiseaseScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = {
                    navController.navigate(Screen.DISEASE_RESULT)
                }
            )
        }

        // AI Chat assistant
        composable(Screen.CHAT) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.HOME) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.PROFILE)
                }
            )
        }

        // User profile
        composable(Screen.PROFILE) {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.HOME) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.CHAT)
                },
                onLogout = {
                    // Navigate to login and clear back stack
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
