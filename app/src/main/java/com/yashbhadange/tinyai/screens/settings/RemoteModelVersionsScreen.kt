package com.yashbhadange.tinyai.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashbhadange.tinyai.ai.ModelDownloadStatus
import com.yashbhadange.tinyai.ai.ModelSpec
import com.yashbhadange.tinyai.data.api.HFRemoteModelGroup
import com.yashbhadange.tinyai.data.api.ModelFormat
import com.yashbhadange.tinyai.screens.chat.ChatViewModel
import com.yashbhadange.tinyai.screens.chat.deleteSelectedModel
import com.yashbhadange.tinyai.screens.chat.downloadSelectedModel
import com.yashbhadange.tinyai.screens.chat.getModelStatus
import com.yashbhadange.tinyai.screens.chat.getRemoteModelGroup
import com.yashbhadange.tinyai.screens.chat.getSystemPrompt
import com.yashbhadange.tinyai.screens.chat.isLoadedModel
import com.yashbhadange.tinyai.screens.chat.isLoadingModel
import com.yashbhadange.tinyai.screens.chat.loadSelectedModel
import com.yashbhadange.tinyai.screens.chat.toggleGpu
import com.yashbhadange.tinyai.screens.chat.unloadSelectedModel
import com.yashbhadange.tinyai.screens.chat.updateSystemPrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteModelVersionsScreen(
    chatViewModel: ChatViewModel,
    repoId: String,
    format: ModelFormat,
    repoTitle: String,
    onBack: () -> Unit
) {
    var repo by remember { mutableStateOf(chatViewModel.getRemoteModelGroup(repoId, format)) }

    LaunchedEffect(repoId, format) {
        val detailedRepos = chatViewModel.remoteModelsRepository.fetchSpecificRepo(repoId)
        repo = detailedRepos.firstOrNull { it.format == format } ?: repo
    }

    val currentRepo = repo
    val versionModels = currentRepo?.toVersionModelSpecs().orEmpty()
    var selectedModelId by remember(versionModels) {
        mutableStateOf(versionModels.firstOrNull()?.id)
    }
    val selectedModel = versionModels.firstOrNull { it.id == selectedModelId } ?: versionModels.firstOrNull()
    val currentDownloadStatus = selectedModel?.let { chatViewModel.getModelStatus(it) } ?: chatViewModel.modelDownloadStatus

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
        },
        bottomBar = {
            if (currentRepo != null && selectedModel != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        OriginalStyleDownloadSection(
                            model = selectedModel,
                            status = currentDownloadStatus,
                            systemPrompt = chatViewModel.getSystemPrompt(selectedModel),
                            isGpuEnabled = chatViewModel.isGpuEnabledForModel(selectedModel.id),
                            isLoading = chatViewModel.isLoadingModel(selectedModel),
                            isLoaded = chatViewModel.isLoadedModel(selectedModel),
                            onDownload = { chatViewModel.downloadSelectedModel(selectedModel) },
                            onDelete = { chatViewModel.deleteSelectedModel(selectedModel) },
                            onLoad = { chatViewModel.loadSelectedModel(selectedModel) },
                            onUnload = { chatViewModel.unloadSelectedModel(selectedModel) },
                            onSystemPromptChange = { prompt -> chatViewModel.updateSystemPrompt(selectedModel, prompt) },
                            onGpuToggle = { enabled -> chatViewModel.toggleGpu(selectedModel.id, enabled) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (currentRepo == null) {
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
                repo = currentRepo,
                versionModels = versionModels,
                selectedModelId = selectedModelId,
                onSelectModel = { selectedModelId = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun RemoteModelVersionsContent(
    repo: HFRemoteModelGroup,
    versionModels: List<ModelSpec>,
    selectedModelId: String?,
    onSelectModel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://huggingface.co/" + repo.id)
                            )
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = repo.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = "Open in browser",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        item {
            ModelStatsCard(
                downloads = repo.downloads,
                likes = repo.likes,
                createdAt = repo.createdAt,
                lastModified = repo.lastModified
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model variants",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (versionModels.isEmpty()) {
            item {
                Text(
                    text = "No " + repo.format.extension + " versions were found for this model.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(versionModels, key = { it.id }) { model ->
                val isSelected = selectedModelId == model.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectModel(model.id) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = model.fileName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = model.sizeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectModel(model.id) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalStyleDownloadSection(
    model: ModelSpec,
    status: ModelDownloadStatus,
    systemPrompt: String,
    isGpuEnabled: Boolean,
    isLoading: Boolean,
    isLoaded: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onSystemPromptChange: (String) -> Unit,
    onGpuToggle: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val arrowRotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.inversePrimary
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (status.isDownloading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Downloading...",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { ((status.progressPercent ?: 0).coerceIn(0, 100)) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            }
        }

        when {
            status.isDownloading -> {
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Downloading...", fontWeight = FontWeight.SemiBold)
                }
            }
            status.isDownloaded -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = if (isLoaded) onUnload else onLoad,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when {
                                isLoading -> "Loading..."
                                isLoaded -> "Unload"
                                else -> "Chat Now"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Model",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Advance Settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            modifier = Modifier.rotate(arrowRotationState)
                        )
                    }

                    if (isExpanded) {
                        Text(
                            text = "Processing Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .width(210.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            SegmentedButton(
                                selected = !isGpuEnabled,
                                onClick = { onGpuToggle(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("CPU")
                            }

                            SegmentedButton(
                                selected = isGpuEnabled,
                                onClick = { onGpuToggle(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("GPU")
                            }
                        }

                        Text(
                            text = "System Prompt:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = onSystemPromptChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true,
                            placeholder = {
                                Text(
                                    text = "Enter your system prompt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(25.dp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            else -> {
                Button(
                    onClick = onDownload,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = gradientBrush, shape = CircleShape)
                            .padding(ButtonDefaults.ContentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Download Model",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelStatsCard(
    downloads: Int,
    likes: Int,
    createdAt: String?,
    lastModified: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.Outlined.Download,
                value = downloads.formatCompact(),
                label = "Downloads",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            StatItem(
                icon = Icons.Outlined.Favorite,
                value = likes.formatCompact(),
                label = "Likes",
                tint = Color(0xFFE53935),
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            StatItem(
                icon = Icons.Outlined.CalendarToday,
                value = createdAt.toDisplayMonthYear(),
                label = "Created",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            StatItem(
                icon = Icons.Outlined.Edit,
                value = lastModified.toDisplayMonthYear(),
                label = "Updated",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

private fun Int.formatCompact(): String {
    if (this >= 1_000_000) {
        val value = this / 1_000_000.0
        return if (value % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%.0fM", value)
        } else {
            String.format(java.util.Locale.US, "%.2fM", value)
        }
    } else if (this >= 1_000) {
        val value = this / 1_000.0
        return if (value % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%.0fK", value)
        } else {
            String.format(java.util.Locale.US, "%.2fK", value)
        }
    }
    return this.toString()
}

private fun String?.toDisplayMonthYear(): String {
    if (this.isNullOrBlank()) return "00/00"
    val clean = this.take(10)
    val parts = clean.split("-")
    if (parts.size >= 2) {
        val year = parts[0].takeLast(2)
        val month = parts[1]
        return month + "/" + year
    }
    return "00/00"
}