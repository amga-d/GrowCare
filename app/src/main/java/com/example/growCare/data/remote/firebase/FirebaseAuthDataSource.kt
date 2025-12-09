package com.example.growCare.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for Firebase Authentication operations
 * Handles user sign-in, sign-up, and authentication state
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    /**
     * Sign in with email and password
     * @return FirebaseUser if successful, null otherwise
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        if (result.user != null) {
            Result.success(result.user!!)
        } else {
            Result.failure(Exception("Sign in failed: User is null"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Create new user with email and password
     * @return FirebaseUser if successful, null otherwise
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        if (result.user != null) {
            Result.success(result.user!!)
        } else {
            Result.failure(Exception("Sign up failed: User is null"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get currently authenticated user
     * @return FirebaseUser if logged in, null otherwise
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = auth.currentUser != null

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update user display name
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete current user account
     */
    suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
