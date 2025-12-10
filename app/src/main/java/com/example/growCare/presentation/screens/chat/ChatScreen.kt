package com.example.growCare.presentation.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.growCare.domain.model.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundGray = Color(0xFFF8F9FA)
    val textBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF757575)
    val inputBackground = Color(0xFFF5F5F5)

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = uiState.messages
    
    var messageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        showImagePickerDialog = false
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.ScrollToBottom -> {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
                is ChatEvent.ShowError -> {
                    // TODO: Show snackbar with error
                }
                is ChatEvent.ShowMessage -> {
                    // TODO: Show snackbar with message
                }
            }
        }
    }

    // Auto scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Image picker dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Add Image") },
            text = { Text("Choose an option to add an image") },
            confirmButton = {
                TextButton(onClick = { 
                    showImagePickerDialog = false
                    showCameraCapture = true
                }) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(end = 4.dp))
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.padding(end = 4.dp))
                    Text("Gallery")
                }
            }
        )
    }
    
    // Camera capture overlay
    if (showCameraCapture) {
        com.example.growCare.presentation.components.CameraCapture(
            onImageCaptured = { uri ->
                selectedImageUri = uri
                showCameraCapture = false
            },
            onError = { 
                showCameraCapture = false
            },
            onClose = {
                showCameraCapture = false
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.conversationTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textBlack,
                            maxLines = 1
                        )
                        if (uiState.messages.isNotEmpty()) {
                            Text(
                                text = "${uiState.messages.size} messages",
                                fontSize = 12.sp,
                                color = textGray
                            )
                        }
                    }
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
                actions = {
                    IconButton(onClick = { viewModel.onAction(ChatAction.StartNewChat) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "New Chat",
                            tint = primaryGreen
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
            Column {
                // Quick Action Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    val chips = listOf("Pest Identification", "Fertilizer Advice", "Weather")
                    items(chips) { chip ->
                        SuggestionChip(
                            onClick = { messageText = chip },
                            label = { 
                                Text(
                                    text = chip,
                                    color = Color(0xFF388E3C),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(20.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White
                            )
                        )
                    }
                }

                // Input Area
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Preview
                        selectedImageUri?.let { uri ->
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                Card(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                        
                        // Camera/Gallery Button
                        IconButton(
                            onClick = { showImagePickerDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add image",
                                tint = if (selectedImageUri != null) primaryGreen else textGray
                            )
                        }

                        // Text Input Field
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(inputBackground, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                                color = textBlack
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (messageText.isEmpty()) {
                                        Text(
                                            text = "Ask about your crops...",
                                            color = Color(0xFF9E9E9E),
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        // Send Button
                        val isSendEnabled = (messageText.isNotBlank() || selectedImageUri != null) && !uiState.isSending
                        IconButton(
                            onClick = {
                                if (isSendEnabled) {
                                    // Send message with or without image through ViewModel
                                    if (selectedImageUri != null) {
                                        viewModel.onAction(
                                            ChatAction.SendMessageWithImage(
                                                messageText.ifBlank { "Analyze this plant" },
                                                selectedImageUri!!
                                            )
                                        )
                                        selectedImageUri = null
                                    } else {
                                        viewModel.onAction(ChatAction.SendMessage(messageText))
                                    }
                                    messageText = ""
                                    
                                    // Scroll to bottom
                                    scope.launch {
                                        listState.animateScrollToItem(messages.size)
                                    }
                                }
                            },
                            enabled = isSendEnabled,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .background(
                                    if (isSendEnabled) primaryGreen else Color(0xFFBDBDBD),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Bottom Navigation (Optional, if part of main nav)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(56.dp)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = false,
                        onClick = onNavigateToHome,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = textGray,
                            unselectedTextColor = textGray
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat AI") },
                        label = { Text("Chat AI") },
                        selected = true,
                        onClick = { /* Already on Chat */ },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryGreen,
                            selectedTextColor = primaryGreen,
                            indicatorColor = Color(0xFFE8F5E9)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = false,
                        onClick = onNavigateToProfile,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = textGray,
                            unselectedTextColor = textGray
                        )
                    )
                }
            }
        },
        containerColor = backgroundGray
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                if (message.isUser) {
                    UserMessageItem(message)
                } else {
                    AiMessageItem(message)
                }
            }
        }
    }
}

@Composable
fun AiMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // System Label with streaming indicator
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Agri Assistant",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
            if (message.isStreaming) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 16.dp
                ),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp) // Approx 75% of screen width
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 21.sp
                    )
                    if (message.isStreaming) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "●",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Label "You"
        Text(
            text = "You",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 4.dp, end = 48.dp) // Align with bubble start
        )

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 16.dp
                ),
                color = Color(0xFF4CAF50),
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = Color.White,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}

