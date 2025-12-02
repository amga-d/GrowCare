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
import com.example.mobileappdev.screens.auth.LoginScreen
import com.example.mobileappdev.screens.auth.SignUpScreen
import com.example.mobileappdev.screens.auth.fertilizer.FertilizerScreen
import com.example.mobileappdev.screens.auth.home.HomeScreen
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