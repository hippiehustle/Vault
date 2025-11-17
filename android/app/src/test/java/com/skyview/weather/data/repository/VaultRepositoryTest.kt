package com.skyview.weather.data.repository

import com.skyview.weather.core.database.VaultFolderDao
import com.skyview.weather.core.database.VaultFolderEntity
import com.skyview.weather.core.database.VaultItemDao
import com.skyview.weather.core.database.VaultItemEntity
import com.skyview.weather.core.database.VaultTrashDao
import com.skyview.weather.core.security.EncryptedData
import com.skyview.weather.core.security.EncryptionService
import com.skyview.weather.data.model.CreateVaultItemRequest
import com.skyview.weather.util.VaultItemType
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for VaultRepository.
 *
 * Tests vault item CRUD operations, encryption/decryption, and folder management.
 */
class VaultRepositoryTest {

    private lateinit var repository: VaultRepository
    private lateinit var vaultItemDao: VaultItemDao
    private lateinit var vaultFolderDao: VaultFolderDao
    private lateinit var vaultTrashDao: VaultTrashDao
    private lateinit var encryptionService: EncryptionService

    private val testPassword = "test_password_123"
    private val testContent = "Sensitive vault content".toByteArray()
    private val testEncryptedData = EncryptedData(
        ciphertext = byteArrayOf(1, 2, 3, 4),
        salt = ByteArray(16) { it.toByte() },
        iv = ByteArray(12) { it.toByte() }
    )

    @Before
    fun setup() {
        vaultItemDao = mockk(relaxed = true)
        vaultFolderDao = mockk(relaxed = true)
        vaultTrashDao = mockk(relaxed = true)
        encryptionService = mockk(relaxed = true)

        repository = VaultRepository(
            vaultItemDao = vaultItemDao,
            vaultFolderDao = vaultFolderDao,
            vaultTrashDao = vaultTrashDao,
            encryptionService = encryptionService
        )

        // Set up common mocks
        every { encryptionService.encrypt(any(), any()) } returns testEncryptedData
        every { encryptionService.decrypt(any(), any()) } returns testContent
        every { encryptionService.hash(any()) } returns ByteArray(32) { 0 }
        every { encryptionService.hashToHex(any()) } returns "hash_hex_string"
    }

    @Test
    fun `setMasterPassword stores password for session`() = runTest {
        // When
        repository.setMasterPassword(testPassword)

        // Then - should be able to create items (password is set)
        val request = CreateVaultItemRequest(
            type = VaultItemType.NOTE,
            title = "Test Note",
            content = testContent,
            folderId = null,
            metadata = null
        )

        coEvery { vaultItemDao.insertItem(any()) } just Runs

        val result = repository.createItem(request)

        // Should succeed because password is set
        assertTrue(result.isSuccess)
    }

    @Test
    fun `createItem fails when master password not set`() = runTest {
        // Given - no password set
        repository.clearMasterPassword()

        val request = CreateVaultItemRequest(
            type = VaultItemType.NOTE,
            title = "Test Note",
            content = testContent,
            folderId = null,
            metadata = null
        )

        // When
        val result = repository.createItem(request)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not unlocked") == true)
    }

    @Test
    fun `createItem encrypts content and stores hash`() = runTest {
        // Given
        repository.setMasterPassword(testPassword)

        val request = CreateVaultItemRequest(
            type = VaultItemType.PASSWORD,
            title = "Gmail Password",
            content = "super_secret_password".toByteArray(),
            folderId = null,
            metadata = null
        )

        val capturedEntity = slot<VaultItemEntity>()
        coEvery { vaultItemDao.insertItem(capture(capturedEntity)) } just Runs

        // When
        val result = repository.createItem(request)

        // Then
        assertTrue(result.isSuccess)
        verify { encryptionService.encrypt(request.content, testPassword) }
        verify { encryptionService.hash(request.content) }

        val entity = capturedEntity.captured
        assertEquals("Gmail Password", entity.title)
        assertEquals("PASSWORD", entity.type)
        assertArrayEquals(testEncryptedData.ciphertext, entity.encrypted_content)
        assertEquals("hash_hex_string", entity.content_hash)
    }

