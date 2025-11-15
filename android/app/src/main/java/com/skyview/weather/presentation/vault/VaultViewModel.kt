package com.skyview.weather.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyview.weather.domain.model.VaultItem
import com.skyview.weather.domain.model.VaultStats
import com.skyview.weather.domain.usecase.*
import com.skyview.weather.util.VaultItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for vault screens.
 */
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val unlockVaultUseCase: UnlockVaultUseCase,
    private val lockVaultUseCase: LockVaultUseCase,
    private val getVaultItemsUseCase: GetVaultItemsUseCase,
    private val getItemsByTypeUseCase: GetItemsByTypeUseCase,
    private val getVaultItemUseCase: GetVaultItemUseCase,
    private val deleteVaultItemUseCase: DeleteVaultItemUseCase,
    private val toggleStarredUseCase: ToggleStarredUseCase,
    private val getVaultStatsUseCase: GetVaultStatsUseCase,
    private val isVaultInitializedUseCase: IsVaultInitializedUseCase,
    private val initializeVaultUseCase: InitializeVaultUseCase
) : ViewModel() {

    /**
     * UI state for vault screen.
     */
    data class VaultUiState(
        val isUnlocked: Boolean = false,
        val isInitialized: Boolean = false,
        val items: List<VaultItem> = emptyList(),
        val currentItem: VaultItem? = null,
        val stats: VaultStats? = null,
        val filterType: VaultItemType? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        checkInitialization()
    }

    /**
     * Checks if vault is initialized.
     */
    private fun checkInitialization() {
        val isInitialized = isVaultInitializedUseCase()
        _uiState.update { it.copy(isInitialized = isInitialized) }
    }

    /**
     * Initializes vault with master password.
     */
    fun initializeVault(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            initializeVaultUseCase(password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isInitialized = true,
                            isLoading = false,
                            error = null
                        )
                    }
                    // Auto-unlock after initialization
                    unlockVault(password)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to initialize vault"
                        )
                    }
                }
        }
    }

    /**
     * Unlocks vault with password.
     */
    fun unlockVault(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            unlockVaultUseCase(password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isUnlocked = true,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadVaultItems()
                    loadVaultStats()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Invalid password"
                        )
                    }
                }
        }
    }

    /**
     * Locks vault.
     */
    fun lockVault() {
        lockVaultUseCase()
        _uiState.update {
            VaultUiState(
                isInitialized = it.isInitialized,
                isUnlocked = false
            )
        }
    }

    /**
     * Loads all vault items.
     */
    private fun loadVaultItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val flow = if (_uiState.value.filterType != null) {
                getItemsByTypeUseCase(_uiState.value.filterType!!)
            } else {
                getVaultItemsUseCase()
            }

            flow
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load items"
                        )
                    }
                }
                .collect { result ->
                    result
                        .onSuccess { items ->
                            _uiState.update {
                                it.copy(
                                    items = items,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load items"
                                )
                            }
                        }
                }
        }
    }

    /**
     * Loads vault statistics.
     */
    private fun loadVaultStats() {
        viewModelScope.launch {
            getVaultStatsUseCase()
                .onSuccess { stats ->
                    _uiState.update { it.copy(stats = stats) }
                }
        }
    }

    /**
     * Filters items by type.
     */
    fun filterByType(type: VaultItemType?) {
        _uiState.update { it.copy(filterType = type) }
        if (_uiState.value.isUnlocked) {
            loadVaultItems()
        }
    }

    /**
     * Gets a single item.
     */
    fun loadItem(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getVaultItemUseCase(id)
                .onSuccess { item ->
                    _uiState.update {
                        it.copy(
                            currentItem = item,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load item"
                        )
                    }
                }
        }
    }

    /**
     * Deletes an item.
     */
    fun deleteItem(id: String) {
        viewModelScope.launch {
            deleteVaultItemUseCase(id)
                .onSuccess {
                    loadVaultItems()
                    loadVaultStats()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to delete item")
                    }
                }
        }
    }

    /**
     * Toggles starred status.
     */
    fun toggleStarred(id: String) {
        viewModelScope.launch {
            toggleStarredUseCase(id)
                .onSuccess {
                    loadVaultItems()
                }
        }
    }

    /**
     * Clears error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clears current item.
     */
    fun clearCurrentItem() {
        _uiState.update { it.copy(currentItem = null) }
    }
}
