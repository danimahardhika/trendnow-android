package com.trend.now.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.UserPrefRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserPrefModule {

    private const val NEWS_PREFERENCES = "news_preferences"

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(NEWS_PREFERENCES)
    }

    @Provides
    @Singleton
    fun provideUserPrefRepository(
        dataStore: DataStore<Preferences>
    ): UserPrefRepository = UserPrefRepositoryImpl(dataStore)
}