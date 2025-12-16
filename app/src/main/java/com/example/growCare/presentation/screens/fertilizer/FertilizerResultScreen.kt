package com.example.growCare.presentation.screens.fertilizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.growCare.domain.model.ApplicationPhase
import com.example.growCare.domain.model.FertilizerProduct
import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.presentation.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerResultScreen(
    recommendation: FertilizerRecommendation,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommendation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                ResultSummaryCard(recommendation)
            }

            // Recommended Products
            item {
                Text(
                    text = "Recommended Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(recommendation.fertilizerProducts) { product ->
                ProductCard(product)
            }

            // Application Schedule
            item {
                Text(
                    text = "Application Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(recommendation.applicationSchedule) { phase ->
                ScheduleCard(phase)
            }

            // Organic Alternatives
            if (recommendation.organicAlternatives.isNotEmpty()) {
                item {
                    OrganicAlternativesCard(recommendation.organicAlternatives)
                }
            }

            // Additional Notes
            recommendation.additionalNotes?.let { notes ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = notes)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Done",
                    onClick = onNavigateBack
                )
            }
        }
    }
}

@Composable
fun ResultSummaryCard(recommendation: FertilizerRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "For ${recommendation.area.formatAmount()} acres of ${recommendation.cropType}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    icon = Icons.Default.Science,
                    label = "Target NPK",
                    value = recommendation.recommendedNPK.toRatioString()
                )
                MetricItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Est. Cost",
                    value = "$${recommendation.estimatedCost.formatAmount()}"
                )
            }
        }
    }
}

@Composable
fun MetricItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProductCard(product: FertilizerProduct) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${product.quantityNeeded.formatAmount()} kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Method: ${product.applicationMethod}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (product.isOrganic) {
                Text(
                    text = "Organic Product",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(phase: ApplicationPhase) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = phase.phase,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apply: ${phase.instructions}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Timing: ${phase.daysAfterPlanting} days after planting",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Amount: ${phase.quantity.formatAmount()} kg",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun Double.formatAmount(): String {
    return if (this % 1.0 == 0.0) {
        String.format("%.0f", this)
    } else {
        String.format("%.2f", this)
    }
}

@Composable
fun OrganicAlternativesCard(alternatives: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Eco, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Organic Alternatives",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            alternatives.forEach { alt ->
                Text(text = "• $alt", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
