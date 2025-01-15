package com.trend.now.core.cache

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.trend.now.core.cache.CacheConfig.MAX_NEWS_CACHE_IN_HOURS
import com.trend.now.core.util.fromMs
import com.trend.now.data.local.NewsCacheDao
import com.trend.now.data.model.NewsCache
import okhttp3.Cache
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class NewsCacheManager @Inject constructor(
    private val cache: Cache,
    private val newsCacheDao: NewsCacheDao,
    // workaround so that we can easily mock the current datetime in unit test
    private val currentDateProvider: () -> Calendar = { Calendar.getInstance() }
) {

    /**
     * Check whether the given url cache is available or unavailable.
     * If it's available check again whether it's still valid or invalid.
     * The validity checker is based on the [CacheConfig.MAX_NEWS_CACHE_IN_HOURS].
     *
     * @param url The endpoint full url including its query params
     * @return Whether should use cache for the given url or not
     */
    suspend fun isPreferUseCache(url: String): Boolean {
        // finding a string inside of list of string may has performance impact
        // if there are so many cached urls
        // but will keep this as for now since this is all we have
        val responseCache = cache.urls().find { it == url }
        if (responseCache == null) {
            return false
        }

        val cacheDateInMillis = newsCacheDao.getNewsCache(url)?.createdAt
        if (cacheDateInMillis != null) {
            val todayDate = currentDateProvider()
            val cacheDate = Calendar.getInstance().apply {
                timeInMillis = cacheDateInMillis
                timeZone = todayDate.timeZone
            }
            val diffInHours = TimeUnit.HOURS.fromMs(
                abs(todayDate.timeInMillis - cacheDate.timeInMillis)
            )
            // force use cache if the cache timestamp is still within the x timeframe
            // or still in the same day
            return (diffInHours < MAX_NEWS_CACHE_IN_HOURS)
                && (todayDate.day() == cacheDate.day()
                && todayDate.month() == cacheDate.month()
                && todayDate.year() == cacheDate.year())

        }
        return false
    }

    /**
     * Add news to local database.
     * This will only cache the news metadata.
     * The news content cache will be handled by okhttp cache.
     *
     * @param url The endpoint full url including its query params
     */
    suspend fun addNewsCache(url: String) {
        val parentUrl = getParentUrl(url)
        if (parentUrl == url) {
            // there are 2 possibilities why this part called
            // 1. news cache has been outdated and needs to be updated
            // 2. first time load news
            // if the news cache has been outdated that means the cache for this url are invalid
            // that's why we need to remove the parent url cache including its child caches manually
            newsCacheDao.deleteNewsCache(parentUrl = parentUrl)
        }

        val cache = NewsCache(
            url = url,
            parentUrl = getParentUrl(url),
            createdAt = currentDateProvider().timeInMillis
        )
        newsCacheDao.insertNewsCache(cache)
    }

    // need to make public as workaround to mock the function
    // because we can't call Uri.parse in junit test
    @VisibleForTesting
    fun getParentUrl(url: String): String = try {
        val uri = Uri.parse(url)
        // parent url only for trendings path
        if (uri.query?.isBlank() == true || uri.path != TRENDING_PATH) {
            url
        } else {
            // original url:
            // ".../v2/trendings?topic=general&language=en&country=US&page=2"
            val uriBuilder = Uri.Builder()
                .scheme(uri.scheme)
                .authority(uri.authority)
                .path(uri.path)
            // remove page query from the url
            uri.queryParameterNames.forEach { param ->
                if (param != PAGE_QUERY_NAME) {
                    uri.getQueryParameters(param).forEach { value ->
                        uriBuilder.appendQueryParameter(param, value)
                    }
                }
            }
            // modified url as parent url:
            // ".../v2/trendings?topic=general&language=en&country=US"
            uriBuilder.build().toString()
        }
    } catch (e: Exception) {
        url
    }

    companion object {
        private const val TRENDING_PATH = "/v2/trendings"
        private const val PAGE_QUERY_NAME = "page"
    }
}

// create our own find function for iterator
// so that it will be easier to mock the cache urls in unit test
private fun <T> Iterator<T>.find(predicate: (T) -> Boolean): T? {
    var result: T? = null
    var hasNext = hasNext()
    while (hasNext) {
        result = next()
        hasNext = if (predicate(result)) {
            false
        } else {
            result = null
            hasNext()
        }
    }
    return result
}

private fun Calendar.day() = get(Calendar.DAY_OF_MONTH)

private fun Calendar.month() = get(Calendar.MONTH)

private fun Calendar.year() = get(Calendar.YEAR)