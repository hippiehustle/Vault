package com.skyview.weather.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.skyview.weather.util.clear
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages cryptographic keys using Android Keystore.
 *
 * Provides secure storage for encryption keys with hardware-backed security
 * when available (StrongBox or TEE).
 *
 * @property context Application context
 */
@Singleton
class KeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "skyview_master_key"
        private const val VAULT_KEY_ALIAS = "skyview_vault_key"
        private const val DATABASE_KEY_ALIAS = "skyview_database_key"
        private const val ENCRYPTED_PREFS_NAME = "skyview_secure_prefs"
        private const val KEY_VAULT_PASSWORD_HASH = "vault_password_hash"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()
    }

    private val encryptedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Initializes vault with master password.
     * Stores password hash for verification and creates encryption key.
     *
     * @param password Master password
     */
    fun initializeVault(password: String) {
        // Hash password for verification (not for encryption)
        val passwordHash = hashPassword(password)

        // Store password hash
        encryptedPreferences.edit()
            .putString(KEY_VAULT_PASSWORD_HASH, passwordHash)
            .apply()

        // Generate and store vault encryption key in Keystore
        generateVaultKey()
    }

    /**
     * Verifies master password.
     *
     * @param password Password to verify
     * @return true if password is correct
     */
    fun verifyPassword(password: String): Boolean {
        val storedHash = encryptedPreferences.getString(KEY_VAULT_PASSWORD_HASH, null)
            ?: return false

        val providedHash = hashPassword(password)
        return storedHash == providedHash
    }

    /**
     * Checks if vault is initialized.
     *
     * @return true if vault has been initialized with a password
     */
    fun isVaultInitialized(): Boolean {
        return encryptedPreferences.contains(KEY_VAULT_PASSWORD_HASH)
    }

    /**
     * Gets the vault encryption key from Keystore.
     *
     * @return Vault encryption key
     * @throws IllegalStateException if vault not initialized
     */
    fun getVaultKey(): SecretKey {
        if (!keyStore.containsAlias(VAULT_KEY_ALIAS)) {
            throw IllegalStateException("Vault not initialized")
        }

        return keyStore.getKey(VAULT_KEY_ALIAS, null) as SecretKey
    }

    /**
     * Changes master password.
     * Updates password hash. Vault data re-encryption is handled by repository.
     *
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     */
    fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!verifyPassword(oldPassword)) {
            return false
        }

        val newPasswordHash = hashPassword(newPassword)
        encryptedPreferences.edit()
            .putString(KEY_VAULT_PASSWORD_HASH, newPasswordHash)
            .apply()

        return true
    }

    /**
     * Generates a new vault encryption key in Keystore.
     * Uses hardware-backed security (StrongBox) if available.
     */
    private fun generateVaultKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val builder = KeyGenParameterSpec.Builder(
            VAULT_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        // Use StrongBox if available for hardware-level security
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    /**
     * Creates a biometric-protected key for vault access.
     * This key can only be used after biometric authentication.
     *
     * @param keyAlias Alias for the biometric key
     */
    fun createBiometricKey(keyAlias: String = "biometric_vault_key") {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setRandomizedEncryptionRequired(true)

        // Require user authentication for every use (Android 11+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0, // timeout of 0 means require auth for every use
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            builder.setUnlockedDeviceRequired(true)
                .setUserAuthenticationValidityDurationSeconds(-1) // Require auth for every use
        } else {
            builder.setUserAuthenticationValidityDurationSeconds(30)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    /**
     * Gets biometric-protected key for encryption/decryption.
     *
     * @param keyAlias Alias of the biometric key
     * @return Biometric-protected key
     */
    fun getBiometricKey(keyAlias: String = "biometric_vault_key"): SecretKey? {
        return try {
            if (!keyStore.containsAlias(keyAlias)) {
                createBiometricKey(keyAlias)
            }
            keyStore.getKey(keyAlias, null) as SecretKey
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes all vault keys.
     * Used when vault is reset or data is wiped.
     */
    fun deleteVaultKeys() {
        if (keyStore.containsAlias(VAULT_KEY_ALIAS)) {
            keyStore.deleteEntry(VAULT_KEY_ALIAS)
        }
        if (keyStore.containsAlias("biometric_vault_key")) {
            keyStore.deleteEntry("biometric_vault_key")
        }
        if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
            keyStore.deleteEntry(DATABASE_KEY_ALIAS)
        }

        encryptedPreferences.edit().clear().apply()
    }

    /**
     * Gets or creates database encryption key from Android Keystore.
     * This key is unique per device installation and stored securely in hardware.
     *
     * @return SecretKey for database encryption
     */
    fun getOrCreateDatabaseKey(): SecretKey {
        return if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
            keyStore.getKey(DATABASE_KEY_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                DATABASE_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(false) // For database, we need deterministic key
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Hashes password using SHA-256.
     *
     * @param password Password to hash
     * @return Hex string of hash
     */
    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Encrypts data with biometric key.
     * Used to wrap vault key with biometric protection.
     *
     * @param data Data to encrypt
     * @param cipher Cipher initialized with biometric key
     * @return Encrypted data with IV
     */
    fun encryptWithBiometric(data: ByteArray, cipher: Cipher): Pair<ByteArray, ByteArray> {
        val encrypted = cipher.doFinal(data)
        val iv = cipher.iv
        return Pair(encrypted, iv)
    }

    /**
     * Decrypts data with biometric key.
     *
     * @param encrypted Encrypted data
     * @param iv Initialization vector
     * @param cipher Cipher initialized with biometric key
     * @return Decrypted data
     */
    fun decryptWithBiometric(
        encrypted: ByteArray,
        iv: ByteArray,
        cipher: Cipher
    ): ByteArray {
        val key = getBiometricKey() ?: throw IllegalStateException("Biometric key not available")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encrypted)
    }

    /**
     * Gets cipher for biometric encryption.
     *
     * @return Initialized cipher for encryption
     */
    fun getBiometricCipherForEncryption(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = getBiometricKey() ?: throw IllegalStateException("Biometric key not available")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    /**
     * Gets cipher for biometric decryption.
     *
     * @param iv Initialization vector from encryption
     * @return Initialized cipher for decryption
     */
    fun getBiometricCipherForDecryption(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = getBiometricKey() ?: throw IllegalStateException("Biometric key not available")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher
    }
}
