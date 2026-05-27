package com.example.localmodelai.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localmodelai.chatui.ChatViewModel
import kotlinx.coroutines.launch

private data class PendingDeleteSession(
    val id: Long,
    val title: String
)
@Composable
fun AppNavigationDrawer(
    chatViewModel: ChatViewModel,
    drawerState: DrawerState,
    onOpenModelSettings: () -> Unit,
    content: @Composable () -> Unit
) {
    var pendingDeleteSession by remember { mutableStateOf<PendingDeleteSession?>(null) }
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
        content()
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
