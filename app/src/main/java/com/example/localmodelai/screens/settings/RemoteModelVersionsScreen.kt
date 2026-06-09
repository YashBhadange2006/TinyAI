package com.example.localmodelai.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localmodelai.components.ModelItemRow
import com.example.localmodelai.data.api.HFRemoteModelGroup
import com.example.localmodelai.screens.chat.deleteSelectedModel
import com.example.localmodelai.screens.chat.downloadSelectedModel
import com.example.localmodelai.screens.chat.getModelStatus
import com.example.localmodelai.screens.chat.getRemoteModelGroup
import com.example.localmodelai.screens.chat.getSystemPrompt
import com.example.localmodelai.screens.chat.isLoadedModel
import com.example.localmodelai.screens.chat.isLoadingModel
import com.example.localmodelai.screens.chat.loadSelectedModel
import com.example.localmodelai.screens.chat.updateSystemPrompt
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.screens.chat.toggleGpu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteModelVersionsScreen(
    chatViewModel: ChatViewModel,
    repoId: String,
    repoTitle: String,
    onBack: () -> Unit
) {
    val repo = chatViewModel.getRemoteModelGroup(repoId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(repoTitle, style = MaterialTheme.typography.titleMedium) },
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
        if (repo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Remote model versions are still loading.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            RemoteModelVersionsContent(
                repo = repo,
                chatViewModel = chatViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun RemoteModelVersionsContent(
    repo: HFRemoteModelGroup,
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val versionModels = repo.toVersionModelSpecs()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = repo.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Download links for the available .litertlm versions in this repository.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (versionModels.isEmpty()) {
            item {
                Text(
                    text = "No .litertlm versions were found for this model.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(versionModels, key = { it.id }) { model ->
                ModelItemRow(
                    model = model,
                    status = chatViewModel.getModelStatus(model),
                    systemPrompt = chatViewModel.getSystemPrompt(model),
                    onDownload = { chatViewModel.downloadSelectedModel(model) },
                    onDelete = { chatViewModel.deleteSelectedModel(model) },
                    onLoad = { chatViewModel.loadSelectedModel(model) },
                    onSystemPromptChange = { prompt -> chatViewModel.updateSystemPrompt(model, prompt) },
                    isLoading = chatViewModel.isLoadingModel(model),
                    isLoaded = chatViewModel.isLoadedModel(model),
                    isGpuEnabled =  chatViewModel.isGpuEnabledForModel(model.id),
                    onGpuToggle = { enabled -> chatViewModel.toggleGpu(model.id,enabled) }
                )
            }
        }
    }
}
