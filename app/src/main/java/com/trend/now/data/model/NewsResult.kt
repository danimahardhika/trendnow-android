package com.trend.now.data.model

data class NewsResult(
    val data: List<News>,
    val fromCache: Boolean,
    val url: String
)