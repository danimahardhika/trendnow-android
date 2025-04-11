package com.trend.now.ui.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.trend.now.core.util.paging.PagingEvent
import com.trend.now.data.model.NewsPreference
import com.trend.now.data.paging.NewsPagingSource
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : ViewModel() {

    // the pull to refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _pagingEvent = MutableSharedFlow<PagingEvent>()
    val pagingEvent: SharedFlow<PagingEvent> = _pagingEvent

    val newsPaging = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            prefetchDistance = 1,
            maxSize = 100
        ),
        pagingSourceFactory = {
            NewsPagingSource(
                newsRepository = newsRepository,
                userPrefRepository = userPrefRepository
            )
        }
    ).flow.cachedIn(viewModelScope)

    init {
        // observe the selected topic, country, and language in datastore
        // because trending news depends on them
        combine(
            userPrefRepository.selectedTopic,
            userPrefRepository.newsCountry,
            userPrefRepository.newsLanguage
        ) { topic, country, language ->
            Pair(topic, NewsPreference(country = country, language = language))
        }.filter {
            it.first.isNotBlank()
                && it.second.language.isNotBlank()
                && it.second.country.isNotBlank()
        }.distinctUntilChanged { old, new ->
            // compare the old and new value
            // make sure the state flow does not trigger emits with the same value
            old.first == new.first && old.second == new.second
        }
            // drop 1 to skip trigger paging event emit for the first load
            // to prevent redundant load with the newsPaging above
            .drop(1)
            .onEach {
                // trigger paging actions reload to fetch the trending news again
                _pagingEvent.emit(PagingEvent.RELOAD)
            }.launchIn(viewModelScope)
    }

    /**
     * Update the [isRefreshing] ui state
     */
    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    /**
     * Trigger [PagingEvent.RETRY]
     */
    fun onRetryFetch() = viewModelScope.launch {
        _pagingEvent.emit(PagingEvent.RETRY)
    }

    /**
     * Trigger [PagingEvent.REFRESH]
     */
    fun onPullToRefresh() = viewModelScope.launch {
        _pagingEvent.emit(PagingEvent.REFRESH)
    }
}