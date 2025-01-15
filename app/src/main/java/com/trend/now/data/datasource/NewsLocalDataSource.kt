package com.trend.now.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.trend.now.core.network.ApiResult
import com.trend.now.data.local.TopicDao
import com.trend.now.data.model.Topic
import com.trend.now.data.model.Country
import com.trend.now.data.model.NewsResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class NewsLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicDao: TopicDao,
    private val gson: Gson
) : NewsDataSource {

    override suspend fun getSupportedTopics(): ApiResult<List<Topic>> =
        ApiResult.Success(topicDao.getAllTopics())

    override suspend fun getTrendingNews(
        topic: String,
        language: String,
        page: Int?,
        country: String?
    ): ApiResult<NewsResult> {
        // using okhttp cache system for the local news data
        TODO("Not yet implemented")
    }

    override suspend fun getSupportedCountries(): ApiResult<Map<String, Country>> {
        // get supported countries from a file in assets dir
        val content = context.assets.open(SUPPORTED_COUNTRIES_FILE)
            .bufferedReader()
            .use {
                it.readText()
            }
        val countries: List<Country> = gson.fromJson(
            gson.fromJson(content, JsonObject::class.java).get("data"),
            object : TypeToken<List<Country>>() {}.type
        )
        return ApiResult.Success(data = countries.associateBy { it.code })
    }

    companion object {
        private const val SUPPORTED_COUNTRIES_FILE = "supported-countries.json"
    }
}