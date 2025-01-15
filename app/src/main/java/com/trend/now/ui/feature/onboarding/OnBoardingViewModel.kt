package com.trend.now.ui.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trend.now.DEFAULT_COUNTRY
import com.trend.now.DEFAULT_LANGUAGE
import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.Country
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : ViewModel() {

    private var countries: Map<String, Country> = mapOf()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            fetchSupportedCountries()
        }
    }

    fun setLoading(loading: Boolean) = viewModelScope.launch {
        _loading.value = loading
    }

    fun setupLocalNews(
        language: String,
        country: String = ""
    ) {
        _loading.value = true

        viewModelScope.launch {
            // countries empty means probably there's an issue
            // when fetching the supported countries
            if (countries.isEmpty() || (language.isBlank() && country.isBlank())) {
                userPrefRepository.setNewsLanguage(DEFAULT_LANGUAGE)
                userPrefRepository.setNewsCountry(DEFAULT_COUNTRY)
                userPrefRepository.setShowOnBoarding(false)
                _loading.value = false
                return@launch
            }

            // try to check whether the user country is supported or not
            val userCountry = countries[country]
            // try to check whether the user device language is supported or not
            val userLanguage = userCountry?.languages?.find {
                it.code == language
            } ?: run {
                // if user device language is not supported based on the user country
                // take the first country supported language
                userCountry?.languages?.firstOrNull()
            }
            if (userCountry != null && userLanguage != null) {
                userPrefRepository.setNewsLanguage(userLanguage.code)
                userPrefRepository.setNewsCountry(userCountry.code)
                // local news has been setup correctly and enabled
                userPrefRepository.setLocalNewsEnabled(true)
            } else {
                // use default settings if user country is not supported
                userPrefRepository.setNewsLanguage(DEFAULT_LANGUAGE)
                userPrefRepository.setNewsCountry(DEFAULT_COUNTRY)
                // local news is not enabled when using default language
                userPrefRepository.setLocalNewsEnabled(false)
            }
            userPrefRepository.setShowOnBoarding(false)
            _loading.value = false
        }
    }

    private fun fetchSupportedCountries() = viewModelScope.launch {
        val result = newsRepository.fetchSupportedCountries()
        // fetchSupportedCountries get the data from a file inside assets directory
        // add delay to simulate real api call for the sake of animation :)
        delay(2000)
        if (result is ApiResult.Success) {
            countries = result.data
        } else {
            // TODO: handle when failed to get supported countries
        }
        _loading.value = false
    }
}