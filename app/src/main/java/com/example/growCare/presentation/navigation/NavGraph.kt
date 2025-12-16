package com.example.growCare.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.growCare.data.local.preferences.ThemeMode
import com.example.growCare.presentation.screens.auth.login.LoginScreen
import com.example.growCare.presentation.screens.auth.signup.SignUpScreen
import com.example.growCare.presentation.screens.chat.ChatScreen
import com.example.growCare.presentation.screens.detection.DiseaseScanScreen
import com.example.growCare.presentation.screens.detection.DiseaseResultScreen
import com.example.growCare.presentation.screens.fertilizer.FertilizerScreen
import com.example.growCare.presentation.screens.fertilizer.FertilizerResultScreen
import com.example.growCare.presentation.screens.home.HomeScreen
import com.example.growCare.presentation.screens.profile.ProfileScreen
import com.example.growCare.presentation.screens.seed.SeedScanScreen
import com.example.growCare.presentation.screens.seed.SeedResultScreen
import kotlinx.coroutines.launch

/**
 * Navigation graph for the GrowCare application
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    navigationViewModel: NavigationViewModel = hiltViewModel(),
    startDestination: String = Screen.HOME,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
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
                },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
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
                },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
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
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.HISTORY)
                }
            )
        }

        // Fertilizer calculator
        composable(Screen.FERTILIZER) {
            FertilizerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { recommendation ->
                    navigationViewModel.setFertilizerRecommendation(recommendation)
                    navController.navigate(Screen.FERTILIZER_RESULT)
                }
            )
        }

        // Fertilizer result
        composable(Screen.FERTILIZER_RESULT) {
            val recommendation by navigationViewModel.currentFertilizerRecommendation.collectAsStateWithLifecycle()
            
            recommendation?.let { rec ->
                FertilizerResultScreen(
                    recommendation = rec,
                    onNavigateBack = {
                        navigationViewModel.clearFertilizerRecommendation()
                        navController.popBackStack()
                    }
                )
            }
        }

        // Seed quality scanner
        composable(Screen.SEED_SCAN) {
            SeedScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { analysis, imageUrl ->
                    navigationViewModel.setSeedAnalysis(analysis, imageUrl)
                    navController.navigate(Screen.SEED_RESULT)
                }
            )
        }

        // Seed quality result
        composable(Screen.SEED_RESULT) {
            val analysis by navigationViewModel.currentSeedAnalysis.collectAsStateWithLifecycle()
            val imageUrl by navigationViewModel.currentSeedImageUrl.collectAsStateWithLifecycle()
            
            analysis?.let { seedAnalysis ->
                SeedResultScreen(
                    analysis = seedAnalysis,
                    imageUrl = imageUrl,
                    onNavigateBack = {
                        navigationViewModel.clearSeedAnalysis()
                        navController.popBackStack()
                    },
                    onScanAnother = {
                        navigationViewModel.clearSeedAnalysis()
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navigationViewModel.clearSeedAnalysis()
                        navController.navigate(Screen.HOME) {
                            popUpTo(Screen.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Disease detection
        composable(Screen.DISEASE_SCAN) {
            DiseaseScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { analysis, imageUrl ->
                    navigationViewModel.setDiseaseAnalysis(analysis, imageUrl)
                    navController.navigate(Screen.DISEASE_RESULT)
                }
            )
        }

        // Disease detection result
        composable(Screen.DISEASE_RESULT) {
            val analysis by navigationViewModel.currentDiseaseAnalysis.collectAsStateWithLifecycle()
            val imageUrl by navigationViewModel.currentDiseaseImageUrl.collectAsStateWithLifecycle()
            
            analysis?.let { diseaseAnalysis ->
                DiseaseResultScreen(
                    analysis = diseaseAnalysis,
                    imageUrl = imageUrl,
                    onNavigateBack = {
                        navigationViewModel.clearDiseaseAnalysis()
                        navController.popBackStack()
                    },
                    onScanAnother = {
                        navigationViewModel.clearDiseaseAnalysis()
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navigationViewModel.clearDiseaseAnalysis()
                        navController.navigate(Screen.HOME) {
                            popUpTo(Screen.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        // AI Chat assistant
        composable(
            route = Screen.CHAT,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val chatViewModel: com.example.growCare.presentation.screens.chat.ChatViewModel = hiltViewModel()
            
            ChatScreen(
                viewModel = chatViewModel,
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
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.CHAT_HISTORY)
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
                },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }
        
        // Activity History
        composable(Screen.HISTORY) {
            val historyViewModel: com.example.growCare.presentation.screens.history.HistoryViewModel = hiltViewModel()
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            
            com.example.growCare.presentation.screens.history.HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDiseaseResult = { diseaseId ->
                    scope.launch {
                        val analysis = historyViewModel.loadDiseaseById(diseaseId)
                        if (analysis != null) {
                            navigationViewModel.setDiseaseAnalysis(analysis, analysis.imageUrl)
                            navController.navigate(Screen.DISEASE_RESULT)
                        }
                    }
                },
                onNavigateToSeedResult = { seedId ->
                    scope.launch {
                        val analysis = historyViewModel.loadSeedById(seedId)
                        if (analysis != null) {
                            navigationViewModel.setSeedAnalysis(analysis, analysis.imageUrl)
                            navController.navigate(Screen.SEED_RESULT)
                        }
                    }
                },
                onNavigateToFertilizerResult = { fertilizerId ->
                    // TODO: Load fertilizer from ID and navigate
                    navController.navigate(Screen.FERTILIZER)
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate("chat?conversationId=$conversationId")
                }
            )
        }
        
        // Chat History
        composable(Screen.CHAT_HISTORY) {
            val chatViewModel: com.example.growCare.presentation.screens.chat.ChatViewModel = hiltViewModel()
            val scope = rememberCoroutineScope()
            
            com.example.growCare.presentation.screens.chat.ChatHistoryScreen(
                viewModel = chatViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConversationClick = { conversationId ->
                    // Navigate to chat with conversation ID
                    navController.navigate("chat?conversationId=$conversationId")
                }
            )
        }
    }
}
