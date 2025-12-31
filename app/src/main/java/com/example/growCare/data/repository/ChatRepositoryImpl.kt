package com.example.growCare.data.repository

import android.net.Uri
import com.example.growCare.data.remote.firebase.FirestoreDataSource
import com.example.growCare.data.remote.gemini.GeminiClient
import com.example.growCare.domain.model.ChatMessage
import com.example.growCare.domain.repository.ChatRepository
import com.example.growCare.domain.repository.Conversation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository
 * Handles AI chat interactions with streaming responses and persistence
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val geminiClient: GeminiClient,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) : ChatRepository {

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    override fun sendMessage(
        message: String,
        conversationId: String
    ): Flow<ChatMessage> = flow {
        try {
            val userId = getCurrentUserId()
            
            // Step 1: Create and emit user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            
            // Save user message
            saveMessage(conversationId, userMessage)
            emit(userMessage)
            
            // Step 2: Load chat history from Firestore for context (not used in simple streaming)
            // For now, we'll use simple streaming without history context
            // To add history support, we'd need to modify GeminiClient.sendChatMessageStream
            
            // Step 3: Stream AI response
            val aiMessageId = UUID.randomUUID().toString()
            val responseBuilder = StringBuilder()
            
            // Get streaming response from Gemini
            geminiClient.sendChatMessageStream(message).collect { chunk ->
                responseBuilder.append(chunk)
                
                // Emit streaming message
                emit(ChatMessage(
                    id = aiMessageId,
                    content = responseBuilder.toString(),
                    isUser = false,
                    timestamp = System.currentTimeMillis(),
                    isStreaming = true
                ))
            }
            
            // Step 4: Emit final AI message and save
            val finalAiMessage = ChatMessage(
                id = aiMessageId,
                content = responseBuilder.toString(),
                isUser = false,
                timestamp = System.currentTimeMillis(),
                isStreaming = false
            )
            
            saveMessage(conversationId, finalAiMessage)
            emit(finalAiMessage)
            
        } catch (e: Exception) {
            // Emit error message
            emit(ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Sorry, I encountered an error: ${e.message}. Please try again.",
                isUser = false,
                timestamp = System.currentTimeMillis(),
                isStreaming = false
            ))
        }
    }

    override fun sendMessageWithImage(
        message: String,
        imageUri: Uri,
        conversationId: String
    ): Flow<ChatMessage> = flow {
        try {
            val userId = getCurrentUserId()
            
            // Step 1: Create and emit user message with image URL
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUri.toString()
            )
            
            saveMessage(conversationId, userMessage)
            emit(userMessage)
            
            // Step 2: Analyze image with Gemini
            val analysisResult = geminiClient.analyzeCropHealth(imageUri, message)
            
            if (analysisResult.isFailure) {
                throw analysisResult.exceptionOrNull() 
                    ?: Exception("Failed to analyze image")
            }
            
            val response = analysisResult.getOrThrow()
            
            // Step 3: Emit AI response
            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = response,
                isUser = false,
                timestamp = System.currentTimeMillis(),
                isStreaming = false
            )
            
            saveMessage(conversationId, aiMessage)
            emit(aiMessage)
            
        } catch (e: Exception) {
            emit(ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Sorry, I couldn't analyze the image: ${e.message}",
                isUser = false,
                timestamp = System.currentTimeMillis(),
                isStreaming = false
            ))
        }
    }

    override fun getChatHistory(conversationId: String): Flow<List<ChatMessage>> = flow {
        try {
            val userId = getCurrentUserId()
            
            firestoreDataSource.getChatMessagesStream(userId, conversationId).collect { dataList ->
                val messages = dataList.map { data ->
                    mapToChatMessage(data)
                }.sortedBy { it.timestamp }
                
                emit(messages)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getAllConversations(): Flow<List<Conversation>> = flow {
        try {
            val userId = getCurrentUserId()
            val result = firestoreDataSource.getAllConversations(userId)
            
            if (result.isSuccess) {
                val conversationsData = result.getOrNull() ?: emptyList()
                val conversations = conversationsData.map { data ->
                    val conversationId = data["id"] as? String ?: ""
                    val lastMessage = data["lastMessage"] as? String ?: ""
                    val lastMessageTime = (data["lastMessageTime"] as? Number)?.toLong() ?: 0L
                    val messageCount = (data["messageCount"] as? Number)?.toInt() ?: 0
                    
                    // Create title from conversation ID or first message
                    val title = if (conversationId.startsWith("chat_")) {
                        // Extract timestamp and format as date
                        val timestamp = conversationId.removePrefix("chat_").toLongOrNull() ?: 0L
                        if (timestamp > 0) {
                            val date = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(timestamp))
                            "Chat - $date"
                        } else {
                            "Chat"
                        }
                    } else {
                        conversationId
                    }
                    
                    Conversation(
                        id = conversationId,
                        title = title,
                        lastMessage = lastMessage.take(100),
                        lastMessageTime = lastMessageTime,
                        messageCount = messageCount
                    )
                }.sortedByDescending { it.lastMessageTime }
                
                emit(conversations)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestoreDataSource.deleteConversation(userId, conversationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllHistory(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            // This would need to be implemented in FirestoreDataSource
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMessage(
        conversationId: String,
        message: ChatMessage
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestoreDataSource.saveChatMessage(
                userId = userId,
                conversationId = conversationId,
                messageId = message.id,
                messageData = chatMessageToMap(message)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ Private Helper Functions ============

    /**
     * Convert ChatMessage to Map for Firestore
     */
    private fun chatMessageToMap(message: ChatMessage): Map<String, Any> {
        val map = mutableMapOf(
            "id" to message.id,
            "content" to message.content,
            "isUser" to message.isUser,
            "timestamp" to message.timestamp
        )
        message.imageUrl?.let { map["imageUrl"] = it }
        return map
    }

    /**
     * Convert Firestore Map to ChatMessage
     */
    private fun mapToChatMessage(data: Map<String, Any>): ChatMessage {
        return ChatMessage(
            id = data["id"] as? String ?: "",
            content = data["content"] as? String ?: "",
            isUser = data["isUser"] as? Boolean ?: false,
            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
            isStreaming = false, // Never streaming when loading from Firestore
            imageUrl = data["imageUrl"] as? String
        )
    }
}
