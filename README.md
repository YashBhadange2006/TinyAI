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

## Application Walkthrough

### Main Chat Interface
The primary screen is a streamlined chat environment designed for low-latency interaction. It supports real-time text streaming, allowing users to see the model's response as it is generated. The input bar is context-aware; it automatically enables the image attachment button only when a vision-capable model is loaded. If no model is active, the input bar provides clear guidance on how to load one from the settings.

### Model Catalog and Settings
This screen serves as the command center for the app's AI capabilities. Users can browse a pre-configured list of optimized models or search for specific repositories on Hugging Face. Each model entry displays its download status, size, and local path. From here, users can trigger downloads, delete unused models to free up space, or load a specific model into memory.

### Session Management
The app supports multiple concurrent chat sessions. Users can view their chat history in a dedicated list, rename sessions for better organization, or delete old conversations. Each session remembers the specific model and system prompt used, allowing for a seamless transition when jumping back into a previous context.

### System Prompt Editor
Accessible through the model settings, this feature allows for granular control over the model's behavior. Users can write and save custom instructions (e.g., "Act as a Python expert" or "Give concise answers"), which the app then injects as the system instruction during the model initialization phase.

## Technical Architecture

The application is built using a modern Android stack with a focus on modularity and efficiency.

### Inference Layer
The core logic resides in the `LocalLLMManager`, which abstracts the complexity of the underlying runtimes. It handles the initialization of the `LlmInference` engine and the LiteRT `Engine`, managing the state of the model throughout the application lifecycle.

### Data and Media Management
* **Model Downloader**: Orchestrates background downloads via the Android `DownloadManager`. It provides real-time progress updates and handles network interruptions gracefully.
* **Media Storage**: A specialized system for managing image attachments. When an image is used in a chat, it is cached and linked to the message entry in the database to ensure persistence.
* **Room Database**: Stores structured data for chat sessions, messages, and model configurations.

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

## Building from Source

1. Clone the repository:

```bash
git clone https://github.com/YashBhadange2006/PocketAI.git
```

2. Open the project in the latest version of Android Studio.

3. Sync the Gradle files to download the required dependencies.

4. Build and run the app on your connected Android device.

## Usage Workflow

### 1. Model Acquisition
Open the **Settings** screen to browse supported models or search Hugging Face. Download your preferred model using the integrated download manager.

### 2. Configuration
Optionally, set a custom system prompt to guide the model's behavior.

### 3. Loading
After the model finishes downloading, tap **Load** to initialize it in memory.

### 4. Interaction
Return to the **Chat** screen. If the selected model supports vision capabilities, the attachment icon will be enabled, allowing image-based queries.

### 5. Persistence
All chat sessions are automatically saved and can be resumed anytime from the session list.   
