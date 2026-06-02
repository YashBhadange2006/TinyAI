package com.example.localmodelai.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localmodelai.ai.ModelCatalog
import com.example.localmodelai.ai.ModelDownloadStatus
import com.example.localmodelai.ai.ModelSpec
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.ui.theme.LocalModelAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(
    chatViewModel: ChatViewModel,
    onBack: () -> Unit
) {
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
                }
            )
        }
    ) { innerPadding ->
        ModelSettingsContent(
            models = ModelCatalog.supportedModels,
            modifier = Modifier.padding(innerPadding),
            onDownload = { model -> chatViewModel.downloadSelectedModel(model) },
            onDelete = { model -> chatViewModel.deleteSelectedModel(model) },
            onLoad = { model -> chatViewModel.loadSelectedModel(model) },
            getStatus = { model -> chatViewModel.getModelStatus(model) },
            checkIsLoading = { model -> chatViewModel.isLoadingModel(model) },
            checkIsLoaded = { model -> chatViewModel.isLoadedModel(model) }
        )
    }
}

@Composable
fun ModelSettingsContent(
    models: List<ModelSpec>,
    modifier: Modifier = Modifier,
    onDownload: (ModelSpec) -> Unit,
    onDelete: (ModelSpec) -> Unit,
    onLoad: (ModelSpec) -> Unit,
    getStatus: (ModelSpec) -> ModelDownloadStatus,
    checkIsLoading: (ModelSpec) -> Boolean,
    checkIsLoaded: (ModelSpec) -> Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Local models",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Download supported .task or .litertlm models on demand, then load them locally on device.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        items(models) { model ->
            ModelItemRow(
                model = model,
                status = getStatus(model),
                onDownload = { onDownload(model) },
                onDelete = { onDelete(model) },
                onLoad = { onLoad(model) },
                isLoading = checkIsLoading(model),
                isLoaded = checkIsLoaded(model)
            )
        }
    }
}

@Composable
fun ModelItemRow(
    model: ModelSpec,
    status: ModelDownloadStatus,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onLoad: () -> Unit,
    isLoading: Boolean,
    isLoaded: Boolean
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = model.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Size: ${model.sizeLabel}",
                style = MaterialTheme.typography.labelSmall
            )

            if (status.isDownloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${status.progressPercent ?: 0}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ((status.progressPercent ?: 0).coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = Color(0xFFB031F3)
                    )
                }
            }

            when {
                status.isDownloading -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Downloading...")
                    }
                }
                status.isDownloaded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp,)
                            )
                            Text("Delete", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onLoad,
                            enabled = !isLoading && !isLoaded,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when {
                                    isLoading -> "Loading..."
                                    isLoaded -> "Loaded"
                                    else -> "Load Model"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Model")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelSettingsPreview() {
    LocalModelAITheme {
        ModelSettingsContent(
            models = listOf(
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
            onLoad = {}
        )
    }
}
