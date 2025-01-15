package com.trend.now.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.data.repository.UserPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val showOnBoarding: Boolean = false,
    val loading: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    userPrefRepository: UserPrefRepository
) : ViewModel() {

    // add the loading state as workaround to prevent visual glitch
    // when the datastore actual value is different with the default value
    val mainUiState: StateFlow<MainUiState> = userPrefRepository.isShowOnBoarding
        .map { MainUiState(showOnBoarding = it, loading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MainUiState(loading = true)
        )
}