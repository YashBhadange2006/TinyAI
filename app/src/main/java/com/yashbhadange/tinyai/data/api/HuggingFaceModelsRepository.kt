package com.yashbhadange.tinyai.data.api

class HuggingFaceModelsRepository(
    private val api: HFApi = RetrofitClient.api
) {
    suspend fun fetchRemoteLiteRtModels(): List<HFRemoteModelGroup> {
        return api.fetchLiteRTModels(
            author = "litert-community",
            expand = "siblings",
            limit = 100,
            sort = "downloads",
            direction = "-1"
        ).mapNotNull { it.toRemoteGroup(ModelFormat.LITERTLM) }
    }

    suspend fun fetchRemoteTaskModels(author: String?=null): List<HFRemoteModelGroup> {
        return api.fetchLiteRTModels(
            author = author,
            expand = "siblings",
            limit = 100,
            sort = "downloads",
            direction = "-1"
        ).mapNotNull { it.toRemoteGroup(ModelFormat.TASK) }
    }

    suspend fun fetchRemoteGgufModels(author: String?=null): List<HFRemoteModelGroup> {
        return api.fetchLiteRTModels(
            author = author,
            expand = "siblings",
            limit = 100,
            sort = "downloads",
            direction = "-1"
        ).mapNotNull { it.toRemoteGroup(ModelFormat.GGUF) }
    }

    suspend fun fetchSpecificRepo(repoId: String): List<HFRemoteModelGroup> {
        return try {
            val model = api.fetchModelInfo(repoId, expand = "siblings")
            val groups = mutableListOf<HFRemoteModelGroup>()

            model.toRemoteGroup(ModelFormat.LITERTLM)?.let { groups.add(it) }
            model.toRemoteGroup(ModelFormat.TASK)?.let { groups.add(it) }
            model.toRemoteGroup(ModelFormat.GGUF)?.let { groups.add(it) }

            groups
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}