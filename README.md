# PocketAI

PocketAI is an Android app for downloading, loading, and running on-device LLMs directly on a phone using MediaPipe GenAI.

Instead of bundling large model files inside the APK, the app keeps the install size small and downloads supported models only when the user chooses one. After download, the model is stored locally on the device and can be used for offline chat.

## Overview

This project focuses on practical on-device inference on constrained Android hardware. The goal is not just to build a chat UI, but to handle the real product problems around local AI on phones:

- keeping the APK lightweight
- selecting models that actually fit within mobile memory limits
- downloading models on demand
- tracking model download progress in-app
- loading local models cleanly for offline inference

The app is designed and tested around real mobile limitations, including mid-range Android devices.

## Features

- Kotlin-based Android app built with Jetpack Compose
- On-demand model downloads using `DownloadManager`
- Local storage of downloaded model files
- Offline chat after the model is downloaded and loaded
- MediaPipe `LlmInference` integration for on-device generation
- Multi-model picker dialog
- Per-model in-app download progress UI
- Lightweight APK with externally hosted model assets

## Currently Supported Models

The app currently exposes these models in the model picker:

- `TinyLlama-1.1B-Chat-q8-1280`
- `Gemma3-1B-q8-4096`
- `DeepSeek-R1-Distill-Qwen-1.5B-Q8-EKV4096`
- `Gemma2-2b-it-cpu-int8`

These models are currently configured in MediaPipe-compatible `.task` format.

## How It Works

1. The user opens the model picker from the app.
2. The app shows the list of supported models.
3. When the user taps `Download Model`, the selected model is downloaded from its hosted URL.
4. The file is saved into app-specific storage on the device.
5. When the user taps `Load Model`, the app initializes MediaPipe `LlmInference` using the downloaded local file path.
6. After loading completes, the user can chat offline on-device.

## Architecture

The project is intentionally kept simple and modular.

### UI Layer

`ChatUI.kt`
- Main chat screen
- Model picker dialog
- Per-model download progress display
- Message list and input area

### State / Control Layer

`ChatViewModel.kt`
- Tracks the active model
- Manages download state
- Manages model load state
- Coordinates chat generation requests

### Model Download Layer

`ModelDownloader.kt`
- Starts model downloads with Android `DownloadManager`
- Tracks current download status
- Computes progress percentage
- Resolves local file paths

### Local Inference Layer

`LocalLLMManager.kt`
- Loads MediaPipe-compatible local model files
- Creates `LlmInference` instances
- Sends prompts for local generation
- Returns generated responses

### Model Registry

`ModelCatalog.kt`
- Stores the supported model list
- Keeps metadata such as:
  - model name
  - file name
  - hosted URL
  - size label
  - description

## Tech Stack

- Kotlin
- Jetpack Compose
- Android ViewModel
- Android DownloadManager
- MediaPipe GenAI `LlmInference`

## Project Structure

- `app/src/main/java/com/example/localmodelai/MainActivity.kt`
- `app/src/main/java/com/example/localmodelai/chatui/ChatUI.kt`
- `app/src/main/java/com/example/localmodelai/chatui/ChatViewModel.kt`
- `app/src/main/java/com/example/localmodelai/chatui/ModelDownloader.kt`
- `app/src/main/java/com/example/localmodelai/chatui/LocalLLMManager.kt`
- `app/src/main/java/com/example/localmodelai/chatui/ModelCatalog.kt`

## Storage Behavior

Downloaded models are stored in app-specific external storage, for example:

`/storage/emulated/0/Android/data/com.example.localmodelai/files/models/`

This keeps the files local to the device while avoiding APK bloat.

## Why Models Are Not Bundled Inside the APK

Model files are large, often hundreds of MB to multiple GB.

Bundling them directly into the APK would:

- increase app size significantly
- make installs slower
- waste storage for users who may never use a model
- make iteration and model replacement harder

This app avoids that by downloading only the selected model when needed.

## Current Inference Format

The current implementation is based on MediaPipe-compatible `.task` model files.

This means:

- MediaPipe `.task` files work with the current inference layer
- random `.gguf` files do not work with the current runtime
- `.litertlm` support is not part of the current production path in this codebase yet

## Planned Improvements

Planned improvements include:

- LiteRT-LM support
- advanced inference controls such as:
  - max tokens
  - top-k
  - temperature
  - context-related settings
- richer per-model configuration UI
- better model capability labels
- improved load/error diagnostics

## Running the Project

1. Open the project in Android Studio
2. Let Gradle sync
3. Run the app on an Android device
4. Open the model picker from the top-right settings icon
5. Download a supported model
6. Load the model
7. Start chatting offline

## Notes

This project reflects hands-on experimentation with running LLMs on real Android hardware, not just emulator-only development. A large part of the work here is around choosing model formats, memory-feasible model sizes, and mobile-friendly inference flow rather than only building the UI.
