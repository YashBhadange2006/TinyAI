package com.example.localmodelai.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AutoAwesomeMosaic
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.screens.chat.deleteChatSession
import com.example.localmodelai.screens.chat.isSelectedSession
import com.example.localmodelai.screens.chat.loadChatSession
import com.example.localmodelai.screens.chat.refreshModelStatus
import com.example.localmodelai.screens.chat.startNewChat
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
    onOpenModelMedia: () -> Unit,
    content: @Composable () -> Unit
) {
    var pendingDeleteSession by remember { mutableStateOf<PendingDeleteSession?>(null) }
    var expandedDropdownSessionId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp) // Adjusted layout spacing for a more fluid floating card effect
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "PocketAI",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )


                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Preferences",
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings", style = MaterialTheme.typography.labelLarge) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            chatViewModel.refreshModelStatus()
                            onOpenModelSettings()
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Media", style = MaterialTheme.typography.labelLarge) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesomeMosaic,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenModelMedia()
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Help and feedback", style = MaterialTheme.typography.labelLarge) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {},
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("New Chat", style = MaterialTheme.typography.labelLarge) },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            chatViewModel.startNewChat()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Chat Sessions",
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (chatViewModel.chatSessions.isEmpty()) {
                        Text(
                            text = "No saved chats yet",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        chatViewModel.chatSessions.forEach { session ->
                            val isSelected = chatViewModel.isSelectedSession(session.id)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    NavigationDrawerItem(
                                        label = {
                                            Text(
                                                text = session.title,
                                                maxLines = 1,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                                ),
                                                fontSize = 16.sp
                                            )
                                        },
                                        selected = isSelected,
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        onClick = {
                                            chatViewModel.loadChatSession(session.id)
                                            scope.launch { drawerState.close() }
                                        },
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),

                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                ) {
                                    IconButton(
                                        onClick = { expandedDropdownSessionId = session.id },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Chat Options",
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expandedDropdownSessionId == session.id,
                                        onDismissRequest = { expandedDropdownSessionId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                expandedDropdownSessionId = null
                                                pendingDeleteSession = PendingDeleteSession(
                                                    id = session.id,
                                                    title = session.title
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        content()
    }

    pendingDeleteSession?.let { session ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSession = null },
            title = { Text("Delete chat") },
            text = { Text("Delete \"${session.title}\" permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.deleteChatSession(session.id)
                        pendingDeleteSession = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { pendingDeleteSession = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
