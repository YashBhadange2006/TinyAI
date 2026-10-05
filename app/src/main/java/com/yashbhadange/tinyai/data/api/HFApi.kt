package com.yashbhadange.tinyai.data.api

import androidx.annotation.Keep
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response

@Keep
interface HFApi {
    @GET("api/models")
    suspend fun fetchModels(
        @Query("filter") filter: String,
        @Query("limit") limit: Int = 30,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: String = "-1",
        @Query("cursor") cursor: String? = null,
        @Query("full") full: Boolean = true
    ): Response<List<HFModel>>

    @GET("api/models/{repoId}")
    suspend fun fetchModelInfo(
        @Path(value = "repoId", encoded = true) repoId: String,
        @Query("blobs") includeFileMetadata: Boolean = true
    ): HFModel
}