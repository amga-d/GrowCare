package com.example.growCare.presentation.screens.detection

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
import coil.compose.AsyncImage
import com.example.growCare.domain.model.DiseaseAnalysis
import com.example.growCare.presentation.components.CameraCapture
import com.example.growCare.presentation.components.AnimatedLoadingIndicator
import com.example.growCare.presentation.components.FadeInContent
import com.example.growCare.presentation.components.SlideInFromBottom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseScanScreen(
    viewModel: DiseaseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (DiseaseAnalysis, String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onAction(DiseaseAction.CaptureImage(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DiseaseEvent.AnalysisComplete -> {
                    onNavigateToResult(event.analysis, uiState.capturedImageUri?.toString())
                }
                is DiseaseEvent.ShowError -> {
                    // TODO: Show snackbar
                }
                is DiseaseEvent.ShowMessage -> {
                    // TODO: Show snackbar
                }
            }
        }
    }

    if (uiState.showCamera) {
        CameraCapture(
            onImageCaptured = { uri ->
                viewModel.onAction(DiseaseAction.CaptureImage(uri))
            },
            onError = { error ->
                viewModel.onAction(DiseaseAction.HideCamera)
            },
            onClose = {
                viewModel.onAction(DiseaseAction.HideCamera)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Disease Detection", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FadeInContent(visible = uiState.capturedImageUri != null) {
                    uiState.capturedImageUri?.let { uri ->
                        Card(modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(12.dp)) {
                            AsyncImage(model = uri, contentDescription = "Captured Plant", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }

                if (uiState.isAnalyzing) {
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedLoadingIndicator(message = "Analyzing plant disease...")
                    Spacer(modifier = Modifier.weight(1f))
                } else if (uiState.error != null) {
                    SlideInFromBottom(visible = true) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(onClick = { viewModel.onAction(DiseaseAction.RetryAnalysis) }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (uiState.capturedImageUri == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Capture a plant image to detect diseases", style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.onAction(DiseaseAction.ShowCamera) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isAnalyzing
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isAnalyzing
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
