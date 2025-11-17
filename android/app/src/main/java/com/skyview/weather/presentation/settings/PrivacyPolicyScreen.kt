package com.skyview.weather.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Privacy Policy screen.
 *
 * Displays the application's privacy policy for user transparency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Last updated: November 17, 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PolicySection(
                title = "1. Data Collection",
                content = """
                    SkyView Weather respects your privacy. We collect minimal data necessary for app functionality:

                    • Weather location data (stored locally on your device)
                    • API keys for weather services (stored securely in encrypted preferences)
                    • Vault data (encrypted using AES-256-GCM with your master password)

                    All sensitive data remains on your device and is never transmitted to our servers.
                """.trimIndent()
            )

            PolicySection(
                title = "2. Data Storage",
                content = """
                    All data is stored locally on your device using industry-standard encryption:

                    • Vault items: AES-256-GCM encryption with Argon2 password hashing
                    • Preferences: Android EncryptedSharedPreferences
                    • Database: SQLCipher with hardware-backed key storage

                    Your master password never leaves your device and cannot be recovered if lost.
                """.trimIndent()
            )

            PolicySection(
                title = "3. Third-Party Services",
                content = """
                    We integrate with third-party weather APIs to provide weather data:

                    • Weather data is fetched from external APIs using your provided API key
                    • Location data is sent to weather services to retrieve local forecasts
                    • We do not control third-party data practices - please review their privacy policies

                    No analytics or tracking services are used in this application.
                """.trimIndent()
            )

            PolicySection(
                title = "4. Permissions",
                content = """
                    The app requests the following permissions:

                    • Location: To fetch weather for your current location
                    • Internet: To retrieve weather data from APIs
                    • Storage: To save vault items and attachments
                    • Biometric: Optional, for unlocking the vault with fingerprint/face

                    All permissions are optional and can be revoked in system settings.
                """.trimIndent()
            )

            PolicySection(
                title = "5. Data Security",
                content = """
                    We implement multiple security layers:

                    • All vault data encrypted at rest using AES-256-GCM
                    • Biometric authentication backed by Android Keystore
                    • Screenshot prevention on sensitive screens
                    • Automatic clipboard clearing for copied passwords
                    • No cloud backup of encrypted vault data

                    You are responsible for maintaining the security of your master password.
                """.trimIndent()
            )

            PolicySection(
                title = "6. Children's Privacy",
                content = """
                    This app is not directed at children under 13 years of age. We do not knowingly collect personal information from children.
                """.trimIndent()
            )

            PolicySection(
                title = "7. Changes to This Policy",
                content = """
                    We may update this privacy policy from time to time. Continued use of the app constitutes acceptance of any changes.
                """.trimIndent()
            )

            PolicySection(
                title = "8. Contact",
                content = """
                    For privacy concerns or questions, please contact us through the app's GitHub repository.
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
