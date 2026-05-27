package com.example.localmodelai.chatui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.localmodelai.components.ChatInput
import com.example.localmodelai.components.MessageBubble
import com.example.localmodelai.navigation.AppNavigationDrawer
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isUser: Boolean,
    val messageType: String = "text",
    val imagePath: String? = null,
    val imageName: String? = null
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUI(
    chatViewModel: ChatViewModel,
    onOpenModelSettings: () -> Unit
) {
    val listState = rememberLazyListState()
    val messages = chatViewModel.messages
    val isTyping = chatViewModel.isTyping
    val context = LocalContext.current
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            chatViewModel.setSelectedAttachment(
                uri = uri,
                displayName = resolveFileName(context, uri),
                mimeType = context.contentResolver.getType(uri)
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val statusLabel = when {
        chatViewModel.isModelLoaded -> "Ready"
        chatViewModel.isModelLoading -> "Loading"
        chatViewModel.modelDownloadStatus.isDownloading -> {
            chatViewModel.modelDownloadStatus.progressPercent?.let { "$it%" } ?: "Downloading"
        }
        chatViewModel.modelDownloadStatus.isDownloaded -> "Downloaded"
        else -> "No model"
    }


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AppNavigationDrawer(
        chatViewModel = chatViewModel,
        drawerState = drawerState,
        onOpenModelSettings = onOpenModelSettings
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PocketAI Chat") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open drawer"
                            )
                        }
                    },
                    actions = {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(
                            onClick = {
                                chatViewModel.refreshModelStatus()
                                onOpenModelSettings()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Model settings"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                ChatInput(
                    selectedAttachmentName = chatViewModel.selectedAttachmentName,
                    onAttachClick = {
                        attachmentPicker.launch(arrayOf("image/*"))
                    },
                    onClearAttachment = {
                        chatViewModel.clearSelectedAttachment()
                    },
                    onSend = { userText ->
                        chatViewModel.sendMessage(userText)
                    }
                )
            }
        ) { padding ->
            MessageList(
                messages = messages,
                listState = listState,
                isTyping = isTyping,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun MessageList(
    messages: List<Message>,
    listState: LazyListState,
    isTyping: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(messages) { message ->
            MessageBubble(message)
        }
        if (isTyping) {
            item {
                TypingBubble()
            }
        }
    }
}

@Composable
fun TypingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Dot(dot1)
                Dot(dot1)
                Dot(dot1)
            }
        }
    }
}

@Composable
fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(alpha)
            .background(
                Color.Gray,
                CircleShape
            )
    )
}

private fun resolveFileName(
    context: Context,
    uri: Uri
): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
    }
    return uri.lastPathSegment ?: "Selected file"
}

