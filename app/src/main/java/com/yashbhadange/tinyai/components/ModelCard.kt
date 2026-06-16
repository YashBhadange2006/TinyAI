package com.yashbhadange.tinyai.components

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashbhadange.tinyai.ai.ModelDownloadStatus
import com.yashbhadange.tinyai.ai.ModelSpec
import com.yashbhadange.tinyai.ui.theme.LocalModelAITheme


data class StorageInfo(
    val totalLabel: String,
    val usedLabel: String,
    val progressFraction: Float,
)

@Composable
fun StorageCard() {
    val context = LocalContext.current
    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun getDetailedStorage(ctx: Context): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)

            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            val progress = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f

            StorageInfo(
                totalLabel = Formatter.formatFileSize(ctx, totalBytes),
                usedLabel = Formatter.formatFileSize(ctx, usedBytes),
                progressFraction = progress.coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            StorageInfo("0 GB", "0 GB", 0f)
        }
    }

    val storageInfo = remember { getDetailedStorage(context) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${(storageInfo.progressFraction * 100).toInt()}% full",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${storageInfo.usedLabel} / ${storageInfo.totalLabel}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { storageInfo.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4F56CD),
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f) // Pale background bar track
                )
            }
        }
    }
}


@Composable
private fun ModelIcon(modelName: String) {
    val nameLower = modelName.lowercase()
    when{
        nameLower.contains("deepseek") -> {
            Icon(
                imageVector = Icons.Default.Psychology ,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
        }
        nameLower.contains("gemma") -> {
            Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
        nameLower.contains("qwen") -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
fun ModelItemRow(
    model: ModelSpec,
    status: ModelDownloadStatus,
    systemPrompt: String,
    isGpuEnabled: Boolean,
    modifier: Modifier = Modifier,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onLoad: () -> Unit,
    onSystemPromptChange: (String) -> Unit,
    onGpuToggle: (Boolean) -> Unit,
    isLoading: Boolean,
    isLoaded: Boolean,
    showSize: Boolean = true,
) {

    var isExpanded by remember { mutableStateOf(false)}

    val arrowRotationState by animateFloatAsState(
        targetValue = if(isExpanded) 180f else 0f,
        label ="ArrowRotation"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.inversePrimary
        )
    )

    val glowingBorderBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), //glow
            MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.2f),
            Color.Transparent
        )
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .padding(5.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                brush = glowingBorderBrush,
                shape = RoundedCornerShape(28.dp)
            ),
        onClick = {
            if(status.isDownloaded){
                isExpanded = !isExpanded
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Circular Badge, Model Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ModelIcon(model.displayName)
                }

                // Name, Description, Size, and Loaded Tag
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        // loaded tag
                        if (isLoaded) {
                            Surface(
                                color = Color(0xFFE2F7EC),
                                contentColor = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Loaded",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    if (!status.isDownloading && showSize) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Model size",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(17.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(
                                text = "Size: ${model.sizeLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Middle Section: Linear Download Progress Indicator (if downloading)
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
                            text = "${status.progressPercent ?: 0}%",
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
                            onClick = onLoad,
                            enabled = !isLoading && !isLoaded,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = if (isLoaded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when {
                                    isLoading -> "Loading..."
                                    isLoaded -> "Chat Now"
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
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isExpanded = !isExpanded
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ){
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
                        if(isExpanded){

                            Text("Processing Mode",
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
                                    onClick = {onGpuToggle(false)},
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) {
                                    Text("CPU")
                                }

                                SegmentedButton(
                                    selected = isGpuEnabled,
                                    onClick = {onGpuToggle(true)},
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
                                placeholder = {Text(
                                    "Enter your system prompt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ) },
                                shape = RoundedCornerShape(25.dp),
                                singleLine = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    // Transparent border
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    // Grey container background inside search bar
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    // icon matching theme color
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
                            Text("Download Model", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
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
fun ModelItemRowPreview() {
    val mockModel = ModelSpec(
        id = "1L",
        displayName = "Gemma 4",
        description = "Advanced text-to-thought reasoning engine. 8B Parameters. Optimized for English and Code.",
        sizeLabel = "4.8 GB",
        fileName = "gemma4-8b.task",
        downloadUrl = ""
    )

    val mockStatus = ModelDownloadStatus(
        isDownloading = false,
        isDownloaded = true,
        progressPercent = 100,
        statusMessage = "Ready"
    )

    // Using your app's custom theme so it shifts colors correctly in Dark Mode
    LocalModelAITheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ModelItemRow(
                model = mockModel,
                status = mockStatus,
                onDownload = {},
                onDelete = {},
                onLoad = {},
                systemPrompt = "",
                onSystemPromptChange = {},
                isLoading = false,
                isLoaded = false,
                isGpuEnabled = true,
                onGpuToggle = {}
            )
        }
    }
}
