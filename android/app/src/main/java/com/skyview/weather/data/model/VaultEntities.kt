package com.skyview.weather.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.skyview.weather.util.VaultItemType

/**
 * Database entities for vault storage.
 */

/**
 * Vault item entity.
 * Stores encrypted content in the database.
 */
@Entity(
    tableName = "vault_items",
    foreignKeys = [
        ForeignKey(
            entity = VaultFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("folder_id"),
        Index("type"),
        Index("starred"),
        Index("created_at")
    ]
)
data class VaultItemEntity(
    @PrimaryKey
    val id: String,
    val type: String, // VaultItemType as string
    val title: String,
    val folder_id: String? = null,
    val encrypted_content: ByteArray, // Encrypted data blob
    val content_hash: String, // SHA-256 hash for integrity
    val thumbnail: ByteArray? = null, // Small encrypted thumbnail for images/videos
    val created_at: Long,
    val updated_at: Long,
    val accessed_at: Long? = null,
    val starred: Boolean = false,
    val metadata: String? = null, // JSON encrypted metadata
    val file_path: String? = null // Path to encrypted file for large content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultItemEntity

        if (id != other.id) return false
        if (type != other.type) return false
        if (title != other.title) return false
        if (folder_id != other.folder_id) return false
        if (!encrypted_content.contentEquals(other.encrypted_content)) return false
        if (content_hash != other.content_hash) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (folder_id?.hashCode() ?: 0)
        result = 31 * result + encrypted_content.contentHashCode()
        result = 31 * result + content_hash.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Vault folder entity.
 * Organizes vault items into folders.
 */
@Entity(
    tableName = "vault_folders",
    foreignKeys = [
        ForeignKey(
            entity = VaultFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parent_id")]
)
data class VaultFolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val parent_id: String? = null,
    val created_at: Long,
    val color: String? = null, // Hex color for visual organization
    val order_index: Int = 0 // For custom ordering
)

/**
 * Vault settings entity.
 * Stores vault configuration.
 */
@Entity(tableName = "vault_settings")
data class VaultSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

/**
 * Deleted items (trash/recycle bin).
 */
@Entity(
    tableName = "vault_trash",
    indices = [Index("deleted_at")]
)
data class VaultTrashEntity(
    @PrimaryKey
    val id: String,
    val original_item_id: String,
    val type: String,
    val title: String,
    val encrypted_content: ByteArray,
    val content_hash: String,
    val thumbnail: ByteArray? = null,
    val metadata: String? = null,
    val file_path: String? = null,
    val deleted_at: Long,
    val original_folder_id: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultTrashEntity

        if (id != other.id) return false
        if (!encrypted_content.contentEquals(other.encrypted_content)) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encrypted_content.contentHashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        return result
    }
}
