package com.example.growCare.data.repository

import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import com.example.growCare.data.remote.firebase.FirestoreDataSource
import com.example.growCare.domain.model.User
import com.example.growCare.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository
 * Handles authentication operations using Firebase Authentication
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : AuthRepository {

    /**
     * Sign in with email and password
     */
    override suspend fun signIn(email: String, password: String): Result<User> {
        return authDataSource.signInWithEmail(email, password)
            .mapCatching { firebaseUser ->
                // Get additional user data from Firestore
                val userData = firestoreDataSource.getUserData(firebaseUser.uid).getOrNull()
                firebaseUser.toUser(userData)
            }
    }

    /**
     * Create new account with email and password
     */
    override suspend fun signUp(email: String, password: String, displayName: String?): Result<User> {
        return authDataSource.signUpWithEmail(email, password, displayName ?: "")
            .mapCatching { firebaseUser ->
                // Create user profile in Firestore
                val user = firebaseUser.toUser()
                firestoreDataSource.createUserProfile(user)
                user
            }
    }

    /**
     * Sign out current user
     */
    override suspend fun signOut(): Result<Unit> {
        return try {
            authDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current authenticated user
     */
    override fun getCurrentUser(): User? {
        val firebaseUser = authDataSource.getCurrentUser()
        return firebaseUser?.toUser()
    }

    /**
     * Check if user is authenticated
     */
    override fun isAuthenticated(): Boolean {
        return authDataSource.isAuthenticated()
    }

    /**
     * Observe authentication state changes
     */
    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        // Firebase auth state listener would go here
        // For now, just emit current state
        trySend(isAuthenticated())
        awaitClose { }
    }

    /**
     * Send password reset email
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authDataSource.sendPasswordResetEmail(email)
    }

    /**
     * Update user display name
     */
    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return authDataSource.updateDisplayName(displayName)
    }

    /**
     * Delete user account
     */
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val userId = authDataSource.getCurrentUser()?.uid
            if (userId != null) {
                // Delete user data from Firestore
                firestoreDataSource.deleteUserData(userId).getOrThrow()
                // Delete Firebase Auth account
                authDataSource.deleteAccount().getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert FirebaseUser to domain User model
     */
    private fun FirebaseUser.toUser(userData: Map<String, Any>? = null): User {
        return User(
            uid = this.uid,
            email = this.email ?: "",
            displayName = this.displayName ?: userData?.get("displayName") as? String,
            phoneNumber = this.phoneNumber ?: userData?.get("phoneNumber") as? String,
            profilePictureUrl = this.photoUrl?.toString() ?: userData?.get("profilePictureUrl") as? String,
            location = userData?.get("location") as? String,
            farmSize = userData?.get("farmSize") as? Double,
            createdAt = this.metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }
}

