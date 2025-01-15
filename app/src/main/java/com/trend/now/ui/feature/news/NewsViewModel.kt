package com.trend.now.ui.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.core.network.ApiResult
import com.trend.now.core.ui.state.UiState
import com.trend.now.data.model.NewsPreference
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository,
) : ViewModel() {

    private val fetchTrendingNewsMutex = Mutex()
    private var trendingNewsJob: Job? = null
    private val _trendingNewsUiState = MutableStateFlow(NewsUiState(loading = true))
    val trendingNewsUiState: StateFlow<NewsUiState> = _trendingNewsUiState

    // observe the selected topic flow in user pref repository from a datastore
    val selectedTopic = userPrefRepository.selectedTopic
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            // use empty string as default value
            initialValue = ""
        )

    // observe the local news ui state
    // based on news language and country from datastore
    val localNewsUiState: StateFlow<UiState<NewsPreference>> = combine(
        userPrefRepository.newsLanguage,
        userPrefRepository.newsCountry
    ) { language, country ->
        UiState.Success(NewsPreference(language = language, country = country))
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState.Loading
        )

    // use different variable for loadingMore
    // because we don't want to trigger recomposition in the screen
    private var loadingMore: Boolean = false

    init {
        viewModelScope.launch {
            combine(
                selectedTopic,
                localNewsUiState
            ) { topic, uiState ->
                Pair(topic, uiState)
            }.filter { it.first.isNotBlank() && it.second !is UiState.Loading }
                .distinctUntilChanged { old, new ->
                    val oldSuccess = old.second as? UiState.Success
                    val newSuccess = new.second as? UiState.Success
                    // compare the old and new value
                    // make sure the state flow does not trigger emits with the same value
                    old.first != new.first && oldSuccess?.data != newSuccess?.data
                }
                .collect {
                    // fetch trending news each time the selected topic or news preference changed
                    fetchTrendingNews()
                }
        }
    }

    fun fetchTrendingNews() {
        if (!_trendingNewsUiState.value.loading) {
            _trendingNewsUiState.value = _trendingNewsUiState.value.copy(
                loading = true
            )
        }
        internalFetchTrendingNews()
    }

    fun loadMoreTrendingNews() {
        if (loadingMore) {
            // immediately return when loading more is still running
            // to prevent load wrong page when called multiple times
            return
        }
        if (!_trendingNewsUiState.value.showLoadMore) {
            // reached at the end of the page no need to load more data
            return
        }
        loadingMore = true
        internalFetchTrendingNews {
            loadingMore = false
        }
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
                        val language = userPrefRepository.newsLanguage.first()
                        val country = userPrefRepository.newsCountry.first()
                        val result = newsRepository.fetchTrendingNews(
                            topic = selectedTopic.value,
                            language = language,
                            country = country,
                            page = if (loadingMore) {
                                // set the page value only when loading more
                                _trendingNewsUiState.value.page + 1
                            } else {
                                // otherwise set to null
                                null
                            }
                        )

                        val data = _trendingNewsUiState.value.data
                        when (result) {
                            is ApiResult.Success -> {
                                val meta = result.meta
                                _trendingNewsUiState.value =
                                    _trendingNewsUiState.value.copy(
                                        data = if (loadingMore) {
                                            // add the new loaded trending news to the existing list
                                            // if the result comes from loading more
                                            data.plus(result.data)
                                        } else {
                                            result.data
                                        },
                                        loading = false,
                                        refreshing = false,
                                        success = true,
                                        // load more only when the current page < total page
                                        // got from the api response
                                        showLoadMore =
                                            // whether should load more or note
                                            // when reached the bottom of the trending news list
                                            meta.page > 0 && meta.page < meta.totalPages,
                                        page = meta.page // save the current page
                                    )
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