package com.example.growCare.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import com.example.growCare.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ProfileAction {
    data object LoadProfile : ProfileAction
    data object SignOut : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.LoadProfile -> loadUserProfile()
            ProfileAction.SignOut -> signOut()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val firebaseUser = authDataSource.getCurrentUser()
                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "User",
                        profilePictureUrl = firebaseUser.photoUrl?.toString(),
                        phoneNumber = firebaseUser.phoneNumber,
                        createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
                        lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
                    )
                    _uiState.update { it.copy(user = user, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            user = null,
                            isLoading = false,
                            error = "No user logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authDataSource.signOut()
            _uiState.update { ProfileUiState() }
        }
    }
}