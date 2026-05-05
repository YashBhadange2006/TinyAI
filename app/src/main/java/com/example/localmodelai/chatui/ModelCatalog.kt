package com.example.localmodelai.chatui

data class ModelSpec(
    val id: String,
    val displayName: String,
    val sizeLabel: String,
    val downloadUrl: String,
    val fileName: String,
    val description: String
)

object ModelCatalog {
    val tinyLlama = ModelSpec(
        id = "tinyllama_task",
        displayName = "TinyLlama-1.1B-Chat-q8-1280",
        sizeLabel = "Hosted download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/TinyLlama-1.1B-Chat-v1.0_multi-prefill-seq_q8_ekv1280.task?download=true",
        fileName = "TinyLlama-1.1B-Chat-v1.0_multi-prefill-seq_q8_ekv1280.task",
        description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
    )

    val gemma3 = ModelSpec(
        id = "gemma3",
        displayName = "Gemma3-1B-q8-4096",
        sizeLabel = "1.5GB download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/Gemma3-1B-IT_seq128_q8_ekv4096.task?download=true",
        fileName = "Gemma3-1B-IT_multi-prefill-seq_q8_ekv4096.task",
        description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
    )

    val qwen3_4b_q4 = ModelSpec(
        id = "Qwen-4b-4q_task",
        displayName = "Qwen3-thinking-4b-q4-ekv2048",
        sizeLabel = "2.1GB download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/qwen3_thinking_4b_q4_block128_ekv2048.task?download=true",
        fileName = "qwen3_thinking_4b_q4_block128_ekv2048.task",
        description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
    )

    val deepseek_r1_distill_qwen_1_5B = ModelSpec(
        id = "DeepSeek-R1-Distill-Qwen_task",
        displayName = "DeepSeek-R1-Distill-Qwen-1.5B-Q8-EKV4096",
        sizeLabel = "1.83GB download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.task?download=true",
        fileName = "DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.task",
        description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
    )

    val supportedModels = listOf(tinyLlama,gemma3,qwen3_4b_q4,deepseek_r1_distill_qwen_1_5B)
    val defaultModel = gemma3
}
