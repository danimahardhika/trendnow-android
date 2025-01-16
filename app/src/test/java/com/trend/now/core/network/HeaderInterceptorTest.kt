package com.trend.now.core.network

import com.trend.now.core.util.NetworkUtil
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HeaderInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    @RelaxedMockK
    private lateinit var mockCache: Cache

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var headerInterceptor: HeaderInterceptor
    private lateinit var request: Request

    @Before
    fun setup() {
        mockkObject(NetworkUtil)
        MockKAnnotations.init(this)
        mockWebServer = MockWebServer()
        headerInterceptor = HeaderInterceptor()
        okHttpClient = OkHttpClient.Builder()
            .cache(mockCache)
            .addInterceptor(headerInterceptor)
            .build()
        request = Request.Builder()
            .url(mockWebServer.url("/something"))
            .build()
    }

    @Test
    fun `should make request with api key`() {
        // when
        mockWebServer.enqueue(MockResponse())
        val response = okHttpClient.newCall(request).execute()

        // then
        assertEquals(
            "news-api14.p.rapidapi.com",
            response.request.headers["x-rapidapi-host"]
        )
        assertEquals(true, response.request.headers["x-rapidapi-key"]!!.isNotBlank())
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