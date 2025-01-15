package com.trend.now.data.api

import com.trend.now.data.api.response.BasicResponse
import com.trend.now.data.api.response.PaginationResponse
import com.trend.now.data.model.News
import com.trend.now.data.model.Topic
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {

    @GET("trendings")
    suspend fun fetchTrendingNews(
        @Query("topic") topic: String,
        @Query("language") language: String,
        @Query("country") country: String?,
        @Query("page") page: Int?,
    // not using the pagination response directly
    // so that we can still inspect the raw response later
    ): Response<PaginationResponse<List<News>>>

    @GET("info/topics")
    suspend fun fetchSupportedTopics(): Response<BasicResponse<List<Topic>>>
}