package com.trend.now.di

import android.content.Context
import com.google.gson.Gson
import com.trend.now.core.cache.NewsCacheManager
import com.trend.now.data.datasource.NewsLocalDataSource
import com.trend.now.data.datasource.NewsRemoteDataSource
import com.trend.now.data.local.TopicDao
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.NewsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
object NewsModule {

    @Provides
    @ActivityRetainedScoped
    fun provideNewsRepository(
        remoteDataSource: NewsRemoteDataSource,
        localDataSource: NewsLocalDataSource,
        topicDao: TopicDao,
        newsCacheManager: NewsCacheManager
    ): NewsRepository = NewsRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        topicDao = topicDao,
        newsCacheManager = newsCacheManager
    )

    @Provides
    @ActivityRetainedScoped
    fun provideNewsLocalDataSource(
        @ApplicationContext context: Context,
        topicDao: TopicDao,
        gson: Gson
    ): NewsLocalDataSource = NewsLocalDataSource(context, topicDao, gson)

    @Provides
    @ActivityRetainedScoped
    fun provideNewsRemoteDataSource(
        retrofit: Retrofit,
        gson: Gson
    ): NewsRemoteDataSource = NewsRemoteDataSource(retrofit, gson)
}