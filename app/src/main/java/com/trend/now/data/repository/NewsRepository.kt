package com.trend.now.data.repository

import com.trend.now.DEFAULT_TOPIC
import com.trend.now.core.cache.CacheConfig.MAX_TOPICS_CACHE_IN_DAYS
import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.core.network.ApiResult
import com.trend.now.core.util.fromMs
import com.trend.now.data.datasource.NewsLocalDataSource
import com.trend.now.data.datasource.NewsRemoteDataSource
import com.trend.now.data.local.TopicDao
import com.trend.now.data.model.Country
import com.trend.now.data.model.News
import com.trend.now.data.model.Topic
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

interface NewsRepository {
    suspend fun fetchSupportedTopics(): ApiResult<List<Topic>>
    suspend fun fetchTrendingNews(
        topic: String,
        language: String,
        country: String? = null,
        page: Int? = null
    ): ApiResult<List<News>>
    suspend fun fetchSupportedCountries(): ApiResult<Map<String, Country>>
}

@ActivityRetainedScoped
class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val localDataSource: NewsLocalDataSource,
    private val topicDao: TopicDao,
    private val newsCacheManager: NewsCacheManager,
    private val currentDateProvider: () -> Calendar = { Calendar.getInstance() }
) : NewsRepository {

    override suspend fun fetchSupportedTopics(): ApiResult<List<Topic>> =
        withContext(Dispatchers.IO) {
            // try to get the news topics from local database first
            val localResult = localDataSource.getSupportedTopics()
            (localResult as? ApiResult.Success)?.let { local ->
                // if its exist then check the first data created time
                local.data.firstOrNull()?.let { first ->
                    val diffInDays = TimeUnit.DAYS.fromMs(
                        abs(currentDateProvider().timeInMillis - first.createdAt)
                    )
                    // check if the cache age is valid or invalid
                    if (diffInDays < MAX_TOPICS_CACHE_IN_DAYS) {
                        // use the data from local database
                        return@withContext moveGeneralToFirst(local)
                    }
                }
            }
            // local data is not exist or outdated
            // call the news topics API
            val remoteResult = remoteDataSource.getSupportedTopics()
            if (remoteResult is ApiResult.Success) {
                // insert news topics from the response to local database
                topicDao.insertAll(
                    // need to set the createdAt field
                    // because retrofit will ignore the default value inside the data class
                    remoteResult.data.map {
                        it.copy(createdAt = currentDateProvider().timeInMillis)
                    }
                )
            }
            return@withContext moveGeneralToFirst(remoteResult)
        }

    override suspend fun fetchTrendingNews(
        topic: String,
        language: String,
        country: String?,
        page: Int?
    ): ApiResult<List<News>> = withContext(Dispatchers.IO) {
        val result = remoteDataSource.getTrendingNews(
            topic = topic,
            language = language,
            country = country,
            page = page
        )
        when (result) {
            is ApiResult.Success -> {
                if (!result.data.fromCache) {
                    // only add to cache when the result comes from network
                    // ignore if its comes from the  cache
                    newsCacheManager.addNewsCache(result.data.url)
                }
                ApiResult.Success(result.data.data, result.meta)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun fetchSupportedCountries(): ApiResult<Map<String, Country>> =
        withContext(Dispatchers.IO) {
            localDataSource.getSupportedCountries()
        }

    // move general topic to index 0 in a List<Topic>
    private fun moveGeneralToFirst(result: ApiResult<List<Topic>>): ApiResult<List<Topic>> {
        if (result is ApiResult.Success) {
            val topics = result.data
            val general = topics.find { it.id == DEFAULT_TOPIC }
            val data = if (general != null) {
                listOf(general).plus(topics.minus(general))
            } else {
                topics
            }
            return ApiResult.Success(data, result.meta)
        }
        return result
    }
}