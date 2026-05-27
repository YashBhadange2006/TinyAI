package com.example.localmodelai.chatui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.text.method.LinkMovementMethod
import android.widget.TextView
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.localmodelai.components.MessageBubble
import com.example.localmodelai.ui.theme.LocalModelAITheme
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isUser: Boolean
)

private data class PendingDeleteSession(
    val id: Long,
    val title: String
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

    var pendingDeleteSession by remember { mutableStateOf<PendingDeleteSession?>(null) }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "PocketAI",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text("New Chat") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            chatViewModel.startNewChat()
                            scope.launch { drawerState.close() }
                        }
                    )

                    Text(
                        text = "Chat Sessions",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (chatViewModel.chatSessions.isEmpty()) {
                        Text(
                            text = "No saved chats yet",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        chatViewModel.chatSessions.forEach { session ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NavigationDrawerItem(
                                    modifier = Modifier.weight(1f),
                                    label = { Text(session.title) },
                                    selected = chatViewModel.isSelectedSession(session.id),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        chatViewModel.loadChatSession(session.id)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        pendingDeleteSession = PendingDeleteSession(
                                            id = session.id,
                                            title = session.title
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete chat"
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Section 2",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            chatViewModel.refreshModelStatus()
                            onOpenModelSettings()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Help and feedback") },
                        selected = false,
                        icon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null) },
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
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

    pendingDeleteSession?.let { session ->
        AlertDialog(
            onDismissRequest = {
                pendingDeleteSession = null
            },
            title = {
                Text("Delete chat")
            },
            text = {
                Text("Delete \"${session.title}\" permanently?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.deleteChatSession(session.id)
                        pendingDeleteSession = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        pendingDeleteSession = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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




@Composable
fun ChatInput(
    selectedAttachmentName: String?,
    onAttachClick: () -> Unit,
    onClearAttachment: () -> Unit,
    onSend: (String) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedAttachmentName != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Attached: $selectedAttachmentName",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = onClearAttachment) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment"
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttachClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach image"
                )
            }
            TextField(
                value = text,
                onValueChange = {
                    text = it
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Type a message...")
                }
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank() || selectedAttachmentName != null) {
                        onSend(text)
                        text = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
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


