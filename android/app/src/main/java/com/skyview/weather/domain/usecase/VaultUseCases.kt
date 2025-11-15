package com.skyview.weather.domain.usecase

import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.core.security.KeyManager
import com.skyview.weather.data.repository.VaultRepository
import com.skyview.weather.domain.model.*
import com.skyview.weather.util.VaultItemType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for unlocking the vault.
 */
class UnlockVaultUseCase @Inject constructor(
    private val keyManager: KeyManager,
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        return try {
            if (keyManager.verifyPassword(password)) {
                vaultRepository.setMasterPassword(password)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for locking the vault.
 */
class LockVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    operator fun invoke() {
        vaultRepository.clearMasterPassword()
    }
}

/**
 * Use case for getting all vault items.
 */
class GetVaultItemsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    operator fun invoke(): Flow<Result<List<VaultItem>>> {
        return vaultRepository.getAllItems()
    }
}

/**
 * Use case for getting items by type.
 */
class GetItemsByTypeUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    operator fun invoke(type: VaultItemType): Flow<Result<List<VaultItem>>> {
        return vaultRepository.getItemsByType(type)
    }
}

/**
 * Use case for getting a single vault item.
 */
class GetVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(id: String): Result<VaultItem> {
        return vaultRepository.getItemById(id)
    }
}

/**
 * Use case for creating a vault item.
 */
class CreateVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(request: CreateVaultItemRequest): Result<VaultItem> {
        return vaultRepository.createItem(request)
    }
}

/**
 * Use case for deleting a vault item.
 */
class DeleteVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return vaultRepository.deleteItem(id)
    }
}

/**
 * Use case for toggling starred status.
 */
class ToggleStarredUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return vaultRepository.toggleStarred(id)
    }
}

/**
 * Use case for getting vault statistics.
 */
class GetVaultStatsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(): Result<VaultStats> {
        return vaultRepository.getVaultStats()
    }
}

/**
 * Use case for checking if vault is initialized.
 */
class IsVaultInitializedUseCase @Inject constructor(
    private val keyManager: KeyManager
) {
    operator fun invoke(): Boolean {
        return keyManager.isVaultInitialized()
    }
}

/**
 * Use case for initializing vault with master password.
 */
class InitializeVaultUseCase @Inject constructor(
    private val keyManager: KeyManager
) {
    operator fun invoke(password: String): Result<Unit> {
        return try {
            keyManager.initializeVault(password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
