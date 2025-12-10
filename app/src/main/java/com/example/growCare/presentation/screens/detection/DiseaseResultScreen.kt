package com.example.growCare.presentation.screens.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.growCare.domain.model.DiseaseAnalysis
import com.example.growCare.domain.model.toDisplayString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultScreen(
    analysis: DiseaseAnalysis,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    val severityColor = when (analysis.severity) {
        com.example.growCare.domain.model.DiseaseSeverity.SEVERE -> Color(0xFFF44336)
        com.example.growCare.domain.model.DiseaseSeverity.MODERATE -> Color(0xFFFFC107)
        com.example.growCare.domain.model.DiseaseSeverity.MILD -> Color(0xFF4CAF50)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Analysis", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (imageUrl != null) {
                Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp)) {
                    AsyncImage(model = imageUrl, contentDescription = "Plant Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, null, tint = severityColor, modifier = Modifier.size(32.dp))
                        Text(analysis.diseaseName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Confidence: ${analysis.confidence}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Severity: ${analysis.severity.toDisplayString()}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = severityColor)
                    }
                }
            }

            ResultSection("Symptoms", analysis.symptoms, Color(0xFFFFF3E0))
            ResultSection("Treatment", analysis.treatment, Color(0xFFE8F5E9))
            ResultSection("Prevention", analysis.prevention, Color(0xFFE3F2FD))

            if (!analysis.additionalNotes.isNullOrEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Additional Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(analysis.additionalNotes ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSection(title: String, items: List<String>, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

