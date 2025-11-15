package com.skyview.weather.data.repository

import com.google.gson.Gson
import com.skyview.weather.core.database.*
import com.skyview.weather.core.security.EncryptionService
import com.skyview.weather.data.model.*
import com.skyview.weather.domain.model.*
import com.skyview.weather.util.VaultItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for vault data operations.
 *
 * Handles encryption/decryption of vault items and folder management.
 *
 * @property vaultItemDao DAO for vault items
 * @property vaultFolderDao DAO for folders
 * @property vaultTrashDao DAO for trash
 * @property encryptionService Encryption service
 */
@Singleton
class VaultRepository @Inject constructor(
    private val vaultItemDao: VaultItemDao,
    private val vaultFolderDao: VaultFolderDao,
    private val vaultTrashDao: VaultTrashDao,
    private val encryptionService: EncryptionService
) {

    private val gson = Gson()
    private var currentPassword: String? = null

    /**
     * Sets the master password for encryption/decryption.
     * Must be called after successful authentication.
     */
    fun setMasterPassword(password: String) {
        currentPassword = password
    }

    /**
     * Clears the master password from memory.
     */
    fun clearMasterPassword() {
        currentPassword = null
    }

    /**
     * Gets all vault items (decrypted).
     */
    fun getAllItems(): Flow<Result<List<VaultItem>>> {
        return vaultItemDao.getAllItems().map { entities ->
            try {
                val items = entities.mapNotNull { entity ->
                    decryptVaultItem(entity)
                }
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Gets items by type.
     */
    fun getItemsByType(type: VaultItemType): Flow<Result<List<VaultItem>>> {
        return vaultItemDao.getItemsByType(type.name).map { entities ->
            try {
                val items = entities.mapNotNull { entity ->
                    decryptVaultItem(entity)
                }
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Gets items in a folder.
     */
    fun getItemsInFolder(folderId: String): Flow<Result<List<VaultItem>>> {
        return vaultItemDao.getItemsInFolder(folderId).map { entities ->
            try {
                val items = entities.mapNotNull { entity ->
                    decryptVaultItem(entity)
                }
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Gets starred items.
     */
    fun getStarredItems(): Flow<Result<List<VaultItem>>> {
        return vaultItemDao.getStarredItems().map { entities ->
            try {
                val items = entities.mapNotNull { entity ->
                    decryptVaultItem(entity)
                }
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Gets a single item by ID.
     */
    suspend fun getItemById(id: String): Result<VaultItem> {
        return try {
            val entity = vaultItemDao.getItemById(id)
                ?: return Result.failure(Exception("Item not found"))

            val item = decryptVaultItem(entity)
                ?: return Result.failure(Exception("Failed to decrypt item"))

            // Update access time
            vaultItemDao.updateAccessTime(id, System.currentTimeMillis())

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new vault item.
     */
    suspend fun createItem(request: CreateVaultItemRequest): Result<VaultItem> {
        return try {
            val password = currentPassword
                ?: return Result.failure(Exception("Vault not unlocked"))

            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            // Encrypt content
            val encryptedData = encryptionService.encrypt(request.content, password)

            // Hash content for integrity
            val contentHash = encryptionService.hashToHex(
                encryptionService.hash(request.content)
            )

            // Serialize metadata if present
            val metadataJson = request.metadata?.let { gson.toJson(it) }
            val encryptedMetadata = metadataJson?.let {
                encryptionService.encryptString(it, password)
            }

            val entity = VaultItemEntity(
                id = id,
                type = request.type.name,
                title = request.title,
                folder_id = request.folderId,
                encrypted_content = encryptedData.ciphertext,
                content_hash = contentHash,
                thumbnail = null, // TODO: Generate thumbnails for images/videos
                created_at = now,
                updated_at = now,
                accessed_at = now,
                starred = false,
                metadata = encryptedMetadata?.let {
                    "${it.salt.joinToString(",")};${it.iv.joinToString(",")};${it.ciphertext.joinToString(",")}"
                },
                file_path = null
            )

            vaultItemDao.insertItem(entity)

            val item = VaultItem(
                id = id,
                type = request.type,
                title = request.title,
                folderId = request.folderId,
                content = request.content,
                thumbnail = null,
                createdAt = now,
                updatedAt = now,
                accessedAt = now,
                starred = false,
                metadata = request.metadata,
                filePath = null
            )

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a vault item.
     */
    suspend fun updateItem(id: String, content: ByteArray, metadata: VaultMetadata?): Result<Unit> {
        return try {
            val password = currentPassword
                ?: return Result.failure(Exception("Vault not unlocked"))

            val entity = vaultItemDao.getItemById(id)
                ?: return Result.failure(Exception("Item not found"))

            val now = System.currentTimeMillis()

            // Encrypt new content
            val encryptedData = encryptionService.encrypt(content, password)
            val contentHash = encryptionService.hashToHex(
                encryptionService.hash(content)
            )

            // Serialize metadata
            val metadataJson = metadata?.let { gson.toJson(it) }
            val encryptedMetadata = metadataJson?.let {
                encryptionService.encryptString(it, password)
            }

            val updatedEntity = entity.copy(
                encrypted_content = encryptedData.ciphertext,
                content_hash = contentHash,
                updated_at = now,
                metadata = encryptedMetadata?.let {
                    "${it.salt.joinToString(",")};${it.iv.joinToString(",")};${it.ciphertext.joinToString(",")}"
                }
            )

            vaultItemDao.updateItem(updatedEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a vault item (moves to trash).
     */
    suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            val entity = vaultItemDao.getItemById(id)
                ?: return Result.failure(Exception("Item not found"))

            // Move to trash
            val trashItem = VaultTrashEntity(
                id = UUID.randomUUID().toString(),
                original_item_id = entity.id,
                type = entity.type,
                title = entity.title,
                encrypted_content = entity.encrypted_content,
                content_hash = entity.content_hash,
                thumbnail = entity.thumbnail,
                metadata = entity.metadata,
                file_path = entity.file_path,
                deleted_at = System.currentTimeMillis(),
                original_folder_id = entity.folder_id
            )

            vaultTrashDao.insertTrashItem(trashItem)
            vaultItemDao.deleteItem(entity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggles starred status of an item.
     */
    suspend fun toggleStarred(id: String): Result<Unit> {
        return try {
            vaultItemDao.toggleStarred(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets vault statistics.
     */
    suspend fun getVaultStats(): Result<VaultStats> {
        return try {
            val stats = VaultStats(
                totalItems = vaultItemDao.getTotalItemCount(),
                photoCount = vaultItemDao.getItemCountByType(VaultItemType.PHOTO.name),
                videoCount = vaultItemDao.getItemCountByType(VaultItemType.VIDEO.name),
                documentCount = vaultItemDao.getItemCountByType(VaultItemType.DOCUMENT.name),
                noteCount = vaultItemDao.getItemCountByType(VaultItemType.NOTE.name),
                passwordCount = vaultItemDao.getItemCountByType(VaultItemType.PASSWORD.name),
                audioCount = vaultItemDao.getItemCountByType(VaultItemType.AUDIO.name),
                contactCount = vaultItemDao.getItemCountByType(VaultItemType.CONTACT.name),
                totalSize = 0L // TODO: Calculate from file paths
            )
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all folders.
     */
    fun getAllFolders(): Flow<List<VaultFolder>> {
        return vaultFolderDao.getAllFolders().map { entities ->
            entities.map { it.toVaultFolder() }
        }
    }

    /**
     * Creates a new folder.
     */
    suspend fun createFolder(name: String, parentId: String? = null, color: String? = null): Result<VaultFolder> {
        return try {
            val id = UUID.randomUUID().toString()
            val entity = VaultFolderEntity(
                id = id,
                name = name,
                parent_id = parentId,
                created_at = System.currentTimeMillis(),
                color = color
            )

            vaultFolderDao.insertFolder(entity)
            Result.success(entity.toVaultFolder())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a folder (and all contents).
     */
    suspend fun deleteFolder(id: String): Result<Unit> {
        return try {
            val entity = vaultFolderDao.getFolderById(id)
                ?: return Result.failure(Exception("Folder not found"))

            vaultFolderDao.deleteFolder(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decrypts a vault item entity.
     */
    private fun decryptVaultItem(entity: VaultItemEntity): VaultItem? {
        val password = currentPassword ?: return null

        return try {
            // Parse encrypted data
            val encryptedData = EncryptionService.EncryptedData(
                ciphertext = entity.encrypted_content,
                iv = ByteArray(0), // Not stored separately in current implementation
                salt = ByteArray(0)
            )

            // For now, use simplified decryption
            // In production, properly parse salt and IV from entity
            val content = entity.encrypted_content // Placeholder

            VaultItem(
                id = entity.id,
                type = VaultItemType.valueOf(entity.type),
                title = entity.title,
                folderId = entity.folder_id,
                content = content,
                thumbnail = entity.thumbnail,
                createdAt = entity.created_at,
                updatedAt = entity.updated_at,
                accessedAt = entity.accessed_at,
                starred = entity.starred,
                metadata = null, // TODO: Decrypt and parse metadata
                filePath = entity.file_path
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts entity to domain model.
     */
    private fun VaultFolderEntity.toVaultFolder(): VaultFolder {
        return VaultFolder(
            id = id,
            name = name,
            parentId = parent_id,
            createdAt = created_at,
            color = color,
            orderIndex = order_index,
            itemCount = 0 // TODO: Query actual count
        )
    }
}
