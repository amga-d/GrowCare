package com.example.growCare.presentation.screens.seed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedScanScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val primaryGreen = Color(0xFF4CAF50)
    val textBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF757575)
    val cameraBg = Color(0xFFF0F9F0)
    val borderGreen = Color(0xFFA5D6A7)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Seeding Quality",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Chat, contentDescription = "Chat AI") },
                    label = { Text("Chat AI") },
                    selected = false,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = textGray,
                        unselectedTextColor = textGray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = textGray,
                        unselectedTextColor = textGray
                    )
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // A. Main Title
                Text(
                    text = "Assess Your Seeding Quality",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textBlack,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.02).sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // B. Instruction Text
                Text(
                    text = "Place seeds on a flat, contrasting surface and tap to scan.",
                    fontSize = 15.sp,
                    color = textGray,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // C. Camera Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cameraBg)
                        .dashedBorder(
                            color = borderGreen,
                            strokeWidth = 2.dp,
                            cornerRadius = 16.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Camera Area",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF424242),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Tap the button below to start scanning your seeds.",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. Start Scan Button
                Button(
                    onClick = { /* TODO: Handle Scan */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Start Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun Modifier.shadow(
    elevation: androidx.compose.ui.unit.Dp,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.ui.graphics.RectangleShape,
    clip: Boolean = elevation > 0.dp
): Modifier {
    return this.drawBehind {
        drawRect(
            color = Color.Black.copy(alpha = 0.2f),
            size = size
        )
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp
): Modifier {
    return this.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        )
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = stroke
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SeedScanScreenPreview() {
    SeedScanScreen()
}

