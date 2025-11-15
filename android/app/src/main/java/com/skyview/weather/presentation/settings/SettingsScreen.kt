package com.skyview.weather.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skyview.weather.data.local.*

/**
 * Settings screen.
 *
 * Allows users to configure app preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Weather Settings Section
            SettingsSectionHeader("Weather")

            SettingsDropdownItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                currentValue = uiState.theme.displayName,
                options = Theme.values().map { it.displayName },
                onOptionSelected = { selected ->
                    val theme = Theme.values().find { it.displayName == selected }
                    theme?.let { viewModel.setTheme(it) }
                }
            )

            SettingsDropdownItem(
                icon = Icons.Default.Thermostat,
                title = "Temperature Unit",
                currentValue = uiState.temperatureUnit.displayName,
                options = TemperatureUnit.values().map { it.displayName },
                onOptionSelected = { selected ->
                    val unit = TemperatureUnit.values().find { it.displayName == selected }
                    unit?.let { viewModel.setTemperatureUnit(it) }
                }
            )

            SettingsDropdownItem(
                icon = Icons.Default.Air,
                title = "Wind Speed Unit",
                currentValue = uiState.windSpeedUnit.displayName,
                options = WindSpeedUnit.values().map { it.displayName },
                onOptionSelected = { selected ->
                    val unit = WindSpeedUnit.values().find { it.displayName == selected }
                    unit?.let { viewModel.setWindSpeedUnit(it) }
                }
            )

            SettingsDropdownItem(
                icon = Icons.Default.Speed,
                title = "Pressure Unit",
                currentValue = uiState.pressureUnit.displayName,
                options = PressureUnit.values().map { it.displayName },
                onOptionSelected = { selected ->
                    val unit = PressureUnit.values().find { it.displayName == selected }
                    unit?.let { viewModel.setPressureUnit(it) }
                }
            )

            SettingsClickableItem(
                icon = Icons.Default.Key,
                title = "Weather API Key",
                subtitle = if (uiState.customApiKey != null) "Custom key configured" else "Using default key",
                onClick = { viewModel.showApiKeyDialog() }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Security Settings Section
            SettingsSectionHeader("Security")

            SettingsSwitchItem(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Unlock",
                subtitle = "Use fingerprint or face ID",
                checked = uiState.biometricEnabled,
                onCheckedChange = { viewModel.setBiometricEnabled(it) }
            )

            SettingsClickableItem(
                icon = Icons.Default.Timer,
                title = "Auto-Lock Timeout",
                subtitle = "${uiState.autoLockTimeout} minutes",
                onClick = { viewModel.showAutoLockDialog() }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader("About")

            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "Version",
                value = uiState.appVersion
            )

            SettingsClickableItem(
                icon = Icons.Default.Security,
                title = "Privacy Policy",
                subtitle = "How we protect your data",
                onClick = { /* TODO: Open privacy policy */ }
            )

            SettingsClickableItem(
                icon = Icons.Default.Article,
                title = "Open Source Licenses",
                subtitle = "View third-party licenses",
                onClick = { /* TODO: Open licenses */ }
            )
        }
    }

    // API Key Dialog
    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = uiState.customApiKey,
            onDismiss = { viewModel.hideApiKeyDialog() },
            onSave = { key ->
                viewModel.setCustomApiKey(key.takeIf { it.isNotBlank() })
                viewModel.hideApiKeyDialog()
            }
        )
    }

    // Auto-Lock Dialog
    if (uiState.showAutoLockDialog) {
        AutoLockDialog(
            currentTimeout = uiState.autoLockTimeout,
            onDismiss = { viewModel.hideAutoLockDialog() },
            onSave = { timeout ->
                viewModel.setAutoLockTimeout(timeout)
                viewModel.hideAutoLockDialog()
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsDropdownItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(currentValue) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        },
        modifier = Modifier.clickable { expanded = true }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onOptionSelected(option)
                    expanded = false
                },
                leadingIcon = if (option == currentValue) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        }
    )
}

@Composable
private fun ApiKeyDialog(
    currentKey: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var key by remember { mutableStateOf(currentKey ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weather API Key") },
        text = {
            Column {
                Text(
                    text = "Enter your OpenWeatherMap API key. Leave blank to use the default key.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(key) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AutoLockDialog(
    currentTimeout: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val timeoutOptions = listOf(1, 2, 5, 10, 15, 30, 60)
    var selectedTimeout by remember { mutableStateOf(currentTimeout) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-Lock Timeout") },
        text = {
            Column {
                Text(
                    text = "Vault will automatically lock after this period of inactivity.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                timeoutOptions.forEach { timeout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTimeout = timeout }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTimeout == timeout,
                            onClick = { selectedTimeout = timeout }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (timeout < 60) "$timeout minutes" else "1 hour",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedTimeout) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Extension properties for display names
private val Theme.displayName: String
    get() = when (this) {
        Theme.LIGHT -> "Light"
        Theme.DARK -> "Dark"
        Theme.SYSTEM -> "System Default"
    }

private val TemperatureUnit.displayName: String
    get() = when (this) {
        TemperatureUnit.FAHRENHEIT -> "Fahrenheit (°F)"
        TemperatureUnit.CELSIUS -> "Celsius (°C)"
    }

private val WindSpeedUnit.displayName: String
    get() = when (this) {
        WindSpeedUnit.MPH -> "Miles per Hour (mph)"
        WindSpeedUnit.KPH -> "Kilometers per Hour (km/h)"
        WindSpeedUnit.MS -> "Meters per Second (m/s)"
    }

private val PressureUnit.displayName: String
    get() = when (this) {
        PressureUnit.INHG -> "Inches of Mercury (inHg)"
        PressureUnit.MBAR -> "Millibars (mbar)"
        PressureUnit.MMHG -> "Millimeters of Mercury (mmHg)"
    }
