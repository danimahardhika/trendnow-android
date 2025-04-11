package com.trend.now.ui.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.NewsPreference
import com.trend.now.data.paging.NewsPagingSource
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : ViewModel() {

    private val fetchTrendingNewsMutex = Mutex()
    private var trendingNewsJob: Job? = null
    private val _trendingNewsUiState = MutableStateFlow(NewsUiState(loading = true))
    val trendingNewsUiState: StateFlow<NewsUiState> = _trendingNewsUiState

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
        viewModelScope.launch {
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
            }.collect {
                // fetch trending news each time the selected topic or news preference changed
                // fetchTrendingNews()
                // TODO: handle paging refresh data
            }
        }
    }

    fun fetchTrendingNews() {
        _trendingNewsUiState.update {
            _trendingNewsUiState.value.copy(loading = true)
        }
        internalFetchTrendingNews()
    }

    fun onPullToRefresh() {
        if (_trendingNewsUiState.value.refreshing) return

        _trendingNewsUiState.value = _trendingNewsUiState.value.copy(
            refreshing = true
        )
        viewModelScope.launch {
            // when the fetch trending news takes data from cache
            // it runs quite fast and sometimes there's a glitch on the pull to refresh indicator
            // use delay as a workaround to fix the glitch
            delay(300)

            internalFetchTrendingNews()
        }
    }

    private fun internalFetchTrendingNews(onFetchDone: () -> Unit = {}) {
        // cancel current job
        trendingNewsJob?.cancel()

        viewModelScope.launch {
            fetchTrendingNewsMutex.withLock {
                trendingNewsJob = launch {
                    try {
                        val result = newsRepository.fetchTrendingNews(
                            // take the value directly from the datastore
                            topic = userPrefRepository.selectedTopic.first(),
                            language = userPrefRepository.newsLanguage.first(),
                            country = userPrefRepository.newsCountry.first(),
                            page = null
                        )

                        when (result) {
                            is ApiResult.Success -> {
                                val meta = result.meta
                                _trendingNewsUiState.update {
                                    _trendingNewsUiState.value.copy(
                                        data = result.data,
                                        loading = false,
                                        refreshing = false,
                                        success = true,
                                        // load more only when the current page < total page
                                        // got from the api response
                                        showLoadMore =
                                            // whether should load more or not
                                            // when reached the bottom of the trending news list
                                            meta.page > 0 && meta.page < meta.totalPages,
                                        page = meta.page // save the current page
                                    )
                                }
                            }
                            is ApiResult.Error -> _trendingNewsUiState.value =
                                _trendingNewsUiState.value.copy(
                                    success = false,
                                    loading = false,
                                    refreshing = false,
                                    message = result.message,
                                )
                        }
                        onFetchDone()
                    } finally {
                        fetchTrendingNewsMutex.withLock { trendingNewsJob = null }
                    }
                }
            }
        }
    }
}