# PocketAI

PocketAI is an Android app for downloading, loading, and running on-device LLMs directly on a phone using MediaPipe GenAI and LiteRT-LM.

Instead of bundling large model files inside the APK, the app keeps the install size small and downloads supported models only when the user chooses one. After download, the model is stored locally on the device and can be used for offline chat.

## Overview

This project focuses on practical on-device inference on constrained Android hardware. The goal is not just to build a chat UI, but to handle the real product problems around local AI on phones:

- Keeping the APK lightweight.
- Selecting models that fit within mobile memory limits.
- Downloading models on demand.
- Tracking model download progress in-app.
- Loading local models cleanly for offline inference.

The app is designed and tested around real mobile limitations, including mid-range Android devices.

## Features

- Kotlin-based Android app built with Jetpack Compose.
- On-demand model downloads using Android's `DownloadManager`.
- Local storage of downloaded model files.
- Offline chat after the model is downloaded and loaded.
- Integration with both MediaPipe `LlmInference` and LiteRT-LM for on-device generation.
- Multi-model picker with support for `.task` and `.litertlm` formats.
- Per-model in-app download progress UI.
- Chat history saved locally using Room database.
- Image input for multimodal models.

## How It Works

1.  **Select a Model**: The user opens the model settings screen to see a list of supported models.
2.  **Download**: The user taps `Download Model`. The app uses Android's `DownloadManager` to fetch the model from its hosted URL. Progress is shown in the UI.
3.  **Store Locally**: The model file is saved into app-specific storage on the device.
4.  **Load for Inference**: The user taps `Load Model`. The app initializes either MediaPipe `LlmInference` or a LiteRT-LM `Engine` depending on the model's file format (`.task` or `.litertlm`).
5.  **Chat Offline**: Once loaded, the user can conduct text and image-based conversations on-device without an internet connection.

## Architecture

The project is structured for modularity and clarity.

-   **UI Layer (`screens`, `components`)**: Built with Jetpack Compose. `ChatUI.kt` is the main screen, supported by components like `MessageBubble.kt` and `InputBar.kt`.
-   **State/Control Layer (`ChatViewModel.kt`)**: Manages UI state, coordinates model downloading and loading, handles message generation, and interacts with the database.
-   **AI/Inference Layer (`ai`)**:
    -   `LocalLLMManager.kt`: A wrapper that abstracts away the specific inference runtime (MediaPipe or LiteRT-LM). It handles loading models and generating responses for text and image prompts.
    -   `ModelDownloader.kt`: Manages the download lifecycle of models using `DownloadManager`.
    -   `ModelCatalog.kt`: A static registry of all supported models, including their metadata, download URLs, and file names.
-   **Data Layer (`data`)**:
    -   `database`: Uses Room DB (`AppDatabase`, `ChatDao`) to persist chat sessions and messages.
    -   `storage`: `MediaStorage.kt` handles saving and retrieving images used in chats.
-   **Navigation (`navigation`)**: A simple navigation setup using a custom `NavKey`-based system to move between the chat, settings, and media screens.

## Tech Stack

-   **Language**: Kotlin
-   **UI**: Jetpack Compose
-   **State Management**: Android ViewModel
-   **On-Device AI**: MediaPipe GenAI `LlmInference`, Google LiteRT-LM
-   **Model Management**: Android `DownloadManager`
-   **Database**: Room
-   **Dependencies**: Coil for image loading, Markwon for Markdown rendering.

## Project Structure

-   `app/src/main/java/.../ai/LocalLLMManager.kt`: Core logic for on-device inference.
-   `app/src/main/java/.../ai/ModelDownloader.kt`: Handles model downloads.
-   `app/src/main/java/.../ai/ModelCatalog.kt`: List of supported models.
-   `app/src/main/java/.../screens/chat/ChatViewModel.kt`: Central ViewModel for the app.
-   `app/src/main/java/.../screens/chat/ChatUI.kt`: Main chat interface.
-   `app/src/main/java/.../screens/settings/ModelSettings.kt`: UI for managing models.
-   `app/src/main/java/.../data/database/AppDatabase.kt`: Room database definition.
-   `app/src/main/java/.../navigation/navigation.kt`: App navigation logic.

## Storage Behavior

-   **Models**: Downloaded models are stored in app-specific external storage, typically at `/storage/emulated/0/Android/data/com.example.localmodelai/files/models/`.
-   **Chat Media**: Images attached to chats are saved in the app's internal files directory to ensure they are persisted with the chat history.

## Running the Project

1.  Open the project in a recent version of Android Studio.
2.  Allow Gradle to sync all dependencies.
3.  Run the app on an Android device (recommended over an emulator for realistic performance testing).
4.  Navigate to **Settings** from the top-right menu icon.
5.  Choose a model and tap **Download Model**.
6.  Once downloaded, tap **Load Model**.
7.  Return to the chat screen and start your offline conversation.
