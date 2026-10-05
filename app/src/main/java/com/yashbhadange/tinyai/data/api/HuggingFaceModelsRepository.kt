package com.yashbhadange.tinyai.data.api

import android.net.Uri
import retrofit2.HttpException

class HuggingFaceModelsRepository(
    private val api: HFApi = RetrofitClient.api
) {
    suspend fun fetchModelsPage(
        cursors: Map<ModelFormat, String?>? = null
    ): HFModelPage {
        val formatsToLoad = cursors
            ?.filterValues { it != null }
            ?.keys
            ?.takeIf { it.isNotEmpty() }
            ?: ModelFormat.entries

        val pages = formatsToLoad.associateWith { format ->
            val response = api.fetchModels(
                filter = format.hfFilter.orEmpty(),
                limit = PAGE_SIZE,
                cursor = cursors?.get(format)
            )
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            response
        }

        return HFModelPage(
            groups = pages.flatMap { (format, response) ->
                response.body().orEmpty().mapNotNull { model -> model.toRemoteGroup(format) }
            },
            nextCursors = pages.mapValues { (_, response) ->
                response.headers()["Link"].nextPageCursor()
            }.filterValues { it != null }
        )
    }

    suspend fun fetchSpecificRepo(repoId: String): List<HFRemoteModelGroup> {
        return try {
            val model = api.fetchModelInfo(repoId)
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

private const val PAGE_SIZE = 10

data class HFModelPage(
    val groups: List<HFRemoteModelGroup>,
    val nextCursors: Map<ModelFormat, String?>
)

private fun String?.nextPageCursor(): String? {
    val nextPageUrl = this
        ?.split(',')
        ?.firstOrNull { it.contains("rel=\"next\"") }
        ?.substringAfter('<')
        ?.substringBefore('>')
        ?.takeIf { it.isNotBlank() }
        ?: return null

    return Uri.parse(nextPageUrl).getQueryParameter("cursor")
}
