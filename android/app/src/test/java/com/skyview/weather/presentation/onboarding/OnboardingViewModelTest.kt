package com.skyview.weather.presentation.onboarding

import com.skyview.weather.core.security.KeyManager
import com.skyview.weather.data.local.PreferencesManager
import com.skyview.weather.data.repository.VaultRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OnboardingViewModel.
 *
 * Tests vault initialization, password validation, and onboarding completion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var keyManager: KeyManager
    private lateinit var vaultRepository: VaultRepository
    private lateinit var preferencesManager: PreferencesManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        keyManager = mockk(relaxed = true)
        vaultRepository = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        // Set up mocks
        coEvery { keyManager.storeMasterPassword(any()) } just Runs
        coEvery { vaultRepository.setMasterPassword(any()) } just Runs
        coEvery { preferencesManager.setVaultInitialized(any()) } just Runs
        coEvery { preferencesManager.setOnboardingCompleted(any()) } just Runs

        viewModel = OnboardingViewModel(
            keyManager = keyManager,
            vaultRepository = vaultRepository,
            preferencesManager = preferencesManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct defaults`() {
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.vaultInitialized)
    }

    @Test
    fun `initializeVault with strong password succeeds`() = runTest {
        // Given
        val strongPassword = "StrongPass123!"
        var completionResult = false

        // When
        viewModel.initializeVault(strongPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertTrue(completionResult)
        assertTrue(viewModel.uiState.value.vaultInitialized)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)

        coVerify { keyManager.storeMasterPassword(strongPassword) }
        coVerify { vaultRepository.setMasterPassword(strongPassword) }
        coVerify { preferencesManager.setVaultInitialized(true) }
    }

    @Test
    fun `initializeVault with weak password fails - no uppercase`() = runTest {
        // Given
        val weakPassword = "weakpass123!"
        var completionResult = true

        // When
        viewModel.initializeVault(weakPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertFalse(viewModel.uiState.value.vaultInitialized)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("at least 8 characters"))

        coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
    }

    @Test
    fun `initializeVault with weak password fails - no lowercase`() = runTest {
        // Given
        val weakPassword = "WEAKPASS123!"
        var completionResult = true

        // When
        viewModel.initializeVault(weakPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertNotNull(viewModel.uiState.value.error)
        coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
    }

    @Test
    fun `initializeVault with weak password fails - no digits`() = runTest {
        // Given
        val weakPassword = "WeakPassword!"
        var completionResult = true

        // When
        viewModel.initializeVault(weakPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertNotNull(viewModel.uiState.value.error)
        coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
    }

    @Test
    fun `initializeVault with weak password fails - no special characters`() = runTest {
        // Given
        val weakPassword = "WeakPassword123"
        var completionResult = true

        // When
        viewModel.initializeVault(weakPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertNotNull(viewModel.uiState.value.error)
        coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
    }

    @Test
    fun `initializeVault with weak password fails - too short`() = runTest {
        // Given
        val weakPassword = "Weak1!"
        var completionResult = true

        // When
        viewModel.initializeVault(weakPassword) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertNotNull(viewModel.uiState.value.error)
        coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
    }

    @Test
    fun `initializeVault sets loading state during operation`() = runTest {
        // Given
        val password = "StrongPass123!"
        var loadingDuringOperation = false

        // Delay the password storage to capture loading state
        coEvery { keyManager.storeMasterPassword(any()) } coAnswers {
            loadingDuringOperation = viewModel.uiState.value.isLoading
            // Continue normally
        }

        // When
        viewModel.initializeVault(password) {}
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringOperation) // Was loading during operation
        assertFalse(viewModel.uiState.value.isLoading) // Not loading after completion
    }

    @Test
    fun `initializeVault handles exception gracefully`() = runTest {
        // Given
        val password = "StrongPass123!"
        val errorMessage = "Keystore unavailable"

        coEvery { keyManager.storeMasterPassword(any()) } throws Exception(errorMessage)

        var completionResult = true

        // When
        viewModel.initializeVault(password) { result ->
            completionResult = result
        }
        advanceUntilIdle()

        // Then
        assertFalse(completionResult)
        assertFalse(viewModel.uiState.value.vaultInitialized)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Failed to initialize vault"))
        assertTrue(viewModel.uiState.value.error!!.contains(errorMessage))
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `completeOnboarding marks onboarding as completed`() = runTest {
        // When
        viewModel.completeOnboarding()
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setOnboardingCompleted(true) }
    }

    @Test
    fun `multiple password requirements are all validated`() = runTest {
        // Test various password combinations
        val testCases = listOf(
            "ValidPass123!" to true,  // All requirements met
            "AnotherGood1@" to true,  // Different special char
            "secure2025#Pass" to true, // Lowercase first
            "nouppercas3!" to false,  // Missing uppercase
            "NOLOWERCASE3!" to false, // Missing lowercase
            "NoNumbers!" to false,    // Missing digits
            "NoSpecial123" to false,  // Missing special chars
            "Short1!" to false        // Too short
        )

        testCases.forEach { (password, shouldSucceed) ->
            // Reset mocks
            clearMocks(keyManager, vaultRepository, preferencesManager)
            coEvery { keyManager.storeMasterPassword(any()) } just Runs
            coEvery { vaultRepository.setMasterPassword(any()) } just Runs
            coEvery { preferencesManager.setVaultInitialized(any()) } just Runs

            var result = false

            // When
            viewModel.initializeVault(password) { completed ->
                result = completed
            }
            advanceUntilIdle()

            // Then
            assertEquals(
                "Password '$password' should ${if (shouldSucceed) "succeed" else "fail"}",
                shouldSucceed,
                result
            )

            if (shouldSucceed) {
                coVerify { keyManager.storeMasterPassword(password) }
            } else {
                coVerify(exactly = 0) { keyManager.storeMasterPassword(any()) }
            }
        }
    }

    @Test
    fun `vault initialization updates all required components`() = runTest {
        // Given
        val password = "SecurePassword123!"

        // When
        viewModel.initializeVault(password) {}
        advanceUntilIdle()

        // Then - verify all components were updated in correct order
        coVerifyOrder {
            keyManager.storeMasterPassword(password)
            preferencesManager.setVaultInitialized(true)
            vaultRepository.setMasterPassword(password)
        }
    }

    @Test
    fun `error state is cleared on successful initialization`() = runTest {
        // Given - first attempt fails
        val weakPassword = "weak"
        viewModel.initializeVault(weakPassword) {}
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error) // Error is set

        // When - second attempt succeeds
        val strongPassword = "StrongPass123!"
        viewModel.initializeVault(strongPassword) {}
        advanceUntilIdle()

        // Then - error should be cleared
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.vaultInitialized)
    }
}
