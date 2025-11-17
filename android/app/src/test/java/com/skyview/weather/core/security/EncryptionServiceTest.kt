package com.skyview.weather.core.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EncryptionService.
 *
 * Tests encryption/decryption round-trip, password hashing, and key derivation.
 */
class EncryptionServiceTest {

    private lateinit var encryptionService: EncryptionService

    @Before
    fun setup() {
        encryptionService = EncryptionService()
    }

    @Test
    fun `encrypt and decrypt returns original data`() {
        // Given
        val originalData = "Hello, World!".toByteArray()
        val password = "Test Password 123"

        // When
        val encrypted = encryptionService.encrypt(originalData, password)
        val decrypted = encryptionService.decrypt(encrypted, password)

        // Then
        assertArrayEquals(originalData, decrypted)
    }

    @Test
    fun `encrypt same data with same password produces different ciphertext`() {
        // Given
        val originalData = "Test Data".toByteArray()
        val password = "password123"

        // When
        val encrypted1 = encryptionService.encrypt(originalData, password)
        val encrypted2 = encryptionService.encrypt(originalData, password)

        // Then - should be different due to random salt and IV
        assertFalse(encrypted1.ciphertext.contentEquals(encrypted2.ciphertext))
    }

    @Test(expected = Exception::class)
    fun `decrypt with wrong password throws exception`() {
        // Given
        val originalData = "Secret Data".toByteArray()
        val correctPassword = "correct password"
        val wrongPassword = "wrong password"

        // When
        val encrypted = encryptionService.encrypt(originalData, correctPassword)

        // Then - should throw exception
        encryptionService.decrypt(encrypted, wrongPassword)
    }

    @Test
    fun `encryptString and decryptString returns original text`() {
        // Given
        val originalText = "Hello, Secure World!"
        val password = "secure_password_456"

        // When
        val encrypted = encryptionService.encryptString(originalText, password)
        val decrypted = encryptionService.decryptString(encrypted, password)

        // Then
        assertEquals(originalText, decrypted)
    }

    @Test
    fun `hash produces consistent results for same input`() {
        // Given
        val data = "Consistent Data".toByteArray()

        // When
        val hash1 = encryptionService.hash(data)
        val hash2 = encryptionService.hash(data)

        // Then
        assertArrayEquals(hash1, hash2)
    }

    @Test
    fun `hash produces different results for different inputs`() {
        // Given
        val data1 = "Data One".toByteArray()
        val data2 = "Data Two".toByteArray()

        // When
        val hash1 = encryptionService.hash(data1)
        val hash2 = encryptionService.hash(data2)

        // Then
        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun `hashToHex produces 64-character hex string`() {
        // Given
        val data = "Test Data".toByteArray()

        // When
        val hash = encryptionService.hash(data)
        val hexHash = encryptionService.hashToHex(hash)

        // Then
        assertEquals(64, hexHash.length) // SHA-256 produces 32 bytes = 64 hex chars
    }

    @Test
    fun `encrypted data contains salt and IV`() {
        // Given
        val data = "Test".toByteArray()
        val password = "password"

        // When
        val encrypted = encryptionService.encrypt(data, password)

        // Then
        assertEquals(16, encrypted.salt.size) // Salt should be 16 bytes
        assertEquals(12, encrypted.iv.size)   // GCM IV should be 12 bytes
        assertTrue(encrypted.ciphertext.isNotEmpty())
    }

    @Test
    fun `empty data can be encrypted and decrypted`() {
        // Given
        val emptyData = ByteArray(0)
        val password = "password123"

        // When
        val encrypted = encryptionService.encrypt(emptyData, password)
        val decrypted = encryptionService.decrypt(encrypted, password)

        // Then
        assertEquals(0, decrypted.size)
    }

    @Test
    fun `large data can be encrypted and decrypted`() {
        // Given
        val largeData = ByteArray(1024 * 1024) { it.toByte() } // 1 MB
        val password = "secure_password"

        // When
        val encrypted = encryptionService.encrypt(largeData, password)
        val decrypted = encryptionService.decrypt(encrypted, password)

        // Then
        assertArrayEquals(largeData, decrypted)
    }
}
