package com.trend.now.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trend.now.core.cache.CacheConfig
import com.trend.now.core.network.CacheInterceptor
import com.trend.now.core.network.HeaderInterceptor
import com.trend.now.core.cache.NewsCacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
        newsCacheManager: NewsCacheManager
    ): OkHttpClient {
        val cacheInterceptor = CacheInterceptor(
            context = context,
            newsCacheManager = newsCacheManager
        )
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(cacheInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpCache(@ApplicationContext context: Context): Cache =
        CacheConfig.create(context)

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://news-api14.p.rapidapi.com/v2/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
}