package com.example.mobileappdev.presentation.screens.chat

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
import com.example.mobileappdev.R
import com.example.mobileappdev.domain.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundGray = Color(0xFFF8F9FA)
    val textBlack = Color(0xFF1A1A1A)
    val textGray = Color(0xFF757575)
    val inputBackground = Color(0xFFF5F5F5)

    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    // Initial dummy messages
    LaunchedEffect(Unit) {
        messages.add(ChatMessage(
            id = "1",
            content = "Hello! I'm your Agri Assistant. How can I help you with your crops today?",
            isUser = false
        ))
    }

    // Auto scroll to bottom when new message added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Assistant",
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
                        // Camera Button
                        IconButton(
                            onClick = { /* TODO: Handle Camera */ },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = textGray
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
                        val isSendEnabled = messageText.isNotBlank()
                        IconButton(
                            onClick = {
                                if (isSendEnabled) {
                                    messages.add(ChatMessage(
                                        id = System.currentTimeMillis().toString(),
                                        content = messageText,
                                        isUser = true
                                    ))
                                    messageText = ""
                                    // Simulate AI response
                                    // In real app, this would be an API call
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
        // System Label
        Text(
            text = "Agri Assistant",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4CAF50),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.ai_avatar), // Ensure this drawable exists
                contentDescription = "AI Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

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
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(12.dp)
                )
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
            Image(
                painter = painterResource(id = R.drawable.user_avatar), // Ensure this drawable exists
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}

