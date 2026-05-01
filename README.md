# PocketAI

PocketAI is an Android app built with Kotlin and Jetpack Compose for downloading and running a local on-device LLM using MediaPipe GenAI.

The app itself stays lightweight. The model is not bundled inside the APK. Instead, the user downloads a hosted `.task` model file when they tap the download button, and the model is then loaded locally on the device for offline chat.

## Features

- Android app built with Kotlin
- Jetpack Compose chat UI
- On-demand model download
- Local model loading from device storage
- Offline chat after the model is downloaded
- Uses MediaPipe GenAI `LlmInference`
- No custom JNI or custom inference engine required

## How It Works

1. The app contains the chat UI and model management logic.
2. The actual LLM model is hosted online as a MediaPipe-compatible `.task` file.
3. When the user taps `Download Model`, the app downloads the `.task` file to device storage.
4. When the user taps `Load Model`, the app loads the `.task` file using MediaPipe.
5. Prompts are then processed locally on the device.

## Current Model Hosting

The app is configured to download the model from:

`https://huggingface.co/Bioniok/LocalModel/resolve/main/gemma-4-E2B-it-web.task`

## Tech Stack

- Kotlin
- Jetpack Compose
- Android ViewModel
- Android DownloadManager
- MediaPipe GenAI

## Project Structure

- `app/src/main/java/com/example/localmodelai/MainActivity.kt`  
  Main Android activity

- `app/src/main/java/com/example/localmodelai/chatui/ChatUI.kt`  
  Compose UI for chat and model actions

- `app/src/main/java/com/example/localmodelai/chatui/ChatViewModel.kt`  
  Handles app state, model download state, loading, and chat messages

- `app/src/main/java/com/example/localmodelai/chatui/ModelDownloader.kt`  
  Downloads the hosted `.task` model file and checks progress/status

- `app/src/main/java/com/example/localmodelai/chatui/LocalLLMManager.kt`  
  Loads the model with MediaPipe and generates responses

- `app/src/main/java/com/example/localmodelai/chatui/ModelCatalog.kt`  
  Stores model metadata such as file name, URL, and display name

## Setup

### Requirements

- Android Studio
- Android SDK
- Internet connection for model download
- A valid MediaPipe-compatible `.task` model file

### Run the Project

1. Open the project in Android Studio
2. Let Gradle sync
3. Run the app on an Android device or emulator
4. Open the model menu
5. Download the model
6. Load the model
7. Start chatting

## Important Note

This app uses MediaPipe `LlmInference`, so the downloaded model must be a valid MediaPipe-compatible `.task` bundle.
A random `.gguf` model will not work with this app unless the whole runtime is changed to something else like `llama.cpp`.

## Why The Model Is Not Bundled Inside The App

Bundling the model inside the APK would make the app very large, often hundreds of MB or more.

This project avoids that by:
- keeping the APK small
- hosting the model externally
- downloading the model only when the user requests it
- running inference locally after download

## Future Improvements

- Better download progress UI
- Multiple model options
- Clearer load failure handling
- Chat history persistence
- Better model status indicators

## Status

This project is an experimental local LLM Android app focused on learning and simple on-device integration without building a custom low-level inference stack.

Clone Project:
```
https://github.com/YashBhadange2006/PocketAI.git
```
