package com.trend.now.ui.navigation

sealed class AppRoute(val path: String) {
    data object News : AppRoute("home")
    data object OnBoarding : AppRoute("onboarding")
    data object LocalNewsSettings : AppRoute("local_news_settings")
    data object CountrySettings : AppRoute("country_settings")
    data object LanguageSettings : AppRoute("language_settings")
}