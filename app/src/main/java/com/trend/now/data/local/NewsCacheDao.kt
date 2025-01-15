package com.trend.now.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trend.now.data.model.NewsCache

@Dao
interface NewsCacheDao {
    @Query("SELECT * FROM news_cache WHERE url = :url")
    suspend fun getNewsCache(url: String): NewsCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsCache(newsCache: NewsCache)

    @Query("DELETE FROM news_cache WHERE parentUrl = :parentUrl")
    suspend fun deleteNewsCache(parentUrl: String)
}