package com.yashbhadange.tinyai.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yashbhadange.tinyai.ai.ModelDownloadStatus
import com.yashbhadange.tinyai.ai.ModelSpec
import com.yashbhadange.tinyai.components.ModelItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnSeeMoreClicked(
    modelsList: List<ModelSpec>,
    getStatus: (ModelSpec) -> ModelDownloadStatus,
    getSystemPrompt: (ModelSpec) -> String,
    checkIsLoading: (ModelSpec) -> Boolean,
    checkIsLoaded: (ModelSpec) -> Boolean,
    isGpuEnabled: (ModelSpec) -> Boolean,
    onGpuToggle: (ModelSpec, Boolean) -> Unit,
    onDownload: (ModelSpec) -> Unit,
    onDelete: (ModelSpec) -> Unit,
    onLoad: (ModelSpec) -> Unit,
    onSystemPromptChange: (ModelSpec, String) -> Unit,
    onBack: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar (
                title = {Text("Recommended Models") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(modelsList,key = { it.id }){ model ->
                ModelItemRow(
                    model = model,
                    status = getStatus(model),
                    systemPrompt = getSystemPrompt(model),
                    modifier = Modifier.fillParentMaxWidth(0.85f), // responsively handle Card layout, 85% of any size of screen
                    onDownload = { onDownload(model) },
                    onDelete = { onDelete(model) },
                    onLoad = { onLoad(model) },
                    onSystemPromptChange = { prompt ->
                        onSystemPromptChange(
                            model,
                            prompt
                        )
                    },
                    isLoading = checkIsLoading(model),
                    isLoaded = checkIsLoaded(model),
                    isGpuEnabled =  isGpuEnabled(model),
                    onGpuToggle = { enabled -> onGpuToggle(model, enabled) }

                )
            }
        }
    }
}