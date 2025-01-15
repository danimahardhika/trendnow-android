package com.trend.now.data.datasource

import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.Country
import com.trend.now.data.model.NewsResult
import com.trend.now.data.model.Topic

interface NewsDataSource {
    suspend fun getSupportedTopics(): ApiResult<List<Topic>>
    suspend fun getTrendingNews(
        topic: String,
        language: String,
        // set the default value to null because this is optional query param
        page: Int? = null,
        country: String? = null
    ): ApiResult<NewsResult>
    suspend fun getSupportedCountries(): ApiResult<Map<String, Country>>
}