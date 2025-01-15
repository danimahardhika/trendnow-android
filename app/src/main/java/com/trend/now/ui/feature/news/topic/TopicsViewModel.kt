package com.trend.now.ui.feature.news.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.Topic
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : ViewModel() {

    // not using the ui state here
    // to keep the topics data that has been loaded being displayed
    // when fetch topics is running or fetch topics is failed
    private val _topics = MutableStateFlow<List<Topic>>(listOf())
    val topics: StateFlow<List<Topic>> = _topics

    private var loading: Boolean = false

    init {
        // fetch the news topics when the viewmodel initialized
        fetchTopics()
    }

    /**
     * Fetch news topics
     * Will use cached topics if the cache still valid
     */
    fun fetchTopics() {
        if (loading) return

        loading = true
        viewModelScope.launch {
            val result = newsRepository.fetchSupportedTopics()
            if (result is ApiResult.Success) {
                _topics.value = result.data
            }
            loading = false
        }
    }

    /**
     * Save and update the selected topic
     * @param topicId The topic id from [Topic.id]
     */
    fun selectTopic(topicId: String) = viewModelScope.launch {
        userPrefRepository.setSelectedTopic(topicId)
    }

    /**
     * @param topicId The topic id from [Topic.id]
     * @return The index of specific topic from [topics]
     */
    fun indexOfTopic(topicId: String): Int {
        return _topics.value.indexOfFirst { it.id == topicId }
            .takeIf { it >= 0 } ?: 0
    }
}