package com.trend.now

import com.trend.now.data.repository.UserPrefRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// use fake instead of mock to simulate realistic behavior of datastore
class FakeUserPrefRepository : UserPrefRepository {
    private val _selectedTopic = MutableStateFlow(DEFAULT_TOPIC)
    private val _newsCountry = MutableStateFlow(DEFAULT_COUNTRY)
    private val _newsLanguage = MutableStateFlow(DEFAULT_LANGUAGE)
    private val _showOnBoarding = MutableStateFlow(true)
    private val _localNewsEnabled = MutableStateFlow(false)

    override val selectedTopic: Flow<String> get() = _selectedTopic

    override suspend fun setSelectedTopic(topicId: String) {
        _selectedTopic.value = topicId
    }

    override val newsCountry: Flow<String> get() = _newsCountry

    override suspend fun setNewsCountry(country: String) {
        _newsCountry.value = country
    }

    override val newsLanguage: Flow<String> get() = _newsLanguage

    override suspend fun setNewsLanguage(language: String) {
        _newsLanguage.value = language
    }

    override val isShowOnBoarding: Flow<Boolean> get() = _showOnBoarding

    override suspend fun setShowOnBoarding(show: Boolean) {
        _showOnBoarding.value = show
    }

    override val isLocalNewsEnabled: Flow<Boolean> get() = _localNewsEnabled

    override suspend fun setLocalNewsEnabled(enabled: Boolean) {
        _localNewsEnabled.value = enabled
    }
}