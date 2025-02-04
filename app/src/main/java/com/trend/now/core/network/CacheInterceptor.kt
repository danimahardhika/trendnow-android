package com.trend.now.core.network

import android.content.Context
import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.core.util.NetworkUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CacheInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val newsCacheManager: NewsCacheManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val url = request.url.toUrl().toString()

        // no need to filter the endpoint url here
        // the filtering should be done in the news cache manager
        val isCacheAvailable = runBlocking(Dispatchers.IO) {
            newsCacheManager.isCacheAvailable(url)
        }
        val newRequest = if (!NetworkUtil.hasNetwork(context) || isCacheAvailable) {
            requestBuilder
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        } else {
            requestBuilder.build()
        }
        return chain.proceed(newRequest)
    }
}