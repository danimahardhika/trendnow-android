package com.trend.now.core.cache

import com.trend.now.TestCoroutineRule
import com.trend.now.core.util.NetworkUtil
import com.trend.now.core.util.toDate
import com.trend.now.data.local.NewsCacheDao
import com.trend.now.data.model.NewsCache
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import okhttp3.Cache
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NewsCacheManagerTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @RelaxedMockK
    private lateinit var mockNewsCacheDao: NewsCacheDao
    @RelaxedMockK
    private lateinit var mockCache: Cache

    private lateinit var newsCacheManager: NewsCacheManager
    private lateinit var currentDateProvider: () -> Calendar

    private val mockParentUrl =
        "https://api.com/v2/trendings?topic=general&language=en&country=US"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(NetworkUtil)
        currentDateProvider = {
            Calendar.getInstance().apply {
                // mock current datetime to check cache mechanism
                time = "2025-01-02T23:00:00+00:00".toDate()!!
                timeZone = TimeZone.getTimeZone("UTC+0")
            }
        }
        newsCacheManager = NewsCacheManager(
            cache = mockCache,
            newsCacheDao = mockNewsCacheDao,
            currentDateProvider = currentDateProvider
        )
    }

    @Test
    fun `isCacheAvailable should return true when cache age is less than 1 hour`() = runTest {
        // given
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator

        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns newsCache(
            // cache age is less than 1 hour
            createdAt = "2025-01-02T22:30:00+00:00".toDate()!!.time
        )

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(true, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return true when cache age is more than 18 hours but still within the same day`() = runTest {
        // given
        currentDateProvider = {
            Calendar.getInstance().apply {
                time = "2025-01-02T00:00:00+00:00".toDate()!!
                timeZone = TimeZone.getTimeZone("UTC+0")
            }
        }
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator

        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns newsCache(
            // cache age is more than 18 hours
            // but still within the same day
            createdAt = "2025-01-02T23:00:00+00:00".toDate()!!.time
        )

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(true, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return false when cache age is invalid`() = runTest {
        // given
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator

        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns newsCache(
            // cache age is after current date time
            createdAt = "2025-01-03T00:00:00+00:00".toDate()!!.time
        )

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(false, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return false when okhttp cache is not exist`() = runTest {
        // given
        // empty cache
        val mockIterator = mutableListOf("").listIterator()
        every { mockCache.urls() } returns mockIterator

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(false, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return false when okhttp cache is exist and cache metadata is not exist`() = runTest {
        // given
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator
        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns null

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(false, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return false when cache age is more than 2 days`() = runTest {
        // given
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator
        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns newsCache(
            // cache age is more than 2 days
            createdAt = "2025-01-05T00:00:00+00:00".toDate()!!.time
        )

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(false, isCacheAvailable)
    }

    @Test
    fun `isCacheAvailable should return false when cache age is less than 18 hours but not within the same day`() = runTest {
        // given
        val mockIterator = mutableListOf(mockParentUrl).listIterator()
        every { mockCache.urls() } returns mockIterator
        coEvery { mockNewsCacheDao.getNewsCache(mockParentUrl) } returns newsCache(
            // cache age is less than 18 hours
            // but not within the same day
            createdAt = "2025-01-01T23:50:00+00:00".toDate()!!.time
        )

        // when
        val isCacheAvailable = newsCacheManager.isCacheAvailable(mockParentUrl)

        // then
        assertEquals(false, isCacheAvailable)
    }

    @Test
    fun `addNewsCache should delete and add cache when url is parent url`() = runTest {
        // given
        val parentUrl = mockParentUrl
        val newsCache = NewsCache(
            url = parentUrl,
            parentUrl = parentUrl,
            createdAt = currentDateProvider().timeInMillis
        )
        coEvery { mockNewsCacheDao.deleteNewsCache(parentUrl = parentUrl) } just Runs
        coEvery { mockNewsCacheDao.insertNewsCache(newsCache) } just Runs

        // when
        newsCacheManager.addNewsCache(parentUrl)

        // then
        coVerify(exactly = 1) { mockNewsCacheDao.deleteNewsCache(parentUrl) }
        coVerify(exactly = 1) { mockNewsCacheDao.insertNewsCache(newsCache) }
    }

    @Test
    fun `addNewsCache should delete and add cache when url query is empty`() = runTest {
        // given
        val url = "https://api.com/info/topics"
        val newsCache = NewsCache(
            url = url,
            parentUrl = url,
            createdAt = currentDateProvider().timeInMillis
        )
        coEvery { mockNewsCacheDao.deleteNewsCache(parentUrl = url) } just Runs
        coEvery { mockNewsCacheDao.insertNewsCache(newsCache) } just Runs

        // when
        newsCacheManager.addNewsCache(url)

        // then
        coVerify(exactly = 1) { mockNewsCacheDao.deleteNewsCache(url) }
        coVerify(exactly = 1) { mockNewsCacheDao.insertNewsCache(newsCache) }
    }

    @Test
    fun `addNewsCache should delete and add cache when url query is not empty but with unknown path`() = runTest {
        // given
        val url = "https://api.com/info/topics?page=1"
        val newsCache = NewsCache(
            url = url,
            parentUrl = url,
            createdAt = currentDateProvider().timeInMillis
        )
        coEvery { mockNewsCacheDao.deleteNewsCache(parentUrl = url) } just Runs
        coEvery { mockNewsCacheDao.insertNewsCache(newsCache) } just Runs

        // when
        newsCacheManager.addNewsCache(url)

        // then
        coVerify(exactly = 1) { mockNewsCacheDao.deleteNewsCache(url) }
        coVerify(exactly = 1) { mockNewsCacheDao.insertNewsCache(newsCache) }
    }

    @Test
    fun `addNewsCache should add cache without delete when url is not parent url`() = runTest {
        // given
        val mockNewsCacheManager = spyk(newsCacheManager)
        val url = "${mockParentUrl}&page=2"
        val newsCache = NewsCache(
            url = url,
            parentUrl = mockParentUrl,
            createdAt = currentDateProvider().timeInMillis
        )
        coEvery { mockNewsCacheDao.insertNewsCache(newsCache) } just Runs
        // need to mock getParentUrl because we can run Uri.parse in junit test
        // isReturnDefaultValues = true will return the given string as it is which not what we wanted
        every { mockNewsCacheManager.getParentUrl(url) } returns mockParentUrl

        // when
        mockNewsCacheManager.addNewsCache(url)

        // then
        coVerify(exactly = 0) { mockNewsCacheDao.deleteNewsCache(any()) }
        coVerify(exactly = 1) { mockNewsCacheDao.insertNewsCache(newsCache) }
    }

    private fun newsCache(createdAt: Long): NewsCache =
        NewsCache(url = mockParentUrl, parentUrl = mockParentUrl, createdAt = createdAt)

    companion object {

        @AfterClass
        @JvmStatic
        fun clearMocks() {
            clearAllMocks()
            unmockkAll()
        }
    }
}