package com.trend.now.data.datasource

import com.google.gson.Gson
import com.trend.now.core.network.ApiResult
import com.trend.now.core.network.Meta
import com.trend.now.core.util.ApiResultUtil
import com.trend.now.data.api.NewsService
import com.trend.now.data.model.Country
import com.trend.now.data.model.NewsResult
import com.trend.now.data.model.Topic
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit
import javax.inject.Inject

@ActivityRetainedScoped
class NewsRemoteDataSource @Inject constructor(
    private val retrofit: Retrofit,
    private val gson: Gson
) : NewsDataSource {

    override suspend fun getSupportedTopics(): ApiResult<List<Topic>> = try {
        val response = retrofit.create(NewsService::class.java)
            // fetch the topics data from info/topics endpoint
            .fetchSupportedTopics()
        val body = response.body()
        if (body != null) {
            ApiResult.Success(body.data)
        } else {
            ApiResultUtil.toApiResultError(gson, response)
        }
    } catch (e: Exception) {
        ApiResult.Error(-1, "")
    }

    override suspend fun getTrendingNews(
        topic: String,
        language: String,
        page: Int?,
        country: String?
    ): ApiResult<NewsResult> = try {
        val response = retrofit.create(NewsService::class.java)
            // fetch news data from trendings endpoint
            .fetchTrendingNews(
                topic = topic,
                language = language,
                country = country,
                page = page
            )
        val body = response.body()
        if (body != null) {
            val result = NewsResult(
                data = body.data,
                fromCache = response.raw().cacheResponse != null,
                url = response.raw().request.url.toUrl().toString()
            )
            ApiResult.Success(result, Meta(body.size, body.page, body.totalPages))
        } else {
            ApiResultUtil.toApiResultError(gson, response)
        }
    } catch (e: Exception) {
        ApiResult.Error(-1, "")
    }

    override suspend fun getSupportedCountries(): ApiResult<Map<String, Country>> {
        TODO("Not yet implemented")
    }
}