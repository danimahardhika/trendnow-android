package com.trend.now.data.model

data class Country(
    val name: String = "",
    val code: String = "",
    val languages: List<Language> = listOf()
)

data class Language(
    val name: String = "",
    val code: String = ""
)