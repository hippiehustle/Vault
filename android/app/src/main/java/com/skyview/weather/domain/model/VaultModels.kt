package com.skyview.weather.domain.model

import com.skyview.weather.util.VaultItemType

/**
 * Domain models for vault functionality.
 */

/**
 * Vault item domain model.
 */
data class VaultItem(
    val id: String,
    val type: VaultItemType,
    val title: String,
    val folderId: String? = null,
    val content: ByteArray, // Decrypted content
    val thumbnail: ByteArray? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val accessedAt: Long? = null,
    val starred: Boolean = false,
    val metadata: VaultMetadata? = null,
    val filePath: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultItem

        if (id != other.id) return false
        if (type != other.type) return false
        if (title != other.title) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}

/**
 * Vault item metadata.
 */
sealed class VaultMetadata {
    data class Photo(
        val width: Int,
        val height: Int,
        val size: Long,
        val mimeType: String
    ) : VaultMetadata()

    data class Video(
        val duration: Long,
        val width: Int,
        val height: Int,
        val size: Long,
        val mimeType: String
    ) : VaultMetadata()

    data class Document(
        val size: Long,
        val mimeType: String,
        val pageCount: Int? = null
    ) : VaultMetadata()

    data class Note(
        val wordCount: Int,
        val isFormatted: Boolean
    ) : VaultMetadata()

    data class Password(
        val username: String?,
        val url: String?,
        val strength: Int // 0-4
    ) : VaultMetadata()

    data class Audio(
        val duration: Long,
        val size: Long,
        val mimeType: String
    ) : VaultMetadata()

    data class Contact(
        val phoneNumber: String?,
        val email: String?
    ) : VaultMetadata()
}

/**
 * Vault folder domain model.
 */
data class VaultFolder(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Long,
    val color: String? = null,
    val orderIndex: Int = 0,
    val itemCount: Int = 0
)

/**
 * Vault statistics.
 */
data class VaultStats(
    val totalItems: Int,
    val photoCount: Int,
    val videoCount: Int,
    val documentCount: Int,
    val noteCount: Int,
    val passwordCount: Int,
    val audioCount: Int,
    val contactCount: Int,
    val totalSize: Long
)

/**
 * Create vault item request.
 */
data class CreateVaultItemRequest(
    val type: VaultItemType,
    val title: String,
    val content: ByteArray,
    val folderId: String? = null,
    val metadata: VaultMetadata? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateVaultItemRequest

        if (type != other.type) return false
        if (title != other.title) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
