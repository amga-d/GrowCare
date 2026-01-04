package com.example.growCare.data.repository

import android.net.Uri
import com.example.growCare.data.local.database.dao.UserDao
import com.example.growCare.data.mapper.UserMapper
import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import com.example.growCare.data.remote.firebase.FirebaseStorageDataSource
import com.example.growCare.data.remote.firebase.FirestoreUserDataSource
import com.example.growCare.domain.model.User
import com.example.growCare.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of UserRepository
 * Handles user profile data using Firebase and local Room database
 */
class UserRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreUserDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : UserRepository {

    override fun getUserProfile(): Flow<User?> {
        val currentUser = authDataSource.getCurrentUser() ?: return kotlinx.coroutines.flow.flowOf(null)

        return kotlinx.coroutines.flow.flow {
            // First, try to get from Firestore and update local
            try {
                val firestoreUser = firestoreDataSource.getUserByIdOnce(currentUser.uid)
                if (firestoreUser != null) {
                    userDao.insertUser(firestoreUser)
                    emit(userMapper.toDomain(firestoreUser))
                }
            } catch (e: Exception) {
                // If Firestore fails, will fallback to local below
            }
            
            // Then observe local database for changes
            userDao.getUserById(currentUser.uid)
                .collect { userEntity ->
                    emit(userEntity?.let { userMapper.toDomain(it) })
                }
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            // Update in Firestore
            val firestoreResult = firestoreDataSource.updateUserProfile(
                userId = user.uid,
                updates = mapOf(
                    "displayName" to user.displayName,
                    "phoneNumber" to user.phoneNumber,
                    "location" to user.location,
                    "farmSize" to user.farmSize,
                    "profilePictureUrl" to user.profilePictureUrl
                )
            )
            
            firestoreResult.onSuccess {
                // Update local database
                userDao.insertUser(userMapper.toEntity(user))
            }
            
            firestoreResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePicture(imageUri: Uri): Result<String> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return storageDataSource.uploadProfilePicture(
            userId = currentUser.uid,
            imageUri = imageUri
        ).onSuccess { downloadUrl ->
            // Update Firestore with new profile picture URL
            firestoreDataSource.updateUserProfile(
                userId = currentUser.uid,
                updates = mapOf("profilePictureUrl" to downloadUrl)
            )
        }
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreDataSource.updateUserProfile(
            userId = currentUser.uid,
            updates = mapOf("displayName" to displayName)
        )
    }

    override suspend fun updatePhoneNumber(phoneNumber: String): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreDataSource.updateUserProfile(
            userId = currentUser.uid,
            updates = mapOf("phoneNumber" to phoneNumber)
        )
    }

    override suspend fun updateLocation(location: String): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreDataSource.updateUserProfile(
            userId = currentUser.uid,
            updates = mapOf("location" to location)
        )
    }

    override suspend fun updateFarmSize(farmSize: Double): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreDataSource.updateUserProfile(
            userId = currentUser.uid,
            updates = mapOf("farmSize" to farmSize)
        )
    }

    override suspend fun updatePreferredCrops(crops: List<String>): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreDataSource.updateUserProfile(
            userId = currentUser.uid,
            updates = mapOf("preferredCrops" to crops)
        )
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val firestoreUser = firestoreDataSource.getUserByIdOnce(userId)
            Result.success(firestoreUser?.let { userMapper.toDomain(it) })
        } catch (e: Exception) {
            // Fallback to local
            try {
                var localUser: User? = null
                userDao.getUserById(userId).collect { userEntity ->
                    localUser = userEntity?.let { userMapper.toDomain(it) }
                }
                Result.success(localUser)
            } catch (localError: Exception) {
                Result.failure(localError)
            }
        }
    }

    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            // Save to Firestore
            firestoreDataSource.setUserProfile(userMapper.toEntity(user)).onSuccess {
                // Save to local database
                userDao.insertUser(userMapper.toEntity(user))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUserData(): Result<Unit> {
        val currentUser = authDataSource.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        return try {
            // Delete from Firestore
            firestoreDataSource.deleteUser(currentUser.uid).onSuccess {
                // Delete from local database
                userDao.deleteUserById(currentUser.uid)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun profileExists(userId: String): Boolean {
        return try {
            val user = firestoreDataSource.getUserByIdOnce(userId)
            user != null
        } catch (e: Exception) {
            false
        }
    }
}
