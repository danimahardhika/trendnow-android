package com.trend.now.ui.feature.news

import com.trend.now.data.model.News

data class NewsUiState(
    val data: List<News> = listOf(),
    val success: Boolean = false,
    val loading: Boolean = true,
    val showLoadMore: Boolean = false,
    val refreshing: Boolean = false,
    val message: String = "",
    val page: Int = -1
)