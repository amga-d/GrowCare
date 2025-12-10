package com.example.growCare.presentation.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToFertilizer: () -> Unit = {},
    onNavigateToSeedScan: () -> Unit = {},
    onNavigateToDiseaseScan: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            // Permission granted, refresh weather
            viewModel.onLocationPermissionResult(true)
        } else {
            // Permission denied
            viewModel.onLocationPermissionResult(false)
        }
    }

    // Request location permission on first composition
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


    Scaffold(
        bottomBar = { 
            BottomNavigationBar(
                onChatClick = onNavigateToChat,
                onProfileClick = onNavigateToProfile
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Handle Scan */ },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Show loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Show error if any
            uiState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 1. Header Section with user data
            HeaderSection(
                user = uiState.user,
                onHistoryClick = onNavigateToHistory
            )

            // 2. Weather Card with real data
            WeatherCard(
                weather = uiState.weather,
                isLoading = uiState.isLoadingWeather,
                error = uiState.weatherError
            )

            // 3. Quick Actions Section
            QuickActionsSection(
                onFertilizerClick = onNavigateToFertilizer,
                onSeedScanClick = onNavigateToSeedScan,
                onDiseaseScanClick = onNavigateToDiseaseScan
            )

            // 4. Crop Health Summary Section
            CropHealthSummarySection()
            
            // Spacer for FAB and BottomBar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection(
    user: com.example.growCare.domain.model.User? = null,
    onHistoryClick: () -> Unit = {}
) {
    // Get greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Get display name (first name only if available)
    val displayName = user?.displayName?.split(" ")?.firstOrNull() ?: user?.email?.split("@")?.firstOrNull() ?: "Farmer"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Image
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Greeting Text with user name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting,",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // History Icon
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Activity History",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Notification Icon
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun WeatherCard(
    weather: com.example.growCare.domain.model.WeatherData? = null,
    isLoading: Boolean = false,
    error: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Weather unavailable",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            weather != null -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Weather info (70%)
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = "${weather.description}, ${weather.temperature.toInt()}°C",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = weather.location,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💧 ${weather.humidity}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "💨 ${weather.windSpeed.toInt()} m/s",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Feels like ${weather.feelsLike.toInt()}°C",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Right side - Weather icon (30%)
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = getWeatherGradient(weather.description)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getWeatherIcon(weather.description),
                                contentDescription = weather.description,
                                tint = getWeatherIconColor(weather.description),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get weather icon based on description
 */
fun getWeatherIcon(description: String): ImageVector {
    return when {
        description.contains("clear", ignoreCase = true) -> Icons.Default.WbSunny
        description.contains("cloud", ignoreCase = true) -> Icons.Default.Cloud
        description.contains("rain", ignoreCase = true) -> Icons.Default.Umbrella
        description.contains("thunder", ignoreCase = true) -> Icons.Default.Thunderstorm
        description.contains("snow", ignoreCase = true) -> Icons.Default.AcUnit
        description.contains("mist", ignoreCase = true) ||
        description.contains("fog", ignoreCase = true) -> Icons.Default.Cloud
        else -> Icons.Default.WbCloudy
    }
}

/**
 * Get weather icon color based on description
 */
fun getWeatherIconColor(description: String): Color {
    return when {
        description.contains("clear", ignoreCase = true) -> Color(0xFFFFC107)
        description.contains("cloud", ignoreCase = true) -> Color(0xFF90CAF9)
        description.contains("rain", ignoreCase = true) -> Color(0xFF64B5F6)
        description.contains("thunder", ignoreCase = true) -> Color(0xFFFFEB3B)
        description.contains("snow", ignoreCase = true) -> Color(0xFFE3F2FD)
        else -> Color(0xFFB0BEC5)
    }
}

/**
 * Get weather gradient colors based on description
 */
fun getWeatherGradient(description: String): List<Color> {
    return when {
        description.contains("clear", ignoreCase = true) ->
            listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
        description.contains("cloud", ignoreCase = true) ->
            listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
        description.contains("rain", ignoreCase = true) ->
            listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        description.contains("thunder", ignoreCase = true) ->
            listOf(Color(0xFFFFF9C4), Color(0xFFFFE082))
        description.contains("snow", ignoreCase = true) ->
            listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))
        else ->
            listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
    }
}

@Composable
fun QuickActionsSection(
    onFertilizerClick: () -> Unit,
    onSeedScanClick: () -> Unit,
    onDiseaseScanClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Baris Pertama (2 Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Disease Detection",
                subtitle = "Scan and identify crop diseases",
                icon = Icons.Default.CenterFocusStrong, // Icon kamera/scan
                modifier = Modifier.weight(1f),
                onClick = onDiseaseScanClick
            )
            QuickActionCard(
                title = "Seeding Quality",
                subtitle = "Assess the quality of your seeds",
                icon = Icons.Default.Eco, // Icon seedling/plant
                modifier = Modifier.weight(1f),
                onClick = onSeedScanClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Baris Kedua (1 Card Full Width)
        QuickActionCard(
            title = "Fertilizer Recipes",
            subtitle = "Calculate ideal fertilizer mixes",
            icon = Icons.Default.Science, // Icon flask/beaker
            modifier = Modifier.fillMaxWidth(),
            onClick = onFertilizerClick
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun CropHealthSummarySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crop Health Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Field A-1 (Corn)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = com.example.growCare.ui.theme.WarningYellow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Warning",
                            color = com.example.growCare.ui.theme.WarningOrange,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Recent harvest",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Labels
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Soil Moisture",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "45%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Button
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("View Crop Overview", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Filled.Home, 
                    contentDescription = "Home",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "Home",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            selected = true,
            onClick = { /* Current screen */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.AutoMirrored.Outlined.Chat, 
                    contentDescription = "Chat AI",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "Chat AI",
                    style = MaterialTheme.typography.labelMedium
                ) 
            },
            selected = false,
            onClick = onChatClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Outlined.Person, 
                    contentDescription = "Profile",
                    modifier = Modifier.size(26.dp)
                ) 
            },
            label = { 
                Text(
                    "Profile",
                    style = MaterialTheme.typography.labelMedium
                ) 
            },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
