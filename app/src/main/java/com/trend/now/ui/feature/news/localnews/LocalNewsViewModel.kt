package com.trend.now.ui.feature.news.localnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.data.model.NewsPreference
import com.trend.now.data.repository.UserPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalNewsViewModel @Inject constructor(
    userPrefRepository: UserPrefRepository
) : ViewModel() {

    val newsPreference: StateFlow<NewsPreference> = combine(
        userPrefRepository.newsCountry,
        userPrefRepository.newsLanguage
    ) { country, language ->
        NewsPreference(country = country, language = language)
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = NewsPreference()
        )
}