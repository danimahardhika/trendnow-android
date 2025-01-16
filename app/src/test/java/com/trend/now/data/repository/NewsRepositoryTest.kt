package com.trend.now.data.repository

import com.trend.now.TestCoroutineRule
import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.core.network.ApiResult
import com.trend.now.core.util.toDate
import com.trend.now.data.datasource.NewsLocalDataSource
import com.trend.now.data.datasource.NewsRemoteDataSource
import com.trend.now.data.local.TopicDao
import com.trend.now.data.model.News
import com.trend.now.data.model.NewsResult
import com.trend.now.data.model.Publisher
import com.trend.now.util.topic
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NewsRepositoryTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @RelaxedMockK
    private lateinit var mockNewsRemoteDataSource: NewsRemoteDataSource
    @RelaxedMockK
    private lateinit var mockNewsLocalDataSource: NewsLocalDataSource
    @RelaxedMockK
    private lateinit var mockTopicDao: TopicDao
    @RelaxedMockK
    private lateinit var mockNewsCacheManager: NewsCacheManager

    private lateinit var newsRepository: NewsRepository
    private lateinit var currentDateProvider: () -> Calendar

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        currentDateProvider = {
            Calendar.getInstance().apply {
                time = "2025-01-02T23:00:00+00:00".toDate()!!
                timeZone = TimeZone.getTimeZone("UTC+0")
            }
        }
        newsRepository = NewsRepositoryImpl(
            remoteDataSource = mockNewsRemoteDataSource,
            localDataSource = mockNewsLocalDataSource,
            topicDao = mockTopicDao,
            newsCacheManager = mockNewsCacheManager,
            currentDateProvider = currentDateProvider
        )
    }

    @Test
    fun `fetchSupportedTopics should use remote data when local data is not exist`() = runTest {
        // given
        val generalTopic = topic(id = "general", createdAt = System.currentTimeMillis())
        val topics = listOf(topic(createdAt = System.currentTimeMillis()), generalTopic)
        // local data is empty
        coEvery {
            mockNewsLocalDataSource.getSupportedTopics()
        } returns ApiResult.Success(listOf())
        coEvery {
            mockNewsRemoteDataSource.getSupportedTopics()
        } returns ApiResult.Success(topics)
        coEvery { mockTopicDao.insertAll(any()) } just Runs

        // when
        val result = newsRepository.fetchSupportedTopics()

        // then
        coVerify(exactly = 1) { mockNewsLocalDataSource.getSupportedTopics() }
        coVerify(exactly = 1) { mockNewsRemoteDataSource.getSupportedTopics() }
        coVerify(exactly = 1) {
            mockTopicDao.insertAll(
                // make sure the inserted data createdAt value adjusted
                topics.map {
                    it.copy(createdAt = currentDateProvider().timeInMillis)
                }
            )
        }
        // make sure the general topic moved to index 0
        assertEquals(
            listOf(generalTopic, topics[0]),
            (result as ApiResult.Success).data
        )
    }

    @Test
    fun `fetchSupportedTopics should use local data when it is exist and cache age is valid`() = runTest {
        // given
        // cache age is less than 180 days
        val createdAt = "2025-01-30T23:00:00+00:00".toDate()!!.time
        val generalTopic = topic(id = "general", createdAt = createdAt)
        val topics = listOf(topic(createdAt = createdAt), generalTopic)

        coEvery {
            mockNewsLocalDataSource.getSupportedTopics()
        } returns ApiResult.Success(topics)

        // when
        val result = newsRepository.fetchSupportedTopics()

        // then
        coVerify(exactly = 1) { mockNewsLocalDataSource.getSupportedTopics() }
        coVerify(exactly = 0) { mockNewsRemoteDataSource.getSupportedTopics() }
        coVerify(exactly = 0) { mockTopicDao.insertAll(any()) }
        // make sure the general topic moved to index 0
        assertEquals(
            listOf(generalTopic, topics[0]),
            (result as ApiResult.Success).data
        )
    }

    @Test
    fun `fetchSupportedTopics should use remote data when local data is exist but cache age is invalid`() = runTest {
        // given
        // cache age is more than 180 days
        val createdAt = "2025-08-02T23:00:00+00:00".toDate()!!.time
        val generalTopic = topic(id = "general", createdAt = createdAt)
        val topics = listOf(topic(createdAt = createdAt), generalTopic)

        coEvery {
            mockNewsLocalDataSource.getSupportedTopics()
        } returns ApiResult.Success(topics)
        coEvery {
            mockNewsRemoteDataSource.getSupportedTopics()
        } returns ApiResult.Success(topics)
        coEvery { mockTopicDao.insertAll(any()) } just Runs

        // when
        val result = newsRepository.fetchSupportedTopics()

        // then
        coVerify(exactly = 1) { mockNewsLocalDataSource.getSupportedTopics() }
        coVerify(exactly = 1) { mockNewsRemoteDataSource.getSupportedTopics() }
        coVerify(exactly = 1) {
            mockTopicDao.insertAll(
                // make sure the inserted data createdAt value adjusted
                topics.map {
                    it.copy(createdAt = currentDateProvider().timeInMillis)
                }
            )
        }
        // make sure the general topic moved to index 0
        assertEquals(
            listOf(generalTopic, topics[0]),
            (result as ApiResult.Success).data
        )
    }

    @Test
    fun `fetchTrendingNews should add news cache when response is from network`() = runTest {
        // given
        coEvery { mockNewsCacheManager.addNewsCache(any()) } just Runs
        coEvery {
            mockNewsRemoteDataSource.getTrendingNews(topic = any(), language = any())
        } returns ApiResult.Success(
            NewsResult(data = listOf(news()), fromCache = false, url = "url")
        )

        // when
        val result = newsRepository.fetchTrendingNews(topic = "", language = "")

        // then
        coVerify(exactly = 1) { mockNewsCacheManager.addNewsCache(any()) }
        assertEquals(1, (result as ApiResult.Success).data.size)
    }

    @Test
    fun `fetchTrendingNews should skip add news cache when response is from cache`() = runTest {
        // given
        coEvery {
            mockNewsRemoteDataSource.getTrendingNews(topic = any(), language = any())
        } returns ApiResult.Success(
            NewsResult(data = listOf(news()), fromCache = true, url = "url")
        )

        // when
        val result = newsRepository.fetchTrendingNews(topic = "", language = "")

        // then
        coVerify(exactly = 0) { mockNewsCacheManager.addNewsCache(any()) }
        assertEquals(1, (result as ApiResult.Success).data.size)
    }

    @Test
    fun `fetchTrendingNews should return api result error`() = runTest {
        // given
        val error = ApiResult.Error(code = 401, message = "Unauthorized")
        coEvery {
            mockNewsRemoteDataSource.getTrendingNews(topic = any(), language = any())
        } returns error

        // when
        val result = newsRepository.fetchTrendingNews(topic = "", language = "")

        // then
        assertEquals(error.code, (result as ApiResult.Error).code)
        assertEquals(error.message, result.message)
    }

    @Test
    fun `fetchSupportedCountries should fetch data from local`() = runTest {
        // given
        coEvery {
            mockNewsLocalDataSource.getSupportedCountries()
        } returns ApiResult.Success(mapOf())

        // when
        newsRepository.fetchSupportedCountries()

        // then
        coVerify(exactly = 1) { mockNewsLocalDataSource.getSupportedCountries() }
        coVerify(exactly = 0) { mockNewsRemoteDataSource.getSupportedCountries() }
    }

    private fun news(): News =
        News(
            title = "title",
            url = "url",
            excerpt = "excerpt",
            thumbnail = "thumbnail",
            date = "date",
            publisher = Publisher(name = "name", url = "url", favicon = "favicon")
        )

    companion object {

        @AfterClass
        @JvmStatic
        fun clearMocks() {
            clearAllMocks()
            unmockkAll()
        }
    }
}