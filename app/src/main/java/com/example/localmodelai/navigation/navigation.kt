package com.example.localmodelai.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.localmodelai.chatui.ChatUI
import com.example.localmodelai.chatui.ChatViewModel
import com.example.localmodelai.chatui.ModelSettingsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface PocketAIScreen : NavKey {
    @Serializable
    data object Chat : PocketAIScreen

    @Serializable
    data object ModelSettings : PocketAIScreen
}

@Composable
fun PocketAINavigation() {
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
                    }
                )
            }
            entry<PocketAIScreen.ModelSettings> {
                ModelSettingsScreen(
                    chatViewModel = chatViewModel,
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

