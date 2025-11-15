package com.skyview.weather.core.security

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages biometric authentication for vault access.
 *
 * Provides methods to check biometric availability and authenticate users
 * using fingerprint, face recognition, or other biometric methods.
 *
 * @property context Application context
 */
@Singleton
class BiometricManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val biometricManager = AndroidBiometricManager.from(context)

    /**
     * Result of biometric authentication.
     */
    sealed class BiometricResult {
        object Success : BiometricResult()
        data class Error(val errorCode: Int, val errorMessage: String) : BiometricResult()
        object Failed : BiometricResult()
        object Cancelled : BiometricResult()
    }

    /**
     * Checks if biometric authentication is available on this device.
     *
     * @return BiometricAvailability status
     */
    fun checkBiometricAvailability(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            AndroidBiometricManager.BIOMETRIC_SUCCESS ->
                BiometricAvailability.Available

            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.NoHardware

            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.HardwareUnavailable

            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.NoneEnrolled

            AndroidBiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricAvailability.SecurityUpdateRequired

            AndroidBiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricAvailability.Unsupported

            AndroidBiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricAvailability.Unknown

            else -> BiometricAvailability.Unknown
        }
    }

    /**
     * Checks if biometric authentication is available and enrolled.
     *
     * @return true if biometric can be used
     */
    fun isBiometricAvailable(): Boolean {
        return checkBiometricAvailability() == BiometricAvailability.Available
    }

    /**
     * Authenticates user with biometric prompt.
     *
     * @param activity FragmentActivity to show prompt
     * @param title Prompt title
     * @param subtitle Prompt subtitle
     * @param description Prompt description
     * @param negativeButtonText Text for negative button (cancel)
     * @param onResult Callback with authentication result
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        description: String? = null,
        negativeButtonText: String = "Cancel",
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(true)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            onResult(BiometricResult.Cancelled)
                        else ->
                            onResult(BiometricResult.Error(errorCode, errString.toString()))
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(BiometricResult.Failed)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Authenticates user with biometric and uses cryptographic operation.
     * Used for biometric-protected key operations.
     *
     * @param activity FragmentActivity to show prompt
     * @param title Prompt title
     * @param subtitle Prompt subtitle
     * @param description Prompt description
     * @param negativeButtonText Text for negative button
     * @param cryptoObject Crypto object with cipher
     * @param onResult Callback with authentication result and crypto object
     */
    fun authenticateWithCrypto(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        description: String? = null,
        negativeButtonText: String = "Cancel",
        cryptoObject: BiometricPrompt.CryptoObject,
        onResult: (BiometricResult, BiometricPrompt.CryptoObject?) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(true)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            onResult(BiometricResult.Cancelled, null)
                        else ->
                            onResult(BiometricResult.Error(errorCode, errString.toString()), null)
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricResult.Success, result.cryptoObject)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(BiometricResult.Failed, null)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    /**
     * Biometric availability status.
     */
    enum class BiometricAvailability {
        Available,
        NoHardware,
        HardwareUnavailable,
        NoneEnrolled,
        SecurityUpdateRequired,
        Unsupported,
        Unknown
    }
}
