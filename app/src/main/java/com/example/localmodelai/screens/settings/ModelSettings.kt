package com.example.localmodelai.screens.settings

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localmodelai.ai.ModelCatalog
import com.example.localmodelai.ai.ModelDownloadStatus
import com.example.localmodelai.ai.ModelSpec
import com.example.localmodelai.components.ModelItemRow
import com.example.localmodelai.components.ModelSearchBar
import com.example.localmodelai.components.RemoteModelSummaryCard
import com.example.localmodelai.data.api.HFRemoteModelGroup
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.ui.theme.LocalModelAITheme

enum class SettingsTab(val title: String) {
    EXPLORE("Explore"),
    MY_MODELS("My Models")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(
    chatViewModel: ChatViewModel,
    isDarkTheme: Boolean,
    themeModeLabel: String,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onOpenRemoteModelVersions: (HFRemoteModelGroup) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        when (themeModeLabel) {
                            "System" -> Text(
                                text = "S",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            "Light" -> Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Switch to dark mode"
                            )

                            else -> Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Switch to system mode"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        ModelSettingsContent(
            builtInModels = ModelCatalog.supportedModels,
            remoteModelGroups = chatViewModel.remoteModelGroups,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            modifier = Modifier.padding(innerPadding),
            onDownload = { model -> chatViewModel.downloadSelectedModel(model) },
            onDelete = { model -> chatViewModel.deleteSelectedModel(model) },
            onLoad = { model -> chatViewModel.loadSelectedModel(model) },
            getSystemPrompt = { model -> chatViewModel.getSystemPrompt(model) },
            onSystemPromptChange = { model, prompt -> chatViewModel.updateSystemPrompt(model, prompt) },
            getStatus = { model -> chatViewModel.getModelStatus(model) },
            checkIsLoading = { model -> chatViewModel.isLoadingModel(model) },
            checkIsLoaded = { model -> chatViewModel.isLoadedModel(model) },
            onOpenRemoteModelVersions = onOpenRemoteModelVersions
        )
    }
}

