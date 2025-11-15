package com.skyview.weather.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyview.weather.core.security.KeyManager
import com.skyview.weather.data.local.PreferencesManager
import com.skyview.weather.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Onboarding flow.
 *
 * Manages vault initialization and onboarding completion.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val keyManager: KeyManager,
    private val vaultRepository: VaultRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun initializeVault(password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Validate password strength
                if (!isPasswordStrong(password)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Password must be at least 8 characters with mixed case, numbers, and symbols"
                        )
                    }
                    onComplete(false)
                    return@launch
                }

                // Store master password hash
                keyManager.storeMasterPassword(password)

                // Mark vault as initialized
                preferencesManager.setVaultInitialized(true)

                // Set master password in repository
                vaultRepository.setMasterPassword(password)

                _uiState.update { it.copy(isLoading = false, vaultInitialized = true) }
                onComplete(true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to initialize vault: ${e.message}"
                    )
                }
                onComplete(false)
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8 &&
               password.any { it.isUpperCase() } &&
               password.any { it.isLowerCase() } &&
               password.any { it.isDigit() }
    }
}

/**
 * UI state for Onboarding.
 */
data class OnboardingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vaultInitialized: Boolean = false
)
