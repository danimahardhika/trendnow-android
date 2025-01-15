package com.trend.now.ui.feature.localnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.core.ui.state.UiState
import com.trend.now.data.model.NewsPreference
import com.trend.now.data.repository.UserPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalNewsSettingsViewModel @Inject constructor(
    userPrefRepository: UserPrefRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<NewsPreference>> = combine(
        userPrefRepository.newsLanguage,
        userPrefRepository.newsCountry
    ) { language, country ->
        UiState.Success(NewsPreference(language = language, country = country))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UiState.Loading
    )
}