@Composable
fun ModelSettingsContent(
    builtInModels: List<ModelSpec>,
    remoteModelGroups: List<HFRemoteModelGroup>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onDownload: (ModelSpec) -> Unit,
    onDelete: (ModelSpec) -> Unit,
    onLoad: (ModelSpec) -> Unit,
    getSystemPrompt: (ModelSpec) -> String,
    onSystemPromptChange: (ModelSpec, String) -> Unit,
    getStatus: (ModelSpec) -> ModelDownloadStatus,
    checkIsLoading: (ModelSpec) -> Boolean,
    checkIsLoaded: (ModelSpec) -> Boolean,
    onOpenRemoteModelVersions: (HFRemoteModelGroup) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.EXPLORE) }
    val remoteVersionModels = remember(remoteModelGroups) {
        remoteModelGroups.flatMap { it.toVersionModelSpecs() }
    }
    val allKnownModels = (builtInModels + remoteVersionModels).distinctBy { it.fileName }

    val filteredBuiltInModels = remember(searchQuery, builtInModels) {
        if (searchQuery.isBlank()) {
            builtInModels
        } else {
            builtInModels.filter { model ->
                model.displayName.contains(searchQuery, ignoreCase = true) ||
                    model.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredRemoteGroups = remember(searchQuery, remoteModelGroups) {
        if (searchQuery.isBlank()) {
            remoteModelGroups
        } else {
            remoteModelGroups.filter { group ->
                group.displayName.contains(searchQuery, ignoreCase = true) ||
                    group.id.contains(searchQuery, ignoreCase = true) ||
                    group.versionFiles.any { it.fileName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val downloadedModels = allKnownModels.filter { getStatus(it).isDownloaded }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModelSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
        }

        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                    .padding(4.dp)
            ) {
                val tabCount = SettingsTab.entries.size
                val tabWidth = maxWidth / tabCount
                val targetOffset = tabWidth * selectedTab.ordinal

                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 400f
                    ),
                    label = "TabSlider"
                )

                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(40.dp)
                        .offset {
                            IntOffset(x = animatedOffset.roundToPx(), y = 0)
                        }
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        )
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        SettingsTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { selectedTab = tab }
                                    ),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (selectedTab == SettingsTab.EXPLORE) "Available Models" else "Downloaded Models",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedTab == SettingsTab.EXPLORE) {
                        "Built-in models stay local, and LiteRT community models are fetched from Hugging Face."
                    } else {
                        "Models downloaded to your device storage that are ready to load."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selectedTab == SettingsTab.EXPLORE) {
            item {
                Text(
                    text = "Built-in Catalog",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredBuiltInModels.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No built-in models match \"$searchQuery\""
                        } else {
                            "No built-in models available."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredBuiltInModels, key = { it.id }) { model ->
                    ModelItemRow(
                        model = model,
                        status = getStatus(model),
                        systemPrompt = getSystemPrompt(model),
                        onDownload = { onDownload(model) },
                        onDelete = { onDelete(model) },
                        onLoad = { onLoad(model) },
                        onSystemPromptChange = { prompt -> onSystemPromptChange(model, prompt) },
                        isLoading = checkIsLoading(model),
                        isLoaded = checkIsLoaded(model)
                    )
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    text = "LiteRT Community",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredRemoteGroups.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No remote models match \"$searchQuery\""
                        } else {
                            "Remote models are loading or unavailable."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredRemoteGroups, key = { it.id }) { group ->
                    RemoteModelSummaryCard(
                        title = group.displayName,
                        subtitle = "${group.versionFiles.size} downloadable .litertlm file${if (group.versionFiles.size == 1) "" else "s"}",
                        versionLabel = "Open versions",
                        onClick = { onOpenRemoteModelVersions(group) }
                    )
                }
            }
        } else {
            if (downloadedModels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "No downloaded models match \"$searchQuery\""
                            } else {
                                "No models downloaded yet."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(downloadedModels, key = { it.id }) { model ->
                    ModelItemRow(
                        model = model,
                        status = getStatus(model),
                        systemPrompt = getSystemPrompt(model),
                        onDownload = { onDownload(model) },
                        onDelete = { onDelete(model) },
                        onLoad = { onLoad(model) },
                        onSystemPromptChange = { prompt -> onSystemPromptChange(model, prompt) },
                        isLoading = checkIsLoading(model),
                        isLoaded = checkIsLoaded(model)
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ModelSettingsPreview() {
    LocalModelAITheme {
        ModelSettingsContent(
            builtInModels = listOf(
                ModelSpec(
                    id = "tinyllama_task",
                    displayName = "TinyLlama-1.1B-Chat-q8-1280",
                    sizeLabel = "Hosted download",
                    downloadUrl = "",
                    fileName = "TinyLlama.task",
                    description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
                ),
                ModelSpec(
                    id = "gemma3_task",
                    displayName = "Gemma3-1B-q8-4096",
                    sizeLabel = "Hosted download",
                    downloadUrl = "",
                    fileName = "Gemma3.task",
                    description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
                )
            ),
            remoteModelGroups = emptyList(),
            getStatus = { model ->
                if (model.id == "tinyllama_task") {
                    ModelDownloadStatus(
                        isDownloaded = true,
                        isDownloading = false,
                        progressPercent = 100,
                        statusMessage = "Downloaded"
                    )
                } else {
                    ModelDownloadStatus(
                        isDownloaded = false,
                        isDownloading = true,
                        progressPercent = 38,
                        statusMessage = "Downloading"
                    )
                }
            },
            checkIsLoading = { false },
            checkIsLoaded = { false },
            onDownload = {},
            onDelete = {},
            onLoad = {},
            getSystemPrompt = { "" },
            onSystemPromptChange = { _, _ -> }
        )
    }
}
