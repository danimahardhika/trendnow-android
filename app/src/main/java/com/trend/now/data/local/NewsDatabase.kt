package com.trend.now.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trend.now.data.model.NewsCache
import com.trend.now.data.model.Topic

@Database(entities = [Topic::class, NewsCache::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun newsCacheDao(): NewsCacheDao
}