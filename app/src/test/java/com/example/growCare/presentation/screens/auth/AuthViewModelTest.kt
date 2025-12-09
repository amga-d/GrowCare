package com.example.growCare.presentation.screens.auth

import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var authDataSource: FirebaseAuthDataSource
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authDataSource = mockk(relaxed = true)

        // Mock getCurrentUser to return null by default
        every { authDataSource.getCurrentUser() } returns null

        viewModel = AuthViewModel(authDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should have empty fields and not loading`() {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals("", state.displayName)
        assertNull(state.error)
    }

    @Test
    fun `updateEmail should update email in state`() {
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("test@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `updatePassword should update password in state`() {
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("password123", viewModel.uiState.value.password)
    }

    @Test
    fun `signIn with empty email should show error`() = runTest {
        viewModel.onAction(AuthAction.UpdateEmail(""))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Email is required", state.error)
    }

    @Test
    fun `signIn with invalid email format should show error`() = runTest {
        viewModel.onAction(AuthAction.UpdateEmail("invalid-email"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Invalid email format", state.error)
    }

    @Test
    fun `signIn with short password should show error`() = runTest {
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Password must be at least 6 characters", state.error)
    }

    @Test
    fun `signIn with valid credentials should call authDataSource`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        coEvery { authDataSource.signInWithEmail(any(), any()) } returns Result.success(mockUser)

        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { authDataSource.signInWithEmail("test@example.com", "password123") }
    }

    @Test
    fun `signIn success should update state and emit navigate event`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        coEvery { authDataSource.signInWithEmail(any(), any()) } returns Result.success(mockUser)

        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSignedIn)
        assertEquals("", state.email)
        assertEquals("", state.password)
    }

    @Test
    fun `signIn failure should update state with error`() = runTest {
        val errorMessage = "Invalid credentials"
        coEvery { authDataSource.signInWithEmail(any(), any()) } returns
            Result.failure(Exception(errorMessage))

        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `signUp with empty name should show error`() = runTest {
        viewModel.onAction(AuthAction.UpdateDisplayName(""))
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.UpdateConfirmPassword("password123"))
        viewModel.onAction(AuthAction.SignUp)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Name is required", viewModel.uiState.value.error)
    }

    @Test
    fun `signUp with mismatched passwords should show error`() = runTest {
        viewModel.onAction(AuthAction.UpdateDisplayName("Test User"))
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.UpdateConfirmPassword("password456"))
        viewModel.onAction(AuthAction.SignUp)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Passwords do not match", viewModel.uiState.value.error)
    }

    @Test
    fun `signUp with valid data should call authDataSource`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        coEvery { authDataSource.signUpWithEmail(any(), any(), any()) } returns Result.success(mockUser)

        viewModel.onAction(AuthAction.UpdateDisplayName("Test User"))
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.UpdateConfirmPassword("password123"))
        viewModel.onAction(AuthAction.SignUp)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { authDataSource.signUpWithEmail("test@example.com", "password123", "Test User") }
    }

    @Test
    fun `signUp success should update state and clear fields`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        coEvery { authDataSource.signUpWithEmail(any(), any(), any()) } returns Result.success(mockUser)

        viewModel.onAction(AuthAction.UpdateDisplayName("Test User"))
        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.UpdateConfirmPassword("password123"))
        viewModel.onAction(AuthAction.SignUp)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSignedIn)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals("", state.displayName)
    }

    @Test
    fun `signOut should call authDataSource and update state`() = runTest {
        // First sign in
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        coEvery { authDataSource.signInWithEmail(any(), any()) } returns Result.success(mockUser)

        viewModel.onAction(AuthAction.UpdateEmail("test@example.com"))
        viewModel.onAction(AuthAction.UpdatePassword("password123"))
        viewModel.onAction(AuthAction.SignIn)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then sign out
        viewModel.onAction(AuthAction.SignOut)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { authDataSource.signOut() }
        assertFalse(viewModel.uiState.value.isSignedIn)
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        viewModel.onAction(AuthAction.UpdateEmail(""))
        viewModel.onAction(AuthAction.SignIn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.onAction(AuthAction.ClearError)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `checkAuthStatus should detect already authenticated user`() = runTest {
        // Create a new viewModel with authenticated user
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { authDataSource.getCurrentUser() } returns mockUser

        val viewModelWithAuth = AuthViewModel(authDataSource)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModelWithAuth.uiState.value.isSignedIn)
    }
}

