package com.example.growCare.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for Cloud Firestore operations
 * Handles CRUD operations for crops, chat messages, and user data
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CROPS = "crops"
        private const val COLLECTION_CHAT_HISTORY = "chat_history"
        private const val COLLECTION_CHAT_MESSAGES = "messages"
        private const val COLLECTION_DISEASE_SCANS = "disease_scans"
        private const val COLLECTION_SEED_SCANS = "seed_scans"
        private const val COLLECTION_FERTILIZER_CALCULATIONS = "fertilizer_calculations"
    }

    // ============ Crop Data Operations ============

    /**
     * Save crop data for a user
     */
    suspend fun saveCropData(userId: String, cropId: String, cropData: Map<String, Any>): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CROPS)
            .document(cropId)
            .set(cropData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get all crops for a user as a real-time stream
     */
    fun getCropDataStream(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CROPS)
            .orderBy("plantedDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val crops = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()

                trySend(crops)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get single crop by ID
     */
    suspend fun getCropById(userId: String, cropId: String): Result<Map<String, Any>?> = try {
        val doc = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CROPS)
            .document(cropId)
            .get()
            .await()
        Result.success(doc.data?.plus("id" to doc.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete crop data
     */
    suspend fun deleteCropData(userId: String, cropId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CROPS)
            .document(cropId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ============ Chat History Operations ============

    /**
     * Save chat message
     */
    suspend fun saveChatMessage(
        userId: String,
        conversationId: String,
        messageId: String,
        messageData: Map<String, Any>
    ): Result<Unit> = try {
        val batch = firestore.batch()
        
        val conversationRef = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CHAT_HISTORY)
            .document(conversationId)
            
        // 1. Save the message
        val messageRef = conversationRef.collection(COLLECTION_CHAT_MESSAGES).document(messageId)
        batch.set(messageRef, messageData)

        // 2. Update conversation metadata
        val conversationUpdate = mapOf(
            "lastMessage" to (messageData["content"] ?: ""),
            "lastMessageTime" to (messageData["timestamp"] ?: System.currentTimeMillis()),
            "messageCount" to com.google.firebase.firestore.FieldValue.increment(1)
        )
        batch.set(conversationRef, conversationUpdate, com.google.firebase.firestore.SetOptions.merge())

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get chat messages as a stream
     */
    fun getChatMessagesStream(userId: String, conversationId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CHAT_HISTORY)
            .document(conversationId)
            .collection(COLLECTION_CHAT_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get all conversations for a user
     */
    suspend fun getAllConversations(userId: String): Result<List<Map<String, Any>>> = try {
        val conversationsSnapshot = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CHAT_HISTORY)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .get()
            .await()

        val conversations = conversationsSnapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            // Only include if it has messages (indicated by lastMessageTime)
            if (data.containsKey("lastMessageTime")) {
                 data.plus("id" to doc.id)
            } else {
                null
            }
        }
        
        Result.success(conversations)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete entire conversation
     */
    suspend fun deleteConversation(userId: String, conversationId: String): Result<Unit> = try {
        // Get all messages in the conversation
        val messages = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CHAT_HISTORY)
            .document(conversationId)
            .collection(COLLECTION_CHAT_MESSAGES)
            .get()
            .await()

        // Delete each message
        val batch = firestore.batch()
        messages.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        // Delete the conversation document
        batch.delete(
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CHAT_HISTORY)
                .document(conversationId)
        )

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ============ Disease Scan Operations ============

    /**
     * Save disease scan result
     */
    suspend fun saveDiseaseAnalysis(
        userId: String,
        scanId: String,
        analysisData: Map<String, Any>
    ): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_DISEASE_SCANS)
            .document(scanId)
            .set(analysisData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get disease scan history
     */
    fun getDiseaseScansStream(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_DISEASE_SCANS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val scans = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()

                trySend(scans)
            }

        awaitClose { listener.remove() }
    }

    // ============ Seed Quality Scan Operations ============

    /**
     * Save seed quality analysis
     */
    suspend fun saveSeedAnalysis(
        userId: String,
        scanId: String,
        analysisData: Map<String, Any>
    ): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_SEED_SCANS)
            .document(scanId)
            .set(analysisData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get seed scan history
     */
    fun getSeedScansStream(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_SEED_SCANS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val scans = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()

                trySend(scans)
            }

        awaitClose { listener.remove() }
    }

    // ============ Fertilizer Calculation Operations ============

    /**
     * Save fertilizer calculation
     */
    suspend fun saveFertilizerCalculation(
        userId: String,
        calculationId: String,
        calculationData: Map<String, Any>
    ): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FERTILIZER_CALCULATIONS)
            .document(calculationId)
            .set(calculationData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get fertilizer calculation history
     */
    fun getFertilizerCalculationsStream(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FERTILIZER_CALCULATIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val calculations = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()

                trySend(calculations)
            }

        awaitClose { listener.remove() }
    }

    // ============ User Profile Operations ============

    /**
     * Save or update user profile
     */
    suspend fun saveUserProfile(userId: String, profileData: Map<String, Any>): Result<Unit> = try {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(profileData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>?> = try {
        val doc = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .await()
        Result.success(doc.data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user data (alias for getUserProfile)
     */
    suspend fun getUserData(userId: String): Result<Map<String, Any>?> = getUserProfile(userId)

    /**
     * Create user profile
     */
    suspend fun createUserProfile(user: com.example.growCare.domain.model.User): Result<Unit> = try {
        val profileData = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to (user.displayName ?: ""),
            "phoneNumber" to (user.phoneNumber ?: ""),
            "profilePictureUrl" to (user.profilePictureUrl ?: ""),
            "location" to (user.location ?: ""),
            "farmSize" to (user.farmSize ?: 0.0),
            "createdAt" to user.createdAt
        )
        saveUserProfile(user.uid, profileData)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(user: com.example.growCare.domain.model.User): Result<Unit> = try {
        val profileData = mapOf(
            "displayName" to (user.displayName ?: ""),
            "phoneNumber" to (user.phoneNumber ?: ""),
            "profilePictureUrl" to (user.profilePictureUrl ?: ""),
            "location" to (user.location ?: ""),
            "farmSize" to (user.farmSize ?: 0.0)
        )
        firestore.collection(COLLECTION_USERS)
            .document(user.uid)
            .update(profileData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete user data
     */
    suspend fun deleteUserData(userId: String): Result<Unit> = try {
        // Delete user document
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
