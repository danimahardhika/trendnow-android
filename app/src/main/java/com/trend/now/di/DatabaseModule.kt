package com.trend.now.di

import android.content.Context
import androidx.room.Room
import com.trend.now.data.local.NewsCacheDao
import com.trend.now.data.local.NewsDatabase
import com.trend.now.data.local.TopicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val NEWS_DATABASE = "news_database"

    @Provides
    @Singleton
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDatabase =
        Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            NEWS_DATABASE
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTopicDao(newsDatabase: NewsDatabase): TopicDao = newsDatabase.topicDao()

    @Provides
    @Singleton
    fun provideNewsCacheDao(newsDatabase: NewsDatabase): NewsCacheDao =
        newsDatabase.newsCacheDao()
}