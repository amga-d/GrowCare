package com.example.mobileappdev.presentation.screens.detection

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Disease detection screen - placeholder for camera-based plant disease scanning
 * TODO: Implement camera capture and Gemini AI analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseScanScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Detection") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Disease Scan Screen - Under Development")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO: Implement camera */ }) {
                Text("Capture Image")
            }
        }
    }
}
