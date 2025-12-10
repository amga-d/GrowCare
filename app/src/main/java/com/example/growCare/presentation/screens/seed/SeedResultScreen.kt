package com.example.growCare.presentation.screens.seed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.growCare.domain.model.SeedQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedResultScreen(
    analysis: SeedQuality,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    val qualityColor = when {
        analysis.qualityScore >= 80 -> Color(0xFF4CAF50)
        analysis.qualityScore >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    
    val qualityLabel = when {
        analysis.qualityScore >= 90 -> "Excellent"
        analysis.qualityScore >= 80 -> "Very Good"
        analysis.qualityScore >= 70 -> "Good"
        analysis.qualityScore >= 60 -> "Fair"
        analysis.qualityScore >= 50 -> "Poor"
        else -> "Very Poor"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seed Quality Analysis", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    AsyncImage(model = imageUrl, contentDescription = "Seed Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            // Quality Score Card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = qualityColor.copy(alpha = 0.2f)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quality Score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${analysis.qualityScore}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = qualityColor)
                    Text(qualityLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = qualityColor)
                    
                    if (analysis.isRecommendedForUse) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            Text("Recommended for Use", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Size", analysis.sizeAssessment.name.lowercase().replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                MetricCard("Color", analysis.colorConsistency.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Damage", "${analysis.damagePercentage}%", Modifier.weight(1f))
                MetricCard("Germination", "${analysis.germinationPotential}%", Modifier.weight(1f))
            }

            // Recommendations
            if (analysis.recommendations.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        analysis.recommendations.forEach { recommendation ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("•", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(recommendation, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (!analysis.storageAdvice.isNullOrEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Storage Advice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(analysis.storageAdvice ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

