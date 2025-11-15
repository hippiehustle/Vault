package com.skyview.weather.presentation.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skyview.weather.domain.model.VaultItem
import com.skyview.weather.util.VaultItemType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vault browser screen showing all items in a grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultBrowserScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateToItem: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.lockVault()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Lock, "Lock Vault")
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }

                    // Filter dropdown
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Items") },
                            onClick = {
                                viewModel.filterByType(null)
                                showFilterMenu = false
                            }
                        )
                        VaultItemType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.filterByType(type)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMenu = true }
            ) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { /* Retry logic */ }
                    )
                }

                uiState.items.isEmpty() -> {
                    EmptyVaultContent()
                }

                else -> {
                    VaultItemsGrid(
                        items = uiState.items,
                        onItemClick = onNavigateToItem
                    )
                }
            }

            // Stats card at bottom
            uiState.stats?.let { stats ->
                VaultStatsCard(
                    stats = stats,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }

        // Add item menu
        if (showAddMenu) {
            AddItemDialog(
                onDismiss = { showAddMenu = false },
                onTypeSelected = { type ->
                    showAddMenu = false
                    // Handle item creation based on type
                }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyVaultContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Vault is Empty",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first secure item",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VaultItemsGrid(
    items: List<VaultItem>,
    onItemClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            VaultItemCard(
                item = item,
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon based on type
            Icon(
                imageVector = when (item.type) {
                    VaultItemType.PHOTO -> Icons.Default.Photo
                    VaultItemType.VIDEO -> Icons.Default.Videocam
                    VaultItemType.DOCUMENT -> Icons.Default.Description
                    VaultItemType.NOTE -> Icons.Default.Note
                    VaultItemType.PASSWORD -> Icons.Default.Key
                    VaultItemType.AUDIO -> Icons.Default.AudioFile
                    VaultItemType.CONTACT -> Icons.Default.Person
                },
                contentDescription = item.type.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(item.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.starred) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Starred",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun VaultStatsCard(
    stats: com.skyview.weather.domain.model.VaultStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Items", stats.totalItems.toString())
            StatItem("Photos", stats.photoCount.toString())
            StatItem("Docs", stats.documentCount.toString())
            StatItem("Notes", stats.noteCount.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (VaultItemType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Vault") },
        text = {
            Column {
                VaultItemType.values().forEach { type ->
                    TextButton(
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = when (type) {
                                VaultItemType.PHOTO -> Icons.Default.Photo
                                VaultItemType.VIDEO -> Icons.Default.Videocam
                                VaultItemType.DOCUMENT -> Icons.Default.Description
                                VaultItemType.NOTE -> Icons.Default.Note
                                VaultItemType.PASSWORD -> Icons.Default.Key
                                VaultItemType.AUDIO -> Icons.Default.AudioFile
                                VaultItemType.CONTACT -> Icons.Default.Person
                            },
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
