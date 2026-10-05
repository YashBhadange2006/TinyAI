package com.yashbhadange.tinyai.data.api

import androidx.annotation.Keep
import com.yashbhadange.tinyai.ai.ModelSpec
import com.google.gson.annotations.SerializedName

@Keep
data class HFModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("author")
    val author: String? = null,

    @SerializedName("downloads")
    val downloads: Int? = null,

    @SerializedName("likes")
    val likes: Int? = null,

    @SerializedName("pipeline_tag")
    val pipelineTag: String? = null,

    @SerializedName("library_name")
    val libraryName: String? = null,

    @SerializedName("lastModified")
    val lastModified: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("siblings")
    val siblings: List<HFSibling>? = null
) {
    fun getLitertLMFiles(): List<HFSibling> {
        return siblings?.filter { it.fileName.endsWith(".litertlm", ignoreCase = true) } ?: emptyList()
    }
    fun getTaskFiles(): List<HFSibling> {
        return siblings?.filter { it.fileName.endsWith(".task", ignoreCase = true) } ?: emptyList()
    }
    fun getGgufFiles(): List<HFSibling> {
        return siblings?.filter { it.fileName.endsWith(".gguf", ignoreCase = true) } ?: emptyList()
    }
}

@Keep
data class HFSibling(
    @SerializedName("rfilename")
    val fileName: String,
    @SerializedName("size") val size: Long? = null
) {
    fun downloadUrl(modelId: String): String {
        return "https://huggingface.co/$modelId/resolve/main/$fileName?download=true"
    }
}

@Keep
data class HFRemoteModelGroup(
    @SerializedName("id")
    val id: String,

    @SerializedName("author")
    val author: String? = null,

    @SerializedName("downloads")
    val downloads: Int,

    @SerializedName("likes")
    val likes: Int,

    @SerializedName("pipelineTag")
    val pipelineTag: String? = null,

    @SerializedName("libraryName")
    val libraryName: String? = null,

    @SerializedName("lastModified")
    val lastModified: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("versionFiles")
    val versionFiles: List<HFSibling>,

    val format: ModelFormat
) {
    val displayName: String
        get() = id.substringAfter("/")

    fun toVersionModelSpecs(): List<ModelSpec> {
        return versionFiles.map { file ->
            ModelSpec(
                id = remoteVersionId(file.fileName),
                displayName = "${displayName} - ${file.fileName.removeSuffix(format.extension)}",
                sizeLabel = file.size.toReadableSize(),
                downloadUrl = file.downloadUrl(id),
                fileName = file.fileName,
                description = "Download ${file.fileName} from the $displayName model repository."
            )
        }
    }

    private fun remoteVersionId(fileName: String): String {
        return "hf_${id.replace('/', '_')}_${fileName.replace('.', '_')}"
    }
}

fun HFModel.toRemoteGroup(format: ModelFormat): HFRemoteModelGroup? {
    val files = when (format) {
        ModelFormat.LITERTLM -> getLitertLMFiles()
        ModelFormat.TASK -> getTaskFiles()
        ModelFormat.GGUF -> getGgufFiles()
    }
    if (files.isEmpty()) {
        return null
    }

    return HFRemoteModelGroup(
        id = id,
        author = author,
        downloads = downloads ?: 0,
        likes = likes ?: 0,
        pipelineTag = pipelineTag,
        libraryName = libraryName,
        lastModified = lastModified,
        createdAt = createdAt,
        versionFiles = files,
        format = format
    )
}

private fun Long?.toReadableSize(): String {
    val bytes = this ?: return "Size unavailable"
    if (bytes <= 0L) return "Size unavailable"

    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0

    return when {
        bytes >= gb -> String.format("%.2f GB", bytes / gb)
        bytes >= mb -> String.format("%.2f MB", bytes / mb)
        bytes >= kb -> String.format("%.2f KB", bytes / kb)
        else -> "$bytes B"
    }
}
