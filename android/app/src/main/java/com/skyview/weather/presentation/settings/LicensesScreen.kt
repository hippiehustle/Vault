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
 * Open Source Licenses screen.
 *
 * Displays third-party library licenses used in the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
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
                text = "This application uses the following open source libraries:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LicenseItem(
                name = "Jetpack Compose",
                copyright = "Copyright © 2020 The Android Open Source Project",
                license = "Apache License 2.0",
                url = "https://github.com/androidx/androidx"
            )

            LicenseItem(
                name = "Kotlin",
                copyright = "Copyright © 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors",
                license = "Apache License 2.0",
                url = "https://github.com/JetBrains/kotlin"
            )

            LicenseItem(
                name = "Dagger Hilt",
                copyright = "Copyright © 2021 The Dagger Authors",
                license = "Apache License 2.0",
                url = "https://github.com/google/dagger"
            )

            LicenseItem(
                name = "Retrofit",
                copyright = "Copyright © 2013 Square, Inc.",
                license = "Apache License 2.0",
                url = "https://github.com/square/retrofit"
            )

            LicenseItem(
                name = "OkHttp",
                copyright = "Copyright © 2019 Square, Inc.",
                license = "Apache License 2.0",
                url = "https://github.com/square/okhttp"
            )

            LicenseItem(
                name = "Room Database",
                copyright = "Copyright © 2017 The Android Open Source Project",
                license = "Apache License 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/room"
            )

            LicenseItem(
                name = "SQLCipher for Android",
                copyright = "Copyright © 2008-2020 Zetetic LLC",
                license = "BSD License",
                url = "https://github.com/sqlcipher/android-database-sqlcipher"
            )

            LicenseItem(
                name = "Coil",
                copyright = "Copyright © 2024 Coil Contributors",
                license = "Apache License 2.0",
                url = "https://github.com/coil-kt/coil"
            )

            LicenseItem(
                name = "MPAndroidChart",
                copyright = "Copyright © 2020 Philipp Jahoda",
                license = "Apache License 2.0",
                url = "https://github.com/PhilJay/MPAndroidChart"
            )

            LicenseItem(
                name = "Accompanist",
                copyright = "Copyright © 2021 Google LLC",
                license = "Apache License 2.0",
                url = "https://github.com/google/accompanist"
            )

            LicenseItem(
                name = "Coroutines",
                copyright = "Copyright © 2016-2024 JetBrains s.r.o. and Kotlin Programming Language contributors",
                license = "Apache License 2.0",
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            )

            LicenseItem(
                name = "Material Design 3",
                copyright = "Copyright © 2021 The Android Open Source Project",
                license = "Apache License 2.0",
                url = "https://github.com/material-components/material-components-android"
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Apache License 2.0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = """
                    Licensed under the Apache License, Version 2.0 (the "License");
                    you may not use this file except in compliance with the License.
                    You may obtain a copy of the License at

                        http://www.apache.org/licenses/LICENSE-2.0

                    Unless required by applicable law or agreed to in writing, software
                    distributed under the License is distributed on an "AS IS" BASIS,
                    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    See the License for the specific language governing permissions and
                    limitations under the License.
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicenseItem(
    name: String,
    copyright: String,
    license: String,
    url: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = copyright,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