    @Test
    fun `getItemById updates access time`() = runTest {
        // Given
        repository.setMasterPassword(testPassword)

        val itemId = "test-item-id"
        val entity = VaultItemEntity(
            id = itemId,
            type = "NOTE",
            title = "Test Note",
            folder_id = null,
            encrypted_content = testEncryptedData.ciphertext,
            content_hash = "hash",
            thumbnail = null,
            created_at = 1000L,
            updated_at = 1000L,
            accessed_at = 1000L,
            starred = false,
            metadata = null,
            file_path = null
        )

        coEvery { vaultItemDao.getItemById(itemId) } returns entity
        coEvery { vaultItemDao.updateAccessTime(any(), any()) } just Runs

        // When
        val result = repository.getItemById(itemId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { vaultItemDao.updateAccessTime(itemId, any()) }
    }

    @Test
    fun `deleteItem moves to trash instead of permanent deletion`() = runTest {
        // Given
        val itemId = "test-item-id"
        val entity = VaultItemEntity(
            id = itemId,
            type = "PHOTO",
            title = "Sensitive Photo",
            folder_id = "folder1",
            encrypted_content = byteArrayOf(1, 2, 3),
            content_hash = "hash",
            thumbnail = null,
            created_at = 1000L,
            updated_at = 1000L,
            accessed_at = 1000L,
            starred = true,
            metadata = null,
            file_path = "/path/to/file"
        )

        coEvery { vaultItemDao.getItemById(itemId) } returns entity
        coEvery { vaultTrashDao.insertTrashItem(any()) } just Runs
        coEvery { vaultItemDao.deleteItem(any()) } just Runs

        // When
        val result = repository.deleteItem(itemId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { vaultTrashDao.insertTrashItem(any()) }
        coVerify { vaultItemDao.deleteItem(entity) }
    }

    @Test
    fun `toggleStarred updates item starred status`() = runTest {
        // Given
        val itemId = "test-item-id"
        coEvery { vaultItemDao.toggleStarred(itemId) } just Runs

        // When
        val result = repository.toggleStarred(itemId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { vaultItemDao.toggleStarred(itemId) }
    }

    @Test
    fun `getVaultStats calculates total size from encrypted content`() = runTest {
        // Given
        val items = listOf(
            createTestEntity(id = "1", encryptedSize = 100),
            createTestEntity(id = "2", encryptedSize = 200),
            createTestEntity(id = "3", encryptedSize = 300)
        )

        coEvery { vaultItemDao.getAllVaultItems() } returns flowOf(items)
        coEvery { vaultItemDao.getTotalItemCount() } returns 3
        coEvery { vaultItemDao.getItemCountByType(any()) } returns 1

        // When
        val result = repository.getVaultStats()

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(600L, stats.totalSize) // 100 + 200 + 300
        assertEquals(3, stats.totalItems)
    }

    @Test
    fun `getAllFolders includes item counts`() = runTest {
        // Given
        repository.setMasterPassword(testPassword)

        val folders = listOf(
            VaultFolderEntity("f1", "Folder 1", null, 1000L, null, 0),
            VaultFolderEntity("f2", "Folder 2", null, 1000L, null, 0)
        )

        val folder1Items = listOf(
            createTestEntity("item1"),
            createTestEntity("item2")
        )
        val folder2Items = listOf(
            createTestEntity("item3")
        )

        every { vaultFolderDao.getAllFolders() } returns flowOf(folders)
        every { vaultItemDao.getItemsInFolder("f1") } returns flowOf(folder1Items)
        every { vaultItemDao.getItemsInFolder("f2") } returns flowOf(folder2Items)

        // When
        val result = repository.getAllFolders().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(2, result[0].itemCount) // Folder 1 has 2 items
        assertEquals(1, result[1].itemCount) // Folder 2 has 1 item
    }

    @Test
    fun `createFolder creates new folder with zero item count`() = runTest {
        // Given
        val folderName = "Personal Photos"
        val folderColor = "#FF0000"

        val capturedEntity = slot<VaultFolderEntity>()
        coEvery { vaultFolderDao.insertFolder(capture(capturedEntity)) } just Runs

        // When
        val result = repository.createFolder(folderName, null, folderColor)

        // Then
        assertTrue(result.isSuccess)
        val folder = result.getOrNull()!!
        assertEquals(folderName, folder.name)
        assertEquals(folderColor, folder.color)
        assertEquals(0, folder.itemCount) // New folders start with 0 items

        val entity = capturedEntity.captured
        assertEquals(folderName, entity.name)
        assertEquals(folderColor, entity.color)
    }

    @Test
    fun `clearMasterPassword removes password from memory`() = runTest {
        // Given
        repository.setMasterPassword(testPassword)

        // When
        repository.clearMasterPassword()

        // Then - creating items should fail
        val request = CreateVaultItemRequest(
            type = VaultItemType.NOTE,
            title = "Test",
            content = testContent,
            folderId = null,
            metadata = null
        )

        val result = repository.createItem(request)
        assertTrue(result.isFailure)
    }

    // Helper functions

    private fun createTestEntity(
        id: String = "test-id",
        encryptedSize: Int = 100
    ): VaultItemEntity {
        return VaultItemEntity(
            id = id,
            type = "NOTE",
            title = "Test Item",
            folder_id = null,
            encrypted_content = ByteArray(encryptedSize) { it.toByte() },
            content_hash = "hash",
            thumbnail = null,
            created_at = 1000L,
            updated_at = 1000L,
            accessed_at = 1000L,
            starred = false,
            metadata = null,
            file_path = null
        )
    }
}
