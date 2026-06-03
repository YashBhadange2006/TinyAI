package com.example.localmodelai.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localmodelai.ai.ModelCatalog
import com.example.localmodelai.ai.ModelDownloadStatus
import com.example.localmodelai.ai.ModelSpec
import com.example.localmodelai.components.ModelItemRow
import com.example.localmodelai.components.StorageCard
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.ui.theme.LocalModelAITheme

enum class SettingsTab(val title: String){
    EXPLORE("Explore"),
    MY_MODELS("My Models")
}
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

    var selectedTab by remember { mutableStateOf(SettingsTab.EXPLORE) }
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
            }
        }

        item {
            StorageCard()
        }

        item{
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                TabRowDefaults.primaryContainerColor,
                TabRowDefaults.primaryContentColor,
                @Composable { HorizontalDivider() },
            ){
                SettingsTab.entries.forEach { tab->
                    Tab(
                       selected =  selectedTab == tab,
                        onClick = {selectedTab = tab},
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if(selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
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
                    text = if (selectedTab == SettingsTab.EXPLORE)
                        "Download supported models on demand to get started."
                    else
                        "Models downloaded to your device storage that are ready to load.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        val filteredModels = when(selectedTab){
            SettingsTab.EXPLORE -> models
            SettingsTab.MY_MODELS -> models.filter { model -> getStatus(model).isDownloaded}

        }

        if(filteredModels.isEmpty()){
            item{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == SettingsTab.MY_MODELS) "No models downloaded yet." else "No models available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredModels,key = {it.id}){ model ->
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
