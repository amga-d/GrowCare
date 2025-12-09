package com.example.growCare.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = ""
)

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}

sealed interface AuthAction {
    data class UpdateEmail(val email: String) : AuthAction
    data class UpdatePassword(val password: String) : AuthAction
    data class UpdateConfirmPassword(val password: String) : AuthAction
    data class UpdateDisplayName(val name: String) : AuthAction
    data object SignIn : AuthAction
    data object SignUp : AuthAction
    data object SignOut : AuthAction
    data object ClearError : AuthAction
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        checkAuthStatus()
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.UpdateEmail -> updateEmail(action.email)
            is AuthAction.UpdatePassword -> updatePassword(action.password)
            is AuthAction.UpdateConfirmPassword -> updateConfirmPassword(action.password)
            is AuthAction.UpdateDisplayName -> updateDisplayName(action.name)
            AuthAction.SignIn -> signIn()
            AuthAction.SignUp -> signUp()
            AuthAction.SignOut -> signOut()
            AuthAction.ClearError -> clearError()
        }
    }

    private fun checkAuthStatus() {
        val currentUser = authDataSource.getCurrentUser()
        _uiState.update { it.copy(isSignedIn = currentUser != null) }
        
        if (currentUser != null) {
            viewModelScope.launch {
                _events.emit(AuthEvent.NavigateToHome)
            }
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    private fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    private fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    private fun updateDisplayName(name: String) {
        _uiState.update { it.copy(displayName = name, error = null) }
    }

    private fun signIn() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (!validateSignIn(email, password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authDataSource.signInWithEmail(email, password)
                .onSuccess { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            email = "",
                            password = ""
                        )
                    }
                    _events.emit(AuthEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Sign in failed"
                        )
                    }
                    _events.emit(AuthEvent.ShowError(error.message ?: "Sign in failed"))
                }
        }
    }

    private fun signUp() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val displayName = _uiState.value.displayName

        if (!validateSignUp(email, password, confirmPassword, displayName)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authDataSource.signUpWithEmail(email, password, displayName)
                .onSuccess { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            email = "",
                            password = "",
                            confirmPassword = "",
                            displayName = ""
                        )
                    }
                    _events.emit(AuthEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Sign up failed"
                        )
                    }
                    _events.emit(AuthEvent.ShowError(error.message ?: "Sign up failed"))
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authDataSource.signOut()
            _uiState.update { 
                AuthUiState(isSignedIn = false)
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateSignIn(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _uiState.update { it.copy(error = "Email is required") }
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(error = "Invalid email format") }
                false
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "Password is required") }
                false
            }
            password.length < 6 -> {
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
                false
            }
            else -> true
        }
    }

    private fun validateSignUp(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String
    ): Boolean {
        return when {
            displayName.isBlank() -> {
                _uiState.update { it.copy(error = "Name is required") }
                false
            }
            email.isBlank() -> {
                _uiState.update { it.copy(error = "Email is required") }
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(error = "Invalid email format") }
                false
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "Password is required") }
                false
            }
            password.length < 6 -> {
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
                false
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(error = "Passwords do not match") }
                false
            }
            else -> true
        }
    }
}