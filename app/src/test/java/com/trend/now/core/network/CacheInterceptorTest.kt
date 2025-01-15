package com.trend.now.core.network

import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.core.util.NetworkUtil
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

class CacheInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    @RelaxedMockK
    private lateinit var mockNewsCacheManager: NewsCacheManager
    @RelaxedMockK
    private lateinit var mockCache: Cache

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var cacheInterceptor: CacheInterceptor
    private lateinit var request: Request

    @Before
    fun setup() {
        mockkObject(NetworkUtil)
        MockKAnnotations.init(this)
        mockWebServer = MockWebServer()
        cacheInterceptor = CacheInterceptor(
            context = mockk(relaxed = true),
            newsCacheManager = mockNewsCacheManager
        )
        okHttpClient = OkHttpClient.Builder()
            .cache(mockCache)
            .addInterceptor(cacheInterceptor)
            .build()
        // since we did not filter specific endpoint for caching
        // let's just re-use the same request for all tests
        request = Request.Builder()
            .url(mockWebServer.url("/something"))
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should make request with cache options when hasNetwork is false`() {
        // given
        every { NetworkUtil.hasNetwork(any()) } returns false
        // when
        mockWebServer.enqueue(MockResponse())
        val response = okHttpClient.newCall(request).execute()
        // then
        assert(response.request.headers["Cache-Control"]!!.isNotBlank())
    }

    @Test
    fun `should make request with cache control when isPreferUseCache returns true`() {
        // given
        every { NetworkUtil.hasNetwork(any()) } returns true
        coEvery { mockNewsCacheManager.isPreferUseCache(any()) } returns true
        // when
        mockWebServer.enqueue(MockResponse())
        val response = okHttpClient.newCall(request).execute()
        // then
        assert(response.request.headers["Cache-Control"]!!.isNotBlank())
    }

    @Test
    fun `should make request without cache control when isPreferUseCache returns false`() {
        // given
        every { NetworkUtil.hasNetwork(any()) } returns true
        coEvery { mockNewsCacheManager.isPreferUseCache(any()) } returns false
        // when
        mockWebServer.enqueue(MockResponse())
        val response = okHttpClient.newCall(request).execute()
        // then
        assert(response.request.headers["Cache-Control"] == null)
    }

    companion object {

        @AfterClass
        @JvmStatic
        fun clearMocks() {
            clearAllMocks()
            unmockkAll()
        }
    }
}