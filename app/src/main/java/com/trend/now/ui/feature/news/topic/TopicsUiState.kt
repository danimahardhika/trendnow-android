package com.trend.now.ui.feature.news.topic

import com.trend.now.data.model.Topic

data class TopicsUiState(
    val topics: List<Topic> = listOf(),
    val selectedTopic: String = ""
)