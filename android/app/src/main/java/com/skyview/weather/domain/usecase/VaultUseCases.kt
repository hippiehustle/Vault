package com.skyview.weather.domain.usecase

import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.core.security.KeyManager
import com.skyview.weather.data.repository.VaultRepository
import com.skyview.weather.domain.model.*
import com.skyview.weather.util.VaultItemType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for unlocking the vault with password authentication.
 *
 * Verifies the provided password against the stored hash and initializes
 * the vault session if successful.
 */
class UnlockVaultUseCase @Inject constructor(
    private val keyManager: KeyManager,
    private val vaultRepository: VaultRepository
) {
    /**
     * Unlocks the vault with the provided master password.
     *
     * @param password Master password to verify
     * @return Result.success if password is correct, Result.failure otherwise
     */
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
 *
 * Clears the master password from memory and ends the vault session.
 */
class LockVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Locks the vault by clearing the master password from memory.
     */
    operator fun invoke() {
        vaultRepository.clearMasterPassword()
    }
}

/**
 * Use case for retrieving all vault items.
 *
 * Returns all items stored in the vault as a Flow for reactive updates.
 */
class GetVaultItemsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Gets all vault items.
     *
     * @return Flow emitting Result containing list of all VaultItems
     */
    operator fun invoke(): Flow<Result<List<VaultItem>>> {
        return vaultRepository.getAllItems()
    }
}

/**
 * Use case for filtering vault items by type.
 *
 * Retrieves only items matching the specified type (photos, videos, documents, etc.).
 */
class GetItemsByTypeUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Gets vault items filtered by type.
     *
     * @param type VaultItemType to filter by (PHOTO, VIDEO, DOCUMENT, NOTE, PASSWORD, etc.)
     * @return Flow emitting Result containing filtered list of VaultItems
     */
    operator fun invoke(type: VaultItemType): Flow<Result<List<VaultItem>>> {
        return vaultRepository.getItemsByType(type)
    }
}

/**
 * Use case for retrieving a single vault item by ID.
 *
 * Fetches and decrypts a specific vault item.
 */
class GetVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Gets a single vault item by its unique identifier.
     *
     * @param id Unique identifier of the vault item
     * @return Result containing the VaultItem if found, failure otherwise
     */
    suspend operator fun invoke(id: String): Result<VaultItem> {
        return vaultRepository.getItemById(id)
    }
}

/**
 * Use case for creating a new vault item.
 *
 * Encrypts and stores a new item in the vault.
 */
class CreateVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Creates a new encrypted vault item.
     *
     * @param request CreateVaultItemRequest containing item details and content
     * @return Result containing the created VaultItem with generated ID
     */
    suspend operator fun invoke(request: CreateVaultItemRequest): Result<VaultItem> {
        return vaultRepository.createItem(request)
    }
}

/**
 * Use case for deleting a vault item.
 *
 * Moves item to trash with 30-day retention before permanent deletion.
 */
class DeleteVaultItemUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Deletes a vault item by moving it to trash.
     *
     * @param id Unique identifier of the item to delete
     * @return Result.success if deletion succeeded, Result.failure otherwise
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        return vaultRepository.deleteItem(id)
    }
}

/**
 * Use case for toggling starred status of a vault item.
 *
 * Allows marking items as favorites for quick access.
 */
class ToggleStarredUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Toggles the starred/favorite status of an item.
     *
     * @param id Unique identifier of the item
     * @return Result.success if toggle succeeded, Result.failure otherwise
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        return vaultRepository.toggleStarred(id)
    }
}

/**
 * Use case for retrieving vault statistics.
 *
 * Provides metrics about vault contents (item counts, sizes, etc.).
 */
class GetVaultStatsUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    /**
     * Gets comprehensive vault statistics.
     *
     * @return Result containing VaultStats with item counts and size information
     */
    suspend operator fun invoke(): Result<VaultStats> {
        return vaultRepository.getVaultStats()
    }
}

/**
 * Use case for checking vault initialization status.
 *
 * Determines if the vault has been set up with a master password.
 */
class IsVaultInitializedUseCase @Inject constructor(
    private val keyManager: KeyManager
) {
    /**
     * Checks if the vault has been initialized.
     *
     * @return true if vault is initialized with master password, false otherwise
     */
    operator fun invoke(): Boolean {
        return keyManager.isPasswordSet()
    }
}

/**
 * Use case for initializing the vault with a new master password.
 *
 * Sets up the vault for first-time use with encryption keys and password.
 */
class InitializeVaultUseCase @Inject constructor(
    private val keyManager: KeyManager,
    private val vaultRepository: VaultRepository
) {
    /**
     * Initializes the vault with a master password.
     *
     * @param password Master password for vault encryption
     * @return Result.success if initialization succeeded, Result.failure otherwise
     */
    suspend operator fun invoke(password: String): Result<Unit> {
        return try {
            keyManager.storeMasterPassword(password)
            vaultRepository.setMasterPassword(password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
