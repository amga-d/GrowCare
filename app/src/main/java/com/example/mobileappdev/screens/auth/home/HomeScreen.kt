package com.example.mobileappdev.screens.auth.home

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileappdev.R

@Composable
fun HomeScreen(
    onNavigateToFertilizer: () -> Unit = {},
    onNavigateToSeedScan: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

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
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = Color(0xFFF5F5F5) // Abu-abu muda
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Header Section
            HeaderSection()

            // 2. Weather Card
            WeatherCard()

            // 3. Quick Actions Section
            QuickActionsSection(
                onFertilizerClick = onNavigateToFertilizer,
                onSeedScanClick = onNavigateToSeedScan
            )

            // 4. Crop Health Summary Section
            CropHealthSummarySection()
            
            // Spacer for FAB and BottomBar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(Color.White, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Image
        Image(
            painter = painterResource(id = R.drawable.user), // Menggunakan user.xml yang ada
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray), // Placeholder background
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Greeting Text
        Text(
            text = "Good morning, John",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        // Notification Icon
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF4B5563), // Abu-abu gelap
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun WeatherCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bagian Kiri (70%)
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = "Sunny, 24°C",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Sunny skies throughout the day.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "3-day forecast looks clear.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text("View Details", color = Color.White, fontSize = 12.sp)
                }
            }

            // Bagian Kanan (30%)
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Menggunakan Icon karena drawable/weather_sunny belum ada
                // Jika ada, ganti dengan: painterResource(id = R.drawable.weather_sunny)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sunny",
                        tint = Color(0xFFFFC107), // Kuning matahari
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onFertilizerClick: () -> Unit,
    onSeedScanClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
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
                modifier = Modifier.weight(1f)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
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
            color = Color.Black,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Field A-1 (Corn)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = Color(0xFFFFF9C4), // Kuning muda
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Warning",
                            color = Color(0xFFF57F17), // Kuning tua
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Recent harvest",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFFC107), // Kuning
                    trackColor = Color(0xFFE0E0E0), // Abu-abu muda
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Labels
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Soil Moisture",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "45%",
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563) // Abu-abu gelap
                    )
                }

                // Button
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8F5E9), // Hijau muda background
                        contentColor = Color(0xFF4CAF50) // Text hijau
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
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                indicatorColor = Color(0xFFE8F5E9)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "Chat AI") },
            label = { Text("Chat AI") },
            selected = false,
            onClick = onChatClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

