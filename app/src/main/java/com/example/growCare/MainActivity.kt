package com.example.growCare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.growCare.presentation.navigation.NavGraph
import com.example.growCare.presentation.navigation.Screen
import com.example.growCare.presentation.screens.auth.signup.SignUpScreen
import com.example.growCare.ui.theme.MobileAppDevTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileAppDevTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = Screen.HOME
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