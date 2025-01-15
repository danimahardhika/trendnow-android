package com.trend.now.core.cache

import android.content.Context
import okhttp3.Cache
import java.io.File

object CacheConfig {
    const val MAX_NEWS_CACHE_IN_HOURS = 18L
    const val MAX_TOPICS_CACHE_IN_DAYS = 60L

    fun create(context: Context): Cache {
        // use filesDir instead of cacheDir to keep our caches persistent
        // preventing external apps clearing our caches
        // because we will be relying those caches to keep our API hit limit
        val file = File(context.filesDir, CACHE_DIR)
        return Cache(file, CACHE_SIZE_IN_MB * 1024 * 1024)
    }

    private const val CACHE_DIR = "news_cache"
    private const val CACHE_SIZE_IN_MB = 50L
}