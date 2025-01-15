package com.trend.now.di

import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.data.local.NewsCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsCacheRepository(
        cache: Cache,
        newsCacheDao: NewsCacheDao,
    ): NewsCacheManager = NewsCacheManager(cache, newsCacheDao)
}