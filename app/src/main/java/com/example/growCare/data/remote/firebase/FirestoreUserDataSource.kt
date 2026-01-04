package com.example.growCare.data.remote.firebase

import com.example.growCare.data.local.database.entity.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Data source for user profile operations with Firestore
 */
class FirestoreUserDataSource @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Get user profile as a real-time stream
     */
    fun getUserProfile(userId: String): Flow<UserEntity?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(UserEntity::class.java)
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get user profile once (for single read)
     */
    suspend fun getUserByIdOnce(userId: String): UserEntity? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(UserEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update user profile fields
     */
    suspend fun updateUserProfile(
        userId: String,
        updates: Map<String, Any?>
    ): Result<Unit> = suspendCoroutine { continuation ->
        usersCollection.document(userId)
            .update(updates)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Create or update complete user profile
     */
    suspend fun setUserProfile(user: UserEntity): Result<Unit> = suspendCoroutine { continuation ->
        usersCollection.document(user.uid)
            .set(user)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Delete user profile
     */
    suspend fun deleteUser(userId: String): Result<Unit> = suspendCoroutine { continuation ->
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Check if user profile exists
     */
    suspend fun userExists(userId: String): Boolean {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }
}
