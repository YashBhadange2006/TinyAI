package com.example.localmodelai.data.api

import com.example.localmodelai.ai.ModelSpec
import com.google.gson.annotations.SerializedName

data class HFModel(
    val id: String,
    val downloads: Int? = null,
    val likes: Int? = null,
    val siblings: List<HFSibling>? = null
) {
    fun getLitertLMFiles(): List<HFSibling> {
        return siblings?.filter { it.fileName.endsWith(".litertlm", ignoreCase = true) } ?: emptyList()
    }
}

data class HFSibling(
    @SerializedName("rfilename")
    val fileName: String,
    val size: Long? = null
) {
    fun downloadUrl(modelId: String): String {
        return "https://huggingface.co/$modelId/resolve/main/$fileName?download=true"
    }
}

data class HFRemoteModelGroup(
    val id: String,
    val downloads: Int,
    val likes: Int,
    val versionFiles: List<HFSibling>
) {
    val displayName: String
        get() = id.substringAfter("/")

    fun toVersionModelSpecs(): List<ModelSpec> {
        return versionFiles.map { file ->
            ModelSpec(
                id = remoteVersionId(file.fileName),
                displayName = "${displayName} - ${file.fileName.removeSuffix(".litertlm")}",
                sizeLabel = "LiteRT-LM version",
                downloadUrl = file.downloadUrl(id),
                fileName = file.fileName,
                description = "Download ${file.fileName} from the ${displayName} model repository."
            )
        }
    }

    private fun remoteVersionId(fileName: String): String {
        return "hf_${id.replace('/', '_')}_${fileName.replace('.', '_')}"
    }
}

fun HFModel.toRemoteGroup(): HFRemoteModelGroup? {
    val files = getLitertLMFiles()
    if (files.isEmpty()) {
        return null
    }

    return HFRemoteModelGroup(
        id = id,
        downloads = downloads ?: 0,
        likes = likes ?: 0,
        versionFiles = files
    )
}
