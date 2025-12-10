package com.example.growCare.presentation.screens.fertilizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.growCare.domain.model.FertilizerRecommendation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerScreen(
    viewModel: FertilizerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (FertilizerRecommendation) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FertilizerEvent.NavigateToResult -> {
                    onNavigateToResult(event.recommendation)
                }
                is FertilizerEvent.ShowError -> {
                    // TODO: Show snackbar
                }
                is FertilizerEvent.ShowMessage -> {
                    // TODO: Show snackbar
                }
            }
        }
    }

    // Dropdown Expanded States
    var cropTypeExpanded by remember { mutableStateOf(false) }
    var growthStageExpanded by remember { mutableStateOf(false) }
    var soilTypeExpanded by remember { mutableStateOf(false) }

    // Data Options
    val cropTypes = listOf("Corn", "Wheat", "Rice", "Soybean", "Cotton")
    val growthStages = listOf("Vegetative", "Flowering", "Fruiting", "Maturity")
    val soilTypes = listOf("Loamy", "Sandy", "Clay", "Silt", "Peaty")

    val primaryBlue = Color(0xFF2196F3)
    val secondaryGreen = Color(0xFF4CAF50)
    val textBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF757575)
    val backgroundGray = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fertilizer Recipe",
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
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle Share */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = primaryBlue
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
                modifier = Modifier.height(80.dp) // Adjusted height to accommodate standard touch targets
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = textGray,
                        unselectedTextColor = textGray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat AI") },
                    label = { Text("Chat AI") },
                    selected = true,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryBlue,
                        selectedTextColor = primaryBlue,
                        indicatorColor = Color.Transparent // Removing indicator background for cleaner look or match spec
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
        containerColor = backgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Tab Layout
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = primaryBlue,
                indicator = {}, // Hide default indicator
                divider = {},
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            ) {
                val tabs = listOf("By Crop Type", "Manual Input")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier
                            .background(
                                if (selectedTabIndex == index) primaryBlue else Color.White
                            ),
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = textGray
                    )
                }
            }

            // Form Content
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // A. Crop Type Dropdown
                Column {
                    Text(
                        text = "Crop Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = cropTypeExpanded,
                        onExpandedChange = { cropTypeExpanded = !cropTypeExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.cropType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            isError = uiState.cropTypeError != null
                        )
                        ExposedDropdownMenu(
                            expanded = cropTypeExpanded,
                            onDismissRequest = { cropTypeExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            cropTypes.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = item) },
                                    onClick = {
                                        viewModel.onAction(FertilizerAction.UpdateCropType(item))
                                        cropTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (uiState.cropTypeError != null) {
                        Text(
                            text = uiState.cropTypeError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // B. Growth Stage & Soil Type (Row Layout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Growth Stage
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Growth Stage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textBlack,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = growthStageExpanded,
                            onExpandedChange = { growthStageExpanded = !growthStageExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.growthStage,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = growthStageExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = growthStageExpanded,
                                onDismissRequest = { growthStageExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                growthStages.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            viewModel.onAction(FertilizerAction.UpdateGrowthStage(item))
                                            growthStageExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Soil Type
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soil Type",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textBlack,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = soilTypeExpanded,
                            onExpandedChange = { soilTypeExpanded = !soilTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.soilType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilTypeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = soilTypeExpanded,
                                onDismissRequest = { soilTypeExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                soilTypes.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            viewModel.onAction(FertilizerAction.UpdateSoilType(item))
                                            soilTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // C. Area Size Input
                Text(
                    text = "Area Size (acres)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.areaSize,
                    onValueChange = { viewModel.onAction(FertilizerAction.UpdateAreaSize(it)) },
                    placeholder = { Text("e.g. 50", color = textGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true,
                    isError = uiState.areaSizeError != null
                )
                if (uiState.areaSizeError != null) {
                    Text(
                        text = uiState.areaSizeError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // D. Target Yield Goal Slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Yield Goal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textBlack
                    )
                    Text(
                        text = "${uiState.targetYield.toInt()} bu/ac",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryGreen
                    )
                }

                Slider(
                    value = uiState.targetYield,
                    onValueChange = { viewModel.onAction(FertilizerAction.UpdateTargetYield(it)) },
                    valueRange = 100f..250f,
                    colors = SliderDefaults.colors(
                        thumbColor = secondaryGreen,
                        activeTrackColor = secondaryGreen,
                        inactiveTrackColor = Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.padding(bottom = 0.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "100", fontSize = 12.sp, color = textGray)
                    Text(text = "250", fontSize = 12.sp, color = textGray)
                }

                // 4. Calculate Button
                Button(
                    onClick = { viewModel.onAction(FertilizerAction.Calculate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    enabled = !uiState.isCalculating
                ) {
                    if (uiState.isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Calculate Recipe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            size = Size(size.width, size.height)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FertilizerScreenPreview() {
    FertilizerScreen()
}


