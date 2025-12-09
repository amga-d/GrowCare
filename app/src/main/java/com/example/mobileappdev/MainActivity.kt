package com.example.mobileappdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileappdev.screens.auth.SignUpScreen
import com.example.mobileappdev.screens.auth.chat.ChatScreen
import com.example.mobileappdev.screens.auth.fertilizer.FertilizerScreen
import com.example.mobileappdev.screens.auth.home.HomeScreen
import com.example.mobileappdev.screens.auth.profile.ProfileScreen
import com.example.mobileappdev.screens.auth.seed.SeedScanScreen
import com.example.mobileappdev.ui.theme.MobileAppDevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileAppDevTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToFertilizer = {
                                navController.navigate("fertilizer")
                            },
                            onNavigateToSeedScan = {
                                navController.navigate("seed_scan")
                            },
                            onNavigateToChat = {
                                navController.navigate("chat")
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            }
                        )
                    }
                    composable("fertilizer") {
                        FertilizerScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("seed_scan") {
                        SeedScanScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("chat") {
                        ChatScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToChat = {
                                navController.navigate("chat")
                            },
                            onLogout = {
                                // TODO: Implement actual logout logic (clear session, etc.)
                                navController.navigate("home") { // For now, just go home or login if we had it
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MobileAppDevTheme {
        SignUpScreen()
    }
}