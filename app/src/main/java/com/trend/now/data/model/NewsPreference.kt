package com.trend.now.data.model

data class NewsPreference(
    val language: String = "",
    val country: String = ""
) {

    /**
     * @return Whether the [language] and [country] is not blank or blank
     */
    fun isNotBlank(): Boolean = language.isNotBlank() && country.isNotBlank()
}