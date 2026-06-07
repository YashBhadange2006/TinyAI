package com.example.localmodelai.ai

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

    val qwen25_1_5B_Instruct = ModelSpec(
        id = "Qwen2.5-1.5B-Instruct_q8_4098",
        displayName = "Qwen2.5-1.5B-Instruct_q8_4098",
        sizeLabel = "1.6GB download",
        downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm?download=true",
        fileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )


    val gemma4_2b = ModelSpec(
        id = "gemma-4-E2B-it",
        displayName = "gemma-4-E2B-it",
        sizeLabel = "2.58GB download",
        downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
        fileName = "gemma-4-E2B-it.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )

    val gemma3_1b = ModelSpec(
        id = "Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096",
        displayName = "Gemma3-1B-it-multi-prefill-int4-4096",
        sizeLabel = "584MB download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm?download=true",
        fileName = "Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )

    val SmolLM = ModelSpec(
        id = "SmolLM-135M-Instruct_multi-prefill-seq_f32_ekv1280",
        displayName = "SmolLM-135M-Instruct_multi-prefill-seq_f32_ekv1280",
        sizeLabel = "553MB download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/SmolLM-135M-Instruct_multi-prefill-seq_f32_ekv1280.task?download=true",
        fileName = "SmolLM-135M-Instruct_multi-prefill-seq_f32_ekv1280.task",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )

    val TinySwallow = ModelSpec(
        id = "TinySwallow-1.5B-Instruct",
        displayName = "TinySwallow-1.5B-Instruct",
        sizeLabel = "1.57GB download",
        downloadUrl = "https://huggingface.co/litert-community/TinySwallow-1.5B-Instruct/resolve/main/TinySwallow-1.5B-Instruct.litertlm?download=true",
        fileName = "TinySwallow-1.5B-Instruct.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )

    val supportedModels = listOf(TinySwallow,tinyLlama,SmolLM,gemma3_1b,gemma3,qwen25_1_5B_Instruct,deepseek_r1_distill_qwen_1_5B,gemma4_2b)
    val defaultModel = gemma3
}
