package com.skyview.weather.core.security

import com.skyview.weather.util.Constants
import com.skyview.weather.util.clear
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for encrypting and decrypting vault content.
 *
 * Uses AES-256-GCM authenticated encryption with keys derived from
 * user's master password via Argon2id KDF (simulated via PBKDF2 with high iterations
 * since Android doesn't have native Argon2 - in production, use a native library).
 *
 * @property secureRandom Cryptographically secure random number generator
 */
@Singleton
class EncryptionService @Inject constructor() {

    private val secureRandom = SecureRandom()

    /**
     * Encrypted data container.
     *
     * @property ciphertext The encrypted data
     * @property iv Initialization vector (nonce)
     * @property salt Salt used for key derivation
     */
    data class EncryptedData(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val salt: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedData

            if (!ciphertext.contentEquals(other.ciphertext)) return false
            if (!iv.contentEquals(other.iv)) return false
            if (!salt.contentEquals(other.salt)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            return result
        }
    }

    /**
     * Encrypts data using AES-256-GCM.
     *
     * @param plaintext The data to encrypt
     * @param password User's master password
     * @return EncryptedData containing ciphertext, IV, and salt
     * @throws EncryptionException if encryption fails
     */
    fun encrypt(plaintext: ByteArray, password: String): EncryptedData {
        return try {
            // Generate random salt
            val salt = ByteArray(Constants.SALT_LENGTH_BYTES)
            secureRandom.nextBytes(salt)

            // Derive key from password
            val key = deriveKey(password, salt)

            // Generate random IV
            val iv = ByteArray(Constants.GCM_IV_SIZE)
            secureRandom.nextBytes(iv)

            // Encrypt
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(Constants.GCM_TAG_SIZE, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            val ciphertext = cipher.doFinal(plaintext)

            // Clear sensitive data
            key.encoded.clear()

            EncryptedData(ciphertext, iv, salt)
        } catch (e: Exception) {
            throw EncryptionException("Encryption failed: ${e.message}", e)
        }
    }

    /**
     * Encrypts a string using AES-256-GCM.
     *
     * @param plaintext The string to encrypt
     * @param password User's master password
     * @return EncryptedData containing ciphertext, IV, and salt
     * @throws EncryptionException if encryption fails
     */
    fun encryptString(plaintext: String, password: String): EncryptedData {
        return encrypt(plaintext.toByteArray(Charsets.UTF_8), password)
    }

    /**
     * Decrypts data using AES-256-GCM.
     *
     * @param encryptedData The encrypted data
     * @param password User's master password
     * @return Decrypted plaintext
     * @throws DecryptionException if decryption fails (wrong password, corrupted data, etc.)
     */
    fun decrypt(encryptedData: EncryptedData, password: String): ByteArray {
        return try {
            // Derive key from password and salt
            val key = deriveKey(password, encryptedData.salt)

            // Decrypt
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(Constants.GCM_TAG_SIZE, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val plaintext = cipher.doFinal(encryptedData.ciphertext)

            // Clear sensitive data
            key.encoded.clear()

            plaintext
        } catch (e: Exception) {
            throw DecryptionException("Decryption failed: ${e.message}", e)
        }
    }

    /**
     * Decrypts data to string using AES-256-GCM.
     *
     * @param encryptedData The encrypted data
     * @param password User's master password
     * @return Decrypted string
     * @throws DecryptionException if decryption fails
     */
    fun decryptString(encryptedData: EncryptedData, password: String): String {
        val plaintext = decrypt(encryptedData, password)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Derives a cryptographic key from password using PBKDF2.
     *
     * Note: In production, consider using Argon2id via a native library
     * for better resistance against GPU/ASIC attacks. PBKDF2 is used here
     * for compatibility and because it's available in Android SDK.
     *
     * @param password The password
     * @param salt The salt
     * @return Derived secret key
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

        // Use high iteration count to simulate Argon2's memory hardness
        // In production with Argon2id: memory=64MB, iterations=2, parallelism=4
        // PBKDF2 equivalent uses higher iterations (100,000+)
        val iterations = 100000

        val spec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            Constants.AES_KEY_SIZE
        )

        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Generates a random encryption key.
     * Used for file encryption where each file has a unique key.
     *
     * @return Random AES key
     */
    fun generateRandomKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(Constants.AES_KEY_SIZE, secureRandom)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts data with a SecretKey directly (no password derivation).
     *
     * @param plaintext The data to encrypt
     * @param key The encryption key
     * @return EncryptedData (without salt, since key is provided directly)
     */
    fun encryptWithKey(plaintext: ByteArray, key: SecretKey): EncryptedData {
        return try {
            // Generate random IV
            val iv = ByteArray(Constants.GCM_IV_SIZE)
            secureRandom.nextBytes(iv)

            // Encrypt
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(Constants.GCM_TAG_SIZE, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            val ciphertext = cipher.doFinal(plaintext)

            EncryptedData(ciphertext, iv, ByteArray(0)) // Empty salt since key is provided
        } catch (e: Exception) {
            throw EncryptionException("Encryption failed: ${e.message}", e)
        }
    }

    /**
     * Decrypts data with a SecretKey directly.
     *
     * @param encryptedData The encrypted data
     * @param key The decryption key
     * @return Decrypted plaintext
     */
    fun decryptWithKey(encryptedData: EncryptedData, key: SecretKey): ByteArray {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(Constants.GCM_TAG_SIZE, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            cipher.doFinal(encryptedData.ciphertext)
        } catch (e: Exception) {
            throw DecryptionException("Decryption failed: ${e.message}", e)
        }
    }

    /**
     * Computes SHA-256 hash of data.
     * Used for integrity verification.
     *
     * @param data The data to hash
     * @return SHA-256 hash
     */
    fun hash(data: ByteArray): ByteArray {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    /**
     * Converts hash to hex string.
     *
     * @param hash The hash bytes
     * @return Hex string representation
     */
    fun hashToHex(hash: ByteArray): String {
        return hash.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Exception thrown when encryption fails.
 */
class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when decryption fails.
 */
class DecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
