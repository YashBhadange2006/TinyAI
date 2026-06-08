# PocketAI

PocketAI is a high-performance Android application designed for private, on-device large language model (LLM) inference. By leveraging MediaPipe GenAI and LiteRT-LM runtimes, the app executes models locally on mobile hardware, ensuring that data never leaves the device and functionality remains available without an active internet connection.

## Key Features

* **Hugging Face Integration**: Beyond a curated list of supported models, the app includes a repository browser that allows users to fetch and download compatible models directly from Hugging Face.
* **Multimodal Vision Support**: Full support for vision-based models. Users can attach images to their prompts for tasks like image description and visual reasoning. The UI dynamically adapts to enable or disable attachment features based on the loaded model capabilities.
* **Dual Inference Engines**: Seamless switching between MediaPipe `.task` and LiteRT-LM `.litertlm` formats depending on the specific model architecture.
* **System Prompt Customization**: A dedicated interface for defining system instructions. This allows users to set specific personas or constraints for the model, which are persisted on a per-model basis and saved with chat sessions.
* **Advanced Markdown Rendering**: The chat interface supports rich text including tables, formatted code blocks, and LaTeX mathematical expressions using the Markwon library suite.
* **Dynamic Lifecycle Management**: To keep the initial installation size minimal, models are downloaded on demand. The app manages the local storage, loading, and unloading of these assets to optimize device memory usage.
* **Local Session Persistence**: All conversations and associated media are stored in a local Room database, providing a complete offline history of interactions.

## Technical Architecture

The application is built using a modern Android stack with a focus on modularity and efficiency.

### Inference Layer
The core logic resides in the `LocalLLMManager`, which abstracts the complexity of the underlying runtimes. It handles the initialization of the `LlmInference` engine and the LiteRT `Engine`, managing the state of the model throughout the application lifecycle.

### Data and Media Management
* **Model Downloader**: Orchestrates background downloads via the Android `DownloadManager`. It provides real-time progress updates and handles network interruptions gracefully.
* **Media Storage**: A specialized system for managing image attachments. When an image is used in a chat, it is cached and linked to the message entry in the database to ensure persistence.
* **Room Database**: Stores structured data for chat sessions, messages, and model configurations.

### User Interface
The UI is implemented entirely in Jetpack Compose.
* **Chat Interface**: A reactive UI that handles streaming text responses and displays image attachments.
* **Model Catalog**: A centralized hub for managing downloaded models, viewing available models on Hugging Face, and configuring system prompts.
* **Navigation**: A custom navigation system that manages transitions between the chat, settings, and media viewing screens.

## Tech Stack

* **Language**: Kotlin
* **UI**: Jetpack Compose
* **State Management**: Android ViewModel with Kotlin Flow
* **Inference**: MediaPipe GenAI, LiteRT-LM
* **Database**: Room
* **Networking**: Android `DownloadManager` for model assets
* **Image Processing**: Coil, MediaPipe `BitmapImageBuilder`
* **Formatting**: Markwon (Core, Tables, LaTeX, Inline Parser)

## Installation and Setup

### Requirements
* A physical Android device running API 24 (Nougat) or higher.
* Recommended 4GB+ of RAM for 1B to 3B parameter models.
* Sufficient internal storage for model files (typically 500MB to 2.5GB per model).

### Building from Source
1. Clone the repository:
   
