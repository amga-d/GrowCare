package com.example.growCare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.growCare.data.local.preferences.ThemeMode
import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import com.example.growCare.presentation.navigation.NavGraph
import com.example.growCare.presentation.navigation.Screen
import com.example.growCare.presentation.screens.auth.signup.SignUpScreen
import com.example.growCare.presentation.theme.ThemeViewModel
import com.example.growCare.presentation.theme.shouldUseDarkTheme
import com.example.growCare.ui.theme.MobileAppDevTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authDataSource: FirebaseAuthDataSource
    
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val isDarkTheme = shouldUseDarkTheme(themeMode)
            
            MobileAppDevTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // Check authentication status to determine start destination
                val startDestination = if (authDataSource.isAuthenticated()) {
                    Screen.HOME
                } else {
                    Screen.LOGIN
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    themeMode = themeMode,
                    onThemeModeChange = { themeViewModel.setThemeMode(it) }
                )
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