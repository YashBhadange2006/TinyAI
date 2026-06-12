package com.yashbhadange.tinyai.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.yashbhadange.tinyai.ai.ModelCatalog
import com.yashbhadange.tinyai.screens.chat.ChatUI
import com.yashbhadange.tinyai.screens.chat.ChatViewModel
import com.yashbhadange.tinyai.screens.chat.*
import com.yashbhadange.tinyai.screens.chat.getSystemPrompt
import com.yashbhadange.tinyai.screens.media.MediaScreen
import com.yashbhadange.tinyai.screens.settings.ModelSettingsScreen
import com.yashbhadange.tinyai.screens.settings.OnSeeMoreClicked
import com.yashbhadange.tinyai.screens.settings.RemoteModelVersionsScreen
import kotlinx.serialization.Serializable
import androidx.core.net.toUri

@Serializable
sealed interface PocketAIScreen : NavKey {
    @Serializable
    data object RecommendedModels: PocketAIScreen
    @Serializable
    data object Chat : PocketAIScreen

    @Serializable
    data object ModelMedia: PocketAIScreen
    @Serializable
    data object ModelSettings : PocketAIScreen
    @Serializable
    data object HelpAndFeedback : PocketAIScreen
    @Serializable
    data class RemoteModelVersions(
        val repoId: String,
        val title: String,
    ) : PocketAIScreen
}

@Composable
fun PocketAINavigation(
    isDarkTheme: Boolean,
    themeModeLabel: String,
    onToggleTheme: () -> Unit,
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
                    },
                    onOpenHelpAndFeedback = {
                        if (backStack.lastOrNull() != PocketAIScreen.HelpAndFeedback) {
                            backStack.add(PocketAIScreen.HelpAndFeedback)
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
                    },
                    onSeeMoreClicked = {
                        backStack.add(PocketAIScreen.RecommendedModels)
                    }
                )
            }
            entry<PocketAIScreen.RecommendedModels> {
                OnSeeMoreClicked(
                    modelsList = ModelCatalog.supportedModels,
                    getStatus = { chatViewModel.getModelStatus(it) },
                    getSystemPrompt = { chatViewModel.getSystemPrompt(it) },
                    checkIsLoading = { chatViewModel.isLoadingModel(it) },
                    checkIsLoaded = { chatViewModel.isLoadedModel(it) },
                    isGpuEnabled = { chatViewModel.isGpuEnabledForModel(it.id) },
                    onGpuToggle = { model, enabled -> chatViewModel.toggleGpu(model.id, enabled) },
                    onDownload = { chatViewModel.downloadSelectedModel(it) },
                    onDelete = { chatViewModel.deleteSelectedModel(it) },
                    onLoad = { chatViewModel.loadSelectedModel(it) },
                    onSystemPromptChange = { model, prompt -> chatViewModel.updateSystemPrompt(model, prompt) },
                    onBack = {
                        if (backStack.lastIndex > 0) {
                            backStack.removeAt(backStack.lastIndex)
                        }
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
            entry<PocketAIScreen.HelpAndFeedback> {
                val context = androidx.compose.ui.platform.LocalContext.current
                com.yashbhadange.tinyai.screens.feedback.HelpAndFeedbackScreen(
                    onBackClick = {
                        if (backStack.lastIndex > 0) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    onGitHubClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            "https://github.com/YashBhadange2006/PocketAI.git".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    )
}
