package com.example.localmodelai.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.localmodelai.screens.chat.ChatUI
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.screens.media.MediaScreen
import com.example.localmodelai.screens.settings.ModelSettingsScreen
import com.example.localmodelai.screens.settings.RemoteModelVersionsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface PocketAIScreen : NavKey {
    @Serializable
    data object Chat : PocketAIScreen

    @Serializable
    data object ModelMedia: PocketAIScreen
    @Serializable
    data object ModelSettings : PocketAIScreen
    @Serializable
    data class RemoteModelVersions(
        val repoId: String,
        val title: String
    ) : PocketAIScreen
}

@Composable
fun PocketAINavigation(
    isDarkTheme: Boolean,
    themeModeLabel: String,
    onToggleTheme: () -> Unit
) {
    val backStack = rememberNavBackStack(PocketAIScreen.Chat)
    val chatViewModel: ChatViewModel = viewModel()

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.lastIndex > 0) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = entryProvider {
            entry<PocketAIScreen.Chat> {
                ChatUI(
                    chatViewModel = chatViewModel,
                    onOpenModelSettings = {
                        if (backStack.lastOrNull() != PocketAIScreen.ModelSettings) {
                            backStack.add(PocketAIScreen.ModelSettings)
                        }
                    } ,
                    onOpenModelMedia = {
                        if (backStack.lastOrNull() != PocketAIScreen.ModelMedia){
                            backStack.add(PocketAIScreen.ModelMedia)
                        }
                    }
                )
            }
            entry<PocketAIScreen.ModelSettings> {
                ModelSettingsScreen(
                    chatViewModel = chatViewModel,
                    isDarkTheme = isDarkTheme,
                    themeModeLabel = themeModeLabel,
                    onToggleTheme = onToggleTheme,
                    onBack = {
                        if (backStack.lastIndex > 0) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    onOpenRemoteModelVersions = { group ->
                        backStack.add(
                            PocketAIScreen.RemoteModelVersions(
                                repoId = group.id,
                                title = group.displayName
                            )
                        )
                    }
                )
            }
            entry<PocketAIScreen.ModelMedia> {
                MediaScreen(
                    chatViewModel = chatViewModel,
                    onBack = {
                        if (backStack.lastIndex > 0) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
            entry<PocketAIScreen.RemoteModelVersions> { key ->
                RemoteModelVersionsScreen(
                    chatViewModel = chatViewModel,
                    repoId = key.repoId,
                    repoTitle = key.title,
                    onBack = {
                        if (backStack.lastIndex > 0) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
        }
    )
}
