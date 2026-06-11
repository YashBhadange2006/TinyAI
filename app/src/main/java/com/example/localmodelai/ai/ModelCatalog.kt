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

    val deepseek_r1_distill_qwen_1_5B = ModelSpec(
        id = "DeepSeek-R1-Distill-Qwen_task",
        displayName = "DeepSeek-R1-Distill-Qwen-1.5B-Q8-EKV4096",
        sizeLabel = "1.83GB download",
        downloadUrl = "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-1.5B/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.litertlm?download=true",
        fileName = "DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
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

    val qwen3_0_6b = ModelSpec(
        id = "Qwen3-0.6B",
        displayName = "Qwen3-0.6B",
        sizeLabel = "614MB download",
        downloadUrl = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm?download=true",
        fileName = "Qwen3-0.6B.litertlm",
        description = "Downloads the hosted LitertLM .litertlm model on demand, then loads it locally on device."
    )

    val supportedModels = listOf(gemma4_2b,deepseek_r1_distill_qwen_1_5B,gemma3_1b,qwen25_1_5B_Instruct,qwen3_0_6b)
    val defaultModel = gemma4_2b
}
