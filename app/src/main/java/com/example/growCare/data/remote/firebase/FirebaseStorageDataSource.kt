package com.example.growCare.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for Firebase Storage operations
 * Handles image upload, download, and deletion
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    companion object {
        private const val FOLDER_DISEASE_SCANS = "disease_scans"
        private const val FOLDER_SEED_SCANS = "seed_scans"
        private const val FOLDER_CROP_IMAGES = "crop_images"
        private const val FOLDER_PROFILE_PICTURES = "profile_pictures"
    }

    /**
     * Upload disease scan image
     * @return Download URL of uploaded image
     */
    suspend fun uploadDiseaseImage(userId: String, imageUri: Uri): Result<String> =
        uploadImage(userId, imageUri, FOLDER_DISEASE_SCANS)

    /**
     * Upload seed scan image
     * @return Download URL of uploaded image
     */
    suspend fun uploadSeedImage(userId: String, imageUri: Uri): Result<String> =
        uploadImage(userId, imageUri, FOLDER_SEED_SCANS)

    /**
     * Upload crop image
     * @return Download URL of uploaded image
     */
    suspend fun uploadCropImage(userId: String, imageUri: Uri): Result<String> =
        uploadImage(userId, imageUri, FOLDER_CROP_IMAGES)

    /**
     * Upload profile picture
     * @return Download URL of uploaded image
     */
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference
                .child("$FOLDER_PROFILE_PICTURES/$userId/profile.jpg")

            // Upload the file
            ref.putFile(imageUri).await()

            // Get download URL
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generic image upload with timestamp
     * @param userId User ID for organizing files
     * @param imageUri Local URI of image to upload
     * @param folder Target folder in storage
     * @return Download URL of uploaded image
     */
    private suspend fun uploadImage(
        userId: String,
        imageUri: Uri,
        folder: String
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val ref = storage.reference
                .child("$folder/$userId/$timestamp.jpg")

            // Upload the file
            ref.putFile(imageUri).await()

            // Get download URL
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download image from URL
     * Note: In most cases, you can use the download URL directly with Coil
     * This method is for when you need the actual file
     */
    suspend fun downloadImage(downloadUrl: String): Result<ByteArray> {
        return try {
            val ref = storage.getReferenceFromUrl(downloadUrl)
            val maxDownloadSize = 10L * 1024 * 1024 // 10 MB max
            val bytes = ref.getBytes(maxDownloadSize).await()
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete image by download URL
     */
    suspend fun deleteImage(downloadUrl: String): Result<Unit> {
        return try {
            val ref = storage.getReferenceFromUrl(downloadUrl)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all images in a user's folder
     */
    suspend fun deleteUserImages(userId: String, folder: String): Result<Unit> {
        return try {
            val ref = storage.reference.child("$folder/$userId")
            
            // List all files
            val listResult = ref.listAll().await()
            
            // Delete each file
            listResult.items.forEach { item ->
                item.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all user data from storage
     */
    suspend fun deleteAllUserData(userId: String): Result<Unit> {
        return try {
            deleteUserImages(userId, FOLDER_DISEASE_SCANS)
            deleteUserImages(userId, FOLDER_SEED_SCANS)
            deleteUserImages(userId, FOLDER_CROP_IMAGES)
            deleteUserImages(userId, FOLDER_PROFILE_PICTURES)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get image metadata
     */
    suspend fun getImageMetadata(downloadUrl: String): Result<Map<String, Any>> {
        return try {
            val ref = storage.getReferenceFromUrl(downloadUrl)
            val metadata = ref.metadata.await()
            
            val metadataMap = mapOf(
                "name" to (metadata.name ?: ""),
                "size" to (metadata.sizeBytes),
                "contentType" to (metadata.contentType ?: ""),
                "timeCreated" to (metadata.creationTimeMillis),
                "updated" to (metadata.updatedTimeMillis)
            )
            
            Result.success(metadataMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
