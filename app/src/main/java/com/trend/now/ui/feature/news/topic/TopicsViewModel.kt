package com.trend.now.ui.feature.news.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.Topic
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.UserPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicsUiState())
    val uiState: StateFlow<TopicsUiState> = _uiState

    private var loading: Boolean = false

    init {
        viewModelScope.launch {
            // wait until we got the selected topic value
            userPrefRepository.selectedTopic
                .filter { it.isNotBlank() }
                // observe only for one time, stop after got the first emit.
                .take(1)
                .collect {
                    // fetch the news topics when the selected topic value is ready
                    fetchTopics()
                }
        }

        viewModelScope.launch {
            // observe the selected topic in datastore
            userPrefRepository.selectedTopic
                .filter { it.isNotBlank() }
                // skip the initial value because already handled above
                .drop(1)
                // skip when the value is identical
                .distinctUntilChanged()
                .collect { topic ->
                    // update the ui state selected topic value
                    _uiState.update {
                        _uiState.value.copy(selectedTopic = topic)
                    }
                }
        }
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
                _uiState.update {
                    _uiState.value.copy(
                        topics = result.data,
                        selectedTopic = userPrefRepository.selectedTopic.first()
                    )
                }
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
     * @return The index of specific topic from [TopicsUiState.topics]
     */
    fun indexOfTopic(topicId: String): Int {
        return _uiState.value.topics.indexOfFirst { it.id == topicId }
            .takeIf { it >= 0 } ?: 0
    }
}