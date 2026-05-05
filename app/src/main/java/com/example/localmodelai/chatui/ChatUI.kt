package com.example.localmodelai.chatui

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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

data class Message(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUI() {
    val listState = rememberLazyListState()
    val chatViewModel: ChatViewModel = viewModel()
    val messages = chatViewModel.messages
    val isTyping = chatViewModel.isTyping

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    var showModelDialog by remember { mutableStateOf(false) }
    val statusLabel = when {
        chatViewModel.isModelLoaded -> "Ready"
        chatViewModel.isModelLoading -> "Loading"
        chatViewModel.modelDownloadStatus.isDownloading -> {
            chatViewModel.modelDownloadStatus.progressPercent?.let { "$it%" } ?: "Downloading"
        }
        chatViewModel.modelDownloadStatus.isDownloaded -> "Downloaded"
        else -> "No model"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Offline AI Chat")
                },
                actions = {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = {
                            chatViewModel.refreshModelStatus()
                            showModelDialog = true
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

    if (showModelDialog) {
        ModelPickerDialog(
            chatViewModel = chatViewModel,
            onDismiss = {
                showModelDialog = false
            }
        )
    }
}

@Composable
fun ModelPickerDialog(
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val allModels = ModelCatalog.supportedModels

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Local model")
                Text(
                    text = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device",
                    style = MaterialTheme.typography.bodySmall
                )
            }

        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allModels){ model->
                    ModelItemRow(
                        model = model,
                        status = chatViewModel.getModelStatus(model),
                        onDownload = {chatViewModel.downloadSelectedModel(model)},
                        onLoad = {
                            chatViewModel.loadSelectedModel(model)
                            onDismiss()
                        },
                        isLoading = chatViewModel.isModelLoading,
                        isLoaded = chatViewModel.isLoadedModel(model)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ModelItemRow(
    model : ModelSpec,
    status : ModelDownloadStatus,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    isLoading: Boolean,
    isLoaded: Boolean
){
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        Column(modifier = Modifier.padding(12.dp)){
            Text(text = model.displayName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Size: ${model.sizeLabel}", style = MaterialTheme.typography.labelSmall)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    onClick = onDownload,
                    enabled = !status.isDownloaded && !status.isDownloading,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(if(status.isDownloading) "Downloading..." else "Download Model")
                }

                Button(
                    onClick = onLoad,
                    enabled = status.isDownloaded && !isLoading,
                ){
                    Text(
                        if (isLoading) "Loading..."
                        else if (isLoaded) "Loaded"
                        else "Load Model"
                    )
                }
            }
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

@Composable
fun MessageBubble(message: Message) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment =
            if (message.isUser) Alignment.CenterEnd
            else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun ChatInput(
    onSend: (String) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(8.dp, 50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                if (text.isNotBlank()) {
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
