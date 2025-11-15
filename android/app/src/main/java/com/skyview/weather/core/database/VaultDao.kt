package com.skyview.weather.core.database

import androidx.room.*
import com.skyview.weather.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for vault items.
 */
@Dao
interface VaultItemDao {

    /**
     * Gets all vault items.
     */
    @Query("SELECT * FROM vault_items ORDER BY created_at DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    /**
     * Gets items by type.
     */
    @Query("SELECT * FROM vault_items WHERE type = :type ORDER BY created_at DESC")
    fun getItemsByType(type: String): Flow<List<VaultItemEntity>>

    /**
     * Gets items in a folder.
     */
    @Query("SELECT * FROM vault_items WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun getItemsInFolder(folderId: String): Flow<List<VaultItemEntity>>

    /**
     * Gets starred items.
     */
    @Query("SELECT * FROM vault_items WHERE starred = 1 ORDER BY created_at DESC")
    fun getStarredItems(): Flow<List<VaultItemEntity>>

    /**
     * Gets recent items (last 10).
     */
    @Query("SELECT * FROM vault_items ORDER BY accessed_at DESC LIMIT 10")
    fun getRecentItems(): Flow<List<VaultItemEntity>>

    /**
     * Gets a single item by ID.
     */
    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: String): VaultItemEntity?

    /**
     * Searches items by title.
     */
    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchItems(query: String): Flow<List<VaultItemEntity>>

    /**
     * Inserts an item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    /**
     * Updates an item.
     */
    @Update
    suspend fun updateItem(item: VaultItemEntity)

    /**
     * Deletes an item.
     */
    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    /**
     * Deletes item by ID.
     */
    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    /**
     * Updates item access time.
     */
    @Query("UPDATE vault_items SET accessed_at = :timestamp WHERE id = :id")
    suspend fun updateAccessTime(id: String, timestamp: Long)

    /**
     * Toggles starred status.
     */
    @Query("UPDATE vault_items SET starred = NOT starred WHERE id = :id")
    suspend fun toggleStarred(id: String)

    /**
     * Gets count of items by type.
     */
    @Query("SELECT COUNT(*) FROM vault_items WHERE type = :type")
    suspend fun getItemCountByType(type: String): Int

    /**
     * Gets total item count.
     */
    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun getTotalItemCount(): Int
}

/**
 * Data Access Object for vault folders.
 */
@Dao
interface VaultFolderDao {

    /**
     * Gets all folders.
     */
    @Query("SELECT * FROM vault_folders ORDER BY order_index, name")
    fun getAllFolders(): Flow<List<VaultFolderEntity>>

    /**
     * Gets root folders (no parent).
     */
    @Query("SELECT * FROM vault_folders WHERE parent_id IS NULL ORDER BY order_index, name")
    fun getRootFolders(): Flow<List<VaultFolderEntity>>

    /**
     * Gets subfolders of a folder.
     */
    @Query("SELECT * FROM vault_folders WHERE parent_id = :parentId ORDER BY order_index, name")
    fun getSubfolders(parentId: String): Flow<List<VaultFolderEntity>>

    /**
     * Gets a folder by ID.
     */
    @Query("SELECT * FROM vault_folders WHERE id = :id")
    suspend fun getFolderById(id: String): VaultFolderEntity?

    /**
     * Inserts a folder.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VaultFolderEntity)

    /**
     * Updates a folder.
     */
    @Update
    suspend fun updateFolder(folder: VaultFolderEntity)

    /**
     * Deletes a folder (cascade deletes items and subfolders).
     */
    @Delete
    suspend fun deleteFolder(folder: VaultFolderEntity)

    /**
     * Gets item count in folder.
     */
    @Query("SELECT COUNT(*) FROM vault_items WHERE folder_id = :folderId")
    suspend fun getItemCountInFolder(folderId: String): Int
}

/**
 * Data Access Object for vault settings.
 */
@Dao
interface VaultSettingsDao {

    /**
     * Gets a setting value.
     */
    @Query("SELECT value FROM vault_settings WHERE key = :key")
    suspend fun getSetting(key: String): String?

    /**
     * Sets a setting value.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: VaultSettingsEntity)

    /**
     * Deletes a setting.
     */
    @Query("DELETE FROM vault_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)

    /**
     * Gets all settings.
     */
    @Query("SELECT * FROM vault_settings")
    suspend fun getAllSettings(): List<VaultSettingsEntity>
}

/**
 * Data Access Object for trash/recycle bin.
 */
@Dao
interface VaultTrashDao {

    /**
     * Gets all items in trash.
     */
    @Query("SELECT * FROM vault_trash ORDER BY deleted_at DESC")
    fun getAllTrashItems(): Flow<List<VaultTrashEntity>>

    /**
     * Gets trash item by ID.
     */
    @Query("SELECT * FROM vault_trash WHERE id = :id")
    suspend fun getTrashItemById(id: String): VaultTrashEntity?

    /**
     * Inserts item into trash.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashItem(item: VaultTrashEntity)

    /**
     * Deletes item from trash (permanent).
     */
    @Delete
    suspend fun deleteTrashItem(item: VaultTrashEntity)

    /**
     * Empties trash (deletes all).
     */
    @Query("DELETE FROM vault_trash")
    suspend fun emptyTrash()

    /**
     * Deletes expired trash items (older than 30 days).
     */
    @Query("DELETE FROM vault_trash WHERE deleted_at < :cutoffTime")
    suspend fun deleteExpiredItems(cutoffTime: Long)

    /**
     * Gets trash count.
     */
    @Query("SELECT COUNT(*) FROM vault_trash")
    suspend fun getTrashCount(): Int
}
