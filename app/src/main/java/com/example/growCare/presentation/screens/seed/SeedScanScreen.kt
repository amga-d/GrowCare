package com.example.growCare.presentation.screens.seed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.presentation.components.CameraCapture
import com.example.growCare.presentation.components.AnimatedLoadingIndicator
import com.example.growCare.presentation.components.FadeInContent
import com.example.growCare.presentation.components.SlideInFromBottom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedScanScreen(
    viewModel: SeedViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (SeedQuality, String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onAction(SeedAction.CaptureImage(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SeedEvent.AnalysisComplete -> {
                    onNavigateToResult(event.analysis, event.imageUrl)
                }
                is SeedEvent.ShowError -> {
                    // TODO: Show snackbar
                }
                is SeedEvent.ShowMessage -> {
                    // TODO: Show snackbar
                }
            }
        }
    }

    val primaryGreen = MaterialTheme.colorScheme.primary

    if (uiState.showCamera) {
        CameraCapture(
            onImageCaptured = { uri ->
                viewModel.onAction(SeedAction.CaptureImage(uri))
            },
            onError = { error ->
                viewModel.onAction(SeedAction.HideCamera)
            },
            onClose = {
                viewModel.onAction(SeedAction.HideCamera)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seed Quality", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show captured image preview if available with animation
                FadeInContent(visible = uiState.capturedImageUri != null) {
                    uiState.capturedImageUri?.let { uri ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Captured seed image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Loading state with animated indicator
                if (uiState.isAnalyzing) {
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedLoadingIndicator(message = "Analyzing seed quality...")
                    Spacer(modifier = Modifier.weight(1f))
                } else if (uiState.error != null) {
                    // Error state with slide-in animation
                    SlideInFromBottom(visible = true) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "An error occurred",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Button(
                                onClick = { viewModel.onAction(SeedAction.RetryAnalysis) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (uiState.capturedImageUri == null) {
                    // Instructions when no image captured
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Capture an image of seeds to analyze their quality",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Capture button - always visible when not analyzing
                if (!uiState.isAnalyzing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onAction(SeedAction.ShowCamera) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Camera")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Gallery")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeedScanScreenPreview() {
    SeedScanScreen()
}

